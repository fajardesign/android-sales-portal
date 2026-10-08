package com.example.and_sales_portal.ui.team

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import com.example.and_sales_portal.AppContainer
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.PlanError
import com.example.and_sales_portal.data.activity.DemoSeed
import com.example.and_sales_portal.data.activity.VisitState
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.partner.AssignError
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.PartnerStatus
import com.example.and_sales_portal.data.partner.Store
import com.example.and_sales_portal.designsystem.components.feedback.AmarBottomSheet
import com.example.and_sales_portal.ui.common.ComposeFragment
import com.example.and_sales_portal.ui.common.ConfirmDialog
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.WithToast
import com.example.and_sales_portal.ui.visits.visitItem
import java.time.DayOfWeek
import java.time.LocalDate

private const val ARG_EMPLOYEE = "employee_id"
private const val ARG_VISIT = "visit_id"
private const val ARG_DATE = "date"
private const val NONE = -1L

/** Team members of [tlId] with today's attendance and assigned partners. */
private fun AppContainer.members(tlId: Long): List<Member> {
    val today = operations.today()
    return team.teamOf(tlId).map { p ->
        Member(p, if (p.status == "ACTIVE") operations.attendanceStatus(p.id, today) else null, partners.assignedPartners(p.id))
    }
}

/** Active SA/SR the TL can assign and plan for. */
private fun AppContainer.activeTeam(tlId: Long) = team.teamOf(tlId).filter { it.status == "ACTIVE" }

/** Spec: TL-01 Tim (TL tab). */
class TeamTabFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val tl = session ?: return
        val ops by container.operations.data.collectAsState()
        val partners by container.partners.partners.collectAsState()
        val members = remember(ops, partners) { container.members(tl.user.id) }
        TeamListScreen(members) { navigate(R.id.team_detail, bundleOf(ARG_EMPLOYEE to it.person.id)) }
    }
}

/** Spec: TL-02 Detail Anggota. */
class MemberDetailFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val tl = session ?: return
        val id = requireArguments().getLong(ARG_EMPLOYEE)
        val ops by container.operations.data.collectAsState()
        val partners by container.partners.partners.collectAsState()
        val (toast, clearToast) = incomingToast()
        val member = remember(ops, partners) { container.members(tl.user.id).firstOrNull { it.person.id == id } } ?: return
        val productivity = remember(ops) { if (member.active) container.team.productivity(id) else null }
        WithToast(toast, clearToast) {
            MemberDetailScreen(
                member, productivity, ::back,
                onSchedule = { navigate(R.id.schedule, bundleOf(ARG_EMPLOYEE to id)) },
                onAssign = { navigate(R.id.assign, bundleOf(ARG_EMPLOYEE to id)) },
            )
        }
    }
}

/** Spec: TL-03 Assign SA/SR. */
class AssignFragment : ComposeFragment() {

    private sealed interface Pending {
        data class Assign(val p: Partner) : Pending
        data class Unassign(val p: Partner) : Pending
        data class Move(val from: Partner, val to: Partner) : Pending
        data class Error(val error: AssignError, val to: Partner) : Pending
    }

    @Composable
    override fun ScreenContent() {
        val tl = session ?: return
        val tlId = tl.user.id
        val partners by container.partners.partners.collectAsState()
        val people = remember { container.activeTeam(tlId) }
        var selectedId by rememberSaveable { mutableLongStateOf(arguments?.getLong(ARG_EMPLOYEE, NONE) ?: NONE) }
        var pending by remember { mutableStateOf<Pending?>(null) }
        var toast by remember { mutableStateOf<String?>(null) }
        val person = people.firstOrNull { it.id == selectedId }
        val assigned = remember(partners, selectedId) { person?.let { container.partners.assignedPartners(it.id) }.orEmpty() }
        val available = remember(partners, selectedId) {
            person?.let { p -> partners.filter { it.status == PartnerStatus.ACTIVE && it.ownerTlId == tlId && it.areaId == p.areaId && it !in assigned } }.orEmpty()
        }
        WithToast(toast, { toast = null }) {
            AssignScreen(
                people, person, { selectedId = it.id }, assigned, available, ::back,
                onAssign = { p ->
                    val error = person?.let { container.partners.assignmentError(tlId, it.id, p.id) }
                    pending = if (error == null) Pending.Assign(p) else Pending.Error(error, p)
                },
                onUnassign = { pending = Pending.Unassign(it) },
            )
        }
        val name = person?.fullName ?: return
        when (val p = pending) {
            is Pending.Assign -> ConfirmDialog(
                stringResource(R.string.asg_confirm_assign_title), stringResource(R.string.asg_confirm_assign, name, p.p.name), stringResource(R.string.asg_assign),
                onConfirm = {
                    pending = null
                    if (container.team.assign(tlId, selectedId, p.p.id) == null) toast = getString(R.string.asg_done_assign, name, p.p.name)
                },
                onDismiss = { pending = null },
            )
            is Pending.Unassign -> ConfirmDialog(
                stringResource(R.string.asg_confirm_unassign_title), stringResource(R.string.asg_confirm_unassign, name, p.p.name), stringResource(R.string.asg_unassign),
                onConfirm = {
                    pending = null
                    val n = container.team.unassign(selectedId, p.p.id, getString(R.string.asg_cancel_reason))
                    toast = getString(R.string.asg_done_unassign, name, p.p.name, n)
                },
                onDismiss = { pending = null },
            )
            is Pending.Move -> ConfirmDialog(
                stringResource(R.string.asg_confirm_move_title), stringResource(R.string.asg_confirm_move, name, p.from.name, p.to.name), stringResource(R.string.asg_move),
                onConfirm = {
                    pending = null
                    if (container.team.reassign(tlId, selectedId, p.to.id, getString(R.string.asg_cancel_reason)) == null) toast = getString(R.string.asg_done_assign, name, p.to.name)
                },
                onDismiss = { pending = null },
            )
            is Pending.Error -> {
                // PRD C6 error copy; an SA can be moved to the other partner instead (reassign).
                val from = assigned.firstOrNull()
                val canMove = p.error == AssignError.SaLimit && from != null
                AmarBottomSheet(
                    onDismissRequest = { pending = null },
                    title = stringResource(R.string.asg_err_title),
                    description = stringResource(errorText(p.error)) + if (canMove) "\n" + stringResource(R.string.asg_err_move_hint, name, from!!.name) else "",
                    primaryLabel = stringResource(if (canMove) R.string.asg_move else R.string.auth_understood),
                    onPrimaryClick = { pending = if (canMove) Pending.Move(from!!, p.to) else null },
                    secondLabel = if (canMove) stringResource(R.string.common_cancel) else null,
                    onSecondClick = { pending = null },
                )
            }
            null -> Unit
        }
    }

    private fun errorText(e: AssignError) = when (e) {
        AssignError.SaLimit -> R.string.asg_err_sa
        AssignError.SrLimit -> R.string.asg_err_sr
        AssignError.OtherArea -> R.string.asg_err_area
        AssignError.NotOwner -> R.string.asg_err_owner
        AssignError.NotLeader -> R.string.asg_err_leader
    }
}

/** Spec: TL-04 Jadwal Kunjungan. */
class ScheduleFragment : ComposeFragment() {

    private sealed interface Pending {
        data class Cancel(val visitId: String, val store: String, val time: String) : Pending
        data object DayOff : Pending
    }

    @Composable
    override fun ScreenContent() {
        val tl = session ?: return
        val ops by container.operations.data.collectAsState()
        val partners by container.partners.partners.collectAsState()
        val repo = container.operations
        val today = repo.today()
        val people = remember { container.activeTeam(tl.user.id) }
        var selectedId by rememberSaveable { mutableLongStateOf(arguments?.getLong(ARG_EMPLOYEE, NONE) ?: NONE) }
        var dayEpoch by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
        var pending by remember { mutableStateOf<Pending?>(null) }
        var localToast by remember { mutableStateOf<String?>(null) }
        val (incoming, clearIncoming) = incomingToast()
        val day = LocalDate.ofEpochDay(dayEpoch)
        val weekStart = day.with(DayOfWeek.MONDAY)
        val person = people.firstOrNull { it.id == selectedId }
        val week = remember(ops, selectedId, weekStart) {
            (0L..6L).map { weekStart.plusDays(it) }.map { d ->
                PlanDay(d, person?.let { p -> repo.visitsOf(p.id, d).count { it.state != VisitState.CANCELLED } } ?: 0, person != null && repo.isDayOff(person.id, d))
            }
        }
        val visits = remember(ops, partners, selectedId, dayEpoch) {
            person?.let { p ->
                repo.visitsOf(p.id, day).mapNotNull { v ->
                    val partner = partners.firstOrNull { pt -> pt.stores.any { it.id == v.storeId } }
                    visitItem(v, partner?.stores?.firstOrNull { it.id == v.storeId }, partner, today)
                }
            }.orEmpty()
        }
        val hasStores = remember(partners, selectedId) { person?.let { storesFor(it).isNotEmpty() } ?: false }
        WithToast(incoming ?: localToast, { clearIncoming(); localToast = null }) {
            ScheduleScreen(
                people, person, { selectedId = it.id }, week, day, today, visits, hasStores, ::back,
                onPrevWeek = { dayEpoch = weekStart.minusWeeks(1).toEpochDay() },
                onNextWeek = { dayEpoch = weekStart.plusWeeks(1).toEpochDay() },
                onDay = { dayEpoch = it.toEpochDay() },
                onCopyLastWeek = {
                    val n = repo.copyLastWeek(selectedId, weekStart, tl.user.id)
                    localToast = if (n > 0) getString(R.string.sch_copy_done, n) else getString(R.string.sch_copy_none)
                },
                onToggleOff = {
                    if (repo.isDayOff(selectedId, day)) repo.setDayOff(selectedId, day, false, "") else pending = Pending.DayOff
                },
                onAdd = { navigate(R.id.visit_plan_form, bundleOf(ARG_EMPLOYEE to selectedId, ARG_DATE to dayEpoch)) },
                onEdit = { navigate(R.id.visit_plan_form, bundleOf(ARG_EMPLOYEE to selectedId, ARG_DATE to dayEpoch, ARG_VISIT to it.visit.id)) },
                onCancel = { pending = Pending.Cancel(it.visit.id, it.store.name, it.visit.plannedTime) },
            )
        }
        when (val p = pending) {
            is Pending.Cancel -> ConfirmDialog(
                stringResource(R.string.sch_cancel_confirm_title), stringResource(R.string.sch_cancel_confirm, p.store, Fmt.withZone(p.time)), stringResource(R.string.sch_cancel),
                onConfirm = {
                    pending = null
                    repo.cancelVisit(p.visitId, getString(R.string.sch_cancel_reason))
                    localToast = getString(R.string.sch_cancel_done)
                },
                onDismiss = { pending = null },
            )
            Pending.DayOff -> ConfirmDialog(
                stringResource(R.string.sch_off_confirm_title), stringResource(R.string.sch_off_confirm, person?.fullName.orEmpty(), Fmt.longDate(day)), stringResource(R.string.sch_mark_off),
                onConfirm = {
                    pending = null
                    repo.setDayOff(selectedId, day, true, getString(R.string.sch_off_reason))
                },
                onDismiss = { pending = null },
            )
            null -> Unit
        }
    }

    private fun storesFor(p: DemoDirectory.Person): List<Store> =
        DemoSeed.storesOf(container.partners.partners.value, p.id, SalesRole.valueOf(p.role))
}

/** Spec: TL-05 Tambah / Ubah Kunjungan. */
class VisitPlanFormFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val tl = session ?: return
        val args = requireArguments()
        val employeeId = args.getLong(ARG_EMPLOYEE)
        val date = LocalDate.ofEpochDay(args.getLong(ARG_DATE))
        val visitId = args.getString(ARG_VISIT)
        val repo = container.operations
        val person = DemoDirectory.person(employeeId) ?: return
        val stores = remember {
            DemoSeed.storesOf(container.partners.partners.value, employeeId, SalesRole.valueOf(person.role))
        }
        val existing = remember { visitId?.let(repo::visit) }
        var storeId by rememberSaveable { mutableStateOf(existing?.storeId) }
        var time by rememberSaveable { mutableStateOf(existing?.plannedTime) }
        var error by remember { mutableStateOf<PlanError?>(null) }
        var tried by remember { mutableStateOf(false) }
        val storeError = when {
            tried && storeId == null -> stringResource(R.string.reg_required)
            error == PlanError.StoreNotAssigned -> stringResource(R.string.sch_err_store)
            else -> null
        }
        val timeError = when {
            tried && time == null -> stringResource(R.string.reg_required)
            else -> error?.takeIf { it != PlanError.StoreNotAssigned }?.let { stringResource(planErrorText(it)) }
        }
        VisitPlanFormScreen(
            editing = visitId != null,
            personName = person.fullName,
            date = date,
            stores = stores,
            store = stores.firstOrNull { it.id == storeId },
            time = time,
            storeError = storeError,
            timeError = timeError,
            onStore = { storeId = it.id; error = null },
            onTime = { time = it; error = null },
            onBack = ::back,
            onSave = {
                tried = true
                val s = storeId
                val t = time
                if (s != null && t != null) {
                    error = if (visitId != null) repo.editVisit(visitId, s, t) else repo.addVisit(employeeId, date, s, t, tl.user.id)
                    if (error == null) backWithToast(getString(R.string.sch_saved, stores.first { it.id == s }.name, Fmt.withZone(t)))
                }
            },
        )
    }

    private fun planErrorText(e: PlanError) = when (e) {
        PlanError.PastDate -> R.string.sch_err_past
        PlanError.DayOff -> R.string.sch_err_off
        PlanError.MaxPerDay -> R.string.sch_err_max
        PlanError.DuplicateTime -> R.string.sch_err_duplicate
        PlanError.StoreNotAssigned -> R.string.sch_err_store
        PlanError.AlreadyStarted -> R.string.sch_err_started
    }
}
