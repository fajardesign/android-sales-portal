package com.example.and_sales_portal.designsystem.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarInfoBoxTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/** Figma variant property `Type`. */
enum class AmarInfoBoxType { Info, Success, Warning, Error, Neutral }

private class InfoBoxColors(val surface: Color, val border: Color, val icon: Color, val action: Color)

@Composable
private fun infoBoxColors(type: AmarInfoBoxType): InfoBoxColors {
    val f = AmarTheme.colors.feedback
    return when (type) {
        AmarInfoBoxType.Info -> InfoBoxColors(f.info.surface, f.info.border, f.info.icon, f.info.text)
        AmarInfoBoxType.Success -> InfoBoxColors(f.success.surface, f.success.border, f.success.icon, f.success.text)
        AmarInfoBoxType.Warning -> InfoBoxColors(f.warning.surface, f.warning.border, f.warning.icon, f.warning.text)
        AmarInfoBoxType.Error -> InfoBoxColors(f.error.surface, f.error.border, f.error.icon, f.error.text)
        AmarInfoBoxType.Neutral -> InfoBoxColors(f.neutral.surface, f.neutral.border, f.neutral.icon, f.neutral.text)
    }
}

/**
 * Info Box: a bordered message block with optional label, text button and close.
 *
 * Figma: [Info Box 676:2342](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=676-2342)
 * (Type × Button × Show Label = 20 variants).
 *
 * @param message Figma `Message` (`Label/MD`, `Info Box/Description`).
 * @param label Figma `Show Label` + `Label` (`Label/MD Medium`); `null` hides it.
 * @param showIcon Figma `Icon`.
 * @param icon Figma `Change Icon`; `null` uses the two-tone `info` icon (type colour + white mark).
 * @param buttonLabel Figma `Button`: naked text button in the type's text colour; shown when set with [onButtonClick].
 * @param onClose Figma `Close`; the close icon is shown when set.
 */
@Composable
fun AmarInfoBox(
    message: String,
    modifier: Modifier = Modifier,
    type: AmarInfoBoxType = AmarInfoBoxType.Info,
    label: String? = null,
    showIcon: Boolean = true,
    icon: Painter? = null,
    buttonLabel: String? = null,
    onButtonClick: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
) {
    val colors = infoBoxColors(type)
    val c = AmarTheme.colors
    val hasButton = buttonLabel != null && onButtonClick != null
    val shape = AmarTheme.shapes.card
    val iconSize = AmarTheme.iconSizes.md
    Row(
        modifier = modifier
            .background(colors.surface, shape)
            .border(AmarTheme.borderWidths.subtle, colors.border, shape)
            .padding(
                start = AmarInfoBoxTokens.Padding,
                top = AmarInfoBoxTokens.Padding,
                end = AmarInfoBoxTokens.Padding,
                bottom = if (hasButton) AmarInfoBoxTokens.PaddingBottomWithButton else AmarInfoBoxTokens.Padding,
            ),
        horizontalArrangement = Arrangement.spacedBy(AmarInfoBoxTokens.Gap),
    ) {
        if (showIcon) {
            if (icon != null) {
                Icon(icon, null, Modifier.size(iconSize), tint = colors.icon)
            } else {
                Box(Modifier.size(iconSize)) {
                    Icon(painterResource(R.drawable.ds_infobox_info_circle_20), null, Modifier.size(iconSize), tint = colors.icon)
                    // Info Box/Action Icon
                    Icon(painterResource(R.drawable.ds_infobox_info_mark_20), null, Modifier.size(iconSize), tint = c.action.primary.icon)
                }
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AmarInfoBoxTokens.Gap)) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AmarInfoBoxTokens.TextGap)) {
                label?.let { Text(it, style = AmarTheme.typography.labelMdMedium, color = c.content.primary) }
                Text(message, style = AmarTheme.typography.labelMd, color = c.content.secondary)
            }
            if (buttonLabel != null && onButtonClick != null) {
                Text(
                    buttonLabel,
                    style = AmarTheme.typography.labelButtonMd,
                    color = colors.action,
                    maxLines = 1,
                    modifier = Modifier.clickable(role = Role.Button, onClick = onButtonClick),
                )
            }
        }
        onClose?.let {
            Icon(
                painterResource(R.drawable.ds_infobox_close_20),
                contentDescription = stringResource(R.string.ds_alert_dismiss),
                tint = c.icon.tertiary, // Info Box/Close Icon
                modifier = Modifier
                    .size(iconSize)
                    .clickable(role = Role.Button, onClick = it),
            )
        }
    }
}

private class InfoBoxTypeProvider : PreviewParameterProvider<AmarInfoBoxType> {
    override val values = AmarInfoBoxType.entries.asSequence()
}

/** One preview per Figma `Type` (5), each with Button × Show Label (4): all 20 variants. */
@Preview(name = "Info Box", widthDp = 360)
@Composable
private fun AmarInfoBoxPreview(@PreviewParameter(InfoBoxTypeProvider::class) type: AmarInfoBoxType) {
    AmarTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(AmarTheme.colors.background.base)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S12),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            listOf(false, true).forEach { button ->
                listOf(false, true).forEach { showLabel ->
                    AmarInfoBox(
                        message = "Insert the description here.",
                        type = type,
                        label = if (showLabel) "Label" else null,
                        buttonLabel = if (button) "Button" else null,
                        onButtonClick = if (button) ({}) else null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
