package com.example.and_sales_portal.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.and_sales_portal.data.sales.Loan
import com.example.and_sales_portal.data.sales.LoanStatus
import com.example.and_sales_portal.data.sales.SalesRepository
import com.example.and_sales_portal.data.sales.SalesSummary
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

private fun AppContainer.item(v: Visit): VisitItem? {
    val partner = partners.partners.value.firstOrNull { p -> p.stores.any { it.id == v.storeId } }
    return visitItem(v, partner?.stores?.firstOrNull { it.id == v.storeId }, partner)
}

private fun AppContainer.storesOf(id: Long, role: SalesRole): List<Store> = DemoSeed.storesOf(partners.partners.value, id, role)

private fun summary(loans: List<Loan>, range: ClosedRange<LocalDate>) = SalesSummary.of(loans, range.start, range.endInclusive)

private fun filterLoans(loans: List<Loan>, status: LoanStatus?, store: Store?, range: ClosedRange<LocalDate>) = loans.filter {
    (status == null || it.status == status) && (store == null || it.storeId == store.id) && SalesRepository.dateOf(it.submittedAt) in range
}

/** Beranda base: Absensi actions shared by TL and SA/SR. */
abstract class HomeBase : ComposeFragment() {
    protected fun attendanceActions() = AttendanceActions(
        onClockIn = { navigate(R.id.capture, CaptureFragment.args(CaptureMode.CLOCK_IN)) },
        onClockOut = { navigate(R.id.capture, CaptureFragment.args(CaptureMode.CLOCK_OUT)) },
        onHistory = { navigate(R.id.attendance) },
    )

    protected fun toTab(tab: SalesTab) = ShellNavigator(this).toTab(tab, SalesTab.home(requireNotNull(session).role))
}

/** Spec: SLS-01 Beranda SA/SR. */
class SalesHomeFragment : HomeBase() {
    @Composable
    override fun ScreenContent() {
        val s = session ?: return
        val ops by container.operations.data.collectAsState()
        val partners by container.partners.partners.collectAsState()
        val online by container.connectivity.online.collectAsState()
        val (toast, clearToast) = incomingToast()
        val repo = activityRepo
        val state = remember(ops, partners, online) {
            val today = repo.today()
            SalesHomeState(
                firstName = s.user.firstName,
                role = s.role,
                date = today,
                attendance = AttendanceFragment.attendanceState(repo, online).today,
                online = online,
                week = repo.visitWeek(today),
                visitedToday = repo.visitsOn(today).isNotEmpty(),
                dayOff = repo.isDayOff(today),
                stores = repo.stores(),
                partners = container.partners.myPartners(s.user.id, s.role),
                month = summary(container.sales.loansOfSales(s.user.id), Period.MONTH.range(today)),
                incentive = container.sales.salesIncentive(s.user.id, s.role),
            )
        }
        WithToast(toast, clearToast) {
            SalesHomeScreen(
                state, attendanceActions(),
                onVisits = { toTab(SalesTab.Visits) },
                onPartners = { toTab(SalesTab.Partner) },
                onIncentive = { toTab(SalesTab.Performance) },
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
        val online by container.connectivity.online.collectAsState()
        val (toast, clearToast) = incomingToast()
        val repo = activityRepo
        val state = remember(ops, partners, online) {
            val today = repo.today()
            val o = container.operations
            val team = container.team.teamOf(tlId).filter { it.status == "ACTIVE" }
            val statuses = team.map { o.attendanceStatus(it.id, today) }
            val visitedToday = team.count { o.visitsOf(it.id, o.todayOf(it.id)).isNotEmpty() }
            val month = Period.MONTH.range(today)
            val teamLoans = container.sales.loansOfTl(tlId)
            val teamPaid = summary(teamLoans, month).paidOutAmount
            TlHomeState(
                firstName = s.user.firstName,
                date = today,
                attendance = AttendanceFragment.attendanceState(repo, online).today,
                online = online,
                myWeek = if (repo.stores().isEmpty()) null else repo.visitWeek(today),
                teamPresent = statuses.count { it == AttendanceStatus.ON_TIME || it == AttendanceStatus.LATE },
                teamNotYet = statuses.count { it == AttendanceStatus.NOT_YET },
                teamAbsent = statuses.count { it == AttendanceStatus.ABSENT || it == AttendanceStatus.DAY_OFF },
                teamVisitedToday = visitedToday,
                teamNotVisitedToday = team.size - visitedToday,
                teamCompleteWeek = team.count { o.visitWeek(it.id, ActivityRules.weekStart(today)).complete },
                teamSize = team.size,
                submissions = container.partners.submissionsFor(tlId, SalesRole.TL).groupingBy { it.status }.eachCount(),
                teamMonth = summary(teamLoans, month),
                incentive = container.sales.tlIncentive(tlId),
                contribution = team.map { p ->
                    val paid = summary(container.sales.loansOfSales(p.id), month).paidOutAmount
                    p.fullName to if (teamPaid == 0L) 0.0 else Math.round(paid * 1000.0 / teamPaid) / 10.0
                }.sortedByDescending { it.second },
            )
        }
        WithToast(toast, clearToast) {
            TlHomeScreen(
                state, attendanceActions(),
                TlHomeActions(
                    onMyVisits = { navigate(R.id.my_visits) },
                    onTeam = { toTab(SalesTab.Team) },
                    onSubmissions = { toTab(SalesTab.Partner) },
                    onRegister = { navigate(R.id.nav_partner_registration) },
                    onSchedule = { navigate(R.id.schedule) },
                    onAssign = { navigate(R.id.assign) },
                    onIncentive = { navigate(R.id.team_incentive) },
                ),
            )
        }
    }
}

/** Spec: SLS-02 … SLS-04 (SA/SR tab Kinerja). */
class PerformanceTabFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val s = session ?: return
        val ops by container.operations.data.collectAsState()
        val partners by container.partners.partners.collectAsState()
        var tab by rememberSaveable { mutableIntStateOf(0) }
        var period by rememberSaveable { mutableStateOf(Period.MONTH) }
        var loanPeriod by rememberSaveable { mutableStateOf(Period.MONTH) }
        var status by rememberSaveable { mutableStateOf<LoanStatus?>(null) }
        var storeId by rememberSaveable { mutableStateOf<String?>(null) }
        val today = container.operations.today()
        val loans = remember(partners) { container.sales.loansOfSales(s.user.id) }
        val stores = remember(partners) { container.storesOf(s.user.id, s.role) }
        val range = period.range(today)
        val perStore = remember(loans, period) { stores.map { st -> StoreSales(st, summary(loans.filter { it.storeId == st.id }, range)) } }
        val productivity = remember(ops, period) { container.team.productivity(s.user.id, range) }
        val incentive = remember(ops, partners) { container.sales.salesIncentive(s.user.id, s.role) }
        val store = stores.firstOrNull { it.id == storeId }
        PerformanceTabScreen(
            tab, { tab = it }, period, { period = it }, summary(loans, range), perStore, productivity,
            LoanSectionState(filterLoans(loans, status, store, loanPeriod.range(today)), stores, status, store, loanPeriod, { status = it }, { storeId = it?.id }, { loanPeriod = it }),
            incentive,
        )
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
    @Composable
    override fun ScreenContent() {
        val id = requireArguments().getLong(ARG_EMPLOYEE)
        val person = DemoDirectory.person(id) ?: return
        val ops by container.operations.data.collectAsState()
        var period by rememberSaveable { mutableStateOf(Period.WEEK) }
        val o = container.operations
        val today = o.today()
        val days = remember(ops, period) {
            val r = period.range(today)
            generateSequence(r.endInclusive) { it.minusDays(1) }.takeWhile { !it.isBefore(r.start) }.filter(ActivityRules::isWorkday).map { d ->
                val a = o.attendanceOf(id, d)
                EvidenceDay(d, o.attendanceStatus(id, d), a, o.visitsOf(id, d).mapNotNull { container.item(it) })
            }.toList()
        }
        EvidenceScreen(person.fullName, period, { period = it }, days, ::back) { lat, lng -> openMaps(lat, lng) }
    }
}

/** Spec: TL-08 Kinerja Penjualan. */
class TeamSalesFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val s = session ?: return
        val tlId = s.user.id
        val partners by container.partners.partners.collectAsState()
        var tab by rememberSaveable { mutableIntStateOf(0) }
        var period by rememberSaveable { mutableStateOf(Period.MONTH) }
        var memberId by rememberSaveable { mutableStateOf<Long?>(null) }
        var partnerId by rememberSaveable { mutableStateOf<String?>(null) }
        val today = container.operations.today()
        val range = period.range(today)
        val loans = remember(partners) { container.sales.loansOfTl(tlId) }
        val team = remember { container.team.teamOf(tlId).filter { it.status == "ACTIVE" } }
        val owned = remember(partners) { partners.filter { it.ownerTlId == tlId && it.status.name == "ACTIVE" } }
        val members = remember(loans, period) { team.map { p -> p to summary(loans.filter { it.salesId == p.id }, range) } }
        val perPartner = remember(loans, period, owned) {
            owned.map { p -> p to p.stores.map { st -> StoreSales(st, summary(loans.filter { it.storeId == st.id }, range)) } }
        }
        TeamSalesScreen(
            period, { period = it }, tab, { tab = it }, summary(loans, range), members, perPartner,
            team.firstOrNull { it.id == memberId }, team, { memberId = it?.id },
            owned.firstOrNull { it.id == partnerId }, owned, { partnerId = it?.id },
            ::back,
        )
    }
}

/** Spec: TL-09 Pinjaman. */
class TeamLoansFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val s = session ?: return
        val partners by container.partners.partners.collectAsState()
        var period by rememberSaveable { mutableStateOf(Period.MONTH) }
        var status by rememberSaveable { mutableStateOf<LoanStatus?>(null) }
        var storeId by rememberSaveable { mutableStateOf<String?>(null) }
        val today = container.operations.today()
        val loans = remember(partners) { container.sales.loansOfTl(s.user.id) }
        val stores = remember(partners) { container.storesOf(s.user.id, SalesRole.TL) }
        val store = stores.firstOrNull { it.id == storeId }
        TeamLoansScreen(
            LoanSectionState(filterLoans(loans, status, store, period.range(today)), stores, status, store, period, { status = it }, { storeId = it?.id }, { period = it }),
            ::back,
        )
    }
}

/** Spec: TL-10 Insentif. */
class TeamIncentiveFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val s = session ?: return
        val tlId = s.user.id
        val ops by container.operations.data.collectAsState()
        val partners by container.partners.partners.collectAsState()
        var tab by rememberSaveable { mutableIntStateOf(0) }
        val sales = container.sales
        val mine = remember(ops, partners) { sales.tlIncentive(tlId) }
        val team = remember(ops, partners) {
            container.team.teamOf(tlId).filter { it.status == "ACTIVE" }.map { it to sales.salesIncentive(it.id, SalesRole.valueOf(it.role)) }
        }
        val own = remember(partners) { partners.filter { it.ownerTlId == tlId && it.status.name == "ACTIVE" }.map { it to sales.partnerIncentive(it) } }
        TeamIncentiveScreen(tab, { tab = it }, mine, team, own, ::back)
    }
}
