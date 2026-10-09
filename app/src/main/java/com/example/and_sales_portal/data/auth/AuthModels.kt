package com.example.and_sales_portal.data.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Realm roles of the sales app (PRD v3 "Role mapping"). Only TL, SR and SA have `sales-app-access`. */
enum class SalesRole { TL, SR, SA }

/** Feature access roles used by the Android app (PRD v3 "Feature access roles"). */
object Feature {
    const val PARTNER_ACQUISITION = "PARTNER_ACQUISITION"
    const val SALES_ASSIGNMENT = "SALES_ASSIGNMENT"
    const val VISIT_PLAN_MANAGEMENT = "VISIT_PLAN_MANAGEMENT"
    const val ATTENDANCE = "ATTENDANCE"
    const val VISIT_EXECUTION = "VISIT_EXECUTION"
    const val LOAN_TRACKING = "LOAN_TRACKING"
    const val SALES_PERFORMANCE = "SALES_PERFORMANCE"
    const val PRODUCTIVITY_CHECK_IN = "PRODUCTIVITY_PERFORMANCE_CHECK_IN"
    /** Renamed from `PRODUCTIVITY_PERFORMANCE_VISIT_PLAN` (PRD alignment 2026-10-09; `sp-mobile-login` sends the new name). */
    const val PRODUCTIVITY_VISIT = "PRODUCTIVITY_PERFORMANCE_VISIT"
    const val INCENTIVE_ESTIMATION = "INCENTIVE_ESTIMATION"
    const val PARTNER_VIEW = "PARTNER_VIEW"
    const val TEAM_VIEW = "TEAM_VIEW"
}

/** Row of `public.app_user` (Supabase, synced from the web Account Management). */
@Serializable
data class AppUser(
    val id: Long,
    @SerialName("full_name") val fullName: String,
    val role: String,
    /** Internal (Keycloak; filled with the email since revision 2026-10-08), never shown in the app. */
    val username: String = "",
    val email: String,
    val phone: String? = null,
    @SerialName("tl_level") val tlLevel: String? = null,
    @SerialName("area_ids") val areaIds: List<Int> = emptyList(),
    @SerialName("supervisor_id") val supervisorId: Long? = null,
    @SerialName("partner_id") val partnerId: String? = null,
    val status: String,
) {
    val salesRole: SalesRole? get() = runCatching { SalesRole.valueOf(role) }.getOrNull()
    val firstName: String get() = fullName.substringBefore(' ')
}

/** `access` of the login response: realm role → platform access → feature access roles. */
@Serializable
data class AccessInfo(
    val realmRole: String,
    val platform: String,
    val features: List<String>,
) {
    fun has(feature: String) = feature in features
}

/** A signed-in user of the sales app. */
@Serializable
data class SalesSession(val user: AppUser, val access: AccessInfo) {
    val role: SalesRole get() = requireNotNull(user.salesRole)
}

/**
 * Login failures of `sp-mobile-login` (flows_android/mobile_login.md MLG-01) plus client-side ones.
 * Copy per case: see `strings.xml` (`auth_error_*`), from the PRD v3 login table and the Figma login sheets.
 */
enum class LoginError {
    /** 401 INVALID: wrong identifier or password (shown inline under the password). */
    Invalid,
    /** 423 LOCKED: 5 wrong attempts, locked 15 minutes. */
    Locked,
    /** 403 DISABLED. */
    Disabled,
    /** 403 NOT_ACTIVATED: Pending or Expired account. */
    NotActivated,
    /** 403 FORBIDDEN_PLATFORM: account without `sales-app-access` (e.g. Partner PIC). */
    ForbiddenPlatform,
    /** 400 BAD_REQUEST: prevented by the form (both fields required); shown as a server error if it still happens. */
    BadRequest,
    /** No network. */
    Offline,
    /** Request timed out. */
    Timeout,
    /** 5xx or an unexpected answer. */
    Server,
}

sealed interface LoginResult {
    data class Success(val session: SalesSession) : LoginResult
    data class Failure(val error: LoginError) : LoginResult
}

/** Outcome of the splash check (MLG-00). */
sealed interface RestoreResult {
    data class SignedIn(val session: SalesSession) : RestoreResult
    data object NoSession : RestoreResult
    /** A stored session could not be refreshed (Figma "Sesi tidak dapat diperbarui"). */
    data object Expired : RestoreResult
}
