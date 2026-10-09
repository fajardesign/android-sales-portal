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

    @Test
    fun sampleMatchesThePortal() {
        val all = repo.partners.value
        assertEquals(18, all.size)
        assertEquals(PartnerStatus.ACTIVE, repo.get("REG2026-0139")?.status)
        assertEquals("TK0139-02", repo.get("REG2026-0139")?.stores?.get(1)?.code)
        assertEquals(PartnerStatus.REVISION_REQUIRED, repo.get("REG2026-0144")?.status)
        assertEquals(PartnerStatus.INACTIVE, repo.get("REG2026-0133")?.status)
        assertEquals(6L, repo.get("REG2026-0146")?.ownerTlId) // submitted by SR Siti, TL Andi owns it
    }

    @Test
    fun visibilityPerRole() {
        val tl = repo.submissionsFor(6, SalesRole.TL).map { it.id }.toSet()
        assertEquals(setOf("REG2026-0148", "REG2026-0146", "REG2026-0142", "REG2026-0139", "REG2026-0132", "REG2026-0135"), tl)
        assertEquals(setOf("REG2026-0146", "REG2026-0135"), repo.submissionsFor(11, SalesRole.SR).map { it.id }.toSet())
        assertTrue(repo.submissionsFor(15, SalesRole.SA).isEmpty())
        assertEquals(listOf("REG2026-0139", "REG2026-0132"), repo.myPartners(6, SalesRole.TL).map { it.id })
        assertEquals(listOf("REG2026-0139"), repo.myPartners(15, SalesRole.SA).map { it.id })
    }

    @Test
    fun submitGivesTheNextNumberAndUnderReview() {
        // Sample Kode Referral like the web portal: AMR + submitter id + number.
        assertEquals("AMR6148", repo.get("REG2026-0148")!!.referralCode)
        val p = repo.submit(repo.get("REG2026-0148")!!.copy(id = "", name = "Toko Baru", submittedBy = 11, submittedAt = now.toEpochMilli(), referralCode = "SITI11"))
        assertEquals("REG2026-0149", p.id)
        assertEquals("SITI11", repo.get(p.id)!!.referralCode)
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
        repo.adminActivate("REG2026-0148")
        val p = repo.get("REG2026-0148")!!
        assertEquals(PartnerStatus.ACTIVE, p.status)
        assertEquals("TK0148-01", p.stores.first().code)
        val s = repo.addStore(p.id, p.stores.first().copy(name = "Sinar Jaya Ponsel Cabang", primary = false))
        assertEquals("TK0148-02", s.code)
        assertEquals("ACTIVE", s.status)
        assertEquals(2, repo.get(p.id)!!.stores.size)
    }

    @Test
    fun referralCodeAndPicPhoneAreUniquePerPartner() {
        // Review 2026-10-09: Kode Referral identifies the partner; No. Handphone PIC signs in to the Partner account.
        assertTrue(repo.referralTaken("amr6148"))
        assertFalse(repo.referralTaken("AMR6148", exceptId = "REG2026-0148"))
        assertFalse(repo.referralTaken("BARU01"))
        val phone = repo.get("REG2026-0148")!!.picPhone
        assertTrue(repo.picPhoneTaken("0$phone"))
        assertTrue(repo.picPhoneTaken("+62 $phone"))
        assertFalse(repo.picPhoneTaken(phone, exceptId = "REG2026-0148"))
        // Rejected and Cancelled partners do not block a new registration.
        assertFalse(repo.referralTaken(repo.get("REG2026-0135")!!.referralCode!!))
        assertEquals("81200047514", PartnerRepository.normalizePhone("+62 812-0004-7514"))
    }

    @Test
    fun assignmentRules() {
        // Per toko (revision 2026-10-08). Eko (SA) has 1 toko, Siti (SR) 3 toko: Siti is below the SR minimum of 4.
        assertEquals(1, repo.assignedStores(15).size)
        assertEquals(3, repo.assignedStores(11).size)
        assertFalse(repo.belowMinimum(15))
        assertTrue(repo.belowMinimum(11))
        // A 4th toko for Siti reaches the minimum; a toko can be shared with an SA.
        assertNull(repo.assign(6, 11, "REG2026-0139-1"))
        assertFalse(repo.belowMinimum(11))
        assertEquals(listOf(15L, 11L), repo.get("REG2026-0139")!!.stores.first().assigned)
        // SA: up to 3 toko, the 4th is refused.
        assertNull(repo.assign(6, 15, "REG2026-0132-1"))
        assertNull(repo.assign(6, 15, "REG2026-0132-2"))
        assertEquals(AssignError.SaLimit, repo.assignmentError(6, 15, "REG2026-0139-2"))
        // SR: up to 6 toko, the 7th is refused.
        assertNull(repo.assign(6, 11, "REG2026-0132-1"))
        val p = repo.get("REG2026-0139")!!
        val extra = (1..2).map { repo.addStore(p.id, p.stores.first().copy(name = "Cabang Baru $it", primary = false, assigned = emptyList())) }
        assertNull(repo.assign(6, 11, extra[0].id))
        assertEquals(6, repo.assignedStores(11).size)
        assertEquals(AssignError.SrLimit, repo.assignmentError(6, 11, extra[1].id))
        // Not the direct leader, not the TL's partner.
        assertEquals(AssignError.NotLeader, repo.assignmentError(7, 11, "REG2026-0139-1"))
        assertEquals(AssignError.NotOwner, repo.assignmentError(6, 16, "REG2026-0138-1"))
        // Unassign one toko only.
        repo.unassign(15, "REG2026-0132-2")
        assertEquals(2, repo.assignedStores(15).size)
    }
}
