package com.example.and_sales_portal.data.activity

import androidx.test.core.app.ApplicationProvider
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.partner.PartnerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/** Attendance and visits saved per account on the device (ATT-01 … ATT-05). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ActivityRepositoryTest {

    private val store = DemoStore(ApplicationProvider.getApplicationContext())
    private var now: Instant = at(LocalDate.of(2026, 10, 7), "09:00")

    private fun at(date: LocalDate, time: String) = date.atTime(LocalTime.parse(time)).atZone(ActivityRules.ZONE).toInstant()
    private fun repo() = ActivityRepository(store, PartnerRepository(store) { now }, 11, SalesRole.SR, 1) { now }

    @Before
    fun clean() = store.reset(11)

    @Test
    fun seedsHistoryAndTodaysPlan() {
        val r = repo()
        assertEquals(3, r.visitsOn(r.today()).size)
        assertTrue(r.visitsOn(r.today()).all { it.state == VisitState.SCHEDULED })
        assertTrue(r.data.value.attendance.isNotEmpty())
        assertTrue(r.data.value.attendance.none { it.date == r.today().toString() })
    }

    @Test
    fun clockInAndOutAreSavedAndReloaded() {
        repo().apply {
            clockIn(Punch(now.toEpochMilli(), -5.1, 119.4))
            clockOut(Punch(now.plusSeconds(3600).toEpochMilli(), -5.1, 119.4))
        }
        val a = repo().attendanceOf(LocalDate.of(2026, 10, 7))
        assertNotNull(a?.clockIn)
        assertNotNull(a?.clockOut)
    }

    @Test
    fun offlineCheckOutIsSyncedLater() {
        val r = repo()
        val v = r.visitsOn(r.today()).first()
        r.checkIn(v.id, Punch(now.toEpochMilli(), 0.0, 0.0))
        r.checkOut(v.id, Punch(now.plusSeconds(1800).toEpochMilli(), 0.0, 0.0, synced = false), "  Stok aman ")
        assertEquals(1, r.pendingSync())
        assertEquals("Stok aman", r.visit(v.id)?.note)
        assertEquals(1, r.syncPending())
        assertEquals(0, r.pendingSync())
        assertEquals(VisitState.DONE, r.visit(v.id)?.state)
    }

    @Test
    fun nextDayGetsItsPlanAndYesterdayIsMissed() {
        val first = repo()
        val yesterday = first.today()
        now = at(yesterday.plusDays(1), "07:00")
        val r = repo()
        assertEquals(3, r.visitsOn(r.today()).size)
        assertTrue(r.visitsOn(yesterday).all { ActivityRules.visitStatus(it, r.today()) == VisitStatus.MISSED })
    }

    @Test
    fun resetRestoresTheSample() {
        val r = repo()
        r.clockIn(Punch(now.toEpochMilli(), 0.0, 0.0))
        r.reset()
        assertEquals(null, r.attendanceOf(r.today()))
        assertEquals(null, repo().attendanceOf(r.today()))
    }
}
