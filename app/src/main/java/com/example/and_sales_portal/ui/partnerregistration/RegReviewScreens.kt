package com.example.and_sales_portal.ui.partnerregistration

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.ui.attendance.CaptureActions
import com.example.and_sales_portal.ui.common.LocationCaptureContent
import com.example.and_sales_portal.ui.common.LocationCaptureState
import com.example.and_sales_portal.ui.common.PersistentAction
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.common.StepIntro
import com.example.and_sales_portal.ui.partner.PartnerSections

/** Spec: REG-07 Ringkasan Pengajuan (PRD v3 C2 step 6 "review summary"), with "Ubah" per section. */
@Composable
fun RegReviewScreen(
    preview: Partner,
    onBack: () -> Unit,
    onEdit: (RegStep) -> Unit,
    onContinue: () -> Unit,
) {
    ScreenScaffold(
        stringResource(R.string.reg_app_bar_title),
        onBack,
        action = { PersistentAction(stringResource(R.string.reg_review_cta), onContinue) },
    ) {
        StepIntro(null, RegStep.count, stringResource(R.string.reg_review_title), stringResource(R.string.reg_review_description))
        PartnerSections(preview, onEdit = onEdit)
    }
}

/**
 * Spec: REG-08 Lokasi Usaha (PRD v3 C2 step 6 "location capture with map pin (mock location blocked)"). No map SDK in
 * the prototype: the location card shows the placeholder, coordinates and address. PRD Scope 1 v1.1 C5 (decision
 * 2026-10-20): no consent; "Kirim Pengajuan" is enabled as soon as the location is read.
 */
@Composable
fun RegLocationScreen(location: LocationCaptureState, submitting: Boolean, onBack: () -> Unit, actions: CaptureActions, onSubmit: () -> Unit) {
    ScreenScaffold(
        stringResource(R.string.reg_app_bar_title),
        onBack,
        action = {
            if (location.found != null) {
                PersistentAction(stringResource(R.string.reg_location_cta), onSubmit, loading = submitting)
            } else {
                PersistentAction(stringResource(R.string.ts_take_location), actions.onLocate, loading = location.locating)
            }
        },
    ) {
        StepIntro(null, RegStep.count, stringResource(R.string.reg_location_title), stringResource(R.string.reg_location_description))
        LocationCaptureContent(location, actions)
    }
}
