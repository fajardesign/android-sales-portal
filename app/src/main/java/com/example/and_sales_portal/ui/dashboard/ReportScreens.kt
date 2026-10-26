package com.example.and_sales_portal.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.team.Productivity
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.actions.AmarChip
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
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
import com.example.and_sales_portal.ui.attendance.AttendanceBadge
import com.example.and_sales_portal.ui.attendance.PresenceBadge
import com.example.and_sales_portal.ui.attendance.distanceLabel
import com.example.and_sales_portal.data.activity.Attendance
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.InfoRow
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.partner.rupiah
import com.example.and_sales_portal.ui.visits.VisitItem
import com.example.and_sales_portal.ui.visits.VisitWeekBadge
import com.example.and_sales_portal.ui.visits.VisitStatusBadge
import com.example.and_sales_portal.ui.attendance.CorrectionHistory
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadge
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeSize
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeType
import java.time.LocalDate

/** Productivity lines of a person: attendance, and visits per week against the target (revision 2026-10-08). */
@Composable
fun ProductivityCard(title: String, p: Productivity, modifier: Modifier = Modifier, team: Boolean = false, onClick: (() -> Unit)? = null) {
    val c = AmarTheme.colors
    AmarCard(modifier.fillMaxWidth(), style = AmarCardStyle.Border, onClick = onClick) {
        Text(title, color = c.content.primary, style = AmarTheme.typography.headingSm)
        Text(stringResource(R.string.prod_attendance, p.present, p.workdays, p.onTime, p.late), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        // TL-07 (same as the APL report): Absen, "Tidak check in" (OQ-S2-03) and the average check in time.
        if (team) Text(stringResource(R.string.prod_absent_avg, p.absent, p.noCheckIn, p.avgClockIn ?: "-"), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        // Like the web portal: the current week, then the other weeks whose Monday is in the period.
        (listOf(p.thisWeek) + p.weeks.filter { it.start != p.thisWeek.start }.sortedByDescending { it.start }).forEach { w ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (w.start == p.thisWeek.start) stringResource(R.string.vis_week) else stringResource(R.string.prod_week_label, Fmt.shortDate(w.start)),
                    Modifier.weight(1f), c.content.secondary, style = AmarTheme.typography.bodySm,
                )
                VisitWeekBadge(w)
            }
        }
    }
}

/** Spec: TL-07 Produktivitas Tim (PRD v3 C9): attendance and visit results per SA/SR, daily, weekly, monthly. */
@Composable
fun TeamProductivityScreen(period: Period, onPeriod: (Period) -> Unit, rows: List<Pair<DemoDirectory.Person, Productivity>>, onBack: () -> Unit, onOpen: (DemoDirectory.Person) -> Unit) {
    ScreenScaffold(stringResource(R.string.prod_title), onBack) {
        PeriodChips(listOf(Period.TODAY, Period.WEEK, Period.MONTH), period, onPeriod)
        if (rows.isEmpty()) AmarInfoBox(stringResource(R.string.team_empty), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
        rows.forEach { (p, pr) -> ProductivityCard(p.fullName, pr, team = true) { onOpen(p) } }
    }
}

/** A day of attendance evidence. */
/** A day of TL-07 evidence. [canCorrect]: the TL can still correct it (until the end of the following week). */
data class EvidenceDay(
    val date: LocalDate,
    val status: AttendanceStatus,
    val attendance: Attendance?,
    val visits: List<VisitItem>,
    val noCheckOut: Boolean = false,
    val canCorrect: Boolean = false,
)

/**
 * Spec: TL-07 Bukti absensi & kunjungan of one SA/SR (PRD C9 "with selfie and map evidence"); decisions 2026-10-19:
 * "Tidak check out", "Dikoreksi TL" with the history, and `Koreksi Absensi` → TL-11 (disabled after the deadline).
 */
@Composable
fun EvidenceScreen(
    name: String,
    period: Period?,
    onPeriod: (Period) -> Unit,
    days: List<EvidenceDay>,
    onBack: () -> Unit,
    onMap: (Double, Double) -> Unit,
    onCorrect: ((LocalDate) -> Unit)? = null,
    /** "Riwayat absensi Oktober 2026" when opened from Penghasilan for one month (PRF-04); no period chips then. */
    subtitle: String? = null,
) {
    val c = AmarTheme.colors
    ScreenScaffold(name, onBack) {
        Text(subtitle ?: stringResource(R.string.prod_evidence), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        period?.let { PeriodChips(listOf(Period.TODAY, Period.WEEK, Period.LAST_WEEK, Period.MONTH), it, onPeriod) }
        if (days.isEmpty()) AmarInfoBox(stringResource(R.string.perf_empty), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
        days.forEach { d ->
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(Fmt.shortDate(d.date), Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                    AttendanceBadge(d.status)
                }
                val inPunch = d.attendance?.clockIn
                if (d.noCheckOut) AmarBadge(stringResource(R.string.att_no_check_out), type = AmarBadgeType.Warning, size = AmarBadgeSize.Small) else PresenceBadge(d.attendance)
                if (d.attendance?.corrected == true) AmarBadge(stringResource(R.string.att_corrected), type = AmarBadgeType.Info, size = AmarBadgeSize.Small)
                Text(d.attendance?.let { Fmt.checkInLabel(it) }?.let { stringResource(R.string.prod_clock_in, it) } ?: stringResource(R.string.prod_no_clock_in), color = c.content.secondary, style = AmarTheme.typography.bodySm)
                if (inPunch != null) {
                    inPunch.place?.let { place -> Text(inPunch.distance?.let { stringResource(R.string.prod_place, place, distanceLabel(it)) } ?: place, color = c.content.secondary, style = AmarTheme.typography.bodySm) }
                    Text(if (inPunch.selfie != null) stringResource(R.string.prod_selfie) else stringResource(R.string.prod_selfie_missing), color = c.content.tertiary, style = AmarTheme.typography.bodySm)
                    AmarButton(stringResource(R.string.prod_map), { onMap(inPunch.lat, inPunch.lng) }, type = AmarButtonType.Naked, size = AmarButtonSize.Small)
                }
                d.visits.forEach { v ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
                        Text(v.visit.checkIn?.let { Fmt.time(it, v.visit.employeeId) } ?: "-", color = c.content.primary, style = AmarTheme.typography.bodySm)
                        Text(v.store.name, Modifier.weight(1f), c.content.secondary, style = AmarTheme.typography.bodySm, maxLines = 1)
                        VisitStatusBadge(v.status)
                    }
                }
                d.attendance?.corrections?.takeIf { it.isNotEmpty() }?.let { CorrectionHistory(it, d.attendance.employeeId) }
                onCorrect?.let { open ->
                    AmarButton(stringResource(R.string.cor_open), { open(d.date) }, type = AmarButtonType.Secondary, size = AmarButtonSize.Small, enabled = d.canCorrect)
                    if (!d.canCorrect) Text(stringResource(R.string.cor_closed_short), color = c.content.tertiary, style = AmarTheme.typography.captionSm)
                }
            }
        }
    }
}
