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
        assertEquals(3, siti().visitsOn(wednesday).size)
        assertEquals(1, ops.visitsOf(15, wednesday).size)
        assertTrue(ops.data.value.attendance.any { it.employeeId == 6L })
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
        val v = r.visitsOn(wednesday).first()
        r.checkIn(v.id, Punch(now.toEpochMilli(), 0.0, 0.0))
        r.checkOut(v.id, Punch(now.plusSeconds(1800).toEpochMilli(), 0.0, 0.0, synced = false), "  Stok aman ")
        assertEquals(1, r.pendingSync())
        assertEquals("Stok aman", r.visit(v.id)?.note)
        assertEquals(1, r.syncPending())
        assertEquals(0, r.pendingSync())
    }

    @Test
    fun nextDayGetsItsPlanAndYesterdayIsMissed() {
        now = at(wednesday.plusDays(1), "07:00")
        ops.ensurePlanned()
        assertEquals(3, ops.visitsOf(11, wednesday.plusDays(1)).size)
        assertTrue(ops.visitsOf(11, wednesday).all { ActivityRules.visitStatus(it, wednesday.plusDays(1)) == VisitStatus.MISSED })
    }

    @Test
    fun planningRules() {
        val friday = wednesday.plusDays(2)
        val store = "REG2026-0139-2"
        assertNull(ops.addVisit(11, friday, store, "09:00", 6))
        assertEquals(PlanError.DuplicateTime, ops.addVisit(11, friday, "REG2026-0139-3", "09:00", 6))
        assertEquals(PlanError.StoreNotAssigned, ops.addVisit(11, friday, "REG2026-0139-1", "10:00", 6))
        assertEquals(PlanError.PastDate, ops.addVisit(11, wednesday.minusDays(1), store, "10:00", 6))
        assertEquals(PlanError.PastDate, ops.addVisit(11, wednesday, store, "08:00", 6))
        assertNull(ops.addVisit(11, friday, store, "11:00", 6))
        assertNull(ops.addVisit(11, friday, store, "13:00", 6))
        assertEquals(PlanError.MaxPerDay, ops.addVisit(11, friday, store, "15:00", 6))
    }

    @Test
    fun dayOffCancelsScheduledVisitsAndBlocksPlanning() {
        ops.setDayOff(11, wednesday, true, "Hari libur")
        assertTrue(ops.visitsOf(11, wednesday).all { it.state == VisitState.CANCELLED })
        assertEquals(PlanError.DayOff, ops.addVisit(11, wednesday, "REG2026-0139-2", "17:00", 6))
        assertEquals(AttendanceStatus.DAY_OFF, ops.attendanceStatus(11, wednesday))
    }

    @Test
    fun copyLastWeekFillsEmptyDaysFromToday() {
        val monday = wednesday.minusDays(2)
        // Wednesday already has its plan; Thursday to Saturday are empty and get last week's visits.
        val expected = (1L..3L).sumOf { d -> ops.visitsOf(11, wednesday.plusDays(d).minusWeeks(1)).count { it.state != VisitState.CANCELLED } }
        assertEquals(expected, ops.copyLastWeek(11, monday, 6))
        assertTrue(expected > 0)
        assertEquals(3, ops.visitsOf(11, wednesday).size)
    }

    @Test
    fun editAndCancelBeforeCheckIn() {
        val v = ops.visitsOf(11, wednesday).last()
        assertNull(ops.editVisit(v.id, v.storeId, "18:00"))
        assertEquals("18:00", ops.visit(v.id)?.plannedTime)
        ops.cancelVisit(v.id, "Dibatalkan oleh Team Leader")
        assertEquals(VisitState.CANCELLED, ops.visit(v.id)?.state)
        val started = ops.visitsOf(11, wednesday).first()
        ops.checkIn(started.id, Punch(now.toEpochMilli(), 0.0, 0.0))
        assertEquals(PlanError.AlreadyStarted, ops.editVisit(started.id, started.storeId, "19:00"))
    }
}
