package com.example.and_sales_portal.data.activity

import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Operational data of one user in the prototype (PRD v3 data model: `attendance`, `visit_plan`), stored on the device
 * per account by [ActivityRepository]. Partners and toko are shared ([com.example.and_sales_portal.data.partner.PartnerRepository]).
 */
/** Operations of every SA/SR/TL on the device (shared, like partners): attendance, visit plans and days off. */
@Serializable
data class OpsData(
    val version: Int = VERSION,
    val attendance: List<Attendance> = emptyList(),
    val visits: List<Visit> = emptyList(),
    /** `employeeId:date` whose plan exists (generated or edited by the TL), so a day is generated once. */
    val plannedDays: Set<String> = emptySet(),
    /** `employeeId:date` marked as day off by the TL (PRD C7 "mark days off"). */
    val daysOff: Set<String> = emptySet(),
) {
    companion object {
        const val VERSION = 3
        fun key(employeeId: Long, date: LocalDate) = "$employeeId:$date"
    }
}

/** One clock in, clock out, check-in or check-out: time, location and selfie (`attendance`, `visit_plan`). */
@Serializable
data class Punch(
    val at: Long,
    val lat: Double,
    val lng: Double,
    val address: String? = null,
    /** Local file of the selfie (prototype; production uploads it). */
    val selfie: String? = null,
    /** False when saved offline and not yet sent (PRD D3: "an ongoing visit can still check out and sync later"). */
    val synced: Boolean = true,
)

@Serializable
data class Attendance(
    val employeeId: Long,
    /** Work date (ISO, WIB). */
    val date: String,
    val clockIn: Punch? = null,
    val clockOut: Punch? = null,
)

@Serializable
enum class VisitState { SCHEDULED, IN_PROGRESS, DONE, CANCELLED }

@Serializable
data class Visit(
    val id: String,
    /** SA/SR (or TL) who visits. */
    val employeeId: Long,
    val storeId: String,
    val date: String,
    /** Planned time (WIB), `HH:mm`. */
    val plannedTime: String,
    val state: VisitState = VisitState.SCHEDULED,
    val checkIn: Punch? = null,
    val checkOut: Punch? = null,
    val note: String? = null,
    /** TL who planned the visit. */
    val createdBy: Long,
    /** Why it was cancelled (by the TL, or because the partner was unassigned). */
    val cancelReason: String? = null,
)

/** Attendance status shown in the history (PRD D2 "Tepat waktu / Terlambat"; absent days in the history too). */
enum class AttendanceStatus { NOT_YET, ON_TIME, LATE, ABSENT, DAY_OFF }

/** Visit status chip (PRD D3) and outcome (PRD C7). */
enum class VisitStatus { NOT_STARTED, ONGOING, DONE, MISSED, CANCELLED }
enum class VisitOutcome { ON_TIME, LATE }

/**
 * Time rules. Work time and zone follow the PRD (WIB, "23:59 WIB"); the clock-in limit (08:00) is the web portal
 * sample's rule (`admin-sales-portal/src/api/db.js`): the PRD does not define it yet.
 */
object ActivityRules {
    val ZONE: ZoneId = ZoneId.of("Asia/Jakarta")
    /** TODO: confirm with the business; not in the PRD. */
    val CLOCK_IN_LIMIT: LocalTime = LocalTime.of(8, 0)
    /** PRD C7: on time when the check-in is at most 15 minutes after the planned time. */
    val VISIT_TOLERANCE: Duration = Duration.ofMinutes(15)
    /** PRD D3: GPS must match the toko within 50 m. */
    const val VISIT_RADIUS_METERS = 50f
    /** PRD C7: up to 3 visits per day. */
    const val MAX_VISITS_PER_DAY = 3

    /** Times offered when planning a visit (not in the PRD: 30-minute steps of the working day). */
    val PLAN_TIMES: List<String> = (7 * 2..19 * 2).map { "%02d:%02d".format(it / 2, it % 2 * 30) }

    /** Monday to Saturday (same as the web portal sample). */
    fun isWorkday(date: LocalDate) = date.dayOfWeek != DayOfWeek.SUNDAY

    fun attendanceStatus(a: Attendance?, date: LocalDate, today: LocalDate, dayOff: Boolean = false): AttendanceStatus {
        val inAt = a?.clockIn?.at
        return when {
            inAt == null && dayOff -> AttendanceStatus.DAY_OFF
            inAt != null -> if (Instant.ofEpochMilli(inAt).atZone(ZONE).toLocalTime() <= CLOCK_IN_LIMIT) AttendanceStatus.ON_TIME else AttendanceStatus.LATE
            date < today -> AttendanceStatus.ABSENT
            else -> AttendanceStatus.NOT_YET
        }
    }

    fun plannedAt(v: Visit) = LocalDate.parse(v.date).atTime(LocalTime.parse(v.plannedTime)).atZone(ZONE).toInstant()

    fun visitStatus(v: Visit, today: LocalDate): VisitStatus = when (v.state) {
        VisitState.CANCELLED -> VisitStatus.CANCELLED
        VisitState.DONE -> VisitStatus.DONE
        VisitState.IN_PROGRESS -> VisitStatus.ONGOING
        VisitState.SCHEDULED -> if (LocalDate.parse(v.date) < today) VisitStatus.MISSED else VisitStatus.NOT_STARTED
    }

    fun visitOutcome(v: Visit): VisitOutcome? {
        val inAt = v.checkIn?.at ?: return null
        return if (Instant.ofEpochMilli(inAt) <= plannedAt(v).plus(VISIT_TOLERANCE)) VisitOutcome.ON_TIME else VisitOutcome.LATE
    }
}
