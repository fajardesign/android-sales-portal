package com.example.and_sales_portal.data.demo

import android.content.Context
import com.example.and_sales_portal.data.activity.OpsData
import com.example.and_sales_portal.data.partner.Partner
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Local sample data of the prototype (decision 2026-10-07: kept on the device until "Reset data demo"): partners and
 * operations shared by every account on the device, plus each user's registration draft. Partners, toko, assignments, visits, attendance, loans and incentives live here because the
 * Supabase backend only holds `app_user`.
 */
class DemoStore(context: Context) {
    private val dir = File(context.filesDir, "demo").apply { mkdirs() }
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    /** Selfies taken in the prototype (evidence of clock in and visit check-in). */
    val selfieDir: File get() = File(dir, "selfies").apply { mkdirs() }

    /** Attendance, visits and days off of every SA/SR/TL, or `null` when there is none (or from an older version). */
    fun loadOps(): OpsData? = runCatching {
        json.decodeFromString(OpsData.serializer(), File(dir, OPS).readText()).takeIf { it.version == OpsData.VERSION }
    }.getOrNull()

    fun saveOps(data: OpsData) = write(OPS, json.encodeToString(OpsData.serializer(), data))

    /** Partners of the organisation, shared by every account on the device. */
    fun loadPartners(): List<Partner>? = runCatching {
        json.decodeFromString(PartnersFile.serializer(), File(dir, PARTNERS).readText()).takeIf { it.version == PartnersFile.VERSION }?.partners
    }.getOrNull()

    fun savePartners(partners: List<Partner>) = write(PARTNERS, json.encodeToString(PartnersFile.serializer(), PartnersFile(partners = partners)))

    /** Registration draft of [userId] (PRD C2: "draft is saved locally between steps"). */
    fun loadDraft(userId: Long): String? = File(dir, "draft_$userId.json").takeIf { it.exists() }?.readText()
    fun saveDraft(userId: Long, text: String) = write("draft_$userId.json", text)
    fun deleteDraft(userId: Long) {
        File(dir, "draft_$userId.json").delete()
    }

    /** Uploaded documents and photos (local copies). */
    val documentDir: File get() = File(dir, "documents").apply { mkdirs() }

    private fun write(name: String, text: String) {
        val tmp = File(dir, "$name.tmp")
        tmp.writeText(text)
        tmp.renameTo(File(dir, name))
    }

    /** Removes what only [userId] has on the device (registration draft). */
    fun reset(userId: Long) {
        deleteDraft(userId)
    }

    private companion object {
        const val PARTNERS = "partners.json"
        const val OPS = "operations.json"
    }
}

@Serializable
private data class PartnersFile(val version: Int = VERSION, val partners: List<Partner>) {
    companion object {
        const val VERSION = 1
    }
}
