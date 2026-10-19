package com.example.and_sales_portal.ui.attendance

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.activity.ActivityRepository
import com.example.and_sales_portal.data.activity.ActivityRules
import com.example.and_sales_portal.data.device.LocationService
import com.example.and_sales_portal.data.device.Place
import com.example.and_sales_portal.ui.common.ComposeFragment
import com.example.and_sales_portal.ui.common.Fmt
import com.example.and_sales_portal.ui.common.WithToast
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

/** Spec: ATT-01 Absensi (PRD v3 D2; TL from Aktivitas, SA/SR from Beranda). */
class AttendanceFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val repo = activityRepo
        val data by repo.data.collectAsState()
        val online by container.connectivity.online.collectAsState()
        val (toast, clearToast) = incomingToast()
        val state = remember(data, online) { attendanceState(repo, online) }
        WithToast(toast, clearToast) {
            AttendanceScreen(
                state,
                onBack = ::back,
                onClockIn = { navigate(R.id.capture, CaptureFragment.args(CaptureMode.CLOCK_IN)) },
                onClockOut = { navigate(R.id.capture, CaptureFragment.args(CaptureMode.CLOCK_OUT)) },
            )
        }
    }

    companion object {
        /** Days shown in the history (every day is a workday, Q6). */
        private const val HISTORY_DAYS = 14L

        fun attendanceState(repo: ActivityRepository, online: Boolean): AttendanceUiState {
            val today = repo.today()
            val history = (1..HISTORY_DAYS).map { today.minusDays(it) }.map { attendanceDay(repo.attendanceOf(it), it, today) }
            return AttendanceUiState(attendanceDay(repo.attendanceOf(today), today, today).copy(week = repo.attendanceWeek(today)), history, online)
        }

        fun attendanceDay(a: com.example.and_sales_portal.data.activity.Attendance?, d: LocalDate, today: LocalDate) =
            AttendanceDay(d, a, ActivityRules.attendanceStatus(a, d, today), ActivityRules.noCheckOut(a, d, today))
    }
}

/**
 * Spec: ATT-02 Check In / Check Out Kerja and ATT-04 Check-In Kunjungan. Device parts live here: location permission and
 * settings, system camera for the selfie (revision 2026-10-08: no biometric or liveness).
 */
class CaptureFragment : ComposeFragment() {

    private val mode by lazy { CaptureMode.valueOf(requireArguments().getString(ARG_MODE)!!) }
    private val storeId by lazy { requireArguments().getString(ARG_STORE) }

    private val vm: CaptureViewModel by viewModels {
        viewModelFactory {
            initializer {
                val repo = activityRepo
                val store = storeId?.let { repo.store(it) }
                val places = if (store != null) listOf(Place(store.lat, store.lng, store.address, store.name)) else repo.attendancePlaces()
                CaptureViewModel(repo, container.location, mode, store?.id, places, store?.name)
            }
        }
    }

    private val permission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.values.any { it }) vm.locate() else vm.permissionDenied()
    }

    private var pendingSelfie: File? = null
    private val camera = registerForActivityResult(FrontCameraPicture()) { saved ->
        val file = pendingSelfie
        pendingSelfie = null
        if (saved && file != null && file.length() > 0) vm.selfieTaken(file.absolutePath) else file?.delete()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingSelfie = savedInstanceState?.getString(KEY_PENDING)?.let(::File)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pendingSelfie?.let { outState.putString(KEY_PENDING, it.absolutePath) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { if (!vm.back()) back() }
        viewLifecycleOwner.lifecycleScope.launch { container.connectivity.online.collect(vm::setOnline) }
    }

    override fun onResume() {
        super.onResume()
        // Back from the app or location settings.
        val s = vm.state.value
        if (s.sheet == CaptureSheet.Permission && container.location.hasPermission()) vm.locate()
        if (s.sheet == CaptureSheet.GpsOff && container.location.isEnabled()) vm.locate()
    }

    @Composable
    override fun ScreenContent() {
        val state by vm.state.collectAsState()
        val finished by vm.finished.collectAsState()
        val doneMessage = when (mode) {
            CaptureMode.CLOCK_IN -> R.string.att_clock_in_done
            CaptureMode.CLOCK_OUT -> R.string.att_clock_out_done
            CaptureMode.CHECK_IN -> R.string.vis_check_in_done
        }
        LaunchedEffect(finished) { finished?.let { backWithToast(getString(doneMessage, Fmt.timeLabel(it))) } }
        LaunchedEffect(Unit) { if (state.found == null && !state.locating && state.sheet == null) startLocation() }
        CaptureScreen(
            state,
            CaptureActions(
                onBack = { if (!vm.back()) back() },
                onLocate = ::startLocation,
                onNext = vm::toSelfie,
                onTakeSelfie = ::takeSelfie,
                onSubmit = vm::submit,
                onOpenMaps = ::openMaps,
                onSheetPrimary = { sheet ->
                    when (sheet) {
                        CaptureSheet.Permission -> {
                            vm.dismissSheet()
                            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                permission.launch(LocationService.PERMISSIONS)
                            } else {
                                open(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", requireContext().packageName, null)))
                            }
                        }
                        CaptureSheet.GpsOff -> open(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        CaptureSheet.Unavailable, CaptureSheet.Radius, CaptureSheet.Accuracy -> vm.locate()
                    }
                },
                onSheetSecondary = { vm.locate() },
                onDismissSheet = vm::dismissSheet,
                onMockClose = ::back,
            ),
        )
    }

    private fun startLocation() {
        if (container.location.hasPermission()) vm.locate() else permission.launch(LocationService.PERMISSIONS)
    }

    private fun takeSelfie() {
        vm.dismissSheet()
        val file = File(container.demo.selfieDir, "selfie_${System.currentTimeMillis()}.jpg")
        pendingSelfie = file
        val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        try {
            camera.launch(uri)
        } catch (e: ActivityNotFoundException) {
            pendingSelfie = null
        }
    }

    private fun openMaps(lat: Double, lng: Double) {
        val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng"))
        if (!open(geo)) open(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")))
    }

    private fun open(intent: Intent): Boolean = try {
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }

    companion object {
        private const val ARG_MODE = "mode"
        private const val ARG_STORE = "store_id"
        private const val KEY_PENDING = "pending_selfie"

        fun args(mode: CaptureMode, storeId: String? = null) = bundleOf(ARG_MODE to mode.name, ARG_STORE to storeId)
    }
}

/** System camera, asking for the front camera (a hint: camera apps may ignore it). */
private class FrontCameraPicture : ActivityResultContracts.TakePicture() {
    override fun createIntent(context: Context, input: Uri): Intent = super.createIntent(context, input).apply {
        putExtra("android.intent.extras.CAMERA_FACING", 1)
        putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
        putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
    }
}
