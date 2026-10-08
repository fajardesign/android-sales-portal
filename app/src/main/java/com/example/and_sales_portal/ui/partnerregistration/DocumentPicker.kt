package com.example.and_sales_portal.ui.partnerregistration

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.and_sales_portal.data.partner.DocType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Camera, gallery and file picking for documents (REG-06, REG-11). Every file is copied to app storage so it survives
 * with the draft. Camera photos are scaled down (longest side [MAX_PX], JPEG) as a production app would before upload,
 * so a store photo fits the 2 MB limit. Create it as a property of the [fragment] and forward its saved state.
 */
class DocumentPicker(
    private val fragment: Fragment,
    private val dir: () -> File,
    private val onPicked: (DocType, String, String, Long) -> Unit,
) {
    private var pending: DocType? = null
    private var pendingFile: File? = null

    private val camera = fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val doc = pending
        val file = pendingFile
        if (saved && doc != null && file != null && file.length() > 0) {
            fragment.lifecycleScope.launch {
                withContext(Dispatchers.IO) { shrink(file) }
                onPicked(doc, file.absolutePath, file.name, file.length())
            }
        } else {
            file?.delete()
        }
    }

    private val gallery = fragment.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let(::copy) }
    private val files = fragment.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(::copy) }

    fun camera(doc: DocType) {
        pending = doc
        val file = File(dir(), "${doc.name.lowercase()}_${System.currentTimeMillis()}.jpg")
        pendingFile = file
        val uri = FileProvider.getUriForFile(fragment.requireContext(), "${fragment.requireContext().packageName}.fileprovider", file)
        try {
            camera.launch(uri)
        } catch (e: ActivityNotFoundException) {
            pendingFile = null
        }
    }

    fun gallery(doc: DocType) {
        pending = doc
        gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun file(doc: DocType) {
        pending = doc
        files.launch(arrayOf("application/pdf", "image/jpeg", "image/png"))
    }

    fun save(out: Bundle) {
        pending?.let { out.putString(KEY_DOC, it.name) }
        pendingFile?.let { out.putString(KEY_FILE, it.absolutePath) }
    }

    fun restore(saved: Bundle?) {
        pending = saved?.getString(KEY_DOC)?.let(DocType::of)
        pendingFile = saved?.getString(KEY_FILE)?.let(::File)
    }

    private fun copy(uri: Uri) {
        val doc = pending ?: return
        val context = fragment.requireContext()
        fragment.lifecycleScope.launch {
            var name = uri.lastPathSegment ?: doc.name
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0)?.let { name = it }
            }
            val target = File(dir(), "${System.currentTimeMillis()}_${name.replace(Regex("[^A-Za-z0-9._-]"), "_")}")
            val ok = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use { input.copyTo(it) } } != null }.getOrDefault(false)
            }
            if (ok) onPicked(doc, target.absolutePath, name, target.length())
        }
    }

    private fun shrink(file: File) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_PX) sample *= 2
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample }) ?: return
            val degrees = when (ExifInterface(file.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            val upright = if (degrees == 0f) bitmap else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(degrees) }, true)
            file.outputStream().use { upright.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        }
    }

    private companion object {
        const val KEY_DOC = "picker_doc"
        const val KEY_FILE = "picker_file"
        const val MAX_PX = 1600
        const val JPEG_QUALITY = 85
    }
}
