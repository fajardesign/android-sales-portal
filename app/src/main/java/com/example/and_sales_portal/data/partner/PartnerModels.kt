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
    /** `PENDING` until the partner is Active, then `ACTIVE`; `INACTIVE` with the partner. */
    val status: String = "PENDING",
    /** `merchant_assignment`: SA/SR assigned to this toko. */
    val assigned: List<Long> = emptyList(),
    val addedBy: Long = 0,
    val addedAt: Long = 0,
)

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
    /** Kode Referral (stakeholder revision 2026-10-08): letters and digits, max. 20; `null` in data saved before it. */
    val referralCode: String? = null,
    val email: String,
    val channel: String,
    val outletCount: Int,
    val picName: String,
    val picEmail: String,
    /** Digits after +62. */
    val picPhone: String,
    val picStatus: String,
    val privyId: String? = null,
    val pksInviteEmail: String? = null,
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
) {
    val registrationNumber: String get() = id
    val primaryStore: Store? get() = stores.firstOrNull { it.primary } ?: stores.firstOrNull()

    /** TL that owns the partner: the TL who submitted it, or the TL of the SR who did (PRD v3 §C). */
    val ownerTlId: Long?
        get() = DemoDirectory.person(submittedBy)?.let { if (it.role == "TL") it.id else it.supervisorId }
}

/** Banks of the prototype (portal `BANKS`). */
object Banks {
    val all = listOf(
        "BCA" to "BCA", "BRI" to "BRI", "MANDIRI" to "Mandiri", "BNI" to "BNI",
        "BTN" to "BTN", "BSI" to "BSI", "CIMB" to "CIMB Niaga", "PERMATA" to "Permata",
    )
    fun name(code: String) = all.firstOrNull { it.first == code }?.second ?: code
}
