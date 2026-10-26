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
    /** At the toko but with a GPS accuracy of 240 m: worse than the 100 m limit, so check in is refused (2026-10-19). */
    LOW_ACCURACY,
}

/**
 * Demo date for the month status of PRD Scope 3 ("Tanggal Scope 3", same as the web DevToolbar): the device date, or a
 * fixed 3 Oct 2026 (September Menunggu rekonsiliasi), 7 Oct 2026 (September Final, cut-off 5 Oct) or 12 Oct 2026 (after the
 * payout day 10 Oct: the logging step).
 */
enum class S3Date(val date: java.time.LocalDate?) {
    DEVICE(null),
    OCT_7(java.time.LocalDate.of(2026, 10, 7)),
    OCT_3(java.time.LocalDate.of(2026, 10, 3)),
    OCT_12(java.time.LocalDate.of(2026, 10, 12)),
}

/**
 * Loading and failure of the Performa data (PRF-01 … PRF-05 states "memuat" and "galat"); [LMT_ERROR] = the loan system call
 * failed, the last stored data is shown with a banner (FR-LP-001, PRD §11).
 */
enum class PerfData { NORMAL, LOADING, ERROR, LMT_ERROR }

/**
 * "Simulasi (debug)" panel (decision 2026-10-07): device conditions for trying every branch of ATT flows on an
 * emulator. Saved in plain preferences, shared by all accounts on the device.
 */
data class Simulation(
    val location: LocationMode = LocationMode.AT_STORE,
    val mockLocation: Boolean = false,
    val offline: Boolean = false,
    val s3Date: S3Date = S3Date.DEVICE,
    /** Admin marked the latest Final month paid for every SA/SR/TL on the demo date (FR-PY-003). */
    val s3PaidLatest: Boolean = false,
    /** Admin marked the latest Final month Tidak dibayar ("Rekening tidak valid") for every SA/SR/TL (FR-PY-004). */
    val s3UnpaidLatest: Boolean = false,
    val perfData: PerfData = PerfData.NORMAL,
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
            .putString(KEY_S3_DATE, s.s3Date.name)
            .putBoolean(KEY_S3_PAID, s.s3PaidLatest)
            .putBoolean(KEY_S3_UNPAID, s.s3UnpaidLatest)
            .putString(KEY_PERF, s.perfData.name)
            .apply()
        _state.value = s
    }

    private fun read() = Simulation(
        location = prefs.getString(KEY_LOCATION, null)?.let { runCatching { LocationMode.valueOf(it) }.getOrNull() } ?: LocationMode.AT_STORE,
        mockLocation = prefs.getBoolean(KEY_MOCK, false),
        offline = prefs.getBoolean(KEY_OFFLINE, false),
        s3Date = prefs.getString(KEY_S3_DATE, null)?.let { runCatching { S3Date.valueOf(it) }.getOrNull() } ?: S3Date.DEVICE,
        s3PaidLatest = prefs.getBoolean(KEY_S3_PAID, false),
        s3UnpaidLatest = prefs.getBoolean(KEY_S3_UNPAID, false),
        perfData = prefs.getString(KEY_PERF, null)?.let { runCatching { PerfData.valueOf(it) }.getOrNull() } ?: PerfData.NORMAL,
    )

    private companion object {
        const val KEY_LOCATION = "location"
        const val KEY_MOCK = "mock_location"
        const val KEY_OFFLINE = "offline"
        const val KEY_S3_DATE = "s3_date"
        const val KEY_S3_PAID = "s3_paid_latest"
        const val KEY_S3_UNPAID = "s3_unpaid_latest"
        const val KEY_PERF = "perf_data"
    }
}
