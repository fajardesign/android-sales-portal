package com.example.and_sales_portal.data.team

import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.AttendanceStatus
import com.example.and_sales_portal.data.activity.OperationsRepository
import com.example.and_sales_portal.data.activity.VisitOutcome
import com.example.and_sales_portal.data.activity.VisitState
import com.example.and_sales_portal.data.activity.VisitStatus
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.partner.AssignError
import com.example.and_sales_portal.data.partner.PartnerRepository
import java.time.LocalDate

/** This month's attendance and visit results of one person (PRD C5 person detail, C9 Produktivitas). */
data class Productivity(
    val workdays: Int,
    val present: Int,
    val onTime: Int,
    val late: Int,
    val absent: Int,
    val daysOff: Int,
    val visitsPlanned: Int,
    val visitsDone: Int,
    val visitsOnTime: Int,
    val visitsLate: Int,
    val visitsMissed: Int,
    val visitsCancelled: Int,
)

/**
 * The TL's team (Spec: TL-01 … TL-03; PRD v3 C5, C6): team members, assignment with automatic cancellation of future
 * visits, and productivity.
 */
class TeamService(private val partners: PartnerRepository, private val ops: OperationsRepository) {

    /** SA/SR led by [tlId] (active and waiting for activation; disabled accounts are not listed). */
    fun teamOf(tlId: Long): List<DemoDirectory.Person> = DemoDirectory.teamOf(tlId).sortedWith(compareBy({ it.status != "ACTIVE" }, { it.fullName }))

    fun assign(tlId: Long, employeeId: Long, partnerId: String): AssignError? = partners.assign(tlId, employeeId, partnerId)

    /** Unassigns and cancels future visits to the partner's toko (PRD C6). Returns the number of cancelled visits. */
    fun unassign(employeeId: Long, partnerId: String, reason: String): Int {
        val stores = partners.unassign(employeeId, partnerId)
        return ops.cancelFutureVisits(employeeId, stores, reason)
    }

    /** SA reassignment: from the current partner to [toPartnerId]. Returns the error, if any. */
    fun reassign(tlId: Long, employeeId: Long, toPartnerId: String, reason: String): AssignError? {
        val current = partners.assignedPartners(employeeId).filter { it.id != toPartnerId }
        val stores = current.flatMap { partners.unassign(employeeId, it.id) }.toSet()
        val error = partners.assign(tlId, employeeId, toPartnerId)
        if (error != null) {
            current.forEach { partners.assign(tlId, employeeId, it.id) }
            return error
        }
        ops.cancelFutureVisits(employeeId, stores, reason)
        return null
    }

    /** From the 1st of this month to today (Monday–Saturday). */
    fun productivity(employeeId: Long): Productivity {
        val today = ops.today()
        val days = generateSequence(today.withDayOfMonth(1)) { it.plusDays(1) }.takeWhile { !it.isAfter(today) }.filter(ActivityRules::isWorkday).toList()
        val statuses = days.map { ops.attendanceStatus(employeeId, it) }
        val visits = ops.visitsBetween(employeeId, today.withDayOfMonth(1), today)
        val vs = visits.map { ActivityRules.visitStatus(it, today) to ActivityRules.visitOutcome(it) }
        return Productivity(
            workdays = days.count { !ops.isDayOff(employeeId, it) },
            present = statuses.count { it == AttendanceStatus.ON_TIME || it == AttendanceStatus.LATE },
            onTime = statuses.count { it == AttendanceStatus.ON_TIME },
            late = statuses.count { it == AttendanceStatus.LATE },
            absent = statuses.count { it == AttendanceStatus.ABSENT },
            daysOff = statuses.count { it == AttendanceStatus.DAY_OFF },
            visitsPlanned = visits.count { it.state != VisitState.CANCELLED },
            visitsDone = vs.count { it.first == VisitStatus.DONE },
            visitsOnTime = vs.count { it.first == VisitStatus.DONE && it.second == VisitOutcome.ON_TIME },
            visitsLate = vs.count { it.first == VisitStatus.DONE && it.second == VisitOutcome.LATE },
            visitsMissed = vs.count { it.first == VisitStatus.MISSED },
            visitsCancelled = vs.count { it.first == VisitStatus.CANCELLED },
        )
    }

    fun today(): LocalDate = ops.today()
}
