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

    @Before
    fun clean() {
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
        // A second visit the same day is allowed but counts once in the weekly target.
        val before = r.visitWeek(wednesday).visited
        assertNotNull(r.checkIn("REG2026-0139-3", Punch(now.plusSeconds(3600).toEpochMilli(), 0.0, 0.0)))
        assertEquals(2, r.visitsOn(wednesday).size)
        assertEquals(before, r.visitWeek(wednesday).visited)
    }

    @Test
    fun weeksOfAPeriodFollowTheWebPortal() {
        // Review 2026-10-09: a week belongs to the period when its Monday is in it, up to today (Wednesday 7 Oct).
        val october = LocalDate.of(2026, 10, 1)..wednesday
        assertEquals(listOf(LocalDate.of(2026, 10, 5)), ops.visitWeeks(11, october).map { it.start })
        assertEquals(listOf(LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 28), LocalDate.of(2026, 10, 5)), ops.visitWeeks(11, LocalDate.of(2026, 9, 21)..wednesday.plusDays(10)).map { it.start })
        // "Hari ini" on a Wednesday has no week of its own; the current week is shown separately.
        assertTrue(ops.visitWeeks(11, wednesday..wednesday).isEmpty())
        assertEquals(6, ops.visitWeek(11, wednesday.minusDays(2)).target)
    }

    @Test
    fun weeklyTargetAndDaysOff() {
        val monday = wednesday.minusDays(2)
        val week = ops.visitWeek(11, monday)
        assertEquals(6, week.target)
        val visitedBefore = ops.visitsBetween(11, monday, wednesday.minusDays(1)).map { it.date }.toSet().size
        assertEquals(visitedBefore, week.visited)
        // A day off leaves the target; past days cannot be changed.
        ops.setDayOff(11, wednesday.plusDays(2), true)
        assertEquals(5, ops.visitWeek(11, monday).target)
        ops.setDayOff(11, monday, true)
        assertEquals(5, ops.visitWeek(11, monday).target)
        ops.setDayOff(11, wednesday, true)
        assertEquals(AttendanceStatus.DAY_OFF, ops.attendanceStatus(11, wednesday))
        assertEquals(VisitCheckInBlock.DayOff, siti().checkInBlock("REG2026-0139-2"))
        ops.setDayOff(11, wednesday, false)
        assertEquals(AttendanceStatus.NOT_YET, ops.attendanceStatus(11, wednesday))
    }
}
