package com.example.and_sales_portal.data.activity

import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.device.Place
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Operational data of one user in the prototype (PRD v3 data model: `attendance`, `visit_plan`), stored on the device
 * per account by [ActivityRepository]. Partners and toko are shared ([com.example.and_sales_portal.data.partner.PartnerRepository]).
 */
/** Operations of every SA/SR/TL on the device (shared, like partners): attendance, visits and days off. */
@Serializable
data class OpsData(
    val version: Int = VERSION,
    val attendance: List<Attendance> = emptyList(),
    val visits: List<Visit> = emptyList(),
    /** `employeeId:date` marked as day off by the TL (PRD C7 "mark days off"). */
    val daysOff: Set<String> = emptySet(),
) {
    companion object {
        /** 5: visits without hourly plans (stakeholder revision 2026-10-08). */
        const val VERSION = 5
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
    /** Office or toko the location was validated against (attendance, revision 2026-10-08). */
    val place: String? = null,
    /** Metres to [place]. */
    val distance: Int? = null,
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
enum class VisitState { IN_PROGRESS, DONE }

/**
 * A visit check-in at a toko (revision 2026-10-08: no hourly plan; the SA/SR/TL checks in at any of their toko from
 * 12:00 local time). Created at check-in.
 */
@Serializable
data class Visit(
    val id: String,
    /** SA/SR (or TL) who visits. */
    val employeeId: Long,
    val storeId: String,
    val date: String,
    val state: VisitState = VisitState.IN_PROGRESS,
    val checkIn: Punch? = null,
    val checkOut: Punch? = null,
    val note: String? = null,
)

/**
 * Attendance status (revision 2026-10-08 labels): Belum Check In, Tepat Waktu (≤ 10:00 local), Terlambat, Absen (no
 * check in by 23:59 local), Libur. "Sudah Check In" / "Sudah Check Out" come from [Attendance] itself.
 */
enum class AttendanceStatus { NOT_YET, ON_TIME, LATE, ABSENT, DAY_OFF }

/** Visit status chip (PRD D3): Berlangsung, Selesai. */
enum class VisitStatus { ONGOING, DONE }

/**
 * Visits of one week (Monday–Saturday): days with a visit over the target (workdays minus days off). At most one visit
 * counts per day and a missed day cannot be made up (revision 2026-10-08). "Lengkap n/n" or "Belum lengkap n/m".
 */
data class VisitWeek(val start: LocalDate, val visited: Int, val target: Int) {
    val complete: Boolean get() = target > 0 && visited >= target
}

/**
 * Time and place rules. Check in kerja is on time up to 10:00 **local time** of the person's area (Makassar WITA,
 * Jabodetabek WIB, …; decision 2026-10-08). Stakeholder revision 2026-10-08: visit check-in from 12:00 local time within
 * 3 km of the toko, no hourly plan, weekly target of one visit per workday.
 */
object ActivityRules {
    /** Display zone: the signed-in user's area (set when the session starts); WIB until then. */
    @Volatile
    var ZONE: ZoneId = ZoneId.of("Asia/Jakarta")
        private set

    /** "WIB", "WITA" or "WIT" of [ZONE]. */
    @Volatile
    var ZONE_LABEL: String = "WIB"
        private set

    fun useArea(areaId: Int?) {
        val area = areaId?.let { DemoDirectory.area(it) } ?: return
        ZONE = area.zone
        ZONE_LABEL = area.zoneLabel
    }

    /** Local zone of a person's area. */
    fun zoneOf(employeeId: Long): ZoneId = DemoDirectory.person(employeeId)?.let { DemoDirectory.area(it.areaId) }?.zone ?: ZONE

    /** Check in kerja on time up to 10:00 local time (decision 2026-10-08), by the minute like the web portal. */
    val CLOCK_IN_LIMIT: LocalTime = LocalTime.of(10, 0)
    /** Revision 2026-10-08: check in and check out kerja within 3 km of an office of the area or a toko. */
    const val ATTENDANCE_RADIUS_METERS = 3_000
    /** Revision 2026-10-08: visit check-in opens at 12:00 local time. */
    val VISIT_START: LocalTime = LocalTime.of(12, 0)
    /** Revision 2026-10-08: visit check-in within 3 km of the toko (was 50 m). */
    const val VISIT_RADIUS_METERS = 3_000

    /** Distance in metres (haversine, same formula as the web portal `distanceKm`). */
    fun distanceMeters(aLat: Double, aLng: Double, bLat: Double, bLng: Double): Int {
        val r = 6_371_000.0
        val dLat = Math.toRadians(bLat - aLat)
        val dLng = Math.toRadians(bLng - aLng)
        val h = sin(dLat / 2).pow(2) + cos(Math.toRadians(aLat)) * cos(Math.toRadians(bLat)) * sin(dLng / 2).pow(2)
        return (2 * r * asin(sqrt(h))).roundToInt()
    }

    /** The closest of [places] to a position, with its distance in metres. */
    fun nearest(places: List<Place>, lat: Double, lng: Double): Pair<Place, Int>? =
        places.map { it to distanceMeters(lat, lng, it.lat, it.lng) }.minByOrNull { it.second }

    /** Monday to Saturday (same as the web portal sample). */
    fun isWorkday(date: LocalDate) = date.dayOfWeek != DayOfWeek.SUNDAY

    fun attendanceStatus(a: Attendance?, date: LocalDate, today: LocalDate, dayOff: Boolean = false, zone: ZoneId = a?.let { zoneOf(it.employeeId) } ?: ZONE): AttendanceStatus {
        val inAt = a?.clockIn?.at
        return when {
            inAt == null && dayOff -> AttendanceStatus.DAY_OFF
            inAt != null -> if (Instant.ofEpochMilli(inAt).atZone(zone).toLocalTime().truncatedTo(ChronoUnit.MINUTES) <= CLOCK_IN_LIMIT) AttendanceStatus.ON_TIME else AttendanceStatus.LATE
            date < today -> AttendanceStatus.ABSENT
            else -> AttendanceStatus.NOT_YET
        }
    }

    /** Visit check-in is open from [VISIT_START] local time of the person's area. */
    fun visitOpen(now: Instant, employeeId: Long): Boolean = !now.atZone(zoneOf(employeeId)).toLocalTime().isBefore(VISIT_START)

    fun visitStatus(v: Visit): VisitStatus = if (v.state == VisitState.DONE) VisitStatus.DONE else VisitStatus.ONGOING

    /** Monday of [date]'s week. */
    fun weekStart(date: LocalDate): LocalDate = date.with(DayOfWeek.MONDAY)

    /** Weekly target and progress (see [VisitWeek]); [visitDates] are the dates with at least one visit. */
    fun visitWeek(weekStart: LocalDate, visitDates: Set<LocalDate>, isDayOff: (LocalDate) -> Boolean): VisitWeek {
        val days = (0L..5L).map { weekStart.plusDays(it) }.filter { !isDayOff(it) }
        return VisitWeek(weekStart, days.count { it in visitDates }, days.size)
    }
}
