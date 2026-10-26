package com.example.and_sales_portal.ui.common

import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.Punch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Dates and times in Indonesian; check in/out times in the zone of the user's area (WIB, WITA, WIT; PRD Scope 2 S8). */
object Fmt {
    private val id = Locale("id", "ID")
    private val time = DateTimeFormatter.ofPattern("HH:mm", id)
    private val longDate = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", id)
    private val shortDate = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", id)

    fun time(millis: Long): String = time.format(Instant.ofEpochMilli(millis).atZone(ActivityRules.ZONE))

    /** "08:30" in [zone]. */
    fun time(millis: Long, zone: java.time.ZoneId): String = time.format(Instant.ofEpochMilli(millis).atZone(zone))

    /** "08:30 WITA": time with the zone of the signed-in user's area. */
    fun timeLabel(millis: Long): String = "${time(millis)} ${ActivityRules.ZONE_LABEL}"

    /** "08:30 WITA" of a check in or check out, in the zone saved with it (PRD alignment 2026-10-09). */
    fun timeLabel(p: Punch, employeeId: Long): String {
        val zone = ActivityRules.zoneOf(p, employeeId)
        return "${time.format(Instant.ofEpochMilli(p.at).atZone(zone))} ${ActivityRules.label(zone)}"
    }

    /** "08:30 WITA" of the check in that counts (a TL correction, else the punch), in the zone of the user's area (S8). */
    fun checkInLabel(a: com.example.and_sales_portal.data.activity.Attendance): String? {
        val at = a.checkInAt ?: return null
        val zone = ActivityRules.zoneOf(a.employeeId)
        return "${time.format(Instant.ofEpochMilli(at).atZone(zone))} ${ActivityRules.label(zone)}"
    }

    /** "08:30" of a check in or check out, in the zone saved with it. */
    fun time(p: Punch, employeeId: Long): String = time.format(Instant.ofEpochMilli(p.at).atZone(ActivityRules.zoneOf(p, employeeId)))

    /** "10:00 WIB" for a planned time. */
    fun withZone(hhmm: String): String = "$hhmm ${ActivityRules.ZONE_LABEL}"
    fun longDate(date: LocalDate): String = longDate.format(date)
    fun shortDate(date: LocalDate): String = shortDate.format(date)

    private val day = DateTimeFormatter.ofPattern("d MMM yyyy", id)
    private val month = DateTimeFormatter.ofPattern("MMMM yyyy", id)

    /** "5 Okt 2026" (PRD Scope 3 dates: lock, payout, paid). */
    fun day(date: LocalDate): String = day.format(date)

    /** "Oktober 2026". */
    fun month(ym: java.time.YearMonth): String = month.format(ym)

    /** "7 Okt 2026, 10:30 WIB" in WIB, the zone of the loan system and of the month (PRD Scope 3). */
    fun dateTimeWib(millis: Long): String {
        val t = Instant.ofEpochMilli(millis).atZone(ActivityRules.LOCK_ZONE)
        return "${day.format(t)}, ${time.format(t)} WIB"
    }

    /** Elapsed time as `HH:MM:SS` (visit timer). */
    fun clock(millis: Long): String {
        val s = (millis / 1000).coerceAtLeast(0)
        return String.format(Locale.US, "%02d:%02d:%02d", s / 3600, s / 60 % 60, s % 60)
    }
}
