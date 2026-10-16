package com.example.and_sales_portal.ui.attendance

import com.example.and_sales_portal.data.activity.ActivityRules
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.Attendance
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.Punch
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadge
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeSize
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeType
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.LayeredIcon
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.common.SectionTitle
import java.time.LocalDate

/** A day in the attendance history. */
data class AttendanceDay(val date: LocalDate, val attendance: Attendance?, val status: AttendanceStatus)

/** What ATT-01 shows: today, the history (newest first) and whether the device is online. */
data class AttendanceUiState(
    val today: AttendanceDay,
    val history: List<AttendanceDay>,
    val online: Boolean,
)

/**
 * Absensi card, Figma "01 | Team Leader Dashboard | Belum Clock In / Clocked In" › `Attendance hero`: Card Shadow with
 * the clock icon, "Absensi" and the status badge, the Clock In tile (`Background/Brand Subtle`) and Clock Out tile
 * (`Background/Canvas`), then Clock In (Primary) or Clock Out (Secondary). Used on Absensi and Beranda.
 *
 * @param onHistory Shows "Lihat riwayat" (Beranda); `null` hides it (Absensi screen).
 */
@Composable
fun AttendanceCard(
    day: AttendanceDay,
    online: Boolean,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit,
    modifier: Modifier = Modifier,
    onHistory: (() -> Unit)? = null,
) {
    val c = AmarTheme.colors
    val a = day.attendance
    AmarCard(modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
        Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.SequenceGap)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.SequenceGap), verticalAlignment = Alignment.CenterVertically) {
                    LayeredIcon(R.drawable.ds_ic_clock_24_l0, R.drawable.ds_ic_clock_24_l1)
                    Text(stringResource(R.string.att_title), color = c.content.primary, style = AmarTheme.typography.headingSm)
                }
                AttendanceBadge(day.status)
            }
            PresenceBadge(a)
            Row(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
                TimeTile(stringResource(R.string.att_clock_in), a?.clockIn, a?.employeeId, c.background.brandSubtle)
                TimeTile(stringResource(R.string.att_clock_out), a?.clockOut, a?.employeeId, c.background.canvas)
            }
        }
        when {
            // Sunday is not a workday (Monday–Saturday, PRD Scope 2 S10): no check in kerja.
            a?.clockIn == null -> AmarButton(stringResource(R.string.att_clock_in), onClockIn, Modifier.fillMaxWidth(), size = AmarButtonSize.Medium, enabled = online && day.status != AttendanceStatus.NOT_WORKDAY)
            a.clockOut == null -> AmarButton(stringResource(R.string.att_clock_out), onClockOut, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium, enabled = online)
            else -> Text(stringResource(R.string.att_done), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        }
        if (onHistory != null) {
            AmarButton(stringResource(R.string.att_see_history), onHistory, Modifier.fillMaxWidth(), type = AmarButtonType.Naked, size = AmarButtonSize.Small)
        }
    }
}

@Composable
private fun RowScope.TimeTile(label: String, punch: Punch?, employeeId: Long?, background: Color) {
    val c = AmarTheme.colors
    Column(
        Modifier
            .weight(1f)
            .background(background, RoundedCornerShape(AmarActivityTokens.TileRadius))
            .padding(AmarActivityTokens.TilePadding),
        verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap),
    ) {
        Text(label, color = c.content.secondary, style = AmarTheme.typography.captionSm)
        Text(
            punch?.let { stringResource(R.string.att_time, Fmt.timeLabel(it, employeeId ?: 0)) } ?: stringResource(R.string.att_time_empty, ActivityRules.ZONE_LABEL),
            color = c.content.primary,
            style = AmarTheme.typography.headingMd,
        )
    }
}

/**
 * Figma `Attendance status`: Badge Small; labels from the revision 2026-10-08: "Belum Check In" Warning, "Tepat Waktu"
 * Success, "Terlambat" Warning, "Absen" Error (also check in without check out, S13), "Bukan hari kerja" Info (Sunday).
 */
@Composable
fun AttendanceBadge(status: AttendanceStatus) {
    val (label, type) = when (status) {
        AttendanceStatus.NOT_YET -> R.string.att_status_not_yet to AmarBadgeType.Warning
        AttendanceStatus.ON_TIME -> R.string.att_status_on_time to AmarBadgeType.Success
        AttendanceStatus.LATE -> R.string.att_status_late to AmarBadgeType.Warning
        AttendanceStatus.ABSENT -> R.string.att_status_absent to AmarBadgeType.Error
        AttendanceStatus.NOT_WORKDAY -> R.string.att_status_not_workday to AmarBadgeType.Info
    }
    AmarBadge(stringResource(label), type = type, size = AmarBadgeSize.Small)
}

/** "Sudah Check In" / "Sudah Check Out" (revision 2026-10-08 report labels); nothing before check in. */
@Composable
fun PresenceBadge(a: Attendance?) {
    val label = when {
        a?.clockOut != null -> R.string.att_status_checked_out
        a?.clockIn != null -> R.string.att_status_checked_in
        else -> return
    }
    AmarBadge(stringResource(label), type = AmarBadgeType.Info, size = AmarBadgeSize.Small)
}

/** Spec: ATT-01 Absensi (PRD v3 D2: clock in/out and history with dates, times and status). */
@Composable
fun AttendanceScreen(state: AttendanceUiState, onBack: () -> Unit, onClockIn: () -> Unit, onClockOut: () -> Unit) {
    ScreenScaffold(stringResource(R.string.att_title), onBack) {
        if (!state.online) AmarInfoBox(stringResource(R.string.att_offline), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
        Text(Fmt.longDate(state.today.date), color = AmarTheme.colors.content.secondary, style = AmarTheme.typography.bodySm)
        AttendanceCard(state.today, state.online, onClockIn, onClockOut)
        SectionTitle(stringResource(R.string.att_history))
        state.history.forEach { HistoryRow(it) }
    }
}

@Composable
private fun HistoryRow(day: AttendanceDay) {
    val c = AmarTheme.colors
    val a = day.attendance
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
                Text(Fmt.shortDate(day.date), color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                Text(
                    stringResource(R.string.att_history_row, a?.clockIn?.let { Fmt.timeLabel(it, a.employeeId) } ?: "--:--", a?.clockOut?.let { Fmt.timeLabel(it, a.employeeId) } ?: "--:--"),
                    color = c.content.secondary,
                    style = AmarTheme.typography.bodySm,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
                AttendanceBadge(day.status)
                PresenceBadge(a)
            }
        }
    }
}
