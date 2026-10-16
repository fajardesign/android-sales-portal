package com.example.and_sales_portal.data.team

import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.OperationsRepository
import com.example.and_sales_portal.data.activity.VisitWeek
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.partner.AssignError
import com.example.and_sales_portal.data.partner.PartnerRepository
import java.time.Instant
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Attendance and visit results of one person in a period (PRD C5 person detail, C9 Produktivitas; stakeholder revision
 * 2026-10-08: visits only as weekly target fulfilment, the same on every platform, review 2026-10-09).
 */
data class Productivity(
    val workdays: Int,
    val present: Int,
    val onTime: Int,
    val late: Int,
    /** No check in, or check in without check out (PRD Scope 2 S13). */
    val absent: Int,
    /** Weeks of the period: Monday in the period, up to today (same as the web portal). */
    val weeks: List<VisitWeek>,
    /** The current week (Monday–Saturday), always shown ("Kunjungan minggu ini"). */
    val thisWeek: VisitWeek,
    /** Average check in kerja time of the present days, "09:42 WITA" (TL-07, PRD alignment 2026-10-09); `null` with none. */
    val avgClockIn: String? = null,
)

/**
 * The TL's team (Spec: TL-01 … TL-03; PRD v3 C5, C6): team members, assignment per toko, and productivity.
 */
class TeamService(private val partners: PartnerRepository, private val ops: OperationsRepository) {

    /** SA/SR led by [tlId] (active and waiting for activation; disabled accounts are not listed). */
    fun teamOf(tlId: Long): List<DemoDirectory.Person> = DemoDirectory.teamOf(tlId).sortedWith(compareBy({ it.status != "ACTIVE" }, { it.fullName }))

    fun assign(tlId: Long, employeeId: Long, storeId: String): AssignError? = partners.assign(tlId, employeeId, storeId)

    fun unassign(employeeId: Long, storeId: String, by: Long) = partners.unassign(employeeId, storeId, by)

    /** From the 1st of this month to today (Monday–Saturday). */
    fun productivity(employeeId: Long): Productivity = productivity(employeeId, ops.today().withDayOfMonth(1)..ops.today())

    /** Attendance up to today and visit weeks in [range] (PRD C9 daily, weekly, monthly). */
    fun productivity(employeeId: Long, range: ClosedRange<LocalDate>): Productivity {
        val today = ops.today()
        val end = minOf(range.endInclusive, today)
        val days = generateSequence(range.start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }.filter(ActivityRules::isWorkday).toList()
        val statuses = days.map { ops.attendanceStatus(employeeId, it) }
        // Like the web APL report: each check in read in its own saved zone, labelled with the first one's zone.
        val punches = days.mapNotNull { ops.attendanceOf(employeeId, it)?.clockIn }
        val minutes = punches.map { p -> Instant.ofEpochMilli(p.at).atZone(ActivityRules.zoneOf(p, employeeId)).toLocalTime().let { it.hour * 60 + it.minute } }
        val avgClockIn = if (minutes.isEmpty()) null else (minutes.sum().toDouble() / minutes.size).roundToInt().let { m ->
            "%02d:%02d %s".format(m / 60, m % 60, ActivityRules.label(ActivityRules.zoneOf(punches.first(), employeeId)))
        }
        return Productivity(
            // S10: Monday–Saturday are workdays, public holidays and leave included.
            workdays = days.size,
            present = statuses.count { it == AttendanceStatus.ON_TIME || it == AttendanceStatus.LATE },
            onTime = statuses.count { it == AttendanceStatus.ON_TIME },
            late = statuses.count { it == AttendanceStatus.LATE },
            absent = statuses.count { it == AttendanceStatus.ABSENT },
            weeks = ops.visitWeeks(employeeId, range.start..range.endInclusive),
            thisWeek = ops.visitWeek(employeeId, ActivityRules.weekStart(today)),
            avgClockIn = avgClockIn,
        )
    }

    fun today(): LocalDate = ops.today()
}
