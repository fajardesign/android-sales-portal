package com.example.and_sales_portal.data.partner

import kotlinx.serialization.Serializable

/** Tugaskan or Lepas (TL-03). */
@Serializable
enum class AssignmentAction { ASSIGN, RELEASE }

/**
 * One Tugaskan or Lepas with actor, time, toko, partner and SA/SR (PRD Scope 1 FR-MP-007, PRD alignment 2026-10-09).
 * [by] is the TL's user id, or [SYSTEM] when the partner became Inactive.
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
    /** Why the system released it: [PARTNER_INACTIVE] or [ACCOUNT_DISABLED]; `null` for a TL's Tugaskan / Lepas. */
    val reason: String? = null,
) {
    companion object {
        const val SYSTEM = "SYSTEM"
        const val PARTNER_INACTIVE = "PARTNER_INACTIVE"
        /** The SA/SR account was disabled (web ACC-05, DD review point 5). */
        const val ACCOUNT_DISABLED = "ACCOUNT_DISABLED"
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
        /** 2: revision notifications. */
        const val VERSION = 2
    }
}
