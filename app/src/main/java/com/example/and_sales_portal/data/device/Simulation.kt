package com.example.and_sales_portal.data.device

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Where the location step puts the user (prototype; production always uses the device GPS). */
enum class LocationMode {
    /** At the toko (attendance: the area office). Default, so the prototype works away from the toko. */
    AT_STORE,
    /** Real device location (fused location provider). */
    DEVICE,
    /** About 9 km away: outside the 3 km radius of every office and toko, so check in is refused. */
    OUT_OF_RADIUS,
}

/**
 * "Simulasi (debug)" panel (decision 2026-10-07): device conditions for trying every branch of ATT flows on an
 * emulator. Saved in plain preferences, shared by all accounts on the device.
 */
data class Simulation(
    val location: LocationMode = LocationMode.AT_STORE,
    val mockLocation: Boolean = false,
    val offline: Boolean = false,
)

class SimulationSettings(context: Context) {
    private val prefs = context.getSharedPreferences("simulation", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(read())
    val state: StateFlow<Simulation> = _state.asStateFlow()

    val value: Simulation get() = _state.value

    fun update(f: (Simulation) -> Simulation) {
        val s = f(_state.value)
        prefs.edit()
            .putString(KEY_LOCATION, s.location.name)
            .putBoolean(KEY_MOCK, s.mockLocation)
            .putBoolean(KEY_OFFLINE, s.offline)
            .apply()
        _state.value = s
    }

    private fun read() = Simulation(
        location = prefs.getString(KEY_LOCATION, null)?.let { runCatching { LocationMode.valueOf(it) }.getOrNull() } ?: LocationMode.AT_STORE,
        mockLocation = prefs.getBoolean(KEY_MOCK, false),
        offline = prefs.getBoolean(KEY_OFFLINE, false),
    )

    private companion object {
        const val KEY_LOCATION = "location"
        const val KEY_MOCK = "mock_location"
        const val KEY_OFFLINE = "offline"
    }
}
