package com.example.and_sales_portal.data.demo

import android.content.Context
import com.example.and_sales_portal.data.activity.DemoData
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Local sample data of the prototype, one file per signed-in user (decision 2026-10-07: kept on the device until
 * "Reset data demo"). Partners, toko, assignments, visits, attendance, loans and incentives live here because the
 * Supabase backend only holds `app_user`.
 */
class DemoStore(context: Context) {
    private val dir = File(context.filesDir, "demo").apply { mkdirs() }
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    /** Selfies taken in the prototype (evidence of clock in and visit check-in). */
    val selfieDir: File get() = File(dir, "selfies").apply { mkdirs() }

    internal fun fileOf(userId: Long) = File(dir, "user_$userId.json")

    /** Saved data of [userId], or `null` when there is none (or it is from an older prototype version). */
    fun load(userId: Long): DemoData? = runCatching {
        json.decodeFromString(DemoData.serializer(), fileOf(userId).readText()).takeIf { it.version == DemoData.VERSION }
    }.getOrNull()

    fun save(userId: Long, data: DemoData) {
        val tmp = File(dir, "user_$userId.tmp")
        tmp.writeText(json.encodeToString(DemoData.serializer(), data))
        tmp.renameTo(fileOf(userId))
    }

    /** Back to the initial sample data for [userId]. */
    fun reset(userId: Long) {
        fileOf(userId).delete()
    }
}
