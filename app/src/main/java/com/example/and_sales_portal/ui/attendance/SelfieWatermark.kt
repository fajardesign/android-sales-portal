package com.example.and_sales_portal.ui.attendance

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import androidx.exifinterface.media.ExifInterface
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.coordinates
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * PRD Scope 2 S14: the selfie comes from the camera only and carries a watermark burned into the photo: the time (server
 * time, prototype: the app clock, in the zone of the user's area, S8) and the GPS coordinates of the punch.
 */
object SelfieWatermark {
    private val ID = Locale("id", "ID")
    private val TIME = DateTimeFormatter.ofPattern("HH:mm", ID)

    /** "Rab, 7 Okt 2026, 10:12 WITA" and "-5.147800, 119.432600". */
    fun lines(at: Long, zone: ZoneId, lat: Double, lng: Double): List<String> {
        val t = Instant.ofEpochMilli(at).atZone(zone)
        return listOf("${Fmt.shortDate(t.toLocalDate())}, ${TIME.format(t)} ${ActivityRules.label(zone)}", coordinates(lat, lng))
    }

    /**
     * Rewrites the JPEG at [path] upright (EXIF rotation applied), at most [MAX_PX] on the long side, with [lines] on a
     * dark band at the bottom. Returns `false` when the file cannot be read (it is then left as it was).
     */
    fun burn(path: String, lines: List<String>): Boolean = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_PX) sample *= 2
        val decoded = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample; inMutable = true }) ?: return false
        val degrees = when (ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        val upright = if (degrees == 0f) decoded else Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, Matrix().apply { postRotate(degrees) }, true)
        val photo = if (upright.isMutable) upright else upright.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(photo)
        // Sizes follow the photo (pixels of the image, not screen units).
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = photo.width * TEXT_RATIO
            typeface = Typeface.DEFAULT_BOLD
        }
        val pad = text.textSize * PAD_RATIO
        val lineHeight = text.textSize * LINE_RATIO
        val bandTop = photo.height - (pad * 2 + lineHeight * lines.size)
        canvas.drawRect(0f, bandTop, photo.width.toFloat(), photo.height.toFloat(), Paint().apply { color = android.graphics.Color.argb(BAND_ALPHA, 0, 0, 0) })
        lines.forEachIndexed { i, line -> canvas.drawText(line, pad, bandTop + pad + lineHeight * (i + 1) - (lineHeight - text.textSize), text) }
        val tmp = File("$path.tmp")
        tmp.outputStream().use { photo.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
        tmp.renameTo(File(path))
    }.getOrDefault(false)

    private const val MAX_PX = 1280
    private const val TEXT_RATIO = 0.04f
    private const val PAD_RATIO = 0.6f
    private const val LINE_RATIO = 1.35f
    private const val BAND_ALPHA = 150
    private const val QUALITY = 88
}
