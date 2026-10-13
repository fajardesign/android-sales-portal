package com.example.and_sales_portal.data.demo

import android.content.Context
import com.example.and_sales_portal.data.activity.OpsData
import com.example.and_sales_portal.data.partner.AssignmentData
import com.example.and_sales_portal.data.partner.Partner
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Local sample data of the prototype (decision 2026-10-07: kept on the device until "Reset data demo"): partners and
 * operations shared by every account on the device, plus each user's registration draft. Partners, toko, assignments, visits, attendance, loans and incentives live here because the
 * Supabase backend only holds `app_user`.
 */
class DemoStore(context: Context, private val draftCipher: DraftCipher = KeystoreDraftCipher(context)) {
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

    /** Assignment history and notifications (TL-03, FR-MP-004 and FR-MP-007), or `null` when there is none. */
    fun loadAssignments(): AssignmentData? = runCatching {
        json.decodeFromString(AssignmentData.serializer(), File(dir, ASSIGNMENTS).readText()).takeIf { it.version == AssignmentData.VERSION }
    }.getOrNull()

    fun saveAssignments(data: AssignmentData) = write(ASSIGNMENTS, json.encodeToString(AssignmentData.serializer(), data))

    /** Accounts disabled and leaders replaced after the sample (web ACC-05). */
    fun loadDirectory(): Map<Long, DemoDirectory.Change> = runCatching {
        json.decodeFromString(DirectoryFile.serializer(), File(dir, DIRECTORY).readText()).changes
    }.getOrDefault(emptyMap())

    fun saveDirectory(changes: Map<Long, DemoDirectory.Change>) = write(DIRECTORY, json.encodeToString(DirectoryFile.serializer(), DirectoryFile(changes)))

    /**
     * Registration draft of [userId] (PRD C2; Scope 1 FR-PA-009: encrypted at rest, gone after [DRAFT_TTL_MILLIS]).
     * A draft older than 30 days at [now] is deleted and not returned.
     */
    fun loadDraft(userId: Long, now: Long = System.currentTimeMillis()): String? {
        File(dir, "draft_$userId.json").delete() // plain-text draft of earlier versions
        val file = draftFile(userId)
        val envelope = draftCipher.read(file)?.let { runCatching { json.decodeFromString(DraftEnvelope.serializer(), it) }.getOrNull() } ?: return null
        if (now - envelope.savedAt > DRAFT_TTL_MILLIS) {
            file.delete()
            return null
        }
        return envelope.text
    }

    fun saveDraft(userId: Long, text: String, now: Long = System.currentTimeMillis()) =
        draftCipher.write(draftFile(userId), json.encodeToString(DraftEnvelope.serializer(), DraftEnvelope(now, text)))

    fun deleteDraft(userId: Long) {
        draftFile(userId).delete()
        File(dir, "draft_$userId.json").delete()
    }

    /** Account switch (FR-PA-009): drafts of other accounts on the device are deleted. */
    fun deleteDraftsExcept(userId: Long) {
        dir.listFiles { f -> f.name.startsWith("draft_") && f.name != draftFile(userId).name }?.forEach { it.delete() }
    }

    private fun draftFile(userId: Long) = File(dir, "draft_$userId.enc")

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
        const val ASSIGNMENTS = "assignments.json"
        const val DIRECTORY = "directory.json"
    }

    /** A draft with the time it was saved. */
    @Serializable
    private data class DraftEnvelope(val savedAt: Long, val text: String)
}

/** FR-PA-009 (proposed in the PRD, OQ-PA-06): a draft expires 30 days after it was last saved. */
const val DRAFT_TTL_MILLIS = 30L * 24 * 3_600_000

@Serializable
private data class DirectoryFile(val changes: Map<Long, DemoDirectory.Change> = emptyMap())

@Serializable
private data class PartnersFile(val version: Int = VERSION, val partners: List<Partner>) {
    companion object {
        /** 3: owner saved per partner (DD review 2026-10-13). */
        const val VERSION = 3
    }
}
