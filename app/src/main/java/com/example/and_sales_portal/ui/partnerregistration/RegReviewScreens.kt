package com.example.and_sales_portal.ui.partnerregistration

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.designsystem.components.inputs.AmarSelectionControl
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
    consentRequired: Boolean = false,
    consent: Boolean = false,
    onConsent: (Boolean) -> Unit = {},
) {
    ScreenScaffold(
        stringResource(R.string.reg_app_bar_title),
        onBack,
        action = { PersistentAction(stringResource(R.string.reg_review_cta), onContinue, enabled = !consentRequired || consent) },
    ) {
        StepIntro(null, RegStep.count, stringResource(R.string.reg_review_title), stringResource(R.string.reg_review_description))
        PartnerSections(preview, onEdit = onEdit)
        // FR-PA-007 (PRD alignment 2026-10-09): consent before submit, behind a flag until Business confirms; copy TBD.
        if (consentRequired) AmarSelectionControl(consent, onConsent, Modifier.fillMaxWidth(), label = stringResource(R.string.reg_consent))
    }
}

/**
 * Spec: REG-08 Lokasi Usaha (PRD v3 C2 step 6 "location capture with map pin (mock location blocked)"). No map SDK in
 * the prototype: the location card shows the placeholder, coordinates and address.
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
