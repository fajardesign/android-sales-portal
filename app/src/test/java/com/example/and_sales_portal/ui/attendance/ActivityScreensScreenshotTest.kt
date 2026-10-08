package com.example.and_sales_portal.ui.attendance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.Attendance
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.DemoSeed
import com.example.and_sales_portal.data.partner.PartnerSeed
import com.example.and_sales_portal.data.activity.Punch
import com.example.and_sales_portal.data.activity.Visit
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
        val place = Place(store.lat, store.lng, store.address)
        val found = LocationResult.Found(store.lat + 0.00007, store.lng - 0.00007, store.address, mock = false)
        val base = CaptureUiState(CaptureMode.CHECK_IN, place, store.name, verified = true)
        capture("02_locating") { CaptureScreen(base.copy(locating = true), CaptureActions()) }
        capture("02_in_radius") { CaptureScreen(base.copy(found = found, distance = 11), CaptureActions()) }
        capture("02_out_of_radius") { CaptureScreen(base.copy(found = found.copy(address = null), distance = 152), CaptureActions()) }
        capture("02_far_away") { CaptureScreen(base.copy(found = found, distance = 12_860_885), CaptureActions()) }
        val clockIn = CaptureUiState(CaptureMode.CLOCK_IN, place, verified = true, found = found)
        capture("02_clock_in_location") { CaptureScreen(clockIn, CaptureActions()) }
        capture("02_selfie_empty") { CaptureScreen(clockIn.copy(step = CaptureStep.Selfie), CaptureActions()) }
        capture("02_selfie_checking") { CaptureScreen(clockIn.copy(step = CaptureStep.Selfie, liveness = Liveness.Checking), CaptureActions()) }
        capture("02_selfie_passed_offline") { CaptureScreen(clockIn.copy(step = CaptureStep.Selfie, liveness = Liveness.Passed, online = false), CaptureActions()) }
    }

    @Test
    fun visits() {
        val partners = PartnerSeed.partners()
        val stores = DemoSeed.storesOf(partners, 11, com.example.and_sales_portal.data.auth.SalesRole.SR)
        val plan = DemoSeed.plan(11, com.example.and_sales_portal.data.auth.SalesRole.SR, stores, today)
        fun item(v: Visit) = requireNotNull(visitItem(v, stores.first { it.id == v.storeId }, partners.first { p -> p.stores.any { it.id == v.storeId } }, today))
        val done = plan[0].copy(state = VisitState.DONE, checkIn = Punch(at("10:05"), 0.0, 0.0), checkOut = Punch(at("10:52"), 0.0, 0.0, synced = false), note = "Stok brosur ditambah")
        val ongoing = plan[1].copy(state = VisitState.IN_PROGRESS, checkIn = Punch(at("13:24"), 0.0, 0.0))
        val items = listOf(item(done), item(ongoing), item(plan[2]))
        capture("03_list") { VisitListScreen("Kunjungan", "Rabu, 7 Oktober 2026", items, online = true, onBack = null, onOpen = {}) }
        capture("03_empty_offline") { VisitListScreen("Kunjungan Saya", "Rabu, 7 Oktober 2026", emptyList(), online = false, onBack = {}, onOpen = {}) }
        capture("04_not_started") { VisitDetailScreen(item(plan[2]), true, { at("15:00") }, {}, { _, _ -> }, {}, {}) }
        capture("04_ongoing") { VisitDetailScreen(item(ongoing), true, { at("13:50") }, {}, { _, _ -> }, {}, {}) }
        capture("04_done") { VisitDetailScreen(item(done), true, { at("15:00") }, {}, { _, _ -> }, {}, {}) }
        capture("04_missed") { VisitDetailScreen(item(plan[2]).copy(status = com.example.and_sales_portal.data.activity.VisitStatus.MISSED), true, { at("15:00") }, {}, { _, _ -> }, {}, {}) }
        capture("05_check_out_offline") { VisitCheckOutScreen(item(ongoing), "", online = false, now = at("13:50"), {}, {}, {}) }
    }

    @Test
    fun simulation() {
        capture("sim") { SimulationScreen(Simulation(), {}, {}) }
    }
}
