package com.example.and_sales_portal.ui.team

import androidx.compose.runtime.Composable
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.DemoSeed
import com.example.and_sales_portal.data.activity.Punch
import com.example.and_sales_portal.data.activity.VisitState
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.partner.PartnerSeed
import com.example.and_sales_portal.data.team.Productivity
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.visits.visitItem
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.LocalTime

/** Tim, Detail Anggota, Assign SA/SR, Jadwal Kunjungan and the visit form (TL-01 … TL-05). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h760dp-mdpi")
class TeamScreensScreenshotTest {

    private fun capture(name: String, content: @Composable () -> Unit) =
        captureRoboImage("build/outputs/roborazzi/tl_$name.png") { AmarTheme { content() } }

    private val partners = PartnerSeed.partners()
    private val today = LocalDate.of(2026, 10, 7)
    private fun person(id: Long) = DemoDirectory.person(id)!!
    private fun assigned(id: Long) = partners.filter { p -> p.stores.any { id in it.assigned } }

    private val members = listOf(
        Member(person(11), AttendanceStatus.ON_TIME, assigned(11)),
        Member(person(15), AttendanceStatus.NOT_YET, assigned(15)),
        Member(person(16), AttendanceStatus.LATE, assigned(16)),
    )

    @Test
    fun teamAndDetail() {
        capture("01_team") { TeamListScreen(members) {} }
        val pr = Productivity(6, 5, 4, 1, 1, 0, 15, 12, 10, 2, 2, 1)
        capture("02_detail") { MemberDetailScreen(members[0], pr, {}, {}, {}) }
    }

    @Test
    fun assign() {
        val sa = person(15)
        val owned = partners.filter { it.ownerTlId == 6L && it.status.name == "ACTIVE" }
        capture("03_assign_sa") { AssignScreen(listOf(person(11), sa), sa, {}, assigned(15), owned - assigned(15).toSet(), {}, {}, {}) }
        capture("03_assign_empty") { AssignScreen(listOf(person(11), sa), null, {}, emptyList(), emptyList(), {}, {}, {}) }
    }

    @Test
    fun schedule() {
        val stores = DemoSeed.storesOf(partners, 11, SalesRole.SR)
        val plan = DemoSeed.plan(11, SalesRole.SR, stores, today)
        val at = { t: String -> today.atTime(LocalTime.parse(t)).atZone(ActivityRules.ZONE).toInstant().toEpochMilli() }
        val visits = listOf(
            plan[0].copy(state = VisitState.DONE, checkIn = Punch(at("10:05"), 0.0, 0.0), checkOut = Punch(at("10:50"), 0.0, 0.0)),
            plan[1],
            plan[2].copy(state = VisitState.CANCELLED, cancelReason = "Dibatalkan oleh Team Leader"),
        ).map { v -> visitItem(v, stores.first { it.id == v.storeId }, partners.first { p -> p.stores.any { it.id == v.storeId } }, today)!! }
        val monday = today.minusDays(2)
        val week = (0L..6L).map { monday.plusDays(it) }.map { PlanDay(it, if (it.dayOfWeek.value < 7) 3 else 0, it.dayOfWeek.value == 6) }
        capture("04_schedule") { ScheduleScreen(listOf(person(11)), person(11), {}, week, today, today, visits, true, {}, {}, {}, {}, {}, {}, {}, {}, {}) }
        capture("04_schedule_off") { ScheduleScreen(listOf(person(11)), person(11), {}, week, today.plusDays(3), today, emptyList(), true, {}, {}, {}, {}, {}, {}, {}, {}, {}) }
        capture("05_form") { VisitPlanFormScreen(false, "Siti Rahmawati", today.plusDays(1), stores, stores[0], "09:00", null, "Sudah ada kunjungan pada waktu ini", {}, {}, {}, {}) }
    }
}
