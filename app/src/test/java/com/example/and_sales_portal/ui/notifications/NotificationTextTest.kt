package com.example.and_sales_portal.ui.notifications

import androidx.compose.ui.test.junit4.createComposeRule
import com.example.and_sales_portal.data.partner.AssignmentAction
import com.example.and_sales_portal.data.partner.AssignmentEvent
import com.example.and_sales_portal.ui.partner.ReferralQr
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** In-app notification copy (PRD Scope 1 v1.1: 1.5 FR-MP-009 for SA/SR, 1.7 FR-006 for TL) and the sample QR. */
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
    fun sampleQrHasFinderSquaresAndDependsOnTheCode() {
        val a = ReferralQr.cells("OFMKS1391")
        assertEquals(ReferralQr.N, a.size)
        // Finder squares: dark ring, light ring, dark centre (top left, top right, bottom left).
        listOf(0 to 0, ReferralQr.N - 7 to 0, 0 to ReferralQr.N - 7).forEach { (x, y) ->
            assertTrue(a[y][x] && !a[y + 1][x + 1] && a[y + 3][x + 3])
        }
        assertTrue(a.zip(ReferralQr.cells("OFMKS1392")).any { (r1, r2) -> !r1.contentEquals(r2) })
        assertTrue(a.zip(ReferralQr.cells("OFMKS1391")).all { (r1, r2) -> r1.contentEquals(r2) })
    }
}
