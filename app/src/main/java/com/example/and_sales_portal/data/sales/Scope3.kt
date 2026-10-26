package com.example.and_sales_portal.data.sales

import com.example.and_sales_portal.data.demo.DemoDirectory
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.floor

/**
 * PRD Scope 3 (Sales Pipeline & Incentive Monitoring, PRD 3.0 draft 9, 2026-10-22): calculation rules, the same as the web
 * portal (`admin-sales-portal/src/api/scope3.js`, tests `src/__tests__/scope3.test.js`). Every amount is rounded down to
 * whole rupiah. Arithmetic follows the web (double precision, same order) so the results are identical.
 * Spec: PRF-01 … PRF-06 (`flows_android/performance.md`).
 */

/** Portal status of a loan (FR-LP-003): Diajukan, Disetujui, Ditolak, Cair. */
enum class PortalStatus { DIAJUKAN, DISETUJUI, DITOLAK, CAIR }

/** Role of a monthly row; one TL value (no Senior/Junior split). */
enum class S3Role { SA, SR, TL }

/** BERJALAN (current month) → REKONSILIASI (1st of the next month until the lock) → FINAL (from the lock day). */
enum class MonthStatus { BERJALAN, REKONSILIASI, FINAL }

/** Achievement band (FSD §12): Tercapai ≥ 100, On Track 70–99,9, Berisiko < 70. */
enum class Band { TERCAPAI, ON_TRACK, BERISIKO }

/** Payment of a Final row (FR-PY): Menunggu pembayaran, Dibayar, Tidak ada pembayaran (amount Rp 0). */
enum class PayStatus { MENUNGGU, DIBAYAR, TIDAK_ADA }

/** Tier "> from, ≤ to" (from `null` = from 0, to `null` = no upper bound); [rate] in percent. */
data class S3Tier(val from: Double?, val to: Double?, val rate: Double)

/** The tier above the current one and the Cair dihitung still needed to pass its lower bound. */
data class NextTier(val tier: S3Tier, val needed: Long)

/** Sales configuration: targets, tier tables, lock day and sales payout day. */
data class SalesConfig(
    val targets: Map<S3Role, Long>,
    val salesTiers: List<S3Tier>,
    val tlTiers: List<S3Tier>,
    val lockDay: Int,
    val payoutDay: Int,
)

/** Basic salary configuration: daily fee per area and role, maximum paid days, multiplier and its threshold. */
data class FeeConfig(
    val fees: Map<Int, Map<S3Role, Long>>,
    val maxDays: Int,
    val multiplier: Double,
    val threshold: Double,
    val roles: Set<S3Role>,
)

/** One person's figures for one month (web `calcSales`). */
data class SalesCalc(
    val target: Long,
    val actual: Long,
    val achievement: Double,
    val tier: S3Tier,
    val coefficient: Double,
    val incentive: Long,
    val next: NextTier?,
    val validDays: Int,
    val paidDays: Int,
    val capped: Boolean,
    val maxDays: Int,
    val dailyFee: Long,
    val multiplier: Double,
    val threshold: Double,
    val multiplierApplies: Boolean,
    val multiplierEligible: Boolean,
    val basicSalary: Long,
    val takeHomePay: Long,
    val band: Band,
)

object Scope3 {

    private fun tiers(vararg rows: Triple<Double?, Double?, Double>) = rows.map { S3Tier(it.first, it.second, it.third) }

    val SALES_TIERS = tiers(Triple(null, 55.0, 0.0), Triple(55.0, 70.0, 0.55), Triple(70.0, 85.0, 0.7), Triple(85.0, 100.0, 0.85), Triple(100.0, 120.0, 0.9), Triple(120.0, null, 1.0))
    val TL_TIERS = tiers(Triple(null, 55.0, 0.0), Triple(55.0, 70.0, 0.18), Triple(70.0, 85.0, 0.23), Triple(85.0, 100.0, 0.28), Triple(100.0, 120.0, 0.3), Triple(120.0, null, 0.33))

    /** Initial values of the PRD (decision 2026-10-22): targets SA Rp 100 jt, SR Rp 150 jt, TL Rp 1,5 M; lock 5th, payout 10th. */
    val SALES = SalesConfig(mapOf(S3Role.SA to 100_000_000L, S3Role.SR to 150_000_000L, S3Role.TL to 1_500_000_000L), SALES_TIERS, TL_TIERS, lockDay = 5, payoutDay = 10)

    /** Daily fee SA/SR Rp 120.000, TL Rp 200.000 in every area; max. 26 paid days; ×2 above 100% for SA, SR and TL. */
    val FEE = FeeConfig(
        DemoDirectory.areas.associate { it.id to mapOf(S3Role.SA to 120_000L, S3Role.SR to 120_000L, S3Role.TL to 200_000L) },
        maxDays = 26, multiplier = 2.0, threshold = 100.0, roles = setOf(S3Role.SA, S3Role.SR, S3Role.TL),
    )

    /** LMS status → portal status; an unknown status is Diajukan (FR-LP-003). */
    fun portalStatus(lms: String): PortalStatus = when (lms) {
        "SUBMITTED", "IN_PROCESS" -> PortalStatus.DIAJUKAN
        "APPROVED" -> PortalStatus.DISETUJUI
        "REJECTED", "CANCELLED" -> PortalStatus.DITOLAK
        "PAID_OUT" -> PortalStatus.CAIR
        else -> PortalStatus.DIAJUKAN
    }

    fun portalStatus(l: Loan): PortalStatus = portalStatus(l.status.name)

    /** Tier with achievement > from and ≤ to. */
    fun tierOf(tiers: List<S3Tier>, achievement: Double): S3Tier =
        tiers.firstOrNull { (it.from == null || achievement > it.from) && (it.to == null || achievement <= it.to) } ?: tiers.first()

    /** "Butuh Rp x lagi untuk tier y": shortfall so that achievement passes the lower bound of the next higher tier. */
    fun nextTierOf(tiers: List<S3Tier>, achievement: Double, actual: Long, target: Long): NextTier? {
        val sorted = tiers.sortedBy { it.from ?: -1.0 }
        val current = tierOf(sorted, achievement)
        val next = sorted.drop(sorted.indexOf(current) + 1).firstOrNull { it.rate > current.rate } ?: return null
        if (target == 0L) return null
        val needed = floor(target.toDouble() * (next.from ?: 0.0) / 100).toLong() - actual + 1
        return NextTier(next, maxOf(1L, needed))
    }

    fun band(achievement: Double): Band = when {
        achievement >= 100 -> Band.TERCAPAI
        achievement >= 70 -> Band.ON_TRACK
        else -> Band.BERISIKO
    }

    /** One person's month: target, achievement, tier, incentive, paid days, daily fee, multiplier, basic salary, take home pay. */
    fun calcSales(sales: SalesConfig, fee: FeeConfig, role: S3Role, actual: Long, validDays: Int, areaId: Int): SalesCalc {
        val target = sales.targets[role] ?: 0L
        val achievement = if (target != 0L) actual.toDouble() / target * 100 else 0.0
        val tiers = if (role == S3Role.TL) sales.tlTiers else sales.salesTiers
        val tier = tierOf(tiers, achievement)
        val incentive = floor(actual.toDouble() * tier.rate / 100).toLong()
        val paidDays = minOf(validDays, fee.maxDays)
        val dailyFee = fee.fees[areaId]?.get(role) ?: 0L
        val applies = role in fee.roles && achievement > fee.threshold
        val basic = floor(paidDays.toDouble() * dailyFee * (if (applies) fee.multiplier else 1.0)).toLong()
        return SalesCalc(
            target, actual, achievement, tier, tier.rate, incentive, nextTierOf(tiers, achievement, actual, target),
            validDays, paidDays, validDays > fee.maxDays, fee.maxDays, dailyFee, fee.multiplier, fee.threshold,
            applies, role in fee.roles, basic, basic + incentive, band(achievement),
        )
    }

    fun calcSales(role: S3Role, actual: Long, validDays: Int, areaId: Int): SalesCalc = calcSales(SALES, FEE, role, actual, validDays, areaId)

    /** A loan counts for [ym] only when it was submitted AND paid out in that month, WIB (performance month). */
    fun countsFor(l: Loan, ym: YearMonth): Boolean =
        portalStatus(l) == PortalStatus.CAIR && YearMonth.from(SalesRepository.dateOf(l.submittedAt)) == ym &&
            l.paidOutAt != null && YearMonth.from(SalesRepository.dateOf(l.paidOutAt)) == ym

    /** Lock of [ym]: the lock day of the next month, 00:00 WIB. */
    fun lockDate(ym: YearMonth, sales: SalesConfig = SALES): LocalDate = ym.plusMonths(1).atDay(sales.lockDay)

    /** Sales payout date of [ym]: the payout day of the next month. */
    fun payoutDate(ym: YearMonth, sales: SalesConfig = SALES): LocalDate = ym.plusMonths(1).atDay(sales.payoutDay)

    fun monthStatus(ym: YearMonth, currentMonth: YearMonth, today: LocalDate): MonthStatus = when {
        ym >= currentMonth -> MonthStatus.BERJALAN
        !today.isBefore(lockDate(ym)) -> MonthStatus.FINAL
        else -> MonthStatus.REKONSILIASI
    }

    /**
     * Last day a TL can correct the attendance of [date]: the end of the following week (Scope 2 FR-014) or the day before
     * the lock of its month, whichever comes first.
     */
    fun correctionDeadline(date: LocalDate): LocalDate {
        val dow = date.dayOfWeek.value % 7 // Sunday 0, as JavaScript getUTCDay
        val sunday = date.plusDays(((7 - dow) % 7 + 7).toLong())
        val beforeLock = lockDate(YearMonth.from(date)).minusDays(1)
        return if (sunday < beforeLock) sunday else beforeLock
    }
}
