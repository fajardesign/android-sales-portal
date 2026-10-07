package com.example.and_sales_portal.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.Json

/** Cached profile of the signed-in user (kept between app starts). */
interface ProfileCache {
    var salesSession: SalesSession?
}

/**
 * Keystore-backed storage for the Supabase session and the signed-in profile (PRD: "tokens are stored in
 * Keystore-backed storage and never logged").
 */
class SecureStore(context: Context) : SessionManager, ProfileCache {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveSession(session: UserSession) =
        prefs.edit { putString(KEY_AUTH, json.encodeToString(UserSession.serializer(), session)) }

    override suspend fun loadSession(): UserSession? =
        prefs.getString(KEY_AUTH, null)?.let { runCatching { json.decodeFromString(UserSession.serializer(), it) }.getOrNull() }

    override suspend fun deleteSession() = prefs.edit { remove(KEY_AUTH) }

    override var salesSession: SalesSession?
        get() = prefs.getString(KEY_PROFILE, null)?.let { runCatching { json.decodeFromString(SalesSession.serializer(), it) }.getOrNull() }
        set(value) = prefs.edit {
            if (value == null) remove(KEY_PROFILE) else putString(KEY_PROFILE, json.encodeToString(SalesSession.serializer(), value))
        }

    private companion object {
        const val FILE = "sales_secure_store"
        const val KEY_AUTH = "supabase_session"
        const val KEY_PROFILE = "sales_session"
    }
}
