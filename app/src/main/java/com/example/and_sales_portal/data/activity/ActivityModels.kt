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
        /** 7: area zone on every punch, no days off, Absen without check out (PRD Scope 2, 2.0). */
        const val VERSION = 7
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
    /** Zone of the user's area at the punch (`Asia/Makassar`, …; PRD Scope 2 S8: server time in the area zone); times are shown in it. */
    val zone: String? = null,
    /** Device zone at the punch, kept for audit only (S8: never decides a status). */
    val deviceZone: String? = null,
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
 * Attendance status: Belum Check In, Tepat Waktu (≤ 10:00 area time), Terlambat, Absen (PRD Scope 2 S13: no check in, or
 * check in without check out before 23:59 that day), Bukan hari kerja (Sunday). No Libur (S10: Monday–Saturday,
 * public holidays and leave included, are workdays). "Sudah Check In" / "Sudah Check Out" come from [Attendance] itself.
 */
enum class AttendanceStatus { NOT_YET, ON_TIME, LATE, ABSENT, NOT_WORKDAY }

/** Visit status chip (PRD D3): Berlangsung (check-in today, not checked out), Selesai (checked out, or a past day without check-out). */
enum class VisitStatus { ONGOING, DONE }

/**
 * Visits of one week (Monday–Saturday), PRD Scope 2 §2.2 (same as the web portal `visitWeekStores`): every toko of the
 * user's kursi (SA/SR or TL) is visited once a week, any day, several toko a day allowed. [visited] = toko visited that
 * week, [target] = toko to visit. "Lengkap n/n" or "Belum lengkap n/m".
 */
data class VisitWeek(val start: LocalDate, val visited: Int, val target: Int) {
    val complete: Boolean get() = target > 0 && visited >= target
}

/** A toko of the week with whether it was visited (one visit per toko per week, PRD Scope 2 S12). */
data class WeekStore(val store: Store, val visited: Boolean)

/** A partner's toko of the week; the partner counts as visited when all its toko are ("Sudah dikunjungi minggu ini"). */
data class WeekPartner(val partner: Partner, val stores: List<WeekStore>) {
    val visited: Boolean get() = stores.all { it.visited }
}

/**
 * Time and place rules. PRD Scope 2 S8: every status uses the server time (prototype: the app clock) in the **zone of the
 * user's area** (Makassar WITA, Jakarta WIB, …), never the device zone, which is only kept for audit. Check in kerja on
 * time up to 10:00; visit check-in from 12:00 within 3 km of the toko; one visit per toko per week.
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

    /** Zone of a person's area (S8: decides every status). */
    fun zoneOf(employeeId: Long): ZoneId = DemoDirectory.person(employeeId)?.let { DemoDirectory.area(it.areaId) }?.zone ?: ZONE

    /** Zone saved with a punch (the area zone); punches saved before it carry none and use the person's area. */
    fun zoneOf(punch: Punch, employeeId: Long): ZoneId = punch.zone?.let { runCatching { ZoneId.of(it) }.getOrNull() } ?: zoneOf(employeeId)

    /** "WIB" / "WITA" / "WIT"; another zone shows its offset ("UTC+08:00"). */
    fun label(zone: ZoneId): String = ZONE_LABELS[zone.id] ?: "UTC${zone.rules.getOffset(Instant.now())}"

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

    /**
     * Status of [date] in the area [zone] (S8). Absen (S13): no check in, or a check in without check out once the day
     * is over; today without check out is still Tepat Waktu / Terlambat.
     */
    fun attendanceStatus(a: Attendance?, date: LocalDate, today: LocalDate, zone: ZoneId = a?.let { zoneOf(it.employeeId) } ?: ZONE): AttendanceStatus {
        val inAt = a?.clockIn?.at
        return when {
            inAt == null && !isWorkday(date) -> AttendanceStatus.NOT_WORKDAY
            inAt != null && a.clockOut == null && date < today -> AttendanceStatus.ABSENT
            inAt != null -> if (Instant.ofEpochMilli(inAt).atZone(zone).toLocalTime().truncatedTo(ChronoUnit.MINUTES) <= CLOCK_IN_LIMIT) AttendanceStatus.ON_TIME else AttendanceStatus.LATE
            date < today -> AttendanceStatus.ABSENT
            else -> AttendanceStatus.NOT_YET
        }
    }

    /** Visit check-in is open from [VISIT_START] in the zone of the person's area (S8). */
    fun visitOpen(now: Instant, employeeId: Long): Boolean = !now.atZone(zoneOf(employeeId)).toLocalTime().isBefore(VISIT_START)

    /** Check-out is optional (S13): a visit of an earlier day without check-out is Selesai ("Tanpa check-out"). */
    fun visitStatus(v: Visit, today: LocalDate): VisitStatus =
        if (v.state == VisitState.DONE || LocalDate.parse(v.date) < today) VisitStatus.DONE else VisitStatus.ONGOING

    /** Monday of [date]'s week. */
    fun weekStart(date: LocalDate): LocalDate = date.with(DayOfWeek.MONDAY)

    /** Last day (Saturday) of the week starting [weekStart]. */
    fun weekEnd(weekStart: LocalDate): LocalDate = weekStart.plusDays(5)

    /** Toko of the week grouped by partner, from the toko to visit and the toko visited that week. */
    fun weekPartners(targets: List<Pair<Partner, Store>>, visitedStoreIds: Set<String>): List<WeekPartner> =
        targets.groupBy({ it.first.id }, { it }).values.map { list ->
            WeekPartner(list.first().first, list.map { (_, s) -> WeekStore(s, s.id in visitedStoreIds) })
        }

    fun visitWeek(weekStart: LocalDate, groups: List<WeekPartner>): VisitWeek {
        val stores = groups.flatMap { it.stores }
        return VisitWeek(weekStart, stores.count { it.visited }, stores.size)
    }
}
