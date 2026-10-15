package com.example.and_sales_portal.data.activity

import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.device.Place
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.data.partner.PartnerRepository
import com.example.and_sales_portal.data.partner.Store
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate

/**
 * Attendance and visits of the signed-in user (Spec: ATT-01 … ATT-05, PRD v3 D2, D3, C8): a view of the shared
 * [OperationsRepository]. Stands in for the mobile API until its contract exists.
 */
class ActivityRepository(
    private val ops: OperationsRepository,
    private val partners: PartnerRepository,
    val userId: Long,
    private val role: SalesRole,
) {
    val data: StateFlow<OpsData> get() = ops.data

    fun now(): Instant = ops.now()
    fun today(): LocalDate = ops.today()

    fun stores(): List<Store> = DemoSeed.storesOf(partners.partners.value, userId, role)

    /**
     * Where check in / check out kerja is allowed (revision 2026-10-08): the office of the user's area, then the toko
     * (SA/SR: assigned toko; TL: toko of the partners they own).
     */
    fun attendancePlaces(): List<Place> = DemoSeed.attendancePlaces(partners.partners.value, userId, role)
    fun store(id: String): Store? = partners.partners.value.flatMap { it.stores }.firstOrNull { it.id == id }
    fun partnerOf(storeId: String): Partner? = partners.partners.value.firstOrNull { p -> p.stores.any { it.id == storeId } }

    fun attendanceOf(date: LocalDate): Attendance? = ops.attendanceOf(userId, date)
    fun isDayOff(date: LocalDate): Boolean = ops.isDayOff(userId, date)
    fun visit(id: String): Visit? = ops.visit(id)
    fun visitsOn(date: LocalDate): List<Visit> = ops.visitsOf(userId, date)
    fun visitWeek(date: LocalDate): VisitWeek = ops.visitWeek(userId, ActivityRules.weekStart(date))
    fun weekPartners(date: LocalDate): List<WeekPartner> = ops.weekPartners(userId, ActivityRules.weekStart(date))
    /** Toko held today, for check-in (see [OperationsRepository.visitPartners]). */
    fun visitPartners(date: LocalDate): List<WeekPartner> = ops.visitPartners(userId, date)
    fun ongoingVisit(): Visit? = ops.ongoingVisit(userId)
    fun checkInBlock(storeId: String): VisitCheckInBlock? = ops.checkInBlock(userId, storeId)

    fun clockIn(punch: Punch) = ops.clockIn(userId, punch)
    fun clockOut(punch: Punch) = ops.clockOut(userId, punch)
    fun checkIn(storeId: String, punch: Punch): Visit? = ops.checkIn(userId, storeId, punch)
    fun checkOut(visitId: String, punch: Punch, note: String?) = ops.checkOut(visitId, punch, note)

    /** Number of check-outs saved offline. */
    fun pendingSync(): Int = ops.pendingSync(userId)

    /** Sends what was saved offline (prototype: marks it sent). Returns how many were sent. */
    fun syncPending(): Int = ops.syncPending(userId)
}
