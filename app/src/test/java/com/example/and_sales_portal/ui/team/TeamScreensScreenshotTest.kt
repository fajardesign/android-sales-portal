package com.example.and_sales_portal.ui.team

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.DemoSeed
import com.example.and_sales_portal.data.activity.Punch
import com.example.and_sales_portal.data.activity.Visit
import com.example.and_sales_portal.data.activity.VisitWeek
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
        captureRoboImage("build/outputs/roborazzi/tl_$name.png") { CompositionLocalProvider(LocalInspectionMode provides true) { AmarTheme { content() } } }

    private val partners = PartnerSeed.partners()
    private val today = LocalDate.of(2026, 10, 7)
    private fun person(id: Long) = DemoDirectory.person(id)!!
    private fun assigned(id: Long) = partners.flatMap { p -> p.stores.filter { id in it.assigned }.map { p to it } }

    private val members = listOf(
        Member(person(11), AttendanceStatus.ON_TIME, assigned(11), belowMinimum = true),
        Member(person(15), AttendanceStatus.NOT_YET, assigned(15)),
        Member(person(16), AttendanceStatus.LATE, assigned(16)),
    )

    @Test
    fun teamAndDetail() {
        capture("01_team") { TeamListScreen(members) {} }
        val pr = Productivity(6, 5, 4, 1, 1, 0, 4, listOf(VisitWeek(today.minusDays(2), 2, 6)))
        capture("02_detail") { MemberDetailScreen(members[0], pr, {}, {}, {}) }
    }

    @Test
    fun assign() {
        val sa = person(15)
        val owned = partners.filter { it.ownerTlId == 6L && it.status.name == "ACTIVE" }.flatMap { p -> p.stores.map { p to it } }
        capture("03_assign_sa") { AssignScreen(listOf(person(11), sa), sa, {}, assigned(15), owned - assigned(15).toSet(), {}, {}, {}) }
        capture("03_assign_empty") { AssignScreen(listOf(person(11), sa), null, {}, emptyList(), emptyList(), {}, {}, {}) }
    }

    @Test
    fun schedule() {
        val stores = DemoSeed.storesOf(partners, 11, SalesRole.SR)
        val at = { d: LocalDate, t: String -> d.atTime(LocalTime.parse(t)).atZone(ActivityRules.ZONE).toInstant().toEpochMilli() }
        val monday = today.minusDays(2)
        fun item(d: LocalDate, i: Int, t: String, done: Boolean = true) = visitItem(
            Visit("v$d$i", 11, stores[i].id, d.toString(), if (done) VisitState.DONE else VisitState.IN_PROGRESS, checkIn = Punch(at(d, t), 0.0, 0.0), checkOut = if (done) Punch(at(d, "15:00"), 0.0, 0.0) else null),
            stores[i], partners.first { p -> p.stores.any { it.id == stores[i].id } },
        )!!
        val days = (0L..5L).map { monday.plusDays(it) }.map { d ->
            when (d.dayOfWeek.value) {
                1 -> WeekDay(d, false, listOf(item(d, 0, "12:10")))
                2 -> WeekDay(d, false, listOf(item(d, 1, "13:40"), item(d, 2, "15:30")))
                3 -> WeekDay(d, false, listOf(item(d, 2, "12:20", done = false)))
                6 -> WeekDay(d, true, emptyList())
                else -> WeekDay(d, false, emptyList())
            }
        }
        capture("04_schedule") { ScheduleScreen(listOf(person(11)), person(11), {}, monday, VisitWeek(monday, 3, 5), days, today, true, {}, {}, {}, {}) }
        capture("04_schedule_no_store") { ScheduleScreen(listOf(person(11)), person(11), {}, monday, VisitWeek(monday, 0, 6), days.map { it.copy(visits = emptyList(), off = false) }, today, false, {}, {}, {}, {}) }
    }
}
