package com.example.and_sales_portal.ui.refresh

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.DemoSeed
import com.example.and_sales_portal.data.activity.OperationsRepository
import com.example.and_sales_portal.data.activity.Punch
import com.example.and_sales_portal.data.activity.Visit
import com.example.and_sales_portal.data.activity.VisitState
import com.example.and_sales_portal.data.activity.VisitWeek
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.device.LocationResult
import com.example.and_sales_portal.data.device.Place
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.data.partner.PartnerStatus
import com.example.and_sales_portal.data.sales.SalesRepository
import com.example.and_sales_portal.data.sales.SalesSummary
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.attendance.AttendanceDay
import com.example.and_sales_portal.ui.attendance.CaptureActions
import com.example.and_sales_portal.ui.attendance.CaptureMode
import com.example.and_sales_portal.ui.attendance.CaptureScreen
import com.example.and_sales_portal.ui.attendance.CaptureStep
import com.example.and_sales_portal.ui.attendance.CaptureUiState
import com.example.and_sales_portal.ui.dashboard.AttendanceActions
import com.example.and_sales_portal.ui.dashboard.SalesHomeScreen
import com.example.and_sales_portal.ui.dashboard.SalesHomeState
import com.example.and_sales_portal.ui.dashboard.TlHomeActions
import com.example.and_sales_portal.ui.dashboard.TlHomeScreen
import com.example.and_sales_portal.ui.dashboard.TlHomeState
import com.example.and_sales_portal.ui.visits.VisitListScreen
import com.example.and_sales_portal.ui.visits.VisitListUiState
import com.example.and_sales_portal.ui.visits.visitItem
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.LocalTime

/**
 * The four screens of the UI refresh (Beranda SA/SR, Beranda TL, Kunjungan, Check-in) on a tall screen, so the whole
 * content can be reviewed at once (`build/outputs/roborazzi/full_*.png`).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h1600dp-mdpi")
class FullScreensScreenshotTest {

    private fun capture(name: String, content: @Composable () -> Unit) =
        captureRoboImage("build/outputs/roborazzi/full_$name.png") { CompositionLocalProvider(LocalInspectionMode provides true) { AmarTheme { content() } } }

    private val today = LocalDate.of(2026, 10, 7)
    private val now = today.atTime(LocalTime.of(10, 30)).atZone(ActivityRules.ZONE).toInstant()
    private fun at(time: String) = today.atTime(LocalTime.parse(time)).atZone(ActivityRules.ZONE).toInstant().toEpochMilli()
    private lateinit var partners: PartnerRepository
    private lateinit var ops: OperationsRepository
    private lateinit var sales: SalesRepository
    private val noop = AttendanceActions({}, {}, {})

    @Before
    fun setUp() {
        val store = DemoStore(ApplicationProvider.getApplicationContext())
        partners = PartnerRepository(store) { now }.also { it.reset() }
        ops = OperationsRepository(store, partners) { now }.also { it.reset() }
        sales = SalesRepository(partners, ops) { now }
    }

    @Test
    fun homes() {
        val week = ops.visitWeek(11, ActivityRules.weekStart(today))
        val groups = ops.weekPartners(11, ActivityRules.weekStart(today))
        val sr = SalesHomeState(
            "Siti", SalesRole.SR, today, AttendanceDay(today, null, AttendanceStatus.NOT_YET), true, week, false, groups,
            partners.myPartners(11, SalesRole.SR), SalesSummary.of(sales.loansOfSales(11), today.withDayOfMonth(1), today), sales.salesIncentive(11, SalesRole.SR),
        )
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
    fun visitsAndCheckIn() {
        val list = partners.partners.value
        val stores = DemoSeed.storesOf(list, 11, SalesRole.SR)
        fun partnerOf(id: String) = list.first { p -> p.stores.any { it.id == id } }
        val done = Visit("v0", 11, stores[0].id, today.toString(), VisitState.DONE, Punch(at("12:05"), 0.0, 0.0), Punch(at("12:52"), 0.0, 0.0, synced = false))
        val state = VisitListUiState(
            date = "Rabu, 7 Oktober 2026",
            week = VisitWeek(ActivityRules.weekStart(today), 1, 2),
            visitedToday = true,
            groups = ActivityRules.weekPartners(stores.map { partnerOf(it.id) to it }, setOf(stores[0].id)),
            visits = listOfNotNull(visitItem(done, stores[0], partnerOf(stores[0].id), today)),
            online = true,
            block = null,
        )
        capture("att_03_list") { VisitListScreen("Kunjungan", state, null, {}, {}, {}) }
        val store = stores[0]
        val place = Place(store.lat, store.lng, store.address, store.name)
        val found = LocationResult.Found(store.lat + 0.00007, store.lng - 0.00007, store.address, mock = false)
        val base = CaptureUiState(CaptureMode.CHECK_IN, listOf(place), store.name, found = found, nearest = place, distance = 11)
        capture("att_02_location") { CaptureScreen(base, CaptureActions()) }
        capture("att_02_selfie") { CaptureScreen(base.copy(step = CaptureStep.Selfie), CaptureActions()) }
    }

    @Test
    fun handoverAdjustments() {
        // Decisions 2026-10-19: attendance history with "Tidak check out" and "Dikoreksi TL"; TL-11 Koreksi Absensi.
        val repo = com.example.and_sales_portal.data.activity.ActivityRepository(ops, partners, 11, SalesRole.SR)
        capture("att_01_absensi") {
            com.example.and_sales_portal.ui.attendance.AttendanceScreen(com.example.and_sales_portal.ui.attendance.AttendanceFragment.attendanceState(repo, true), {}, {}, {})
        }
        val day = ActivityRules.weekStart(today).minusWeeks(1).let { mon -> (0L..6L).map { mon.plusDays(it) }.first { ops.attendanceOf(11, it)?.corrected == true } }
        val a = ops.attendanceOf(11, day)
        val st = com.example.and_sales_portal.ui.team.CorrectionUiState("Siti Rahmawati", com.example.and_sales_portal.ui.attendance.AttendanceFragment.attendanceDay(a, day, today), true, today.plusDays(4))
        capture("tl_11_correction") {
            com.example.and_sales_portal.ui.team.AttendanceCorrectionScreen(st, com.example.and_sales_portal.ui.team.CorrectionForm(AttendanceStatus.ON_TIME, "08:50", ""), {}, {}, {})
        }
        capture("tl_11_correction_expired") {
            com.example.and_sales_portal.ui.team.AttendanceCorrectionScreen(st.copy(canCorrect = false), com.example.and_sales_portal.ui.team.CorrectionForm(), {}, {}, {})
        }
    }
}
