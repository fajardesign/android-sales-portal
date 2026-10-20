package com.example.and_sales_portal.ui.notifications

import androidx.compose.ui.test.junit4.createComposeRule
import com.example.and_sales_portal.data.partner.AppNotification
import com.example.and_sales_portal.data.partner.AssignmentAction
import com.example.and_sales_portal.data.partner.AssignmentEvent
import com.example.and_sales_portal.data.partner.StoreCancelNotice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** In-app notification copy (PRD Scope 1 v1.1: 1.5 FR-MP-009 for SA/SR, 1.7 FR-006 for TL). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationTextTest {

    @get:Rule
    val compose = createComposeRule()

    private fun event(action: AssignmentAction, by: String, source: String? = null, seat: String = AssignmentEvent.SALES, withSales: List<String> = emptyList()) =
        AssignmentEvent(0, action, 11, "REG2026-0139-3", "Jaya Abadi Cellular Sudiang", "REG2026-0139", "Jaya Abadi Cellular", by, null, source, seat, withSales)

    private fun text(e: AssignmentEvent): String {
        var out = ""
        compose.setContent { out = notificationText(e) }
        compose.waitForIdle()
        return out
    }

    @Test
    fun salesMessages() {
        assertEquals("Anda ditugaskan ke Jaya Abadi Cellular Sudiang (Jaya Abadi Cellular).", text(event(AssignmentAction.ASSIGN, "6")))
    }

    @Test
    fun salesReleaseNamesTheReason() {
        assertEquals(
            "Penugasan Anda di Jaya Abadi Cellular Sudiang (Jaya Abadi Cellular) dicabut (TL dilepas).",
            text(event(AssignmentAction.RELEASE, "Hasan Basri (APL)", "TL dilepas")),
        )
    }

    @Test
    fun tlAssignedByTheApl() {
        assertEquals(
            "Anda ditugaskan ke Jaya Abadi Cellular Sudiang (Jaya Abadi Cellular) oleh Hasan Basri (APL).",
            text(event(AssignmentAction.ASSIGN, "Hasan Basri (APL)", "APL", AssignmentEvent.TL)),
        )
    }

    @Test
    fun tlReleasedWithTheSales() {
        assertEquals(
            "Anda dilepas dari Jaya Abadi Cellular Sudiang (Jaya Abadi Cellular). Penugasan Siti Rahmawati di toko ini juga dicabut.",
            text(event(AssignmentAction.RELEASE, "Hasan Basri (APL)", "dilepas APL", AssignmentEvent.TL, listOf("Siti Rahmawati"))),
        )
    }

    @Test
    fun tlReleasedWithoutSales() {
        assertEquals(
            "Anda dilepas dari Jaya Abadi Cellular Sudiang (Jaya Abadi Cellular).",
            text(event(AssignmentAction.RELEASE, AssignmentEvent.SYSTEM, "partner nonaktif", AssignmentEvent.TL)),
        )
    }

    @Test
    fun storeRequestCancelled() {
        // C7 FR-023 (decision 2026-10-20): the TL/SR who added the toko learns the Admin cancelled it, with the reason.
        val n = AppNotification("n", 11, storeCancelled = StoreCancelNotice(0, "REG2026-0139-5", "Jaya Abadi Cellular Daya", "REG2026-0139", "Jaya Abadi Cellular", "Lokasi toko tidak sesuai"))
        var out = ""
        compose.setContent { out = notificationText(n) }
        compose.waitForIdle()
        assertEquals("Permintaan tambah toko Jaya Abadi Cellular Daya (Jaya Abadi Cellular) dibatalkan: Lokasi toko tidak sesuai.", out)
    }
}
