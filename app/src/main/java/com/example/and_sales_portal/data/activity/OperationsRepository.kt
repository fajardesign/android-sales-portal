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

    /** Today for [employeeId]: the app clock in the zone of their area (PRD Scope 2 S8). */
    fun todayOf(employeeId: Long): LocalDate = clock().atZone(ActivityRules.zoneOf(employeeId)).toLocalDate()

    fun attendanceOf(employeeId: Long, date: LocalDate): Attendance? =
        _data.value.attendance.firstOrNull { it.employeeId == employeeId && it.date == date.toString() }

    fun attendanceStatus(employeeId: Long, date: LocalDate): AttendanceStatus =
        ActivityRules.attendanceStatus(attendanceOf(employeeId, date), date, todayOf(employeeId), ActivityRules.zoneOf(employeeId))

    fun visit(id: String): Visit? = _data.value.visits.firstOrNull { it.id == id }

    fun visitsOf(employeeId: Long, date: LocalDate): List<Visit> =
        _data.value.visits.filter { it.employeeId == employeeId && it.date == date.toString() }.sortedBy { it.checkIn?.at }

    fun visitsBetween(employeeId: Long, from: LocalDate, to: LocalDate): List<Visit> =
        _data.value.visits.filter { it.employeeId == employeeId && LocalDate.parse(it.date) in from..to }

    /**
     * Toko of [employeeId] to visit in the week of [weekStart], by partner, with whether each was visited (same as the
     * web `visitWeekStores`). PRD Scope 2 S9/S11: the current week uses the toko held **now** (the target changes at once,
     * no proration); a finished week stays frozen from the history (held on Monday and still held at its end).
     */
    fun weekPartners(employeeId: Long, weekStart: LocalDate): List<WeekPartner> {
        val role = DemoDirectory.person(employeeId)?.role?.let { SalesRole.valueOf(it) } ?: return emptyList()
        val weekEnd = ActivityRules.weekEnd(weekStart)
        val targets = if (!weekEnd.isBefore(todayOf(employeeId))) {
            val list = partners.partners.value
            DemoSeed.storesOf(list, employeeId, role).mapNotNull { s -> list.firstOrNull { p -> p.stores.any { it.id == s.id } }?.let { it to s } }
        } else {
            val zone = ActivityRules.zoneOf(employeeId)
            val endOf = { d: LocalDate -> d.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1 }
            partners.weekStores(employeeId, role, endOf(weekStart), endOf(weekEnd))
        }
        val seen = visitsBetween(employeeId, weekStart, weekEnd).map { it.storeId }.toSet()
        return ActivityRules.weekPartners(targets, seen)
    }

    /** Toko [employeeId] can visit now (ATT-03, Beranda SA/SR), by partner: the toko held today, i.e. this week's target. */
    fun visitPartners(employeeId: Long, date: LocalDate): List<WeekPartner> = weekPartners(employeeId, ActivityRules.weekStart(date))

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

    fun clockIn(employeeId: Long, punch: Punch) = change { d ->
        val date = todayOf(employeeId).toString()
        d.copy(attendance = d.attendance.filterNot { it.employeeId == employeeId && it.date == date } + Attendance(employeeId, date, clockIn = punch))
    }

    fun clockOut(employeeId: Long, punch: Punch) = change { d ->
        val date = todayOf(employeeId).toString()
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
            !ActivityRules.visitOpen(clock(), employeeId) -> VisitCheckInBlock.BeforeStart
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
