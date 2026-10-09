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
) {
    companion object {
        const val SYSTEM = "SYSTEM"
    }
}

/** In-app notification to an SA/SR (FR-MP-004, decision 2026-10-09: inbox in the app). */
@Serializable
data class AppNotification(val id: String, val employeeId: Long, val event: AssignmentEvent, val read: Boolean = false)

/** Assignment history and notifications on the device (shared by every account, like partners). */
@Serializable
data class AssignmentData(
    val version: Int = VERSION,
    val events: List<AssignmentEvent> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
) {
    companion object {
        const val VERSION = 1
    }
}
