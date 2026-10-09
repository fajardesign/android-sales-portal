package com.example.and_sales_portal.ui.attendance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.Attendance
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.DemoSeed
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.partner.PartnerSeed
import com.example.and_sales_portal.data.activity.Punch
import com.example.and_sales_portal.data.activity.Visit
import com.example.and_sales_portal.data.activity.VisitCheckInBlock
import com.example.and_sales_portal.data.activity.VisitWeek
import com.example.and_sales_portal.ui.visits.VisitListUiState
import com.example.and_sales_portal.data.activity.VisitState
import com.example.and_sales_portal.data.device.LocationResult
import com.example.and_sales_portal.data.device.Place
import com.example.and_sales_portal.data.device.Simulation
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.simulation.SimulationScreen
import com.example.and_sales_portal.ui.visits.VisitCheckOutScreen
import com.example.and_sales_portal.ui.visits.VisitDetailScreen
import com.example.and_sales_portal.ui.visits.VisitListScreen
import com.example.and_sales_portal.ui.visits.visitItem
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.LocalTime

/** Absensi (ATT-01, ATT-02), Kunjungan (ATT-03 … ATT-05) and the Simulasi panel. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h760dp-mdpi")
class ActivityScreensScreenshotTest {

    private val today = LocalDate.of(2026, 10, 7)
    private fun at(time: String, date: LocalDate = today) = date.atTime(LocalTime.parse(time)).atZone(ActivityRules.ZONE).toInstant().toEpochMilli()
    private fun capture(name: String, content: @Composable () -> Unit) =
        captureRoboImage("build/outputs/roborazzi/att_$name.png") { CompositionLocalProvider(LocalInspectionMode provides true) { AmarTheme { content() } } }

    private fun day(a: Attendance?, date: LocalDate = today) = AttendanceDay(date, a, ActivityRules.attendanceStatus(a, date, today))

    @Test
    fun attendance() {
        val history = (1L..6L).map { today.minusDays(it) }.filter(ActivityRules::isWorkday).mapIndexed { i, d ->
            day(if (i == 2) null else Attendance(11, d.toString(), Punch(at(if (i == 1) "08:12" else "07:41", d), 0.0, 0.0), Punch(at("17:20", d), 0.0, 0.0)), d)
        }
        capture("01_not_yet") { AttendanceScreen(AttendanceUiState(day(null), history, online = true), {}, {}, {}) }
        val clockedIn = Attendance(11, today.toString(), Punch(at("07:52"), 0.0, 0.0))
        capture("01_clocked_in") { AttendanceScreen(AttendanceUiState(day(clockedIn), history, online = true), {}, {}, {}) }
        capture("01_offline") { AttendanceScreen(AttendanceUiState(day(null), history, online = false), {}, {}, {}) }
        val done = clockedIn.copy(clockOut = Punch(at("17:05"), 0.0, 0.0))
        capture("01_card_done") { AttendanceCard(day(done), true, {}, {}, onHistory = {}) }
        capture("01_card_late") { AttendanceCard(AttendanceDay(today, clockedIn.copy(clockIn = Punch(at("08:30"), 0.0, 0.0)), AttendanceStatus.LATE), true, {}, {}) }
    }

    @Test
    fun capture() {
        val store = PartnerSeed.partners().first { it.id == "REG2026-0139" }.stores.first()
        val place = Place(store.lat, store.lng, store.address, store.name)
        val found = LocationResult.Found(store.lat + 0.00007, store.lng - 0.00007, store.address, mock = false)
        val base = CaptureUiState(CaptureMode.CHECK_IN, listOf(place), store.name)
        capture("02_locating") { CaptureScreen(base.copy(locating = true), CaptureActions()) }
        capture("02_in_radius") { CaptureScreen(base.copy(found = found, nearest = place, distance = 11), CaptureActions()) }
        capture("02_out_of_radius") { CaptureScreen(base.copy(found = found.copy(address = null), nearest = place, distance = 152), CaptureActions()) }
        capture("02_far_away") { CaptureScreen(base.copy(found = found, nearest = place, distance = 12_860_885), CaptureActions()) }
        // Attendance (revision 2026-10-08): within 3 km of the area office or a toko.
        val office = DemoDirectory.office(1)!!.let { Place(it.lat, it.lng, null, it.name) }
        val atOffice = LocationResult.Found(office.lat + 0.002, office.lng, "Jl. A. P. Pettarani, Makassar", mock = false)
        val clockIn = CaptureUiState(CaptureMode.CLOCK_IN, listOf(office, place), found = atOffice, nearest = office, distance = 222)
        capture("02_clock_in_location") { CaptureScreen(clockIn, CaptureActions()) }
        capture("02_clock_in_out_of_radius") { CaptureScreen(clockIn.copy(distance = 8_950, sheet = CaptureSheet.Radius), CaptureActions()) }
        capture("02_selfie_empty") { CaptureScreen(clockIn.copy(step = CaptureStep.Selfie), CaptureActions()) }
        capture("02_selfie_taken_offline") { CaptureScreen(clockIn.copy(step = CaptureStep.Selfie, selfie = "missing.jpg", online = false), CaptureActions()) }
    }

    @Test
    fun visits() {
        val partners = PartnerSeed.partners()
        val stores = DemoSeed.storesOf(partners, 11, com.example.and_sales_portal.data.auth.SalesRole.SR)
        fun partnerOf(id: String) = partners.first { p -> p.stores.any { it.id == id } }
        fun item(v: Visit) = requireNotNull(visitItem(v, stores.first { it.id == v.storeId }, partnerOf(v.storeId)))
        fun visit(i: Int, state: VisitState, inAt: String, outAt: String? = null, synced: Boolean = true) = Visit(
            "v$i", 11, stores[i].id, today.toString(), state,
            checkIn = Punch(at(inAt), stores[i].lat, stores[i].lng, distance = 120),
            checkOut = outAt?.let { Punch(at(it), stores[i].lat, stores[i].lng, synced = synced) },
            note = if (outAt != null) "Stok brosur ditambah" else null,
        )
        val done = visit(0, VisitState.DONE, "12:05", "12:52", synced = false)
        val ongoing = visit(1, VisitState.IN_PROGRESS, "14:24")
        val monday = ActivityRules.weekStart(today)
        val state = VisitListUiState(
            date = "Rabu, 7 Oktober 2026",
            week = VisitWeek(monday, 1, 3),
            visitedToday = true,
            dayOff = false,
            groups = com.example.and_sales_portal.data.activity.ActivityRules.weekPartners(stores.map { partnerOf(it.id) to it }, setOf(stores[0].id)),
            visits = listOf(item(done), item(ongoing)),
            online = true,
            block = VisitCheckInBlock.Ongoing,
        )
        capture("03_list") { VisitListScreen("Kunjungan", state, null, {}, {}, {}) }
        capture("03_before_start") { VisitListScreen("Kunjungan", state.copy(visits = emptyList(), visitedToday = false, block = VisitCheckInBlock.BeforeStart), null, {}, {}, {}) }
        capture("03_need_work_check_in") { VisitListScreen("Kunjungan", state.copy(visits = emptyList(), visitedToday = false, block = VisitCheckInBlock.NotWorkCheckedIn), null, {}, {}, {}) }
        capture("03_complete") { VisitListScreen("Kunjungan", state.copy(week = VisitWeek(monday, 3, 3), visits = listOf(item(done)), block = null), null, {}, {}, {}) }
        capture("03_empty_offline") { VisitListScreen("Kunjungan Saya", state.copy(groups = emptyList(), visits = emptyList(), visitedToday = false, online = false, block = null), {}, {}, {}, {}) }
        capture("04_ongoing") { VisitDetailScreen(item(ongoing), { at("14:50") }, {}, { _, _ -> }, {}) }
        capture("04_done") { VisitDetailScreen(item(done), { at("15:00") }, {}, { _, _ -> }, {}) }
        capture("05_check_out_offline") { VisitCheckOutScreen(item(ongoing), "", online = false, now = at("14:50"), {}, {}, {}) }
    }

    @Test
    fun simulation() {
        capture("sim") { SimulationScreen(Simulation(), {}, {}) }
    }
}
