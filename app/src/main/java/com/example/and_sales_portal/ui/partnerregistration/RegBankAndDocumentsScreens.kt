package com.example.and_sales_portal.ui.partnerregistration

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.partner.DocType
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
import com.example.and_sales_portal.ui.partnerregistration.data.Bank

/** Callbacks of REG-05. */
class BankAccountActions(
    val onBank: (Bank) -> Unit = {},
    val onBranch: (String) -> Unit = {},
    val onAccountNumber: (String) -> Unit = {},
    val onAccountName: (String) -> Unit = {},
    val onContinue: () -> Unit = {},
    val onBack: () -> Unit = {},
)

/**
 * Spec: REG-05 Data Rekening (PRD v3 C2 step 4): Bank, Cabang, No. Rekening, Nama Rekening (as printed), with the note
 * "Data rekening akan diverifikasi manual oleh Admin." No bank inquiry in phase 1 (PRD overrides the Figma "Periksa").
 */
@Composable
fun RegBankAccountScreen(state: PartnerRegistrationState, actions: BankAccountActions) {
    val form = state.bank
    val errors = if (RegStep.BankAccount in state.showErrors) PartnerRegistrationValidator.bank(form) else emptyMap()
    RegStepScaffold(
        step = RegStep.BankAccount,
        title = stringResource(R.string.reg_bank_title),
        description = stringResource(R.string.reg_bank_description),
        onBack = actions.onBack,
        cta = stringResource(R.string.reg_bank_cta),
        onCta = actions.onContinue,
    ) {
        AmarInfoBox(stringResource(R.string.reg_bank_note), Modifier.fillMaxWidth(), type = AmarInfoBoxType.Info)
        val bankError = errors[RegField.Bank]
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
            hint = errorHint(bankError),
            error = bankError != null,
            enabled = state.banks.isNotEmpty(),
            searchPlaceholder = stringResource(R.string.reg_bank_search),
        )
        RegTextField(form.branch, actions.onBranch, R.string.reg_bank_branch, R.string.reg_bank_branch_placeholder, errors[RegField.Branch])
        val numberError = errors[RegField.AccountNumber]
        AmarTextField(
            value = form.accountNumber,
            onValueChange = actions.onAccountNumber,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.reg_account_number),
            required = true,
            placeholder = stringResource(R.string.reg_account_number_placeholder),
            hint = errorHint(numberError),
            error = numberError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        RegTextField(form.accountName, actions.onAccountName, R.string.reg_account_name, R.string.reg_account_name_placeholder, errors[RegField.AccountName])
    }
}

/** Callbacks of REG-06. */
class DocumentsActions(
    val onCamera: (DocType) -> Unit = {},
    val onGallery: (DocType) -> Unit = {},
    val onFile: (DocType) -> Unit = {},
    val onRemove: (DocType) -> Unit = {},
    val onRetry: (DocType) -> Unit = {},
    val onContinue: () -> Unit = {},
    val onBack: () -> Unit = {},
)

/**
 * Spec: REG-06 Dokumen Upload (PRD v3 C2 step 5 and document table). The list follows Jenis Badan Usaha and Status PIC.
 * Legal documents: camera, gallery or file (JPG/PNG/PDF up to 5 MB) through the "Metode Upload Dokumen" sheet; store
 * photos: camera only (up to 2 MB). "Lanjut Ringkasan" is enabled once every mandatory document is uploaded.
 *
 * @param only In "Perbaiki Pengajuan", the flagged documents only.
 */
@Composable
fun RegDocumentsScreen(state: PartnerRegistrationState, actions: DocumentsActions, only: List<DocType>? = null) {
    var methodFor by remember { mutableStateOf<DocType?>(null) }
    val list = only?.map { it to true } ?: state.documentList
    RegStepScaffold(
        step = RegStep.Documents,
        title = stringResource(R.string.reg_documents_title),
        description = stringResource(R.string.reg_documents_description),
        onBack = actions.onBack,
        cta = stringResource(R.string.reg_documents_cta),
        onCta = actions.onContinue,
        ctaEnabled = PartnerRegistrationValidator.documentsComplete(list, state.documents),
    ) {
        list.forEach { (document, mandatory) ->
            DocumentField(document, mandatory, state.documents[document], state.fileErrors[document],
                onUpload = { if (document.store) actions.onCamera(document) else methodFor = document },
                onRemove = { actions.onRemove(document) },
                onRetry = { actions.onRetry(document) },
            )
        }
    }
    methodFor?.let { document ->
        UploadMethodSheet(
            onDismiss = { methodFor = null },
            onCamera = { actions.onCamera(document) },
            onGallery = { actions.onGallery(document) },
            onFile = { actions.onFile(document) },
        )
    }
}

/** One document: Upload Files card with its rule, the uploaded file row, or the refusal message. */
@Composable
fun DocumentField(
    document: DocType,
    mandatory: Boolean,
    upload: DocumentUpload?,
    fileError: FileError?,
    onUpload: () -> Unit,
    onRemove: () -> Unit,
    onRetry: () -> Unit,
) {
    val label = stringResource(document.label) + if (mandatory) "" else " (${stringResource(R.string.reg_optional)})"
    AmarUploadFiles(
        label = label,
        buttonLabel = stringResource(if (document.store) R.string.reg_doc_take_photo else R.string.reg_doc_choose),
        onUploadClick = onUpload,
        modifier = Modifier.fillMaxWidth(),
        buttonIcon = painterResource(if (document.store) R.drawable.ds_ic_camera_24 else R.drawable.ds_ic_upload_document_24),
        supportingLabel = stringResource(if (document.store) R.string.reg_doc_hint_photo else R.string.reg_doc_hint_legal),
        files = upload?.let { { RegUploadRow(it, onRemove, onRetry) } },
    )
    fileError?.let {
        val message = when (it) {
            FileError.TooLarge -> stringResource(R.string.reg_err_file_size, formatFileSize(document.maxBytes))
            FileError.WrongType -> stringResource(R.string.reg_err_file_type)
        }
        AmarInfoBox(message, Modifier.fillMaxWidth(), type = AmarInfoBoxType.Error)
    }
}

/** "Metode Upload Dokumen" (Figma): camera, gallery, and file (PDF, PRD C2). */
@Composable
fun UploadMethodSheet(onDismiss: () -> Unit, onCamera: () -> Unit, onGallery: () -> Unit, onFile: () -> Unit) {
    AmarBottomSheet(
        onDismissRequest = onDismiss,
        type = AmarBottomSheetType.Compact,
        title = stringResource(R.string.reg_upload_method_title),
        showHandle = false,
    ) {
        val dismiss = amarBottomSheetDismiss()
        AmarSheetSelection(
            stringResource(R.string.reg_upload_camera),
            onClick = { dismiss(); onCamera() },
            description = stringResource(R.string.reg_upload_camera_description),
            icon = painterResource(R.drawable.ds_ic_camera_24),
        )
        AmarDivider()
        AmarSheetSelection(
            stringResource(R.string.reg_upload_gallery),
            onClick = { dismiss(); onGallery() },
            description = stringResource(R.string.reg_upload_gallery_description),
            icon = painterResource(R.drawable.ds_ic_photo_24),
        )
        AmarDivider()
        AmarSheetSelection(
            stringResource(R.string.reg_upload_file),
            onClick = { dismiss(); onFile() },
            description = stringResource(R.string.reg_upload_file_description),
            icon = painterResource(R.drawable.ds_ic_files_24),
        )
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
