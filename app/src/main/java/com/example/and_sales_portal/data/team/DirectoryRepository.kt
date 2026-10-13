package com.example.and_sales_portal.data.team

import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.demo.DemoStore
import com.example.and_sales_portal.data.partner.PartnerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Account changes made by the Admin on the web that the Android sample must reflect (web ACC-05, DD review points 5
 * and 7, decision PO 2026-10-12): a disabled account and a replaced leader. Saved on the device and applied to
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
     * Disables TL [tlId]: their SA/SR move to [replacementId] and the partners they own move to [replacementId] (past
     * sales stay with [tlId]). Returns the number of partners moved.
     */
    fun disableTl(tlId: Long, replacementId: Long): Int {
        val old = DemoDirectory.person(tlId) ?: return 0
        val new = DemoDirectory.person(replacementId) ?: return 0
        val subs = DemoDirectory.people.filter { it.supervisorId == tlId && it.status != "DISABLED" }
        apply(subs.associate { it.id to DemoDirectory.Change(supervisorId = replacementId) } + (tlId to DemoDirectory.Change(status = "DISABLED")))
        return partners.transferOwnership(tlId, replacementId, "Kepemilikan partner pindah dari ${old.fullName} ke ${new.fullName} (TL lama dinonaktifkan)")
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
