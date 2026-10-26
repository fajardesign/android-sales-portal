package com.example.and_sales_portal.data.activity

import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.partner.PartnerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDate

/**
 * Why a visit check-in cannot start (SOP decision 2026-10-08; PRD Scope 2 S12: one visit per toko per week, so
 * [VisitedThisWeek] until the next Monday).
 */
enum class VisitCheckInBlock { NotWorkCheckedIn, BeforeStart, VisitedThisWeek, StoreNotAssigned }

/** Why the TL cannot save a correction (decision 2026-10-19, Q6). */
enum class CorrectionError { NotLeader, Expired, Future, ReasonRequired, TimeRequired, TimeMismatch, NoChange }

/**
 * Attendance and visits of every SA/SR/TL on the device (shared, so the TL sees the team's attendance and visits).
 * Spec: ATT-01 … ATT-05, TL-01 … TL-04 (PRD v3 C5, C7, C8, D2, D3; PRD Scope 2, 2.0: no days off, S10).
 */
class OperationsRepository(
    private val store: DemoStore,
    private val partners: PartnerRepository,
    private val clock: () -> Instant = Instant::now,
) {
    private val saved = store.loadOps()
    private val _data = MutableStateFlow(saved ?: DemoSeed.initial(today(), partners.partners.value))
    val data: StateFlow<OpsData> = _data.asStateFlow()

    init {
        if (saved == null) store.saveOps(_data.value)
    }

    fun now(): Instant = clock()
    fun today(): LocalDate = clock().atZone(ActivityRules.ZONE).toLocalDate()

    /** Today for [employeeId] before any check in: the app clock in the zone of their area. */
    fun todayOf(employeeId: Long): LocalDate = clock().atZone(ActivityRules.zoneOf(employeeId)).toLocalDate()

    fun attendanceOf(employeeId: Long, date: LocalDate): Attendance? =
        _data.value.attendance.firstOrNull { it.employeeId == employeeId && it.date == date.toString() }

    fun attendanceStatus(employeeId: Long, date: LocalDate): AttendanceStatus =
        ActivityRules.attendanceStatus(attendanceOf(employeeId, date), date, todayOf(employeeId))

    /** Q6: days present in the week of [weekStart] (Monday–Sunday) over the target of 6; Absen days do not count (S13). */
    fun attendanceWeek(employeeId: Long, weekStart: LocalDate): AttendanceWeek {
        val end = ActivityRules.weekEnd(weekStart)
        val today = todayOf(employeeId)
        val n = _data.value.attendance.count { it.employeeId == employeeId && LocalDate.parse(it.date) in weekStart..end && ActivityRules.present(it, LocalDate.parse(it.date), today) }
        return AttendanceWeek(weekStart, n)
    }

    fun visit(id: String): Visit? = _data.value.visits.firstOrNull { it.id == id }

    fun visitsOf(employeeId: Long, date: LocalDate): List<Visit> =
        _data.value.visits.filter { it.employeeId == employeeId && it.date == date.toString() }.sortedBy { it.checkIn?.at }

    fun visitsBetween(employeeId: Long, from: LocalDate, to: LocalDate): List<Visit> =
        _data.value.visits.filter { it.employeeId == employeeId && LocalDate.parse(it.date) in from..to }

    /**
     * Toko of [employeeId] to visit in the week of [weekStart], by partner, with whether this person visited each (same
     * as the web `visitWeekStores`). The target is the toko held on Monday 00:00 WIB (PRD Scope 2 S9); a new holder counts
     * from the next week; a visit counts for the person who made it. PRD S20 (decision 2026-10-20): a toko released
     * mid-week that the old holder has not visited leaves their target without penalty; once visited it still counts.
     */
    fun weekPartners(employeeId: Long, weekStart: LocalDate): List<WeekPartner> {
        val role = DemoDirectory.person(employeeId)?.role?.let { SalesRole.valueOf(it) } ?: return emptyList()
        val lock = ActivityRules.weekLock(weekStart)
        val end = ActivityRules.weekLock(weekStart.plusWeeks(1)) - 1
        val at = minOf(clock().toEpochMilli(), end)
        val stillHeld = partners.weekStores(employeeId, role, at, at).map { it.second.id }.toSet()
        val seen = visitsBetween(employeeId, weekStart, ActivityRules.weekEnd(weekStart)).map { it.storeId }.toSet()
        val targets = partners.weekStores(employeeId, role, lock, lock).filter { (_, s) -> s.id in stillHeld || s.id in seen }
        return ActivityRules.weekPartners(targets, seen)
    }

    /**
     * Toko [employeeId] can visit now (ATT-03, Beranda SA/SR), by partner: the toko held today. A toko that came after
     * Monday 00:00 WIB is not in this week's target ("Dihitung mulai minggu depan") but can be visited.
     */
    fun visitPartners(employeeId: Long, date: LocalDate): List<WeekPartner> {
        val role = DemoDirectory.person(employeeId)?.role?.let { SalesRole.valueOf(it) } ?: return emptyList()
        val weekStart = ActivityRules.weekStart(date)
        val list = partners.partners.value
        val current = DemoSeed.storesOf(list, employeeId, role).mapNotNull { s -> list.firstOrNull { p -> p.stores.any { it.id == s.id } }?.let { it to s } }
        val counted = weekPartners(employeeId, weekStart).flatMap { g -> g.stores.map { it.store.id } }.toSet()
        val seen = visitsBetween(employeeId, weekStart, ActivityRules.weekEnd(weekStart)).map { it.storeId }.toSet()
        return ActivityRules.weekPartners(current, seen, counted)
    }

    /** Week of [weekStart] (Monday): toko visited over toko to visit. */
    fun visitWeek(employeeId: Long, weekStart: LocalDate): VisitWeek = ActivityRules.visitWeek(weekStart, weekPartners(employeeId, weekStart))

    /**
     * Weeks of [range] like the web portal `visitWeeks` (review 2026-10-09): a week belongs to the period when its Monday
     * is in it, up to today. "Hari ini" on a day other than Monday has none; screens show the current week separately.
     */
    fun visitWeeks(employeeId: Long, range: ClosedRange<LocalDate>): List<VisitWeek> {
        val monday = ActivityRules.weekStart(range.start)
        val first = if (monday.isBefore(range.start)) monday.plusWeeks(1) else monday
        val last = minOf(range.endInclusive, today())
        return generateSequence(first) { it.plusWeeks(1) }.takeWhile { !it.isAfter(last) }.map { visitWeek(employeeId, it) }.toList()
    }

    // region Attendance and visit execution (ATT)

    /** Date of a punch in the zone of the user's area (S8). */
    private fun dateOf(punch: Punch, employeeId: Long): LocalDate = java.time.Instant.ofEpochMilli(punch.at).atZone(ActivityRules.zoneOf(punch, employeeId)).toLocalDate()

    fun clockIn(employeeId: Long, punch: Punch) = change { d ->
        val date = dateOf(punch, employeeId).toString()
        d.copy(attendance = d.attendance.filterNot { it.employeeId == employeeId && it.date == date } + Attendance(employeeId, date, clockIn = punch))
    }

    fun clockOut(employeeId: Long, punch: Punch) = change { d ->
        val date = dateOf(punch, employeeId).toString()
        d.copy(attendance = d.attendance.map { if (it.employeeId == employeeId && it.date == date) it.copy(clockOut = punch) else it })
    }

    /** Visits of today not checked out yet (check-out is optional, S13, so several can be open at once). */
    fun ongoingVisits(employeeId: Long): List<Visit> {
        val today = todayOf(employeeId)
        return visitsOf(employeeId, today).filter { ActivityRules.visitStatus(it, today) == VisitStatus.ONGOING }
    }

    /**
     * Why [employeeId] cannot check in at [storeId] now: only at their toko, check in kerja first (SOP), from 12:00 in the
     * area zone, and once per toko per week (S12; until the next Monday). No check-out needed before another toko (S13).
     */
    fun checkInBlock(employeeId: Long, storeId: String): VisitCheckInBlock? {
        val role = DemoDirectory.person(employeeId)?.role?.let { SalesRole.valueOf(it) } ?: return VisitCheckInBlock.StoreNotAssigned
        val today = todayOf(employeeId)
        val monday = ActivityRules.weekStart(today)
        return when {
            DemoSeed.storesOf(partners.partners.value, employeeId, role).none { it.id == storeId } -> VisitCheckInBlock.StoreNotAssigned
            visitsBetween(employeeId, monday, ActivityRules.weekEnd(monday)).any { it.storeId == storeId } -> VisitCheckInBlock.VisitedThisWeek
            attendanceOf(employeeId, today)?.clockIn == null -> VisitCheckInBlock.NotWorkCheckedIn
            !ActivityRules.visitOpen(clock(), ActivityRules.zoneOf(employeeId)) -> VisitCheckInBlock.BeforeStart
            else -> null
        }
    }

    /** Starts a visit at [storeId]; returns it, or `null` when [checkInBlock] refuses. */
    fun checkIn(employeeId: Long, storeId: String, punch: Punch): Visit? {
        if (checkInBlock(employeeId, storeId) != null) return null
        val date = todayOf(employeeId)
        val v = Visit("V-$employeeId-$date-$storeId-${punch.at}", employeeId, storeId, date.toString(), checkIn = punch)
        change { it.copy(visits = it.visits + v) }
        return v
    }

    /** Optional check-out with an optional note (S13), the same day only; it can be saved offline and sent later. */
    fun checkOut(visitId: String, punch: Punch, note: String?): Boolean {
        val v = visit(visitId) ?: return false
        if (ActivityRules.visitStatus(v, todayOf(v.employeeId)) != VisitStatus.ONGOING) return false
        changeVisit(visitId) { it.copy(state = VisitState.DONE, checkOut = punch, note = note?.trim()?.ifEmpty { null }) }
        return true
    }

    fun pendingSync(employeeId: Long): Int = _data.value.visits.count { it.employeeId == employeeId && it.checkOut?.synced == false }

    fun syncPending(employeeId: Long): Int {
        val n = pendingSync(employeeId)
        if (n > 0) change { d ->
            d.copy(visits = d.visits.map { v -> v.checkOut?.takeIf { !it.synced && v.employeeId == employeeId }?.let { v.copy(checkOut = it.copy(synced = true)) } ?: v })
        }
        return n
    }

    // endregion

    // region Koreksi absensi oleh TL (decision 2026-10-19, Q6)

    /**
     * The TL can correct a day up to 23:59 WIB of the end of the following week (Sunday) or of the day before the lock of
     * its month (PRD Scope 3, FR-DF-006), whichever comes first; after the lock Koreksi Absensi is not available.
     */
    fun correctionDeadline(date: LocalDate): Long =
        com.example.and_sales_portal.data.sales.Scope3.correctionDeadline(date).plusDays(1).atStartOfDay(ActivityRules.LOCK_ZONE).toInstant().toEpochMilli() - 1

    fun canCorrect(employeeId: Long, date: LocalDate): Boolean = !date.isAfter(todayOf(employeeId)) && clock().toEpochMilli() <= correctionDeadline(date)

    /**
     * Corrects [employeeId]'s attendance of [date] (status Absen / Tepat Waktu / Terlambat and the check-in time) by the
     * TL [tlId], with a required reason; old and new values are kept in [Attendance.corrections].
     */
    fun correct(tlId: Long, employeeId: Long, date: LocalDate, toStatus: AttendanceStatus, toTime: java.time.LocalTime?, reason: String): CorrectionError? {
        if (DemoDirectory.person(employeeId)?.supervisorId != tlId) return CorrectionError.NotLeader
        val today = todayOf(employeeId)
        if (date.isAfter(today)) return CorrectionError.Future
        if (clock().toEpochMilli() > correctionDeadline(date)) return CorrectionError.Expired
        if (reason.isBlank()) return CorrectionError.ReasonRequired
        val old = attendanceOf(employeeId, date) ?: Attendance(employeeId, date.toString())
        val zone = ActivityRules.zoneOf(employeeId)
        val newAt: Long? = when (toStatus) {
            AttendanceStatus.ABSENT -> null
            AttendanceStatus.ON_TIME, AttendanceStatus.LATE -> {
                val t = toTime ?: return CorrectionError.TimeRequired
                val onTime = !t.isAfter(ActivityRules.CLOCK_IN_LIMIT)
                if (onTime != (toStatus == AttendanceStatus.ON_TIME)) return CorrectionError.TimeMismatch
                date.atTime(t).atZone(zone).toInstant().toEpochMilli()
            }
            AttendanceStatus.NOT_YET, AttendanceStatus.NO_CHECKIN -> return CorrectionError.NoChange
        }
        val fromStatus = ActivityRules.attendanceStatus(old, date, today)
        if (fromStatus == toStatus && old.checkInAt == newAt) return CorrectionError.NoChange
        val c = AttendanceCorrection(clock().toEpochMilli(), tlId, fromStatus, old.checkInAt, toStatus, newAt, reason.trim())
        val next = old.copy(
            statusOverride = toStatus,
            clockInOverride = newAt?.takeIf { it != old.clockIn?.at },
            corrections = old.corrections + c,
        )
        change { d -> d.copy(attendance = d.attendance.filterNot { it.employeeId == employeeId && it.date == date.toString() } + next) }
        return null
    }

    // endregion

    /** "Reset data demo". */
    fun reset() {
        _data.value = DemoSeed.initial(today(), partners.partners.value)
        store.saveOps(_data.value)
    }

    private fun changeVisit(id: String, f: (Visit) -> Visit) = change { d -> d.copy(visits = d.visits.map { if (it.id == id) f(it) else it }) }

    private fun change(f: (OpsData) -> OpsData) {
        _data.update(f)
        store.saveOps(_data.value)
    }
}
