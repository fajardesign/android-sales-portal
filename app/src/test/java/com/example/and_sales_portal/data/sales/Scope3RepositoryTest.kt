package com.example.and_sales_portal.data.sales

import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.activity.ActivityRepository
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.OperationsRepository
import com.example.and_sales_portal.data.activity.Punch
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.device.S3Date
import com.example.and_sales_portal.data.device.Simulation
import com.example.and_sales_portal.data.partner.PartnerRepository
import org.junit.After
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

/**
 * PRD Scope 3 on the sample data (same as the web `scope3.test.js` "Baris sales, visibilitas, dan pembayaran"): months,
 * pipeline, visibility, valid days, payment, income notifications and the TL correction deadline against the lock.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Scope3RepositoryTest {

    private val store = DemoStore(ApplicationProvider.getApplicationContext())
    private val oct7 = LocalDate.of(2026, 10, 7)
    private var now: Instant = at(oct7, "10:30")
    private var sim = Simulation()
    private lateinit var partners: PartnerRepository
    private lateinit var ops: OperationsRepository
    private lateinit var sales: SalesRepository
    private val sep = YearMonth.of(2026, 9)
    private val aug = YearMonth.of(2026, 8)
    private val oct = YearMonth.of(2026, 10)
    private val machineZone = ActivityRules.systemZone

    private fun at(date: LocalDate, time: String) = date.atTime(LocalTime.parse(time)).atZone(ActivityRules.LOCK_ZONE).toInstant()

    @Before
    fun setUp() {
        ActivityRules.systemZone = { ActivityRules.LOCK_ZONE }
        partners = PartnerRepository(store) { now }.also { it.reset() }
        ops = OperationsRepository(store, partners) { now }.also { it.reset() }
        sales = SalesRepository(partners, ops, simulation = { sim }) { now }
    }

    @After
    fun restoreZone() {
        ActivityRules.systemZone = machineZone
    }

    @Test
    fun monthsAndStatusOn7October() {
        assertEquals(listOf(oct, sep, aug, YearMonth.of(2026, 7), YearMonth.of(2026, 6), YearMonth.of(2026, 5)), sales.months())
        assertEquals(MonthStatus.BERJALAN, sales.monthStatus(oct))
        assertEquals(MonthStatus.FINAL, sales.monthStatus(sep))
        // Simulasi "3 Okt 2026": September waits for the lock.
        sim = Simulation(s3Date = S3Date.OCT_3)
        assertEquals(MonthStatus.REKONSILIASI, sales.monthStatus(sep))
        assertNull(sales.salesRow(11, sep)?.payment)
    }

    @Test
    fun pipelineFunnelAndPaidLater() {
        val p = sales.pipeline(sep, tlId = 6)
        assertEquals(p.total, p.funnel.values.sumOf { it.count })
        assertEquals(sales.loansOfTl(6).filter { Scope3.countsFor(it, sep) }.sumOf { it.amount }, p.countedAmount)
        assertTrue(p.rows.filter { it.paidLater }.all { it.status == PortalStatus.CAIR && !it.counted })
        assertTrue(p.paidLaterCount > 0)
        // Filters: status, SA/SR, "Tanpa SA/SR", newest first.
        assertTrue(sales.pipeline(sep, tlId = 6, status = PortalStatus.CAIR).rows.all { it.status == PortalStatus.CAIR })
        assertTrue(sales.pipeline(sep, tlId = 6, salesFilter = 11).rows.all { it.loan.salesId == 11L })
        assertTrue(sales.pipeline(sep, tlId = 6, salesFilter = SalesRepository.SALES_NONE).rows.all { it.loan.salesId == null })
        assertEquals(p.rows.sortedByDescending { it.loan.submittedAt }, p.rows)
        // An SA/SR sees only their own loans.
        assertTrue(sales.pipeline(sep, salesId = 11).rows.all { it.loan.salesId == 11L })
    }

    @Test
    fun visibilityTlSelfAndTeamSaOnlySelf() {
        val tl = sales.rowsFor(6, SalesRole.TL, sep)
        assertEquals(6L, tl.first().userId)
        assertTrue(tl.all { it.userId == 6L || it.leaderId == 6L })
        assertTrue(tl.any { it.userId == 11L })
        assertEquals(listOf(11L), sales.rowsFor(11, SalesRole.SR, sep).map { it.userId })
        assertTrue(sales.canView(6, 11))
        assertFalse(sales.canView(11, 15)) // peer
        assertFalse(sales.canView(22, 11)) // another TL
    }

    @Test
    fun takeHomePayIsBasicSalaryPlusIncentive() {
        val r = requireNotNull(sales.salesRow(11, sep))
        assertEquals(r.calc.basicSalary + r.calc.incentive, r.calc.takeHomePay)
        assertTrue(r.calc.validDays in 1..30)
        assertEquals(S3Role.SR, r.role)
        assertEquals(120_000L, r.calc.dailyFee)
        assertEquals(1_500_000_000L, requireNotNull(sales.salesRow(6, sep)).calc.target)
    }

    @Test
    fun validDayNeedsCheckInAndCheckOutOrATlCorrection() {
        // 8 Oct: Siti checks in and forgets to check out → Absen, not valid; the TL corrects it → valid.
        val before = sales.validDays(11, oct)
        now = at(oct7.plusDays(1), "09:00")
        ActivityRepository(ops, partners, 11, SalesRole.SR).clockIn(Punch(now.toEpochMilli(), -5.15, 119.43))
        now = at(oct7.plusDays(2), "09:00")
        assertEquals(AttendanceStatus.ABSENT, ops.attendanceStatus(11, oct7.plusDays(1)))
        assertEquals(before, sales.validDays(11, oct))
        assertNull(ops.correct(6, 11, oct7.plusDays(1), AttendanceStatus.ON_TIME, LocalTime.of(9, 0), "Lupa check out, hadir sampai sore"))
        assertEquals(before + 1, sales.validDays(11, oct))
    }

    @Test
    fun correctionClosesAtTheEndOfNextWeekOrBeforeTheLock() {
        // 29 Sep: closes 4 Oct 23:59 WIB (before the lock of 5 Oct), not 11 Oct.
        assertEquals(at(LocalDate.of(2026, 10, 4), "23:59").toEpochMilli() + 59_999, ops.correctionDeadline(LocalDate.of(2026, 9, 29)))
        assertFalse(ops.canCorrect(11, LocalDate.of(2026, 9, 29)))
        // 1 Oct (October, lock 5 Nov): up to Sunday 11 Oct.
        assertTrue(ops.canCorrect(11, LocalDate.of(2026, 10, 1)))
        assertEquals(at(LocalDate.of(2026, 10, 11), "23:59").toEpochMilli() + 59_999, ops.correctionDeadline(LocalDate.of(2026, 10, 1)))
    }

    @Test
    fun augustPaidWithCorrectionSeptemberWaiting() {
        val augRow = requireNotNull(sales.salesRow(11, aug))
        val p = requireNotNull(augRow.payment)
        assertEquals(PayStatus.DIBAYAR, p.status)
        assertEquals(150_000L, p.correction)
        assertEquals("Koreksi Juli 2026", p.note)
        assertEquals(LocalDate.of(2026, 9, 10), p.paidAt)
        assertEquals(augRow.calc.takeHomePay + 150_000, p.paidAmount)
        assertEquals(PayStatus.MENUNGGU, sales.salesRow(11, sep)?.payment?.status)
        assertEquals(LocalDate.of(2026, 10, 10), sales.salesRow(11, sep)?.payment?.scheduledAt)
        assertNull(sales.salesRow(11, oct)?.payment)
        // Simulasi "Admin tandai bulan Final terbaru dibayar": September paid on the demo date.
        sim = Simulation(s3PaidLatest = true)
        assertEquals(PayStatus.DIBAYAR, sales.salesRow(11, sep)?.payment?.status)
        assertEquals(oct7, sales.salesRow(11, sep)?.payment?.paidAt)
    }

    @Test
    fun incomeNotificationsAtTheLockAndWhenPaid() {
        sales.syncIncomeNotices(11)
        val shown = partners.notificationsOf(11).mapNotNull { it.income }
        // 30 days: August final (5 Sep) is gone; August paid (10 Sep) and September final (5 Oct) are kept.
        assertEquals(listOf("2026-09" to false, "2026-08" to true), shown.map { it.month to it.paid })
        val final = shown.first()
        val row = requireNotNull(sales.salesRow(11, sep))
        assertEquals(row.calc.takeHomePay, final.takeHomePay)
        assertEquals("2026-10-10", final.payoutDate)
        assertEquals(150_000L, shown[1].correction)
        // Before the lock (Simulasi 3 Okt) September is not final yet: its notification goes away.
        sim = Simulation(s3Date = S3Date.OCT_3)
        sales.syncIncomeNotices(11)
        assertTrue(partners.notificationsOf(11).none { it.income?.month == "2026-09" })
        // Paid in the Simulasi: a "sudah dibayar" notification for September; read state is kept on sync.
        sim = Simulation(s3PaidLatest = true)
        sales.syncIncomeNotices(11)
        partners.markNotificationsRead(11)
        sales.syncIncomeNotices(11)
        assertTrue(partners.notificationsOf(11).filter { it.income?.month == "2026-09" }.let { n -> n.size == 2 && n.all { it.read } })
    }

    @Test
    fun beforeTheLockLastMonthIsShownOnTheCard() {
        sim = Simulation(s3Date = S3Date.OCT_3)
        val card = com.example.and_sales_portal.ui.performance.performanceCardState(sales, 6, SalesRole.TL, true, com.example.and_sales_portal.data.device.PerfData.NORMAL)
        assertEquals(sep, card.prev?.month)
        assertEquals(sales.totals(sales.rowsFor(6, SalesRole.TL, oct)).takeHomePay, card.teamThp)
        sim = Simulation(s3Date = S3Date.OCT_7)
        assertNull(com.example.and_sales_portal.ui.performance.performanceCardState(sales, 11, SalesRole.SR, true, com.example.and_sales_portal.data.device.PerfData.NORMAL).prev)
    }
}
