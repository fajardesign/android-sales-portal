package com.example.and_sales_portal.designsystem.components.inputs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.feedback.AmarSpinner
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.designsystem.theme.AmarUploadTokens

/** Figma variant property `Status` of `Element / Progress Photos`. */
enum class AmarProgressPhotosStatus { Uploaded, UploadedDefault, Waiting, Loading, Failed }

/**
 * Element / Progress Photos: one file row in an upload field (thumbnail or file icon, name, progress, actions).
 *
 * Figma: [Element / Progress Photos 1052:6555](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1052-6555)
 * (Status = 5 variants). Fills the width of its parent (296 in Figma).
 *
 * - Uploaded: [thumbnail] 56, file name as link, delete.
 * - UploadedDefault: file icon, file name as link, delete (used for documents).
 * - Waiting / Loading: camera icon (disabled), file name, progress bar, [fileSize] and status with spinner.
 *   Loading fills the bar to [progress] and its action cancels; Waiting has an empty bar and its action deletes.
 * - Failed: camera icon (disabled), file name and [errorText] in error colour, a naked retry button, delete.
 *
 * @param fileName Figma `Edit Name`; one line, ellipsis at the end.
 * @param status Figma `Status`.
 * @param thumbnail Image for Uploaded (cropped to fill); `null` shows an empty `Background/Subtle` box.
 * @param fileSize Figma `File Size`, already formatted (Waiting, Loading).
 * @param progress Upload progress 0..1 (Loading).
 * @param statusText Status next to the spinner; defaults to the Figma copy for Waiting / Loading.
 * @param errorText Hint for Failed; defaults to the Figma copy.
 * @param retryLabel Retry button label for Failed; the button is shown when [onRetry] is set.
 * @param onDelete Delete action (cancel while Loading); the icon is shown when set.
 * @param onFileClick Makes the file name clickable in Uploaded / UploadedDefault (not a Figma property).
 */
@Composable
fun AmarProgressPhotos(
    fileName: String,
    status: AmarProgressPhotosStatus,
    modifier: Modifier = Modifier,
    thumbnail: Painter? = null,
    fileSize: String? = null,
    progress: Float = 0f,
    statusText: String = when (status) {
        AmarProgressPhotosStatus.Waiting -> stringResource(R.string.ds_upload_status_waiting)
        else -> stringResource(R.string.ds_upload_status_loading)
    },
    errorText: String = stringResource(R.string.ds_upload_failed),
    retryLabel: String = stringResource(R.string.ds_upload_retry),
    onRetry: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onFileClick: (() -> Unit)? = null,
) {
    when (status) {
        AmarProgressPhotosStatus.Uploaded, AmarProgressPhotosStatus.UploadedDefault -> UploadedRow(
            fileName, status, thumbnail, onDelete, onFileClick, modifier,
        )
        AmarProgressPhotosStatus.Waiting, AmarProgressPhotosStatus.Loading -> ProgressRow(
            fileName, status, fileSize, progress, statusText, onDelete, modifier,
        )
        AmarProgressPhotosStatus.Failed -> FailedRow(fileName, errorText, retryLabel, onRetry, onDelete, modifier)
    }
}

@Composable
private fun UploadedRow(
    fileName: String,
    status: AmarProgressPhotosStatus,
    thumbnail: Painter?,
    onDelete: (() -> Unit)?,
    onFileClick: (() -> Unit)?,
    modifier: Modifier,
) {
    val c = AmarTheme.colors
    Row(
        modifier.fillMaxWidth().padding(vertical = AmarUploadTokens.PaddingVertical),
        horizontalArrangement = Arrangement.spacedBy(AmarUploadTokens.Gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (status == AmarProgressPhotosStatus.Uploaded) {
            val shape = RoundedCornerShape(AmarUploadTokens.ThumbnailRadius)
            val thumbModifier = Modifier.size(AmarUploadTokens.ThumbnailSize).clip(shape)
            if (thumbnail != null) {
                Image(thumbnail, contentDescription = null, modifier = thumbModifier, contentScale = ContentScale.Crop)
            } else {
                Box(thumbModifier.background(c.background.subtle))
            }
        } else {
            UploadIcon(R.drawable.ds_ic_files_24, c.icon.primary)
        }
        Row(Modifier.weight(1f)) {
            Text(
                fileName,
                style = AmarTheme.typography.labelButtonSm,
                color = c.content.link,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (onFileClick != null) Modifier.clickable(role = Role.Button, onClick = onFileClick) else Modifier,
            )
        }
        onDelete?.let { DeleteAction(it) }
    }
}

@Composable
private fun ProgressRow(
    fileName: String,
    status: AmarProgressPhotosStatus,
    fileSize: String?,
    progress: Float,
    statusText: String,
    onDelete: (() -> Unit)?,
    modifier: Modifier,
) {
    val c = AmarTheme.colors
    val t = AmarTheme.typography
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AmarUploadTokens.Gap)) {
        UploadIcon(R.drawable.ds_ic_camera_24, c.icon.disabled)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AmarUploadTokens.ContentGap)) {
            Text(fileName, style = t.labelSm, color = c.content.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Column(verticalArrangement = Arrangement.spacedBy(AmarUploadTokens.ProgressGap)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(AmarUploadTokens.ProgressHeight)
                        .clip(AmarTheme.shapes.full)
                        .background(c.background.subtle),
                ) {
                    if (status == AmarProgressPhotosStatus.Loading) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .clip(AmarTheme.shapes.full)
                                .background(c.feedback.success.solidBackground),
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AmarUploadTokens.Gap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    fileSize?.let { Text(it, style = t.labelSm, color = c.content.secondary) }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AmarUploadTokens.ContentGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AmarSpinner(color = c.icon.tertiary, size = AmarUploadTokens.SpinnerSize)
                        Text(statusText, style = t.labelSm, color = c.content.tertiary)
                    }
                }
            }
        }
        if (onDelete != null) {
            if (status == AmarProgressPhotosStatus.Loading) CancelAction(onDelete) else DeleteAction(onDelete)
        }
    }
}

@Composable
private fun FailedRow(
    fileName: String,
    errorText: String,
    retryLabel: String,
    onRetry: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    modifier: Modifier,
) {
    val c = AmarTheme.colors
    Row(
        modifier.fillMaxWidth().padding(vertical = AmarUploadTokens.PaddingVertical),
        horizontalArrangement = Arrangement.spacedBy(AmarUploadTokens.Gap),
    ) {
        UploadIcon(R.drawable.ds_ic_camera_24, c.icon.disabled)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AmarUploadTokens.Gap)) {
            Column(verticalArrangement = Arrangement.spacedBy(AmarUploadTokens.ContentGap)) {
                // Component/Text Field/Border/Error, as in Figma.
                Text(
                    fileName,
                    style = AmarTheme.typography.labelSm,
                    color = c.feedback.error.border,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AmarInputHint(errorText, error = true, showIcon = true)
            }
            onRetry?.let {
                AmarButton(label = retryLabel, onClick = it, type = AmarButtonType.Naked, size = AmarButtonSize.Small)
            }
        }
        onDelete?.let { DeleteAction(it) }
    }
}

@Composable
private fun UploadIcon(res: Int, tint: Color) {
    Icon(painterResource(res), contentDescription = null, tint = tint, modifier = Modifier.size(AmarTheme.iconSizes.md))
}

/** `Trash` 20, `Icon/Tertiary`, centred vertically in the row (Figma `Action` frame). */
@Composable
private fun RowScope.DeleteAction(onClick: () -> Unit) {
    Icon(
        painterResource(R.drawable.ds_ic_trash_24),
        contentDescription = stringResource(R.string.ds_upload_delete),
        tint = AmarTheme.colors.icon.tertiary,
        modifier = Modifier
            .align(Alignment.CenterVertically)
            .size(AmarTheme.iconSizes.md)
            .clickable(role = Role.Button, onClick = onClick),
    )
}

/** `delete, error, disabled` 20: circle `Icon/Tertiary`, cross `Action/Primary/Icon`. */
@Composable
private fun RowScope.CancelAction(onClick: () -> Unit) {
    val size = AmarTheme.iconSizes.md
    val label = stringResource(R.string.ds_upload_cancel)
    Box(
        Modifier
            .align(Alignment.CenterVertically)
            .size(size)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
    ) {
        Icon(painterResource(R.drawable.ds_ic_error_24_l0), label, Modifier.size(size), tint = AmarTheme.colors.icon.tertiary)
        Icon(painterResource(R.drawable.ds_ic_error_24_l1), null, Modifier.size(size), tint = AmarTheme.colors.action.primary.icon)
    }
}

/** All 5 Figma `Status` variants with the Figma sample copy, 296 wide (360 − 2 × 32). */
@Preview(name = "Progress Photos", widthDp = 360)
@Composable
private fun AmarProgressPhotosPreview() {
    AmarTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(AmarTheme.colors.background.base)
                .padding(horizontal = AmarSpace.S32, vertical = AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S16),
        ) {
            AmarProgressPhotosStatus.entries.forEach { status ->
                AmarProgressPhotos(
                    fileName = "filename.jpg",
                    status = status,
                    fileSize = "3.2 MB",
                    progress = 0.1f,
                    onRetry = {},
                    onDelete = {},
                )
            }
        }
    }
}
