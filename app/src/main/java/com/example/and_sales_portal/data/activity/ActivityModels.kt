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
        /** 6: visits once per toko per week, zone on every punch (PRD alignment 2026-10-09). */
        const val VERSION = 6
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
    /** Device time zone at the punch (`Asia/Makassar`, …), PRD alignment 2026-10-09; times are shown in it. */
    val zone: String? = null,
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
 * Visits of one week (Monday–Saturday), PRD alignment 2026-10-09 (Scope 2 §2.2, same as the web portal
 * `visitWeekStores`): every assigned toko (TL: toko of own partners) is visited once a week, any day, several toko a day
 * allowed. [visited] = toko visited that week, [target] = toko to visit. "Lengkap n/n" or "Belum lengkap n/m".
 */
data class VisitWeek(val start: LocalDate, val visited: Int, val target: Int) {
    val complete: Boolean get() = target > 0 && visited >= target
}

/** A toko of the week with whether it was visited. */
data class WeekStore(val store: Store, val visited: Boolean)

/** A partner's toko of the week; the partner counts as visited when all its toko are ("Sudah dikunjungi minggu ini"). */
data class WeekPartner(val partner: Partner, val stores: List<WeekStore>) {
    val visited: Boolean get() = stores.all { it.visited }
}

/**
 * Time and place rules. Check in kerja is on time up to 10:00 **local time** of the person's area (Makassar WITA,
 * Jabodetabek WIB, …; decision 2026-10-08). Stakeholder revision 2026-10-08: visit check-in from 12:00 local time within
 * 3 km of the toko, no hourly plan, weekly target of one visit per workday.
 */
object ActivityRules {
    /** Indonesian zones and their labels (PRD alignment 2026-10-09: WIB, WITA, WIT). */
    val ZONE_LABELS: Map<String, String> = mapOf(
        "Asia/Jakarta" to "WIB", "Asia/Pontianak" to "WIB", "Asia/Makassar" to "WITA", "Asia/Jayapura" to "WIT",
    )

    /** The device's zone (replaceable in tests). */
    @Volatile
    var systemZone: () -> ZoneId = { ZoneId.systemDefault() }

    /** Display zone: the device zone, or the signed-in user's area when the device is not on WIB/WITA/WIT. */
    @Volatile
    var ZONE: ZoneId = ZoneId.of("Asia/Jakarta")
        private set

    /** "WIB", "WITA" or "WIT" of [ZONE]. */
    @Volatile
    var ZONE_LABEL: String = "WIB"
        private set

    fun useArea(areaId: Int?) {
        val area = areaId?.let { DemoDirectory.area(it) } ?: return
        ZONE = systemZone().takeIf { it.id in ZONE_LABELS } ?: area.zone
        ZONE_LABEL = label(ZONE)
    }

    /** Local zone of a person's area. */
    fun zoneOf(employeeId: Long): ZoneId = DemoDirectory.person(employeeId)?.let { DemoDirectory.area(it.areaId) }?.zone ?: ZONE

    /**
     * Zone of a check in made now on this device (PRD alignment 2026-10-09): the device zone when it is WIB, WITA or WIT,
     * otherwise the person's area (decision 2026-10-09, e.g. an emulator on UTC).
     */
    fun deviceZone(employeeId: Long): ZoneId = systemZone().takeIf { it.id in ZONE_LABELS } ?: zoneOf(employeeId)

    /** Zone saved with a punch; punches saved before it carry none and use the person's area. */
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

    fun attendanceStatus(a: Attendance?, date: LocalDate, today: LocalDate, dayOff: Boolean = false, zone: ZoneId = a?.let { zoneOf(it.employeeId) } ?: ZONE): AttendanceStatus {
        val inAt = a?.clockIn?.at
        return when {
            inAt == null && dayOff -> AttendanceStatus.DAY_OFF
            // The zone saved with the check in decides (PRD alignment 2026-10-09), else the given zone.
            inAt != null -> if (Instant.ofEpochMilli(inAt).atZone(a.clockIn.zone?.let { ZoneId.of(it) } ?: zone).toLocalTime().truncatedTo(ChronoUnit.MINUTES) <= CLOCK_IN_LIMIT) AttendanceStatus.ON_TIME else AttendanceStatus.LATE
            date < today -> AttendanceStatus.ABSENT
            else -> AttendanceStatus.NOT_YET
        }
    }

    /** Visit check-in is open from [VISIT_START] local time of the person's area. */
    fun visitOpen(now: Instant, employeeId: Long): Boolean = !now.atZone(deviceZone(employeeId)).toLocalTime().isBefore(VISIT_START)

    fun visitStatus(v: Visit): VisitStatus = if (v.state == VisitState.DONE) VisitStatus.DONE else VisitStatus.ONGOING

    /** Monday of [date]'s week. */
    fun weekStart(date: LocalDate): LocalDate = date.with(DayOfWeek.MONDAY)

    /** Last day (Saturday) of the week starting [weekStart]. */
    fun weekEnd(weekStart: LocalDate): LocalDate = weekStart.plusDays(5)

    /** Toko of the week grouped by partner, from the toko to visit and the toko visited that week. */
    fun weekPartners(targets: List<Pair<Partner, Store>>, visitedStoreIds: Set<String>): List<WeekPartner> =
        targets.groupBy({ it.first.id }, { it }).values.map { list -> WeekPartner(list.first().first, list.map { (_, s) -> WeekStore(s, s.id in visitedStoreIds) }) }

    fun visitWeek(weekStart: LocalDate, groups: List<WeekPartner>): VisitWeek {
        val stores = groups.flatMap { it.stores }
        return VisitWeek(weekStart, stores.count { it.visited }, stores.size)
    }
}
