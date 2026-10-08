package com.example.and_sales_portal.data.activity

import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.partner.PartnerSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/** Attendance and visit rules (PRD v3 C7, D2, D3) and the sample plan (same pattern as the web portal). */
class ActivityRulesTest {

    private val today = LocalDate.of(2026, 10, 7)
    /** Local time of employee 11 (Makassar, WITA). */
    private fun at(time: String, date: LocalDate = today) = date.atTime(LocalTime.parse(time)).atZone(ActivityRules.zoneOf(11)).toInstant().toEpochMilli()
    private fun punch(time: String) = Punch(at(time), 0.0, 0.0)

    @Test
    fun attendanceStatus() {
        assertEquals(AttendanceStatus.ON_TIME, ActivityRules.attendanceStatus(Attendance(11, today.toString(), punch("10:00")), today, today))
        assertEquals(AttendanceStatus.ON_TIME, ActivityRules.attendanceStatus(Attendance(11, today.toString(), punch("10:00:59")), today, today))
        assertEquals(AttendanceStatus.LATE, ActivityRules.attendanceStatus(Attendance(11, today.toString(), punch("10:01")), today, today))
        // Late check in is still allowed and recorded; no check in by the end of the day = Absen.
        assertEquals(AttendanceStatus.LATE, ActivityRules.attendanceStatus(Attendance(11, today.toString(), punch("15:30")), today, today))
        assertEquals(AttendanceStatus.NOT_YET, ActivityRules.attendanceStatus(null, today, today))
        assertEquals(AttendanceStatus.ABSENT, ActivityRules.attendanceStatus(null, today.minusDays(1), today))
        // Local time decides: 10:00 WIB is 11:00 WITA in Makassar, so late.
        val wib = today.atTime(LocalTime.of(10, 0)).atZone(java.time.ZoneId.of("Asia/Jakarta")).toInstant().toEpochMilli()
        assertEquals(AttendanceStatus.LATE, ActivityRules.attendanceStatus(Attendance(11, today.toString(), Punch(wib, 0.0, 0.0)), today, today))
        assertEquals("WITA", com.example.and_sales_portal.data.demo.DemoDirectory.area(1)?.zoneLabel)
        assertEquals(AttendanceStatus.DAY_OFF, ActivityRules.attendanceStatus(null, today.minusDays(1), today, dayOff = true))
    }

    @Test
    fun attendancePlacesAndRadius() {
        val partners = PartnerSeed.partners()
        // Office of Makassar (same as the web portal OFFICES) first, then the SR's 3 toko.
        val places = DemoSeed.attendancePlaces(partners, 11, SalesRole.SR)
        assertEquals(listOf("Kantor Amar Bank Makassar", "Jaya Abadi Cellular Mall Panakkukang", "Jaya Abadi Cellular Sudiang", "PT Bintang Gadget Store Tamalanrea"), places.map { it.name })
        assertEquals(-5.1437, places.first().lat, 1e-9)
        assertEquals(119.4297, places.first().lng, 1e-9)
        // TL: office + the toko of the partners they own.
        assertEquals(6, DemoSeed.attendancePlaces(partners, 6, SalesRole.TL).size)
        // Haversine like the portal: 0.027 deg latitude is about 3 km.
        val office = places.first()
        assertEquals(3002, ActivityRules.distanceMeters(office.lat, office.lng, office.lat + 0.027, office.lng))
        val (near, d) = ActivityRules.nearest(places, office.lat + 0.001, office.lng)!!
        assertEquals("Kantor Amar Bank Makassar", near.name)
        assertTrue(d <= ActivityRules.ATTENDANCE_RADIUS_METERS)
        // The "Di luar radius" simulation (0.06 deg north-east of the office) is outside 3 km of every place.
        assertTrue(ActivityRules.nearest(places, office.lat + 0.06, office.lng + 0.06)!!.second > ActivityRules.ATTENDANCE_RADIUS_METERS)
    }

    @Test
    fun visitRules() {
        // Check-in opens at 12:00 local time (Makassar WITA for employee 11).
        assertTrue(!ActivityRules.visitOpen(java.time.Instant.ofEpochMilli(at("11:59")), 11))
        assertTrue(ActivityRules.visitOpen(java.time.Instant.ofEpochMilli(at("12:00")), 11))
        assertEquals(3_000, ActivityRules.VISIT_RADIUS_METERS)
        // Weekly target: Monday–Saturday minus days off; one visit counts per day.
        val monday = LocalDate.of(2026, 10, 5)
        assertEquals(monday, ActivityRules.weekStart(today))
        val two = ActivityRules.visitWeek(monday, setOf(monday, monday.plusDays(1))) { false }
        assertEquals(VisitWeek(monday, 2, 6), two)
        assertTrue(!two.complete)
        val all = (0L..5L).map { monday.plusDays(it) }.toSet()
        assertTrue(ActivityRules.visitWeek(monday, all) { false }.complete)
        // Sunday visits do not count; a day off leaves the target.
        assertEquals(VisitWeek(monday, 5, 5), ActivityRules.visitWeek(monday, all - monday.plusDays(5) + monday.plusDays(6)) { it == monday.plusDays(5) })
    }

    @Test
    fun storesPerRoleMatchThePortalSample() {
        val partners = PartnerSeed.partners()
        assertEquals(listOf("REG2026-0139-1"), DemoSeed.storesOf(partners, 15, SalesRole.SA).map { it.id })
        assertEquals(listOf("REG2026-0139-2", "REG2026-0139-3", "REG2026-0132-2"), DemoSeed.storesOf(partners, 11, SalesRole.SR).map { it.id })
        assertEquals(5, DemoSeed.storesOf(partners, 6, SalesRole.TL).size)
    }
}
