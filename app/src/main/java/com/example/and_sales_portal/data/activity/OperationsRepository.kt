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
import java.time.LocalTime

/** Why a visit cannot be planned (PRD v3 C7 validation). */
enum class PlanError { PastDate, DayOff, MaxPerDay, DuplicateTime, StoreNotAssigned, AlreadyStarted }

/**
 * Attendance, visit plans and days off of every SA/SR/TL on the device (shared, so a TL's plan reaches the SA/SR and the
 * TL sees the team's attendance). Spec: ATT-01 … ATT-05, TL-01 … TL-05 (PRD v3 C5, C7, C8, D2, D3).
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
        ensurePlanned()
    }

    fun now(): Instant = clock()
    fun today(): LocalDate = clock().atZone(ActivityRules.ZONE).toLocalDate()

    fun attendanceOf(employeeId: Long, date: LocalDate): Attendance? =
        _data.value.attendance.firstOrNull { it.employeeId == employeeId && it.date == date.toString() }

    fun isDayOff(employeeId: Long, date: LocalDate) = OpsData.key(employeeId, date) in _data.value.daysOff

    fun attendanceStatus(employeeId: Long, date: LocalDate): AttendanceStatus =
        ActivityRules.attendanceStatus(attendanceOf(employeeId, date), date, today(), isDayOff(employeeId, date), ActivityRules.zoneOf(employeeId))

    fun visit(id: String): Visit? = _data.value.visits.firstOrNull { it.id == id }

    fun visitsOf(employeeId: Long, date: LocalDate): List<Visit> =
        _data.value.visits.filter { it.employeeId == employeeId && it.date == date.toString() }.sortedBy { it.plannedTime }

    fun visitsBetween(employeeId: Long, from: LocalDate, to: LocalDate): List<Visit> =
        _data.value.visits.filter { it.employeeId == employeeId && LocalDate.parse(it.date) in from..to }

    /**
     * The daily plan of each active SA/SR/TL arrives each day (sample pattern), for days the TL has not planned by hand.
     */
    fun ensurePlanned() {
        val today = today()
        val d = _data.value
        val newVisits = mutableListOf<Visit>()
        val keys = mutableSetOf<String>()
        DemoSeed.employees().forEach { p ->
            val role = SalesRole.valueOf(p.role)
            val last = d.plannedDays.filter { it.startsWith("${p.id}:") }.maxOfOrNull { LocalDate.parse(it.substringAfter(':')) } ?: today.minusDays(1)
            var day = last.plusDays(1)
            while (!day.isAfter(today)) {
                val key = OpsData.key(p.id, day)
                if (key !in d.plannedDays && key !in d.daysOff) {
                    newVisits += DemoSeed.plan(p.id, role, DemoSeed.storesOf(partners.partners.value, p.id, role), day)
                }
                keys += key
                day = day.plusDays(1)
            }
        }
        if (keys.isNotEmpty()) change { it.copy(visits = it.visits + newVisits, plannedDays = it.plannedDays + keys) }
    }

    // region Attendance and visit execution (ATT)

    fun clockIn(employeeId: Long, punch: Punch) = change { d ->
        val date = today().toString()
        d.copy(attendance = d.attendance.filterNot { it.employeeId == employeeId && it.date == date } + Attendance(employeeId, date, clockIn = punch))
    }

    fun clockOut(employeeId: Long, punch: Punch) = change { d ->
        val date = today().toString()
        d.copy(attendance = d.attendance.map { if (it.employeeId == employeeId && it.date == date) it.copy(clockOut = punch) else it })
    }

    fun checkIn(visitId: String, punch: Punch) = changeVisit(visitId) { it.copy(state = VisitState.IN_PROGRESS, checkIn = punch) }

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

    // region Visit planning (TL-04, TL-05; PRD C7)

    /** Problems with planning [storeId] at [time] on [date] for [employeeId]; [editing] is the visit being changed. */
    fun planError(employeeId: Long, date: LocalDate, storeId: String, time: String, editing: String? = null): PlanError? {
        val role = DemoDirectory.person(employeeId)?.role?.let { SalesRole.valueOf(it) } ?: return PlanError.StoreNotAssigned
        val sameDay = visitsOf(employeeId, date).filter { it.id != editing && it.state != VisitState.CANCELLED }
        val nowTime = clock().atZone(ActivityRules.zoneOf(employeeId)).toLocalTime()
        return when {
            editing != null && visit(editing)?.state != VisitState.SCHEDULED -> PlanError.AlreadyStarted
            date.isBefore(today()) || (date == today() && LocalTime.parse(time).isBefore(nowTime)) -> PlanError.PastDate
            isDayOff(employeeId, date) -> PlanError.DayOff
            sameDay.size >= ActivityRules.MAX_VISITS_PER_DAY -> PlanError.MaxPerDay
            sameDay.any { it.plannedTime == time } -> PlanError.DuplicateTime
            DemoSeed.storesOf(partners.partners.value, employeeId, role).none { it.id == storeId } -> PlanError.StoreNotAssigned
            else -> null
        }
    }

    fun addVisit(employeeId: Long, date: LocalDate, storeId: String, time: String, by: Long): PlanError? {
        planError(employeeId, date, storeId, time)?.let { return it }
        val v = Visit("V-$employeeId-$date-$storeId-${time.replace(":", "")}", employeeId, storeId, date.toString(), time, createdBy = by)
        change { it.copy(visits = it.visits + v, plannedDays = it.plannedDays + OpsData.key(employeeId, date)) }
        return null
    }

    fun editVisit(id: String, storeId: String, time: String): PlanError? {
        val v = visit(id) ?: return PlanError.AlreadyStarted
        planError(v.employeeId, LocalDate.parse(v.date), storeId, time, editing = id)?.let { return it }
        changeVisit(id) { it.copy(storeId = storeId, plannedTime = time) }
        return null
    }

    /** "Dibatalkan" by the TL, before check-in. */
    fun cancelVisit(id: String, reason: String) {
        if (visit(id)?.state != VisitState.SCHEDULED) return
        changeVisit(id) { it.copy(state = VisitState.CANCELLED, cancelReason = reason) }
    }

    /** Marks or clears a day off; marking cancels the day's scheduled visits. */
    fun setDayOff(employeeId: Long, date: LocalDate, off: Boolean, reason: String) {
        if (date.isBefore(today())) return
        val key = OpsData.key(employeeId, date)
        change { d ->
            if (off) {
                d.copy(
                    daysOff = d.daysOff + key,
                    plannedDays = d.plannedDays + key,
                    visits = d.visits.map { if (it.employeeId == employeeId && it.date == date.toString() && it.state == VisitState.SCHEDULED) it.copy(state = VisitState.CANCELLED, cancelReason = reason) else it },
                )
            } else {
                d.copy(daysOff = d.daysOff - key)
            }
        }
    }

    /**
     * "Salin jadwal minggu lalu": last week's visits (not cancelled) on the same weekday, for days of [weekStart]'s week
     * from today on that have no plan yet and are not days off. Returns how many visits were added.
     */
    fun copyLastWeek(employeeId: Long, weekStart: LocalDate, by: Long): Int {
        var added = 0
        (0L..6L).map { weekStart.plusDays(it) }.filter { !it.isBefore(today()) }.forEach { day ->
            if (isDayOff(employeeId, day) || visitsOf(employeeId, day).any { it.state != VisitState.CANCELLED }) return@forEach
            visitsOf(employeeId, day.minusWeeks(1)).filter { it.state != VisitState.CANCELLED }.forEach { v ->
                if (addVisit(employeeId, day, v.storeId, v.plannedTime, by) == null) added++
            }
        }
        return added
    }

    /** PRD C6: future visits to toko of an unassigned partner are cancelled automatically. Returns how many. */
    fun cancelFutureVisits(employeeId: Long, storeIds: Set<String>, reason: String): Int {
        val today = today()
        val ids = _data.value.visits.filter {
            it.employeeId == employeeId && it.storeId in storeIds && it.state == VisitState.SCHEDULED && !LocalDate.parse(it.date).isBefore(today)
        }.map { it.id }.toSet()
        if (ids.isNotEmpty()) change { d -> d.copy(visits = d.visits.map { if (it.id in ids) it.copy(state = VisitState.CANCELLED, cancelReason = reason) else it }) }
        return ids.size
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
