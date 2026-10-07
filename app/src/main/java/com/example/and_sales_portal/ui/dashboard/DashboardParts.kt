package com.example.and_sales_portal.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.partner.Store
import com.example.and_sales_portal.data.sales.ComponentKind
import com.example.and_sales_portal.data.sales.Incentive
import com.example.and_sales_portal.data.sales.IncentiveComponent
import com.example.and_sales_portal.data.sales.Loan
import com.example.and_sales_portal.data.sales.LoanStatus
import com.example.and_sales_portal.data.sales.SalesSummary
import com.example.and_sales_portal.data.sales.Tier
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.actions.AmarChip
import com.example.and_sales_portal.designsystem.components.extended.AmarProgressBar
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadge
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeSize
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeType
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.InfoRow
import com.example.and_sales_portal.ui.common.SectionTitle
import com.example.and_sales_portal.ui.partner.rupiah
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.util.Locale

/** Report periods (PRD C9 "daily, weekly, monthly"; D6 "per period"). */
enum class Period(val label: Int) {
    TODAY(R.string.perf_period_today),
    WEEK(R.string.perf_period_week),
    MONTH(R.string.perf_period_month),
    LAST_MONTH(R.string.perf_period_last_month),
    ;

    fun range(today: LocalDate): ClosedRange<LocalDate> = when (this) {
        TODAY -> today..today
        WEEK -> today.with(DayOfWeek.MONDAY)..today
        MONTH -> today.withDayOfMonth(1)..today
        LAST_MONTH -> today.minusMonths(1).withDayOfMonth(1)..today.withDayOfMonth(1).minusDays(1)
    }
}

@Composable
fun PeriodChips(options: List<Period>, selected: Period, onSelect: (Period) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
        options.forEach { AmarChip(stringResource(it.label), it == selected, { onSelect(it) }) }
    }
}

/** Figma TL Dashboard › `Daily activity metrics`: Card Shadow, title, equal tiles (value Heading/MD, label Caption/SM). */
@Composable
fun MetricTilesCard(title: String, tiles: List<Pair<String, String>>, modifier: Modifier = Modifier, footer: (@Composable () -> Unit)? = null) {
    val c = AmarTheme.colors
    AmarCard(modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
        Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.SequenceGap)) {
            Text(title, color = c.content.primary, style = AmarTheme.typography.headingSm)
            Row(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
                tiles.forEach { (value, label) -> MetricTile(value, label) }
            }
            footer?.invoke()
        }
    }
}

@Composable
private fun RowScope.MetricTile(value: String, label: String) {
    val c = AmarTheme.colors
    Column(
        Modifier
            .weight(1f)
            .background(c.background.canvas, RoundedCornerShape(AmarActivityTokens.TileRadius))
            .padding(horizontal = AmarActivityTokens.MetricPaddingX, vertical = AmarActivityTokens.MetricPaddingY),
        verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = c.content.primary, style = AmarTheme.typography.headingMd, maxLines = 1)
        Text(label, Modifier.fillMaxWidth(), c.content.secondary, style = AmarTheme.typography.captionSm, textAlign = TextAlign.Center)
    }
}

/** # submitted, # accepted, # paid out, paid-out amount. */
@Composable
fun SalesSummaryCard(title: String, s: SalesSummary, modifier: Modifier = Modifier) {
    MetricTilesCard(
        title,
        listOf(s.submitted.toString() to stringResource(R.string.sales_submitted), s.accepted.toString() to stringResource(R.string.sales_accepted), s.paidOut.toString() to stringResource(R.string.sales_paid_out)),
        modifier,
    ) {
        InfoRow(stringResource(R.string.sales_paid_out_amount), rupiah(s.paidOutAmount))
        InfoRow(stringResource(R.string.sales_paid_out_units), s.paidOutUnits.toString())
    }
}

/** "87", "0,85", "12,5" (id-ID, up to 2 decimals). */
fun pctText(v: Double): String = java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 2 }.format(v)

/**
 * Figma TL Dashboard › `Incentive hero`: target icon, "Estimasi Insentif" and "Lihat Detail", the amount (Heading/XL),
 * paid-out achievement with a progress bar, and two tiles. The Figma tiles "Earned / Pending" are shown as the incentive
 * parts (Daily Fee, and the paid-out or leader incentive) with DS feedback colours (see components.md).
 */
@Composable
fun IncentiveHero(incentive: Incentive, onDetail: (() -> Unit)?, modifier: Modifier = Modifier) {
    val c = AmarTheme.colors
    AmarCard(modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
        Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.HeroGap)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.SequenceGap), verticalAlignment = Alignment.CenterVertically) {
                    TargetIcon()
                    Text(stringResource(R.string.inc_title), color = c.content.primary, style = AmarTheme.typography.headingSm)
                }
                onDetail?.let { AmarButton(stringResource(R.string.inc_see_detail), it, type = AmarButtonType.Naked, size = AmarButtonSize.Small) }
            }
            Text(rupiah(incentive.total), color = c.content.primary, style = AmarTheme.typography.headingXl)
            Row(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.inc_achievement), Modifier.weight(1f), c.content.secondary, style = AmarTheme.typography.bodySm)
                Text(stringResource(R.string.inc_achievement_value, pctText(incentive.achievementPct)), color = c.content.link, style = AmarTheme.typography.captionSm)
            }
            AmarProgressBar((incentive.achievementPct / 100).toFloat())
            Row(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
                incentive.components.take(2).forEachIndexed { i, comp ->
                    val feedback = if (i == 0) c.feedback.success else c.feedback.warning
                    BreakdownTile(stringResource(componentLabel(comp.kind)), rupiah(comp.amount), feedback.surface, feedback.text)
                }
            }
        }
    }
}

@Composable
private fun RowScope.BreakdownTile(label: String, value: String, background: Color, valueColor: Color) {
    Column(
        Modifier
            .weight(1f)
            .background(background, RoundedCornerShape(AmarActivityTokens.TileRadius))
            .padding(AmarActivityTokens.TilePadding),
        verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap),
    ) {
        Text(label, color = AmarTheme.colors.content.secondary, style = AmarTheme.typography.captionSm, maxLines = 1)
        Text(value, color = valueColor, style = AmarTheme.typography.headingSm, maxLines = 1)
    }
}

/** `target` icon: three layers (primary, inverse, primary), as in the bottom navigation. */
@Composable
private fun TargetIcon() {
    val c = AmarTheme.colors
    val size = Modifier.size(AmarTheme.iconSizes.lg)
    Box {
        Icon(painterResource(R.drawable.ds_ic_target_24_l0), null, size, tint = c.icon.primary)
        Icon(painterResource(R.drawable.ds_ic_target_24_l1), null, size, tint = c.icon.inverse)
        Icon(painterResource(R.drawable.ds_ic_target_24_l2), null, size, tint = c.icon.primary)
    }
}

fun componentLabel(k: ComponentKind): Int = when (k) {
    ComponentKind.DAILY_FEE -> R.string.inc_daily_fee
    ComponentKind.PAID_OUT -> R.string.inc_paid_out
    ComponentKind.LEADER -> R.string.inc_leader
    ComponentKind.VOLUME -> R.string.inc_volume
    ComponentKind.COLLECTION -> R.string.inc_collection
    ComponentKind.COMMISSION -> R.string.inc_commission
}

@Composable
private fun componentDetail(comp: IncentiveComponent): String = when (comp.kind) {
    ComponentKind.DAILY_FEE -> stringResource(R.string.inc_daily_fee_detail, comp.days, rupiah(comp.unitAmount))
    ComponentKind.COLLECTION -> stringResource(R.string.inc_mfp_detail, pctText(comp.mfpPct ?: 0.0), pctText(comp.ratePct))
    else -> stringResource(R.string.inc_rate_detail, pctText(comp.ratePct), rupiah(comp.base))
}

@Composable
fun tierText(t: Tier): String = when {
    t.to == null -> stringResource(R.string.inc_tier_open, pctText(t.from))
    t.from == 0.0 -> stringResource(R.string.inc_tier_first, pctText(t.to))
    else -> stringResource(R.string.inc_tier_range, pctText(t.from), pctText(t.to))
}

/** Incentive breakdown (PRD D6: daily fee × days present, paid-out achievement %, tier and estimated incentive). */
@Composable
fun IncentiveDetailCard(title: String, incentive: Incentive, modifier: Modifier = Modifier) {
    val c = AmarTheme.colors
    AmarCard(modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.headingSm)
            Text(rupiah(incentive.total), color = c.content.primary, style = AmarTheme.typography.headingSm)
        }
        incentive.components.forEach { comp ->
            Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
                InfoRow(stringResource(componentLabel(comp.kind)), rupiah(comp.amount))
                Text(componentDetail(comp), color = c.content.tertiary, style = AmarTheme.typography.bodySm)
            }
        }
        InfoRow(stringResource(R.string.inc_target), rupiah(incentive.target))
        InfoRow(stringResource(R.string.inc_paid), rupiah(incentive.paidOutAmount))
        InfoRow(stringResource(R.string.inc_achievement), stringResource(R.string.inc_achievement_value, pctText(incentive.achievementPct)))
        incentive.tier?.let { InfoRow(stringResource(R.string.inc_tier), tierText(it)) }
    }
}

/** Figma TL Dashboard › `Team contribution`: name, % and a progress bar per person. */
@Composable
fun ContributionCard(rows: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val c = AmarTheme.colors
    AmarCard(modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.home_contribution), Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.headingSm)
            Text(stringResource(R.string.home_contribution_hint), color = c.content.secondary, style = AmarTheme.typography.captionSm)
        }
        rows.forEach { (name, pct) ->
            Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.SequenceGap)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.SequenceGap)) {
                    Icon(painterResource(R.drawable.ds_ic_menu_profile_24), null, Modifier.size(AmarTheme.iconSizes.lg), tint = c.icon.secondary)
                    Text(name, Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.bodySm)
                    Text(stringResource(R.string.inc_achievement_value, pctText(pct)), color = c.content.link, style = AmarTheme.typography.captionSm)
                }
                AmarProgressBar((pct / 100).toFloat(), color = c.icon.brand)
            }
        }
    }
}

@Composable
fun LoanStatusBadge(s: LoanStatus) {
    val (label, type) = when (s) {
        LoanStatus.SUBMITTED -> R.string.loan_status_submitted to AmarBadgeType.Info
        LoanStatus.IN_PROCESS -> R.string.loan_status_in_process to AmarBadgeType.Info
        LoanStatus.APPROVED -> R.string.loan_status_approved to AmarBadgeType.Success
        LoanStatus.REJECTED -> R.string.loan_status_rejected to AmarBadgeType.Error
        LoanStatus.PAID_OUT -> R.string.loan_status_paid_out to AmarBadgeType.Success
    }
    AmarBadge(stringResource(label), type = type, size = AmarBadgeSize.Small)
}

fun loanStatusLabel(s: LoanStatus): Int = when (s) {
    LoanStatus.SUBMITTED -> R.string.loan_status_submitted
    LoanStatus.IN_PROCESS -> R.string.loan_status_in_process
    LoanStatus.APPROVED -> R.string.loan_status_approved
    LoanStatus.REJECTED -> R.string.loan_status_rejected
    LoanStatus.PAID_OUT -> R.string.loan_status_paid_out
}

/** PRD D5: masked application ID and customer, toko, amount, CRM status, rejection reason, last updated. */
@Composable
fun LoanCard(loan: Loan, store: Store?) {
    val c = AmarTheme.colors
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(loan.maskedId, Modifier.weight(1f), c.content.secondary, style = AmarTheme.typography.bodySm)
            LoanStatusBadge(loan.status)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(loan.maskedCustomer, Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.labelMdSemibold)
            Text(rupiah(loan.amount), color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
        }
        store?.let { Text(it.name, color = c.content.secondary, style = AmarTheme.typography.bodySm) }
        loan.rejectionReason?.let { Text(it, color = c.feedback.error.text, style = AmarTheme.typography.bodySm) }
        val updated = Instant.ofEpochMilli(loan.updatedAt).atZone(ActivityRules.ZONE).toLocalDate()
        Text(stringResource(R.string.loan_updated, "${Fmt.shortDate(updated)}, ${Fmt.time(loan.updatedAt)}"), color = c.content.tertiary, style = AmarTheme.typography.bodySm)
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { SectionTitle(title) }
        if (action != null && onAction != null) AmarButton(action, onAction, type = AmarButtonType.Naked, size = AmarButtonSize.Small)
    }
}
