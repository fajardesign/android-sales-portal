package com.example.and_sales_portal.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.PartnerStatus
import com.example.and_sales_portal.ui.performance.PerformanceCard
import com.example.and_sales_portal.ui.performance.PerformanceCardState
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
import com.example.and_sales_portal.ui.account.roleLabel
import com.example.and_sales_portal.ui.attendance.AttendanceCard
import com.example.and_sales_portal.ui.attendance.AttendanceDay
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.partner.PartnerStatusBadge
import com.example.and_sales_portal.ui.visits.VisitWeekBadge
import com.example.and_sales_portal.data.activity.VisitWeek
import com.example.and_sales_portal.data.activity.WeekPartner
import com.example.and_sales_portal.data.partner.AppNotification
import com.example.and_sales_portal.ui.notifications.notificationText
import com.example.and_sales_portal.ui.visits.VisitedBadge
import com.example.and_sales_portal.designsystem.components.layout.AmarDivider
import com.example.and_sales_portal.ui.common.InfoRow
import com.example.and_sales_portal.ui.common.ListRow
import com.example.and_sales_portal.ui.common.ProgressSummary
import com.example.and_sales_portal.ui.common.QuickAction
import com.example.and_sales_portal.ui.common.QuickActions
import java.time.LocalDate

class AttendanceActions(val onClockIn: () -> Unit, val onClockOut: () -> Unit, val onHistory: () -> Unit)

/** Figma `Priority heading`: role badge and today's date under the greeting app bar. */
@Composable
private fun PriorityHeading(role: SalesRole, date: LocalDate) {
    Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
        AmarBadge(stringResource(roleLabel(role)), type = AmarBadgeType.Info, size = AmarBadgeSize.Small)
        Text(Fmt.longDate(date), color = AmarTheme.colors.content.secondary, style = AmarTheme.typography.bodySm)
    }
}

data class SalesHomeState(
    val firstName: String,
    val role: SalesRole,
    val date: LocalDate,
    val attendance: AttendanceDay,
    val online: Boolean,
    /** This week's visits against the target (revision 2026-10-08). */
    val week: VisitWeek,
    val visitedToday: Boolean,
    /** This week's toko by partner with "Sudah dikunjungi minggu ini" / "Belum dikunjungi" (PRD alignment 2026-10-09). */
    val groups: List<WeekPartner>,
    val partners: List<Partner>,
    /** PRF-01 "Performa bulan ini" (PRD Scope 3 SCR-3-01). */
    val performance: PerformanceCardState,
    /** Unread in-app notifications (FR-MP-004), newest first. */
    val unread: List<AppNotification> = emptyList(),
)

/**
 * Spec: SLS-01 Beranda SA/SR (PRD v3 D1; revision 2026-10-08; UI refresh 2026-10-17): Absensi card first (the day's first
 * task), this week's visits as progress ("n dari m toko dikunjungi") with the toko as rows, "Performa bulan ini" (PRD
 * Scope 3 SCR-3-01, replacing this month's sales and the old incentive estimate), and the partners as rows.
 */
@Composable
fun SalesHomeScreen(
    s: SalesHomeState,
    attendance: AttendanceActions,
    onVisits: () -> Unit,
    onPartners: () -> Unit,
    onPerformance: () -> Unit,
    onNotifications: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    val c = AmarTheme.colors
    ScreenScaffold(stringResource(R.string.home_greeting, s.firstName), onBack = null, onNotifications = onNotifications) {
        PriorityHeading(s.role, s.date)
        s.unread.firstOrNull()?.let { n ->
            AmarInfoBox(
                stringResource(R.string.notif_new, s.unread.size, notificationText(n)), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Info,
                buttonLabel = stringResource(R.string.notif_see), onButtonClick = onNotifications,
            )
        }
        AttendanceCard(s.attendance, s.online, attendance.onClockIn, attendance.onClockOut, onHistory = attendance.onHistory)
        WeekVisitsCard(s.week, s.visitedToday, s.groups, onVisits)
        PerformanceCard(s.performance, onPerformance, onRetry)
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            SectionHeader(stringResource(R.string.home_partners), stringResource(R.string.home_see_all), onPartners)
            if (s.partners.isEmpty()) Text(stringResource(R.string.ptn_mine_empty), color = c.content.secondary, style = AmarTheme.typography.bodySm)
            Column {
                s.partners.forEachIndexed { i, p ->
                    if (i > 0) AmarDivider()
                    ListRow(p.name, stringResource(R.string.home_store_count, p.stores.size), onClick = onPartners)
                }
            }
        }
    }
}

data class TlHomeState(
    val firstName: String,
    val date: LocalDate,
    val attendance: AttendanceDay,
    val online: Boolean,
    /** Own visits this week (TL toko); `null` when the TL has no toko. */
    val myWeek: VisitWeek?,
    val teamPresent: Int,
    val teamNotYet: Int,
    val teamAbsent: Int,
    /** Team members with a visit today, without, and with a complete week (revision 2026-10-08). */
    val teamVisitedToday: Int,
    val teamNotVisitedToday: Int,
    val teamCompleteWeek: Int,
    val teamSize: Int,
    val submissions: Map<PartnerStatus, Int>,
    /** PRF-01 "Performa bulan ini" with "Total take home pay tim" (PRD Scope 3 SCR-3-01). */
    val performance: PerformanceCardState,
    val contribution: List<Pair<String, Double>>,
    /** Unread notifications (DD review point 2: revision requests), newest first. */
    val unread: List<AppNotification> = emptyList(),
)

class TlHomeActions(
    val onMyVisits: () -> Unit,
    val onTeam: () -> Unit,
    val onSubmissions: () -> Unit,
    val onRegister: () -> Unit,
    val onSchedule: () -> Unit,
    val onAssign: () -> Unit,
    val onPerformance: () -> Unit,
    val onNotifications: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

/** This week's visits: progress over the target, today, and the toko as rows (at most [MAX_STORES], then "+n toko lainnya"). */
@Composable
private fun WeekVisitsCard(week: VisitWeek, visitedToday: Boolean, groups: List<WeekPartner>, onVisits: () -> Unit) {
    val c = AmarTheme.colors
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
        SectionHeader(stringResource(R.string.home_week_visits), stringResource(R.string.home_see_all), onVisits)
        if (week.target == 0) {
            Text(stringResource(R.string.home_week_no_store), color = c.content.secondary, style = AmarTheme.typography.bodySm)
            return@AmarCard
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ProgressSummary(stringResource(R.string.home_week_progress, week.visited, week.target), week.visited, week.target, Modifier.weight(1f))
        }
        Text(stringResource(if (visitedToday) R.string.vis_today_done else R.string.vis_today_not_yet), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        val stores = groups.flatMap { g -> g.stores.map { g.partner to it } }
        Column {
            stores.take(MAX_STORES).forEachIndexed { i, (p, ws) ->
                if (i > 0) AmarDivider()
                val subtitle = if (!ws.counted) stringResource(R.string.vis_row_next_week, p.name) else p.name
                ListRow(ws.store.name, subtitle, onClick = onVisits) { VisitedBadge(ws.visited, partner = false) }
            }
        }
        if (stores.size > MAX_STORES) {
            AmarButton(stringResource(R.string.home_more_stores, stores.size - MAX_STORES), onVisits, Modifier.fillMaxWidth(), type = AmarButtonType.Naked, size = AmarButtonSize.Small)
        }
    }
}

private const val MAX_STORES = 3

/**
 * Spec: TL-06 Beranda TL (PRD v3 C1; Figma "01 | Team Leader Dashboard"; UI refresh 2026-10-17): own Absensi card first,
 * Pintasan as a row of tiles, the team today (attendance and visits), own visits this week as progress, Pengajuan Partner
 * by status with Revision Required highlighted, "Performa bulan ini" with the team total (PRD Scope 3 SCR-3-01) and team
 * contribution (share of the TL's Cair dihitung).
 * Figma's Lead and RTS metrics are out of scope (PRD) and replaced by today's activity.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TlHomeScreen(s: TlHomeState, attendance: AttendanceActions, a: TlHomeActions) {
    val c = AmarTheme.colors
    ScreenScaffold(stringResource(R.string.home_greeting, s.firstName), onBack = null, onNotifications = a.onNotifications) {
        PriorityHeading(SalesRole.TL, s.date)
        s.unread.firstOrNull()?.let { n ->
            AmarInfoBox(
                stringResource(R.string.notif_new, s.unread.size, notificationText(n)), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Info,
                buttonLabel = stringResource(R.string.notif_see), onButtonClick = a.onNotifications,
            )
        }
        AttendanceCard(s.attendance, s.online, attendance.onClockIn, attendance.onClockOut, onHistory = attendance.onHistory)
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            SectionHeader(stringResource(R.string.home_shortcuts))
            QuickActions(
                listOf(
                    QuickAction(R.drawable.ds_ic_plus_24, stringResource(R.string.ptn_register), a.onRegister),
                    QuickAction(R.drawable.ds_ic_contact_24, stringResource(R.string.act_assign), a.onAssign),
                    QuickAction(R.drawable.ds_ic_calendar_24, stringResource(R.string.home_schedule), a.onSchedule),
                    QuickAction(R.drawable.ds_ic_location_24, stringResource(R.string.vis_title_own), a.onMyVisits),
                ),
            )
        }
        MetricTilesCard(
            stringResource(R.string.home_team_today),
            listOf(
                s.teamPresent.toString() to stringResource(R.string.home_present),
                s.teamNotYet.toString() to stringResource(R.string.home_not_yet),
                s.teamAbsent.toString() to stringResource(R.string.home_absent),
            ),
        ) {
            InfoRow(stringResource(R.string.home_team_visited_today), stringResource(R.string.home_members_value, s.teamVisitedToday, s.teamSize))
            InfoRow(stringResource(R.string.home_team_week_complete), stringResource(R.string.home_members_value, s.teamCompleteWeek, s.teamSize))
            AmarButton(stringResource(R.string.home_see_team), a.onTeam, Modifier.fillMaxWidth(), type = AmarButtonType.Naked, size = AmarButtonSize.Small)
        }
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            SectionHeader(stringResource(R.string.home_my_week), stringResource(R.string.home_see_all), a.onMyVisits)
            s.myWeek?.takeIf { it.target > 0 }?.let { ProgressSummary(stringResource(R.string.home_week_progress, it.visited, it.target), it.visited, it.target) }
                ?: Text(stringResource(R.string.home_week_no_store), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        }
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            SectionHeader(stringResource(R.string.home_submissions), stringResource(R.string.home_see_all), a.onSubmissions)
            val revision = s.submissions[PartnerStatus.REVISION_REQUIRED] ?: 0
            if (revision > 0) AmarInfoBox(stringResource(R.string.home_submissions_revision, revision), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
                PartnerStatus.ordered.forEach { st ->
                    val n = s.submissions[st] ?: 0
                    if (n > 0) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
                        Text(n.toString(), color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                        PartnerStatusBadge(st)
                    }
                }
            }
        }
        PerformanceCard(s.performance, a.onPerformance, a.onRetry)
        if (s.contribution.isNotEmpty()) ContributionCard(s.contribution)
    }
}
