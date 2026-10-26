package com.example.and_sales_portal.ui.performance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import com.example.and_sales_portal.AppContainer
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.DemoSeed
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.device.PerfData
import com.example.and_sales_portal.data.partner.Store
import com.example.and_sales_portal.data.sales.MonthStatus
import com.example.and_sales_portal.data.sales.PortalStatus
import com.example.and_sales_portal.data.sales.S3Role
import com.example.and_sales_portal.data.sales.Scope3
import com.example.and_sales_portal.ui.account.roleLabel
import com.example.and_sales_portal.ui.attendance.AttendanceFragment
import com.example.and_sales_portal.ui.common.ComposeFragment
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.dashboard.EvidenceFragment
import com.example.and_sales_portal.ui.dashboard.ProductivityCard
import com.example.and_sales_portal.ui.dashboard.Period
import com.example.and_sales_portal.ui.dashboard.PeriodChips
import com.example.and_sales_portal.ui.team.MemberCard
import com.example.and_sales_portal.ui.team.members
import kotlinx.coroutines.delay
import java.time.YearMonth

/** Sub-tabs of the Performa tab: Pipeline, Penghasilan, then Tim (TL) or Produktivitas (SA/SR, same position). */
enum class PerformanceTab { PIPELINE, INCOME, TEAM }

private const val ARG_EMPLOYEE = "employee_id"
private const val ARG_MONTH = "month"
private const val ARG_LOAN = "loan_id"

private fun AppContainer.monthOptions(): List<Pair<YearMonth, MonthStatus>> = sales.months().map { it to sales.monthStatus(it) }

private fun AppContainer.storesOf(id: Long, role: SalesRole): List<Store> = DemoSeed.storesOf(partners.partners.value, id, role)

private fun areaName(id: Int): String = DemoDirectory.area(id)?.name.orEmpty()

/** Shared parts of the Performa screens: sub-tab, Pipeline state, Penghasilan state. */
abstract class PerformanceBase : ComposeFragment() {

    /** Sub-tab to open: a one-shot request from Beranda, else the remembered one. */
    @Composable
    protected fun rememberSubTab(): Pair<Int, (Int) -> Unit> {
        var tab by rememberSaveable { mutableIntStateOf(0) }
        container.pendingPerformanceTab?.let { tab = it; container.pendingPerformanceTab = null }
        return tab to { t: Int -> tab = t }
    }

    @Composable
    protected fun androidx.compose.foundation.layout.ColumnScope.PipelineContent(viewerId: Long, role: SalesRole, refreshKey: Int) {
        val partners by container.partners.partners.collectAsState()
        val sim by container.simulation.state.collectAsState()
        var month by rememberSaveable { mutableStateOf(container.sales.currentMonth().toString()) }
        var status by rememberSaveable { mutableStateOf<String?>(null) }
        var storeId by rememberSaveable { mutableStateOf<String?>(null) }
        var salesFilter by rememberSaveable { mutableStateOf<Long?>(null) }
        val ym = YearMonth.parse(month)
        val tl = role == SalesRole.TL
        val stores = remember(partners) { container.storesOf(viewerId, role) }
        val team = remember(partners) { if (tl) container.team.teamOf(viewerId) else null }
        val pipeline = remember(partners, sim, month, status, storeId, salesFilter, refreshKey) {
            container.sales.pipeline(
                ym, salesId = if (tl) null else viewerId, tlId = if (tl) viewerId else null,
                status = status?.let { PortalStatus.valueOf(it) }, storeId = storeId, salesFilter = salesFilter,
            )
        }
        when (sim.perfData) {
            PerfData.LOADING -> PerfLoading()
            PerfData.ERROR -> PerfError(::retry)
            PerfData.NORMAL, PerfData.LMT_ERROR -> {
                if (sim.perfData == PerfData.LMT_ERROR) LmtBanner(pipeline.asOf)
                PipelineSection(
                PipelineState(ym, container.monthOptions(), pipeline, status?.let { PortalStatus.valueOf(it) }, stores, storeId, team, salesFilter),
                PipelineActions(
                    onMonth = { month = it.toString() },
                    onStatus = { status = it?.name },
                    onStore = { storeId = it },
                    onSales = { salesFilter = it },
                    onOpen = { navigate(R.id.loan_detail, bundleOf(ARG_LOAN to it.id, ARG_MONTH to month)) },
                ),
            )
            }
        }
    }

    @Composable
    protected fun androidx.compose.foundation.layout.ColumnScope.IncomeContent(employeeId: Long, readOnlyMember: Boolean) {
        val ops by container.operations.data.collectAsState()
        val partners by container.partners.partners.collectAsState()
        val sim by container.simulation.state.collectAsState()
        var month by rememberSaveable { mutableStateOf(container.sales.currentMonth().toString()) }
        val ym = YearMonth.parse(month)
        val row = remember(ops, partners, sim, month) { container.sales.salesRow(employeeId, ym) }
        val history = remember(ops, partners, sim) { container.sales.history(employeeId) }
        val person = DemoDirectory.person(employeeId)
        IncomeSection(
            IncomeState(sim.perfData, ym, container.monthOptions(), row, areaName(row?.areaId ?: person?.areaId ?: 0), history, container.sales.asOf()),
            IncomeActions(
                onMonth = { month = it.toString() },
                onAttendance = { m ->
                    if (readOnlyMember) navigate(R.id.evidence, EvidenceFragment.args(employeeId, m)) else navigate(R.id.attendance, AttendanceFragment.args(m))
                },
                onMonthDetail = { m -> navigate(R.id.income_month, IncomeMonthFragment.args(employeeId, m)) },
                onRetry = ::retry,
            ),
        )
    }

    /** "Coba lagi": the Simulasi failure is cleared, as a successful retry. */
    protected fun retry() = container.simulation.update { it.copy(perfData = PerfData.NORMAL) }
}

/** Pull to refresh of the Pipeline: a new fetch ("Data per" moves on). */
@Composable
private fun rememberRefresh(): Triple<Boolean, () -> Unit, Int> {
    var refreshing by remember { mutableStateOf(false) }
    var key by remember { mutableIntStateOf(0) }
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(REFRESH_MS)
            key++
            refreshing = false
        }
    }
    return Triple(refreshing, { refreshing = true }, key)
}

private const val REFRESH_MS = 600L

/** Spec: PRF-02, PRF-04 (SA/SR tab Performa: Pipeline, Penghasilan, Produktivitas; replaces SLS-02 … SLS-04). */
class SalesPerformanceTabFragment : PerformanceBase() {
    @Composable
    override fun ScreenContent() {
        val s = session ?: return
        val (tab, onTab) = rememberSubTab()
        val (refreshing, onRefresh, key) = rememberRefresh()
        PerformanceScaffold(
            stringResource(R.string.nav_performa), null, listOf(R.string.s3_tab_pipeline, R.string.s3_tab_income, R.string.s3_tab_productivity), tab, onTab,
            refreshing = refreshing, onRefresh = if (tab == PerformanceTab.PIPELINE.ordinal) onRefresh else null,
        ) {
            when (tab) {
                PerformanceTab.PIPELINE.ordinal -> PipelineContent(s.user.id, s.role, key)
                PerformanceTab.INCOME.ordinal -> IncomeContent(s.user.id, readOnlyMember = false)
                else -> {
                    val ops by container.operations.data.collectAsState()
                    var period by rememberSaveable { mutableStateOf(Period.MONTH) }
                    val today = container.operations.today()
                    val productivity = remember(ops, period) { container.team.productivity(s.user.id, period.range(today)) }
                    PeriodChips(listOf(Period.MONTH, Period.LAST_MONTH, Period.WEEK, Period.TODAY), period) { period = it }
                    ProductivityCard(stringResource(R.string.s3_tab_productivity), productivity)
                }
            }
        }
    }
}

/** Spec: PRF-02, PRF-04, PRF-05 (TL tab Performa: Pipeline, Penghasilan, Tim; replaces the Tim tab and TL-08 … TL-10). */
class TlPerformanceTabFragment : PerformanceBase() {
    @Composable
    override fun ScreenContent() {
        val s = session ?: return
        val tlId = s.user.id
        val (tab, onTab) = rememberSubTab()
        val (refreshing, onRefresh, key) = rememberRefresh()
        PerformanceScaffold(
            stringResource(R.string.nav_performa), null, listOf(R.string.s3_tab_pipeline, R.string.s3_tab_income, R.string.s3_tab_team), tab, onTab,
            refreshing = refreshing, onRefresh = if (tab == PerformanceTab.PIPELINE.ordinal) onRefresh else null,
        ) {
            when (tab) {
                PerformanceTab.PIPELINE.ordinal -> PipelineContent(tlId, SalesRole.TL, key)
                PerformanceTab.INCOME.ordinal -> IncomeContent(tlId, readOnlyMember = false)
                else -> TeamContent(tlId)
            }
        }
    }

    @Composable
    private fun androidx.compose.foundation.layout.ColumnScope.TeamContent(tlId: Long) {
        val ops by container.operations.data.collectAsState()
        val partners by container.partners.partners.collectAsState()
        val sim by container.simulation.state.collectAsState()
        var month by rememberSaveable { mutableStateOf(container.sales.currentMonth().toString()) }
        val ym = YearMonth.parse(month)
        val state = remember(ops, partners, sim, month) {
            val sales = container.sales
            val rows = sales.rowsFor(tlId, SalesRole.TL, ym)
            val members = container.members(tlId)
            val pipeline = sales.pipeline(ym, tlId = tlId)
            val stores = partners.filter { p -> p.stores.any { it.tlId == tlId } && p.status.name == "ACTIVE" }.flatMap { p ->
                p.stores.filter { it.tlId == tlId && it.status == "ACTIVE" }.map { st ->
                    val counts = PortalStatus.entries.associateWith { status -> pipeline.rows.count { it.loan.storeId == st.id && it.status == status } }
                    StoreFunnel(st, p.name, counts, st.assigned.firstOrNull()?.let { DemoDirectory.person(it)?.fullName })
                }
            }
            TeamState(
                sim.perfData, ym, container.monthOptions(), rows, sales.totals(rows),
                rows.firstOrNull { it.userId == tlId }?.calc?.actual ?: 0L,
                members.associate { it.person.id to it.today }, stores, container.team.teamOf(tlId).size, sales.asOf(),
            ) to members.filter { m -> rows.none { it.userId == m.person.id } }
        }
        val (team, pending) = state
        val you = stringResource(R.string.s3_you, "%s")
        TeamSection(
            team,
            TeamActions(
                onMonth = { month = it.toString() },
                onOpenIncome = { navigate(R.id.member_income, bundleOf(ARG_EMPLOYEE to it.userId)) },
                onMember = { navigate(R.id.team_detail, bundleOf(ARG_EMPLOYEE to it)) },
                onRetry = ::retry,
            ),
            youLabel = { you.format(it) },
        ) {
            pending.forEach { m -> MemberCard(m) { navigate(R.id.team_detail, bundleOf(ARG_EMPLOYEE to it.person.id)) } }
        }
    }
}

/** Spec: PRF-04 Penghasilan of an SA/SR opened by their TL (read-only, header name and role). */
class MemberIncomeFragment : PerformanceBase() {
    @Composable
    override fun ScreenContent() {
        val tl = session ?: return
        val id = requireArguments().getLong(ARG_EMPLOYEE)
        val person = DemoDirectory.person(id) ?: return
        // FR-IN-006: only the TL of this SA/SR; never peers.
        if (!container.sales.canView(tl.user.id, id)) return
        ScreenScaffold(person.fullName, ::back) {
            val role = runCatching { SalesRole.valueOf(person.role) }.getOrNull()
            role?.let {
                androidx.compose.material3.Text(
                    stringResource(R.string.s3_member_subtitle, stringResource(roleLabel(it)), areaName(person.areaId)),
                    color = com.example.and_sales_portal.designsystem.theme.AmarTheme.colors.content.secondary,
                    style = com.example.and_sales_portal.designsystem.theme.AmarTheme.typography.bodySm,
                )
            }
            IncomeContent(id, readOnlyMember = true)
        }
    }
}

/** Spec: PRF-04 detail bulan: blocks of the month and its loans (counted and struck through). Opened from a notification too. */
class IncomeMonthFragment : PerformanceBase() {
    @Composable
    override fun ScreenContent() {
        val viewer = session ?: return
        val id = requireArguments().getLong(ARG_EMPLOYEE)
        val ym = YearMonth.parse(requireArguments().getString(ARG_MONTH))
        if (!container.sales.canView(viewer.user.id, id)) return
        val ops by container.operations.data.collectAsState()
        val partners by container.partners.partners.collectAsState()
        val sim by container.simulation.state.collectAsState()
        val row = remember(ops, partners, sim) { container.sales.salesRow(id, ym) }
        val loans = remember(ops, partners, sim) {
            val tl = row?.role == S3Role.TL
            container.sales.pipeline(ym, salesId = if (tl) null else id, tlId = if (tl) id else null).rows.filter { it.counted || it.paidLater }
        }
        val person = DemoDirectory.person(id)
        val subtitle = if (id != viewer.user.id && person != null) {
            runCatching { SalesRole.valueOf(person.role) }.getOrNull()?.let { stringResource(R.string.s3_member_subtitle, "${person.fullName} · ${stringResource(roleLabel(it))}", areaName(person.areaId)) }
        } else null
        IncomeMonthScreen(
            row, ym, subtitle, areaName(row?.areaId ?: person?.areaId ?: 0), loans,
            onAttendance = { if (id == viewer.user.id) navigate(R.id.attendance, AttendanceFragment.args(ym)) else navigate(R.id.evidence, EvidenceFragment.args(id, ym)) },
            onBack = ::back,
        )
    }

    companion object {
        fun args(employeeId: Long, month: YearMonth) = bundleOf(ARG_EMPLOYEE to employeeId, ARG_MONTH to month.toString())
    }
}

/** Spec: PRF-03 Detail Pinjaman (SCR-3-03), read-only. */
class LoanDetailFragment : PerformanceBase() {
    @Composable
    override fun ScreenContent() {
        val viewer = session ?: return
        val partners by container.partners.partners.collectAsState()
        val id = requireArguments().getString(ARG_LOAN) ?: return
        val loan = remember(partners) { container.sales.loan(id) } ?: return
        // Visibility: the SA/SR of the loan or the TL of its toko.
        if (loan.salesId != viewer.user.id && loan.tlId != viewer.user.id) return
        val ym = requireArguments().getString(ARG_MONTH)?.let { YearMonth.parse(it) } ?: YearMonth.from(com.example.and_sales_portal.data.sales.SalesRepository.dateOf(loan.submittedAt))
        val st = Scope3.portalStatus(loan)
        val counted = Scope3.countsFor(loan, ym)
        val store = partners.flatMap { it.stores }.firstOrNull { it.id == loan.storeId }
        LoanDetailScreen(
            com.example.and_sales_portal.data.sales.PipelineLoan(loan, st, counted, st == PortalStatus.CAIR && !counted),
            store?.name, DemoDirectory.person(loan.salesId)?.fullName, DemoDirectory.person(loan.tlId)?.fullName, ::back,
        )
    }
}
