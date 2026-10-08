package com.example.and_sales_portal.ui.partnerregistration

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.partner.PartnerStatus
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.ui.partner.PartnerStatusBadge
import com.example.and_sales_portal.designsystem.theme.AmarSuccessTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.designsystem.theme.amarShadow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Spec: REG-09 Pengajuan Terkirim (Figma "06 | Partner Registration | Success | Submission Sent"). Shows the Large /
 * Success illustration, the status Under Review (PRD C2) and the registration number, with "Kembali ke Partner" and
 * "Lihat Pengajuan".
 *
 * @param partnerName Nama Partner from REG-02 (Figma shows a store name; the flow has no separate store name).
 * @param submissionId Registration number `REGYYYY-NNNN`.
 */
@Composable
fun RegSuccessScreen(
    partnerName: String,
    submissionId: String,
    submittedAtMillis: Long,
    onDone: () -> Unit,
    onSeeSubmission: () -> Unit = {},
    windowInsets: WindowInsets = WindowInsets.statusBars,
) {
    val c = AmarTheme.colors
    val t = AmarSuccessTokens
    Column(Modifier.fillMaxSize().background(c.background.base)) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(windowInsets)
                .padding(start = t.ContentPadding, end = t.ContentPadding, top = t.ContentPaddingTop, bottom = t.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(t.ContentGap),
        ) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(t.MessageGap), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.ds_ill_large_success), contentDescription = null)
                Text(stringResource(R.string.reg_success_title), color = c.content.primary, style = AmarTheme.typography.headingLg, textAlign = TextAlign.Center)
                Text(stringResource(R.string.reg_success_description, partnerName), color = c.content.secondary, style = AmarTheme.typography.bodyMd, textAlign = TextAlign.Center)
                PartnerStatusBadge(PartnerStatus.UNDER_REVIEW)
            }
            val shape = AmarTheme.shapes.card
            Column(
                Modifier
                    .fillMaxWidth()
                    .amarShadow(AmarTheme.elevation.surface, shape)
                    .background(c.surface.base, shape) // Card/Background
                    .border(AmarTheme.borderWidths.subtle, c.border.subtle, shape) // Card/Border Color
                    .padding(t.SummaryPadding),
                verticalArrangement = Arrangement.spacedBy(t.SummaryGap),
            ) {
                SummaryRow(stringResource(R.string.reg_success_number), submissionId)
                SummaryRow(stringResource(R.string.reg_success_partner), partnerName)
                SummaryRow(stringResource(R.string.reg_success_sent), formatSubmittedAt(submittedAtMillis))
            }
            AmarButton(stringResource(R.string.reg_success_see), onSeeSubmission, Modifier.fillMaxWidth(), type = AmarButtonType.Naked, size = AmarButtonSize.Large)
        }
        RegPersistentAction(stringResource(R.string.reg_success_cta), onDone, windowInsets = WindowInsets.navigationBars)
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val c = AmarTheme.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AmarSuccessTokens.RowGap), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = c.content.secondary, style = AmarTheme.typography.bodySm)
        Text(value, Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.bodyMdMedium, textAlign = TextAlign.End)
    }
}

/** "29 Sep 2026, 14:32" as in Figma (Indonesian month abbreviations). */
fun formatSubmittedAt(millis: Long): String = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id", "ID")).format(Date(millis))
