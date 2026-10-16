package com.example.and_sales_portal.ui.partnerregistration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.partner.Partner
import com.example.and_sales_portal.designsystem.components.inputs.AmarSelectionControl
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme
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
 * the prototype: the location card shows the placeholder, coordinates and address. PRD Scope 1 v2 C5: the consent
 * checkbox sits above "Kirim Pengajuan", which stays disabled until it is ticked; its title opens REG-12.
 */
@Composable
fun RegLocationScreen(
    location: LocationCaptureState,
    submitting: Boolean,
    onBack: () -> Unit,
    actions: CaptureActions,
    onSubmit: () -> Unit,
    consent: Boolean = false,
    onConsent: (Boolean) -> Unit = {},
    onOpenConsent: () -> Unit = {},
) {
    ScreenScaffold(
        stringResource(R.string.reg_app_bar_title),
        onBack,
        action = {
            if (location.found != null) {
                PersistentAction(stringResource(R.string.reg_location_cta), onSubmit, enabled = consent, loading = submitting)
            } else {
                PersistentAction(stringResource(R.string.ts_take_location), actions.onLocate, loading = location.locating)
            }
        },
    ) {
        StepIntro(null, RegStep.count, stringResource(R.string.reg_location_title), stringResource(R.string.reg_location_description))
        LocationCaptureContent(location, actions)
        if (location.found != null) ConsentCheck(consent, onConsent, onOpenConsent)
    }
}

/** "Saya telah membaca dan menyetujui [Konfirmasi dan Persetujuan Pendaftaran Toko]." with the title as a link. */
@Composable
private fun ConsentCheck(checked: Boolean, onChecked: (Boolean) -> Unit, onOpen: () -> Unit) {
    val title = stringResource(R.string.reg_consent_title)
    val full = stringResource(R.string.reg_consent_check, title)
    val start = full.indexOf(title)
    val linkStyle = TextLinkStyles(SpanStyle(color = AmarTheme.colors.content.link, fontWeight = FontWeight.SemiBold))
    val text = buildAnnotatedString {
        append(full.substring(0, start))
        withLink(LinkAnnotation.Clickable("consent", linkStyle) { onOpen() }) { append(title) }
        append(full.substring(start + title.length))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AmarSpace.S8), verticalAlignment = Alignment.Top) {
        AmarSelectionControl(checked, onChecked)
        Text(text, style = AmarTheme.typography.bodyMd, color = AmarTheme.colors.content.primary)
    }
}

/** Spec: REG-12 Konfirmasi dan Persetujuan Pendaftaran Toko (PRD Scope 1 v2 C5), opened from the REG-08 checkbox. */
@Composable
fun RegConsentScreen(onBack: () -> Unit) {
    ScreenScaffold(stringResource(R.string.reg_consent_title), onBack) {
        Text(stringResource(R.string.reg_consent_p1), style = AmarTheme.typography.bodyMd, color = AmarTheme.colors.content.primary)
        Text(stringResource(R.string.reg_consent_p2), style = AmarTheme.typography.bodyMd, color = AmarTheme.colors.content.primary)
    }
}
