package com.example.and_sales_portal.data.sales

import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.OperationsRepository
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

/** Loans synced from the loan system (sample generated like the web portal; PRD Scope 3 rules in [Scope3Test]). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SalesRepositoryTest {

    private val store = DemoStore(ApplicationProvider.getApplicationContext())
    private val now = LocalDate.of(2026, 10, 7).atTime(LocalTime.of(10, 30)).atZone(ActivityRules.ZONE).toInstant()
    private lateinit var sales: SalesRepository
    private lateinit var partners: PartnerRepository

    @Before
    fun setUp() {
        partners = PartnerRepository(store) { now }.also { it.reset() }
        val ops = OperationsRepository(store, partners) { now }.also { it.reset() }
        sales = SalesRepository(partners, ops) { now }
    }

    @Test
    fun loansAreDeterministicAndBelongToActiveToko() {
        val a = sales.loans()
        assertTrue(a.isNotEmpty())
        assertEquals(a.map { it.id }, SalesRepository(PartnerRepository(store) { now }, OperationsRepository(store, PartnerRepository(store) { now }) { now }) { now }.loans().map { it.id })
        assertTrue(a.all { it.submittedAt <= now.toEpochMilli() })
        assertTrue(sales.loansOfSales(15).all { it.storeId == "REG2026-0139-1" })
        assertTrue(a.filter { it.status == LoanStatus.REJECTED }.all { it.rejectionReason != null })
        assertTrue(a.filter { it.status == LoanStatus.PAID_OUT }.all { it.paidOutAt != null })
    }

    @Test
    fun masking() {
        val l = sales.loans().first()
        assertTrue(l.maskedId.contains("•••"))
        assertTrue(l.maskedCustomer.split(' ').all { it.length >= 3 && it.drop(2).all { ch -> ch == '•' } })
    }

    @Test
    fun pastSalesStayWithTheOldOwnerAfterTheTlIsReplaced() {
        // PRD Scope 1 v2: toko of Andi (6) move to Bagus (22); loans before the move stay with Andi.
        val before = sales.loansOfTl(6).map { it.id }.toSet()
        val bagus = sales.loansOfTl(22).map { it.id }.toSet()
        assertTrue(before.isNotEmpty())
        partners.transferStores(6, 22)
        assertEquals(before, sales.loansOfTl(6).map { it.id }.toSet())
        assertEquals(bagus, sales.loansOfTl(22).map { it.id }.toSet())
    }
}
