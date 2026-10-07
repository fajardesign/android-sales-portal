package com.example.and_sales_portal.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Login of the sales app against the prototype backend (flows_android/mobile_login.md):
 * MLG-01 `POST /functions/v1/sp-mobile-login` → `auth.importSession`, MLG-02 profile from `app_user` (RLS: own row).
 * Production uses Keycloak (PRD v3); this is the Supabase prototype setup.
 */
class AuthRepository(
    private val supabase: SupabaseClient,
    private val store: ProfileCache,
    private val baseUrl: String,
    private val publishableKey: String,
    private val http: HttpClient = HttpClient(Android) {
        install(HttpTimeout) { requestTimeoutMillis = TIMEOUT_MS; connectTimeoutMillis = TIMEOUT_MS }
        expectSuccess = false
    },
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val _session = MutableStateFlow(store.salesSession)
    /** The signed-in user, or `null`. */
    val session: StateFlow<SalesSession?> = _session.asStateFlow()

    suspend fun login(identifier: String, password: String): LoginResult {
        val response = try {
            http.post("$baseUrl/functions/v1/sp-mobile-login") {
                header("apikey", publishableKey)
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("identifier", identifier.trim())
                        put("password", password)
                        put("platform", PLATFORM)
                    }.toString(),
                )
            }
        } catch (e: HttpRequestTimeoutException) {
            return LoginResult.Failure(LoginError.Timeout)
        } catch (e: SocketTimeoutException) {
            return LoginResult.Failure(LoginError.Timeout)
        } catch (e: IOException) {
            return LoginResult.Failure(LoginError.Offline)
        }
        val body = runCatching { json.parseToJsonElement(response.bodyAsText()).jsonObject }.getOrNull()
        val status = response.status.value
        if (status >= 500 || body == null) return LoginResult.Failure(LoginError.Server)
        if (status != 200) return LoginResult.Failure(errorOf(body))

        return try {
            val user = json.decodeFromJsonElement(AppUser.serializer(), body.getValue("user"))
            val access = json.decodeFromJsonElement(AccessInfo.serializer(), body.getValue("access"))
            if (user.salesRole == null || access.platform != PLATFORM) return LoginResult.Failure(LoginError.ForbiddenPlatform)
            supabase.auth.importSession(sessionOf(body.getValue("session").jsonObject))
            val session = SalesSession(user, access)
            store.salesSession = session
            _session.value = session
            LoginResult.Success(session)
        } catch (e: Exception) {
            LoginResult.Failure(LoginError.Server)
        }
    }

    /**
     * Splash (MLG-00): a stored session is refreshed silently and the profile reloaded. Offline, the cached profile is
     * used (the prototype's operational data is local). A rejected refresh or a non-Active account ends the session.
     */
    suspend fun restore(): RestoreResult {
        val cached = store.salesSession ?: return RestoreResult.NoSession
        supabase.auth.awaitInitialization()
        if (supabase.auth.currentSessionOrNull() == null) {
            clear()
            return RestoreResult.Expired
        }
        try {
            supabase.auth.refreshCurrentSession()
            val user = supabase.from(APP_USER).select().decodeSingle<AppUser>()
            if (user.status != "ACTIVE" || user.salesRole == null) {
                logout()
                return RestoreResult.Expired
            }
            val session = cached.copy(user = user)
            store.salesSession = session
            _session.value = session
            return RestoreResult.SignedIn(session)
        } catch (e: RestException) {
            clear()
            return RestoreResult.Expired
        } catch (e: Exception) {
            // Network problem: keep the cached profile.
            _session.value = cached
            return RestoreResult.SignedIn(cached)
        }
    }

    /** "Keluar" (MLG-02). */
    suspend fun logout() {
        runCatching { supabase.auth.signOut() }
        clear()
    }

    private suspend fun clear() {
        runCatching { supabase.auth.clearSession() }
        store.salesSession = null
        _session.value = null
    }

    private fun errorOf(body: JsonObject): LoginError = when (body["error"]?.jsonPrimitive?.content) {
        "INVALID" -> LoginError.Invalid
        "LOCKED" -> LoginError.Locked
        "DISABLED" -> LoginError.Disabled
        "NOT_ACTIVATED" -> LoginError.NotActivated
        "FORBIDDEN_PLATFORM" -> LoginError.ForbiddenPlatform
        "BAD_REQUEST" -> LoginError.BadRequest
        else -> LoginError.Server
    }

    private fun sessionOf(s: JsonObject) = UserSession(
        accessToken = s.getValue("access_token").jsonPrimitive.content,
        refreshToken = s.getValue("refresh_token").jsonPrimitive.content,
        expiresIn = s.getValue("expires_in").jsonPrimitive.long,
        tokenType = s.getValue("token_type").jsonPrimitive.content,
        user = null,
    )

    companion object {
        const val PLATFORM = "sales-app-access"
        private const val APP_USER = "app_user"
        private const val TIMEOUT_MS = 15_000L
    }
}
