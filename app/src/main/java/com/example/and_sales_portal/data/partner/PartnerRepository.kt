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

/**
 * Why an assignment is refused (PRD Scope 1 v1.1, 1.5 FR-MP-002): "Bukan anggota tim Anda", "Toko belum Active",
 * "Toko bukan pegangan Anda", "Toko sudah ditugaskan ke {nama}". No limit of toko per SA/SR (C6).
 */
enum class AssignError { NotTeam, NotActive, NotHolder, Taken }

/**
 * Partners of the organisation on this device (shared by every account, so an SR's submission shows up for their TL),
 * standing in for the Partner Pipeline API. Spec: REG-01 … REG-11, PRT-01 … PRT-02 (PRD v3 C2, C3, C4, D4).
 */
class PartnerRepository(private val store: DemoStore, private val clock: () -> Instant = Instant::now) {

    private val saved = store.loadPartners()
    private val _partners = MutableStateFlow(saved ?: PartnerSeed.partners())
    val partners: StateFlow<List<Partner>> = _partners.asStateFlow()

    private val _assignments = MutableStateFlow(store.loadAssignments() ?: PartnerSeed.assignments())
    /** Assignment history and notifications (TL-03, PRD Scope 1 FR-MP-004, FR-MP-007). */
    val assignments: StateFlow<AssignmentData> = _assignments.asStateFlow()

    init {
        if (saved == null) {
            store.savePartners(_partners.value)
            // The sample history belongs to the sample partners (Tamalanrea released by the APL).
            _assignments.value = PartnerSeed.assignments()
            store.saveAssignments(_assignments.value)
        }
    }

    fun now(): Long = clock().toEpochMilli()

    fun get(id: String): Partner? = _partners.value.firstOrNull { it.id == id }

    /**
     * PRD C3: a TL sees own submissions, those of their SRs and partners where they hold a kursi TL (a submission has no
     * kursi until activation, PRD Scope 1 v1.1 C6); an SR sees own submissions; an SA has none.
     */
    fun submissionsFor(userId: Long, role: SalesRole): List<Partner> = when (role) {
        SalesRole.TL -> _partners.value.filter { it.submittedBy == userId || DemoDirectory.person(it.submittedBy)?.supervisorId == userId || it.hasTl(userId) }
        SalesRole.SR -> _partners.value.filter { it.submittedBy == userId }
        SalesRole.SA -> emptyList()
    }.sortedByDescending { it.submittedAt }

    /**
     * FR-PA-012 (PRD Scope 1 v1.1 C6): TL the Active partners where they hold a kursi TL; SA/SR the Active partners where
     * they hold a kursi SA/SR. Each sees only the toko of their kursi ([storesOf]).
     */
    fun myPartners(userId: Long, role: SalesRole): List<Partner> = _partners.value
        .filter { it.status == PartnerStatus.ACTIVE }
        .mapNotNull { p -> storesOf(p, userId, role).takeIf { it.isNotEmpty() }?.let { p.copy(stores = it) } }
        .sortedBy { it.name }

    /** Toko of [p] where [userId] holds the kursi of their [role] (PRT-01 shows only these, FR-PA-012). */
    fun storesOf(p: Partner, userId: Long, role: SalesRole): List<Store> = p.stores.filter { s ->
        when (role) {
            SalesRole.TL -> s.tlId == userId
            SalesRole.SR, SalesRole.SA -> userId in s.assigned
        }
    }

    /** FR-PA-013: a TL adds toko to a partner where they hold ≥ 1 kursi TL, an SR where they hold ≥ 1 kursi SA/SR. */
    fun canAddStore(p: Partner, userId: Long, role: SalesRole): Boolean =
        p.status == PartnerStatus.ACTIVE && role != SalesRole.SA && storesOf(p, userId, role).isNotEmpty()

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
        ).let { p ->
            // C6: no kursi until activation ([adminActivate]).
            p.copy(stores = p.stores.mapIndexed { i, s -> s.copy(id = "${p.id}-${i + 1}", tlId = null, tlLog = emptyList(), assigned = emptyList(), referralCode = null, referralQr = null) })
        }
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

    /**
     * PRD C4 Tambah Toko on an Active partner ([canAddStore]): the toko waits for its Kode Referral and QR from the Admin
     * (OQ-25 option 1) and becomes active, with its toko code, once they are filled ([adminFillReferral]). It starts
     * without kursi (PRD Scope 1 v1.1 C6, FR-PA-013): "Belum ada TL" until the APL assigns one; it never inherits an SA/SR.
     */
    fun addStore(partnerId: String, store: Store): Store {
        val p = requireNotNull(get(partnerId))
        val n = p.stores.size + 1
        val s = store.copy(
            id = "${p.id}-$n", code = null, status = Store.WAITING_REFERRAL, primary = false,
            tlId = null, tlLog = emptyList(), referralCode = null, referralQr = null, assigned = emptyList(),
        )
        replace(p.copy(stores = p.stores + s, outletCount = maxOf(p.outletCount, p.stores.size + 1)))
        return s
    }

    // region Unique per partner (review 2026-10-09)

    /** Partners a new registration must not clash with: every partner except Rejected / Cancelled ones and [exceptId]. */
    private fun registered(exceptId: String?) =
        _partners.value.filter { it.id != exceptId && it.status != PartnerStatus.REJECTED && it.status != PartnerStatus.CANCELLED }

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

    /**
     * Why [employeeId] cannot be assigned to toko [storeId] by [tlId] (1.5 FR-MP-002): the SA/SR must be the TL's direct
     * subordinate, the toko Active and held by the TL, and free (one toko = one SA/SR). No limit of toko per SA/SR (C6).
     */
    fun assignmentError(tlId: Long, employeeId: Long, storeId: String): AssignError? {
        val person = DemoDirectory.person(employeeId)
        val p = _partners.value.firstOrNull { pt -> pt.stores.any { it.id == storeId } } ?: return AssignError.NotActive
        val store = p.stores.first { it.id == storeId }
        return when {
            person == null || person.supervisorId != tlId -> AssignError.NotTeam
            p.status != PartnerStatus.ACTIVE || store.status != Store.ACTIVE -> AssignError.NotActive
            store.tlId != tlId -> AssignError.NotHolder
            store.assigned.any { it != employeeId } -> AssignError.Taken
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

    /** Removes [employeeId] from toko [storeId] (Lepas by the TL [by]); logged and notified "dicabut (dilepas <TL>)". */
    fun unassign(employeeId: Long, storeId: String, by: Long) {
        val assigned = _partners.value.flatMap { it.stores }.firstOrNull { it.id == storeId }?.assigned.orEmpty()
        if (employeeId !in assigned) return
        changeStore(storeId) { it.copy(assigned = it.assigned - employeeId) }
        log(AssignmentAction.RELEASE, employeeId, storeId, by.toString(), source = "dilepas ${actorName(by.toString())}")
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
            log(AssignmentAction.RELEASE, employeeId, id, AssignmentEvent.SYSTEM, AssignmentEvent.ACCOUNT_DISABLED, "akun dinonaktifkan")
        }
        return ids.size
    }

    /**
     * The TL [fromTl] was disabled (web ACC-05 handover): their kursi TL on active toko move to [toTl]. Loans keep the TL
     * of their date ([Store.tlAt]). No notification (decision 2026-10-17). Returns how many toko moved.
     */
    fun transferStores(fromTl: Long, toTl: Long): Int {
        val ids = activeStores().filter { (_, s) -> s.tlId == fromTl }.map { it.second.id }.toSet()
        if (ids.isEmpty()) return 0
        val at = now()
        ids.forEach { id -> changeStore(id) { it.copy(tlId = toTl, tlLog = it.tlLog + TlChange(at, fromTl, toTl, ADMIN, "TL dinonaktifkan")) } }
        return ids.size
    }

    // region Kursi TL (1.7 Mapping TL; the APL acts on the web, the prototype simulates it)

    /**
     * 1.7 FR-004 Tugaskan TL: the APL [apl] gives an Active toko without TL to TL [tlId] (Active, same area). The TL is
     * notified "Anda ditugaskan ke {toko} ({partner}) oleh {APL}." (FR-006). `false` when the toko cannot take it.
     */
    fun aplAssignTl(storeId: String, tlId: Long, apl: String): Boolean {
        val p = _partners.value.firstOrNull { pt -> pt.stores.any { it.id == storeId } } ?: return false
        val s = p.stores.first { it.id == storeId }
        val tl = DemoDirectory.person(tlId)
        if (p.status != PartnerStatus.ACTIVE || s.status != Store.ACTIVE || s.tlId != null) return false
        if (tl == null || tl.role != "TL" || tl.status != "ACTIVE" || tl.areaId != p.areaId) return false
        changeStore(storeId) { it.copy(tlId = tlId, tlLog = it.tlLog + TlChange(now(), null, tlId, apl, "APL")) }
        log(AssignmentAction.ASSIGN, tlId, storeId, apl, seat = AssignmentEvent.TL)
        return true
    }

    /**
     * 1.7 FR-005 Lepas TL by the APL [apl]: the SA/SR of the toko is released too ("TL dilepas"). Returns the SA/SR
     * released, or `null` when the toko has no TL.
     */
    fun aplReleaseTl(storeId: String, apl: String): List<Long>? {
        val s = _partners.value.flatMap { it.stores }.firstOrNull { it.id == storeId } ?: return null
        if (s.tlId == null) return null
        releaseTlSeat(storeId, apl, "dilepas APL")
        return s.assigned
    }

    /**
     * C6 kursi release: the SA/SR (source "TL dilepas", or [source] for Inactive / Ubah Area) and then the kursi TL; the TL
     * is notified "Anda dilepas dari {toko} ({partner}). Penugasan {SA/SR} di toko ini juga dicabut." (FR-006).
     */
    private fun releaseTlSeat(storeId: String, by: String, source: String) {
        val s = _partners.value.flatMap { it.stores }.firstOrNull { it.id == storeId } ?: return
        val salesSource = if (source == "dilepas APL") "TL dilepas" else source
        s.assigned.forEach { x ->
            changeStore(storeId) { it.copy(assigned = it.assigned - x) }
            log(AssignmentAction.RELEASE, x, storeId, by, AssignmentEvent.TL_RELEASED, salesSource)
        }
        val tl = s.tlId ?: return
        changeStore(storeId) { it.copy(tlId = null, tlLog = it.tlLog + TlChange(now(), tl, null, by, source)) }
        log(AssignmentAction.RELEASE, tl, storeId, by, AssignmentEvent.TL_RELEASED, source, AssignmentEvent.TL, s.assigned.mapNotNull { DemoDirectory.person(it)?.fullName })
    }

    // endregion

    /**
     * FR-AM-013 (per toko, decision 2026-10-16): [employeeId] got a new leader, so every toko whose TL is not that leader is
     * released, logged as the system with [source] ("Pindahkan Bawahan", "atasan baru setelah … dinonaktifkan").
     * Returns the toko released.
     */
    fun releaseNotLedBy(employeeId: Long, leaderId: Long, source: String): List<String> =
        assignedStores(employeeId).map { it.second }.filter { it.tlId != leaderId }.map { it.id }.onEach { releaseBySystem(employeeId, it, source) }

    private fun releaseBySystem(employeeId: Long, storeId: String, source: String) {
        changeStore(storeId) { it.copy(assigned = it.assigned - employeeId) }
        log(AssignmentAction.RELEASE, employeeId, storeId, AssignmentEvent.SYSTEM, AssignmentEvent.LEADER_CHANGED, source)
    }

    /** The SA/SR kursi events of [storeId] (kursi TL events are not holders). */
    private fun salesEvents(storeId: String) = _assignments.value.events.filter { it.storeId == storeId && it.seat == AssignmentEvent.SALES }

    // region Holder by date (pending fix 2026-10-13): the past stays with the person of that date.

    /** SA/SR holding toko [storeId] at [at] (millis): loans of that date are theirs (one toko = one SA/SR). */
    fun holderAt(storeId: String, at: Long): Long? = heldAt(storeId, at).firstOrNull()

    /** The current holders of [storeId] with every Tugaskan / Lepas after [at] undone, newest first. */
    private fun heldAt(storeId: String, at: Long): Set<Long> {
        val held = _partners.value.firstNotNullOfOrNull { p -> p.stores.firstOrNull { it.id == storeId } }?.assigned?.toMutableSet() ?: return emptySet()
        salesEvents(storeId).filter { it.at > at }.asReversed().forEach { e ->
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
                        SalesRole.TL -> s.tlAt(at) == employeeId
                        SalesRole.SA, SalesRole.SR -> employeeId in heldAt(s.id, at)
                    }
                }
            }.map { p to it }
        }

    private fun activeAt(p: Partner, s: Store, at: Long): Boolean =
        s.status != Store.WAITING_REFERRAL && s.addedAt <= at && p.history.filter { it.at <= at }.maxByOrNull { it.at }?.to == PartnerStatus.ACTIVE

    // endregion

    /** Tugaskan / Lepas of the SA/SR [employeeId], newest first (the later one first at the same time). */
    fun historyOf(employeeId: Long): List<AssignmentEvent> =
        _assignments.value.events.filter { it.employeeId == employeeId && it.seat == AssignmentEvent.SALES }.asReversed().sortedByDescending { it.at }

    /** Notifications of [employeeId], newest first. */
    fun notificationsOf(employeeId: Long): List<AppNotification> =
        _assignments.value.notifications.filter { it.employeeId == employeeId }.asReversed().sortedByDescending { it.at }

    fun markNotificationsRead(employeeId: Long) = changeAssignments { d ->
        d.copy(notifications = d.notifications.map { if (it.employeeId == employeeId) it.copy(read = true) else it })
    }

    /** Logs a Tugaskan / Lepas and notifies the person (FR-MP-007, FR-MP-004; kursi TL 1.7 FR-006). */
    private fun log(
        action: AssignmentAction, employeeId: Long, storeId: String, by: String, reason: String? = null, source: String? = null,
        seat: String = AssignmentEvent.SALES, withSales: List<String> = emptyList(),
    ) {
        val p = _partners.value.firstOrNull { pt -> pt.stores.any { it.id == storeId } } ?: return
        val s = p.stores.first { it.id == storeId }
        val e = AssignmentEvent(now(), action, employeeId, storeId, s.name, p.id, p.name, by, reason, source, seat, withSales)
        changeAssignments { d ->
            d.copy(events = d.events + e, notifications = d.notifications + AppNotification("N-${e.at}-$employeeId-$storeId-$seat-${action.name}", employeeId, e))
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
        // DD review point 2 (decision PO 2026-10-12): the submitter and their TL (a submission has no kursi TL yet).
        val notice = RevisionNotice(at, p.id, p.name)
        val to = listOfNotNull(p.submittedBy, Partner.defaultTl(p.submittedBy)).distinct()
        changeAssignments { d -> d.copy(notifications = d.notifications + to.map { AppNotification("N-$at-$it-${p.id}-REVISION", it, revision = notice) }) }
    }


    fun adminVerify(id: String) = adminMove(id, PartnerStatus.VERIFIED, "Seluruh dokumen wajib dan data rekening valid") { p ->
        p.copy(documents = p.documents.map { it.copy(verification = Verification.VALID, note = null) })
    }

    fun adminSendPks(id: String) {
        val p = get(id) ?: return
        // PRD Scope 1 v2 C3: PKS goes to the Privy ID when filled, otherwise to the Email PIC (no separate invitation email).
        val via = p.privyId?.let { "via Privy ID $it" } ?: "via Email PIC ${p.picEmail}"
        replace(PartnerActions.move(p, PartnerStatus.WAITING_PKS, now(), ADMIN, "PKS dikirim di Privy web $via"))
    }

    /**
     * Every toko needs a Kode Referral and QR before activation (FR-010, AC-028); `false` when one is missing. C6 FR-010:
     * kursi TL = the TL who submitted, or the TL of the SR who did, when that account is Active, otherwise "Belum ada TL";
     * kursi SA/SR empty (the SR who submitted does not hold the toko).
     */
    fun adminActivate(id: String): Boolean {
        if (referralMissing(id)) return false
        val p = get(id) ?: return false
        val tl = Partner.defaultTl(p.submittedBy)?.let { DemoDirectory.person(it) }?.takeIf { it.role == "TL" && it.status == "ACTIVE" }?.id
        val at = now()
        adminMove(id, PartnerStatus.ACTIVE, "PKS ditandatangani (dicek di Privy web); partner diaktifkan") { pt ->
            pt.copy(stores = pt.stores.map { s ->
                s.copy(assigned = emptyList(), tlId = tl, tlLog = if (tl != null) s.tlLog + TlChange(at, null, tl, "Sistem", "aktivasi") else s.tlLog)
            })
        }
        return true
    }

    /** Whether a toko of the partner has no Kode Referral or QR yet. */
    fun referralMissing(id: String): Boolean = get(id)?.stores?.any { it.referralCode == null || it.referralQr == null } ?: false

    /**
     * Simulasi Admin "Isi kode referral" (web Toko Aktif / tab Toko, FR-019): every toko without a code gets a sample code
     * (no required prefix, PRD 1.1) and its QR; a toko waiting for them becomes Active with its toko code and "Belum ada
     * TL" until the APL assigns one. Returns how many toko were filled.
     */
    fun adminFillReferral(id: String): Int {
        val p = get(id) ?: return 0
        val area = DemoDirectory.area(p.areaId)?.code ?: return 0
        val at = now()
        var filled = 0
        val events = mutableListOf<StatusEvent>()
        val stores = p.stores.mapIndexed { i, s ->
            if (s.referralCode != null && s.referralQr != null) return@mapIndexed s
            filled++
            val code = s.referralCode ?: "OF$area${p.id.takeLast(3)}${i + 1}"
            val qr = "qr_${code.lowercase()}.png"
            events += StatusEvent(at, null, p.status, ADMIN, "Kode referral ${s.name}: $code, QR $qr")
            if (s.status == Store.WAITING_REFERRAL) {
                events += StatusEvent(at, null, p.status, "Sistem", "${s.name} aktif setelah kode referral dan QR diisi; menunggu penugasan TL oleh APL")
                s.copy(referralCode = code, referralQr = qr, status = Store.ACTIVE, code = "TK${p.id.takeLast(4)}-${(i + 1).toString().padStart(2, '0')}")
            } else {
                s.copy(referralCode = code, referralQr = qr)
            }
        }
        if (filled > 0) replace(p.copy(stores = stores, history = p.history + events))
        return filled
    }

    fun adminReject(id: String) = adminMove(id, PartnerStatus.REJECTED, "Usaha tidak sesuai kriteria partner") { it }
    fun adminCancel(id: String) = adminMove(id, PartnerStatus.CANCELLED, "Dibatalkan atas permintaan partner") { it }
    /** C6 FR-013: Inactive releases every kursi (TL and SA/SR) of the partner's toko, logged as the system (FR-MP-007). */
    fun adminDeactivate(id: String) {
        val p = get(id) ?: return
        p.stores.forEach { releaseTlSeat(it.id, AssignmentEvent.SYSTEM, "partner nonaktif") }
        adminMove(id, PartnerStatus.INACTIVE, "Partner berhenti bekerja sama") { it }
    }

    /** Statuses where the Admin can change the partner's area (FR-021: Under Review up to Active). */
    fun canChangeArea(id: String): Boolean = get(id)?.status in AREA_EDITABLE

    /**
     * C6 FR-021 Ubah Area Partner (Simulasi Admin): the partner and all its toko move to the next area of the sample; every
     * kursi TL and SA/SR is released (source "ubah area"). Returns the new area name, or `null` when not allowed.
     */
    fun adminChangeArea(id: String): String? {
        val p = get(id)?.takeIf { it.status in AREA_EDITABLE } ?: return null
        val areas = DemoDirectory.areas
        val to = areas[(areas.indexOfFirst { it.id == p.areaId } + 1) % areas.size]
        p.stores.forEach { releaseTlSeat(it.id, ADMIN, "ubah area") }
        val from = DemoDirectory.area(p.areaId)?.name.orEmpty()
        val released = get(id) ?: return null
        replace(released.copy(areaId = to.id, history = released.history + StatusEvent(now(), null, released.status, ADMIN, "Area partner diubah dari $from ke ${to.name}: simulasi")))
        return to.name
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
        _assignments.value = PartnerSeed.assignments()
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
        /** FR-021: Ubah Area from Under Review up to Active. */
        private val AREA_EDITABLE = setOf(PartnerStatus.UNDER_REVIEW, PartnerStatus.REVISION_REQUIRED, PartnerStatus.VERIFIED, PartnerStatus.WAITING_PKS, PartnerStatus.ACTIVE)

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
