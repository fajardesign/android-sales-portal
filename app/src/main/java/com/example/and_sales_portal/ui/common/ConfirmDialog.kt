package com.example.and_sales_portal.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.feedback.AmarModal

/**
 * Confirmation before an action (PRD v3: "Every status change has a confirmation modal"): DS Modal / Default in a
 * dialog, primary action plus "Batal".
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        AmarModal(
            primaryLabel = confirmLabel,
            onPrimaryClick = onConfirm,
            title = title,
            description = message,
            secondLabel = stringResource(R.string.common_cancel),
            onSecondClick = onDismiss,
        )
    }
}
