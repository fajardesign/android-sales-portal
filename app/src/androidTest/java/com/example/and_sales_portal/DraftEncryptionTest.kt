package com.example.and_sales_portal

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.and_sales_portal.data.demo.DemoStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** FR-PA-009 on a device: the registration draft is encrypted with the Android Keystore and reads back. */
@RunWith(AndroidJUnit4::class)
class DraftEncryptionTest {

    @Test
    fun draftIsEncryptedAtRestAndReadsBack() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = DemoStore(context)
        val id = 999_001L
        store.deleteDraft(id)
        val text = "{\"partner\":{\"name\":\"Sinar Jaya Ponsel\",\"referralCode\":\"AMR6148\"}}"
        store.saveDraft(id, text, now = 1_000L)
        val raw = File(File(context.filesDir, "demo"), "draft_$id.enc").readBytes().toString(Charsets.ISO_8859_1)
        assertFalse(raw.contains("Sinar Jaya Ponsel"))
        assertEquals(text, store.loadDraft(id, now = 2_000L))
        // Saving again replaces the encrypted file.
        store.saveDraft(id, "{}", now = 3_000L)
        assertEquals("{}", store.loadDraft(id, now = 3_000L))
        store.deleteDraft(id)
        assertNull(store.loadDraft(id, now = 3_000L))
    }
}
