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
import com.example.and_sales_portal.data.device.PerfData
import com.example.and_sales_portal.ui.performance.performanceCardState
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

/** Beranda TL / SA-SR with "Performa bulan ini" (TL-06, SLS-01, PRF-01) and Produktivitas (TL-07). */
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

    private fun card(id: Long, role: SalesRole) = performanceCardState(sales, id, role, true, PerfData.NORMAL)

    @Test
    fun homes() {
        val week = ops.visitWeek(11, ActivityRules.weekStart(today))
        val groups = ops.weekPartners(11, ActivityRules.weekStart(today))
        val sr = SalesHomeState("Siti", SalesRole.SR, today, AttendanceDay(today, null, AttendanceStatus.NOT_YET), true, week, false, groups, partners.myPartners(11, SalesRole.SR), card(11, SalesRole.SR))
        capture("sls_01_home") { SalesHomeScreen(sr, noop, {}, {}, {}) }
        val tl = TlHomeState(
            "Andi", today, AttendanceDay(today, null, AttendanceStatus.NOT_YET), true, ops.visitWeek(6, ActivityRules.weekStart(today)), 2, 1, 0, 1, 2, 0, 3,
            partners.submissionsFor(6, SalesRole.TL).groupingBy { it.status }.eachCount() + (PartnerStatus.REVISION_REQUIRED to 1),
            card(6, SalesRole.TL),
            listOf("Siti Rahmawati" to 52.4, "Eko Saputra" to 28.1, "Nurul Hidayah" to 19.5),
        )
        capture("tl_06_home") { TlHomeScreen(tl, noop, TlHomeActions({}, {}, {}, {}, {}, {}, {})) }
    }

    @Test
    fun teamReports() {
        val members = team.teamOf(6).filter { it.status == "ACTIVE" }
        capture("tl_07_productivity") { TeamProductivityScreen(Period.MONTH, {}, members.map { it to team.productivity(it.id) }, {}, {}) }
        val days = (0L..3L).map { today.minusDays(it) }.map { d ->
            val a = ops.attendanceOf(15, d)
            EvidenceDay(d, ops.attendanceStatus(15, d), a, emptyList())
        }
        capture("tl_07_evidence") { EvidenceScreen(DemoDirectory.person(15)!!.fullName, Period.WEEK, {}, days, {}, { _, _ -> }) }
    }
}
