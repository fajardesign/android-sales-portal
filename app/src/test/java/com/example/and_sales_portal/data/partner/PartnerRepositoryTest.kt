package com.example.and_sales_portal.data.partner

import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoStore
import org.junit.Assert.assertEquals
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
        val p = repo.submit(repo.get("REG2026-0148")!!.copy(id = "", name = "Toko Baru", submittedBy = 11, submittedAt = now.toEpochMilli()))
        assertEquals("REG2026-0149", p.id)
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
    fun assignmentRules() {
        // Eko (SA) already has Jaya Abadi Cellular: a second partner is refused.
        assertEquals(AssignError.SaLimit, repo.assignmentError(6, 15, "REG2026-0132"))
        // Siti (SR) has 2 partners; a 3rd would be allowed if one existed in the area; not owner / other leader refused.
        assertEquals(AssignError.NotLeader, repo.assignmentError(7, 11, "REG2026-0139"))
        assertEquals(AssignError.NotOwner, repo.assignmentError(6, 16, "REG2026-0138"))
        // Nurul (SA) moves: unassign then assign covers every toko.
        repo.unassign(16, "REG2026-0132")
        assertTrue(repo.assignedPartners(16).isEmpty())
        assertNull(repo.assign(6, 16, "REG2026-0139"))
        assertTrue(repo.get("REG2026-0139")!!.stores.all { 16L in it.assigned })
    }
}
