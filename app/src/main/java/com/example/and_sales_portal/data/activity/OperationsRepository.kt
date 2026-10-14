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

/** Why a visit check-in cannot start (stakeholder revision 2026-10-08, SOP decision 2026-10-08). */
enum class VisitCheckInBlock { NotWorkCheckedIn, BeforeStart, DayOff, Ongoing, StoreNotAssigned }

/**
 * Attendance, visits and days off of every SA/SR/TL on the device (shared, so the TL sees the team's attendance and
 * visits and marks days off). Spec: ATT-01 … ATT-05, TL-01 … TL-04 (PRD v3 C5, C7, C8, D2, D3; stakeholder revision
 * 2026-10-08: no hourly visit plan).
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

    /** Today for [employeeId] on this device (device zone, else the area's). */
    fun todayOf(employeeId: Long): LocalDate = clock().atZone(ActivityRules.deviceZone(employeeId)).toLocalDate()

    fun attendanceOf(employeeId: Long, date: LocalDate): Attendance? =
        _data.value.attendance.firstOrNull { it.employeeId == employeeId && it.date == date.toString() }

    fun isDayOff(employeeId: Long, date: LocalDate) = OpsData.key(employeeId, date) in _data.value.daysOff

    fun attendanceStatus(employeeId: Long, date: LocalDate): AttendanceStatus =
        ActivityRules.attendanceStatus(attendanceOf(employeeId, date), date, today(), isDayOff(employeeId, date), ActivityRules.zoneOf(employeeId))

    fun visit(id: String): Visit? = _data.value.visits.firstOrNull { it.id == id }

    fun visitsOf(employeeId: Long, date: LocalDate): List<Visit> =
        _data.value.visits.filter { it.employeeId == employeeId && it.date == date.toString() }.sortedBy { it.checkIn?.at }

    fun visitsBetween(employeeId: Long, from: LocalDate, to: LocalDate): List<Visit> =
        _data.value.visits.filter { it.employeeId == employeeId && LocalDate.parse(it.date) in from..to }

    /** Toko of [employeeId] to visit in the week of [weekStart], by partner, with whether each was visited. */
    fun weekPartners(employeeId: Long, weekStart: LocalDate): List<WeekPartner> {
        val role = DemoDirectory.person(employeeId)?.role?.let { SalesRole.valueOf(it) } ?: return emptyList()
        // Toko of that week, not of today (decision 2026-10-14): held on Monday and still held at the end of the week.
        val zone = ActivityRules.zoneOf(employeeId)
        val endOf = { d: LocalDate -> d.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1 }
        val targets = partners.weekStores(employeeId, role, endOf(weekStart), minOf(endOf(ActivityRules.weekEnd(weekStart)), clock().toEpochMilli()))
        val seen = visitsBetween(employeeId, weekStart, ActivityRules.weekEnd(weekStart)).map { it.storeId }.toSet()
        return ActivityRules.weekPartners(targets, seen)
    }

    /** Week of [weekStart] (Monday): toko visited over toko to visit (PRD alignment 2026-10-09; days off do not count). */
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

    /** Visit that has started and not ended. */
    fun ongoingVisit(employeeId: Long): Visit? = _data.value.visits.firstOrNull { it.employeeId == employeeId && it.state == VisitState.IN_PROGRESS }

    /**
     * Why [employeeId] cannot check in at [storeId] now: check in kerja first (SOP), from 12:00 local time, not on a day
     * off, one visit at a time, only at their toko. A second visit on the same day is allowed but counts once.
     */
    fun checkInBlock(employeeId: Long, storeId: String): VisitCheckInBlock? {
        val role = DemoDirectory.person(employeeId)?.role?.let { SalesRole.valueOf(it) } ?: return VisitCheckInBlock.StoreNotAssigned
        val today = todayOf(employeeId)
        return when {
            DemoSeed.storesOf(partners.partners.value, employeeId, role).none { it.id == storeId } -> VisitCheckInBlock.StoreNotAssigned
            isDayOff(employeeId, today) -> VisitCheckInBlock.DayOff
            attendanceOf(employeeId, today)?.clockIn == null -> VisitCheckInBlock.NotWorkCheckedIn
            !ActivityRules.visitOpen(clock(), employeeId) -> VisitCheckInBlock.BeforeStart
            ongoingVisit(employeeId) != null -> VisitCheckInBlock.Ongoing
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

    fun checkOut(visitId: String, punch: Punch, note: String?) =
        changeVisit(visitId) { it.copy(state = VisitState.DONE, checkOut = punch, note = note?.trim()?.ifEmpty { null }) }

    fun pendingSync(employeeId: Long): Int = _data.value.visits.count { it.employeeId == employeeId && it.checkOut?.synced == false }

    fun syncPending(employeeId: Long): Int {
        val n = pendingSync(employeeId)
        if (n > 0) change { d ->
            d.copy(visits = d.visits.map { v -> v.checkOut?.takeIf { !it.synced && v.employeeId == employeeId }?.let { v.copy(checkOut = it.copy(synced = true)) } ?: v })
        }
        return n
    }

    // endregion

    /** Marks or clears a day off (TL-04, today or later): attendance shows Libur and visit check-in is closed that day. */
    fun setDayOff(employeeId: Long, date: LocalDate, off: Boolean) {
        if (date.isBefore(todayOf(employeeId))) return
        val key = OpsData.key(employeeId, date)
        change { d -> d.copy(daysOff = if (off) d.daysOff + key else d.daysOff - key) }
    }

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
