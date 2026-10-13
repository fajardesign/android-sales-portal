package com.example.and_sales_portal.data.team

import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.partner.PartnerRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

/** Web ACC-05 effects on the Android sample (DD review points 5 and 7, decision PO 2026-10-12). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DirectoryRepositoryTest {

    private val store = DemoStore(ApplicationProvider.getApplicationContext())
    private val now = Instant.parse("2026-10-13T03:00:00Z")
    private lateinit var partners: PartnerRepository
    private lateinit var directory: DirectoryRepository

    @Before
    fun setUp() {
        partners = PartnerRepository(store) { now }.also { it.reset() }
        directory = DirectoryRepository(store, partners).also { it.reset() }
    }

    @After
    fun clean() = directory.reset()

    @Test
    fun bagusIsTheSecondTlOfMakassar() {
        val bagus = DemoDirectory.person(22)!!
        assertEquals("Bagus Wicaksono", bagus.fullName)
        assertEquals("TL", bagus.role)
        assertEquals("JUNIOR", bagus.tlLevel)
        assertEquals(1, bagus.areaId)
        assertEquals(3L, bagus.supervisorId)
    }

    @Test
    fun disablingATlMovesTeamAndPartnersToTheReplacement() {
        assertEquals(listOf(15L, 16L, 11L).sorted(), DemoDirectory.teamOf(6).map { it.id }.sorted())
        assertEquals(6, directory.disableTl(6, 22)) // every partner Andi owns, whatever its status
        assertEquals("DISABLED", DemoDirectory.person(6)!!.status)
        // Subordinates now see Bagus as their leader; Bagus's team and partners.
        assertEquals(22L, DemoDirectory.person(11)!!.supervisorId)
        assertEquals(listOf(11L, 15L, 16L), DemoDirectory.teamOf(22).map { it.id }.sorted())
        assertTrue(DemoDirectory.teamOf(6).isEmpty())
        assertEquals(setOf("REG2026-0139", "REG2026-0132"), partners.myPartners(22, SalesRole.TL).map { it.id }.toSet())
        // A new submission by Siti now belongs to Bagus.
        val p = partners.submit(partners.get("REG2026-0148")!!.copy(id = "", submittedBy = 11, ownerTl = null, submittedAt = now.toEpochMilli()))
        assertEquals(22L, p.ownerTlId)
        // Saved on the device: a new repository reads it back.
        DemoDirectory.changes = emptyMap()
        DirectoryRepository(store, partners)
        assertEquals("DISABLED", DemoDirectory.person(6)!!.status)
    }

    @Test
    fun disablingAnSaReleasesItsToko() {
        assertEquals(1, directory.disableSales(15))
        assertEquals("DISABLED", DemoDirectory.person(15)!!.status)
        assertTrue(partners.assignedStores(15).isEmpty())
        assertTrue(DemoDirectory.teamOf(6).none { it.id == 15L })
    }
}
