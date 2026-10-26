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
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.DemoSeed
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
import java.time.LocalDate

private const val ARG_EMPLOYEE = "employee_id"
private const val NONE = -1L

/** Team members of [tlId] with today's attendance and assigned partners. */
internal fun AppContainer.members(tlId: Long): List<Member> {
    val today = operations.today()
    return team.teamOf(tlId).map { p ->
        Member(p, if (p.status == "ACTIVE") operations.attendanceStatus(p.id, today) else null, partners.assignedStores(p.id))
    }
}

/** Active SA/SR the TL can assign and plan for. */
private fun AppContainer.activeTeam(tlId: Long) = team.teamOf(tlId).filter { it.status == "ACTIVE" }

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
                onAttendance = { navigate(R.id.evidence, bundleOf(ARG_EMPLOYEE to id)) },
            )
        }
    }
}

/** Spec: TL-03 Assign SA/SR (per toko, revision 2026-10-08). */
class AssignFragment : ComposeFragment() {

    private sealed interface Pending {
        data class Assign(val item: Pair<Partner, Store>) : Pending
        data class Unassign(val item: Pair<Partner, Store>) : Pending
        data class Error(val error: AssignError, val holder: Long? = null) : Pending
    }

    @Composable
    override fun ScreenContent() {
        val tl = session ?: return
        val tlId = tl.user.id
        val partners by container.partners.partners.collectAsState()
        val log by container.partners.assignments.collectAsState()
        val people = remember { container.activeTeam(tlId) }
        var selectedId by rememberSaveable { mutableLongStateOf(arguments?.getLong(ARG_EMPLOYEE, NONE) ?: NONE) }
        var pending by remember { mutableStateOf<Pending?>(null) }
        var toast by remember { mutableStateOf<String?>(null) }
        val person = people.firstOrNull { it.id == selectedId }
        val assigned = remember(partners, selectedId) { person?.let { container.partners.assignedStores(it.id) }.orEmpty() }
        val available = remember(partners, selectedId) {
            person?.let { p ->
                container.partners.activeStores().filter { (pt, s) -> s.tlId == tlId && pt.areaId == p.areaId && p.id !in s.assigned }
            }.orEmpty()
        }
        WithToast(toast, { toast = null }) {
            AssignScreen(
                people, person, { selectedId = it.id }, assigned, available, ::back,
                onAssign = { item ->
                    val error = person?.let { container.partners.assignmentError(tlId, it.id, item.second.id) }
                    pending = if (error == null) Pending.Assign(item) else Pending.Error(error, container.partners.holderOf(item.second.id))
                },
                onUnassign = { pending = Pending.Unassign(it) },
                history = remember(log, selectedId) { container.partners.historyOf(selectedId) },
            )
        }
        val name = person?.fullName ?: return
        when (val p = pending) {
            is Pending.Assign -> ConfirmDialog(
                stringResource(R.string.asg_confirm_assign_title), stringResource(R.string.asg_confirm_assign, name, p.item.second.name, p.item.first.name), stringResource(R.string.asg_assign),
                onConfirm = {
                    pending = null
                    if (container.team.assign(tlId, selectedId, p.item.second.id) == null) toast = getString(R.string.asg_done_assign, name, p.item.second.name)
                },
                onDismiss = { pending = null },
            )
            is Pending.Unassign -> ConfirmDialog(
                stringResource(R.string.asg_confirm_unassign_title), stringResource(R.string.asg_confirm_unassign, name, p.item.second.name), stringResource(R.string.asg_unassign),
                onConfirm = {
                    pending = null
                    container.team.unassign(selectedId, p.item.second.id, tlId)
                    toast = getString(R.string.asg_done_unassign, name, p.item.second.name)
                },
                onDismiss = { pending = null },
            )
            is Pending.Error -> AmarBottomSheet(
                onDismissRequest = { pending = null },
                title = stringResource(R.string.asg_err_title),
                // 1.5 FR-MP-002: one toko = one SA/SR; the message names who holds it.
                description = if (p.error == AssignError.Taken) {
                    stringResource(R.string.asg_err_taken, DemoDirectory.person(p.holder)?.fullName.orEmpty())
                } else {
                    stringResource(errorText(p.error))
                },
                primaryLabel = stringResource(R.string.auth_understood),
                onPrimaryClick = { pending = null },
            )
            null -> Unit
        }
    }

    private fun errorText(e: AssignError) = when (e) {
        AssignError.NotTeam -> R.string.asg_err_team
        AssignError.NotActive -> R.string.asg_err_not_active
        AssignError.NotHolder -> R.string.asg_err_not_holder
        AssignError.Taken -> R.string.asg_err_title
    }
}

/** Spec: TL-04 Jadwal Kunjungan (weekly view and days off, revision 2026-10-08). */
class ScheduleFragment : ComposeFragment() {

    @Composable
    override fun ScreenContent() {
        val tl = session ?: return
        val ops by container.operations.data.collectAsState()
        val partners by container.partners.partners.collectAsState()
        val repo = container.operations
        val people = remember { container.activeTeam(tl.user.id) }
        var selectedId by rememberSaveable { mutableLongStateOf(arguments?.getLong(ARG_EMPLOYEE, NONE) ?: NONE) }
        var weekEpoch by rememberSaveable { mutableLongStateOf(ActivityRules.weekStart(repo.today()).toEpochDay()) }
        val (incoming, clearIncoming) = incomingToast()
        val weekStart = LocalDate.ofEpochDay(weekEpoch)
        val person = people.firstOrNull { it.id == selectedId }
        val today = person?.let { repo.todayOf(it.id) } ?: repo.today()
        val week = remember(ops, selectedId, weekStart) { person?.let { repo.visitWeek(it.id, weekStart) } }
        val days = remember(ops, partners, selectedId, weekStart) {
            person?.let { p ->
                (0L..6L).map { weekStart.plusDays(it) }.map { d ->
                    WeekDay(d, repo.visitsOf(p.id, d).mapNotNull { v ->
                        val partner = partners.firstOrNull { pt -> pt.stores.any { it.id == v.storeId } }
                        visitItem(v, partner?.stores?.firstOrNull { it.id == v.storeId }, partner, repo.todayOf(p.id))
                    })
                }
            }.orEmpty()
        }
        val hasStores = remember(partners, selectedId) { person?.let { storesFor(it).isNotEmpty() } ?: false }
        WithToast(incoming, clearIncoming) {
            ScheduleScreen(
                people, person, { selectedId = it.id }, weekStart, week, days, today, hasStores, ::back,
                onPrevWeek = { weekEpoch = weekStart.minusWeeks(1).toEpochDay() },
                onNextWeek = { weekEpoch = weekStart.plusWeeks(1).toEpochDay() },
            )
        }
    }

    private fun storesFor(p: DemoDirectory.Person): List<Store> =
        DemoSeed.storesOf(container.partners.partners.value, p.id, SalesRole.valueOf(p.role))
}
