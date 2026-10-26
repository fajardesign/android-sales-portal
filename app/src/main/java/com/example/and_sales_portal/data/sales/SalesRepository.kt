package com.example.and_sales_portal.data.sales

import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.OperationsRepository
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.demo.seededRandom
import com.example.and_sales_portal.data.device.Simulation
import com.example.and_sales_portal.data.partner.AppNotification
import com.example.and_sales_portal.data.partner.IncomeNotice
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.data.partner.PartnerStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

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

    /**
     * Date of each portal status reached (SCR-3-03): Diajukan, then Disetujui, Ditolak or Cair. The sample keeps no approval
     * time for a paid-out loan, so it is placed halfway between submission and payout (prototype data).
     */
    val statusDates: List<Pair<PortalStatus, Long>> get() = when (status) {
        LoanStatus.SUBMITTED, LoanStatus.IN_PROCESS -> listOf(PortalStatus.DIAJUKAN to submittedAt)
        LoanStatus.APPROVED -> listOf(PortalStatus.DIAJUKAN to submittedAt, PortalStatus.DISETUJUI to updatedAt)
        LoanStatus.REJECTED -> listOf(PortalStatus.DIAJUKAN to submittedAt, PortalStatus.DITOLAK to updatedAt)
        LoanStatus.PAID_OUT -> listOf(PortalStatus.DIAJUKAN to submittedAt, PortalStatus.DISETUJUI to submittedAt + (updatedAt - submittedAt) / 2, PortalStatus.CAIR to (paidOutAt ?: updatedAt))
    }
}

/** A loan of the pipeline with its portal status; [paidLater] = Cair in a later month, not counted (FR-LP-005). */
data class PipelineLoan(val loan: Loan, val status: PortalStatus, val counted: Boolean, val paidLater: Boolean)

/** Count and amount of one funnel card. */
data class Funnel(val count: Int, val amount: Long)

/** Pipeline of a month (FR-LP-004/005): funnel per portal status, "Dihitung untuk insentif", and the filtered list. */
data class Pipeline(
    val funnel: Map<PortalStatus, Funnel>,
    val total: Int,
    val countedAmount: Long,
    val countedCount: Int,
    val paidLaterCount: Int,
    val rows: List<PipelineLoan>,
    val asOf: Long,
)

/** Payment of a Final row (FR-PY-006): amount to pay = take home pay + Admin correction. */
data class Payment(
    val status: PayStatus,
    val correction: Long,
    val note: String,
    val amountToPay: Long,
    val paidAt: LocalDate?,
    val paidAmount: Long?,
    val scheduledAt: LocalDate,
    val overdue: Boolean,
)

/** One person's month (web `salesRow`): figures, month status and payment. */
data class SalesRow(
    val userId: Long,
    val name: String,
    val role: S3Role,
    val leaderId: Long?,
    val areaId: Int,
    val loanCount: Int,
    val calc: SalesCalc,
    val month: YearMonth,
    val monthStatus: MonthStatus,
    val payment: Payment?,
)

/** Team or overall totals of rows (TL Tim, Beranda "Total take home pay tim"). */
data class Totals(val basicSalary: Long, val incentive: Long, val takeHomePay: Long, val actual: Long, val people: Int)

/**
 * Loans (synced from the loan system) and PRD Scope 3 monthly figures of the prototype. Loans are generated the same way
 * as the web portal sample (`admin-sales-portal/src/api/db.js`); the rules are the web's `scope3.js` ([Scope3]).
 * Spec: PRF-01 … PRF-06 (SCR-3-01 … SCR-3-05; FR-LP, FR-IN, FR-DF, FR-TG, FR-PY-006).
 */
class SalesRepository(
    private val partners: PartnerRepository,
    private val ops: OperationsRepository,
    private val simulation: () -> Simulation = { Simulation() },
    private val clock: () -> Instant = Instant::now,
) {

    private var cache: Pair<String, List<Loan>>? = null

    fun today(): LocalDate = ops.today()

    /** Every loan of Active (and later Inactive) partners up to now. */
    fun loans(): List<Loan> {
        val key = System.identityHashCode(partners.partners.value).toString() + partners.assignments.value.events.size + today()
        cache?.takeIf { it.first == key }?.let { return it.second }
        val now = clock().toEpochMilli()
        val list = partners.partners.value.flatMap { generate(it, now) }
        cache = key to list
        return list
    }

    fun loansOfSales(employeeId: Long) = loans().filter { it.salesId == employeeId }
    fun loansOfTl(tlId: Long) = loans().filter { it.tlId == tlId }
    fun loansOfPartner(partnerId: String) = loans().filter { it.partnerId == partnerId }
    fun loan(id: String): Loan? = loans().firstOrNull { it.id == id }

    // region PRD Scope 3: months

    /** "Today" of the month status: the Simulasi demo date ("Tanggal Scope 3") or the device date. */
    fun s3Today(): LocalDate = simulation().s3Date.date ?: today()

    fun currentMonth(): YearMonth = YearMonth.from(s3Today())

    fun monthStatus(ym: YearMonth): MonthStatus = Scope3.monthStatus(ym, currentMonth(), s3Today())

    /** Last 12 months with data, newest first (the configuration starts in May 2026). */
    fun months(): List<YearMonth> {
        val cur = currentMonth()
        val first = maxOf(FIRST_MONTH, cur.minusMonths(11))
        return generateSequence(cur) { it.minusMonths(1) }.takeWhile { it >= first }.toList()
    }

    /** "Data per": the time of the last fetch (prototype: now). */
    fun asOf(): Long = clock().toEpochMilli()

    // endregion

    // region PRD Scope 3: monthly rows

    private val finals = mutableMapOf<Pair<YearMonth, Long>, Figures?>()

    private data class Figures(val name: String, val role: S3Role, val leaderId: Long?, val areaId: Int, val loanCount: Int, val calc: SalesCalc)

    /** Valid days (FR-DF-001): check in and check out, or corrected by the TL; Absen and Tidak check in are not valid. */
    fun validDays(employeeId: Long, ym: YearMonth): Int {
        val today = ops.todayOf(employeeId)
        return ops.data.value.attendance.count { a ->
            a.employeeId == employeeId && a.date.startsWith(ym.toString()) && LocalDate.parse(a.date).let { d ->
                ActivityRules.attendanceStatus(a, d, today).let { it == AttendanceStatus.ON_TIME || it == AttendanceStatus.LATE } &&
                    (a.clockOut != null || a.statusOverride != null)
            }
        }
    }

    private fun roleOf(p: DemoDirectory.Person): S3Role? = runCatching { S3Role.valueOf(p.role) }.getOrNull()

    /** SA/SR/TL with a row in [ym]: attributed loans or attendance in the month, or still active in the current month. */
    private fun hasRow(p: DemoDirectory.Person, ym: YearMonth): Boolean {
        if (roleOf(p) == null) return false
        if (ym == currentMonth() && p.status == "ACTIVE") return true
        if (loans().any { (it.salesId == p.id || it.tlId == p.id) && YearMonth.from(dateOf(it.submittedAt)) == ym }) return true
        return ops.data.value.attendance.any { it.employeeId == p.id && it.date.startsWith(ym.toString()) }
    }

    private fun figures(p: DemoDirectory.Person, ym: YearMonth): Figures? {
        val role = roleOf(p) ?: return null
        if (!hasRow(p, ym)) return null
        val ls = loans().filter { (if (role == S3Role.TL) it.tlId else it.salesId) == p.id && YearMonth.from(dateOf(it.submittedAt)) == ym }
        val actual = ls.filter { Scope3.countsFor(it, ym) }.sumOf { it.amount }
        val calc = Scope3.calcSales(role, actual, validDays(p.id, ym), p.areaId)
        return Figures(p.fullName, role, if (role == S3Role.TL) null else p.supervisorId, p.areaId, ls.size, calc)
    }

    /** One person's month, or `null` when they have no row. Final rows are kept as first read after the lock. */
    fun salesRow(employeeId: Long, ym: YearMonth): SalesRow? {
        val p = DemoDirectory.person(employeeId) ?: return null
        val status = monthStatus(ym)
        val f = if (status == MonthStatus.FINAL) finals.getOrPut(ym to employeeId) { figures(p, ym) } else figures(p, ym)
        f ?: return null
        return SalesRow(employeeId, f.name, f.role, f.leaderId, f.areaId, f.loanCount, f.calc, ym, status, paymentOf(employeeId, ym, f.calc.takeHomePay))
    }

    /** Visibility (FR-IN-006): SA/SR only their own row; TL their own row then the SA/SR they lead; never peers. */
    fun rowsFor(viewerId: Long, role: SalesRole, ym: YearMonth): List<SalesRow> {
        val own = listOfNotNull(salesRow(viewerId, ym))
        if (role != SalesRole.TL) return own
        val team = DemoDirectory.people.filter { it.supervisorId == viewerId && it.role in setOf("SA", "SR") }
            .mapNotNull { salesRow(it.id, ym) }
            .sortedWith(compareBy<SalesRow>({ listOf(S3Role.TL, S3Role.SR, S3Role.SA).indexOf(it.role) }, { it.name }))
        return own + team
    }

    /** Whether [viewerId] may see [employeeId]'s figures: themselves, or the TL of that SA/SR. */
    fun canView(viewerId: Long, employeeId: Long): Boolean =
        viewerId == employeeId || DemoDirectory.person(employeeId)?.let { it.supervisorId == viewerId && it.role in setOf("SA", "SR") } == true

    fun totals(rows: List<SalesRow>) = Totals(rows.sumOf { it.calc.basicSalary }, rows.sumOf { it.calc.incentive }, rows.sumOf { it.calc.takeHomePay }, rows.sumOf { it.calc.actual }, rows.size)

    /** Income history: the last 12 months with a row, newest first. */
    fun history(employeeId: Long): List<SalesRow> = months().mapNotNull { salesRow(employeeId, it) }

    // endregion

    // region PRD Scope 3: pipeline

    /**
     * Loans submitted in [ym] in the scope ([salesId] for SA/SR, [tlId] for a TL), with their current portal status.
     * Filters: [status], [storeId], [salesFilter] (`null` all, [SALES_NONE] loans without SA/SR, else that SA/SR).
     */
    fun pipeline(ym: YearMonth, salesId: Long? = null, tlId: Long? = null, status: PortalStatus? = null, storeId: String? = null, salesFilter: Long? = null): Pipeline {
        val all = loans().filter { YearMonth.from(dateOf(it.submittedAt)) == ym && (salesId == null || it.salesId == salesId) && (tlId == null || it.tlId == tlId) }
        val funnel = PortalStatus.entries.associateWith { st -> all.filter { Scope3.portalStatus(it) == st }.let { Funnel(it.size, it.sumOf { l -> l.amount }) } }
        val counted = all.filter { Scope3.countsFor(it, ym) }
        val rows = all.filter { l ->
            (status == null || Scope3.portalStatus(l) == status) && (storeId == null || l.storeId == storeId) &&
                (salesFilter == null || (if (salesFilter == SALES_NONE) l.salesId == null else l.salesId == salesFilter))
        }.sortedByDescending { it.submittedAt }.map { l ->
            val st = Scope3.portalStatus(l)
            val c = Scope3.countsFor(l, ym)
            PipelineLoan(l, st, c, st == PortalStatus.CAIR && !c)
        }
        return Pipeline(funnel, all.size, counted.sumOf { it.amount }, counted.size, all.count { Scope3.portalStatus(it) == PortalStatus.CAIR && !Scope3.countsFor(it, ym) }, rows, asOf())
    }

    // endregion

    // region PRD Scope 3: payment and notifications

    /** The latest Final month (Simulasi "Admin tandai dibayar"). */
    private fun latestFinal(): YearMonth? = months().firstOrNull { monthStatus(it) == MonthStatus.FINAL }

    /** Payment of a Final row (FR-PY): sample data paid up to August 2026; SR Siti August with a correction of Rp 150.000. */
    fun paymentOf(employeeId: Long, ym: YearMonth, finalAmount: Long): Payment? {
        if (monthStatus(ym) != MonthStatus.FINAL) return null
        val r = PAYMENTS[employeeId to ym]
        val correction = r?.first ?: 0L
        val amount = maxOf(0L, finalAmount + correction)
        val scheduled = Scope3.payoutDate(ym)
        val simPaid = if (simulation().s3PaidLatest && ym == latestFinal()) s3Today() else null
        val paidAt = simPaid ?: if (ym <= PAID_THROUGH && amount > 0) scheduled else null
        val status = when {
            paidAt != null && amount > 0 -> PayStatus.DIBAYAR
            amount > 0 -> PayStatus.MENUNGGU
            else -> PayStatus.TIDAK_ADA
        }
        return Payment(
            status, correction, r?.second.orEmpty(), amount, paidAt.takeIf { status == PayStatus.DIBAYAR },
            amount.takeIf { status == PayStatus.DIBAYAR }, scheduled, status == PayStatus.MENUNGGU && s3Today().isAfter(scheduled),
        )
    }

    /**
     * Income notifications of [employeeId] (FR-IN-005, FR-PY-006): "sudah final" at the lock (00:00 WIB) and "sudah dibayar"
     * on the payment date (10:00 WIB), up to the demo date. Kept 30 days like every notification.
     */
    fun incomeNotices(employeeId: Long): List<AppNotification> = months().filter { monthStatus(it) == MonthStatus.FINAL }.flatMap { ym ->
        val row = salesRow(employeeId, ym) ?: return@flatMap emptyList()
        val lockAt = Scope3.lockDate(ym).atStartOfDay(ActivityRules.LOCK_ZONE).toInstant().toEpochMilli()
        val c = row.calc
        val final = AppNotification(
            "N-S3-FINAL-$ym-$employeeId", employeeId,
            income = IncomeNotice(lockAt, ym.toString(), paid = false, takeHomePay = c.takeHomePay, basicSalary = c.basicSalary, incentive = c.incentive, payoutDate = Scope3.payoutDate(ym).toString()),
        )
        val p = row.payment
        val paid = if (p?.status == PayStatus.DIBAYAR && p.paidAt != null && !p.paidAt.isAfter(s3Today())) {
            AppNotification(
                "N-S3-PAID-$ym-$employeeId", employeeId,
                income = IncomeNotice(p.paidAt.atTime(10, 0).atZone(ActivityRules.LOCK_ZONE).toInstant().toEpochMilli(), ym.toString(), paid = true, amount = p.paidAmount ?: 0, correction = p.correction),
            )
        } else null
        listOfNotNull(final, paid)
    }

    /** Puts the income notifications of [employeeId] in the inbox (read state kept). */
    fun syncIncomeNotices(employeeId: Long) = partners.syncIncomeNotices(employeeId, incomeNotices(employeeId))

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
                // Sample loan pattern (not an attendance rule): no loans on Sundays.
                if (d.dayOfWeek != java.time.DayOfWeek.SUNDAY) {
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
                            // The SA/SR who held the toko on that date: past sales stay with them after a Lepas (fix 2026-10-14).
                            salesId = partners.holderAt(s.id, submittedAt),
                            // The TL of the toko on that date: past sales stay with the old TL (PRD Scope 1 v2, TL per toko).
                            tlId = s.tlAt(submittedAt),
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

        /** Pipeline filter "Tanpa SA/SR": loans of toko without an SA/SR on the submission date. */
        const val SALES_NONE = -1L

        /** First month of the Scope 3 configuration (versions effective from May 2026, same as the web). */
        val FIRST_MONTH: YearMonth = YearMonth.of(2026, 5)

        /** Sample payments (web `s3Payments`): Final months up to August 2026 paid on the scheduled date. */
        val PAID_THROUGH: YearMonth = YearMonth.of(2026, 8)

        /** Admin corrections (amount, note) per person and month: SR Siti Rahmawati, August 2026, +Rp 150.000. */
        val PAYMENTS: Map<Pair<Long, YearMonth>, Pair<Long, String>> = mapOf((11L to YearMonth.of(2026, 8)) to (150_000L to "Koreksi Juli 2026"))

        fun dateOf(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ActivityRules.ZONE).toLocalDate()

        private fun seed(s: String): Long = s.fold(7L) { a, c -> a * 31 + c.code }.let { if (it < 0) -it else it }

        private val FIRST = listOf("Ahmad", "Siti", "Budi", "Dewi", "Rizal", "Nur", "Agus", "Rina", "Hendra", "Yuni", "Fajar", "Lia", "Dodi", "Maya", "Joko", "Intan", "Wahyu", "Ratna", "Bayu", "Sari")
        private val LAST = listOf("Saputra", "Wijaya", "Lestari", "Hidayat", "Pratama", "Kusuma", "Siregar", "Nasution", "Santoso", "Rahayu", "Gunawan", "Halim", "Utami", "Purnomo", "Sitompul")
        private val REJECT = listOf("Skor kredit tidak memenuhi syarat", "Data penghasilan tidak sesuai", "Dokumen identitas tidak valid", "Pengajuan ganda")
    }
}
