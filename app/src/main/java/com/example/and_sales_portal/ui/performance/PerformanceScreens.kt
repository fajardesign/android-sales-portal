package com.example.and_sales_portal.ui.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.device.PerfData
import com.example.and_sales_portal.data.partner.Store
import com.example.and_sales_portal.data.sales.Band
import com.example.and_sales_portal.data.sales.Loan
import com.example.and_sales_portal.data.sales.MonthStatus
import com.example.and_sales_portal.data.sales.Pipeline
import com.example.and_sales_portal.data.sales.PipelineLoan
import com.example.and_sales_portal.data.sales.PortalStatus
import com.example.and_sales_portal.data.sales.SalesRepository
import com.example.and_sales_portal.data.sales.SalesRow
import com.example.and_sales_portal.data.sales.Totals
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.actions.AmarChip
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
import com.example.and_sales_portal.designsystem.components.inputs.AmarDropdownPicker
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.components.layout.AmarDivider
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.attendance.AttendanceBadge
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.InfoRow
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.common.SectionTitle
import com.example.and_sales_portal.ui.partner.rupiah
import java.time.YearMonth

// region PRF-01 Beranda card "Performa bulan ini" (SCR-3-01)

/**
 * Beranda card state: [row] this month, [prev] last month while it waits for the lock, [hasStore] the user holds a toko,
 * [teamThp] the TL's "Total take home pay tim" (own row + the SA/SR they lead).
 */
data class PerformanceCardState(
    val load: PerfData,
    val row: SalesRow?,
    val prev: SalesRow?,
    val hasStore: Boolean,
    val teamThp: Long?,
    val asOf: Long,
)

/** Builds the PRF-01 card of [id]: this month, last month while it waits for the lock, and for a TL the team total. */
fun performanceCardState(sales: SalesRepository, id: Long, role: com.example.and_sales_portal.data.auth.SalesRole, hasStore: Boolean, load: PerfData): PerformanceCardState {
    val month = sales.currentMonth()
    val prev = month.minusMonths(1).takeIf { sales.monthStatus(it) == MonthStatus.REKONSILIASI }?.let { sales.salesRow(id, it) }
    val teamThp = if (role == com.example.and_sales_portal.data.auth.SalesRole.TL) sales.totals(sales.rowsFor(id, role, month)).takeHomePay else null
    return PerformanceCardState(load, sales.salesRow(id, month), prev, hasStore, teamThp, sales.asOf())
}

/**
 * Spec: PRF-01 (SCR-3-01, FR-DF-003, FR-TG-001) Beranda card "Performa bulan ini": estimated take home pay with basic
 * salary (×2 badge) and incentive, Cair dihitung against the target with the achievement bar and band, last month while
 * waiting for the lock, the TL team total and "Data per". States: loading, populated, zero, no toko, error.
 */
@Composable
fun PerformanceCard(s: PerformanceCardState, onDetail: () -> Unit, onRetry: () -> Unit) {
    val c = AmarTheme.colors
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.s3_card_title), Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.headingSm)
            if (s.load == PerfData.NORMAL || s.load == PerfData.LMT_ERROR) AmarButton(stringResource(R.string.s3_see_detail), onDetail, type = AmarButtonType.Naked, size = AmarButtonSize.Small)
        }
        when {
            s.load == PerfData.LOADING -> PerfLoading()
            s.load == PerfData.ERROR -> PerfError(onRetry)
            else -> {
                if (s.load == PerfData.LMT_ERROR) LmtBanner(s.asOf)
                s.row?.let { r -> CardContent(s, r) }
            }
        }
    }
}

@Composable
private fun CardContent(s: PerformanceCardState, r: SalesRow) {
    val c = AmarTheme.colors
    Text(stringResource(R.string.s3_card_thp, rupiah(r.takeHomePay)), color = c.content.primary, style = AmarTheme.typography.headingMd)
    ValueRow(stringResource(R.string.s3_basic), rupiah(r.calc.basicSalary)) { MultiplierBadge(r) }
    ValueRow(stringResource(R.string.s3_incentive), rupiah(r.calc.incentive))
    r.payment?.takeIf { it.correction != 0L }?.let { ValueRow(stringResource(R.string.s3_correction_label), signedRupiah(it.correction)) }
    if (!s.hasStore) {
        AmarInfoBox(stringResource(R.string.s3_no_store), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
    } else {
        ValueRow(stringResource(R.string.s3_counted), rupiah(r.calc.actual))
        ValueRow(stringResource(R.string.s3_target), rupiah(r.calc.target))
        AchievementBar(r)
        if (r.calc.actual == 0L) Text(stringResource(R.string.s3_zero), color = c.content.secondary, style = AmarTheme.typography.bodySm)
    }
    // FR-IN-003, AC-058: last month until the cut-off, take home pay with the Koreksi.
    s.prev?.let { p ->
        AmarDivider()
        Text(stringResource(R.string.s3_prev_month, Fmt.month(p.month), rupiah(p.takeHomePay)), color = c.content.primary, style = AmarTheme.typography.bodySm)
        p.payment?.let { pay ->
            Text(stringResource(R.string.s3_note_recon, Fmt.day(pay.lockAt)), color = c.content.secondary, style = AmarTheme.typography.captionSm)
            if (pay.correction != 0L) Text(correctionText(pay), color = c.content.secondary, style = AmarTheme.typography.captionSm)
        } ?: Text(monthNote(p.month, p.monthStatus), color = c.content.secondary, style = AmarTheme.typography.captionSm)
    }
    s.teamThp?.let { Text(stringResource(R.string.s3_team_thp, rupiah(it)), color = c.content.primary, style = AmarTheme.typography.labelMdSemibold) }
    AsOfLine(s.asOf)
}

// endregion

// region PRF-02 Pipeline (SCR-3-02)

/** Pipeline filters and options; [team] is set for a TL (filter per SA/SR). */
data class PipelineState(
    val month: YearMonth,
    val months: List<Pair<YearMonth, MonthStatus>>,
    val pipeline: Pipeline,
    val status: PortalStatus?,
    val stores: List<Store>,
    val storeId: String?,
    val team: List<DemoDirectory.Person>?,
    val salesFilter: Long?,
)

class PipelineActions(
    val onMonth: (YearMonth) -> Unit,
    val onStatus: (PortalStatus?) -> Unit,
    val onStore: (String?) -> Unit,
    val onSales: (Long?) -> Unit,
    val onOpen: (Loan) -> Unit,
)

/**
 * Spec: PRF-02 (SCR-3-02, FR-LP-004/005) Pipeline Pinjaman: month, funnel per portal status (the Cair card with
 * "Dihitung untuk insentif"), filters (TL: SA/SR and toko; everyone: status), the loans newest first, "Data per".
 */
@Composable
fun ColumnScope.PipelineSection(s: PipelineState, a: PipelineActions) {
    val c = AmarTheme.colors
    val month = Fmt.month(s.month)
    Text(stringResource(R.string.s3_pipeline_title), color = c.content.primary, style = AmarTheme.typography.headingSm)
    Text(stringResource(R.string.s3_pipeline_helper, month), color = c.content.secondary, style = AmarTheme.typography.bodySm)
    MonthPicker(s.month, s.months, a.onMonth)
    val p = s.pipeline
    Row(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
        FunnelTile(PortalStatus.DIAJUKAN, p)
        FunnelTile(PortalStatus.DISETUJUI, p)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
        FunnelTile(PortalStatus.DITOLAK, p)
        FunnelTile(PortalStatus.CAIR, p) {
            Text(stringResource(R.string.s3_counted_line, rupiah(p.countedAmount), p.countedCount), color = c.feedback.success.text, style = AmarTheme.typography.captionSm)
        }
    }
    s.team?.let { team ->
        val all = stringResource(R.string.s3_filter_all)
        val none = stringResource(R.string.s3_no_sales)
        AmarDropdownPicker(
            value = s.salesFilter ?: ALL,
            options = listOf(ALL) + team.map { it.id } + SalesRepository.SALES_NONE,
            onSelect = { a.onSales(it.takeIf { id -> id != ALL }) },
            sheetTitle = stringResource(R.string.s3_filter_sales_sheet),
            modifier = Modifier.fillMaxWidth(),
            optionLabel = { id -> when (id) { ALL -> all; SalesRepository.SALES_NONE -> none; else -> team.firstOrNull { it.id == id }?.fullName.orEmpty() } },
            label = stringResource(R.string.s3_filter_sales),
        )
        val allStores = stringResource(R.string.loan_filter_all_stores)
        AmarDropdownPicker(
            value = s.stores.firstOrNull { it.id == s.storeId },
            options = listOf<Store?>(null) + s.stores,
            onSelect = { a.onStore(it?.id) },
            sheetTitle = stringResource(R.string.loan_filter_sheet_store),
            modifier = Modifier.fillMaxWidth(),
            optionLabel = { it?.name ?: allStores },
            label = stringResource(R.string.loan_filter_store),
            placeholder = allStores,
        )
    }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
        AmarChip(stringResource(R.string.s3_filter_all), s.status == null, { a.onStatus(null) })
        PortalStatus.entries.forEach { st -> AmarChip(portalLabel(st), s.status == st, { a.onStatus(st) }) }
    }
    AsOfLine(p.asOf)
    when {
        p.total == 0 -> AmarInfoBox(stringResource(R.string.s3_pipeline_empty, month), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
        p.rows.isEmpty() -> AmarInfoBox(stringResource(R.string.loan_empty), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
    }
    val names = s.stores.associate { it.id to it.name }
    p.rows.forEach { l -> LoanRow(l, names[l.loan.storeId], onClick = { a.onOpen(l.loan) }) }
}

private const val ALL = -2L

@Composable
private fun RowScope.FunnelTile(st: PortalStatus, p: Pipeline, extra: (@Composable () -> Unit)? = null) {
    val c = AmarTheme.colors
    val f = p.funnel.getValue(st)
    Column(
        Modifier
            .weight(1f)
            .background(c.background.base, RoundedCornerShape(AmarActivityTokens.TileRadius))
            .padding(AmarActivityTokens.TilePadding),
        verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap),
    ) {
        PortalStatusBadge(st)
        Text(f.count.toString(), color = c.content.primary, style = AmarTheme.typography.headingMd)
        Text(rupiah(f.amount), color = c.content.secondary, style = AmarTheme.typography.captionSm)
        extra?.invoke()
    }
}

// endregion

// region PRF-03 Detail Pinjaman (SCR-3-03)

/**
 * Spec: PRF-03 (SCR-3-03, FR-LP-005) Detail Pinjaman, read-only: ref no, masked customer name, toko, amount, status, the
 * date of each status, the attributed SA/SR and TL. No NIK, phone or address.
 */
@Composable
fun LoanDetailScreen(l: PipelineLoan, storeName: String?, salesName: String?, tlName: String?, onBack: () -> Unit) {
    val c = AmarTheme.colors
    val none = stringResource(R.string.s3_loan_none)
    ScreenScaffold(stringResource(R.string.s3_loan_title), onBack) {
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(l.loan.maskedCustomer, Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.headingSm)
                PortalStatusBadge(l.status)
            }
            InfoRow(stringResource(R.string.s3_loan_ref), l.loan.id)
            InfoRow(stringResource(R.string.s3_loan_customer), l.loan.maskedCustomer)
            InfoRow(stringResource(R.string.s3_loan_store), storeName ?: none)
            InfoRow(stringResource(R.string.s3_loan_amount), rupiah(l.loan.amount))
            InfoRow(stringResource(R.string.s3_loan_status), portalLabel(l.status))
            InfoRow(stringResource(R.string.s3_loan_sales), salesName ?: stringResource(R.string.s3_no_sales))
            InfoRow(stringResource(R.string.s3_loan_tl), tlName ?: none)
            if (l.paidLater) PaidLaterLine(l)
        }
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
            SectionTitle(stringResource(R.string.s3_loan_dates))
            l.loan.statusDates.forEach { (st, at) -> InfoRow(portalLabel(st), Fmt.dateTimeWib(at)) }
        }
    }
}

// endregion

// region PRF-04 Penghasilan (SCR-3-04)

/** Penghasilan of one person: selected month, its row, the 12-month history; [readOnly] = a TL viewing an SA/SR. */
data class IncomeState(
    val load: PerfData,
    val month: YearMonth,
    val months: List<Pair<YearMonth, MonthStatus>>,
    val row: SalesRow?,
    val areaName: String,
    val history: List<SalesRow>,
    val asOf: Long,
)

class IncomeActions(
    val onMonth: (YearMonth) -> Unit,
    val onAttendance: (YearMonth) -> Unit,
    val onMonthDetail: (YearMonth) -> Unit,
    val onRetry: () -> Unit,
)

/**
 * Spec: PRF-04 (SCR-3-04, FR-IN-001/004, FR-DF-003, FR-PY-006) Penghasilan: month with its status, Gaji pokok block
 * ("Lihat absensi"), Insentif block with the tier table, Take home pay with the payment line, and the income history of
 * the last 12 months (tap opens the month detail).
 */
@Composable
fun ColumnScope.IncomeSection(s: IncomeState, a: IncomeActions) {
    val c = AmarTheme.colors
    MonthPicker(s.month, s.months, a.onMonth)
    when (s.load) {
        PerfData.LOADING -> { PerfLoading(); return }
        PerfData.ERROR -> { PerfError(a.onRetry); return }
        PerfData.LMT_ERROR -> LmtBanner(s.asOf)
        PerfData.NORMAL -> Unit
    }
    val r = s.row
    if (r == null) {
        AmarInfoBox(stringResource(R.string.s3_no_row), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
    } else {
        MonthHeader(r)
        BasicSalaryBlock(r, s.areaName) { a.onAttendance(r.month) }
        IncentiveBlock(r)
        TakeHomePayBlock(r)
        AmarButton(stringResource(R.string.s3_month_detail), { a.onMonthDetail(r.month) }, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium)
    }
    AsOfLine(s.asOf)
    SectionTitle(stringResource(R.string.s3_history))
    if (s.history.isEmpty()) Text(stringResource(R.string.s3_history_empty), color = c.content.secondary, style = AmarTheme.typography.bodySm)
    s.history.forEach { h ->
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border, onClick = { a.onMonthDetail(h.month) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(Fmt.month(h.month), Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                MonthStatusBadge(h.monthStatus)
            }
            ValueRow(stringResource(R.string.s3_thp), rupiah(h.takeHomePay)) { PaymentBadge(h.payment) }
            Text(thpFormula(h), color = c.content.secondary, style = AmarTheme.typography.bodySm)
            PaymentLines(h.payment)
        }
    }
}

@Composable
private fun MonthHeader(r: SalesRow) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
        MonthStatusBadge(r.monthStatus)
        Text(monthNote(r.month, r.monthStatus), color = AmarTheme.colors.content.secondary, style = AmarTheme.typography.bodySm)
    }
}

/**
 * Spec: PRF-04 month detail: the blocks of one month and its loans, counted and struck through (paid out in a later
 * month). [subtitle] = name and role when a TL views an SA/SR.
 */
@Composable
fun IncomeMonthScreen(r: SalesRow?, month: YearMonth, subtitle: String?, areaName: String, loans: List<PipelineLoan>, onAttendance: () -> Unit, onBack: () -> Unit) {
    val c = AmarTheme.colors
    ScreenScaffold(Fmt.month(month), onBack) {
        subtitle?.let { Text(it, color = c.content.secondary, style = AmarTheme.typography.bodySm) }
        if (r == null) {
            AmarInfoBox(stringResource(R.string.s3_no_row), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
            return@ScreenScaffold
        }
        MonthHeader(r)
        BasicSalaryBlock(r, areaName, onAttendance)
        IncentiveBlock(r)
        TakeHomePayBlock(r)
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
            Text(stringResource(R.string.s3_month_loans), color = c.content.primary, style = AmarTheme.typography.headingSm)
            if (loans.isEmpty()) Text(stringResource(R.string.s3_month_loans_empty), color = c.content.secondary, style = AmarTheme.typography.bodySm)
            loans.forEachIndexed { i, l ->
                if (i > 0) AmarDivider()
                MonthLoanLine(l)
            }
        }
    }
}

// endregion

// region PRF-05 Tim (SCR-3-05)

/** A toko the TL holds, with its funnel in the month and its SA/SR (`null` = "Tanpa SA/SR"). */
data class StoreFunnel(val store: Store, val partnerName: String, val counts: Map<PortalStatus, Int>, val sales: String?)

/** Tim sub-tab of the TL (PRF-05). */
data class TeamState(
    val load: PerfData,
    val month: YearMonth,
    val months: List<Pair<YearMonth, MonthStatus>>,
    /** The TL's own row first, then each SA/SR. */
    val rows: List<SalesRow>,
    val totals: Totals,
    /** Cair dihitung of the toko the TL holds (the TL's own actual). */
    val teamCounted: Long,
    val today: Map<Long, AttendanceStatus?>,
    val stores: List<StoreFunnel>,
    val teamSize: Int,
    val asOf: Long,
)

class TeamActions(
    val onMonth: (YearMonth) -> Unit,
    val onOpenIncome: (SalesRow) -> Unit,
    val onMember: (Long) -> Unit,
    val onRetry: () -> Unit,
)

/**
 * Spec: PRF-05 (SCR-3-05, FR-TG-002, FR-IN-006) Tim: summary (team Cair dihitung, "Total take home pay tim", people per
 * band), the TL's own row then each SA/SR (valid days, Cair dihitung, target, achievement, band, tier, basic salary with
 * ×2, incentive, take home pay and payment in a Final month), and the toko the TL holds with their funnel and SA/SR.
 * Tapping an SA/SR opens their Penghasilan (read-only); "Detail anggota" opens TL-02.
 */
@Composable
fun ColumnScope.TeamSection(s: TeamState, a: TeamActions, youLabel: (String) -> String, pending: @Composable ColumnScope.() -> Unit = {}) {
    val c = AmarTheme.colors
    MonthPicker(s.month, s.months, a.onMonth)
    when (s.load) {
        PerfData.LOADING -> { PerfLoading(); return }
        PerfData.ERROR -> { PerfError(a.onRetry); return }
        PerfData.LMT_ERROR -> LmtBanner(s.asOf)
        PerfData.NORMAL -> Unit
    }
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
        Text(stringResource(R.string.s3_team_summary), color = c.content.primary, style = AmarTheme.typography.headingSm)
        ValueRow(stringResource(R.string.s3_team_counted), rupiah(s.teamCounted))
        ValueRow(stringResource(R.string.s3_team_total_thp), rupiah(s.totals.takeHomePay))
        Text(
            if (s.totals.correction != 0L) stringResource(R.string.s3_thp_formula_correction, rupiah(s.totals.basicSalary), rupiah(s.totals.incentive), signedRupiah(s.totals.correction))
            else stringResource(R.string.s3_thp_formula, rupiah(s.totals.basicSalary), rupiah(s.totals.incentive)),
            color = c.content.secondary, style = AmarTheme.typography.bodySm,
        )
        val bands = s.rows.groupingBy { it.calc.band }.eachCount()
        Text(stringResource(R.string.s3_team_bands, bands[Band.TERCAPAI] ?: 0, bands[Band.ON_TRACK] ?: 0, bands[Band.BERISIKO] ?: 0), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        AsOfLine(s.asOf)
    }
    s.rows.forEachIndexed { i, r ->
        val own = i == 0 && r.role == com.example.and_sales_portal.data.sales.S3Role.TL
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border, onClick = if (own) null else ({ a.onOpenIncome(r) })) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
                Text(if (own) youLabel(r.name) else "${r.name} (${r.role})", Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                s.today[r.userId]?.let { AttendanceBadge(it) }
            }
            ValueRow(stringResource(R.string.s3_valid_days), stringResource(R.string.team_days_value, r.calc.validDays))
            ValueRow(stringResource(R.string.s3_counted), rupiah(r.calc.actual))
            ValueRow(stringResource(R.string.s3_target), rupiah(r.calc.target))
            AchievementBar(r)
            ValueRow(stringResource(R.string.s3_tier), "${tierText(r.calc.tier)} · ${rateText(r.calc.coefficient)}%")
            ValueRow(stringResource(R.string.s3_basic), rupiah(r.calc.basicSalary)) { MultiplierBadge(r) }
            ValueRow(stringResource(R.string.s3_incentive), rupiah(r.calc.incentive))
            // Round 10: Koreksi and payment status per person from reconciliation on (FR-TG-002).
            r.payment?.takeIf { it.correction != 0L }?.let { ValueRow(stringResource(R.string.s3_correction_label), signedRupiah(it.correction)) }
            ValueRow(stringResource(R.string.s3_thp), rupiah(r.takeHomePay)) { PaymentBadge(r.payment) }
            PaymentLines(r.payment)
            if (!own) AmarButton(stringResource(R.string.s3_member_detail), { a.onMember(r.userId) }, type = AmarButtonType.Naked, size = AmarButtonSize.Small)
        }
    }
    if (s.teamSize == 0) AmarInfoBox(stringResource(R.string.s3_team_empty), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
    pending()
    SectionTitle(stringResource(R.string.s3_team_stores))
    s.stores.forEach { f ->
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
            Text(f.store.name, color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
            Text(f.partnerName, color = c.content.secondary, style = AmarTheme.typography.bodySm)
            Text(
                stringResource(
                    R.string.s3_store_funnel,
                    f.counts[PortalStatus.DIAJUKAN] ?: 0, f.counts[PortalStatus.DISETUJUI] ?: 0, f.counts[PortalStatus.DITOLAK] ?: 0, f.counts[PortalStatus.CAIR] ?: 0,
                ),
                color = c.content.secondary, style = AmarTheme.typography.bodySm,
            )
            Text(f.sales ?: stringResource(R.string.s3_no_sales), color = c.content.tertiary, style = AmarTheme.typography.bodySm)
        }
    }
}

// endregion
