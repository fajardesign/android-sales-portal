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
import com.example.and_sales_portal.ui.visits.VisitItem
import com.example.and_sales_portal.ui.visits.VisitStatusBadge
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
    val visits: List<VisitItem>,
    val partners: List<Partner>,
    val month: SalesSummary,
    val incentive: Incentive,
)

/**
 * Spec: SLS-01 Beranda SA/SR (PRD v3 D1): Absensi card, today's visit schedule with status chips, assigned partners, this
 * month's # submitted, # accepted, # paid out and paid-out amount, incentive estimate.
 */
@Composable
fun SalesHomeScreen(
    s: SalesHomeState,
    attendance: AttendanceActions,
    onVisits: () -> Unit,
    onVisit: (VisitItem) -> Unit,
    onPartners: () -> Unit,
    onIncentive: () -> Unit,
) {
    val c = AmarTheme.colors
    ScreenScaffold(stringResource(R.string.home_greeting, s.firstName), onBack = null) {
        PriorityHeading(s.role, s.date)
        AttendanceCard(s.attendance, s.online, attendance.onClockIn, attendance.onClockOut, onHistory = attendance.onHistory)
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            SectionHeader(stringResource(R.string.home_visits_today), stringResource(R.string.home_see_all), onVisits)
            if (s.visits.isEmpty()) Text(stringResource(R.string.home_no_visits), color = c.content.secondary, style = AmarTheme.typography.bodySm)
            s.visits.forEach { v ->
                AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border, onClick = { onVisit(v) }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
                        Text(stringResource(R.string.vis_planned, v.visit.plannedTime), color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                        Text(v.store.name, Modifier.weight(1f), c.content.secondary, style = AmarTheme.typography.bodySm, maxLines = 1)
                        VisitStatusBadge(v.status)
                    }
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
    val myVisitsDone: Int,
    val myVisitsTotal: Int,
    val teamPresent: Int,
    val teamNotYet: Int,
    val teamAbsent: Int,
    val visitsPlanned: Int,
    val visitsDone: Int,
    val visitsLate: Int,
    val visitsMissed: Int,
    val submissions: Map<PartnerStatus, Int>,
    val teamMonth: SalesSummary,
    val incentive: Incentive,
    val contribution: List<Pair<String, Double>>,
)

class TlHomeActions(
    val onMyVisits: () -> Unit,
    val onTeam: () -> Unit,
    val onSubmissions: () -> Unit,
    val onRegister: () -> Unit,
    val onSchedule: () -> Unit,
    val onAssign: () -> Unit,
    val onIncentive: () -> Unit,
)

/**
 * Spec: TL-06 Beranda TL (PRD v3 C1; Figma "01 | Team Leader Dashboard"): Absensi card; today — own visits, team attendance
 * and team visits; Pengajuan Partner by status with Revision Required highlighted; this month's team sales and own
 * incentive estimate; shortcuts. Figma's Lead and RTS metrics are out of scope (PRD) and replaced by today's activity.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TlHomeScreen(s: TlHomeState, attendance: AttendanceActions, a: TlHomeActions) {
    val c = AmarTheme.colors
    ScreenScaffold(stringResource(R.string.home_greeting, s.firstName), onBack = null) {
        PriorityHeading(SalesRole.TL, s.date)
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
                s.visitsPlanned.toString() to stringResource(R.string.home_planned),
                s.visitsDone.toString() to stringResource(R.string.home_done),
                s.visitsLate.toString() to stringResource(R.string.home_late),
                s.visitsMissed.toString() to stringResource(R.string.home_missed),
            ),
        )
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            SectionHeader(stringResource(R.string.home_my_visits), stringResource(R.string.home_see_all), a.onMyVisits)
            Text(stringResource(R.string.home_my_visits_value, s.myVisitsDone, s.myVisitsTotal), color = c.content.secondary, style = AmarTheme.typography.bodyMd)
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
