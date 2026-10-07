package com.example.and_sales_portal.data.activity

import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.data.partner.Store
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDate

/**
 * Attendance and visits of the signed-in user (Spec: ATT-01 … ATT-05, PRD v3 D2, D3, C8), on top of [DemoStore].
 * Stands in for the mobile API until its contract exists.
 */
class ActivityRepository(
    private val store: DemoStore,
    private val partners: PartnerRepository,
    val userId: Long,
    private val role: SalesRole,
    private val areaId: Int?,
    private val clock: () -> Instant = Instant::now,
) {
    private val saved = store.load(userId)
    private val _data = MutableStateFlow(saved ?: DemoSeed.initial(userId, role, areaId, today(), partners.partners.value))
    val data: StateFlow<DemoData> = _data.asStateFlow()

    init {
        if (saved == null) store.save(userId, _data.value)
        ensurePlanned()
    }

    fun now(): Instant = clock()
    fun today(): LocalDate = clock().atZone(ActivityRules.ZONE).toLocalDate()

    fun stores(): List<Store> = DemoSeed.storesOf(partners.partners.value, userId, role)
    fun store(id: String): Store? = partners.partners.value.flatMap { it.stores }.firstOrNull { it.id == id }
    fun partnerOf(storeId: String): Partner? = partners.partners.value.firstOrNull { p -> p.stores.any { it.id == storeId } }

    fun attendanceOf(date: LocalDate): Attendance? = _data.value.attendance.firstOrNull { it.date == date.toString() }
    fun visit(id: String): Visit? = _data.value.visits.firstOrNull { it.id == id }
    fun visitsOn(date: LocalDate): List<Visit> = _data.value.visits.filter { it.date == date.toString() }.sortedBy { it.plannedTime }

    /** Plans every workday between the last planned day and today (the TL's plan arrives each day). */
    fun ensurePlanned() {
        val today = today()
        val last = _data.value.plannedDays.maxOrNull()?.let(LocalDate::parse) ?: today.minusDays(1)
        if (!last.isBefore(today)) return
        val stores = stores()
        val days = generateSequence(last.plusDays(1)) { it.plusDays(1) }.takeWhile { !it.isAfter(today) }.toList()
        change { d ->
            d.copy(
                visits = d.visits + days.flatMap { DemoSeed.plan(userId, role, stores, it) },
                plannedDays = d.plannedDays + days.map { it.toString() },
            )
        }
    }

    fun clockIn(punch: Punch) = change { d ->
        val date = today().toString()
        d.copy(attendance = d.attendance.filterNot { it.date == date } + Attendance(date, clockIn = punch))
    }

    fun clockOut(punch: Punch) = change { d ->
        val date = today().toString()
        d.copy(attendance = d.attendance.map { if (it.date == date) it.copy(clockOut = punch) else it })
    }

    fun checkIn(visitId: String, punch: Punch) = changeVisit(visitId) { it.copy(state = VisitState.IN_PROGRESS, checkIn = punch) }

    fun checkOut(visitId: String, punch: Punch, note: String?) =
        changeVisit(visitId) { it.copy(state = VisitState.DONE, checkOut = punch, note = note?.trim()?.ifEmpty { null }) }

    /** Number of check-outs saved offline. */
    fun pendingSync(): Int = _data.value.visits.count { it.checkOut?.synced == false }

    /** Sends what was saved offline (prototype: marks it sent). Returns how many were sent. */
    fun syncPending(): Int {
        val n = pendingSync()
        if (n > 0) change { d -> d.copy(visits = d.visits.map { v -> v.checkOut?.takeIf { !it.synced }?.let { v.copy(checkOut = it.copy(synced = true)) } ?: v }) }
        return n
    }

    /** "Reset data demo": back to the initial sample data. */
    fun reset() {
        store.reset(userId)
        _data.value = DemoSeed.initial(userId, role, areaId, today(), partners.partners.value)
        store.save(userId, _data.value)
    }

    private fun changeVisit(id: String, f: (Visit) -> Visit) = change { d -> d.copy(visits = d.visits.map { if (it.id == id) f(it) else it }) }

    private fun change(f: (DemoData) -> DemoData) {
        _data.update(f)
        store.save(userId, _data.value)
    }
}
