package com.example.and_sales_portal.ui.team

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.activity.VisitStatus
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.Store
import com.example.and_sales_portal.data.team.Productivity
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.actions.AmarChip
import com.example.and_sales_portal.designsystem.components.actions.AmarChipSupporting
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
import com.example.and_sales_portal.ui.visits.VisitOutcomeBadge
import com.example.and_sales_portal.ui.visits.VisitStatusBadge
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** A team member with today's attendance and assigned partners (TL-01). */
data class Member(val person: DemoDirectory.Person, val today: AttendanceStatus?, val partners: List<Partner>) {
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
                    if (m.partners.isEmpty()) stringResource(R.string.team_no_partner) else stringResource(R.string.team_partners, m.partners.joinToString { it.name }),
                    color = c.content.secondary,
                    style = AmarTheme.typography.bodySm,
                )
            }
        }
    }
}

/** Spec: TL-02 Detail Anggota (PRD v3 C5 "person detail with this month's … productivity"). */
@Composable
fun MemberDetailScreen(m: Member, productivity: Productivity?, onBack: () -> Unit, onSchedule: () -> Unit, onAssign: () -> Unit) {
    val c = AmarTheme.colors
    val p = m.person
    val action: (@Composable () -> Unit)? = if (m.active) { { PersistentAction(stringResource(R.string.team_schedule), onSchedule) } } else null
    ScreenScaffold(stringResource(R.string.team_detail_title), onBack, action = action) {
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Shadow) {
            Text(p.fullName, color = c.content.primary, style = AmarTheme.typography.headingSm)
            MemberBadges(m)
            InfoRow(stringResource(R.string.account_username), p.username)
            InfoRow(stringResource(R.string.account_email), p.email)
            InfoRow(stringResource(R.string.account_phone), "+62 ${p.phone}")
            InfoRow(stringResource(R.string.account_area), DemoDirectory.area(p.areaId)?.name ?: "-")
            InfoRow(stringResource(R.string.team_registered_label), date(p.createdAt))
        }
        if (!m.active) {
            AmarInfoBox(stringResource(R.string.team_pending_info), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
            return@ScreenScaffold
        }
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
            SectionTitle(stringResource(R.string.team_partners_title))
            if (m.partners.isEmpty()) Text(stringResource(R.string.team_no_partner), color = c.content.secondary, style = AmarTheme.typography.bodySm)
            m.partners.forEach { Text(it.name, color = c.content.primary, style = AmarTheme.typography.labelMdMedium) }
            AmarButton(stringResource(R.string.team_assign), onAssign, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium)
        }
        productivity?.let { pr ->
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
                SectionTitle(stringResource(R.string.team_month))
                InfoRow(stringResource(R.string.team_present), stringResource(R.string.team_present_value, pr.present, pr.workdays))
                InfoRow(stringResource(R.string.team_on_time), stringResource(R.string.team_days_value, pr.onTime))
                InfoRow(stringResource(R.string.team_late), stringResource(R.string.team_days_value, pr.late))
                InfoRow(stringResource(R.string.team_absent), stringResource(R.string.team_days_value, pr.absent))
                InfoRow(stringResource(R.string.team_days_off), stringResource(R.string.team_days_value, pr.daysOff))
                InfoRow(stringResource(R.string.team_visits), stringResource(R.string.team_visits_value, pr.visitsDone, pr.visitsPlanned))
                InfoRow(stringResource(R.string.team_visits_late), pr.visitsLate.toString())
                InfoRow(stringResource(R.string.team_visits_missed), pr.visitsMissed.toString())
                InfoRow(stringResource(R.string.team_visits_cancelled), pr.visitsCancelled.toString())
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
 * Spec: TL-03 Assign SA/SR (PRD v3 C6): pick an SA or SR, then partners. Per partner, covering all its toko; SA max 1,
 * SR max 3; only the TL's Active partners in the person's area.
 */
@Composable
fun AssignScreen(
    people: List<DemoDirectory.Person>,
    selected: DemoDirectory.Person?,
    onSelect: (DemoDirectory.Person) -> Unit,
    assigned: List<Partner>,
    available: List<Partner>,
    onBack: () -> Unit,
    onAssign: (Partner) -> Unit,
    onUnassign: (Partner) -> Unit,
) {
    ScreenScaffold(stringResource(R.string.act_assign), onBack) {
        PersonPicker(people, selected, onSelect)
        if (selected == null) {
            AmarInfoBox(stringResource(R.string.asg_select_person), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
            return@ScreenScaffold
        }
        AmarInfoBox(stringResource(if (selected.role == "SA") R.string.asg_rule_sa else R.string.asg_rule_sr), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Info)
        SectionTitle(stringResource(R.string.asg_assigned, assigned.size))
        if (assigned.isEmpty()) Text(stringResource(R.string.asg_none), color = AmarTheme.colors.content.secondary, style = AmarTheme.typography.bodySm)
        assigned.forEach { PartnerRow(it, stringResource(R.string.asg_unassign), AmarButtonType.Secondary) { onUnassign(it) } }
        SectionTitle(stringResource(R.string.asg_available))
        if (available.isEmpty()) Text(stringResource(R.string.asg_no_partners), color = AmarTheme.colors.content.secondary, style = AmarTheme.typography.bodySm)
        available.forEach { PartnerRow(it, stringResource(R.string.asg_assign), AmarButtonType.Primary) { onAssign(it) } }
    }
}

@Composable
private fun PartnerRow(p: Partner, action: String, type: AmarButtonType, onClick: () -> Unit) {
    val c = AmarTheme.colors
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
                Text(p.name, color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                Text(stringResource(R.string.asg_store_count, p.stores.count { it.status == "ACTIVE" }, p.id), color = c.content.secondary, style = AmarTheme.typography.bodySm)
            }
            AmarButton(action, onClick, type = type, size = AmarButtonSize.Small)
        }
    }
}

/** A planned day of the week strip. */
data class PlanDay(val date: LocalDate, val visits: Int, val off: Boolean)

/**
 * Spec: TL-04 Jadwal Kunjungan (PRD v3 C7): SA/SR and week (Monday–Sunday), visits of the selected day with status;
 * add (max 3 a day, not in the past, not on a day off), edit and cancel before check-in, days off, "Salin jadwal minggu
 * lalu".
 */
@Composable
fun ScheduleScreen(
    people: List<DemoDirectory.Person>,
    selected: DemoDirectory.Person?,
    onSelect: (DemoDirectory.Person) -> Unit,
    week: List<PlanDay>,
    day: LocalDate,
    today: LocalDate,
    visits: List<VisitItem>,
    hasStores: Boolean,
    onBack: () -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onDay: (LocalDate) -> Unit,
    onCopyLastWeek: () -> Unit,
    onToggleOff: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (VisitItem) -> Unit,
    onCancel: (VisitItem) -> Unit,
) {
    val c = AmarTheme.colors
    val selectedDay = week.firstOrNull { it.date == day }
    val past = day.isBefore(today)
    val active = visits.count { it.status != VisitStatus.CANCELLED }
    val canAdd = selected != null && hasStores && !past && selectedDay?.off != true && active < 3
    val action: (@Composable () -> Unit)? = if (selected != null) { { PersistentAction(stringResource(R.string.sch_add), onAdd, enabled = canAdd) } } else null
    ScreenScaffold(stringResource(R.string.act_schedule), onBack, action = action) {
        PersonPicker(people, selected, onSelect)
        if (selected == null) {
            AmarInfoBox(stringResource(R.string.asg_select_person), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
            return@ScreenScaffold
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            AmarButton(null, onPrevWeek, type = AmarButtonType.Naked, size = AmarButtonSize.Small, icon = painterResource(R.drawable.ds_ic_chevron_left_24), contentDescription = stringResource(R.string.sch_prev_week))
            val f = DateTimeFormatter.ofPattern("d MMM yyyy", ID)
            Text("${week.first().date.dayOfMonth} – ${f.format(week.last().date)}", Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.labelMdSemibold, textAlign = TextAlign.Center)
            AmarButton(null, onNextWeek, type = AmarButtonType.Naked, size = AmarButtonSize.Small, icon = painterResource(R.drawable.ds_ic_chevron_right_24), contentDescription = stringResource(R.string.sch_next_week))
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
            val f = DateTimeFormatter.ofPattern("EEE d", ID)
            week.forEach { d ->
                AmarChip(
                    f.format(d.date),
                    d.date == day,
                    { onDay(d.date) },
                    supporting = if (d.visits > 0) AmarChipSupporting.Counter else AmarChipSupporting.Default,
                    counterValue = d.visits.toString(),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
            AmarButton(stringResource(R.string.sch_copy), onCopyLastWeek, type = AmarButtonType.Naked, size = AmarButtonSize.Small, leftIcon = painterResource(R.drawable.ds_ic_copy_24))
            if (!past) AmarButton(stringResource(if (selectedDay?.off == true) R.string.sch_unmark_off else R.string.sch_mark_off), onToggleOff, type = AmarButtonType.Naked, size = AmarButtonSize.Small, leftIcon = painterResource(R.drawable.ds_ic_calendar_24))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(Fmt.longDate(day), Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.headingSm)
            Text(stringResource(R.string.sch_count, active), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        }
        when {
            !hasStores -> AmarInfoBox(stringResource(R.string.sch_no_store), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
            past -> AmarInfoBox(stringResource(R.string.sch_past_info), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
            selectedDay?.off == true -> AmarInfoBox(stringResource(R.string.sch_off_info), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Info)
            active >= 3 -> AmarInfoBox(stringResource(R.string.sch_max_info), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Neutral)
        }
        if (visits.isEmpty() && selectedDay?.off != true) Text(stringResource(R.string.sch_empty), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        visits.forEach { v -> PlannedVisitCard(v, editable = v.status == VisitStatus.NOT_STARTED && !past, onEdit = { onEdit(v) }, onCancel = { onCancel(v) }) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlannedVisitCard(v: VisitItem, editable: Boolean, onEdit: () -> Unit, onCancel: () -> Unit) {
    val c = AmarTheme.colors
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
        Text(stringResource(R.string.vis_planned, Fmt.withZone(v.visit.plannedTime)), color = c.content.primary, style = AmarTheme.typography.headingSm)
        Text(v.store.name, color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
        Text(v.partner.name, color = c.content.secondary, style = AmarTheme.typography.bodySm)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
            VisitStatusBadge(v.status)
            v.outcome?.let { VisitOutcomeBadge(it) }
        }
        v.visit.cancelReason?.let { Text(it, color = c.content.tertiary, style = AmarTheme.typography.bodySm) }
        if (editable) {
            Row(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
                AmarButton(stringResource(R.string.sch_edit), onEdit, type = AmarButtonType.Secondary, size = AmarButtonSize.Small)
                AmarButton(stringResource(R.string.sch_cancel), onCancel, type = AmarButtonType.Naked, size = AmarButtonSize.Small)
            }
        }
    }
}

/** Spec: TL-05 Tambah / Ubah Kunjungan: toko of the person's assigned partners and a planned time (PRD v3 C7). */
@Composable
fun VisitPlanFormScreen(
    editing: Boolean,
    personName: String,
    date: LocalDate,
    stores: List<Store>,
    store: Store?,
    time: String?,
    storeError: String?,
    timeError: String?,
    onStore: (Store) -> Unit,
    onTime: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    ScreenScaffold(
        stringResource(if (editing) R.string.sch_form_edit else R.string.sch_form_add),
        onBack,
        action = { PersistentAction(stringResource(R.string.sch_form_save), onSave) },
    ) {
        Text(stringResource(R.string.sch_form_desc, personName, Fmt.longDate(date)), color = AmarTheme.colors.content.secondary, style = AmarTheme.typography.bodySm)
        AmarDropdownPicker(
            value = store,
            options = stores,
            onSelect = onStore,
            sheetTitle = stringResource(R.string.sch_form_store_sheet),
            modifier = Modifier.fillMaxWidth(),
            optionLabel = { it.name },
            label = stringResource(R.string.sch_form_store),
            required = true,
            placeholder = stringResource(R.string.sch_form_store_placeholder),
            hint = storeError,
            error = storeError != null,
        )
        val res = LocalContext.current.resources
        AmarDropdownPicker(
            value = time,
            options = ActivityRules.PLAN_TIMES,
            onSelect = onTime,
            sheetTitle = stringResource(R.string.sch_form_time_sheet),
            modifier = Modifier.fillMaxWidth(),
            optionLabel = { res.getString(R.string.sch_time_option, Fmt.withZone(it)) },
            label = stringResource(R.string.sch_form_time),
            required = true,
            placeholder = stringResource(R.string.sch_form_time_placeholder),
            hint = timeError,
            error = timeError != null,
        )
    }
}
