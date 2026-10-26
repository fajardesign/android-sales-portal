package com.example.and_sales_portal.data.activity

import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.device.Place
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.Store
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
/** Operations of every SA/SR/TL on the device (shared, like partners): attendance and visits. */
@Serializable
data class OpsData(
    val version: Int = VERSION,
    val attendance: List<Attendance> = emptyList(),
    val visits: List<Visit> = emptyList(),
) {
    companion object {
        /** 9: area zone (S8) and Absen without check out (S13) again, decisions 2026-10-20. */
        const val VERSION = 11
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
    /**
     * Zone of the user's area at the punch (`Asia/Makassar`, …; PRD Scope 2 S8: server time in the area zone, confirmed
     * 2026-10-20); times are shown in it.
     */
    val zone: String? = null,
    /** Device zone at the punch, kept for audit only (never decides a status). */
    val deviceZone: String? = null,
)

@Serializable
data class Attendance(
    val employeeId: Long,
    /** Work date (ISO), in the zone of the user's area (S8). */
    val date: String,
    val clockIn: Punch? = null,
    val clockOut: Punch? = null,
    /** Status set by the TL ([corrections]); `null` = from the check in. */
    val statusOverride: AttendanceStatus? = null,
    /** Check-in time set by the TL ([corrections]); `null` = the punch time. */
    val clockInOverride: Long? = null,
    /** Koreksi absensi oleh TL (decision 2026-10-19), oldest first. */
    val corrections: List<AttendanceCorrection> = emptyList(),
) {
    /** Check-in time that counts: the TL's correction, else the punch; `null` when Absen. */
    val checkInAt: Long? get() = if (statusOverride == AttendanceStatus.ABSENT) null else clockInOverride ?: clockIn?.at
    val hasCheckIn: Boolean get() = checkInAt != null
    val corrected: Boolean get() = corrections.isNotEmpty()
}

/** One correction by the TL: who and when, the old and new status and check-in time, and the reason (required). */
@Serializable
data class AttendanceCorrection(
    val at: Long,
    val by: Long,
    val fromStatus: AttendanceStatus,
    val fromClockIn: Long?,
    val toStatus: AttendanceStatus,
    val toClockIn: Long?,
    val reason: String,
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
 * Attendance status: Belum Check In, Tepat Waktu (≤ 10:00 in the zone of the user's area, S8), Terlambat, Absen (PRD
 * Scope 2 S13, decision 2026-10-20: no check in, or a check in without check out before 23:59 that day). Every day is a
 * workday, weekends, holidays and leave included (Q6). "Sudah Check In" / "Sudah Check Out" come from [Attendance].
 */
@Serializable
/**
 * Status of a day. [ABSENT] "Absen" = a check in without check out before 23:59 (S13, a violation); [NO_CHECKIN]
 * "Tidak check in" = a past day without check in (OQ-S2-03, decision 2026-10-20, neutral). Neither counts toward the 6.
 */
enum class AttendanceStatus { NOT_YET, ON_TIME, LATE, ABSENT, NO_CHECKIN }

/** Attendance of one week (Monday–Sunday): days present (not Absen, S13) over the target of 6 (Q6). */
data class AttendanceWeek(val start: LocalDate, val checkIns: Int, val target: Int = ActivityRules.WEEKLY_CHECKINS) {
    val met: Boolean get() = checkIns >= target
}

/** Visit status chip (PRD D3): Berlangsung (check-in today, not checked out), Selesai (checked out, or a past day without check-out). */
enum class VisitStatus { ONGOING, DONE }

/**
 * Visits of one week (Monday–Sunday), same as the web portal `visitWeekStores`: every toko of the user's kursi held on
 * Monday 00:00 WIB (decision 2026-10-19, Q5) is visited once that week, any day, several toko a day allowed. [visited] =
 * toko of the target visited by this person that week, [target] = toko to visit. "Lengkap n/n" or "Belum lengkap n/m".
 */
data class VisitWeek(val start: LocalDate, val visited: Int, val target: Int) {
    val complete: Boolean get() = target > 0 && visited >= target
}

/**
 * A toko of the week with whether it was visited (one visit per toko per week, PRD Scope 2 S12). [counted] = false: held
 * now but not in this week's target (it came after Monday 00:00 WIB, Q5), counted from next week; it can be visited.
 */
data class WeekStore(val store: Store, val visited: Boolean, val counted: Boolean = true)

/** A partner's toko of the week; visited when all its counted toko are ("Sudah dikunjungi minggu ini"). */
data class WeekPartner(val partner: Partner, val stores: List<WeekStore>) {
    val visited: Boolean get() = stores.filter { it.counted }.ifEmpty { stores }.all { it.visited }
}

/**
 * Time and place rules: the server time in the **zone of the user's area** (WIB/WITA/WIT, PRD Scope 2 S8, decision
 * 2026-10-20) decides Tepat Waktu (≤ 10:00), the 12:00 visit start, 23:59, dates and weeks; every day is a workday; 6
 * days present a week; the visit week runs Monday–Sunday and its target is locked on Monday 00:00 WIB. Visit check-in
 * within 3 km of the toko, once per toko a week.
 */
object ActivityRules {
    /** Indonesian zones and their labels (WIB, WITA, WIT). */
    val ZONE_LABELS: Map<String, String> = mapOf(
        "Asia/Jakarta" to "WIB", "Asia/Pontianak" to "WIB", "Asia/Makassar" to "WITA", "Asia/Jayapura" to "WIT",
    )

    /** The device's zone, saved with each punch for audit only (replaceable in tests). */
    @Volatile
    var systemZone: () -> ZoneId = { ZoneId.systemDefault() }

    /** Display zone: the signed-in user's area. */
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
        ZONE_LABEL = label(ZONE)
    }

    /** Zone of a person's area (S8): decides every status, date and week. */
    fun zoneOf(employeeId: Long): ZoneId = DemoDirectory.person(employeeId)?.let { DemoDirectory.area(it.areaId) }?.zone ?: ZONE

    /** Zone saved with a punch (the area zone); punches saved before it carry none and use the person's area. */
    fun zoneOf(punch: Punch, employeeId: Long): ZoneId = punch.zone?.let { runCatching { ZoneId.of(it) }.getOrNull() } ?: zoneOf(employeeId)

    /** Q6: 6 check-ins a week (Monday–Sunday), any day. */
    const val WEEKLY_CHECKINS = 6

    /** Q5: the visit target is locked on Monday 00:00 WIB. */
    val LOCK_ZONE: ZoneId = ZoneId.of("Asia/Jakarta")

    /** GPS accuracy limit (decision 2026-10-19, #13): a fix worse than 100 m is refused, no override. */
    const val MAX_ACCURACY_METERS = 100

    /** "WIB" / "WITA" / "WIT"; another zone shows its offset ("UTC+08:00"). */
    fun label(zone: ZoneId): String = ZONE_LABELS[zone.id] ?: "UTC${zone.rules.getOffset(Instant.now())}"

    /** Check in kerja on time up to 10:00 in the zone of the user's area (S8), by the minute like the web portal. */
    val CLOCK_IN_LIMIT: LocalTime = LocalTime.of(10, 0)
    /** Revision 2026-10-08: check in and check out kerja within 3 km of an office of the area or a toko. */
    const val ATTENDANCE_RADIUS_METERS = 3_000
    /** Revision 2026-10-08: visit check-in opens at 12:00 in the zone of the user's area (S8). */
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

    /**
     * Status of [date]: the TL's correction first; else, once the day is over, "Tidak check in" without check in
     * (OQ-S2-03) and Absen with a check in but no check out (S13, decision 2026-10-20); else Tepat Waktu / Terlambat from the check-in time in the zone of the user's area.
     */
    fun attendanceStatus(a: Attendance?, date: LocalDate, today: LocalDate): AttendanceStatus {
        a?.statusOverride?.let { return it }
        val inAt = a?.checkInAt
        return when {
            inAt == null -> if (date < today) AttendanceStatus.NO_CHECKIN else AttendanceStatus.NOT_YET
            a.clockOut == null && date < today -> AttendanceStatus.ABSENT
            Instant.ofEpochMilli(inAt).atZone(zoneOf(a.employeeId)).toLocalTime().truncatedTo(ChronoUnit.MINUTES) <= CLOCK_IN_LIMIT -> AttendanceStatus.ON_TIME
            else -> AttendanceStatus.LATE
        }
    }

    /** "Tidak check out": a past day with a check in and no check out, shown next to Absen (S13). */
    fun noCheckOut(a: Attendance?, date: LocalDate, today: LocalDate): Boolean = a != null && a.hasCheckIn && a.clockOut == null && date < today

    /** A day that counts toward the 6 check-ins of the week: present, neither Absen (S13) nor "Tidak check in" (OQ-S2-03). */
    fun present(a: Attendance?, date: LocalDate, today: LocalDate): Boolean = attendanceStatus(a, date, today).let { it == AttendanceStatus.ON_TIME || it == AttendanceStatus.LATE }

    /** Visit check-in is open from [VISIT_START] in the zone of the user's area (S8). */
    fun visitOpen(now: Instant, zone: ZoneId): Boolean = !now.atZone(zone).toLocalTime().isBefore(VISIT_START)

    /** Check-out is optional (S13): a visit of an earlier day without check-out is Selesai ("Tanpa check-out"). */
    fun visitStatus(v: Visit, today: LocalDate): VisitStatus =
        if (v.state == VisitState.DONE || LocalDate.parse(v.date) < today) VisitStatus.DONE else VisitStatus.ONGOING

    /** Monday of [date]'s week. */
    fun weekStart(date: LocalDate): LocalDate = date.with(DayOfWeek.MONDAY)

    /** Last day (Sunday) of the week starting [weekStart] (Q5/Q6: Monday–Sunday). */
    fun weekEnd(weekStart: LocalDate): LocalDate = weekStart.plusDays(6)

    /** Monday 00:00 WIB of the week starting [weekStart]: the visit target is the toko held at that instant (Q5). */
    fun weekLock(weekStart: LocalDate): Long = weekStart.atStartOfDay(LOCK_ZONE).toInstant().toEpochMilli()

    /** Toko of the week grouped by partner. [countedStoreIds] `null` = every toko counts (the target itself). */
    fun weekPartners(targets: List<Pair<Partner, Store>>, visitedStoreIds: Set<String>, countedStoreIds: Set<String>? = null): List<WeekPartner> =
        targets.groupBy({ it.first.id }, { it }).values.map { list ->
            WeekPartner(list.first().first, list.map { (_, s) -> WeekStore(s, s.id in visitedStoreIds, countedStoreIds?.contains(s.id) ?: true) })
        }

    fun visitWeek(weekStart: LocalDate, groups: List<WeekPartner>): VisitWeek {
        val stores = groups.flatMap { it.stores }.filter { it.counted }
        return VisitWeek(weekStart, stores.count { it.visited }, stores.size)
    }
}
