package com.example.and_sales_portal.data.partner

import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.data.demo.DemoStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.ZoneId

/** Why an assignment is refused (PRD v3 C6). */
enum class AssignError { SaLimit, SrLimit, NotOwner, OtherArea, NotLeader, Taken }

/**
 * Partners of the organisation on this device (shared by every account, so an SR's submission shows up for their TL),
 * standing in for the Partner Pipeline API. Spec: REG-01 … REG-11, PRT-01 … PRT-02 (PRD v3 C2, C3, C4, D4).
 */
class PartnerRepository(private val store: DemoStore, private val clock: () -> Instant = Instant::now) {

    private val saved = store.loadPartners()
    private val _partners = MutableStateFlow(saved ?: PartnerSeed.partners())
    val partners: StateFlow<List<Partner>> = _partners.asStateFlow()

    private val _assignments = MutableStateFlow(store.loadAssignments() ?: AssignmentData())
    /** Assignment history and notifications (TL-03, PRD Scope 1 FR-MP-004, FR-MP-007). */
    val assignments: StateFlow<AssignmentData> = _assignments.asStateFlow()

    init {
        if (saved == null) store.savePartners(_partners.value)
    }

    fun now(): Long = clock().toEpochMilli()

    fun get(id: String): Partner? = _partners.value.firstOrNull { it.id == id }

    /** PRD C3: a TL sees own submissions and those of their SRs; an SR sees own submissions; an SA has none. */
    fun submissionsFor(userId: Long, role: SalesRole): List<Partner> = when (role) {
        SalesRole.TL -> _partners.value.filter { it.ownerTlId == userId }
        SalesRole.SR -> _partners.value.filter { it.submittedBy == userId }
        SalesRole.SA -> emptyList()
    }.sortedByDescending { it.submittedAt }

    /** PRD C4 / D4: TL the Active partners they own; SA/SR the Active partners with a toko assigned to them. */
    fun myPartners(userId: Long, role: SalesRole): List<Partner> = _partners.value.filter { p ->
        p.status == PartnerStatus.ACTIVE && when (role) {
            SalesRole.TL -> p.ownerTlId == userId
            SalesRole.SR, SalesRole.SA -> p.stores.any { userId in it.assigned }
        }
    }.sortedBy { it.name }

    /** Active toko of Active partners (for visits). */
    fun activeStores(): List<Pair<Partner, Store>> =
        _partners.value.filter { it.status == PartnerStatus.ACTIVE }.flatMap { p -> p.stores.filter { it.status == "ACTIVE" }.map { p to it } }

    /** Next registration number `REGYYYY-NNNN` (PRD C2 step 6). */
    fun nextRegistrationNumber(): String {
        val year = clock().atZone(ZONE).year
        val prefix = "REG$year-"
        val last = _partners.value.mapNotNull { it.id.takeIf { id -> id.startsWith(prefix) }?.removePrefix(prefix)?.toIntOrNull() }.maxOrNull() ?: 0
        return prefix + (last + 1).toString().padStart(4, '0')
    }

    /** New submission: status Under Review, routed to the Admin's Partner Pipeline. */
    fun submit(partner: Partner): Partner {
        val p = partner.copy(
            id = nextRegistrationNumber(),
            status = PartnerStatus.UNDER_REVIEW,
            history = listOf(StatusEvent(partner.submittedAt, null, PartnerStatus.UNDER_REVIEW, partner.submittedBy.toString(), "Pengajuan dikirim dari aplikasi mobile")),
            ownerTl = Partner.defaultOwner(partner.submittedBy),
        ).let { p -> p.copy(stores = p.stores.mapIndexed { i, s -> s.copy(id = "${p.id}-${i + 1}") }) }
        change { it + p }
        return p
    }

    /** PRD C3 "Perbaiki & Kirim Ulang": Revision Required → Under Review (system transition). */
    fun resubmit(updated: Partner, by: Long) {
        val at = now()
        val items = updated.revision?.items.orEmpty()
        val p = PartnerActions.move(updated, PartnerStatus.UNDER_REVIEW, at, by.toString(), "Perbaikan dikirim ulang: ${items.joinToString { it.label }}")
        replace(p)
    }

    /** PRD C4 Tambah Toko: saved immediately as active and visible to the Admin. */
    fun addStore(partnerId: String, store: Store): Store {
        val p = requireNotNull(get(partnerId))
        val n = p.stores.size + 1
        val s = store.copy(id = "${p.id}-$n", code = "TK${p.id.takeLast(4)}-${n.toString().padStart(2, '0')}", status = "ACTIVE", primary = false)
        replace(p.copy(stores = p.stores + s, outletCount = maxOf(p.outletCount, p.stores.size + 1)))
        return s
    }

    // region Unique per partner (review 2026-10-09)

    /** Partners a new registration must not clash with: every partner except Rejected / Cancelled ones and [exceptId]. */
    private fun registered(exceptId: String?) =
        _partners.value.filter { it.id != exceptId && it.status != PartnerStatus.REJECTED && it.status != PartnerStatus.CANCELLED }

    /** Kode Referral identifies the partner (loans from the partner carry it), so it is unique, ignoring case. */
    fun referralTaken(code: String, exceptId: String? = null): Boolean =
        code.isNotBlank() && registered(exceptId).any { it.referralCode.equals(code.trim(), ignoreCase = true) }

    /** No. Handphone PIC is used to sign in to the Partner account, so it is unique per partner (`8…`, 0/62 removed). */
    fun picPhoneTaken(phone: String, exceptId: String? = null): Boolean {
        val n = normalizePhone(phone)
        return n.isNotEmpty() && registered(exceptId).any { normalizePhone(it.picPhone) == n }
    }

    // endregion

    // region Assign SA/SR (TL-03; PRD C6, stakeholder revision 2026-10-08: per toko)

    /** Active partners with a toko assigned to [employeeId]. */
    fun assignedPartners(employeeId: Long): List<Partner> =
        _partners.value.filter { p -> p.status == PartnerStatus.ACTIVE && p.stores.any { employeeId in it.assigned } }

    /** Active toko of Active partners assigned to [employeeId], with their partner. */
    fun assignedStores(employeeId: Long): List<Pair<Partner, Store>> =
        activeStores().filter { (_, s) -> employeeId in s.assigned }

    /** Assigned toko below the role's minimum (SA 1, SR 4): only a marker, never blocks (revision 2026-10-08). */
    fun belowMinimum(employeeId: Long): Boolean {
        val role = DemoDirectory.person(employeeId)?.role ?: return false
        return assignedStores(employeeId).size < storeLimits(role).first
    }

    /**
     * Why [employeeId] cannot be assigned to toko [storeId] by [tlId]: SA max 3 toko, SR max 6; only active toko of
     * Active partners owned by the TL in the person's area; the TL must be the person's direct leader.
     */
    fun assignmentError(tlId: Long, employeeId: Long, storeId: String): AssignError? {
        val person = DemoDirectory.person(employeeId) ?: return AssignError.NotLeader
        val p = _partners.value.firstOrNull { pt -> pt.stores.any { it.id == storeId } } ?: return AssignError.NotOwner
        val store = p.stores.first { it.id == storeId }
        val current = assignedStores(employeeId).count { (_, s) -> s.id != storeId }
        return when {
            person.supervisorId != tlId -> AssignError.NotLeader
            p.ownerTlId != tlId || p.status != PartnerStatus.ACTIVE || store.status != "ACTIVE" -> AssignError.NotOwner
            p.areaId != person.areaId -> AssignError.OtherArea
            // DD review point 5 (decision PO 2026-10-12): one toko = one SA/SR.
            store.assigned.any { it != employeeId } -> AssignError.Taken
            current >= storeLimits(person.role).last -> if (person.role == "SA") AssignError.SaLimit else AssignError.SrLimit
            else -> null
        }
    }

    /** Assigns one toko (revision 2026-10-08: assignment per toko, not per partner); logged and notified. */
    fun assign(tlId: Long, employeeId: Long, storeId: String): AssignError? {
        assignmentError(tlId, employeeId, storeId)?.let { return it }
        changeStore(storeId) { if (employeeId in it.assigned) it else it.copy(assigned = it.assigned + employeeId) }
        log(AssignmentAction.ASSIGN, employeeId, storeId, tlId.toString())
        return null
    }

    /** Removes [employeeId] from toko [storeId] (Lepas by [by]); logged and notified. */
    fun unassign(employeeId: Long, storeId: String, by: Long) {
        val assigned = _partners.value.flatMap { it.stores }.firstOrNull { it.id == storeId }?.assigned.orEmpty()
        if (employeeId !in assigned) return
        changeStore(storeId) { it.copy(assigned = it.assigned - employeeId) }
        log(AssignmentAction.RELEASE, employeeId, storeId, by.toString())
    }

    /** SA/SR holding toko [storeId], if any (one toko = one SA/SR). */
    fun holderOf(storeId: String): Long? = _partners.value.flatMap { it.stores }.firstOrNull { it.id == storeId }?.assigned?.firstOrNull()

    /**
     * The SA/SR account was disabled (web ACC-05, DD review point 5): every toko is released, logged as the system.
     * Returns how many toko were released.
     */
    fun releaseAll(employeeId: Long): Int {
        val ids = _partners.value.flatMap { it.stores }.filter { employeeId in it.assigned }.map { it.id }
        ids.forEach { id ->
            changeStore(id) { it.copy(assigned = it.assigned - employeeId) }
            log(AssignmentAction.RELEASE, employeeId, id, AssignmentEvent.SYSTEM, AssignmentEvent.ACCOUNT_DISABLED)
        }
        return ids.size
    }

    /**
     * The owning TL [fromTl] was disabled (web ACC-05, DD review point 7): every partner they own moves to [toTl]. Loans
     * keep the TL who owned the partner on their date ([Partner.ownerAt]). Returns how many partners moved.
     */
    fun transferOwnership(fromTl: Long, toTl: Long, reason: String): Int {
        val moved = _partners.value.filter { it.ownerTlId == fromTl }.map { it.id }.toSet()
        if (moved.isEmpty()) return 0
        val at = now()
        change { list ->
            list.map { p -> if (p.id in moved) p.copy(ownerTl = toTl, ownerHistory = p.ownerHistory + OwnerChange(at, fromTl, toTl, reason)) else p }
        }
        return moved.size
    }

    // region Holder by date (pending fix 2026-10-13): the past stays with the person of that date.

    /** SA/SR holding toko [storeId] at [at] (millis): loans of that date are theirs (one toko = one SA/SR). */
    fun holderAt(storeId: String, at: Long): Long? = heldAt(storeId, at).firstOrNull()

    /** The current holders of [storeId] with every Tugaskan / Lepas after [at] undone, newest first. */
    private fun heldAt(storeId: String, at: Long): Set<Long> {
        val held = _partners.value.firstNotNullOfOrNull { p -> p.stores.firstOrNull { it.id == storeId } }?.assigned?.toMutableSet() ?: return emptySet()
        _assignments.value.events.filter { it.storeId == storeId && it.at > at }.asReversed().forEach { e ->
            if (e.action == AssignmentAction.ASSIGN) held -= e.employeeId else held += e.employeeId
        }
        return held
    }

    /**
     * Toko [employeeId] visits in a week (decision 2026-10-14): held on its Monday ([monday], end of day) and still held
     * at [until] (end of the week, or now). SA/SR: their toko; TL: the toko of the partners they own. A toko assigned
     * mid-week counts from the next week; one released mid-week leaves that week; a closed week never changes.
     */
    fun weekStores(employeeId: Long, role: SalesRole, monday: Long, until: Long): List<Pair<Partner, Store>> =
        _partners.value.flatMap { p ->
            p.stores.filter { s ->
                listOf(monday, until).all { at ->
                    activeAt(p, s, at) && when (role) {
                        SalesRole.TL -> p.ownerAt(at) == employeeId
                        SalesRole.SA, SalesRole.SR -> employeeId in heldAt(s.id, at)
                    }
                }
            }.map { p to it }
        }

    private fun activeAt(p: Partner, s: Store, at: Long): Boolean =
        s.addedAt <= at && p.history.filter { it.at <= at }.maxByOrNull { it.at }?.to == PartnerStatus.ACTIVE

    // endregion

    /** Tugaskan / Lepas of [employeeId], newest first (the later one first at the same time). */
    fun historyOf(employeeId: Long): List<AssignmentEvent> = _assignments.value.events.filter { it.employeeId == employeeId }.asReversed().sortedByDescending { it.at }

    /** Notifications of [employeeId], newest first. */
    fun notificationsOf(employeeId: Long): List<AppNotification> =
        _assignments.value.notifications.filter { it.employeeId == employeeId }.asReversed().sortedByDescending { it.at }

    fun markNotificationsRead(employeeId: Long) = changeAssignments { d ->
        d.copy(notifications = d.notifications.map { if (it.employeeId == employeeId) it.copy(read = true) else it })
    }

    /** Logs a Tugaskan / Lepas and notifies the SA/SR (FR-MP-007, FR-MP-004). */
    private fun log(action: AssignmentAction, employeeId: Long, storeId: String, by: String, reason: String? = null) {
        val p = _partners.value.firstOrNull { pt -> pt.stores.any { it.id == storeId } } ?: return
        val s = p.stores.first { it.id == storeId }
        val e = AssignmentEvent(now(), action, employeeId, storeId, s.name, p.id, p.name, by, reason)
        changeAssignments { d ->
            d.copy(events = d.events + e, notifications = d.notifications + AppNotification("N-${e.at}-$employeeId-$storeId-${action.name}", employeeId, e))
        }
    }

    private fun changeAssignments(f: (AssignmentData) -> AssignmentData) {
        _assignments.update(f)
        store.saveAssignments(_assignments.value)
    }

    private fun changeStore(storeId: String, f: (Store) -> Store) {
        val p = _partners.value.firstOrNull { pt -> pt.stores.any { it.id == storeId } } ?: return
        replace(p.copy(stores = p.stores.map { if (it.id == storeId) f(it) else it }))
    }

    // endregion

    // region Simulasi Admin (debug): the web portal's actions, so the whole lifecycle can be tried on the device.

    fun adminRequestRevision(id: String) {
        val p = get(id) ?: return
        val doc = p.documents.firstOrNull { it.key == DocType.FOTO_DEPAN.name } ?: p.documents.first()
        val items = listOf(
            RevisionItem(RevisionItem.DOC, doc.key, DocType.of(doc.key)?.adminLabel ?: doc.key, "Foto buram, mohon ambil ulang dengan cahaya yang cukup"),
            RevisionItem(RevisionItem.SEC, Sections.BANK, "Data Rekening", "Nomor rekening tidak sesuai dengan buku rekening"),
        )
        val at = now()
        val withNotes = p.copy(
            revision = RevisionRequest(at, ADMIN, "Mohon diperbaiki maksimal 2 hari kerja.", items),
            documents = p.documents.map { if (it.key == doc.key) it.copy(verification = Verification.NEEDS_REVISION, note = items[0].note) else it },
        )
        replace(PartnerActions.move(withNotes, PartnerStatus.REVISION_REQUIRED, at, ADMIN, "${items.size} item diminta revisi: ${items.joinToString { it.label }}"))
        // DD review point 2 (decision PO 2026-10-12): the submitter and the owning TL (when different) are notified.
        val notice = RevisionNotice(at, p.id, p.name)
        val to = listOfNotNull(p.submittedBy, p.ownerTlId).distinct()
        changeAssignments { d -> d.copy(notifications = d.notifications + to.map { AppNotification("N-$at-$it-${p.id}-REVISION", it, revision = notice) }) }
    }


    fun adminVerify(id: String) = adminMove(id, PartnerStatus.VERIFIED, "Seluruh dokumen wajib dan data rekening valid") { p ->
        p.copy(documents = p.documents.map { it.copy(verification = Verification.VALID, note = null) })
    }

    fun adminSendPks(id: String) {
        val p = get(id) ?: return
        val via = p.privyId?.let { "via Privy ID $it" } ?: "via email ${p.pksInviteEmail ?: p.picEmail}"
        replace(PartnerActions.move(p, PartnerStatus.WAITING_PKS, now(), ADMIN, "PKS dikirim di Privy web $via"))
    }

    fun adminActivate(id: String) = adminMove(id, PartnerStatus.ACTIVE, "PKS ditandatangani (dicek di Privy web); partner diaktifkan") { it }
    fun adminReject(id: String) = adminMove(id, PartnerStatus.REJECTED, "Usaha tidak sesuai kriteria partner") { it }
    fun adminCancel(id: String) = adminMove(id, PartnerStatus.CANCELLED, "Dibatalkan atas permintaan partner") { it }
    /** Inactive releases every SA/SR from the partner's toko; each release is logged as the system (FR-MP-007). */
    fun adminDeactivate(id: String) {
        val p = get(id) ?: return
        val released = p.stores.flatMap { s -> s.assigned.map { it to s.id } }
        adminMove(id, PartnerStatus.INACTIVE, "Partner berhenti bekerja sama") { it.copy(stores = it.stores.map { s -> s.copy(assigned = emptyList()) }) }
        released.forEach { (employeeId, storeId) -> log(AssignmentAction.RELEASE, employeeId, storeId, AssignmentEvent.SYSTEM, AssignmentEvent.PARTNER_INACTIVE) }
    }

    private fun adminMove(id: String, to: PartnerStatus, reason: String?, prepare: (Partner) -> Partner) {
        val p = get(id) ?: return
        replace(PartnerActions.move(prepare(p), to, now(), ADMIN, reason))
    }

    // endregion

    /** "Reset data demo": back to the sample partners. */
    fun reset() {
        _partners.value = PartnerSeed.partners()
        store.savePartners(_partners.value)
        _assignments.value = AssignmentData()
        store.saveAssignments(_assignments.value)
    }

    fun replace(p: Partner) = change { list -> list.map { if (it.id == p.id) p else it } }

    private fun change(f: (List<Partner>) -> List<Partner>) {
        _partners.update(f)
        store.savePartners(_partners.value)
    }

    companion object {
        private val ZONE: ZoneId = ZoneId.of("Asia/Jakarta")
        const val ADMIN = "Rina Saraswati (Admin)"
        /** Toko per SA (1–3) and SR (4–6), revision 2026-10-08: the maximum blocks, the minimum is only a marker. */
        val SA_STORES = 1..3
        val SR_STORES = 4..6

        fun storeLimits(role: String): IntRange = if (role == "SA") SA_STORES else SR_STORES

        /** Phone digits as stored (`8…`): `08…`, `62…`, `+62…` and `8…` are the same number. */
        fun normalizePhone(phone: String): String {
            val d = phone.filter(Char::isDigit)
            return when {
                d.startsWith("62") -> d.removePrefix("62")
                d.startsWith("0") -> d.removePrefix("0")
                else -> d
            }
        }

        /** Name of the person who moved a status (user id or Admin name). */
        fun actorName(by: String): String = by.toLongOrNull()?.let { DemoDirectory.person(it)?.fullName } ?: by
    }
}
