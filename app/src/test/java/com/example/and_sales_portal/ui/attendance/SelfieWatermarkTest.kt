package com.example.and_sales_portal.ui.attendance

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId

/** PRD Scope 2 S14: the selfie watermark carries the time in the area zone (S8) and the GPS coordinates. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SelfieWatermarkTest {

    @Test
    fun linesUseTheAreaZone() {
        val makassar = ZoneId.of("Asia/Makassar")
        val at = LocalDateTime.of(2026, 10, 7, 10, 12).atZone(makassar).toInstant().toEpochMilli()
        assertEquals(listOf("Rab, 7 Okt 2026, 10:12 WITA", "-5.147800, 119.432600"), SelfieWatermark.lines(at, makassar, -5.1478, 119.4326))
        // The same instant in Jakarta reads one hour earlier, labelled WIB.
        assertEquals("Rab, 7 Okt 2026, 09:12 WIB", SelfieWatermark.lines(at, ZoneId.of("Asia/Jakarta"), 0.0, 0.0).first())
    }

    @Test
    fun burnRewritesThePhoto() {
        val file = File(ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir, "selfie_test.jpg")
        file.outputStream().use { Bitmap.createBitmap(400, 600, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.JPEG, 90, it) }
        assertTrue(SelfieWatermark.burn(file.absolutePath, listOf("Rab, 7 Okt 2026, 10:12 WITA", "-5.147800, 119.432600")))
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        assertEquals(400, bounds.outWidth)
        assertTrue(!SelfieWatermark.burn(File(file.parentFile, "missing.jpg").absolutePath, emptyList()))
    }
}
