package com.example.and_sales_portal.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.os.bundleOf
import com.example.and_sales_portal.AppContainer
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.DemoSeed
import com.example.and_sales_portal.data.activity.Visit
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.partner.Store
import com.example.and_sales_portal.data.device.PerfData
import com.example.and_sales_portal.ui.performance.PerformanceCardState
import com.example.and_sales_portal.ui.performance.performanceCardState
import com.example.and_sales_portal.ui.performance.PerformanceTab
import com.example.and_sales_portal.ui.attendance.AttendanceFragment
import com.example.and_sales_portal.ui.attendance.CaptureFragment
import com.example.and_sales_portal.ui.attendance.CaptureMode
import com.example.and_sales_portal.ui.common.ComposeFragment
import com.example.and_sales_portal.ui.common.WithToast
import com.example.and_sales_portal.ui.shell.SalesTab
import com.example.and_sales_portal.ui.shell.ShellNavigator
import com.example.and_sales_portal.ui.visits.VisitItem
import com.example.and_sales_portal.ui.visits.openMaps
import com.example.and_sales_portal.ui.visits.visitItem
import java.time.LocalDate

private const val ARG_EMPLOYEE = "employee_id"
private const val ARG_MONTH = "month"

private fun AppContainer.item(v: Visit): VisitItem? {
    val partner = partners.partners.value.firstOrNull { p -> p.stores.any { it.id == v.storeId } }
    return visitItem(v, partner?.stores?.firstOrNull { it.id == v.storeId }, partner, operations.todayOf(v.employeeId))
}

private fun AppContainer.storesOf(id: Long, role: SalesRole): List<Store> = DemoSeed.storesOf(partners.partners.value, id, role)

/** PRF-01 Beranda card "Performa bulan ini" (PRD Scope 3 SCR-3-01). */
internal fun AppContainer.performanceCard(id: Long, role: SalesRole, load: PerfData): PerformanceCardState =
    performanceCardState(sales, id, role, storesOf(id, role).isNotEmpty(), load)

/** Beranda base: Absensi actions shared by TL and SA/SR. */
abstract class HomeBase : ComposeFragment() {
    protected fun attendanceActions() = AttendanceActions(
        onClockIn = { navigate(R.id.capture, CaptureFragment.args(CaptureMode.CLOCK_IN)) },
        onClockOut = { navigate(R.id.capture, CaptureFragment.args(CaptureMode.CLOCK_OUT)) },
        onHistory = { navigate(R.id.attendance) },
    )

    protected fun toTab(tab: SalesTab) = ShellNavigator(this).toTab(tab, SalesTab.home(requireNotNull(session).role))

    /** Opens the Performa tab on [sub] (Beranda "Lihat detail", TL "Lihat tim"). */
    protected fun openPerformance(tab: SalesTab, sub: PerformanceTab) {
        container.pendingPerformanceTab = sub.ordinal
        toTab(tab)
    }

    /** "Coba lagi" of the Performa card: the Simulasi failure is cleared, as a successful retry. */
    protected fun retryPerformance() = container.simulation.update { it.copy(perfData = PerfData.NORMAL) }
}

/** Spec: SLS-01 Beranda SA/SR. */
class SalesHomeFragment : HomeBase() {
    @Composable
    override fun ScreenContent() {
        val s = session ?: return
        val ops by container.operations.data.collectAsState()
        val partners by container.partners.partners.collectAsState()
        val notes by container.partners.assignments.collectAsState()
        val online by container.connectivity.online.collectAsState()
        val sim by container.simulation.state.collectAsState()
        val (toast, clearToast) = incomingToast()
        val repo = activityRepo
        val state = remember(ops, partners, notes, online, sim) {
            val today = repo.today()
            container.sales.syncIncomeNotices(s.user.id)
            SalesHomeState(
                firstName = s.user.firstName,
                role = s.role,
                date = today,
                attendance = AttendanceFragment.attendanceState(repo, online).today,
                online = online,
                week = repo.visitWeek(today),
                visitedToday = repo.visitsOn(today).isNotEmpty(),
                groups = repo.visitPartners(today),
                unread = container.partners.notificationsOf(s.user.id).filter { !it.read },
                partners = container.partners.myPartners(s.user.id, s.role),
                performance = container.performanceCard(s.user.id, s.role, sim.perfData),
            )
        }
        WithToast(toast, clearToast) {
            SalesHomeScreen(
                state, attendanceActions(),
                onVisits = { toTab(SalesTab.Visits) },
                onNotifications = { navigate(R.id.notifications) },
                onPartners = { toTab(SalesTab.Partner) },
                onPerformance = { openPerformance(SalesTab.Performance, PerformanceTab.INCOME) },
                onRetry = ::retryPerformance,
            )
        }
    }
}

/** Spec: TL-06 Beranda TL. */
class TlHomeFragment : HomeBase() {
    @Composable
    override fun ScreenContent() {
        val s = session ?: return
        val tlId = s.user.id
        val ops by container.operations.data.collectAsState()
        val partners by container.partners.partners.collectAsState()
        val notes by container.partners.assignments.collectAsState()
        val online by container.connectivity.online.collectAsState()
        val sim by container.simulation.state.collectAsState()
        val (toast, clearToast) = incomingToast()
        val repo = activityRepo
        val state = remember(ops, partners, notes, online, sim) {
            val today = repo.today()
            container.sales.syncIncomeNotices(tlId)
            val o = container.operations
            val team = container.team.teamOf(tlId).filter { it.status == "ACTIVE" }
            val statuses = team.map { o.attendanceStatus(it.id, today) }
            val visitedToday = team.count { o.visitsOf(it.id, o.todayOf(it.id)).isNotEmpty() }
            // Team contribution (Figma TL Dashboard): each SA/SR's Cair dihitung over the TL's Cair dihitung this month.
            val month = container.sales.currentMonth()
            val teamCounted = container.sales.salesRow(tlId, month)?.calc?.actual ?: 0L
            TlHomeState(
                firstName = s.user.firstName,
                date = today,
                attendance = AttendanceFragment.attendanceState(repo, online).today,
                online = online,
                myWeek = if (repo.stores().isEmpty()) null else repo.visitWeek(today),
                teamPresent = statuses.count { it == AttendanceStatus.ON_TIME || it == AttendanceStatus.LATE },
                teamNotYet = statuses.count { it == AttendanceStatus.NOT_YET },
                teamAbsent = statuses.count { it == AttendanceStatus.ABSENT },
                teamVisitedToday = visitedToday,
                teamNotVisitedToday = team.size - visitedToday,
                teamCompleteWeek = team.count { o.visitWeek(it.id, ActivityRules.weekStart(today)).complete },
                teamSize = team.size,
                submissions = container.partners.submissionsFor(tlId, SalesRole.TL).groupingBy { it.status }.eachCount(),
                performance = container.performanceCard(tlId, SalesRole.TL, sim.perfData),
                contribution = team.map { p ->
                    val counted = container.sales.salesRow(p.id, month)?.calc?.actual ?: 0L
                    p.fullName to if (teamCounted == 0L) 0.0 else Math.round(counted * 1000.0 / teamCounted) / 10.0
                }.sortedByDescending { it.second },
                unread = container.partners.notificationsOf(tlId).filter { !it.read },
            )
        }
        WithToast(toast, clearToast) {
            TlHomeScreen(
                state, attendanceActions(),
                TlHomeActions(
                    onMyVisits = { navigate(R.id.my_visits) },
                    onTeam = { openPerformance(SalesTab.TlPerformance, PerformanceTab.TEAM) },
                    onSubmissions = { toTab(SalesTab.Partner) },
                    onRegister = { navigate(R.id.nav_partner_registration) },
                    onSchedule = { navigate(R.id.schedule) },
                    onAssign = { navigate(R.id.assign) },
                    onPerformance = { openPerformance(SalesTab.TlPerformance, PerformanceTab.INCOME) },
                    onRetry = ::retryPerformance,
                    onNotifications = { navigate(R.id.notifications) },
                ),
            )
        }
    }
}

/** Spec: TL-07 Produktivitas Tim. */
class TeamProductivityFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val s = session ?: return
        val ops by container.operations.data.collectAsState()
        var period by rememberSaveable { mutableStateOf(Period.MONTH) }
        val today = container.operations.today()
        val rows = remember(ops, period) {
            container.team.teamOf(s.user.id).filter { it.status == "ACTIVE" }.map { it to container.team.productivity(it.id, period.range(today)) }
        }
        TeamProductivityScreen(period, { period = it }, rows, ::back) { navigate(R.id.evidence, bundleOf(ARG_EMPLOYEE to it.id)) }
    }
}

/** Spec: TL-07 Bukti absensi & kunjungan of one SA/SR. */
class EvidenceFragment : ComposeFragment() {
    companion object {
        /** Evidence of [employeeId], or of one [month] (PRF-04 "Lihat absensi"). */
        fun args(employeeId: Long, month: java.time.YearMonth? = null) = bundleOf(ARG_EMPLOYEE to employeeId, ARG_MONTH to month?.toString())
    }

    @Composable
    override fun ScreenContent() {
        val id = requireArguments().getLong(ARG_EMPLOYEE)
        val person = DemoDirectory.person(id) ?: return
        val ops by container.operations.data.collectAsState()
        var period by rememberSaveable { mutableStateOf(Period.WEEK) }
        // PRF-04 "Lihat absensi" of an SA/SR: the days of one month (PRD Scope 3 FR-DF-003).
        val month = requireArguments().getString(ARG_MONTH)?.let { java.time.YearMonth.parse(it) }
        val o = container.operations
        val today = o.today()
        val days = remember(ops, period) {
            val r = month?.let { it.atDay(1)..minOf(it.atEndOfMonth(), today) } ?: period.range(today)
            generateSequence(r.endInclusive) { it.minusDays(1) }.takeWhile { !it.isBefore(r.start) }.map { d ->
                val a = o.attendanceOf(id, d)
                EvidenceDay(d, o.attendanceStatus(id, d), a, o.visitsOf(id, d).mapNotNull { container.item(it) }, ActivityRules.noCheckOut(a, d, o.todayOf(id)), o.canCorrect(id, d))
            }.toList()
        }
        // TL-11 (decision 2026-10-19): the TL corrects a subordinate's day from here.
        val leads = person.supervisorId == session?.user?.id
        EvidenceScreen(
            person.fullName, period.takeIf { month == null }, { period = it }, days, ::back, { lat, lng -> openMaps(lat, lng) },
            subtitle = month?.let { androidx.compose.ui.res.stringResource(R.string.s3_attendance_month, com.example.and_sales_portal.ui.common.Fmt.month(it)) },
            onCorrect = if (leads) { d -> navigate(R.id.attendance_correction, com.example.and_sales_portal.ui.team.AttendanceCorrectionFragment.args(id, d)) } else null,
        )
    }
}
