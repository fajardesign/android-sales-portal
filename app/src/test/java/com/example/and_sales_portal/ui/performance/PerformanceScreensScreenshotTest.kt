package com.example.and_sales_portal.ui.performance

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.DemoSeed
import com.example.and_sales_portal.data.activity.OperationsRepository
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.device.PerfData
import com.example.and_sales_portal.data.device.S3Date
import com.example.and_sales_portal.data.device.Simulation
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.data.sales.PortalStatus
import com.example.and_sales_portal.data.sales.SalesRepository
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

/** PRD Scope 3 Performa screens (PRF-01 … PRF-05) and their states, on the sample data of 7 Oct 2026. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h1600dp-mdpi")
class PerformanceScreensScreenshotTest {

    private fun capture(name: String, content: @Composable () -> Unit) =
        captureRoboImage("build/outputs/roborazzi/prf_$name.png") { CompositionLocalProvider(LocalInspectionMode provides true) { AmarTheme { content() } } }

    private val now = LocalDate.of(2026, 10, 7).atTime(LocalTime.of(10, 30)).atZone(ActivityRules.LOCK_ZONE).toInstant()
    private var sim = Simulation(s3Date = S3Date.OCT_7)
    private lateinit var partners: PartnerRepository
    private lateinit var sales: SalesRepository
    private val oct = YearMonth.of(2026, 10)
    private val sep = YearMonth.of(2026, 9)
    private val aug = YearMonth.of(2026, 8)

    @Before
    fun setUp() {
        val store = DemoStore(ApplicationProvider.getApplicationContext())
        partners = PartnerRepository(store) { now }.also { it.reset() }
        val ops = OperationsRepository(store, partners) { now }.also { it.reset() }
        sales = SalesRepository(partners, ops, simulation = { sim }) { now }
    }

    private fun months() = sales.months().map { it to sales.monthStatus(it) }

    @Test
    fun homeCard() {
        val sr = performanceCardState(sales, 11, SalesRole.SR, true, PerfData.NORMAL)
        capture("01_card_sr") { PerformanceCard(sr, {}, {}) }
        capture("01_card_loading") { PerformanceCard(sr.copy(load = PerfData.LOADING), {}, {}) }
        capture("01_card_error") { PerformanceCard(sr.copy(load = PerfData.ERROR), {}, {}) }
        capture("01_card_no_store") { PerformanceCard(sr.copy(hasStore = false), {}, {}) }
        val zero = sr.row!!.let { r -> r.copy(calc = com.example.and_sales_portal.data.sales.Scope3.calcSales(r.role, 0, r.calc.validDays, r.areaId)) }
        capture("01_card_zero") { PerformanceCard(sr.copy(row = zero), {}, {}) }
        sim = Simulation(s3Date = S3Date.OCT_3)
        capture("01_card_tl_before_lock") { PerformanceCard(performanceCardState(sales, 6, SalesRole.TL, true, PerfData.NORMAL), {}, {}) }
    }

    @Test
    fun pipeline() {
        val stores = DemoSeed.storesOf(partners.partners.value, 6, SalesRole.TL)
        val team = DemoDirectory.teamOf(6)
        val actions = PipelineActions({}, {}, {}, {}, {})
        val tl = PipelineState(sep, months(), sales.pipeline(sep, tlId = 6), null, stores, null, team, null)
        capture("02_pipeline_tl") { ScreenScaffold("Performa", null) { PipelineSection(tl, actions) } }
        val sa = PipelineState(oct, months(), sales.pipeline(oct, salesId = 11, status = PortalStatus.CAIR), PortalStatus.CAIR, DemoSeed.storesOf(partners.partners.value, 11, SalesRole.SR), null, null, null)
        capture("02_pipeline_sr_cair") { ScreenScaffold("Performa", null) { PipelineSection(sa, actions) } }
        val empty = PipelineState(YearMonth.of(2026, 5), months(), sales.pipeline(YearMonth.of(2026, 5), salesId = 16), null, emptyList(), null, null, null)
        capture("02_pipeline_empty") { ScreenScaffold("Performa", null) { PipelineSection(empty, actions) } }
        val later = sales.pipeline(sep, tlId = 6).rows.first { it.paidLater }
        capture("03_loan_detail") { LoanDetailScreen(later, "Jaya Abadi Cellular", DemoDirectory.person(later.loan.salesId)?.fullName, DemoDirectory.person(later.loan.tlId)?.fullName) {} }
    }

    @Test
    fun income() {
        val actions = IncomeActions({}, {}, {}, {})
        val history = sales.history(11)
        capture("04_income_oct") { ScreenScaffold("Performa", null) { IncomeSection(IncomeState(PerfData.NORMAL, oct, months(), sales.salesRow(11, oct), "Makassar", history, sales.asOf()), actions) } }
        capture("04_income_sep_final") { ScreenScaffold("Performa", null) { IncomeSection(IncomeState(PerfData.NORMAL, sep, months(), sales.salesRow(11, sep), "Makassar", history, sales.asOf()), actions) } }
        capture("04_income_aug_paid") { ScreenScaffold("Performa", null) { IncomeSection(IncomeState(PerfData.NORMAL, aug, months(), sales.salesRow(11, aug), "Makassar", history, sales.asOf()), actions) } }
        val loans = sales.pipeline(sep, salesId = 11).rows.filter { it.counted || it.paidLater }
        capture("04_month_detail") { IncomeMonthScreen(sales.salesRow(11, sep), sep, null, "Makassar", loans, {}, {}) }
        capture("04_income_loading") { ScreenScaffold("Performa", null) { IncomeSection(IncomeState(PerfData.LOADING, oct, months(), null, "Makassar", history, sales.asOf()), actions) } }
    }

    @Test
    fun team() {
        val rows = sales.rowsFor(6, SalesRole.TL, sep)
        val stores = partners.partners.value.flatMap { p ->
            p.stores.filter { it.tlId == 6L && it.status == "ACTIVE" }.map { st ->
                StoreFunnel(st, p.name, PortalStatus.entries.associateWith { s -> sales.pipeline(sep, tlId = 6).rows.count { it.loan.storeId == st.id && it.status == s } }, st.assigned.firstOrNull()?.let { DemoDirectory.person(it)?.fullName })
            }
        }
        val state = TeamState(PerfData.NORMAL, sep, months(), rows, sales.totals(rows), rows.first().calc.actual, emptyMap(), stores, 3, sales.asOf())
        capture("05_team_sep") { ScreenScaffold("Performa", null) { TeamSection(state, TeamActions({}, {}, {}, {}), { "$it (Anda)" }) } }
        capture("05_team_empty") { ScreenScaffold("Performa", null) { TeamSection(state.copy(rows = rows.take(1), teamSize = 0, stores = emptyList()), TeamActions({}, {}, {}, {}), { "$it (Anda)" }) } }
    }

    @Test
    fun notifications() {
        sales.syncIncomeNotices(11)
        sim = Simulation(s3Date = S3Date.OCT_7, s3PaidLatest = true)
        sales.syncIncomeNotices(11)
        val items = partners.notificationsOf(11).filter { it.income != null }
        capture("06_notifications") { Column { com.example.and_sales_portal.ui.notifications.NotificationsScreen(items, {}) {} } }
    }
}
