package com.example.and_sales_portal.ui.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.sales.Band
import com.example.and_sales_portal.data.sales.MonthStatus
import com.example.and_sales_portal.data.sales.PayStatus
import com.example.and_sales_portal.data.sales.Payment
import com.example.and_sales_portal.data.sales.PipelineLoan
import com.example.and_sales_portal.data.sales.PortalStatus
import com.example.and_sales_portal.data.sales.S3Tier
import com.example.and_sales_portal.data.sales.SalesRepository
import com.example.and_sales_portal.data.sales.SalesRow
import com.example.and_sales_portal.data.sales.Scope3
import com.example.and_sales_portal.data.sales.S3Role
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.extended.AmarProgressBar
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadge
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeSize
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeType
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
import com.example.and_sales_portal.designsystem.components.feedback.AmarSpinner
import com.example.and_sales_portal.designsystem.components.inputs.AmarDropdownPicker
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBar
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBarType
import com.example.and_sales_portal.designsystem.components.navigation.AmarTab
import com.example.and_sales_portal.designsystem.components.navigation.AmarTabItem
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarFormTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.partner.rupiah
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale
import kotlin.math.abs

/** Shared parts of the PRD Scope 3 screens (PRF-01 … PRF-06), same labels as the web `S3Common.jsx`. */

private val ID = Locale("id", "ID")

/** "+Rp 150.000" / "−Rp 150.000" (web `signedRp`). */
fun signedRupiah(n: Long): String = (if (n > 0) "+" else if (n < 0) "−" else "") + rupiah(abs(n))

/** Achievement "106,7" (one decimal at most). */
fun pctText(v: Double): String = NumberFormat.getNumberInstance(ID).apply { maximumFractionDigits = 1 }.format(v)

/** Coefficient "0,85" (two decimals, web `rateLabel`). */
fun rateText(r: Double): String = NumberFormat.getNumberInstance(ID).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }.format(r)

/** "×2" or "×1,5". */
fun multiplierText(m: Double): String = NumberFormat.getNumberInstance(ID).apply { maximumFractionDigits = 2 }.format(m)

@Composable
fun monthStatusLabel(st: MonthStatus): String = stringResource(
    when (st) {
        MonthStatus.BERJALAN -> R.string.s3_month_running
        MonthStatus.REKONSILIASI -> R.string.s3_month_recon
        MonthStatus.FINAL -> R.string.s3_month_final
    },
)

/** Month status badge: Berjalan Info, Menunggu rekonsiliasi Warning, Final Success (web `MonthStatusBadge`). */
@Composable
fun MonthStatusBadge(st: MonthStatus) {
    val type = when (st) {
        MonthStatus.BERJALAN -> AmarBadgeType.Info
        MonthStatus.REKONSILIASI -> AmarBadgeType.Warning
        MonthStatus.FINAL -> AmarBadgeType.Success
    }
    AmarBadge(monthStatusLabel(st), type = type, size = AmarBadgeSize.Small)
}

/** Month note (FR-IN-001): estimate, waiting for the lock, or Final since the lock. */
@Composable
fun monthNote(ym: YearMonth, st: MonthStatus): String = when (st) {
    MonthStatus.BERJALAN -> stringResource(R.string.s3_note_running)
    MonthStatus.REKONSILIASI -> stringResource(R.string.s3_note_recon, Fmt.day(Scope3.lockDate(ym)))
    MonthStatus.FINAL -> stringResource(R.string.s3_note_final, Fmt.day(Scope3.lockDate(ym)))
}

/** Achievement band chip: Tercapai Success, On Track Info, Berisiko Error. */
@Composable
fun BandBadge(b: Band) {
    val (label, type) = when (b) {
        Band.TERCAPAI -> R.string.s3_band_tercapai to AmarBadgeType.Success
        Band.ON_TRACK -> R.string.s3_band_on_track to AmarBadgeType.Info
        Band.BERISIKO -> R.string.s3_band_berisiko to AmarBadgeType.Error
    }
    AmarBadge(stringResource(label), type = type, size = AmarBadgeSize.Small)
}

@Composable
fun portalLabel(st: PortalStatus): String = stringResource(
    when (st) {
        PortalStatus.DIAJUKAN -> R.string.s3_status_diajukan
        PortalStatus.DISETUJUI -> R.string.s3_status_disetujui
        PortalStatus.DITOLAK -> R.string.s3_status_ditolak
        PortalStatus.CAIR -> R.string.s3_status_cair
    },
)

/** Portal status chip, same colours as the web loan list: Diajukan Info, Disetujui Warning, Ditolak Error, Cair Success. */
@Composable
fun PortalStatusBadge(st: PortalStatus) {
    val type = when (st) {
        PortalStatus.DIAJUKAN -> AmarBadgeType.Info
        PortalStatus.DISETUJUI -> AmarBadgeType.Warning
        PortalStatus.DITOLAK -> AmarBadgeType.Error
        PortalStatus.CAIR -> AmarBadgeType.Success
    }
    AmarBadge(portalLabel(st), type = type, size = AmarBadgeSize.Small)
}

/** Month selector: last 12 months, newest first, with the month status ("Oktober 2026 · Berjalan"). */
@Composable
fun MonthPicker(value: YearMonth, months: List<Pair<YearMonth, MonthStatus>>, onSelect: (YearMonth) -> Unit) {
    val labels = months.associate { (m, st) -> m to stringResource(R.string.s3_month_option, Fmt.month(m), monthStatusLabel(st)) }
    AmarDropdownPicker(
        value = value,
        options = months.map { it.first },
        onSelect = onSelect,
        sheetTitle = stringResource(R.string.s3_month_sheet),
        modifier = Modifier.fillMaxWidth(),
        optionLabel = { labels[it] ?: Fmt.month(it) },
        label = stringResource(R.string.s3_month),
    )
}

/** "Data per 7 Okt 2026, 10:30 WIB" (FR-LP-001). */
@Composable
fun AsOfLine(asOf: Long) {
    Text(stringResource(R.string.s3_as_of, Fmt.dateTimeWib(asOf)), color = AmarTheme.colors.content.tertiary, style = AmarTheme.typography.captionSm)
}

/** Loading state of a Performa screen or card. */
@Composable
fun PerfLoading(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.SequenceGap)) {
        AmarSpinner(AmarTheme.colors.icon.brand)
        Text(stringResource(R.string.s3_loading), color = AmarTheme.colors.content.secondary, style = AmarTheme.typography.bodySm)
    }
}

/** Error state: "Gagal memuat data. Coba lagi." with `Coba lagi`. */
@Composable
fun PerfError(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    AmarInfoBox(stringResource(R.string.s3_error), modifier.fillMaxWidth(), type = AmarInfoBoxType.Error, buttonLabel = stringResource(R.string.s3_retry), onButtonClick = onRetry)
}

/**
 * Payment chip (PRD 3.0 round 10): Rekonsiliasi Info (blue), Menunggu pembayaran Warning (orange), Dibayar Success (green),
 * Tidak dibayar Error (red). Tidak ada pembayaran has no chip (the DS Badge has no grey type; shown as grey text).
 */
@Composable
fun PaymentBadge(p: Payment?) {
    p ?: return
    val (label, type) = when (p.status) {
        PayStatus.REKONSILIASI -> R.string.s3_pay_chip_recon to AmarBadgeType.Info
        PayStatus.MENUNGGU -> R.string.s3_pay_chip_waiting to AmarBadgeType.Warning
        PayStatus.DIBAYAR -> R.string.s3_pay_chip_paid to AmarBadgeType.Success
        PayStatus.TIDAK_DIBAYAR -> R.string.s3_pay_chip_unpaid to AmarBadgeType.Error
        PayStatus.TIDAK_ADA -> return
    }
    AmarBadge(stringResource(label), type = type, size = AmarBadgeSize.Small)
}

/** "Koreksi −Rp 150.000: Potongan seragam hilang" (FR-PY-002, shown from reconciliation on). */
@Composable
fun correctionText(p: Payment): String {
    val corr = signedRupiah(p.correction)
    return if (p.note.isBlank()) stringResource(R.string.s3_correction, corr) else stringResource(R.string.s3_correction_note, corr, p.note)
}

/**
 * Payment lines of a month past Berjalan (§ Month and payment status, FR-PY-006): the status line ("Menunggu rekonsiliasi —
 * final {tanggal}", "Menunggu pembayaran (jadwal {tanggal})" also after the date, "Dibayar {tanggal}: Rp {jumlah}",
 * "Tidak dibayar: {keterangan}. Hubungi TL Anda.", "Tidak ada pembayaran"), the Koreksi line, and an earlier keterangan.
 */
@Composable
fun PaymentLines(p: Payment?, showCorrection: Boolean = true) {
    p ?: return
    val c = AmarTheme.colors
    val (text, color) = when (p.status) {
        PayStatus.REKONSILIASI -> stringResource(R.string.s3_note_recon, Fmt.day(p.lockAt)) to c.feedback.info.text
        PayStatus.DIBAYAR -> stringResource(R.string.s3_pay_paid, Fmt.day(requireNotNull(p.paidAt)), rupiah(p.paidAmount ?: 0)) to c.feedback.success.text
        PayStatus.MENUNGGU -> stringResource(R.string.s3_pay_waiting, Fmt.day(p.scheduledAt)) to c.feedback.warning.text
        PayStatus.TIDAK_DIBAYAR -> stringResource(R.string.s3_pay_unpaid, p.unpaidNote) to c.feedback.error.text
        PayStatus.TIDAK_ADA -> stringResource(R.string.s3_pay_none) to c.content.secondary
    }
    Text(text, color = color, style = AmarTheme.typography.bodySm)
    if (showCorrection && p.correction != 0L) Text(correctionText(p), color = c.content.secondary, style = AmarTheme.typography.bodySm)
    if (p.previousUnpaidNote.isNotBlank()) Text(stringResource(R.string.s3_pay_prev_unpaid, p.previousUnpaidNote), color = c.content.tertiary, style = AmarTheme.typography.bodySm)
}

/** "Gaji pokok Rp a + insentif Rp b" (+ " + koreksi ±Rp c" when there is a Koreksi). */
@Composable
fun thpFormula(r: SalesRow): String {
    val corr = r.payment?.correction ?: 0L
    return if (corr != 0L) stringResource(R.string.s3_thp_formula_correction, rupiah(r.calc.basicSalary), rupiah(r.calc.incentive), signedRupiah(corr))
    else stringResource(R.string.s3_thp_formula, rupiah(r.calc.basicSalary), rupiah(r.calc.incentive))
}

/** LMT error (FR-LP-001, PRD §11): the last stored data is shown with this banner. */
@Composable
fun LmtBanner(asOf: Long) {
    AmarInfoBox(stringResource(R.string.s3_lmt_error, Fmt.dateTimeWib(asOf)), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
}

@Composable
fun tierText(t: S3Tier): String = when {
    t.from == null -> stringResource(R.string.s3_tier_first, pctText(t.to ?: 0.0))
    t.to == null -> stringResource(R.string.s3_tier_open, pctText(t.from))
    else -> stringResource(R.string.s3_tier_range, pctText(t.from), pctText(t.to))
}

/** Tier table, highest first, with the reached tier highlighted (web `TierTable`). */
@Composable
fun TierTable(tiers: List<S3Tier>, reached: S3Tier) {
    val c = AmarTheme.colors
    Column {
        Row(Modifier.fillMaxWidth().padding(AmarActivityTokens.TilePadding)) {
            Text(stringResource(R.string.s3_achievement), Modifier.weight(1f), c.content.secondary, style = AmarTheme.typography.captionSm)
            Text(stringResource(R.string.s3_coefficient), color = c.content.secondary, style = AmarTheme.typography.captionSm)
        }
        tiers.sortedByDescending { it.from ?: -1.0 }.forEach { t ->
            val on = t == reached
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(if (on) c.feedback.info.surface else c.background.base, RoundedCornerShape(AmarActivityTokens.TileRadius))
                    .padding(AmarActivityTokens.TilePadding),
            ) {
                val style = if (on) AmarTheme.typography.labelMdSemibold else AmarTheme.typography.bodySm
                Text(tierText(t), Modifier.weight(1f), c.content.primary, style = style)
                Text("${rateText(t.rate)}%", color = c.content.primary, style = style)
            }
        }
    }
}

/** Row with a label, a value and an optional badge after the value. */
@Composable
fun ValueRow(label: String, value: String, badge: (@Composable () -> Unit)? = null) {
    val c = AmarTheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
        Text(label, Modifier.weight(1f), c.content.secondary, style = AmarTheme.typography.bodySm)
        badge?.invoke()
        Text(value, color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
    }
}

/** "×2" Success badge when the multiplier applies. */
@Composable
fun MultiplierBadge(r: SalesRow) {
    if (r.calc.multiplierApplies) AmarBadge(stringResource(R.string.s3_multiplier_badge, multiplierText(r.calc.multiplier)), type = AmarBadgeType.Success, size = AmarBadgeSize.Small)
}

/** Achievement with the progress bar and the band chip. */
@Composable
fun AchievementBar(r: SalesRow) {
    val c = AmarTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
            Text(stringResource(R.string.s3_achievement), Modifier.weight(1f), c.content.secondary, style = AmarTheme.typography.bodySm)
            Text("${pctText(r.calc.achievement)}%", color = c.content.link, style = AmarTheme.typography.captionSm)
            BandBadge(r.calc.band)
        }
        AmarProgressBar((r.calc.achievement / 100).toFloat())
    }
}

/** Gaji pokok block (FR-DF-003): valid days × daily fee (area), maximum days, multiplier line, basic salary. */
@Composable
fun BasicSalaryBlock(r: SalesRow, areaName: String, onAttendance: (() -> Unit)?) {
    val c = AmarTheme.colors
    val m = multiplierText(r.calc.multiplier)
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
        Text(stringResource(R.string.s3_basic), color = c.content.primary, style = AmarTheme.typography.headingSm)
        // States of SCR-3-04: daily fee not set, no valid attendance.
        if (r.calc.dailyFee == 0L) AmarInfoBox(stringResource(R.string.s3_fee_not_set, areaName), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
        if (r.calc.validDays == 0) AmarInfoBox(stringResource(R.string.s3_no_valid_days), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
        Text(stringResource(R.string.s3_income_days, r.calc.validDays, rupiah(r.calc.dailyFee), areaName), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        if (r.calc.capped) Text(stringResource(R.string.s3_income_cap, r.calc.maxDays), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        if (r.calc.multiplierEligible) {
            if (r.calc.multiplierApplies) Text(stringResource(R.string.s3_mult_on, m), color = c.feedback.success.text, style = AmarTheme.typography.bodySm)
            else Text(stringResource(R.string.s3_mult_off, m, pctText(r.calc.threshold)), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        }
        ValueRow(stringResource(R.string.s3_basic), rupiah(r.calc.basicSalary)) { MultiplierBadge(r) }
        onAttendance?.let { AmarButton(stringResource(R.string.s3_see_attendance), it, type = AmarButtonType.Naked, size = AmarButtonSize.Small) }
    }
}

/** Insentif block (FR-IN-001/004): target, Cair dihitung, achievement, coefficient, incentive, next tier, tier table. */
@Composable
fun IncentiveBlock(r: SalesRow) {
    val c = AmarTheme.colors
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
        Text(stringResource(R.string.s3_incentive), color = c.content.primary, style = AmarTheme.typography.headingSm)
        ValueRow(stringResource(R.string.s3_target), rupiah(r.calc.target))
        ValueRow(stringResource(R.string.s3_counted), rupiah(r.calc.actual))
        AchievementBar(r)
        ValueRow(stringResource(R.string.s3_coefficient), "${rateText(r.calc.coefficient)}%")
        ValueRow(stringResource(R.string.s3_incentive), rupiah(r.calc.incentive))
        r.calc.next?.takeIf { r.monthStatus != MonthStatus.FINAL }?.let {
            Text(stringResource(R.string.s3_next_tier, rupiah(it.needed), "${rateText(it.tier.rate)}%"), color = c.content.link, style = AmarTheme.typography.bodySm)
        }
        TierTable(if (r.role == S3Role.TL) Scope3.TL_TIERS else Scope3.SALES_TIERS, r.calc.tier)
    }
}

/**
 * Take home pay block (FR-IN-004, SCR-3-04 round 10): Gaji pokok, Insentif, Koreksi with its note, Take home pay final, the
 * payment status, and the question line of a Final month.
 */
@Composable
fun TakeHomePayBlock(r: SalesRow) {
    val c = AmarTheme.colors
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
        ValueRow(stringResource(R.string.s3_basic), rupiah(r.calc.basicSalary)) { MultiplierBadge(r) }
        ValueRow(stringResource(R.string.s3_incentive), rupiah(r.calc.incentive))
        // "Koreksi {±Rp}: {catatan}" (FR-PY-002); without a note just the amount.
        r.payment?.takeIf { it.correction != 0L }?.let { p ->
            if (p.note.isBlank()) ValueRow(stringResource(R.string.s3_correction_label), signedRupiah(p.correction))
            else Text(correctionText(p), color = c.content.primary, style = AmarTheme.typography.bodySm)
        }
        ValueRow(stringResource(R.string.s3_thp), rupiah(r.takeHomePay)) { PaymentBadge(r.payment) }
        PaymentLines(r.payment, showCorrection = false)
        if (r.monthStatus == MonthStatus.FINAL) Text(stringResource(R.string.s3_final_question), color = c.content.tertiary, style = AmarTheme.typography.bodySm)
    }
}

/** A loan of the pipeline (FR-LP-005): ref no, masked customer, toko, amount, status, dates, "beda bulan" flag. */
@Composable
fun LoanRow(l: PipelineLoan, storeName: String?, onClick: (() -> Unit)?) {
    val c = AmarTheme.colors
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(l.loan.id, Modifier.weight(1f), c.content.secondary, style = AmarTheme.typography.bodySm)
            PortalStatusBadge(l.status)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(l.loan.maskedCustomer, Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.labelMdSemibold)
            Text(rupiah(l.loan.amount), color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
        }
        storeName?.let { Text(it, color = c.content.secondary, style = AmarTheme.typography.bodySm) }
        val dates = listOfNotNull(
            stringResource(R.string.s3_loan_submitted, Fmt.day(SalesRepository.dateOf(l.loan.submittedAt))),
            l.loan.paidOutAt?.let { stringResource(R.string.s3_loan_paid, Fmt.day(SalesRepository.dateOf(it))) },
        ).joinToString(" · ")
        Text(dates, color = c.content.tertiary, style = AmarTheme.typography.bodySm)
        if (l.paidLater) PaidLaterLine(l)
    }
}

@Composable
fun PaidLaterLine(l: PipelineLoan) {
    val paid = l.loan.paidOutAt ?: return
    Text(stringResource(R.string.s3_paid_later, Fmt.day(SalesRepository.dateOf(paid))), color = AmarTheme.colors.feedback.warning.text, style = AmarTheme.typography.bodySm)
}

/** A counted loan, or a struck-through one paid out in a later month (month detail, web `IncomeDrawer`). */
@Composable
fun MonthLoanLine(l: PipelineLoan) {
    val c = AmarTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
        val deco = if (l.counted) TextDecoration.None else TextDecoration.LineThrough
        val color = if (l.counted) c.content.primary else c.content.tertiary
        Text("${l.loan.id} · ${rupiah(l.loan.amount)}", color = color, style = AmarTheme.typography.labelMdSemibold.copy(textDecoration = deco))
        val dates = listOfNotNull(
            stringResource(R.string.s3_loan_submitted, Fmt.dateTimeWib(l.loan.submittedAt)),
            l.loan.paidOutAt?.let { stringResource(R.string.s3_loan_paid, Fmt.dateTimeWib(it)) },
        ).joinToString(" · ")
        Text(dates, color = color, style = AmarTheme.typography.bodySm.copy(textDecoration = deco))
        if (!l.counted) PaidLaterLine(l)
    }
}

/**
 * Screen with an App Bar, sub-tabs and scrolling content; with [onRefresh] the content can be pulled to refresh
 * (Pipeline, FR-LP-001). The DS has no pull-to-refresh component: Material 3 `PullToRefreshBox` with theme colours.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScaffold(
    title: String,
    onBack: (() -> Unit)?,
    tabs: List<Int>,
    selected: Int,
    onTab: (Int) -> Unit,
    refreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = AmarTheme.colors
    Column(Modifier.fillMaxSize().background(c.background.canvas)) {
        AmarAppBar(title = title, type = if (onBack == null) AmarAppBarType.Primary else AmarAppBarType.Secondary, onNavigationClick = onBack)
        if (tabs.isNotEmpty()) AmarTab { tabs.forEachIndexed { i, label -> AmarTabItem(stringResource(label), i == selected, { onTab(i) }, icon = null) } }
        val body: @Composable () -> Unit = {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AmarFormTokens.PaddingHorizontal, vertical = AmarFormTokens.PaddingVertical),
                verticalArrangement = Arrangement.spacedBy(AmarFormTokens.FieldGap),
                content = content,
            )
        }
        Box(Modifier.weight(1f)) {
            if (onRefresh == null) body() else {
                val state = rememberPullToRefreshState()
                PullToRefreshBox(
                    isRefreshing = refreshing,
                    onRefresh = onRefresh,
                    state = state,
                    indicator = {
                        PullToRefreshDefaults.Indicator(state = state, isRefreshing = refreshing, modifier = Modifier.align(Alignment.TopCenter), containerColor = c.background.base, color = c.icon.brand)
                    },
                ) { body() }
            }
        }
    }
}
