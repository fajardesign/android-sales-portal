package com.example.and_sales_portal.ui.common

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.device.LocationResult
import com.example.and_sales_portal.data.device.LocationService
import com.example.and_sales_portal.data.device.Place
import com.example.and_sales_portal.designsystem.components.feedback.AmarSpinner
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.attendance.CaptureActions
import com.example.and_sales_portal.ui.attendance.CaptureSheet
import com.example.and_sales_portal.ui.attendance.CaptureSheetFor
import com.example.and_sales_portal.ui.attendance.MockLocationDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocationCaptureState(
    val locating: Boolean = false,
    val found: LocationResult.Found? = null,
    val sheet: CaptureSheet? = null,
    /** PRD C2/C4 "mock location blocked": a non-dismissable dialog. */
    val mock: Boolean = false,
)

/**
 * Location capture of REG-08 (registration "map pin") and Tambah Toko: permission, GPS off, no fix and mock location,
 * with the same sheets as the ATT capture flow. Create it as a property of the [fragment] (it registers the permission
 * request) and call [onResume] from the fragment's `onResume`.
 */
class LocationCapture(private val fragment: Fragment, private val service: () -> LocationService, private val reference: () -> Place) {

    private val _state = MutableStateFlow(LocationCaptureState())
    val state: StateFlow<LocationCaptureState> = _state.asStateFlow()

    private val permission = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.values.any { it }) locate() else _state.update { it.copy(locating = false, sheet = CaptureSheet.Permission) }
    }

    fun start() {
        if (service().hasPermission()) locate() else permission.launch(LocationService.PERMISSIONS)
    }

    fun locate() {
        if (_state.value.locating) return
        _state.update { it.copy(locating = true, sheet = null) }
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            val r = service().current(reference())
            _state.update {
                when (r) {
                    is LocationResult.Found -> it.copy(locating = false, found = r.takeUnless { f -> f.mock }, mock = r.mock)
                    LocationResult.PermissionDenied -> it.copy(locating = false, sheet = CaptureSheet.Permission)
                    LocationResult.GpsOff -> it.copy(locating = false, sheet = CaptureSheet.GpsOff)
                    LocationResult.Unavailable -> it.copy(locating = false, sheet = CaptureSheet.Unavailable)
                }
            }
        }
    }

    /** Back from the app or location settings. */
    fun onResume() {
        val s = _state.value
        if (s.sheet == CaptureSheet.Permission && service().hasPermission()) locate()
        if (s.sheet == CaptureSheet.GpsOff && service().isEnabled()) locate()
    }

    fun dismissMock() = _state.update { it.copy(mock = false) }

    fun actions(onOpenMaps: (Double, Double) -> Unit) = CaptureActions(
        onLocate = ::start,
        onOpenMaps = onOpenMaps,
        onSheetPrimary = { sheet ->
            when (sheet) {
                CaptureSheet.Permission -> {
                    _state.update { it.copy(sheet = null) }
                    if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        permission.launch(LocationService.PERMISSIONS)
                    } else {
                        open(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", fragment.requireContext().packageName, null)))
                    }
                }
                CaptureSheet.GpsOff -> open(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                else -> locate()
            }
        },
        onSheetSecondary = { locate() },
        onDismissSheet = { _state.update { it.copy(sheet = null) } },
        onMockClose = ::dismissMock,
    )

    private fun open(intent: Intent) {
        try {
            fragment.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // No settings screen: the sheet stays so the user can retry.
        }
    }
}

/** Location card (map placeholder, coordinates, address) with the sheets and the mock-location dialog. */
@Composable
fun LocationCaptureContent(state: LocationCaptureState, actions: CaptureActions) {
    val c = AmarTheme.colors
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
        val found = state.found
        if (found == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.SequenceGap), verticalAlignment = Alignment.CenterVertically) {
                if (state.locating) AmarSpinner(c.icon.brand)
                Text(
                    stringResource(if (state.locating) R.string.cap_locating else R.string.ts_location_required),
                    color = c.content.secondary,
                    style = AmarTheme.typography.bodyMd,
                )
            }
        } else {
            MapPreview(found.lat, found.lng, { actions.onOpenMaps(found.lat, found.lng) })
            InfoRow(stringResource(R.string.cap_address), found.address ?: stringResource(R.string.cap_address_unknown))
        }
    }
    state.sheet?.let { CaptureSheetFor(it, actions) }
    if (state.mock) MockLocationDialog(actions.onMockClose)
}
