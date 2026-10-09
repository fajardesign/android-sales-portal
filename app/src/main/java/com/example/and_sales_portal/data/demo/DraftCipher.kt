package com.example.and_sales_portal.data.demo

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File

/** How a registration draft is written at rest (PRD Scope 1 FR-PA-009: "encrypted at rest"). */
interface DraftCipher {
    fun write(file: File, text: String)
    fun read(file: File): String?
}

/** Keystore-backed encryption (AES-256 GCM, `androidx.security.crypto`). The key is made on first use. */
class KeystoreDraftCipher(private val context: Context) : DraftCipher {
    private val key by lazy { MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build() }

    private fun encrypted(file: File) =
        EncryptedFile.Builder(context, file, key, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB).build()

    override fun write(file: File, text: String) {
        // EncryptedFile does not overwrite: the old draft is removed first.
        file.delete()
        encrypted(file).openFileOutput().use { it.write(text.toByteArray()) }
    }

    override fun read(file: File): String? =
        if (!file.exists()) null else runCatching { encrypted(file).openFileInput().use { String(it.readBytes()) } }.getOrNull()
}

/** Plain text, for unit tests without the Android Keystore. */
class PlainDraftCipher : DraftCipher {
    override fun write(file: File, text: String) = file.writeText(text)
    override fun read(file: File): String? = file.takeIf { it.exists() }?.readText()
}
