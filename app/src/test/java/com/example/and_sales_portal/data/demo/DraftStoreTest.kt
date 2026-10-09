package com.example.and_sales_portal.data.demo

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** Registration draft (PRD Scope 1 FR-PA-009): expiry after 30 days, deleted on logout and account switch. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DraftStoreTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val store = DemoStore(context, PlainDraftCipher()) // the Android Keystore is not available in unit tests
    private val day = 24L * 3_600_000

    @Before
    fun clean() {
        listOf(11L, 15L).forEach(store::deleteDraft)
    }

    @Test
    fun draftExpiresAfter30Days() {
        store.saveDraft(11, "{\"draft\":1}", now = 1_000L)
        assertEquals("{\"draft\":1}", store.loadDraft(11, now = 1_000L + 30 * day))
        assertNull(store.loadDraft(11, now = 1_000L + 30 * day + 1))
        // Expired = deleted, not only hidden.
        assertNull(store.loadDraft(11, now = 1_000L))
    }

    @Test
    fun logoutAndAccountSwitchDeleteDrafts() {
        store.saveDraft(11, "siti", now = 1_000L)
        store.saveDraft(15, "eko", now = 1_000L)
        store.deleteDraftsExcept(15)
        assertNull(store.loadDraft(11, now = 1_000L))
        assertEquals("eko", store.loadDraft(15, now = 1_000L))
        store.deleteDraft(15)
        assertNull(store.loadDraft(15, now = 1_000L))
    }

    @Test
    fun plainTextDraftsOfEarlierVersionsAreRemoved() {
        val old = File(File(context.filesDir, "demo"), "draft_11.json").apply { writeText("{}") }
        assertNull(store.loadDraft(11, now = 1_000L))
        assertEquals(false, old.exists())
    }
}
