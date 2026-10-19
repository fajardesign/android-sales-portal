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
    /** Wednesday of next week: the sample history ends before today, so that week has no visit yet. */
    private val next = wednesday.plusWeeks(1)
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
            assertEquals(VisitStatus.DONE, ActivityRules.visitStatus(v, wednesday))
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
    fun visitCheckInRules() {
        val r = siti()
        val store = "REG2026-0139-2"
        now = at(next, "09:00")
        // SOP: check in kerja first; then from 12:00 in the area zone (09:00 WIB is 10:00 WITA).
        assertEquals(VisitCheckInBlock.NotWorkCheckedIn, r.checkInBlock(store))
        r.clockIn(Punch(now.toEpochMilli(), 0.0, 0.0))
        assertEquals(VisitCheckInBlock.BeforeStart, r.checkInBlock(store))
        assertNull(r.checkIn(store, Punch(now.toEpochMilli(), 0.0, 0.0)))
        now = at(next, "11:00")
        assertNull(r.checkInBlock(store))
        // Only at toko of the SR's kursi (REG2026-0139-1 is Eko's).
        assertEquals(VisitCheckInBlock.StoreNotAssigned, r.checkInBlock("REG2026-0139-1"))
        assertEquals(0, r.visitWeek(next).visited)
        val first = requireNotNull(r.checkIn(store, Punch(now.toEpochMilli(), 0.0, 0.0)))
        // S13: counted at check-in, no check-out needed before another toko.
        assertEquals(1, r.visitWeek(next).visited)
        assertNull(r.checkInBlock("REG2026-0139-3"))
        val second = requireNotNull(r.checkIn("REG2026-0139-3", Punch(now.plusSeconds(600).toEpochMilli(), 0.0, 0.0)))
        assertEquals(listOf(first.id, second.id), r.ongoingVisits().map { it.id })
        assertTrue(r.visitWeek(next).complete)
        // S12: one visit per toko per week, until next Monday.
        assertEquals(VisitCheckInBlock.VisitedThisWeek, r.checkInBlock(store))
        assertNull(r.checkIn(store, Punch(now.plusSeconds(1200).toEpochMilli(), 0.0, 0.0)))
        now = at(next.plusDays(3), "12:30") // Saturday
        assertEquals(VisitCheckInBlock.VisitedThisWeek, r.checkInBlock(store))
        now = at(next.plusDays(5), "12:30") // next Monday
        r.clockIn(Punch(now.toEpochMilli(), 0.0, 0.0))
        assertNull(r.checkInBlock(store))
    }

    @Test
    fun checkOutIsOptionalAndCanBeOffline() {
        val r = siti()
        now = at(next, "11:00")
        r.clockIn(Punch(now.toEpochMilli(), 0.0, 0.0))
        val v = requireNotNull(r.checkIn("REG2026-0139-2", Punch(now.toEpochMilli(), 0.0, 0.0)))
        assertEquals(VisitStatus.ONGOING, ActivityRules.visitStatus(r.visit(v.id)!!, next))
        // Saved offline with an optional note, sent later.
        assertTrue(r.checkOut(v.id, Punch(now.plusSeconds(1800).toEpochMilli(), 0.0, 0.0, synced = false), "  Stok aman "))
        assertEquals(1, r.pendingSync())
        assertEquals("Stok aman", r.visit(v.id)?.note)
        assertEquals(1, r.syncPending())
        assertEquals(0, r.pendingSync())
        // A visit never checked out still counts; the next day it is Selesai and can no longer be checked out.
        val w = requireNotNull(r.checkIn("REG2026-0139-3", Punch(now.plusSeconds(3600).toEpochMilli(), 0.0, 0.0)))
        now = at(next.plusDays(1), "09:00")
        assertEquals(VisitStatus.DONE, ActivityRules.visitStatus(r.visit(w.id)!!, next.plusDays(1)))
        assertTrue(!r.checkOut(w.id, Punch(now.toEpochMilli(), 0.0, 0.0), null))
        assertNull(r.visit(w.id)?.checkOut)
        assertEquals(2, ops.visitWeek(11, ActivityRules.weekStart(next)).visited)
        // Attendance is independent: a work check in without check out stays present ("Tidak check out", Q6).
        assertEquals(AttendanceStatus.LATE, ops.attendanceStatus(11, next))
        assertTrue(ActivityRules.noCheckOut(ops.attendanceOf(11, next), next, next.plusDays(1)))
    }

    @Test
    fun weeksOfAPeriodFollowTheWebPortal() {
        // Review 2026-10-09: a week belongs to the period when its Monday is in it, up to today (Wednesday 7 Oct).
        val october = LocalDate.of(2026, 10, 1)..wednesday
        assertEquals(listOf(LocalDate.of(2026, 10, 5)), ops.visitWeeks(11, october).map { it.start })
        assertEquals(listOf(LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 28), LocalDate.of(2026, 10, 5)), ops.visitWeeks(11, LocalDate.of(2026, 9, 21)..wednesday.plusDays(10)).map { it.start })
        // "Hari ini" on a Wednesday has no week of its own; the current week is shown separately.
        assertTrue(ops.visitWeeks(11, wednesday..wednesday).isEmpty())
        // Target = Siti's 3 toko held on Monday 00:00 WIB (Q5): Tamalanrea left on Monday 10:30, after the lock.
        assertEquals(3, ops.visitWeek(11, wednesday.minusDays(2)).target)
        assertEquals(3, ops.visitWeek(11, wednesday.minusDays(9)).target)
    }

    @Test
    fun weekTargetIsLockedOnMondayWib() {
        // Q5 (2026-10-19, replacing S9): the target is the toko held on Monday 00:00 WIB; the week runs Monday–Sunday.
        val monday = wednesday.minusDays(2)
        assertEquals(monday.plusDays(6), ActivityRules.weekEnd(monday))
        assertEquals(java.time.Instant.parse("2026-10-04T17:00:00Z").toEpochMilli(), ActivityRules.weekLock(monday))
        assertEquals(3, ops.visitWeek(11, monday).target)
        val lastWeek = ops.visitWeek(11, monday.minusWeeks(1))
        // Released on Wednesday: still in Siti's target this week (she can no longer check in there), gone next week.
        partners.unassign(11, "REG2026-0139-2", 6)
        assertEquals(3, ops.visitWeek(11, monday).target)
        assertEquals(1, ops.visitWeek(11, monday.plusWeeks(1)).target)
        assertEquals(lastWeek, ops.visitWeek(11, monday.minusWeeks(1)))
        // The new holder can visit it now, but it counts for her from next week ("Dihitung mulai minggu depan").
        val nurulNow = ops.visitWeek(16, monday).target
        assertEquals(null, partners.assign(6, 16, "REG2026-0139-2"))
        val row = ops.visitPartners(16, wednesday).flatMap { it.stores }.first { it.store.id == "REG2026-0139-2" }
        assertTrue(!row.counted)
        assertEquals(nurulNow, ops.visitWeek(16, monday).target)
        assertEquals(nurulNow + 1, ops.visitWeek(16, monday.plusWeeks(1)).target)
    }

    @Test
    fun visitCountsForThePersonWhoMadeIt() {
        // Nurul visits the toko she got mid-week: counted for her (not in her target this week), not for Siti.
        val monday = wednesday.minusDays(2)
        partners.unassign(11, "REG2026-0139-2", 6)
        assertEquals(null, partners.assign(6, 16, "REG2026-0139-2"))
        val sitiBefore = ops.visitWeek(11, monday).visited
        now = at(wednesday, "11:30")
        val nurul = ActivityRepository(ops, partners, 16, SalesRole.SA)
        nurul.clockIn(Punch(now.toEpochMilli(), 0.0, 0.0))
        assertNotNull(nurul.checkIn("REG2026-0139-2", Punch(now.toEpochMilli(), 0.0, 0.0)))
        assertEquals(sitiBefore, ops.visitWeek(11, monday).visited)
        assertTrue(ops.visitPartners(16, wednesday).flatMap { it.stores }.first { it.store.id == "REG2026-0139-2" }.visited)
    }

    @Test
    fun sixCheckInsAWeekAndAbsentWithoutCheckIn() {
        // Q6: every day is a workday; 6 check ins a week (Monday–Sunday) on any days; Absen = a day without check in.
        val week = next.with(java.time.DayOfWeek.MONDAY)
        val r = siti()
        assertEquals(AttendanceWeek(week, 0), ops.attendanceWeek(11, week))
        (0L..6L).filter { it != 2L }.forEach { d ->
            now = at(week.plusDays(d), "09:00")
            r.clockIn(Punch(now.toEpochMilli(), -5.15, 119.43, zone = "Asia/Makassar")) // no check out
        }
        val w = ops.attendanceWeek(11, week)
        assertEquals(6, w.checkIns)
        assertTrue(w.met)
        // Saturday and Sunday count like any day; Wednesday without check in is Absen.
        now = at(week.plusDays(7), "09:00")
        assertEquals(AttendanceStatus.ABSENT, ops.attendanceStatus(11, week.plusDays(2)))
        assertEquals(AttendanceStatus.ON_TIME, ops.attendanceStatus(11, week.plusDays(6)))
        assertTrue(ActivityRules.noCheckOut(ops.attendanceOf(11, week.plusDays(6)), week.plusDays(6), week.plusDays(7)))
        assertTrue(!ops.attendanceWeek(11, week.plusWeeks(1)).met)
    }

    @Test
    fun tlCorrectsUntilTheEndOfTheFollowingWeek() {
        // Decision 2026-10-19: Andi (TL 6) corrects Siti's day with a reason; old and new values are kept.
        val day = next // Wednesday of next week: Siti did not check in
        now = at(day.plusDays(1), "09:00")
        assertEquals(AttendanceStatus.ABSENT, ops.attendanceStatus(11, day))
        assertEquals(CorrectionError.NotLeader, ops.correct(7, 11, day, AttendanceStatus.ON_TIME, LocalTime.of(9, 0), "x"))
        assertEquals(CorrectionError.ReasonRequired, ops.correct(6, 11, day, AttendanceStatus.ON_TIME, LocalTime.of(9, 0), " "))
        assertEquals(CorrectionError.TimeRequired, ops.correct(6, 11, day, AttendanceStatus.ON_TIME, null, "Ponsel rusak"))
        assertEquals(CorrectionError.TimeMismatch, ops.correct(6, 11, day, AttendanceStatus.ON_TIME, LocalTime.of(10, 30), "Ponsel rusak"))
        assertNull(ops.correct(6, 11, day, AttendanceStatus.ON_TIME, LocalTime.of(8, 50), "Ponsel rusak"))
        val a = ops.attendanceOf(11, day)!!
        assertEquals(AttendanceStatus.ON_TIME, ops.attendanceStatus(11, day))
        val c = a.corrections.single()
        assertEquals(6L, c.by)
        assertEquals(AttendanceStatus.ABSENT, c.fromStatus)
        assertEquals(AttendanceStatus.ON_TIME, c.toStatus)
        assertEquals("Ponsel rusak", c.reason)
        assertTrue(a.hasCheckIn) // counts as a check in of the week
        // Up to Sunday 23:59 WIB of the following week, not after.
        val deadline = ops.correctionDeadline(day)
        assertEquals(java.time.Instant.parse("2026-10-25T16:59:59.999Z").toEpochMilli(), deadline)
        now = java.time.Instant.ofEpochMilli(deadline)
        assertTrue(ops.canCorrect(11, day))
        now = java.time.Instant.ofEpochMilli(deadline + 1)
        assertTrue(!ops.canCorrect(11, day))
        assertEquals(CorrectionError.Expired, ops.correct(6, 11, day, AttendanceStatus.LATE, LocalTime.of(10, 5), "Lain"))
    }

    @Test
    fun sampleCorrectionsMatchTheWeb() {
        // Same as the web sample: Siti, last week, Absen → Tepat Waktu and Terlambat → Tepat Waktu with the reasons.
        val lastMon = ActivityRules.weekStart(wednesday).minusWeeks(1)
        val corrected = (0L..6L).mapNotNull { ops.attendanceOf(11, lastMon.plusDays(it)) }.filter { it.corrected }
        assertEquals(2, corrected.size)
        val reasons = corrected.associate { it.corrections.single().fromStatus to it.corrections.single().reason }
        assertEquals("Ponsel rusak, hadir di kantor sesuai daftar hadir manual", reasons[AttendanceStatus.ABSENT])
        assertEquals("Gangguan sinyal saat check in, sudah di lokasi sebelum 10:00", reasons[AttendanceStatus.LATE])
        corrected.forEach { assertEquals(AttendanceStatus.ON_TIME, ops.attendanceStatus(11, LocalDate.parse(it.date))) }
    }

    @Test
    fun teamProductivityHasAbsentAndAverageCheckIn() {
        // TL-07 (PRD alignment 2026-10-09): Absen and the average check in, each read in its saved zone (Makassar WITA).
        val team = com.example.and_sales_portal.data.team.TeamService(partners, ops)
        val range = LocalDate.of(2026, 10, 1)..wednesday.minusDays(1)
        val p = team.productivity(11, range)
        val punches = (0L..5L).map { range.start.plusDays(it) }.mapNotNull { d -> ops.attendanceOf(11, d)?.takeIf { it.hasCheckIn }?.clockIn }
        val minutes = punches.map { java.time.Instant.ofEpochMilli(it.at).atZone(java.time.ZoneId.of("Asia/Makassar")).toLocalTime().let { t -> t.hour * 60 + t.minute } }
        val avg = Math.round(minutes.sum().toDouble() / minutes.size).toInt()
        assertEquals("%02d:%02d WITA".format(avg / 60, avg % 60), p.avgClockIn)
        // Q6: every day a workday; Absen = a day without check in.
        assertEquals(6, p.workdays)
        assertEquals(p.workdays - p.present, p.absent)
        // No check in in the period: no average.
        assertNull(team.productivity(11, wednesday..wednesday).avgClockIn)
    }
}
