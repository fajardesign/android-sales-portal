package com.example.and_sales_portal.data.activity

import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.partner.PartnerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/** Shared attendance and visits (ATT-01 … ATT-05) and visit planning (TL-04, TL-05; PRD v3 C7). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ActivityRepositoryTest {

    private val store = DemoStore(ApplicationProvider.getApplicationContext())
    private val wednesday = LocalDate.of(2026, 10, 7)
    private var now: Instant = at(wednesday, "09:00")
    private lateinit var partners: PartnerRepository
    private lateinit var ops: OperationsRepository

    private fun at(date: LocalDate, time: String) = date.atTime(LocalTime.parse(time)).atZone(ActivityRules.ZONE).toInstant()
    private fun siti() = ActivityRepository(ops, partners, 11, SalesRole.SR)

    private val machineZone = ActivityRules.systemZone

    /** The device zone of the test machine would decide (PRD alignment 2026-10-09); use the area zones instead. */
    @org.junit.After
    fun restoreZone() {
        ActivityRules.systemZone = machineZone
    }

    @Before
    fun clean() {
        ActivityRules.systemZone = { java.time.ZoneId.of("UTC") }
        partners = PartnerRepository(store) { now }
        partners.reset()
        ops = OperationsRepository(store, partners) { now }
        ops.reset()
    }

    @Test
    fun seedsEveryActiveEmployee() {
        // Revision 2026-10-08: no planned visits; today starts empty, history has check-ins 12:00–16:59 local time.
        assertTrue(siti().visitsOn(wednesday).isEmpty())
        val history = ops.data.value.visits.filter { it.employeeId == 11L }
        assertTrue(history.isNotEmpty())
        history.forEach { v ->
            assertEquals(VisitState.DONE, v.state)
            val local = java.time.Instant.ofEpochMilli(v.checkIn!!.at).atZone(ActivityRules.zoneOf(11)).toLocalTime()
            assertTrue(local.hour in 12..16)
            assertTrue(v.checkIn!!.distance!! <= ActivityRules.VISIT_RADIUS_METERS)
        }
        // At most one visit a day in the sample.
        assertEquals(history.size, history.map { it.date }.toSet().size)
        assertTrue(ops.data.value.attendance.any { it.employeeId == 6L })
        // Sample punches are within 3 km of the area office or a toko and record where (revision 2026-10-08).
        ops.data.value.attendance.mapNotNull { it.clockIn }.forEach { p ->
            assertNotNull(p.place)
            assertTrue(p.distance!! <= ActivityRules.ATTENDANCE_RADIUS_METERS)
        }
        assertNull(siti().attendanceOf(wednesday))
    }

    @Test
    fun clockInAndOutAreSavedAndReloaded() {
        siti().clockIn(Punch(now.toEpochMilli(), -5.1, 119.4))
        siti().clockOut(Punch(now.plusSeconds(3600).toEpochMilli(), -5.1, 119.4))
        val reloaded = ActivityRepository(OperationsRepository(store, partners) { now }, partners, 11, SalesRole.SR)
        assertNotNull(reloaded.attendanceOf(wednesday)?.clockOut)
    }

    @Test
    fun offlineCheckOutIsSyncedLater() {
        val r = siti()
        r.clockIn(Punch(now.toEpochMilli(), 0.0, 0.0))
        now = at(wednesday, "11:00") // 12:00 WITA in Makassar
        val v = requireNotNull(r.checkIn("REG2026-0139-2", Punch(now.toEpochMilli(), 0.0, 0.0)))
        r.checkOut(v.id, Punch(now.plusSeconds(1800).toEpochMilli(), 0.0, 0.0, synced = false), "  Stok aman ")
        assertEquals(1, r.pendingSync())
        assertEquals("Stok aman", r.visit(v.id)?.note)
        assertEquals(1, r.syncPending())
        assertEquals(0, r.pendingSync())
    }

    @Test
    fun visitCheckInRules() {
        val r = siti()
        val store = "REG2026-0139-2"
        // SOP: check in kerja first; then from 12:00 local time (09:00 WIB is 10:00 WITA).
        assertEquals(VisitCheckInBlock.NotWorkCheckedIn, r.checkInBlock(store))
        r.clockIn(Punch(now.toEpochMilli(), 0.0, 0.0))
        assertEquals(VisitCheckInBlock.BeforeStart, r.checkInBlock(store))
        assertNull(r.checkIn(store, Punch(now.toEpochMilli(), 0.0, 0.0)))
        now = at(wednesday, "11:00")
        assertNull(r.checkInBlock(store))
        // Only at toko assigned to the SR (REG2026-0139-1 is Eko's).
        assertEquals(VisitCheckInBlock.StoreNotAssigned, r.checkInBlock("REG2026-0139-1"))
        val first = requireNotNull(r.checkIn(store, Punch(now.toEpochMilli(), 0.0, 0.0)))
        assertEquals(VisitCheckInBlock.Ongoing, r.checkInBlock("REG2026-0139-3"))
        r.checkOut(first.id, Punch(now.plusSeconds(1800).toEpochMilli(), 0.0, 0.0), null)
        // Several toko a day are allowed; each toko counts once a week (a second visit to it adds nothing).
        val before = r.visitWeek(wednesday).visited
        val second = requireNotNull(r.checkIn("REG2026-0139-3", Punch(now.plusSeconds(3600).toEpochMilli(), 0.0, 0.0)))
        r.checkOut(second.id, Punch(now.plusSeconds(5400).toEpochMilli(), 0.0, 0.0), null)
        assertEquals(2, r.visitsOn(wednesday).size)
        val after = r.visitWeek(wednesday).visited
        assertNotNull(r.checkIn("REG2026-0139-3", Punch(now.plusSeconds(7200).toEpochMilli(), 0.0, 0.0)))
        assertEquals(after, r.visitWeek(wednesday).visited)
        assertTrue(after >= before)
        assertTrue(r.weekPartners(wednesday).first { it.partner.id == "REG2026-0139" }.visited)
    }

    @Test
    fun weeksOfAPeriodFollowTheWebPortal() {
        // Review 2026-10-09: a week belongs to the period when its Monday is in it, up to today (Wednesday 7 Oct).
        val october = LocalDate.of(2026, 10, 1)..wednesday
        assertEquals(listOf(LocalDate.of(2026, 10, 5)), ops.visitWeeks(11, october).map { it.start })
        assertEquals(listOf(LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 28), LocalDate.of(2026, 10, 5)), ops.visitWeeks(11, LocalDate.of(2026, 9, 21)..wednesday.plusDays(10)).map { it.start })
        // "Hari ini" on a Wednesday has no week of its own; the current week is shown separately.
        assertTrue(ops.visitWeeks(11, wednesday..wednesday).isEmpty())
        // Target = Siti's 3 toko.
        assertEquals(3, ops.visitWeek(11, wednesday.minusDays(2)).target)
    }

    @Test
    fun weeklyTargetAndDaysOff() {
        val monday = wednesday.minusDays(2)
        val week = ops.visitWeek(11, monday)
        assertEquals(3, week.target)
        val visitedBefore = ops.visitsBetween(11, monday, wednesday.minusDays(1)).map { it.storeId }.toSet().size
        assertEquals(visitedBefore, week.visited)
        // Days off no longer change the target (it counts toko); past days cannot be changed.
        ops.setDayOff(11, wednesday.plusDays(2), true)
        assertEquals(3, ops.visitWeek(11, monday).target)
        ops.setDayOff(11, monday, true)
        assertTrue(!ops.isDayOff(11, monday))
        ops.setDayOff(11, wednesday, true)
        assertEquals(AttendanceStatus.DAY_OFF, ops.attendanceStatus(11, wednesday))
        assertEquals(VisitCheckInBlock.DayOff, siti().checkInBlock("REG2026-0139-2"))
        ops.setDayOff(11, wednesday, false)
        assertEquals(AttendanceStatus.NOT_YET, ops.attendanceStatus(11, wednesday))
    }

    @Test
    fun teamProductivityHasAbsentAndAverageCheckIn() {
        // TL-07 (PRD alignment 2026-10-09): Absen and the average check in, each read in its saved zone (Makassar WITA).
        val team = com.example.and_sales_portal.data.team.TeamService(partners, ops)
        val range = LocalDate.of(2026, 10, 1)..wednesday.minusDays(1)
        val p = team.productivity(11, range)
        val punches = (0L..5L).map { range.start.plusDays(it) }.filter(ActivityRules::isWorkday).mapNotNull { ops.attendanceOf(11, it)?.clockIn }
        val minutes = punches.map { java.time.Instant.ofEpochMilli(it.at).atZone(java.time.ZoneId.of("Asia/Makassar")).toLocalTime().let { t -> t.hour * 60 + t.minute } }
        val avg = Math.round(minutes.sum().toDouble() / minutes.size).toInt()
        assertEquals("%02d:%02d WITA".format(avg / 60, avg % 60), p.avgClockIn)
        assertEquals(p.workdays - p.present - p.daysOff, p.absent)
        // No check in in the period: no average.
        assertNull(team.productivity(11, wednesday..wednesday).avgClockIn)
    }
}
