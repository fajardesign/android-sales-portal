package com.example.and_sales_portal.data.team

import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.partner.PartnerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Account changes made by the Admin on the web that the Android sample must reflect (web ACC-05, DD review points 5
 * and 7, PRD Scope 1 v2 C4): a disabled account, a replaced leader, a subordinate moved to another TL. Saved on the device and applied to
 * [DemoDirectory]; the prototype triggers them from Akun › Simulasi (debug), there is no admin screen on Android.
 */
class DirectoryRepository(private val store: DemoStore, private val partners: PartnerRepository) {

    private val _changes = MutableStateFlow(store.loadDirectory())
    /** Observed by screens that show a leader or a status (they redraw after Simulasi or Reset data demo). */
    val changes: StateFlow<Map<Long, DemoDirectory.Change>> = _changes.asStateFlow()

    init {
        DemoDirectory.changes = _changes.value
    }

    /**
     * Disables TL [tlId] with a handover (web ACC-05, decision 2026-10-16): their SA/SR move to [replacementId], their
     * active toko move to [replacementId] (past sales stay with [tlId]), and each SA/SR is released from toko whose TL is
     * not their new leader (FR-AM-013). Returns the number of toko moved.
     */
    fun disableTl(tlId: Long, replacementId: Long): Int {
        val old = DemoDirectory.person(tlId) ?: return 0
        val new = DemoDirectory.person(replacementId) ?: return 0
        val subs = DemoDirectory.people.filter { it.supervisorId == tlId && it.status != "DISABLED" }
        apply(subs.associate { it.id to DemoDirectory.Change(supervisorId = replacementId) } + (tlId to DemoDirectory.Change(status = "DISABLED")))
        val moved = partners.transferStores(tlId, replacementId, "TL toko pindah dari ${old.fullName} ke ${new.fullName} (TL lama dinonaktifkan)")
        subs.forEach { partners.releaseNotLedBy(it.id, replacementId, "atasan baru setelah ${old.fullName} dinonaktifkan") }
        return moved
    }

    /**
     * Pindahkan Bawahan (web FR-AM-012): [employeeId] moves to TL [toTl]; the old TL keeps their toko (OQ-AM-13), so the
     * SA/SR is released from toko whose TL is not [toTl] (FR-AM-013). Returns the toko released.
     */
    fun moveSubordinate(employeeId: Long, toTl: Long): Int {
        DemoDirectory.person(employeeId) ?: return 0
        apply(mapOf(employeeId to DemoDirectory.Change(supervisorId = toTl)))
        return partners.releaseNotLedBy(employeeId, toTl, "Pindahkan Bawahan").size
    }

    /** Disables SA/SR [employeeId]: every toko is released, logged as the system. Returns the toko released. */
    fun disableSales(employeeId: Long): Int {
        apply(mapOf(employeeId to DemoDirectory.Change(status = "DISABLED")))
        return partners.releaseAll(employeeId)
    }

    /** "Reset data demo". */
    fun reset() {
        DemoDirectory.changes = emptyMap()
        _changes.value = emptyMap()
        store.saveDirectory(emptyMap())
    }

    private fun apply(update: Map<Long, DemoDirectory.Change>) {
        val merged = DemoDirectory.changes.toMutableMap()
        update.forEach { (id, c) ->
            val prev = merged[id]
            merged[id] = DemoDirectory.Change(status = c.status ?: prev?.status, supervisorId = c.supervisorId ?: prev?.supervisorId)
        }
        DemoDirectory.changes = merged
        _changes.value = merged
        store.saveDirectory(merged)
    }
}
