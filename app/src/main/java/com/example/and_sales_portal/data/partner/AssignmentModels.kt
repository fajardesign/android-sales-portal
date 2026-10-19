package com.example.and_sales_portal.data.partner

import kotlinx.serialization.Serializable

/** Tugaskan or Lepas (TL-03). */
@Serializable
enum class AssignmentAction { ASSIGN, RELEASE }

/**
 * One Tugaskan or Lepas with actor, time, toko, partner and person (PRD Scope 1 FR-MP-007; v1.1 C6 kursi per toko).
 * [seat] [SALES]: the SA/SR kursi, [by] the TL's user id, [SYSTEM], or the APL by name. [seat] [TL]: the kursi TL of
 * [employeeId] (only APL actions and releases are logged here, 1.7 FR-006).
 */
@Serializable
data class AssignmentEvent(
    val at: Long,
    val action: AssignmentAction,
    val employeeId: Long,
    val storeId: String,
    val storeName: String,
    val partnerId: String,
    val partnerName: String,
    val by: String,
    /** Why the system released it: [PARTNER_INACTIVE], [ACCOUNT_DISABLED], [LEADER_CHANGED] or [TL_RELEASED]. */
    val reason: String? = null,
    /** Why it was released, shown as "dicabut (<source>)" ("TL dilepas", "Pindahkan Bawahan", "dilepas Andi Pratama", …). */
    val source: String? = null,
    val seat: String = SALES,
    /** Kursi TL released: the SA/SR released with it ("Penugasan Siti Rahmawati di toko ini juga dicabut."). */
    val withSales: List<String> = emptyList(),
) {
    companion object {
        const val SYSTEM = "SYSTEM"
        const val SALES = "SALES"
        const val TL = "TL"
        /** Released together with the kursi TL (APL Lepas TL, partner Inactive, Ubah Area). */
        const val TL_RELEASED = "TL_RELEASED"
        const val PARTNER_INACTIVE = "PARTNER_INACTIVE"
        /** The SA/SR account was disabled (web ACC-05, DD review point 5). */
        const val ACCOUNT_DISABLED = "ACCOUNT_DISABLED"
        /** The SA/SR got a new TL who does not hold the toko (FR-AM-013, decision 2026-10-16). */
        const val LEADER_CHANGED = "LEADER_CHANGED"
    }
}

/** Admin asked to fix a submission (DD review point 2): to the submitter and the owning TL. */
@Serializable
data class RevisionNotice(val at: Long, val partnerId: String, val partnerName: String)

/**
 * In-app notification (FR-MP-004, decision 2026-10-09: inbox in the app): an assignment change to the SA/SR, or a
 * revision request to the TL/SR who submitted the partner and its owning TL (DD review point 2).
 */
@Serializable
data class AppNotification(
    val id: String,
    val employeeId: Long,
    val event: AssignmentEvent? = null,
    val revision: RevisionNotice? = null,
    val read: Boolean = false,
) {
    val at: Long get() = event?.at ?: revision?.at ?: 0
}

/** Assignment history and notifications on the device (shared by every account, like partners). */
@Serializable
data class AssignmentData(
    val version: Int = VERSION,
    val events: List<AssignmentEvent> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
) {
    companion object {
        /** 4: sample notification older than 30 days (decision 2026-10-19). */
        const val VERSION = 4
    }
}
