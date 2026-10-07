package com.example.and_sales_portal.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.and_sales_portal.data.activity.ActivityRepository
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.Punch
import com.example.and_sales_portal.data.device.LocationResult
import com.example.and_sales_portal.data.device.LocationService
import com.example.and_sales_portal.data.device.Place
import com.example.and_sales_portal.data.device.SimulationSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

/** What the capture flow records. */
enum class CaptureMode { CLOCK_IN, CLOCK_OUT, CHECK_IN }

enum class CaptureStep { Location, Selfie }

/** Bottom sheets of the flow (the mock-location dialog is separate: it cannot be dismissed). */
enum class CaptureSheet { Permission, GpsOff, Unavailable, Radius, Liveness }

enum class Liveness { None, Checking, Passed }

data class CaptureUiState(
    val mode: CaptureMode,
    val reference: Place,
    /** Toko name for a visit check-in. */
    val storeName: String? = null,
    val step: CaptureStep = CaptureStep.Location,
    val locating: Boolean = false,
    val found: LocationResult.Found? = null,
    /** Metres to the toko (visit check-in only). */
    val distance: Int? = null,
    val sheet: CaptureSheet? = null,
    val mockDetected: Boolean = false,
    val selfie: String? = null,
    val liveness: Liveness = Liveness.None,
    val online: Boolean = true,
    /** Biometric/PIN passed (clock in/out only, PRD D2). */
    val verified: Boolean = false,
) {
    val inRadius: Boolean get() = distance == null || distance <= ActivityRules.VISIT_RADIUS_METERS
    val locationOk: Boolean get() = found != null && !found.mock && inRadius
}

/**
 * Spec: ATT-02 Clock In / Clock Out and ATT-04 Check-In Kunjungan (PRD v3 D2, D3). Steps: [biometric/PIN for
 * attendance] → location (map, coordinates, address; visits: within 50 m of the toko) → selfie with passive liveness
 * (simulated) → submit (online only).
 */
class CaptureViewModel(
    private val repo: ActivityRepository,
    private val location: LocationService,
    private val simulation: SimulationSettings,
    mode: CaptureMode,
    private val visitId: String?,
    reference: Place,
    storeName: String?,
) : ViewModel() {

    private val _state = MutableStateFlow(CaptureUiState(mode, reference, storeName, verified = mode == CaptureMode.CHECK_IN))
    val state: StateFlow<CaptureUiState> = _state.asStateFlow()

    /** Time of the saved punch; the screen closes and shows a toast. */
    private val _finished = MutableStateFlow<Long?>(null)
    val finished: StateFlow<Long?> = _finished.asStateFlow()

    fun setOnline(online: Boolean) = _state.update { it.copy(online = online) }

    fun verified() = _state.update { it.copy(verified = true) }

    fun locate() {
        if (_state.value.locating) return
        _state.update { it.copy(locating = true, sheet = null, found = null, distance = null) }
        viewModelScope.launch {
            val s = _state.value
            when (val r = location.current(s.reference)) {
                is LocationResult.Found -> {
                    val distance = if (s.mode == CaptureMode.CHECK_IN) {
                        LocationService.distance(r.lat, r.lng, s.reference.lat, s.reference.lng).roundToInt()
                    } else null
                    val outside = distance != null && distance > ActivityRules.VISIT_RADIUS_METERS
                    _state.update {
                        it.copy(
                            locating = false, found = r, distance = distance, mockDetected = r.mock,
                            sheet = if (!r.mock && outside) CaptureSheet.Radius else null,
                        )
                    }
                }
                LocationResult.PermissionDenied -> fail(CaptureSheet.Permission)
                LocationResult.GpsOff -> fail(CaptureSheet.GpsOff)
                LocationResult.Unavailable -> fail(CaptureSheet.Unavailable)
            }
        }
    }

    fun permissionDenied() = fail(CaptureSheet.Permission)

    private fun fail(sheet: CaptureSheet) = _state.update { it.copy(locating = false, sheet = sheet) }

    fun dismissSheet() = _state.update { it.copy(sheet = null) }

    fun toSelfie() {
        if (_state.value.locationOk) _state.update { it.copy(step = CaptureStep.Selfie) }
    }

    /** Back inside the flow: from the selfie to the location; `false` when the flow should close. */
    fun back(): Boolean {
        if (_state.value.step == CaptureStep.Selfie) {
            _state.update { it.copy(step = CaptureStep.Location, sheet = null) }
            return true
        }
        return false
    }

    fun selfieTaken(path: String) {
        _state.value.selfie?.takeIf { it != path }?.let { File(it).delete() }
        _state.update { it.copy(selfie = path, liveness = Liveness.Checking, sheet = null) }
        viewModelScope.launch {
            delay(LIVENESS_MILLIS)
            if (simulation.value.livenessFails) {
                File(path).delete()
                _state.update { it.copy(selfie = null, liveness = Liveness.None, sheet = CaptureSheet.Liveness) }
            } else {
                _state.update { it.copy(liveness = Liveness.Passed) }
            }
        }
    }

    fun submit() {
        val s = _state.value
        val found = s.found ?: return
        if (!s.online || s.liveness != Liveness.Passed || _finished.value != null) return
        val at = repo.now().toEpochMilli()
        val punch = Punch(at, found.lat, found.lng, found.address ?: s.reference.address, s.selfie)
        when (s.mode) {
            CaptureMode.CLOCK_IN -> repo.clockIn(punch)
            CaptureMode.CLOCK_OUT -> repo.clockOut(punch)
            CaptureMode.CHECK_IN -> repo.checkIn(requireNotNull(visitId), punch)
        }
        _finished.value = at
    }

    override fun onCleared() {
        // A selfie that was not submitted is not kept.
        if (_finished.value == null) _state.value.selfie?.let { File(it).delete() }
    }

    private companion object {
        /** Simulated passive liveness check. */
        const val LIVENESS_MILLIS = 1_500L
    }
}
