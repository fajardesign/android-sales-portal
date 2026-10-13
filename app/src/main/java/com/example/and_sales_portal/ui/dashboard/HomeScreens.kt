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
import com.example.and_sales_portal.data.sales.Incentive
import com.example.and_sales_portal.data.sales.SalesSummary
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
    val dayOff: Boolean,
    /** This week's toko by partner with "Sudah dikunjungi minggu ini" / "Belum dikunjungi" (PRD alignment 2026-10-09). */
    val groups: List<WeekPartner>,
    val partners: List<Partner>,
    val month: SalesSummary,
    val incentive: Incentive,
    /** Unread in-app notifications (FR-MP-004), newest first. */
    val unread: List<AppNotification> = emptyList(),
)

/**
 * Spec: SLS-01 Beranda SA/SR (PRD v3 D1; revision 2026-10-08): Absensi card, this week's visits ("Lengkap n/n" / "Belum
 * lengkap n/m", today) with the assigned toko, assigned partners, this month's # submitted, # accepted, # paid out and
 * paid-out amount, incentive estimate.
 */
@Composable
fun SalesHomeScreen(
    s: SalesHomeState,
    attendance: AttendanceActions,
    onVisits: () -> Unit,
    onPartners: () -> Unit,
    onIncentive: () -> Unit,
    onNotifications: () -> Unit = {},
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
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            SectionHeader(stringResource(R.string.home_week_visits), stringResource(R.string.home_see_all), onVisits)
            VisitWeekBadge(s.week)
            Text(
                stringResource(
                    when {
                        s.dayOff -> R.string.vis_today_off
                        s.visitedToday -> R.string.vis_today_done
                        else -> R.string.vis_today_not_yet
                    },
                ),
                color = c.content.primary,
                style = AmarTheme.typography.bodyMd,
            )
            Text(stringResource(R.string.vis_stores), color = c.content.secondary, style = AmarTheme.typography.captionSm)
            if (s.groups.isEmpty()) Text(stringResource(R.string.vis_no_stores), color = c.content.secondary, style = AmarTheme.typography.bodySm)
            s.groups.forEach { g ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
                    Text(stringResource(R.string.home_partner_stores, g.partner.name, g.stores.size), Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.labelMdMedium)
                    VisitedBadge(g.visited, partner = true)
                }
            }
        }
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            SectionHeader(stringResource(R.string.home_partners), stringResource(R.string.home_see_all), onPartners)
            if (s.partners.isEmpty()) Text(stringResource(R.string.ptn_mine_empty), color = c.content.secondary, style = AmarTheme.typography.bodySm)
            s.partners.forEach { p ->
                Text(p.name, color = c.content.primary, style = AmarTheme.typography.labelMdMedium)
            }
        }
        SalesSummaryCard(stringResource(R.string.home_this_month), s.month)
        IncentiveHero(s.incentive, onIncentive)
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
    val teamMonth: SalesSummary,
    val incentive: Incentive,
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
    val onIncentive: () -> Unit,
    val onNotifications: () -> Unit = {},
)

/**
 * Spec: TL-06 Beranda TL (PRD v3 C1; Figma "01 | Team Leader Dashboard"): Absensi card; team attendance today, team visits
 * (today and complete weeks, revision 2026-10-08), own visits this week; Pengajuan Partner by status with Revision Required highlighted; this month's team sales and own
 * incentive estimate; shortcuts. Figma's Lead and RTS metrics are out of scope (PRD) and replaced by today's activity.
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
        MetricTilesCard(
            stringResource(R.string.home_team_attendance),
            listOf(
                s.teamPresent.toString() to stringResource(R.string.home_present),
                s.teamNotYet.toString() to stringResource(R.string.home_not_yet),
                s.teamAbsent.toString() to stringResource(R.string.home_absent),
            ),
        ) { AmarButton(stringResource(R.string.nav_tim), a.onTeam, Modifier.fillMaxWidth(), type = AmarButtonType.Naked, size = AmarButtonSize.Small) }
        MetricTilesCard(
            stringResource(R.string.home_team_visits),
            listOf(
                s.teamVisitedToday.toString() to stringResource(R.string.home_visited_today),
                s.teamNotVisitedToday.toString() to stringResource(R.string.home_not_yet),
            ),
        ) { Text(stringResource(R.string.home_team_visits_week, s.teamCompleteWeek, s.teamSize), color = c.content.secondary, style = AmarTheme.typography.bodySm) }
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            SectionHeader(stringResource(R.string.home_my_week), stringResource(R.string.home_see_all), a.onMyVisits)
            s.myWeek?.let { VisitWeekBadge(it) } ?: Text(stringResource(R.string.vis_no_stores), color = c.content.secondary, style = AmarTheme.typography.bodySm)
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
        SalesSummaryCard(stringResource(R.string.home_this_month_team), s.teamMonth)
        IncentiveHero(s.incentive, a.onIncentive)
        if (s.contribution.isNotEmpty()) ContributionCard(s.contribution)
        AttendanceCard(s.attendance, s.online, attendance.onClockIn, attendance.onClockOut, onHistory = attendance.onHistory)
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
            SectionHeader(stringResource(R.string.home_shortcuts))
            AmarButton(stringResource(R.string.ptn_register), a.onRegister, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium)
            AmarButton(stringResource(R.string.home_schedule), a.onSchedule, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium)
            AmarButton(stringResource(R.string.act_assign), a.onAssign, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium)
        }
    }
}
