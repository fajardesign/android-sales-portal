package com.example.and_sales_portal.data.partner

import com.example.and_sales_portal.data.demo.DemoDirectory
import kotlinx.serialization.Serializable

/**
 * Partner pipeline data (PRD v3 data model `partner`, `store`, `partner_document`, `partner_status_history`,
 * `partner_revision_item`). Keys and labels follow the web portal (`admin-sales-portal/src/lib/constants.js`) so a
 * submission from the app reads the same in the Admin's Partner Pipeline.
 */
@Serializable
enum class PartnerStatus {
    UNDER_REVIEW, REVISION_REQUIRED, VERIFIED, WAITING_PKS, ACTIVE, REJECTED, CANCELLED, INACTIVE;

    val isFinal: Boolean get() = this == REJECTED || this == CANCELLED || this == INACTIVE

    companion object {
        /** Order of the status filter (review flow, then final statuses). */
        val ordered = listOf(UNDER_REVIEW, REVISION_REQUIRED, VERIFIED, WAITING_PKS, ACTIVE, REJECTED, CANCELLED, INACTIVE)
    }
}

/** Option keys (portal constants). */
object Keys {
    const val ENTITY_INDIVIDU = "INDIVIDU"
    const val ENTITY_PT = "PT"
    const val ENTITY_CV = "CV"
    const val PIC_OWNER = "OWNER"
    const val PIC_KARYAWAN = "KARYAWAN"
    const val STORE_OFFLINE = "OFFLINE"
    const val STORE_ONLINE = "ONLINE"
}

/** Document verification by the Admin. */
@Serializable
enum class Verification { UNVERIFIED, VALID, NEEDS_REVISION }

@Serializable
data class PartnerDoc(
    /** `DOC_TYPES` key, e.g. `KTP_PIC`, `FOTO_DEPAN`. */
    val key: String,
    val fileName: String,
    /** Local copy of the file (prototype); `null` for sample data. */
    val path: String? = null,
    val sizeBytes: Long = 0,
    val version: Int = 1,
    val verification: Verification = Verification.UNVERIFIED,
    val note: String? = null,
    val uploadedAt: Long = 0,
)

@Serializable
data class StatusEvent(
    val at: Long,
    val from: PartnerStatus? = null,
    val to: PartnerStatus,
    /** Who moved it: a user id from the directory, or the Admin's name. */
    val by: String,
    val reason: String? = null,
)

/** One flagged item of a revision request: a document (`DOC`, ref = document key) or a section (`SEC`). */
@Serializable
data class RevisionItem(val kind: String, val ref: String, val label: String, val note: String) {
    companion object {
        const val DOC = "DOC"
        const val SEC = "SEC"
    }
}

@Serializable
data class RevisionRequest(val at: Long, val by: String, val general: String? = null, val items: List<RevisionItem>)

/** Section keys of a revision request (portal `REVISION_SECTIONS`). */
object Sections {
    const val PARTNER = "partner"
    const val BUSINESS = "business"
    const val PIC = "pic"
    const val BANK = "bank"
    const val STORE = "store"
}

@Serializable
data class Store(
    val id: String,
    /** Toko code, given when the partner becomes Active (`TK0139-01`). */
    val code: String? = null,
    val name: String,
    val address: String,
    val province: String = "",
    val regency: String = "",
    val district: String = "",
    val village: String = "",
    val rt: String = "",
    val rw: String = "",
    val lat: Double,
    val lng: Double,
    val omzet: Long = 0,
    val storeType: String = Keys.STORE_OFFLINE,
    val channelOffline: String? = "NON_AGENCY",
    val scale: String = "MODERN",
    val productSold: String = "BRAND_NEW",
    val location: String = "PINGGIR_JALAN",
    val productType: String = "GADGET",
    val primary: Boolean = false,
    /**
     * `PENDING` until the partner is Active, then `ACTIVE`; [WAITING_REFERRAL] for a toko added after the partner is
     * Active until the Admin fills its Kode Referral (PRD Scope 1 v2 C2, OQ-25); `INACTIVE` with the partner.
     */
    val status: String = "PENDING",
    /** Kursi SA/SR (`merchant_assignment`, PRD Scope 1 v1.1 C6): one SA/SR, a direct subordinate of [tlId]; empty when free. */
    val assigned: List<Long> = emptyList(),
    val addedBy: Long = 0,
    val addedAt: Long = 0,
    /**
     * Kursi TL of the toko (PRD Scope 1 v1.1 C6): the partner is only an umbrella for its toko, so one partner can have
     * several TL. `null` = "Belum ada TL". Filled at activation (the TL who submitted, or the TL of the SR who did, when
     * active), by the APL (Mapping TL), or moved to the replacement when the TL is disabled; a toko from Tambah Toko
     * starts without one.
     */
    val tlId: Long? = null,
    /** Every change of [tlId], oldest first (activation, APL, TL disabled, partner Inactive, Ubah Area). */
    val tlLog: List<TlChange> = emptyList(),
    /** Kode Referral, filled by the Admin per toko (PRD Scope 1 v1.1 C2); read-only here. */
    val referralCode: String? = null,
    /** File name of the toko's QR, uploaded by the Admin with the code (FR-019); the prototype draws a sample QR from the code. */
    val referralQr: String? = null,
) {
    val active: Boolean get() = status == ACTIVE

    /** TL of the toko at [at] (millis): loans keep the TL of that date, so past sales stay with the old TL. */
    fun tlAt(at: Long): Long? {
        val next = tlLog.firstOrNull { it.at > at } ?: return tlId
        return next.fromTlId
    }

    companion object {
        const val ACTIVE = "ACTIVE"
        const val WAITING_REFERRAL = "WAITING_REFERRAL"
    }
}

/** The TL of a toko changed: the TL was disabled (web ACC-05) or the APL remapped it (decision 2026-10-16). */
@Serializable
data class TlChange(
    val at: Long,
    val fromTlId: Long?,
    /** `null` when the kursi TL was released. */
    val toTlId: Long?,
    /** Who changed it: "Sistem", the APL or the Admin by name. */
    val by: String,
    /** Why: "aktivasi", "APL", "dilepas APL", "TL dinonaktifkan", "partner nonaktif", "ubah area". */
    val source: String,
)

/**
 * "Konfirmasi dan Persetujuan Pendaftaran Toko" accepted before the first submission (PRD Scope 1 v2 C5): when, the
 * version of the page and who submitted.
 */
@Serializable
/** Proof of consent (PRD Scope 1 v2 C5; decision 2026-10-19 Q2: also the URL of the page shown). */
data class Consent(val at: Long, val version: String, val by: Long, val url: String = URL) {
    companion object {
        const val VERSION = "1.0"

        // TODO(OQ-PA-13): placeholder until the consent page URL is decided; the only place it is set.
        const val URL = "https://placeholder.amarbank.co.id/persetujuan-pendaftaran-toko/v1.0"
    }
}

@Serializable
data class Partner(
    val id: String,
    val name: String,
    val entity: String,
    val address: String,
    val province: String,
    val regency: String,
    val district: String,
    val village: String,
    val rt: String,
    val rw: String,
    val email: String,
    val channel: String,
    val outletCount: Int,
    val picName: String,
    val picEmail: String,
    /** Digits after +62. */
    val picPhone: String,
    val picStatus: String,
    val privyId: String? = null,
    val bankCode: String,
    val bankName: String,
    val bankBranch: String,
    val accountNumber: String,
    val accountName: String,
    val areaId: Int,
    val submittedBy: Long,
    val submittedAt: Long,
    val status: PartnerStatus,
    val statusUpdatedAt: Long,
    /** Location captured at submission (PRD C2 step 6). */
    val lat: Double,
    val lng: Double,
    val stores: List<Store>,
    val documents: List<PartnerDoc>,
    val history: List<StatusEvent>,
    val revision: RevisionRequest? = null,
    /** Reason of a Rejected, Cancelled or Inactive status. */
    val statusReason: String? = null,
    /** Consent of the first submission (C5); resubmitting a revision does not ask again. */
    val consent: Consent? = null,
) {
    val registrationNumber: String get() = id
    val primaryStore: Store? get() = stores.firstOrNull { it.primary } ?: stores.firstOrNull()

    /** TL of the partner's toko (several when toko have different TL), in toko order. */
    val tlIds: List<Long> get() = stores.mapNotNull { it.tlId }.distinct()

    /** Whether [tlId] holds at least one toko of the partner. */
    fun hasTl(tlId: Long): Boolean = stores.any { it.tlId == tlId }

    companion object {
        /** TL submitter / adder, or the current leader of the SR who submitted or added the toko. */
        fun defaultTl(by: Long, person: (Long) -> DemoDirectory.Person? = { DemoDirectory.person(it) }): Long? =
            person(by)?.let { if (it.role == "TL") it.id else it.supervisorId }
    }
}

/** Banks of the prototype (portal `BANKS`). */
object Banks {
    val all = listOf(
        "BCA" to "BCA", "BRI" to "BRI", "MANDIRI" to "Mandiri", "BNI" to "BNI",
        "BTN" to "BTN", "BSI" to "BSI", "CIMB" to "CIMB Niaga", "PERMATA" to "Permata",
    )
    fun name(code: String) = all.firstOrNull { it.first == code }?.second ?: code
}
