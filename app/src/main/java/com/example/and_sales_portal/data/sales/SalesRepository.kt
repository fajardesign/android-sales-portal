package com.example.and_sales_portal.data.sales

import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.OperationsRepository
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.demo.seededRandom
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.data.partner.PartnerStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlin.math.roundToLong

/** CRM status of a loan application (web portal: Pengajuan, Diproses, Disetujui, Ditolak, Dicairkan). */
enum class LoanStatus { SUBMITTED, IN_PROCESS, APPROVED, REJECTED, PAID_OUT }

/** Loan application synced from Tunaiku CRM (PRD v3 `loan_application`), read-only. */
data class Loan(
    val id: String,
    val partnerId: String,
    val storeId: String,
    val salesId: Long?,
    val tlId: Long?,
    val customer: String,
    val amount: Long,
    val units: Int,
    val submittedAt: Long,
    val status: LoanStatus,
    val rejectionReason: String?,
    val paidOutAt: Long?,
    val updatedAt: Long,
) {
    /** PRD D5: application ID and customer name masked. */
    val maskedId: String get() = id.take(5) + "•••" + id.takeLast(2)
    val maskedCustomer: String get() = customer.split(' ').joinToString(" ") { it.take(2) + "•".repeat((it.length - 2).coerceAtLeast(1)) }
}

/** # submitted, # accepted, # paid out, paid-out units and amount (PRD C1, C9, D1, D6). */
data class SalesSummary(val submitted: Int, val accepted: Int, val paidOut: Int, val paidOutUnits: Int, val paidOutAmount: Long) {
    companion object {
        fun of(loans: List<Loan>, from: LocalDate, to: LocalDate): SalesSummary {
            val submitted = loans.filter { SalesRepository.dateOf(it.submittedAt) in from..to }
            val paid = loans.filter { it.paidOutAt != null && SalesRepository.dateOf(it.paidOutAt) in from..to }
            return SalesSummary(
                submitted = submitted.size,
                accepted = submitted.count { it.status == LoanStatus.APPROVED || it.status == LoanStatus.PAID_OUT },
                paidOut = paid.size,
                paidOutUnits = paid.sumOf { it.units },
                paidOutAmount = paid.sumOf { it.amount },
            )
        }
    }
}

/** One incentive component ("Daily Fee", "Paid Out Incentive", …) with how it is computed. */
data class IncentiveComponent(val kind: ComponentKind, val amount: Long, val days: Int = 0, val unitAmount: Long = 0, val ratePct: Double = 0.0, val base: Long = 0, val mfpPct: Double? = null)

enum class ComponentKind { DAILY_FEE, PAID_OUT, LEADER, VOLUME, COLLECTION, COMMISSION }

/** Monthly incentive estimate of a person or a partner (portal `computeIncentives`). */
data class Incentive(
    val month: YearMonth,
    val target: Long,
    val paidOutAmount: Long,
    /** Paid-out achievement, % of the target. */
    val achievementPct: Double,
    /** Tier as "> 85% s/d 100%"; `null` without tiers (commission). */
    val tier: Tier?,
    val components: List<IncentiveComponent>,
) {
    val total: Long get() = components.sumOf { it.amount }
}

data class Tier(val from: Double, val to: Double?, val ratePct: Double)

/**
 * Loans (synced from CRM), targets and incentive estimates of the prototype, generated the same way as the web portal
 * sample (`admin-sales-portal/src/api/db.js`, `mockApi.js computeIncentives`) and the BRD V2.2 schemes (PRD v3 §E1).
 * Spec: TL-06 … TL-10, SLS-01 … SLS-04 (PRD v3 C1, C9, D1, D5, D6).
 */
class SalesRepository(private val partners: PartnerRepository, private val ops: OperationsRepository, private val clock: () -> Instant = Instant::now) {

    private var cache: Pair<String, List<Loan>>? = null

    fun today(): LocalDate = ops.today()

    /** Every loan of Active (and later Inactive) partners up to now. */
    fun loans(): List<Loan> {
        val key = System.identityHashCode(partners.partners.value).toString() + today()
        cache?.takeIf { it.first == key }?.let { return it.second }
        val now = clock().toEpochMilli()
        val list = partners.partners.value.flatMap { generate(it, now) }
        cache = key to list
        return list
    }

    fun loansOfSales(employeeId: Long) = loans().filter { it.salesId == employeeId }
    fun loansOfTl(tlId: Long) = loans().filter { it.tlId == tlId }
    fun loansOfPartner(partnerId: String) = loans().filter { it.partnerId == partnerId }

    // region Incentive (schemes of the web portal, BRD V2.2)

    /** SA/SR: Daily Fee × days present + Paid Out Incentive (tier on own paid-out achievement). */
    fun salesIncentive(employeeId: Long, role: SalesRole, month: YearMonth = YearMonth.from(today())): Incentive {
        val loans = loansOfSales(employeeId)
        val fee = if (role == SalesRole.SA) SA_DAILY_FEE else SR_DAILY_FEE
        return personIncentive(employeeId, loans, month, fee, SALES_TIERS, ComponentKind.PAID_OUT)
    }

    /** TL: Daily Fee (Senior/Junior) + Leader Incentive (tier on the team's paid-out achievement). */
    fun tlIncentive(tlId: Long, month: YearMonth = YearMonth.from(today())): Incentive {
        val senior = DemoDirectory.person(tlId)?.tlLevel == "SENIOR"
        return personIncentive(tlId, loansOfTl(tlId), month, if (senior) TL_SENIOR_DAILY_FEE else TL_JUNIOR_DAILY_FEE, LEADER_TIERS, ComponentKind.LEADER)
    }

    /** Partner: Volume + Collection (MFP) for Store channel, Commission 5% for Non-Store (affiliate). */
    fun partnerIncentive(p: Partner, month: YearMonth = YearMonth.from(today())): Incentive {
        val paid = paidIn(loansOfPartner(p.id), month)
        val amount = paid.sumOf { it.amount }
        val target = targetOf(paid.map { it.storeId }.toSet(), month)
        val ach = pct(amount, target)
        if (p.channel == "NON_STORE") {
            return Incentive(month, target, amount, ach, null, listOf(IncentiveComponent(ComponentKind.COMMISSION, (amount * AFFILIATE_RATE / 100).roundToLong(), ratePct = AFFILIATE_RATE, base = amount)))
        }
        val volume = tierFor(VOLUME_TIERS, ach)
        val stores = paid.map { it.storeId }.toSet()
        val mfp = if (stores.isEmpty()) 0.0 else stores.sumOf { mfpOf(it, month) } / stores.size
        val collection = tierFor(COLLECTION_TIERS, mfp)
        return Incentive(
            month, target, amount, ach, volume,
            listOf(
                IncentiveComponent(ComponentKind.VOLUME, (amount * volume.ratePct / 100).roundToLong(), ratePct = volume.ratePct, base = amount),
                IncentiveComponent(ComponentKind.COLLECTION, (amount * collection.ratePct / 100).roundToLong(), ratePct = collection.ratePct, base = amount, mfpPct = mfp),
            ),
        )
    }

    private fun personIncentive(id: Long, loans: List<Loan>, month: YearMonth, fee: Long, tiers: List<Tier>, kind: ComponentKind): Incentive {
        val paid = paidIn(loans, month)
        val amount = paid.sumOf { it.amount }
        val target = targetOf(paid.map { it.storeId }.toSet(), month)
        val ach = pct(amount, target)
        val tier = tierFor(tiers, ach)
        val days = daysPresent(id, month)
        return Incentive(
            month, target, amount, ach, tier,
            listOf(
                IncentiveComponent(ComponentKind.DAILY_FEE, days * fee, days = days, unitAmount = fee),
                IncentiveComponent(kind, (amount * tier.ratePct / 100).roundToLong(), ratePct = tier.ratePct, base = amount),
            ),
        )
    }

    private fun daysPresent(id: Long, month: YearMonth): Int {
        val today = today()
        return generateSequence(month.atDay(1)) { it.plusDays(1) }
            .takeWhile { !it.isAfter(month.atEndOfMonth()) && !it.isAfter(today) }
            .count { ops.attendanceStatus(id, it).let { s -> s == AttendanceStatus.ON_TIME || s == AttendanceStatus.LATE } }
    }

    private fun paidIn(loans: List<Loan>, month: YearMonth) = loans.filter { it.paidOutAt != null && YearMonth.from(dateOf(it.paidOutAt)) == month }

    /** Target paid out per toko per month (read-only; source still TBD in the PRD). */
    fun targetOf(storeIds: Set<String>, month: YearMonth): Long = storeIds.sumOf { sid ->
        val paid = loans().filter { it.storeId == sid && it.paidOutAt != null && YearMonth.from(dateOf(it.paidOutAt)) == month }.sumOf { it.amount }
        if (paid == 0L) 0L else {
            val r = seededRandom(seed(sid) * 31 + month.monthValue + month.year * 12L)
            Math.round(paid * (0.75 + r.nextDouble() * 0.65) / 1e6) * 1_000_000
        }
    }

    private fun mfpOf(storeId: String, month: YearMonth): Double = seededRandom(seed(storeId) * 17 + month.monthValue).let { 6 + it.nextDouble() * 9 }

    // endregion

    /** Same pattern as the portal sample: applications per workday since the toko became active, status by age. */
    private fun generate(p: Partner, now: Long): List<Loan> {
        val activatedAt = p.history.lastOrNull { it.to == PartnerStatus.ACTIVE }?.at ?: return emptyList()
        val stop = if (p.status == PartnerStatus.INACTIVE) dateOf(p.statusUpdatedAt) else today().plusDays(1)
        val out = mutableListOf<Loan>()
        p.stores.forEach { s ->
            val start = dateOf(maxOf(activatedAt, s.addedAt))
            val rate = 0.9 + seededRandom(seed(s.id)).nextDouble() * 1.4
            var d = start
            while (d.isBefore(stop) && !d.isAfter(today())) {
                if (ActivityRules.isWorkday(d)) {
                    val rnd = seededRandom(seed(s.id) * 1_000_003 + d.toEpochDay())
                    val n = kotlin.math.floor(rate + rnd.nextDouble() - 0.5 + if (rnd.nextDouble() < 0.15) 1 else 0).toInt()
                    repeat(n) { i ->
                        val submittedAt = d.atTime(LocalTime.of(9 + rnd.nextInt(11), rnd.nextInt(60))).atZone(ActivityRules.ZONE).toInstant().toEpochMilli()
                        if (submittedAt > now) return@repeat
                        val ageDays = (now - submittedAt) / DAY_MS.toDouble()
                        val r = rnd.nextDouble()
                        val status = when {
                            ageDays < 1 -> if (r < 0.6) LoanStatus.SUBMITTED else LoanStatus.IN_PROCESS
                            ageDays < 3 -> when { r < 0.3 -> LoanStatus.IN_PROCESS; r < 0.55 -> LoanStatus.APPROVED; r < 0.75 -> LoanStatus.REJECTED; else -> LoanStatus.PAID_OUT }
                            else -> when { r < 0.28 -> LoanStatus.REJECTED; r < 0.36 -> LoanStatus.APPROVED; else -> LoanStatus.PAID_OUT }
                        }
                        val updatedAt = minOf(now, submittedAt + (2 + rnd.nextInt(71)) * 3_600_000L)
                        out += Loan(
                            id = "APP" + (4_100_000 + seed("${s.id}:$d:$i") % 5_900_000),
                            partnerId = p.id,
                            storeId = s.id,
                            salesId = s.assigned.firstOrNull(),
                            tlId = p.ownerTlId,
                            customer = "${FIRST[rnd.nextInt(FIRST.size)]} ${LAST[rnd.nextInt(LAST.size)]}",
                            amount = (30 + rnd.nextInt(121)) * 100_000L,
                            units = if (rnd.nextDouble() < 0.2) 2 else 1,
                            submittedAt = submittedAt,
                            status = status,
                            rejectionReason = if (status == LoanStatus.REJECTED) REJECT[rnd.nextInt(REJECT.size)] else null,
                            paidOutAt = if (status == LoanStatus.PAID_OUT) updatedAt else null,
                            updatedAt = updatedAt,
                        )
                    }
                }
                d = d.plusDays(1)
            }
        }
        return out
    }

    companion object {
        private const val DAY_MS = 86_400_000L
        const val SA_DAILY_FEE = 108_000L
        const val SR_DAILY_FEE = 126_000L
        const val TL_SENIOR_DAILY_FEE = 165_000L
        const val TL_JUNIOR_DAILY_FEE = 126_000L
        const val AFFILIATE_RATE = 5.0

        private fun t(vararg rows: Triple<Double, Double?, Double>) = rows.map { Tier(it.first, it.second, it.third) }
        val SALES_TIERS = t(Triple(0.0, 55.0, 0.0), Triple(55.0, 70.0, 0.55), Triple(70.0, 85.0, 0.7), Triple(85.0, 100.0, 0.85), Triple(100.0, 120.0, 0.9), Triple(120.0, null, 1.0))
        val LEADER_TIERS = t(Triple(0.0, 55.0, 0.0), Triple(55.0, 70.0, 0.18), Triple(70.0, 85.0, 0.23), Triple(85.0, 100.0, 0.28), Triple(100.0, 120.0, 0.3), Triple(120.0, null, 0.33))
        val VOLUME_TIERS = t(Triple(0.0, 55.0, 0.0), Triple(55.0, 70.0, 0.05), Triple(70.0, 85.0, 0.1), Triple(85.0, 100.0, 0.2), Triple(100.0, null, 0.3))
        val COLLECTION_TIERS = t(Triple(0.0, 10.0, 0.1), Triple(10.0, 13.0, 0.05), Triple(13.0, null, 0.0))

        /** Matches when value > from and ≤ to; the first tier starts at 0 inclusive (portal `tierFor`). */
        fun tierFor(tiers: List<Tier>, value: Double): Tier =
            tiers.firstOrNull { (value > it.from || it.from == 0.0) && (it.to == null || value <= it.to) } ?: tiers.last()

        fun pct(value: Long, target: Long): Double = if (target <= 0) 0.0 else Math.round(value * 1000.0 / target) / 10.0

        fun dateOf(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ActivityRules.ZONE).toLocalDate()

        private fun seed(s: String): Long = s.fold(7L) { a, c -> a * 31 + c.code }.let { if (it < 0) -it else it }

        private val FIRST = listOf("Ahmad", "Siti", "Budi", "Dewi", "Rizal", "Nur", "Agus", "Rina", "Hendra", "Yuni", "Fajar", "Lia", "Dodi", "Maya", "Joko", "Intan", "Wahyu", "Ratna", "Bayu", "Sari")
        private val LAST = listOf("Saputra", "Wijaya", "Lestari", "Hidayat", "Pratama", "Kusuma", "Siregar", "Nasution", "Santoso", "Rahayu", "Gunawan", "Halim", "Utami", "Purnomo", "Sitompul")
        private val REJECT = listOf("Skor kredit tidak memenuhi syarat", "Data penghasilan tidak sesuai", "Dokumen identitas tidak valid", "Pengajuan ganda")
    }
}
