package com.example.and_sales_portal.ui.team

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.partner.AssignmentAction
import com.example.and_sales_portal.data.partner.AssignmentEvent
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.ui.notifications.eventTime
import com.example.and_sales_portal.data.partner.Store
import com.example.and_sales_portal.data.team.Productivity
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadge
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeSize
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeType
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
import com.example.and_sales_portal.designsystem.components.inputs.AmarDropdownPicker
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.account.roleLabel
import com.example.and_sales_portal.ui.attendance.AttendanceBadge
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.InfoRow
import com.example.and_sales_portal.ui.common.PersistentAction
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.common.SectionTitle
import com.example.and_sales_portal.ui.visits.VisitItem
import com.example.and_sales_portal.ui.visits.VisitWeekBadge
import com.example.and_sales_portal.data.activity.VisitWeek
import com.example.and_sales_portal.ui.visits.VisitStatusBadge
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** A team member with today's attendance and assigned toko (TL-01); no minimum of toko (PRD Scope 1 v1.1 C6). */
data class Member(val person: DemoDirectory.Person, val today: AttendanceStatus?, val stores: List<Pair<Partner, Store>>) {
    val active: Boolean get() = person.status == "ACTIVE"
}

private val ID = Locale("id", "ID")
private fun date(millis: Long) = Fmt.shortDate(Instant.ofEpochMilli(millis).atZone(ActivityRules.ZONE).toLocalDate())

@Composable
private fun RoleBadge(role: String) {
    val r = runCatching { SalesRole.valueOf(role) }.getOrNull() ?: return
    AmarBadge(stringResource(roleLabel(r)), type = AmarBadgeType.Info, size = AmarBadgeSize.Small)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemberBadges(m: Member) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
        RoleBadge(m.person.role)
        if (!m.active) AmarBadge(stringResource(R.string.team_pending), type = AmarBadgeType.Warning, size = AmarBadgeSize.Small)
        m.today?.let { AttendanceBadge(it) }
    }
}

/** Spec: TL-01 Tim (PRD v3 C5): SA/SR under the TL with role, registration date, contact, partners and today's attendance. */
@Composable
fun TeamListScreen(members: List<Member>, onOpen: (Member) -> Unit) {
    val c = AmarTheme.colors
    ScreenScaffold(stringResource(R.string.nav_tim), onBack = null) {
        if (members.isEmpty()) AmarInfoBox(stringResource(R.string.team_empty), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
        members.forEach { m ->
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border, onClick = { onOpen(m) }) {
                Text(m.person.fullName, color = c.content.primary, style = AmarTheme.typography.headingSm)
                MemberBadges(m)
                Text("${m.person.email} · +62 ${m.person.phone}", color = c.content.secondary, style = AmarTheme.typography.bodySm)
                Text(stringResource(R.string.team_registered, date(m.person.createdAt)), color = c.content.tertiary, style = AmarTheme.typography.bodySm)
                Text(
                    if (m.stores.isEmpty()) stringResource(R.string.team_no_store) else stringResource(R.string.team_stores, m.stores.size, m.stores.joinToString { it.second.name }),
                    color = c.content.secondary,
                    style = AmarTheme.typography.bodySm,
                )
            }
        }
    }
}

/** Spec: TL-02 Detail Anggota (PRD v3 C5 "person detail with this month's … productivity"). */
@Composable
fun MemberDetailScreen(m: Member, productivity: Productivity?, onBack: () -> Unit, onSchedule: () -> Unit, onAssign: () -> Unit, onAttendance: () -> Unit = {}) {
    val c = AmarTheme.colors
    val p = m.person
    val action: (@Composable () -> Unit)? = if (m.active) { { PersistentAction(stringResource(R.string.team_schedule), onSchedule) } } else null
    ScreenScaffold(stringResource(R.string.team_detail_title), onBack, action = action) {
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            Text(p.fullName, color = c.content.primary, style = AmarTheme.typography.headingSm)
            MemberBadges(m)
            InfoRow(stringResource(R.string.account_email), p.email)
            InfoRow(stringResource(R.string.account_phone), "+62 ${p.phone}")
            InfoRow(stringResource(R.string.account_area), DemoDirectory.area(p.areaId)?.name ?: "-")
            // DD review point 7: a replaced leader shows here (the TL viewing it is the new leader).
            InfoRow(stringResource(R.string.account_supervisor), DemoDirectory.person(p.supervisorId)?.fullName ?: "-")
            InfoRow(stringResource(R.string.team_registered_label), date(p.createdAt))
        }
        if (!m.active) {
            AmarInfoBox(stringResource(R.string.team_pending_info), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
            return@ScreenScaffold
        }
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
            SectionTitle(stringResource(R.string.team_partners_title))
            if (m.stores.isEmpty()) Text(stringResource(R.string.team_no_store), color = c.content.secondary, style = AmarTheme.typography.bodySm)
            m.stores.forEach { (p, s) ->
                Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
                    Text(s.name, color = c.content.primary, style = AmarTheme.typography.labelMdMedium)
                    Text(p.name, color = c.content.secondary, style = AmarTheme.typography.bodySm)
                }
            }
            AmarButton(stringResource(R.string.team_assign), onAssign, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium)
        }
        productivity?.let { pr ->
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
                SectionTitle(stringResource(R.string.team_month))
                InfoRow(stringResource(R.string.team_present), stringResource(R.string.team_present_value, pr.present, pr.workdays))
                InfoRow(stringResource(R.string.team_on_time), stringResource(R.string.team_days_value, pr.onTime))
                InfoRow(stringResource(R.string.team_late), stringResource(R.string.team_days_value, pr.late))
                InfoRow(stringResource(R.string.team_absent), stringResource(R.string.team_days_value, pr.absent))
                // Q6: a check in without check out stays present; 6 check ins a week.
                if (pr.noCheckOut > 0) InfoRow(stringResource(R.string.att_no_check_out), stringResource(R.string.team_days_value, pr.noCheckOut))
                pr.attendanceWeek?.let { w -> InfoRow(stringResource(R.string.team_week_checkins), stringResource(R.string.team_checkins_value, w.checkIns, w.target)) }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.team_week_visits), Modifier.weight(1f), c.content.secondary, style = AmarTheme.typography.bodySm)
                    VisitWeekBadge(pr.thisWeek)
                }
                // TL-11 (decision 2026-10-19): the day list with `Koreksi Absensi`.
                AmarButton(stringResource(R.string.cor_see_attendance), onAttendance, Modifier.fillMaxWidth(), type = AmarButtonType.Naked, size = AmarButtonSize.Small)
            }
        }
        AmarInfoBox(stringResource(R.string.team_sales_wip), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
    }
}

/** Person picker of TL-03 and TL-04: active SA/SR of the team. */
@Composable
fun PersonPicker(people: List<DemoDirectory.Person>, selected: DemoDirectory.Person?, onSelect: (DemoDirectory.Person) -> Unit) {
    AmarDropdownPicker(
        value = selected,
        options = people,
        onSelect = onSelect,
        sheetTitle = stringResource(R.string.asg_person_sheet),
        modifier = Modifier.fillMaxWidth(),
        optionLabel = { "${it.fullName} (${it.role})" },
        label = stringResource(R.string.asg_person),
        placeholder = stringResource(R.string.asg_person_placeholder),
    )
}

/**
 * Spec: TL-03 Assign SA/SR (PRD v3 C6, revision 2026-10-08): pick an SA or SR, then toko one by one; SA 1–3 toko, SR 4–6
 * (the maximum blocks); only active toko of the TL's Active partners in the person's area.
 */
@Composable
fun AssignScreen(
    people: List<DemoDirectory.Person>,
    selected: DemoDirectory.Person?,
    onSelect: (DemoDirectory.Person) -> Unit,
    assigned: List<Pair<Partner, Store>>,
    available: List<Pair<Partner, Store>>,
    onBack: () -> Unit,
    onAssign: (Pair<Partner, Store>) -> Unit,
    onUnassign: (Pair<Partner, Store>) -> Unit,
    history: List<AssignmentEvent> = emptyList(),
) {
    ScreenScaffold(stringResource(R.string.act_assign), onBack) {
        PersonPicker(people, selected, onSelect)
        if (selected == null) {
            AmarInfoBox(stringResource(R.string.asg_select_person), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
            return@ScreenScaffold
        }
        AmarInfoBox(stringResource(R.string.asg_rule), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Info)
        SectionTitle(stringResource(R.string.asg_assigned, assigned.size))
        if (assigned.isEmpty()) Text(stringResource(R.string.asg_none), color = AmarTheme.colors.content.secondary, style = AmarTheme.typography.bodySm)
        assigned.forEach { StoreRow(it, stringResource(R.string.asg_unassign), AmarButtonType.Secondary) { onUnassign(it) } }
        SectionTitle(stringResource(R.string.asg_available))
        if (available.isEmpty()) Text(stringResource(R.string.asg_no_stores), color = AmarTheme.colors.content.secondary, style = AmarTheme.typography.bodySm)
        available.forEach { StoreRow(it, stringResource(R.string.asg_assign), AmarButtonType.Primary) { onAssign(it) } }
        // FR-MP-007 (PRD alignment 2026-10-09): every Tugaskan and Lepas of this SA/SR, newest first.
        SectionTitle(stringResource(R.string.asg_history))
        if (history.isEmpty()) Text(stringResource(R.string.asg_history_empty), color = AmarTheme.colors.content.secondary, style = AmarTheme.typography.bodySm)
        history.forEach { e ->
            Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
                Text(
                    stringResource(if (e.action == AssignmentAction.ASSIGN) R.string.asg_history_assign else R.string.asg_history_release, e.storeName, e.partnerName),
                    color = AmarTheme.colors.content.primary, style = AmarTheme.typography.labelMdMedium,
                )
                // "oleh Andi Pratama", "oleh Hasan Basri (APL)", or "oleh Sistem (partner nonaktif)".
                val by = if (e.by != AssignmentEvent.SYSTEM) PartnerRepository.actorName(e.by) else stringResource(R.string.asg_by_system, e.source.orEmpty())
                Text(stringResource(R.string.asg_history_meta, by, eventTime(e)), color = AmarTheme.colors.content.tertiary, style = AmarTheme.typography.bodySm)
            }
        }
    }
}

@Composable
private fun StoreRow(item: Pair<Partner, Store>, action: String, type: AmarButtonType, onClick: () -> Unit) {
    val c = AmarTheme.colors
    val (p, s) = item
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
                Text(s.name, color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                Text(stringResource(R.string.asg_store_row, p.name, s.code ?: p.id), color = c.content.secondary, style = AmarTheme.typography.bodySm)
            }
            AmarButton(action, onClick, type = type, size = AmarButtonSize.Small)
        }
    }
}

/** A workday of TL-04 with the visits done on it. */
data class WeekDay(val date: LocalDate, val visits: List<VisitItem>)

/**
 * Spec: TL-04 Jadwal Kunjungan (PRD v3 C7; stakeholder revision 2026-10-08: no hourly plan). Per SA/SR and week
 * (Monday–Sunday, decision 2026-10-19): progress against the weekly target and visits done per day. No "Tandai Libur" (PRD Scope 2 S10).
 */
@Composable
fun ScheduleScreen(
    people: List<DemoDirectory.Person>,
    selected: DemoDirectory.Person?,
    onSelect: (DemoDirectory.Person) -> Unit,
    weekStart: LocalDate,
    week: VisitWeek?,
    days: List<WeekDay>,
    today: LocalDate,
    hasStores: Boolean,
    onBack: () -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    val c = AmarTheme.colors
    ScreenScaffold(stringResource(R.string.act_schedule), onBack) {
        PersonPicker(people, selected, onSelect)
        if (selected == null) {
            AmarInfoBox(stringResource(R.string.asg_select_person), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
            return@ScreenScaffold
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            AmarButton(null, onPrevWeek, type = AmarButtonType.Naked, size = AmarButtonSize.Small, icon = painterResource(R.drawable.ds_ic_chevron_left_24), contentDescription = stringResource(R.string.sch_prev_week))
            val f = DateTimeFormatter.ofPattern("d MMM yyyy", ID)
            Text("${weekStart.dayOfMonth} – ${f.format(ActivityRules.weekEnd(weekStart))}", Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.labelMdSemibold, textAlign = TextAlign.Center)
            AmarButton(null, onNextWeek, type = AmarButtonType.Naked, size = AmarButtonSize.Small, icon = painterResource(R.drawable.ds_ic_chevron_right_24), contentDescription = stringResource(R.string.sch_next_week))
        }
        if (!hasStores) AmarInfoBox(stringResource(R.string.sch_no_store), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
        week?.let { w ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.vis_week), Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.headingSm)
                VisitWeekBadge(w)
            }
        }
        val f = DateTimeFormatter.ofPattern("EEEE, d MMM", ID)
        days.forEach { d ->
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(f.format(d.date), Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                }
                if (d.visits.isEmpty()) Text(stringResource(R.string.sch_no_visit), color = c.content.secondary, style = AmarTheme.typography.bodySm)
                d.visits.forEach { v ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
                        Text(
                            stringResource(R.string.sch_visit_row, v.visit.checkIn?.let { Fmt.timeLabel(it, v.visit.employeeId) } ?: "-", v.store.name),
                            Modifier.weight(1f), c.content.secondary, style = AmarTheme.typography.bodySm, maxLines = 1,
                        )
                        VisitStatusBadge(v.status)
                    }
                }
            }
        }
    }
}
