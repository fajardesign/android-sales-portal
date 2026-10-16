package com.example.and_sales_portal.data.partner

import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.auth.SalesRole
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

    /** A toko added to an Active partner, with its Kode Referral filled by the Admin so it can be assigned. */
    private fun newStore(partnerId: String, name: String): Store {
        val s = repo.addStore(partnerId, repo.get(partnerId)!!.stores.first().copy(name = name, primary = false, assigned = emptyList()))
        repo.adminFillReferral(partnerId)
        return repo.get(partnerId)!!.stores.first { it.id == s.id }
    }

    @Test
    fun sampleMatchesThePortal() {
        val all = repo.partners.value
        assertEquals(18, all.size)
        assertEquals(PartnerStatus.ACTIVE, repo.get("REG2026-0139")?.status)
        assertEquals("TK0139-02", repo.get("REG2026-0139")?.stores?.get(1)?.code)
        assertEquals(PartnerStatus.REVISION_REQUIRED, repo.get("REG2026-0144")?.status)
        assertEquals(PartnerStatus.INACTIVE, repo.get("REG2026-0133")?.status)
        assertEquals(6L, repo.get("REG2026-0146")?.stores?.first()?.tlId) // submitted by SR Siti, TL Andi holds its toko
    }

    @Test
    fun visibilityPerRole() {
        val tl = repo.submissionsFor(6, SalesRole.TL).map { it.id }.toSet()
        assertEquals(setOf("REG2026-0148", "REG2026-0146", "REG2026-0142", "REG2026-0139", "REG2026-0132", "REG2026-0135"), tl)
        assertEquals(setOf("REG2026-0146", "REG2026-0135"), repo.submissionsFor(11, SalesRole.SR).map { it.id }.toSet())
        assertTrue(repo.submissionsFor(15, SalesRole.SA).isEmpty())
        assertEquals(listOf("REG2026-0139", "REG2026-0132"), repo.myPartners(6, SalesRole.TL).map { it.id })
        assertEquals(listOf("REG2026-0139"), repo.myPartners(15, SalesRole.SA).map { it.id })
        // PRD Scope 1 v2: Bagus (22) holds only Jaya Abadi Cellular Antang of REG2026-0139.
        assertEquals(listOf("REG2026-0139"), repo.myPartners(22, SalesRole.TL).map { it.id })
        assertTrue("REG2026-0139" in repo.submissionsFor(22, SalesRole.TL).map { it.id })
    }

    @Test
    fun submitGivesTheNextNumberAndUnderReview() {
        val p = repo.submit(repo.get("REG2026-0148")!!.copy(id = "", name = "Toko Baru", submittedBy = 11, submittedAt = now.toEpochMilli()))
        assertEquals("REG2026-0149", p.id)
        // PRD Scope 1 v2: no Kode Referral from the app (the Admin fills it per toko); the toko goes to Siti's TL.
        assertNull(p.stores.first().referralCode)
        assertEquals(6L, p.stores.first().tlId)
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
    fun activationAndNewStore() {
        repo.adminVerify("REG2026-0148")
        repo.adminSendPks("REG2026-0148")
        // PRD Scope 1 v2 C2: Aktifkan is locked until every toko has its Kode Referral.
        assertTrue(repo.referralMissing("REG2026-0148"))
        assertFalse(repo.adminActivate("REG2026-0148"))
        assertEquals(PartnerStatus.WAITING_PKS, repo.get("REG2026-0148")!!.status)
        assertEquals(1, repo.adminFillReferral("REG2026-0148"))
        assertTrue(repo.adminActivate("REG2026-0148"))
        val p = repo.get("REG2026-0148")!!
        assertEquals(PartnerStatus.ACTIVE, p.status)
        assertEquals("TK0148-01", p.stores.first().code)
        assertEquals("OFMKS1481", p.stores.first().referralCode)
        repo.addStore(p.id, p.stores.first().copy(name = "Sinar Jaya Ponsel Cabang", primary = false))
        assertEquals(2, repo.get(p.id)!!.stores.size)
    }

    @Test
    fun storeAddedToAnActivePartnerWaitsForItsReferralCode() {
        // PRD Scope 1 v2 C2: status "Menunggu kode referral", no toko code; it cannot be assigned until the Admin fills it.
        val s = repo.addStore("REG2026-0139", repo.get("REG2026-0139")!!.stores.first().copy(name = "Cabang Baru", primary = false, assigned = emptyList(), addedBy = 11))
        assertEquals(Store.WAITING_REFERRAL, s.status)
        assertNull(s.code)
        assertNull(s.referralCode)
        assertEquals(6L, s.tlId) // added by SR Siti: her TL
        assertEquals(AssignError.NotOwner, repo.assignmentError(6, 11, s.id))
        assertTrue(repo.activeStores().none { it.second.id == s.id })
        // The sample toko of Bagus waits too.
        val antang = repo.get("REG2026-0139")!!.stores.first { it.id == "REG2026-0139-4" }
        assertEquals(Store.WAITING_REFERRAL, antang.status)
        assertEquals(22L, antang.tlId)
        // "Isi kode referral" (Simulasi Admin): both become active with a toko code.
        assertEquals(2, repo.adminFillReferral("REG2026-0139"))
        val filled = repo.get("REG2026-0139")!!.stores.first { it.id == s.id }
        assertEquals(Store.ACTIVE, filled.status)
        assertEquals("TK0139-05", filled.code)
        assertEquals("OFMKS1395", filled.referralCode)
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
        assertNull(repo.assign(6, 11, free.id))
        repo.unassign(11, free.id, 6)
        val history = repo.historyOf(11)
        assertEquals(listOf(AssignmentAction.RELEASE, AssignmentAction.ASSIGN), history.map { it.action })
        assertEquals(free.id, history.first().storeId)
        assertEquals("Jaya Abadi Cellular", history.first().partnerName)
        assertEquals("6", history.first().by)
        // Lepas of a toko that is not assigned does nothing.
        repo.unassign(11, free.id, 6)
        assertEquals(2, repo.historyOf(11).size)
        // FR-MP-004: the SA/SR gets an in-app notification per event; opening the inbox marks them read.
        assertEquals(2, repo.notificationsOf(11).count { !it.read })
        assertTrue(repo.notificationsOf(15).isEmpty())
        repo.markNotificationsRead(11)
        assertTrue(repo.notificationsOf(11).all { it.read })
        // Partner Inactive releases its SA/SR, logged as the system.
        repo.adminDeactivate("REG2026-0132")
        assertTrue(repo.get("REG2026-0132")!!.stores.all { it.assigned.isEmpty() })
        val system = repo.historyOf(16).first()
        assertEquals(AssignmentEvent.SYSTEM, system.by)
        assertEquals(AssignmentAction.RELEASE, system.action)
        assertEquals(1, repo.notificationsOf(16).count { !it.read })
    }

    @Test
    fun assignmentRules() {
        // Per toko (revision 2026-10-08). Eko (SA) has 1 toko, Siti (SR) 3 toko: Siti is below the SR minimum of 4.
        assertEquals(1, repo.assignedStores(15).size)
        assertEquals(3, repo.assignedStores(11).size)
        assertFalse(repo.belowMinimum(15))
        assertTrue(repo.belowMinimum(11))
        // DD review point 5: one toko = one SA/SR. Eko's toko cannot go to Siti; the holder is named in the message.
        assertEquals(AssignError.Taken, repo.assignmentError(6, 11, "REG2026-0139-1"))
        assertEquals(15L, repo.holderOf("REG2026-0139-1"))
        assertEquals(AssignError.Taken, repo.assign(6, 11, "REG2026-0139-1"))
        assertEquals(listOf(15L), repo.get("REG2026-0139")!!.stores.first().assigned)
        // Every toko of Andi's partners is held, so new toko are added for the limits below.
        val p = repo.get("REG2026-0139")!!
        val extra = (1..5).map { newStore(p.id, "Cabang Baru $it") }
        // A 4th toko for Siti reaches the minimum.
        assertNull(repo.assign(6, 11, extra[0].id))
        assertFalse(repo.belowMinimum(11))
        // SA: up to 3 toko, the 4th is refused.
        assertNull(repo.assign(6, 15, extra[1].id))
        assertNull(repo.assign(6, 15, extra[2].id))
        assertEquals(AssignError.SaLimit, repo.assignmentError(6, 15, extra[3].id))
        // SR: up to 6 toko, the 7th is refused.
        assertNull(repo.assign(6, 11, extra[3].id))
        assertNull(repo.assign(6, 11, extra[4].id))
        assertEquals(6, repo.assignedStores(11).size)
        val seventh = newStore(p.id, "Cabang Baru 6")
        assertEquals(AssignError.SrLimit, repo.assignmentError(6, 11, seventh.id))
        // Not the direct leader, not the TL's partner.
        assertEquals(AssignError.NotLeader, repo.assignmentError(7, 11, extra[4].id))
        assertEquals(AssignError.NotOwner, repo.assignmentError(6, 16, "REG2026-0138-1"))
        // Lepas frees the toko for someone else.
        repo.unassign(15, extra[2].id, 6)
        assertEquals(2, repo.assignedStores(15).size)
        assertNull(repo.assignmentError(6, 16, extra[2].id))
    }


    @Test
    fun tlIsSavedPerTokoAndMovesWithItsHistory() {
        // PRD Scope 1 v2: one partner can have several TL; each toko has one TL, by default the TL who submitted or
        // added it (or the TL of the SR who did).
        val p = repo.get("REG2026-0139")!!
        assertEquals(listOf(6L, 22L), p.tlIds.sorted())
        val before = now.toEpochMilli() - 1
        val andi = repo.activeStores().count { it.second.tlId == 6L }
        assertEquals(andi, repo.transferStores(6, 22, "TL lama dinonaktifkan"))
        assertEquals(0, repo.activeStores().count { it.second.tlId == 6L })
        val moved = repo.get("REG2026-0139")!!.stores.first()
        assertEquals(22L, moved.tlId)
        // Loans keep the TL of the toko on their date.
        assertEquals(6L, moved.tlAt(before))
        assertEquals(22L, moved.tlAt(now.toEpochMilli()))
        assertTrue(repo.myPartners(6, SalesRole.TL).isEmpty())
        // A toko the old TL did not hold stays as it was.
        assertEquals(7L, repo.get("REG2026-0138")!!.stores.first().tlId)
    }

    @Test
    fun aplRemapReleasesSalesWhoAreNotTheNewTlsSubordinates() {
        // FR-AM-013: Siti (SR of Andi) holds Sudiang; the APL moves its TL to Bagus, so Siti is released by the system.
        val sudiang = "REG2026-0139-3"
        assertEquals(listOf(11L), repo.get("REG2026-0139")!!.stores.first { it.id == sudiang }.assigned)
        assertEquals(listOf(11L), repo.remapStoreTl(sudiang, 22, "APL ganti TL toko"))
        val s = repo.get("REG2026-0139")!!.stores.first { it.id == sudiang }
        assertEquals(22L, s.tlId)
        assertTrue(s.assigned.isEmpty())
        val e = repo.historyOf(11).first()
        assertEquals(AssignmentEvent.SYSTEM, e.by)
        assertEquals(AssignmentEvent.LEADER_CHANGED, e.reason)
        assertEquals(1, repo.notificationsOf(11).count { !it.read })
        // Andi can no longer assign it; Bagus can (once he has a subordinate there).
        assertEquals(AssignError.NotOwner, repo.assignmentError(6, 11, sudiang))
    }

    @Test
    fun revisionRequestNotifiesSubmitterAndOwner() {
        // DD review point 2: REG2026-0145 was submitted by TL Maya (9), who also owns it: one notification.
        repo.adminRequestRevision("REG2026-0145")
        assertEquals(1, repo.notificationsOf(9).count { it.revision?.partnerId == "REG2026-0145" })
        // REG2026-0146 was submitted by SR Siti (11), owned by TL Andi (6): both are notified.
        repo.adminRequestRevision("REG2026-0146")
        assertEquals(1, repo.notificationsOf(11).count { it.revision?.partnerId == "REG2026-0146" })
        assertEquals(1, repo.notificationsOf(6).count { it.revision?.partnerId == "REG2026-0146" })
    }

    @Test
    fun disabledSalesAccountReleasesItsToko() {
        // DD review point 5: the SA/SR account disabled → every toko released, logged as the system.
        assertEquals(3, repo.releaseAll(11))
        assertTrue(repo.assignedStores(11).isEmpty())
        val e = repo.historyOf(11).first()
        assertEquals(AssignmentEvent.SYSTEM, e.by)
        assertEquals(AssignmentEvent.ACCOUNT_DISABLED, e.reason)
        assertEquals(3, repo.notificationsOf(11).count { !it.read })
    }
}
