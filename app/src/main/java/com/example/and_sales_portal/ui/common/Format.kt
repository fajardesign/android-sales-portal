package com.example.and_sales_portal.ui.common

import com.example.and_sales_portal.data.activity.ActivityRules
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Dates and times in Indonesian, in the local zone of the signed-in user's area (WIB, WITA). */
object Fmt {
    private val id = Locale("id", "ID")
    private val time = DateTimeFormatter.ofPattern("HH:mm", id)
    private val longDate = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", id)
    private val shortDate = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", id)

    fun time(millis: Long): String = time.format(Instant.ofEpochMilli(millis).atZone(ActivityRules.ZONE))

    /** "08:30 WITA": time with the zone of the signed-in user's area. */
    fun timeLabel(millis: Long): String = "${time(millis)} ${ActivityRules.ZONE_LABEL}"

    /** "10:00 WIB" for a planned time. */
    fun withZone(hhmm: String): String = "$hhmm ${ActivityRules.ZONE_LABEL}"
    fun longDate(date: LocalDate): String = longDate.format(date)
    fun shortDate(date: LocalDate): String = shortDate.format(date)

    /** Elapsed time as `HH:MM:SS` (visit timer). */
    fun clock(millis: Long): String {
        val s = (millis / 1000).coerceAtLeast(0)
        return String.format(Locale.US, "%02d:%02d:%02d", s / 3600, s / 60 % 60, s % 60)
    }
}
