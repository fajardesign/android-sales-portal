package com.example.and_sales_portal.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.DemoSeed
import com.example.and_sales_portal.data.activity.OperationsRepository
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.data.partner.PartnerStatus
import com.example.and_sales_portal.data.sales.SalesRepository
import com.example.and_sales_portal.data.sales.SalesSummary
import com.example.and_sales_portal.data.team.TeamService
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.attendance.AttendanceDay
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.LocalTime

/** Beranda TL / SA-SR, Kinerja, Pinjaman, Insentif and Produktivitas (TL-06 … TL-10, SLS-01 … SLS-04). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h760dp-mdpi")
class DashboardScreensScreenshotTest {

    private fun capture(name: String, content: @Composable () -> Unit) =
        captureRoboImage("build/outputs/roborazzi/dash_$name.png") { CompositionLocalProvider(LocalInspectionMode provides true) { AmarTheme { content() } } }

    private val today = LocalDate.of(2026, 10, 7)
    private val now = today.atTime(LocalTime.of(10, 30)).atZone(ActivityRules.ZONE).toInstant()
    private lateinit var partners: PartnerRepository
    private lateinit var ops: OperationsRepository
    private lateinit var sales: SalesRepository
    private lateinit var team: TeamService
    private val noop = AttendanceActions({}, {}, {})

    @Before
    fun setUp() {
        val store = DemoStore(ApplicationProvider.getApplicationContext())
        partners = PartnerRepository(store) { now }.also { it.reset() }
        ops = OperationsRepository(store, partners) { now }.also { it.reset() }
        sales = SalesRepository(partners, ops) { now }
        team = TeamService(partners, ops)
    }

    private fun summary(id: Long) = SalesSummary.of(sales.loansOfSales(id), today.withDayOfMonth(1), today)

    @Test
    fun homes() {
        val week = ops.visitWeek(11, ActivityRules.weekStart(today))
        val stores = DemoSeed.storesOf(partners.partners.value, 11, SalesRole.SR)
        val sr = SalesHomeState("Siti", SalesRole.SR, today, AttendanceDay(today, null, AttendanceStatus.NOT_YET), true, week, false, false, stores, partners.myPartners(11, SalesRole.SR), summary(11), sales.salesIncentive(11, SalesRole.SR))
        capture("sls_01_home") { SalesHomeScreen(sr, noop, {}, {}, {}) }
        val tl = TlHomeState(
            "Andi", today, AttendanceDay(today, null, AttendanceStatus.NOT_YET), true, ops.visitWeek(6, ActivityRules.weekStart(today)), 2, 1, 0, 1, 2, 0, 3,
            partners.submissionsFor(6, SalesRole.TL).groupingBy { it.status }.eachCount() + (PartnerStatus.REVISION_REQUIRED to 1),
            SalesSummary.of(sales.loansOfTl(6), today.withDayOfMonth(1), today), sales.tlIncentive(6),
            listOf("Siti Rahmawati" to 52.4, "Eko Saputra" to 28.1, "Nurul Hidayah" to 19.5),
        )
        capture("tl_06_home") { TlHomeScreen(tl, noop, TlHomeActions({}, {}, {}, {}, {}, {}, {})) }
    }

    @Test
    fun performance() {
        val loans = sales.loansOfSales(11)
        val stores = DemoSeed.storesOf(partners.partners.value, 11, SalesRole.SR)
        val range = Period.MONTH.range(today)
        val perStore = stores.map { st -> StoreSales(st, SalesSummary.of(loans.filter { it.storeId == st.id }, range.start, range.endInclusive)) }
        val loanState = LoanSectionState(loans.filter { SalesRepository.dateOf(it.submittedAt) in range }, stores, null, null, Period.MONTH, {}, {}, {})
        val inc = sales.salesIncentive(11, SalesRole.SR)
        val prod = team.productivity(11)
        capture("sls_02_sales") { PerformanceTabScreen(0, {}, Period.MONTH, {}, summary(11), perStore, prod, loanState, inc) }
        capture("sls_03_loans") { PerformanceTabScreen(1, {}, Period.MONTH, {}, summary(11), perStore, prod, loanState, inc) }
        capture("sls_04_incentive") { PerformanceTabScreen(2, {}, Period.MONTH, {}, summary(11), perStore, prod, loanState, inc) }
    }

    @Test
    fun teamReports() {
        val members = team.teamOf(6).filter { it.status == "ACTIVE" }
        capture("tl_07_productivity") { TeamProductivityScreen(Period.MONTH, {}, members.map { it to team.productivity(it.id) }, {}, {}) }
        val inc = members.map { it to sales.salesIncentive(it.id, SalesRole.valueOf(it.role)) }
        val own = partners.partners.value.filter { it.ownerTlId == 6L && it.status == PartnerStatus.ACTIVE }
        capture("tl_10_incentive_team") { TeamIncentiveScreen(1, {}, sales.tlIncentive(6), inc, own.map { it to sales.partnerIncentive(it) }, {}) }
        capture("tl_10_incentive_partner") { TeamIncentiveScreen(2, {}, sales.tlIncentive(6), inc, own.map { it to sales.partnerIncentive(it) }, {}) }
        val loans = sales.loansOfTl(6)
        val range = Period.MONTH.range(today)
        val members2 = members.map { p -> p to SalesSummary.of(loans.filter { it.salesId == p.id }, range.start, range.endInclusive) }
        val perPartner = own.map { p -> p to p.stores.map { st -> StoreSales(st, SalesSummary.of(loans.filter { it.storeId == st.id }, range.start, range.endInclusive)) } }
        val total = SalesSummary.of(loans, range.start, range.endInclusive)
        capture("tl_08_sales_partner") { TeamSalesScreen(Period.MONTH, {}, 1, {}, total, members2, perPartner, null, members, {}, null, own, {}, {}) }
        val days = (0L..3L).map { today.minusDays(it) }.map { d ->
            val a = ops.attendanceOf(15, d)
            EvidenceDay(d, ops.attendanceStatus(15, d), a, emptyList())
        }
        capture("tl_07_evidence") { EvidenceScreen(DemoDirectory.person(15)!!.fullName, Period.WEEK, {}, days, {}, { _, _ -> }) }
    }
}
