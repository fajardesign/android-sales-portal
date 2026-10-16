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
        // Office of Makassar (same as the web portal OFFICES) first, then the SR's 2 toko (Tamalanrea left with its TL).
        val places = DemoSeed.attendancePlaces(partners, 11, SalesRole.SR)
        assertEquals(listOf("Kantor Amar Bank Makassar", "Jaya Abadi Cellular Mall Panakkukang", "Jaya Abadi Cellular Sudiang"), places.map { it.name })
        assertEquals(-5.1437, places.first().lat, 1e-9)
        assertEquals(119.4297, places.first().lng, 1e-9)
        // TL: office + the toko of their kursi TL (4, Tamalanrea has no TL).
        assertEquals(5, DemoSeed.attendancePlaces(partners, 6, SalesRole.TL).size)
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
    fun deviceZoneIsSavedAndDecides() {
        val original = ActivityRules.systemZone
        try {
            // PRD alignment 2026-10-09: WIB, WITA, WIT from the device; another zone falls back to the area (Makassar WITA).
            ActivityRules.systemZone = { java.time.ZoneId.of("Asia/Jayapura") }
            assertEquals("Asia/Jayapura", ActivityRules.deviceZone(11).id)
            assertEquals("WIT", ActivityRules.label(ActivityRules.deviceZone(11)))
            ActivityRules.systemZone = { java.time.ZoneId.of("UTC") }
            assertEquals("Asia/Makassar", ActivityRules.deviceZone(11).id)
            // The zone saved with the check in decides on time: 09:30 WIB (10:30 WITA) is on time when checked in on WIB.
            val wib = java.time.ZoneId.of("Asia/Jakarta")
            val at = today.atTime(LocalTime.of(9, 30)).atZone(wib).toInstant().toEpochMilli()
            assertEquals(AttendanceStatus.ON_TIME, ActivityRules.attendanceStatus(Attendance(11, today.toString(), Punch(at, 0.0, 0.0, zone = "Asia/Jakarta")), today, today))
            // Saved before zones existed: the area zone (WITA) decides, 10:30 WITA is late.
            assertEquals(AttendanceStatus.LATE, ActivityRules.attendanceStatus(Attendance(11, today.toString(), Punch(at, 0.0, 0.0)), today, today))
            // Visit check-in opens at 12:00 on the device zone: 11:30 WIB is 12:30 WITA but 11:30 on a WIB device.
            ActivityRules.systemZone = { wib }
            val noonWib = today.atTime(LocalTime.of(11, 30)).atZone(wib).toInstant()
            assertTrue(!ActivityRules.visitOpen(noonWib, 11))
        } finally {
            ActivityRules.systemZone = original
        }
    }

    @Test
    fun visitRules() {
        // Check-in opens at 12:00 local time (Makassar WITA for employee 11).
        val original = ActivityRules.systemZone
        ActivityRules.systemZone = { java.time.ZoneId.of("UTC") } // not WIB/WITA/WIT: the area zone (WITA) is used
        assertTrue(!ActivityRules.visitOpen(java.time.Instant.ofEpochMilli(at("11:59")), 11))
        assertTrue(ActivityRules.visitOpen(java.time.Instant.ofEpochMilli(at("12:00")), 11))
        ActivityRules.systemZone = original
        assertEquals(3_000, ActivityRules.VISIT_RADIUS_METERS)
        // PRD alignment 2026-10-09: every toko to visit once a week, grouped by partner (like the web visitWeekStores).
        val monday = LocalDate.of(2026, 10, 5)
        assertEquals(monday, ActivityRules.weekStart(today))
        val partners = PartnerSeed.partners()
        val targets = DemoSeed.storesOf(partners, 11, SalesRole.SR).map { s -> partners.first { p -> p.stores.any { it.id == s.id } } to s }
        val groups = ActivityRules.weekPartners(targets, setOf("REG2026-0139-2"))
        assertEquals(listOf("REG2026-0139"), groups.map { it.partner.id })
        assertTrue(!groups[0].visited)
        assertTrue(ActivityRules.weekPartners(targets, setOf("REG2026-0139-2", "REG2026-0139-3"))[0].visited)
        val week = ActivityRules.visitWeek(monday, groups)
        assertEquals(VisitWeek(monday, 1, 2), week)
        assertTrue(!week.complete)
        assertTrue(ActivityRules.visitWeek(monday, ActivityRules.weekPartners(targets, targets.map { it.second.id }.toSet())).complete)
        // No toko to visit: never complete.
        assertTrue(!VisitWeek(monday, 0, 0).complete)
    }

    @Test
    fun storesPerRoleMatchThePortalSample() {
        val partners = PartnerSeed.partners()
        assertEquals(listOf("REG2026-0139-1"), DemoSeed.storesOf(partners, 15, SalesRole.SA).map { it.id })
        // Tamalanrea was released with its TL by the APL (PRD Scope 1 v1.1 sample).
        assertEquals(listOf("REG2026-0139-2", "REG2026-0139-3"), DemoSeed.storesOf(partners, 11, SalesRole.SR).map { it.id })
        assertEquals(4, DemoSeed.storesOf(partners, 6, SalesRole.TL).size)
    }
}
