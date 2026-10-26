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
    /** Last Monday–Sunday: the TL can still correct it (decision 2026-10-19). */
    LAST_WEEK(R.string.perf_period_last_week),
    MONTH(R.string.perf_period_month),
    LAST_MONTH(R.string.perf_period_last_month),
    ;

    fun range(today: LocalDate): ClosedRange<LocalDate> = when (this) {
        TODAY -> today..today
        WEEK -> today.with(DayOfWeek.MONDAY)..today
        LAST_WEEK -> today.with(DayOfWeek.MONDAY).minusWeeks(1)..today.with(DayOfWeek.MONDAY).minusDays(1)
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

/** "87", "0,85", "12,5" (id-ID, up to 2 decimals). */
fun pctText(v: Double): String = java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 2 }.format(v)

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
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { SectionTitle(title) }
        if (action != null && onAction != null) AmarButton(action, onAction, type = AmarButtonType.Naked, size = AmarButtonSize.Small)
    }
}
