package com.example.and_sales_portal.data.sales

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId

/**
 * PRD Scope 3 rules with the PRD worked examples, the same cases as the web `admin-sales-portal/src/__tests__/scope3.test.js`
 * (Surabaya = area 4, Jakarta = area 5).
 */
class Scope3Test {

    private val sby = 4
    private val jkt = 5

    @Test
    fun saIncentive() {
        // SA 90 jt of 100 jt = 90% → 0,85% → Rp 765.000.
        val r = Scope3.calcSales(S3Role.SA, 90_000_000, 0, sby)
        assertEquals(90.0, r.achievement, 0.0)
        assertEquals(0.85, r.coefficient, 0.0)
        assertEquals(765_000L, r.incentive)
    }

    @Test
    fun srAtTheTierEdge() {
        // SR exactly 180 jt of 150 jt = 120% → tier > 100–120% (0,90%) → Rp 1.620.000.
        val r = Scope3.calcSales(S3Role.SR, 180_000_000, 0, sby)
        assertEquals(120.0, r.achievement, 0.0)
        assertEquals(0.9, r.coefficient, 0.0)
        assertEquals(1_620_000L, r.incentive)
    }

    @Test
    fun tlIncentive() {
        // TL 1,2 M of 1,5 M = 80% → 0,23% → Rp 2.760.000.
        val r = Scope3.calcSales(S3Role.TL, 1_200_000_000, 0, sby)
        assertEquals(80.0, r.achievement, 0.0)
        assertEquals(0.23, r.coefficient, 0.0)
        assertEquals(2_760_000L, r.incentive)
    }

    @Test
    fun tierBoundaryIsGreaterThanFromAndAtMostTo() {
        assertEquals(0.85, Scope3.tierOf(Scope3.SALES_TIERS, 100.0).rate, 0.0)
        assertEquals(0.9, Scope3.tierOf(Scope3.SALES_TIERS, 100.0001).rate, 0.0)
        assertEquals(0.0, Scope3.tierOf(Scope3.SALES_TIERS, 55.0).rate, 0.0)
        assertEquals(0.55, Scope3.tierOf(Scope3.SALES_TIERS, 55.1).rate, 0.0)
        assertEquals(0.33, Scope3.tierOf(Scope3.TL_TIERS, 121.0).rate, 0.0)
        assertEquals(listOf(0.0, 0.18, 0.23, 0.28, 0.3, 0.33), Scope3.TL_TIERS.map { it.rate })
    }

    @Test
    fun neededForTheNextTier() {
        // "Butuh Rp x lagi": SA 90 jt needs Rp 10.000.001 to pass 100% (tier 0,90%).
        val next = requireNotNull(Scope3.calcSales(S3Role.SA, 90_000_000, 0, sby).next)
        assertEquals(10_000_001L, next.needed)
        assertEquals(0.9, next.tier.rate, 0.0)
        assertEquals(null, Scope3.calcSales(S3Role.SA, 130_000_000, 0, sby).next)
    }

    @Test
    fun achievementBands() {
        assertEquals(listOf(Band.TERCAPAI, Band.ON_TRACK, Band.ON_TRACK, Band.BERISIKO), listOf(100.0, 99.9, 70.0, 69.9).map { Scope3.band(it) })
    }

    @Test
    fun dailyFeeIllustration() {
        // SA Surabaya, 23 valid days, 300% → 23 × 120.000 × 2 = 5.520.000; incentive 3.000.000; take home pay 8.520.000.
        val r = Scope3.calcSales(S3Role.SA, 300_000_000, 23, sby)
        assertEquals(5_520_000L, r.basicSalary)
        assertEquals(3_000_000L, r.incentive)
        assertEquals(8_520_000L, r.takeHomePay)
        assertTrue(r.multiplierApplies)
        // The same SA at 90%: no multiplier, 2.760.000 + 765.000 = 3.525.000.
        val s = Scope3.calcSales(S3Role.SA, 90_000_000, 23, sby)
        assertEquals(2_760_000L, s.basicSalary)
        assertEquals(3_525_000L, s.takeHomePay)
        assertFalse(s.multiplierApplies)
    }

    @Test
    fun exactly100PercentHasNoMultiplier() {
        assertFalse(Scope3.calcSales(S3Role.SA, 100_000_000, 10, sby).multiplierApplies)
    }

    @Test
    fun atMost26PaidDays() {
        val r = Scope3.calcSales(S3Role.SR, 0, 30, sby)
        assertEquals(26, r.paidDays)
        assertTrue(r.capped)
        assertEquals(26L * 120_000, r.basicSalary)
    }

    @Test
    fun dailyFeePerArea() {
        // Jakarta SA set to Rp 130.000 → 23 days = 2.990.000; Surabaya stays 2.760.000.
        val fee = Scope3.FEE.copy(fees = Scope3.FEE.fees + (jkt to mapOf(S3Role.SA to 130_000L, S3Role.SR to 120_000L, S3Role.TL to 200_000L)))
        assertEquals(2_990_000L, Scope3.calcSales(Scope3.SALES, fee, S3Role.SA, 0, 23, jkt).basicSalary)
        assertEquals(2_760_000L, Scope3.calcSales(Scope3.SALES, fee, S3Role.SA, 0, 23, sby).basicSalary)
    }

    @Test
    fun tlDailyFeeAndMultiplier() {
        // TL: daily fee 200.000 (one value), multiplier for TL too: 1,6 M (106,7%), 20 days → 8.000.000.
        val r = Scope3.calcSales(S3Role.TL, 1_600_000_000, 20, sby)
        assertEquals(200_000L, r.dailyFee)
        assertEquals(8_000_000L, r.basicSalary)
        assertTrue(r.multiplierApplies)
    }

    @Test
    fun countsOnlyWhenSubmittedAndPaidOutInTheSameMonthWib() {
        fun at(iso: String) = LocalDate.parse(iso).atTime(LocalTime.of(10, 0)).atZone(ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli()
        val l = Loan("APP1", "P", "S", 11, 6, "Ahmad Saputra", 10_000_000, 1, at("2026-09-28"), LoanStatus.PAID_OUT, null, at("2026-09-30"), at("2026-09-30"))
        val sep = YearMonth.of(2026, 9)
        assertTrue(Scope3.countsFor(l, sep))
        val late = l.copy(submittedAt = at("2026-09-30"), paidOutAt = at("2026-10-02"))
        assertFalse(Scope3.countsFor(late, sep))
        assertFalse(Scope3.countsFor(late, sep.plusMonths(1)))
        // WIB boundary: 30 Sep 23:30 WIB is still September.
        val edge = l.copy(paidOutAt = LocalDate.parse("2026-09-30").atTime(23, 30).atZone(ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli())
        assertTrue(Scope3.countsFor(edge, sep))
    }

    @Test
    fun portalStatusFromLms() {
        assertEquals(
            listOf(PortalStatus.DIAJUKAN, PortalStatus.DIAJUKAN, PortalStatus.DISETUJUI, PortalStatus.DITOLAK, PortalStatus.CAIR, PortalStatus.DIAJUKAN),
            listOf("SUBMITTED", "IN_PROCESS", "APPROVED", "REJECTED", "PAID_OUT", "X").map { Scope3.portalStatus(it) },
        )
    }

    @Test
    fun monthStatusLockAndPayout() {
        // Today 7 Oct: October Berjalan, September Final (lock 5 Oct), sales payout 10 Oct; on 3 Oct September waits.
        val oct = YearMonth.of(2026, 10)
        val sep = YearMonth.of(2026, 9)
        val oct7 = LocalDate.of(2026, 10, 7)
        assertEquals(MonthStatus.BERJALAN, Scope3.monthStatus(oct, oct, oct7))
        assertEquals(LocalDate.of(2026, 10, 5), Scope3.lockDate(sep))
        assertEquals(MonthStatus.FINAL, Scope3.monthStatus(sep, oct, oct7))
        assertEquals(LocalDate.of(2026, 10, 10), Scope3.payoutDate(sep))
        assertEquals(MonthStatus.REKONSILIASI, Scope3.monthStatus(sep, oct, LocalDate.of(2026, 10, 3)))
        assertEquals(MonthStatus.FINAL, Scope3.monthStatus(sep, oct, LocalDate.of(2026, 10, 5)))
    }

    @Test
    fun tlCorrectionDeadlineIsTheEndOfNextWeekOrBeforeTheLock() {
        assertEquals(LocalDate.of(2026, 9, 13), Scope3.correctionDeadline(LocalDate.of(2026, 9, 1)))
        assertEquals(LocalDate.of(2026, 10, 4), Scope3.correctionDeadline(LocalDate.of(2026, 9, 29)))
        // A Sunday: the Sunday of the following week.
        assertEquals(LocalDate.of(2026, 10, 18), Scope3.correctionDeadline(LocalDate.of(2026, 10, 11)))
    }

    @Test
    fun finalTakeHomePayWithKoreksi() {
        // AC-024: Siti's calculated September take home pay Rp 3.525.000, Koreksi −150.000 → Rp 3.375.000; never negative.
        val calc = Scope3.calcSales(S3Role.SA, 90_000_000, 23, sby).takeHomePay
        assertEquals(3_525_000L, calc)
        assertEquals(3_375_000L, Scope3.finalAmount(calc, -150_000))
        assertEquals(0L, Scope3.finalAmount(calc, -4_000_000))
    }

    @Test
    fun paymentStatusThroughTheMonth() {
        // Same as web `paymentOf`: Berjalan none, Rekonsiliasi until the cut-off with Koreksi, then Menunggu (also after the
        // payout day, AC-030), Tidak dibayar with keterangan, later Dibayar with the earlier keterangan kept (AC-053).
        val sep = YearMonth.of(2026, 9)
        val paidThrough = YearMonth.of(2026, 8)
        val koreksi = PaymentRecord(-150_000, "Potongan seragam hilang")
        assertEquals(null, Scope3.paymentOf(MonthStatus.BERJALAN, sep, 3_525_000, koreksi, paidThrough, LocalDate.of(2026, 9, 30)))
        val recon = requireNotNull(Scope3.paymentOf(MonthStatus.REKONSILIASI, sep, 3_525_000, koreksi, paidThrough, LocalDate.of(2026, 10, 2)))
        assertEquals(PayStatus.REKONSILIASI, recon.status)
        assertEquals(3_375_000L, recon.finalAmount)
        assertEquals(LocalDate.of(2026, 10, 5), recon.lockAt)
        val waiting = requireNotNull(Scope3.paymentOf(MonthStatus.FINAL, sep, 3_525_000, koreksi, paidThrough, LocalDate.of(2026, 10, 12)))
        assertEquals(PayStatus.MENUNGGU, waiting.status)
        assertTrue(waiting.overdue)
        assertEquals(LocalDate.of(2026, 10, 10), waiting.scheduledAt)
        val unpaid = koreksi.copy(unpaidNote = "Rekening tidak valid", unpaidAt = LocalDate.of(2026, 10, 11))
        val u = requireNotNull(Scope3.paymentOf(MonthStatus.FINAL, sep, 3_525_000, unpaid, paidThrough, LocalDate.of(2026, 10, 12)))
        assertEquals(PayStatus.TIDAK_DIBAYAR, u.status)
        assertEquals("Rekening tidak valid", u.unpaidNote)
        val paid = requireNotNull(Scope3.paymentOf(MonthStatus.FINAL, sep, 3_525_000, unpaid.copy(paidAt = LocalDate.of(2026, 10, 19)), paidThrough, LocalDate.of(2026, 10, 20)))
        assertEquals(PayStatus.DIBAYAR, paid.status)
        assertEquals(3_375_000L, paid.paidAmount)
        assertEquals("Rekening tidak valid", paid.previousUnpaidNote)
        assertEquals(PayStatus.TIDAK_ADA, Scope3.paymentOf(MonthStatus.FINAL, sep, 0, null, paidThrough, LocalDate.of(2026, 10, 7))?.status)
        assertEquals(PayStatus.DIBAYAR, Scope3.paymentOf(MonthStatus.FINAL, YearMonth.of(2026, 8), 100, null, paidThrough, LocalDate.of(2026, 10, 7))?.status)
    }
}
