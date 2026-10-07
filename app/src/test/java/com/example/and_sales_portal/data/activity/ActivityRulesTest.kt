package com.example.and_sales_portal.data.activity

import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.partner.PartnerSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/** Attendance and visit rules (PRD v3 C7, D2, D3) and the sample plan (same pattern as the web portal). */
class ActivityRulesTest {

    private val today = LocalDate.of(2026, 10, 7)
    private fun at(time: String, date: LocalDate = today) = date.atTime(LocalTime.parse(time)).atZone(ActivityRules.ZONE).toInstant().toEpochMilli()
    private fun punch(time: String) = Punch(at(time), 0.0, 0.0)

    @Test
    fun attendanceStatus() {
        assertEquals(AttendanceStatus.ON_TIME, ActivityRules.attendanceStatus(Attendance(11, today.toString(), punch("08:00")), today, today))
        assertEquals(AttendanceStatus.LATE, ActivityRules.attendanceStatus(Attendance(11, today.toString(), punch("08:01")), today, today))
        assertEquals(AttendanceStatus.NOT_YET, ActivityRules.attendanceStatus(null, today, today))
        assertEquals(AttendanceStatus.ABSENT, ActivityRules.attendanceStatus(null, today.minusDays(1), today))
        assertEquals(AttendanceStatus.DAY_OFF, ActivityRules.attendanceStatus(null, today.minusDays(1), today, dayOff = true))
    }

    @Test
    fun visitOutcomeAndStatus() {
        val v = Visit("v", 11, "s", today.toString(), "10:00", createdBy = 6)
        assertNull(ActivityRules.visitOutcome(v))
        assertEquals(VisitOutcome.ON_TIME, ActivityRules.visitOutcome(v.copy(checkIn = punch("10:15"))))
        assertEquals(VisitOutcome.LATE, ActivityRules.visitOutcome(v.copy(checkIn = punch("10:16"))))
        assertEquals(VisitStatus.NOT_STARTED, ActivityRules.visitStatus(v, today))
        assertEquals(VisitStatus.MISSED, ActivityRules.visitStatus(v, today.plusDays(1)))
        assertEquals(VisitStatus.ONGOING, ActivityRules.visitStatus(v.copy(state = VisitState.IN_PROGRESS), today))
        assertEquals(VisitStatus.CANCELLED, ActivityRules.visitStatus(v.copy(state = VisitState.CANCELLED), today.plusDays(1)))
    }

    @Test
    fun storesPerRoleMatchThePortalSample() {
        val partners = PartnerSeed.partners()
        assertEquals(listOf("REG2026-0139-1"), DemoSeed.storesOf(partners, 15, SalesRole.SA).map { it.id })
        assertEquals(listOf("REG2026-0139-2", "REG2026-0139-3", "REG2026-0132-2"), DemoSeed.storesOf(partners, 11, SalesRole.SR).map { it.id })
        assertEquals(5, DemoSeed.storesOf(partners, 6, SalesRole.TL).size)
    }

    @Test
    fun dailyPlan() {
        val partners = PartnerSeed.partners()
        val sa = DemoSeed.plan(15, SalesRole.SA, DemoSeed.storesOf(partners, 15, SalesRole.SA), today)
        assertEquals(listOf("10:00"), sa.map { it.plannedTime })
        assertEquals(6L, sa.single().createdBy)
        val sr = DemoSeed.plan(11, SalesRole.SR, DemoSeed.storesOf(partners, 11, SalesRole.SR), today)
        assertEquals(listOf("10:00", "13:00", "16:00"), sr.map { it.plannedTime })
        assertTrue(sr.size <= ActivityRules.MAX_VISITS_PER_DAY)
        val tl = DemoSeed.plan(6, SalesRole.TL, DemoSeed.storesOf(partners, 6, SalesRole.TL), today)
        assertEquals(listOf("14:00"), tl.map { it.plannedTime })
        assertTrue(DemoSeed.plan(15, SalesRole.SA, DemoSeed.storesOf(partners, 15, SalesRole.SA), LocalDate.of(2026, 10, 11)).isEmpty())
    }
}
