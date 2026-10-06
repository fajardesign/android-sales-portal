package com.example.and_sales_portal.designsystem.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarAlertTokens
import com.example.and_sales_portal.designsystem.theme.AmarFeedbackColors
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.designsystem.theme.amarShadow

/** Figma variant property `Style`. */
enum class AmarAlertStyle { Default, Solid, Inverse }

/** Figma variant property `Type`. */
enum class AmarAlertType { Info, Success, Warning, Error }

private data class AlertColors(
    val background: Color,
    val icon: Color,
    val message: Color,
    val action: Color,
    val close: Color,
)

@Composable
private fun alertColors(style: AmarAlertStyle, type: AmarAlertType): AlertColors {
    val colors = AmarTheme.colors
    val feedback: AmarFeedbackColors = when (type) {
        AmarAlertType.Info -> colors.feedback.info
        AmarAlertType.Success -> colors.feedback.success
        AmarAlertType.Warning -> colors.feedback.warning
        AmarAlertType.Error -> colors.feedback.error
    }
    return when (style) {
        // Component/Alert/Light/* + Close Icon/Light
        AmarAlertStyle.Default -> AlertColors(
            background = feedback.surface,
            icon = feedback.icon,
            message = feedback.text,
            action = feedback.text,
            close = colors.icon.tertiary,
        )
        // Component/Alert/Solid/* + Close Icon/Solid (→ Feedback/Info/Solid Text for every type)
        AmarAlertStyle.Solid -> AlertColors(
            background = feedback.solidBackground,
            icon = feedback.solidText,
            message = feedback.solidText,
            action = feedback.solidText,
            close = colors.feedback.info.solidText,
        )
        // Component/Alert/Inverse/* + Close Icon/Inverse; action = Button/Naked/Inverse/Text
        AmarAlertStyle.Inverse -> AlertColors(
            background = colors.background.inverse,
            icon = feedback.icon,
            message = colors.content.inverse,
            action = colors.action.nakedInverse.text,
            close = colors.content.inverse,
        )
    }
}

/**
 * Alert: a short inline message with optional icon, text action and dismiss button.
 *
 * Figma: [Alert 683:300](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=683-300)
 * (Style × Type = 12 variants).
 *
 * The alert does not set its own width. Place it with `Modifier.fillMaxWidth()` and
 * [AmarAlertTokens.MarginHorizontal], or `Modifier.width(AmarAlertTokens.Width)` as in Figma.
 *
 * @param message Figma `Message`.
 * @param style Figma `Style`.
 * @param type Figma `Type`.
 * @param showIcon Figma `Icon`.
 * @param icon Figma `Change Icon` (instance swap). Tinted with the variant's icon colour.
 * @param actionLabel Figma `Action`; the action is shown when this and [onAction] are set.
 * @param onDismiss Figma `Dismiss`; the close button is shown when this is set.
 */
@Composable
fun AmarAlert(
    message: String,
    modifier: Modifier = Modifier,
    style: AmarAlertStyle = AmarAlertStyle.Default,
    type: AmarAlertType = AmarAlertType.Info,
    showIcon: Boolean = true,
    icon: Painter = painterResource(R.drawable.ds_ic_default_20),
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    val colors = alertColors(style, type)
    val shape = RoundedCornerShape(AmarAlertTokens.Radius)
    val iconSize = AmarTheme.iconSizes.md
    Row(
        modifier = modifier
            .amarShadow(AmarTheme.elevation.floating, shape)
            .clip(shape)
            .background(colors.background)
            .padding(
                horizontal = AmarAlertTokens.PaddingHorizontal,
                vertical = AmarAlertTokens.PaddingVertical,
            ),
        horizontalArrangement = Arrangement.spacedBy(AmarAlertTokens.Gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showIcon) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = colors.icon,
                modifier = Modifier.size(iconSize),
            )
        }
        Text(
            text = message,
            style = AmarTheme.typography.labelMdMedium,
            color = colors.message,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = AmarTheme.typography.labelButtonMd,
                color = colors.action,
                maxLines = 1,
                modifier = Modifier.clickable(role = Role.Button, onClick = onAction),
            )
        }
        if (onDismiss != null) {
            Icon(
                painter = painterResource(R.drawable.ds_ic_close_20),
                contentDescription = stringResource(R.string.ds_alert_dismiss),
                tint = colors.close,
                modifier = Modifier
                    .size(iconSize)
                    .clickable(role = Role.Button, onClick = onDismiss),
            )
        }
    }
}

private class AlertVariantProvider : PreviewParameterProvider<Pair<AmarAlertStyle, AmarAlertType>> {
    override val values = AmarAlertStyle.entries.flatMap { style ->
        AmarAlertType.entries.map { type -> style to type }
    }.asSequence()
}

/** All 12 Figma variants, with every optional element on (Figma defaults). */
@Preview(name = "Alert")
@Composable
private fun AmarAlertVariantPreview(
    @PreviewParameter(AlertVariantProvider::class) variant: Pair<AmarAlertStyle, AmarAlertType>,
) {
    val (style, type) = variant
    AmarTheme {
        AmarAlert(
            message = "Alert message goes here",
            style = style,
            type = type,
            actionLabel = if (style == AmarAlertStyle.Inverse) "Button" else "Action",
            onAction = {},
            onDismiss = {},
            modifier = Modifier
                .background(AmarTheme.colors.background.canvas)
                .padding(AmarAlertTokens.MarginHorizontal)
                .width(AmarAlertTokens.Width),
        )
    }
}

/** Optional elements off, and a long message wrapping to two lines. */
@Preview(name = "Alert options")
@Composable
private fun AmarAlertOptionsPreview() {
    AmarTheme {
        Column(
            modifier = Modifier
                .background(AmarTheme.colors.background.canvas)
                .padding(AmarAlertTokens.MarginHorizontal),
            verticalArrangement = Arrangement.spacedBy(AmarAlertTokens.StackGap),
        ) {
            AmarAlert(
                message = "Alert message goes here",
                modifier = Modifier.width(AmarAlertTokens.Width),
            )
            AmarAlert(
                message = "Alert message goes here",
                showIcon = false,
                onDismiss = {},
                modifier = Modifier.width(AmarAlertTokens.Width),
            )
            AmarAlert(
                message = "Pesan yang lebih panjang akan turun ke baris berikutnya tanpa memotong tombol",
                type = AmarAlertType.Warning,
                actionLabel = "Action",
                onAction = {},
                onDismiss = {},
                modifier = Modifier.width(AmarAlertTokens.Width),
            )
        }
    }
}
