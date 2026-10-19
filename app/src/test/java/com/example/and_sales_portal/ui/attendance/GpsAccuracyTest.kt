package com.example.and_sales_portal.ui.attendance

import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.device.LocationResult
import com.example.and_sales_portal.data.device.Place
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Decision 2026-10-19 (#13): a GPS fix less accurate than 100 m blocks check in (kerja and kunjungan); no override. */
class GpsAccuracyTest {

    private val toko = Place(-5.1478, 119.4326, null, "Toko")
    private fun state(accuracy: Float?, mode: CaptureMode = CaptureMode.CHECK_IN, mock: Boolean = false) = CaptureUiState(
        mode, listOf(toko), found = LocationResult.Found(toko.lat, toko.lng, null, mock, accuracy), nearest = toko, distance = 10,
    )

    @Test
    fun limitIsOneHundredMetres() {
        assertEquals(100, ActivityRules.MAX_ACCURACY_METERS)
        assertTrue(state(8f).locationOk)
        assertTrue(state(100f).locationOk)
        assertTrue(!state(100.5f).locationOk)
        assertTrue(!state(240f).accuracyOk)
        assertTrue(!state(240f, CaptureMode.CLOCK_IN).locationOk)
        // Unknown accuracy is not blocked; mock location and the 3 km radius still apply.
        assertTrue(state(null).locationOk)
        assertTrue(!state(8f, mock = true).locationOk)
        assertTrue(!state(8f).copy(distance = 3_500).locationOk)
    }
}
