package com.example.and_sales_portal.ui.partnerregistration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.feedback.AmarBottomSheet
import com.example.and_sales_portal.designsystem.components.feedback.AmarBottomSheetType
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBox
import com.example.and_sales_portal.designsystem.components.feedback.AmarInfoBoxType
import com.example.and_sales_portal.designsystem.components.feedback.AmarSheetSelection
import com.example.and_sales_portal.designsystem.components.feedback.amarBottomSheetDismiss
import com.example.and_sales_portal.designsystem.components.inputs.AmarDropdownPicker
import com.example.and_sales_portal.designsystem.components.inputs.AmarProgressPhotos
import com.example.and_sales_portal.designsystem.components.inputs.AmarProgressPhotosStatus
import com.example.and_sales_portal.designsystem.components.inputs.AmarTextField
import com.example.and_sales_portal.designsystem.components.inputs.AmarUploadFiles
import com.example.and_sales_portal.designsystem.components.inputs.formatFileSize
import com.example.and_sales_portal.designsystem.components.layout.AmarDivider
import com.example.and_sales_portal.designsystem.theme.AmarFormTokens
import com.example.and_sales_portal.ui.partnerregistration.data.Bank

/** Callbacks of REG-05. */
class BankAccountActions(
    val onBank: (Bank) -> Unit = {},
    val onAccountNumber: (String) -> Unit = {},
    val onCheck: () -> Unit = {},
    val onContinue: () -> Unit = {},
    val onBack: () -> Unit = {},
)

/**
 * Spec: REG-05 Data Rekening (Figma "04 | Bank Account Information").
 *
 * - "Periksa" is enabled once a bank is chosen (Figma "Bank Selected") and disabled again after a successful inquiry.
 * - Found: Nama Rekening appears disabled (`*Show (disabled)`) with an Info Box; not found: an error Info Box.
 * - "Lanjut Dokumen Upload" appears only after the account is verified (it is absent in the other Figma frames).
 */
@Composable
fun RegBankAccountScreen(state: PartnerRegistrationState, actions: BankAccountActions) {
    val form = state.bankAccount
    val missing = if (RegStep.BankAccount in state.showErrors) PartnerRegistrationValidator.missingBankAccount(form) else emptySet()
    val found = form.inquiry as? AccountInquiry.Found
    RegStepScaffold(
        step = RegStep.BankAccount,
        title = stringResource(R.string.reg_bank_title),
        description = stringResource(R.string.reg_bank_description),
        onBack = actions.onBack,
        cta = if (found != null) stringResource(R.string.reg_bank_cta) else null,
        onCta = actions.onContinue,
    ) {
        val bankError = RegField.Bank in missing
        AmarDropdownPicker(
            value = form.bank,
            options = state.banks,
            onSelect = actions.onBank,
            sheetTitle = stringResource(R.string.reg_bank_sheet),
            modifier = Modifier.fillMaxWidth(),
            optionLabel = { it.name },
            label = stringResource(R.string.reg_bank),
            required = true,
            placeholder = stringResource(R.string.reg_bank_placeholder),
            hint = requiredHint(bankError),
            error = bankError,
            enabled = state.banks.isNotEmpty(),
            searchPlaceholder = stringResource(R.string.reg_bank_search),
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AmarFormTokens.SideBySideGap),
            verticalAlignment = Alignment.Bottom,
        ) {
            val numberError = RegField.AccountNumber in missing
            AmarTextField(
                value = form.accountNumber,
                onValueChange = actions.onAccountNumber,
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.reg_account_number),
                required = true,
                placeholder = stringResource(R.string.reg_account_number_placeholder),
                hint = requiredHint(numberError),
                error = numberError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            AmarButton(
                stringResource(R.string.reg_account_check),
                actions.onCheck,
                type = AmarButtonType.Secondary,
                size = AmarButtonSize.Medium,
                enabled = form.bank != null && found == null,
                loading = form.inquiry == AccountInquiry.Loading,
            )
        }
        if (found != null) {
            AmarTextField(
                value = found.accountName,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.reg_account_name),
                required = true,
                enabled = false,
            )
            AmarInfoBox(stringResource(R.string.reg_account_found), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Info)
        }
        if (form.inquiry == AccountInquiry.NotFound) {
            AmarInfoBox(stringResource(R.string.reg_account_not_found), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Error)
        }
    }
}

/** Callbacks of REG-06. */
class DocumentsActions(
    val onCamera: (RegDocument) -> Unit = {},
    val onGallery: (RegDocument) -> Unit = {},
    val onRemove: (RegDocument) -> Unit = {},
    val onRetry: (RegDocument) -> Unit = {},
    val onSubmit: () -> Unit = {},
    val onBack: () -> Unit = {},
)

/**
 * Spec: REG-06 Dokumen Upload (Figma "05 | Document Upload"). The documents follow `Jenis Badan Usaha` from REG-03:
 * PT and CV upload seven company documents ("Ambil Dokumen"), Individu three photos ("Ambil Foto"). Each button opens
 * the "Metode Upload Dokumen" sheet (camera or gallery). "Kirim Dokumen" is enabled once every document is uploaded.
 */
@Composable
fun RegDocumentsScreen(state: PartnerRegistrationState, actions: DocumentsActions) {
    var methodFor by remember { mutableStateOf<RegDocument?>(null) }
    val individual = state.entityType == ENTITY_INDIVIDUAL
    RegStepScaffold(
        step = RegStep.Documents,
        title = stringResource(R.string.reg_documents_title),
        description = stringResource(R.string.reg_documents_description),
        onBack = actions.onBack,
        cta = stringResource(R.string.reg_documents_cta),
        onCta = actions.onSubmit,
        ctaEnabled = PartnerRegistrationValidator.documentsComplete(state.requiredDocuments, state.documents),
        ctaLoading = state.submitting,
    ) {
        state.requiredDocuments.forEach { document ->
            val upload = state.documents[document]
            AmarUploadFiles(
                label = stringResource(document.label),
                buttonLabel = stringResource(if (individual) R.string.reg_doc_take_photo else R.string.reg_doc_take_document),
                onUploadClick = { methodFor = document },
                modifier = Modifier.fillMaxWidth(),
                buttonIcon = painterResource(R.drawable.ds_ic_upload_document_24),
                files = upload?.let { { RegUploadRow(it, { actions.onRemove(document) }, { actions.onRetry(document) }) } },
            )
        }
    }
    methodFor?.let { document ->
        AmarBottomSheet(
            onDismissRequest = { methodFor = null },
            type = AmarBottomSheetType.Compact,
            title = stringResource(R.string.reg_upload_method_title),
            showHandle = false,
        ) {
            val dismiss = amarBottomSheetDismiss()
            AmarSheetSelection(
                stringResource(R.string.reg_upload_camera),
                onClick = { dismiss(); actions.onCamera(document) },
                description = stringResource(R.string.reg_upload_camera_description),
                icon = painterResource(R.drawable.ds_ic_camera_24),
            )
            AmarDivider()
            AmarSheetSelection(
                stringResource(R.string.reg_upload_gallery),
                onClick = { dismiss(); actions.onGallery(document) },
                description = stringResource(R.string.reg_upload_gallery_description),
                icon = painterResource(R.drawable.ds_ic_photo_24),
            )
        }
    }
}

/** One uploaded file: Loading with progress, Uploaded Default, or Failed with retry. */
@Composable
private fun RegUploadRow(upload: DocumentUpload, onRemove: () -> Unit, onRetry: () -> Unit) {
    val status = when (upload.status) {
        UploadStatus.Uploading -> AmarProgressPhotosStatus.Loading
        UploadStatus.Uploaded -> AmarProgressPhotosStatus.UploadedDefault
        UploadStatus.Failed -> AmarProgressPhotosStatus.Failed
    }
    AmarProgressPhotos(
        fileName = upload.fileName,
        status = status,
        fileSize = formatFileSize(upload.sizeBytes),
        progress = upload.progress,
        onRetry = onRetry,
        onDelete = onRemove,
    )
}
