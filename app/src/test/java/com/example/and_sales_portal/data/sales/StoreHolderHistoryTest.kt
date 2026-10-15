package com.example.and_sales_portal.data.sales

import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.OperationsRepository
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.partner.PartnerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime

/**
 * A toko changing hands keeps the past with the person of that date (pending fix 2026-10-13): loans and incentive stay
 * with the SA/SR who held the toko when the loan was submitted, and a closed visit week keeps the toko of that week.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StoreHolderHistoryTest {

    private val store = DemoStore(ApplicationProvider.getApplicationContext())
    private val wednesday = LocalDate.of(2026, 10, 7)
    private val now = wednesday.atTime(LocalTime.of(10, 30)).atZone(ActivityRules.ZONE).toInstant()
    private val lastWeek = LocalDate.of(2026, 9, 28)
    private val thisWeek = LocalDate.of(2026, 10, 5)
    private lateinit var partners: PartnerRepository
    private lateinit var ops: OperationsRepository
    private lateinit var sales: SalesRepository

    @Before
    fun setUp() {
        partners = PartnerRepository(store) { now }.also { it.reset() }
        ops = OperationsRepository(store, partners) { now }.also { it.reset() }
        sales = SalesRepository(partners, ops) { now }
    }

    @Test
    fun salesAndIncentiveStayWithTheSaWhoHeldTheToko() {
        val toko = "REG2026-0139-1" // Eko (SA 15)
        val before = sales.loansOfSales(15).map { it.id }
        val incentive = sales.salesIncentive(15, SalesRole.SA)
        assertTrue(before.isNotEmpty())

        partners.unassign(15, toko, 6)
        assertEquals(before, sales.loansOfSales(15).map { it.id })
        assertEquals(incentive, sales.salesIncentive(15, SalesRole.SA))

        // The next holder gets only loans submitted after their Tugaskan (none yet), not Eko's past.
        assertEquals(null, partners.assign(6, 16, toko))
        assertEquals(before, sales.loansOfSales(15).map { it.id })
        assertTrue(sales.loansOfSales(16).none { it.storeId == toko })
    }

    @Test
    fun closedWeeksKeepTheTokoOfThatWeek() {
        val toko = partners.assignedStores(11).first().second.id // one of Siti's (SR 11)
        val past = ops.visitWeek(11, lastWeek)
        assertEquals(3, past.target)

        partners.unassign(11, toko, 6)
        assertEquals(past, ops.visitWeek(11, lastWeek))
        // Released on Wednesday: gone from this week (Siti can no longer check in there).
        assertEquals(2, ops.visitWeek(11, thisWeek).target)

        // Assigned on Wednesday to Nurul (SA 16): from next week, never in a past week.
        val nurulPast = ops.visitWeek(16, lastWeek).target
        val nurulNow = ops.visitWeek(16, thisWeek).target
        assertEquals(null, partners.assign(6, 16, toko))
        assertEquals(nurulPast, ops.visitWeek(16, lastWeek).target)
        assertEquals(nurulNow, ops.visitWeek(16, thisWeek).target)
        assertEquals(nurulNow + 1, ops.visitWeek(16, thisWeek.plusWeeks(1)).target)
    }

    @Test
    fun closedWeeksOfATlKeepThePartnersOwnedThatWeek() {
        val andi = ops.visitWeek(6, lastWeek)
        val bagus = ops.visitWeek(22, lastWeek)
        assertTrue(andi.target > 0)

        partners.transferOwnership(6, 22, "TL lama dinonaktifkan")
        assertEquals(andi, ops.visitWeek(6, lastWeek))
        assertEquals(bagus, ops.visitWeek(22, lastWeek))
    }
}
