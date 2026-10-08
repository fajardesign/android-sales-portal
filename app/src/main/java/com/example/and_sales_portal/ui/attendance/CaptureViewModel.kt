package com.example.and_sales_portal.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.and_sales_portal.data.activity.ActivityRepository
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.activity.Punch
import com.example.and_sales_portal.data.device.LocationResult
import com.example.and_sales_portal.data.device.LocationService
import com.example.and_sales_portal.data.device.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/** What the capture flow records. */
enum class CaptureMode { CLOCK_IN, CLOCK_OUT, CHECK_IN }

enum class CaptureStep { Location, Selfie }

/** Bottom sheets of the flow (the mock-location dialog is separate: it cannot be dismissed). */
enum class CaptureSheet { Permission, GpsOff, Unavailable, Radius }

data class CaptureUiState(
    val mode: CaptureMode,
    /** Where the user must be: the toko (visit), or the area office and the toko (attendance). */
    val places: List<Place>,
    /** Toko name for a visit check-in. */
    val storeName: String? = null,
    val step: CaptureStep = CaptureStep.Location,
    val locating: Boolean = false,
    val found: LocationResult.Found? = null,
    /** Closest of [places] (attendance: office or toko). */
    val nearest: Place? = null,
    /** Metres to [nearest]. */
    val distance: Int? = null,
    val sheet: CaptureSheet? = null,
    val mockDetected: Boolean = false,
    val selfie: String? = null,
    val online: Boolean = true,
) {
    val radius: Int get() = if (mode == CaptureMode.CHECK_IN) ActivityRules.VISIT_RADIUS_METERS else ActivityRules.ATTENDANCE_RADIUS_METERS
    val inRadius: Boolean get() = distance != null && distance <= radius
    val locationOk: Boolean get() = found != null && !found.mock && inRadius
}

/**
 * Spec: ATT-02 Check In / Check Out Kerja and ATT-04 Check-In Kunjungan (PRD v3 D2, D3; revision 2026-10-08: selfie and
 * location only, no biometric or liveness). Steps: location (map, coordinates, address; within 3 km of the area office
 * or a toko for attendance, of the toko for a visit) → selfie → submit (online only).
 */
class CaptureViewModel(
    private val repo: ActivityRepository,
    private val location: LocationService,
    mode: CaptureMode,
    /** Toko of a visit check-in. */
    private val storeId: String?,
    places: List<Place>,
    storeName: String?,
) : ViewModel() {

    private val _state = MutableStateFlow(CaptureUiState(mode, places, storeName))
    val state: StateFlow<CaptureUiState> = _state.asStateFlow()

    /** Time of the saved punch; the screen closes and shows a toast. */
    private val _finished = MutableStateFlow<Long?>(null)
    val finished: StateFlow<Long?> = _finished.asStateFlow()

    fun setOnline(online: Boolean) = _state.update { it.copy(online = online) }

    fun locate() {
        if (_state.value.locating) return
        _state.update { it.copy(locating = true, sheet = null, found = null, nearest = null, distance = null) }
        viewModelScope.launch {
            val s = _state.value
            when (val r = location.current(s.places.first())) {
                is LocationResult.Found -> {
                    val nearest = ActivityRules.nearest(s.places, r.lat, r.lng)
                    _state.update {
                        val next = it.copy(locating = false, found = r, nearest = nearest?.first, distance = nearest?.second, mockDetected = r.mock)
                        next.copy(sheet = if (!r.mock && !next.inRadius) CaptureSheet.Radius else null)
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
        _state.update { it.copy(selfie = path, sheet = null) }
    }

    fun submit() {
        val s = _state.value
        val found = s.found ?: return
        if (!s.online || s.selfie == null || !s.locationOk || _finished.value != null) return
        val at = repo.now().toEpochMilli()
        val punch = Punch(at, found.lat, found.lng, found.address ?: s.nearest?.address, s.selfie, place = s.nearest?.name, distance = s.distance)
        when (s.mode) {
            CaptureMode.CLOCK_IN -> repo.clockIn(punch)
            CaptureMode.CLOCK_OUT -> repo.clockOut(punch)
            // Revision 2026-10-08: the visit is created at check-in; refused when not allowed (e.g. before 12:00).
            CaptureMode.CHECK_IN -> if (repo.checkIn(requireNotNull(storeId), punch) == null) return
        }
        _finished.value = at
    }

    override fun onCleared() {
        // A selfie that was not submitted is not kept.
        if (_finished.value == null) _state.value.selfie?.let { File(it).delete() }
    }
}
