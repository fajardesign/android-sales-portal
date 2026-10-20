package com.example.and_sales_portal.data.partner

import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.demo.DemoStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

/** Partner pipeline of the prototype (REG, PRT; PRD v3 A2, C2–C4, D4). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PartnerRepositoryTest {

    private val store = DemoStore(ApplicationProvider.getApplicationContext())
    private val now = Instant.parse("2026-10-08T03:00:00Z")
    private lateinit var repo: PartnerRepository

    @Before
    fun setUp() {
        repo = PartnerRepository(store) { now }
        repo.reset()
    }

    /** A toko added to an Active partner, active after the Admin fills its code and QR, given to Andi by the APL. */
    private fun newStore(partnerId: String, name: String): Store {
        val s = repo.addStore(partnerId, repo.get(partnerId)!!.stores.first().copy(name = name, primary = false, assigned = emptyList()))
        repo.adminFillReferral(partnerId)
        assertTrue(repo.aplAssignTl(s.id, 6, APL))
        return repo.get(partnerId)!!.stores.first { it.id == s.id }
    }

    private fun store(id: String) = repo.partners.value.flatMap { it.stores }.first { it.id == id }

    @Test
    fun sampleMatchesThePortal() {
        val all = repo.partners.value
        assertEquals(18, all.size)
        assertEquals(PartnerStatus.ACTIVE, repo.get("REG2026-0139")?.status)
        assertEquals("TK0139-02", repo.get("REG2026-0139")?.stores?.get(1)?.code)
        assertEquals(PartnerStatus.REVISION_REQUIRED, repo.get("REG2026-0144")?.status)
        assertEquals(PartnerStatus.INACTIVE, repo.get("REG2026-0133")?.status)
        // C6: kursi only after activation; Inactive released them; Antang and Tamalanrea without TL (web `db.js`).
        assertNull(repo.get("REG2026-0146")?.stores?.first()?.tlId)
        assertTrue(repo.get("REG2026-0133")!!.stores.all { it.tlId == null && it.assigned.isEmpty() })
        assertEquals(10L, repo.get("REG2026-0133")!!.stores.first().tlAt(repo.get("REG2026-0133")!!.statusUpdatedAt - 1))
        assertNull(store("REG2026-0139-4").tlId)
        val tamalanrea = store("REG2026-0132-2")
        assertNull(tamalanrea.tlId)
        assertTrue(tamalanrea.assigned.isEmpty())
        assertEquals("dilepas APL", tamalanrea.tlLog.single().source)
        // Loans before the release stay with Andi and Siti.
        val before = tamalanrea.tlLog.single().at - 1
        assertEquals(6L, tamalanrea.tlAt(before))
        assertEquals(11L, repo.holderAt(tamalanrea.id, before))
        // Kode Referral and QR per toko.
        assertEquals("qr_ofmks1391.png", store("REG2026-0139-1").referralQr)
    }

    @Test
    fun visibilityPerRole() {
        val tl = repo.submissionsFor(6, SalesRole.TL).map { it.id }.toSet()
        assertEquals(setOf("REG2026-0148", "REG2026-0146", "REG2026-0142", "REG2026-0139", "REG2026-0132", "REG2026-0135"), tl)
        assertEquals(setOf("REG2026-0146", "REG2026-0135"), repo.submissionsFor(11, SalesRole.SR).map { it.id }.toSet())
        assertTrue(repo.submissionsFor(15, SalesRole.SA).isEmpty())
        // FR-PA-012: Partner saya shows only the toko of the user's kursi.
        val mine = repo.myPartners(6, SalesRole.TL)
        assertEquals(listOf("REG2026-0139", "REG2026-0132"), mine.map { it.id })
        assertEquals(listOf("REG2026-0139-1", "REG2026-0139-2", "REG2026-0139-3"), mine[0].stores.map { it.id })
        assertEquals(listOf("REG2026-0132-1"), mine[1].stores.map { it.id })
        assertEquals(listOf("REG2026-0139-1"), repo.myPartners(15, SalesRole.SA).single().stores.map { it.id })
        // Bagus (22) holds no kursi; he sees Antang, the toko he asked for, while it waits for its code (C7).
        assertEquals(listOf("REG2026-0139-4"), repo.myPartners(22, SalesRole.TL).single().stores.map { it.id })
    }

    @Test
    fun submitGivesTheNextNumberAndUnderReview() {
        val p = repo.submit(repo.get("REG2026-0148")!!.copy(id = "", name = "Toko Baru", submittedBy = 11, submittedAt = now.toEpochMilli()))
        assertEquals("REG2026-0149", p.id)
        // No Kode Referral from the app; no kursi until activation (C6), but Siti's TL sees the submission.
        assertNull(p.stores.first().referralCode)
        assertNull(p.stores.first().tlId)
        assertEquals(PartnerStatus.UNDER_REVIEW, p.status)
        assertEquals("REG2026-0149-1", p.stores.first().id)
        assertTrue(p.id in repo.submissionsFor(6, SalesRole.TL).map { it.id })
    }

    @Test
    fun revisionLoop() {
        repo.adminRequestRevision("REG2026-0148")
        val flagged = repo.get("REG2026-0148")!!
        assertEquals(PartnerStatus.REVISION_REQUIRED, flagged.status)
        assertEquals(2, flagged.revision?.items?.size)
        repo.resubmit(flagged.copy(accountNumber = "999"), 6)
        val back = repo.get("REG2026-0148")!!
        assertEquals(PartnerStatus.UNDER_REVIEW, back.status)
        assertNull(back.revision)
        assertEquals("999", back.accountNumber)
    }

    @Test
    fun activationFillsTheKursiTlOfTheSubmittersTl() {
        // REG2026-0146 was submitted by SR Siti: at activation the kursi TL goes to her TL Andi, the kursi SA/SR stays empty.
        listOf<(String) -> Unit>(repo::adminVerify, repo::adminSendPks).forEach { it("REG2026-0146") }
        // FR-010: Aktifkan is locked until every toko has its Kode Referral and QR.
        assertTrue(repo.referralMissing("REG2026-0146"))
        assertFalse(repo.adminActivate("REG2026-0146"))
        assertEquals(PartnerStatus.WAITING_PKS, repo.get("REG2026-0146")!!.status)
        assertEquals(1, repo.adminFillReferral("REG2026-0146"))
        assertTrue(repo.adminActivate("REG2026-0146"))
        val s = repo.get("REG2026-0146")!!.stores.single()
        assertEquals("TK0146-01", s.code)
        assertEquals("qr_ofmks1461.png", s.referralQr)
        assertEquals(6L, s.tlId)
        assertTrue(s.assigned.isEmpty())
        assertEquals("aktivasi", s.tlLog.single().source)
    }

    @Test
    fun activationWithoutAnActiveTlLeavesBelumAdaTl() {
        // Siti's TL Andi disabled before activation: "Belum ada TL" until the APL assigns one.
        DemoDirectory.changes = mapOf(6L to DemoDirectory.Change(status = "DISABLED"))
        try {
            listOf<(String) -> Unit>(repo::adminVerify, repo::adminSendPks, { repo.adminFillReferral(it) }).forEach { it("REG2026-0146") }
            assertTrue(repo.adminActivate("REG2026-0146"))
            assertNull(repo.get("REG2026-0146")!!.stores.single().tlId)
        } finally {
            DemoDirectory.changes = emptyMap()
        }
    }

    @Test
    fun tambahTokoStartsWithoutKursi() {
        // FR-PA-013: TL with a kursi TL in the partner, SR with a kursi SA/SR; Bagus and Eko (SA) cannot add.
        val p = repo.get("REG2026-0139")!!
        assertTrue(repo.canAddStore(p, 6, SalesRole.TL))
        assertTrue(repo.canAddStore(p, 11, SalesRole.SR))
        assertFalse(repo.canAddStore(p, 22, SalesRole.TL))
        assertFalse(repo.canAddStore(p, 15, SalesRole.SA))
        assertFalse(repo.canAddStore(repo.get("REG2026-0138")!!, 6, SalesRole.TL))
        // Added by TL Andi: no kursi TL nor SA/SR, waiting for its Kode Referral and QR.
        val s = repo.addStore(p.id, p.stores[2].copy(name = "Cabang Baru", primary = false, addedBy = 6))
        assertEquals(Store.WAITING_REFERRAL, s.status)
        assertNull(s.code)
        assertNull(s.referralCode)
        assertNull(s.tlId)
        assertTrue(s.assigned.isEmpty()) // never inherits the SA/SR of the toko it was copied from
        assertEquals(AssignError.NotActive, repo.assignmentError(6, 11, s.id))
        assertTrue(repo.activeStores().none { it.second.id == s.id })
        // "Isi kode referral dan QR": Active with a toko code and "Belum ada TL"; Andi still cannot assign it.
        assertEquals(2, repo.adminFillReferral(p.id)) // with Antang
        val filled = store(s.id)
        assertEquals(Store.ACTIVE, filled.status)
        assertEquals("TK0139-05", filled.code)
        assertEquals("OFMKS1395", filled.referralCode)
        assertEquals("qr_ofmks1395.png", filled.referralQr)
        assertNull(filled.tlId)
        assertEquals(AssignError.NotHolder, repo.assignmentError(6, 11, s.id))
        // Once the APL gives it to Andi, Siti can be assigned.
        assertTrue(repo.aplAssignTl(s.id, 6, APL))
        assertNull(repo.assign(6, 11, s.id))
    }

    @Test
    fun picPhoneIsUniquePerPartner() {
        // Review 2026-10-09: No. Handphone PIC signs in to the Partner account (Kode Referral is no longer asked).
        val phone = repo.get("REG2026-0148")!!.picPhone
        assertTrue(repo.picPhoneTaken("0$phone"))
        assertTrue(repo.picPhoneTaken("+62 $phone"))
        assertFalse(repo.picPhoneTaken(phone, exceptId = "REG2026-0148"))
        assertEquals("81200047514", PartnerRepository.normalizePhone("+62 812-0004-7514"))
    }

    @Test
    fun assignmentHistoryAndNotifications() {
        // FR-MP-007: every Tugaskan and Lepas logged with actor, time, toko, partner and SA/SR.
        val free = newStore("REG2026-0139", "Cabang Baru")
        val before = repo.historyOf(11).size // the sample release of Tamalanrea
        assertNull(repo.assign(6, 11, free.id))
        repo.unassign(11, free.id, 6)
        val history = repo.historyOf(11)
        assertEquals(listOf(AssignmentAction.RELEASE, AssignmentAction.ASSIGN), history.take(2).map { it.action })
        assertEquals(free.id, history.first().storeId)
        assertEquals("Jaya Abadi Cellular", history.first().partnerName)
        assertEquals("6", history.first().by)
        assertEquals("dilepas Andi Pratama", history.first().source)
        // Lepas of a toko that is not assigned does nothing.
        repo.unassign(11, free.id, 6)
        assertEquals(before + 2, repo.historyOf(11).size)
        // FR-MP-004: the SA/SR gets an in-app notification per event; opening the inbox marks them read.
        assertEquals(2, repo.notificationsOf(11).count { !it.read })
        assertTrue(repo.notificationsOf(15).isEmpty())
        repo.markNotificationsRead(11)
        assertTrue(repo.notificationsOf(11).all { it.read })
    }

    @Test
    fun notificationsAreKeptThirtyDaysAndAllReadWhenTheListOpens() {
        // Designer Q3 / PRD 1.1 AC-014: the sample has an unread one older than 30 days for Siti; it is not shown.
        val all = repo.assignments.value.notifications.filter { it.employeeId == 11L }
        assertTrue(all.any { now.toEpochMilli() - it.at > 30L * 86_400_000 })
        val shown = repo.notificationsOf(11)
        assertTrue(shown.all { now.toEpochMilli() - it.at <= 30L * 86_400_000 })
        assertEquals(all.size - 1, shown.size)
        // Two new ones ("Baru"); opening the list marks them all read.
        assertNull(repo.assign(6, 11, newStore("REG2026-0139", "Cabang Baru").id))
        assertNull(repo.assign(6, 11, newStore("REG2026-0139", "Cabang Baru 2").id))
        assertEquals(2, repo.notificationsOf(11).count { !it.read })
        repo.markNotificationsRead(11)
        assertTrue(repo.notificationsOf(11).all { it.read })
    }

    @Test
    fun cancelledStoreRequestNotifiesTheAdderAndStaysAsHistory() {
        // C7 FR-023 (decision 2026-10-20): Siti adds a toko; the Admin cancels it with a reason.
        val p = repo.get("REG2026-0139")!!
        val s = repo.addStore(p.id, p.stores[2].copy(name = "Cabang Baru", primary = false, addedBy = 11))
        assertTrue(s.id in repo.storesOf(repo.get(p.id)!!, 11, SalesRole.SR).map { it.id }) // her request, while it waits
        assertTrue(!repo.adminCancelStoreRequest(s.id, " "))
        assertTrue(repo.adminCancelStoreRequest(s.id, "Lokasi toko tidak sesuai"))
        val cancelled = store(s.id)
        assertEquals(Store.CANCELLED, cancelled.status)
        assertEquals("Lokasi toko tidak sesuai", cancelled.cancelReason)
        val n = repo.notificationsOf(11).first().storeCancelled!!
        assertEquals(s.name, n.storeName)
        assertEquals("Lokasi toko tidak sesuai", n.reason)
        // History in her Partner saya, never assigned or visited; cannot be cancelled twice or filled.
        assertTrue(s.id in repo.myPartners(11, SalesRole.SR).first { it.id == p.id }.stores.map { it.id })
        assertTrue(repo.activeStores().none { it.second.id == s.id })
        assertEquals(AssignError.NotActive, repo.assignmentError(6, 11, s.id))
        assertTrue(!repo.adminCancelStoreRequest(s.id, "lagi"))
        // A partner going Inactive cancels the toko still waiting (Antang), source "partner nonaktif".
        repo.adminDeactivate(p.id)
        assertEquals(Store.CANCELLED, store("REG2026-0139-4").status)
        assertEquals("partner nonaktif", store("REG2026-0139-4").cancelReason)
    }

    @Test
    fun aStoreNoLongerHeldIsKnown() {
        // AC-014: tapping a notification about a toko Siti no longer holds opens the partner with "Anda tidak lagi ditugaskan".
        val sudiang = store("REG2026-0139-3")
        assertTrue(repo.holds(sudiang, 11, SalesRole.SR))
        repo.unassign(11, sudiang.id, 6)
        assertTrue(!repo.holds(store(sudiang.id), 11, SalesRole.SR))
        assertTrue(repo.notificationsOf(11).first().event!!.storeId == sudiang.id)
    }

    @Test
    fun partnerInactiveReleasesEveryKursi() {
        // C6 FR-013: kursi TL and SA/SR released, logged as the system; the TL is told who was released with them.
        repo.adminDeactivate("REG2026-0132")
        assertTrue(repo.get("REG2026-0132")!!.stores.all { it.assigned.isEmpty() && it.tlId == null })
        val nurul = repo.historyOf(16).first()
        assertEquals(AssignmentEvent.SYSTEM, nurul.by)
        assertEquals("partner nonaktif", nurul.source)
        assertEquals(1, repo.notificationsOf(16).count { !it.read })
        val andi = repo.notificationsOf(6).first { !it.read }.event!!
        assertEquals(AssignmentEvent.TL, andi.seat)
        assertEquals(listOf("Nurul Hidayah"), andi.withSales)
        assertTrue(repo.myPartners(6, SalesRole.TL).none { it.id == "REG2026-0132" })
    }

    @Test
    fun ubahAreaReleasesEveryKursi() {
        // C6 FR-021: Under Review up to Active; every kursi released ("ubah area"), the partner moves area.
        assertFalse(repo.canChangeArea("REG2026-0135")) // Rejected
        val area = repo.get("REG2026-0139")!!.areaId
        assertEquals("Bandung", repo.adminChangeArea("REG2026-0139"))
        val p = repo.get("REG2026-0139")!!
        assertTrue(area != p.areaId)
        assertTrue(p.stores.all { it.tlId == null && it.assigned.isEmpty() })
        assertEquals("ubah area", repo.historyOf(15).first().source)
        assertTrue(repo.myPartners(6, SalesRole.TL).none { it.id == p.id })
    }

    @Test
    fun assignmentRulesWithoutLimits() {
        // C6: no limit of toko per SA/SR. One toko = one SA/SR; the holder is named in the message.
        assertEquals(1, repo.assignedStores(15).size)
        assertEquals(2, repo.assignedStores(11).size) // Tamalanrea released with its TL
        assertEquals(AssignError.Taken, repo.assignmentError(6, 11, "REG2026-0139-1"))
        assertEquals(15L, repo.holderOf("REG2026-0139-1"))
        assertEquals(AssignError.Taken, repo.assign(6, 11, "REG2026-0139-1"))
        // An SA takes 8 toko and an SR 8 more: nothing is refused.
        val p = repo.get("REG2026-0139")!!
        val extra = (1..16).map { newStore(p.id, "Cabang Baru $it") }
        extra.take(8).forEach { assertNull(repo.assign(6, 15, it.id)) }
        extra.drop(8).forEach { assertNull(repo.assign(6, 11, it.id)) }
        assertEquals(9, repo.assignedStores(15).size)
        assertEquals(10, repo.assignedStores(11).size)
        // 1.5 FR-MP-002 reasons.
        assertEquals(AssignError.NotTeam, repo.assignmentError(7, 11, extra[0].id))
        assertEquals(AssignError.NotHolder, repo.assignmentError(6, 16, "REG2026-0132-2")) // Tamalanrea: no TL
        assertEquals(AssignError.NotActive, repo.assignmentError(6, 16, "REG2026-0148-1")) // still Under Review
        // Lepas frees the toko for someone else.
        repo.unassign(15, extra[0].id, 6)
        assertNull(repo.assignmentError(6, 16, extra[0].id))
    }

    @Test
    fun tlIsSavedPerTokoAndMovesWithItsHistory() {
        // C6 + ACC-05: the kursi TL of a disabled TL moves to the replacement; loans keep the TL of their date.
        val before = now.toEpochMilli() - 1
        val andi = repo.activeStores().count { it.second.tlId == 6L }
        assertEquals(4, andi)
        assertEquals(andi, repo.transferStores(6, 22))
        assertEquals(0, repo.activeStores().count { it.second.tlId == 6L })
        val moved = store("REG2026-0139-1")
        assertEquals(22L, moved.tlId)
        assertEquals(6L, moved.tlAt(before))
        assertEquals(22L, moved.tlAt(now.toEpochMilli()))
        assertTrue(repo.myPartners(6, SalesRole.TL).isEmpty())
        // A toko the old TL did not hold stays as it was; no TL notification on a handover (decision 2026-10-17).
        assertEquals(7L, store("REG2026-0138-1").tlId)
        assertTrue(repo.notificationsOf(22).isEmpty())
    }

    @Test
    fun aplAssignAndReleaseTl() {
        // 1.7 FR-004: Tamalanrea (no TL) to Bagus; the TL is notified "oleh {APL}".
        assertTrue(repo.aplAssignTl("REG2026-0132-2", 22, APL))
        assertEquals(22L, store("REG2026-0132-2").tlId)
        val assigned = repo.notificationsOf(22).single().event!!
        assertEquals(AssignmentEvent.TL, assigned.seat)
        assertEquals(AssignmentAction.ASSIGN, assigned.action)
        assertEquals(APL, assigned.by)
        // Only toko without TL, and a TL of the partner's area.
        assertFalse(repo.aplAssignTl("REG2026-0132-2", 6, APL))
        assertFalse(repo.aplAssignTl("REG2026-0139-4", 22, APL)) // waiting for its code
        // FR-005: Lepas TL releases the SA/SR too ("TL dilepas"); Andi is told who was released with him.
        assertEquals(listOf(11L), repo.aplReleaseTl("REG2026-0139-3", APL))
        val sudiang = store("REG2026-0139-3")
        assertNull(sudiang.tlId)
        assertTrue(sudiang.assigned.isEmpty())
        val siti = repo.historyOf(11).first()
        assertEquals(APL, siti.by)
        assertEquals("TL dilepas", siti.source)
        val andi = repo.notificationsOf(6).first { !it.read }.event!!
        assertEquals(AssignmentEvent.TL, andi.seat)
        assertEquals(listOf("Siti Rahmawati"), andi.withSales)
        assertNull(repo.aplReleaseTl("REG2026-0139-3", APL))
        // Past loans stay with Siti; Andi can no longer assign it.
        assertEquals(11L, repo.holderAt("REG2026-0139-3", now.toEpochMilli() - 1))
        assertEquals(AssignError.NotHolder, repo.assignmentError(6, 11, "REG2026-0139-3"))
    }

    @Test
    fun revisionRequestNotifiesSubmitterAndTheirTl() {
        // DD review point 2: REG2026-0145 was submitted by TL Maya (9): one notification.
        repo.adminRequestRevision("REG2026-0145")
        assertEquals(1, repo.notificationsOf(9).count { it.revision?.partnerId == "REG2026-0145" })
        // REG2026-0146 was submitted by SR Siti (11), whose TL is Andi (6): both are notified.
        repo.adminRequestRevision("REG2026-0146")
        assertEquals(1, repo.notificationsOf(11).count { it.revision?.partnerId == "REG2026-0146" })
        assertEquals(1, repo.notificationsOf(6).count { it.revision?.partnerId == "REG2026-0146" })
    }

    @Test
    fun disabledSalesAccountReleasesItsToko() {
        // DD review point 5: the SA/SR account disabled → every toko released, logged as the system.
        assertEquals(2, repo.releaseAll(11))
        assertTrue(repo.assignedStores(11).isEmpty())
        val e = repo.historyOf(11).first()
        assertEquals(AssignmentEvent.SYSTEM, e.by)
        assertEquals(AssignmentEvent.ACCOUNT_DISABLED, e.reason)
        assertEquals("akun dinonaktifkan", e.source)
        assertEquals(2, repo.notificationsOf(11).count { !it.read })
    }

    private companion object {
        const val APL = "Hasan Basri (APL)"
    }
}
