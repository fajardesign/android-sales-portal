package com.example.and_sales_portal.data.demo

import android.content.Context
import java.io.File

/**
 * Local sample data of the prototype, one file per signed-in user (decision 2026-10-07: kept on the device until
 * "Reset data demo"). Partners, toko, assignments, visits, attendance, loans and incentives live here because the
 * Supabase backend only holds `app_user`.
 */
class DemoStore(context: Context) {
    private val dir = File(context.filesDir, "demo").apply { mkdirs() }

    internal fun fileOf(userId: Long) = File(dir, "user_$userId.json")

    /** Back to the initial sample data for [userId]. */
    fun reset(userId: Long) {
        fileOf(userId).delete()
    }
}
