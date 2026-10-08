package com.example.and_sales_portal.ui.attendance

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.exifinterface.media.ExifInterface
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadge
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeSize
import com.example.and_sales_portal.designsystem.components.feedback.AmarBadgeType
import com.example.and_sales_portal.designsystem.components.feedback.AmarBottomSheet
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
import com.example.and_sales_portal.designsystem.components.feedback.AmarModal
import com.example.and_sales_portal.designsystem.components.feedback.AmarModalIllustration
import com.example.and_sales_portal.designsystem.components.feedback.AmarSpinner
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.common.InfoRow
import com.example.and_sales_portal.ui.common.MapPreview
import com.example.and_sales_portal.ui.common.PersistentAction
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.common.StepIntro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class CaptureActions(
    val onBack: () -> Unit = {},
    val onLocate: () -> Unit = {},
    val onNext: () -> Unit = {},
    val onTakeSelfie: () -> Unit = {},
    val onSubmit: () -> Unit = {},
    val onOpenMaps: (Double, Double) -> Unit = { _, _ -> },
    val onSheetPrimary: (CaptureSheet) -> Unit = {},
    val onSheetSecondary: (CaptureSheet) -> Unit = {},
    val onDismissSheet: () -> Unit = {},
    val onMockClose: () -> Unit = {},
)

fun CaptureMode.title(): Int = when (this) {
    CaptureMode.CLOCK_IN -> R.string.att_clock_in
    CaptureMode.CLOCK_OUT -> R.string.att_clock_out
    CaptureMode.CHECK_IN -> R.string.cap_title_check_in
}

private fun CaptureMode.submitLabel(): Int = when (this) {
    CaptureMode.CLOCK_IN -> R.string.cap_submit_clock_in
    CaptureMode.CLOCK_OUT -> R.string.cap_submit_clock_out
    CaptureMode.CHECK_IN -> R.string.cap_submit_check_in
}

/** Spec: ATT-02 / ATT-04 capture steps. One primary action per step, in the persistent action bar. */
@Composable
fun CaptureScreen(state: CaptureUiState, actions: CaptureActions) {
    val action: @Composable () -> Unit = when (state.step) {
        CaptureStep.Location -> {
            {
                when {
                    state.locationOk -> PersistentAction(stringResource(R.string.cap_next_selfie), actions.onNext)
                    else -> PersistentAction(stringResource(R.string.cap_refresh), actions.onLocate, loading = state.locating)
                }
            }
        }
        CaptureStep.Selfie -> {
            {
                if (state.selfie == null) PersistentAction(stringResource(R.string.cap_take_selfie), actions.onTakeSelfie)
                else PersistentAction(stringResource(state.mode.submitLabel()), actions.onSubmit, enabled = state.online)
            }
        }
    }
    ScreenScaffold(stringResource(state.mode.title()), actions.onBack, background = AmarTheme.colors.background.base, action = action) {
        when (state.step) {
            CaptureStep.Location -> LocationStep(state, actions)
            CaptureStep.Selfie -> SelfieStep(state, actions)
        }
    }
    state.sheet?.let { CaptureSheetFor(it, actions, attendance = state.mode != CaptureMode.CHECK_IN) }
    if (state.mockDetected) MockLocationDialog(actions.onMockClose)
}

@Composable
private fun LocationStep(state: CaptureUiState, actions: CaptureActions) {
    val c = AmarTheme.colors
    StepIntro(
        1, 2,
        stringResource(R.string.cap_location_title),
        stringResource(if (state.mode == CaptureMode.CHECK_IN) R.string.cap_location_desc_visit else R.string.cap_location_desc_attendance),
    )
    state.storeName?.let { Text(it, color = c.content.primary, style = AmarTheme.typography.labelMdSemibold) }
    if (state.mode != CaptureMode.CHECK_IN) AmarInfoBox(stringResource(R.string.cap_radius_info), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Info)
    AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
        val found = state.found
        if (found == null) {
            Row(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.SequenceGap), verticalAlignment = Alignment.CenterVertically) {
                if (state.locating) AmarSpinner(c.icon.brand)
                Text(
                    stringResource(if (state.locating) R.string.cap_locating else R.string.cap_err_unavailable_title),
                    color = c.content.secondary,
                    style = AmarTheme.typography.bodyMd,
                )
            }
        } else {
            MapPreview(found.lat, found.lng, { actions.onOpenMaps(found.lat, found.lng) })
            InfoRow(stringResource(R.string.cap_address), found.address ?: stringResource(R.string.cap_address_unknown))
            if (state.mode != CaptureMode.CHECK_IN) state.nearest?.name?.let { InfoRow(stringResource(R.string.cap_nearest), it) }
            state.distance?.let { d ->
                InfoRow(stringResource(if (state.mode == CaptureMode.CHECK_IN) R.string.cap_distance else R.string.cap_distance_nearest), distanceLabel(d))
                AmarBadge(
                    stringResource(if (state.inRadius) R.string.cap_in_radius else R.string.cap_out_of_radius),
                    type = if (state.inRadius) AmarBadgeType.Success else AmarBadgeType.Error,
                    size = AmarBadgeSize.Small,
                )
            }
        }
    }
}

@Composable
private fun SelfieStep(state: CaptureUiState, actions: CaptureActions) {
    val c = AmarTheme.colors
    StepIntro(2, 2, stringResource(R.string.cap_selfie_title), stringResource(R.string.cap_selfie_desc))
    val bitmap by produceState<ImageBitmap?>(null, state.selfie) { value = state.selfie?.let { loadSelfie(it) } }
    Box(
        Modifier
            .fillMaxWidth()
            .height(AmarActivityTokens.PreviewHeight)
            .clip(RoundedCornerShape(AmarActivityTokens.TileRadius))
            .background(c.background.brandSubtle),
        contentAlignment = Alignment.Center,
    ) {
        val b = bitmap
        if (b != null) {
            Image(b, stringResource(R.string.cap_selfie), Modifier.fillMaxWidth().height(AmarActivityTokens.PreviewHeight), contentScale = ContentScale.Fit)
        } else {
            Icon(painterResource(R.drawable.ds_ic_camera_24), null, Modifier.size(AmarTheme.iconSizes.lg), tint = c.icon.brand)
        }
    }
    if (state.selfie != null) {
        AmarButton(stringResource(R.string.cap_retake), actions.onTakeSelfie, type = AmarButtonType.Naked, size = AmarButtonSize.Small, leftIcon = painterResource(R.drawable.ds_ic_refresh_24))
    }
    if (!state.online) AmarInfoBox(stringResource(R.string.cap_offline), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Warning)
}

@Composable
internal fun CaptureSheetFor(sheet: CaptureSheet, actions: CaptureActions, attendance: Boolean = false) {
    data class Spec(val illustration: Int, val title: Int, val message: Int, val primary: Int, val second: Int?)
    val s = when (sheet) {
        CaptureSheet.Permission -> Spec(R.drawable.ds_ill_medium_permission_location, R.string.cap_err_permission_title, R.string.cap_err_permission, R.string.cap_err_permission_allow, null)
        CaptureSheet.GpsOff -> Spec(R.drawable.ds_ill_medium_permission_location, R.string.cap_err_gps_title, R.string.cap_err_gps, R.string.cap_err_gps_enable, R.string.common_retry)
        CaptureSheet.Unavailable -> Spec(R.drawable.ds_ill_medium_permission_location, R.string.cap_err_unavailable_title, R.string.cap_err_unavailable, R.string.common_retry, null)
        CaptureSheet.Radius -> if (attendance) {
            Spec(R.drawable.ds_ill_medium_stopper, R.string.cap_err_radius_attendance_title, R.string.cap_err_radius_attendance, R.string.common_retry, null)
        } else {
            Spec(R.drawable.ds_ill_medium_stopper, R.string.cap_err_radius_title, R.string.cap_err_radius, R.string.common_retry, null)
        }
    }
    AmarBottomSheet(
        onDismissRequest = actions.onDismissSheet,
        title = stringResource(s.title),
        description = stringResource(s.message),
        illustration = { Image(painterResource(s.illustration), null) },
        primaryLabel = stringResource(s.primary),
        onPrimaryClick = { actions.onSheetPrimary(sheet) },
        secondLabel = s.second?.let { stringResource(it) },
        onSecondClick = { actions.onSheetSecondary(sheet) },
    )
}

/** PRD D2/D3: mock location blocked with a non-dismissable dialog; the only way out closes the flow. */
@Composable
internal fun MockLocationDialog(onClose: () -> Unit) {
    Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        AmarModal(
            primaryLabel = stringResource(R.string.common_back),
            onPrimaryClick = onClose,
            title = stringResource(R.string.cap_err_mock_title),
            description = stringResource(R.string.cap_err_mock),
            illustration = { AmarModalIllustration { Image(painterResource(R.drawable.ds_ill_medium_stopper), null) } },
        )
    }
}

@Composable
internal fun distanceLabel(meters: Int): String =
    if (meters < METERS_PER_KM) stringResource(R.string.cap_distance_value, meters)
    else stringResource(R.string.cap_distance_km, NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 1 }.format(meters / METERS_PER_KM.toDouble()))

private const val METERS_PER_KM = 1_000

/** Downsampled selfie, upright (camera apps store the rotation in EXIF). */
private suspend fun loadSelfie(path: String): ImageBitmap? = withContext(Dispatchers.IO) {
    runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SELFIE_PX) sample *= 2
        val bitmap = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample }) ?: return@runCatching null
        val degrees = when (ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        val upright: Bitmap = if (degrees == 0f) bitmap else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(degrees) }, true)
        upright.asImageBitmap()
    }.getOrNull()
}

private const val MAX_SELFIE_PX = 720
