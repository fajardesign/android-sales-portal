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

/** Loans, sales summaries and incentive estimates (TL-06 … TL-10, SLS-01 … SLS-04; schemes of the web portal). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SalesRepositoryTest {

    private val store = DemoStore(ApplicationProvider.getApplicationContext())
    private val now = LocalDate.of(2026, 10, 7).atTime(LocalTime.of(10, 30)).atZone(ActivityRules.ZONE).toInstant()
    private lateinit var sales: SalesRepository

    @Before
    fun setUp() {
        val partners = PartnerRepository(store) { now }.also { it.reset() }
        val ops = OperationsRepository(store, partners) { now }.also { it.reset() }
        sales = SalesRepository(partners, ops) { now }
    }

    @Test
    fun tiersMatchThePortal() {
        assertEquals(0.0, SalesRepository.tierFor(SalesRepository.SALES_TIERS, 0.0).ratePct, 0.0)
        assertEquals(0.0, SalesRepository.tierFor(SalesRepository.SALES_TIERS, 55.0).ratePct, 0.0)
        assertEquals(0.55, SalesRepository.tierFor(SalesRepository.SALES_TIERS, 55.1).ratePct, 0.0)
        assertEquals(0.85, SalesRepository.tierFor(SalesRepository.SALES_TIERS, 100.0).ratePct, 0.0)
        assertEquals(1.0, SalesRepository.tierFor(SalesRepository.SALES_TIERS, 150.0).ratePct, 0.0)
        assertEquals(0.33, SalesRepository.tierFor(SalesRepository.LEADER_TIERS, 121.0).ratePct, 0.0)
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
    fun summaryCounts() {
        val loans = sales.loansOfTl(6)
        val month = LocalDate.of(2026, 10, 1)..LocalDate.of(2026, 10, 7)
        val s = SalesSummary.of(loans, month.start, month.endInclusive)
        assertTrue(s.accepted <= s.submitted)
        assertEquals(loans.filter { it.paidOutAt != null && SalesRepository.dateOf(it.paidOutAt) in month }.sumOf { it.amount }, s.paidOutAmount)
    }

    @Test
    fun incentiveComponents() {
        val eko = sales.salesIncentive(15, SalesRole.SA)
        val fee = eko.components.first { it.kind == ComponentKind.DAILY_FEE }
        assertEquals(SalesRepository.SA_DAILY_FEE * fee.days, fee.amount)
        val paid = eko.components.first { it.kind == ComponentKind.PAID_OUT }
        assertEquals(Math.round(eko.paidOutAmount * paid.ratePct / 100), paid.amount)
        assertEquals(SalesRepository.tierFor(SalesRepository.SALES_TIERS, eko.achievementPct), eko.tier)
        val andi = sales.tlIncentive(6)
        assertEquals(SalesRepository.TL_SENIOR_DAILY_FEE, andi.components.first().unitAmount)
        assertTrue(andi.components.any { it.kind == ComponentKind.LEADER })
    }
}
