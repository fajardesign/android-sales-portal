package com.example.and_sales_portal.designsystem.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarBadgePadding
import com.example.and_sales_portal.designsystem.theme.AmarBadgeTokens
import com.example.and_sales_portal.designsystem.theme.AmarFeedbackColors
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/** Figma variant property `Type`. */
enum class AmarBadgeType { Info, Success, Warning, Error }

/** Figma variant property `Size`. */
enum class AmarBadgeSize { Small, Medium, Large }

private class BadgeColors(
    val background: Color,
    val border: Color?,
    val text: Color,
    val leftIcon: Color,
    val close: Color,
    val counter: Color,
)

@Composable
private fun badgeColors(type: AmarBadgeType, solid: Boolean): BadgeColors {
    val f = AmarTheme.colors.feedback
    val g: AmarFeedbackColors = when (type) {
        AmarBadgeType.Info -> f.info
        AmarBadgeType.Success -> f.success
        AmarBadgeType.Warning -> f.warning
        AmarBadgeType.Error -> f.error
    }
    return if (solid) {
        // Component/Badge/<Type>/Solid/...
        BadgeColors(g.solidBackground, null, g.solidText, g.solidText, g.solidText, g.solidText)
    } else {
        // Component/Badge/<Type>/... : Icon = Icon Subtle (close), Icon Strong (left icon), Text (counter)
        BadgeColors(g.background, g.borderSubtle, g.text, g.iconStrong, g.iconSubtle, g.text)
    }
}

/**
 * Badge: a small pill label with an optional close, counter or left icon.
 *
 * Figma: [Badge 981:4166](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=981-4166)
 * (Type × Size × Supporting × Solid = 96 variants). Figma `Supporting` maps to the optional parameters:
 * Default (none), Close ([onClose]), Counter ([count]), Left Icon ([leftIcon]). Set at most one.
 *
 * @param solid Figma `Solid`: filled background, no border.
 * @param count Figma `Supporting=Counter`: number in a 16dp circle (the digits are cut out of the circle).
 * @param leftIcon Figma `Supporting=Left Icon` + `Change Icon`.
 * @param onClose Figma `Supporting=Close`.
 */
@Composable
fun AmarBadge(
    label: String,
    modifier: Modifier = Modifier,
    type: AmarBadgeType = AmarBadgeType.Info,
    size: AmarBadgeSize = AmarBadgeSize.Medium,
    solid: Boolean = false,
    count: Int? = null,
    leftIcon: Painter? = null,
    onClose: (() -> Unit)? = null,
) {
    val colors = badgeColors(type, solid)
    val tokens = when (size) {
        AmarBadgeSize.Small -> AmarBadgeTokens.Small
        AmarBadgeSize.Medium -> AmarBadgeTokens.Medium
        AmarBadgeSize.Large -> AmarBadgeTokens.Large
    }
    val padding: AmarBadgePadding = when {
        onClose != null -> tokens.close
        count != null -> tokens.counter
        leftIcon != null -> tokens.leftIcon
        else -> tokens.default
    }
    val textStyle = if (size == AmarBadgeSize.Small) AmarTheme.typography.labelButtonSm else AmarTheme.typography.labelButtonMd
    val shape = AmarTheme.shapes.full
    Row(
        modifier = modifier
            .background(colors.background, shape)
            .then(colors.border?.let { Modifier.border(AmarTheme.borderWidths.subtle, it, shape) } ?: Modifier)
            .padding(start = padding.start, top = padding.top, end = padding.end, bottom = padding.bottom),
        horizontalArrangement = Arrangement.spacedBy(padding.gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leftIcon?.let { Icon(it, null, Modifier.size(AmarBadgeTokens.LeftIconSize), tint = colors.leftIcon) }
        Text(label, style = textStyle, color = colors.text, maxLines = 1)
        count?.let {
            // Figma: Exclude(circle, digits). The cut-out digits show the badge background.
            Box(
                Modifier
                    .size(AmarBadgeTokens.CounterSize)
                    .background(colors.counter, shape),
                contentAlignment = Alignment.Center,
            ) {
                Text(it.toString(), style = AmarTheme.typography.labelSm, color = colors.background, textAlign = TextAlign.Center)
            }
        }
        onClose?.let {
            Box(
                Modifier
                    .size(AmarBadgeTokens.CloseFrame)
                    .clickable(role = Role.Button, onClick = it),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(R.drawable.ds_badge_close_12),
                    contentDescription = stringResource(R.string.ds_alert_dismiss),
                    tint = colors.close,
                    modifier = Modifier.size(AmarBadgeTokens.CloseIcon),
                )
            }
        }
    }
}

private class BadgeVariantProvider : PreviewParameterProvider<Pair<AmarBadgeType, Boolean>> {
    override val values = AmarBadgeType.entries.flatMap { listOf(it to false, it to true) }.asSequence()
}

/** One preview per Type × Solid (8), each with Size × Supporting (12): all 96 Figma variants. */
@Preview(name = "Badge")
@Composable
private fun AmarBadgePreview(@PreviewParameter(BadgeVariantProvider::class) variant: Pair<AmarBadgeType, Boolean>) {
    val (type, solid) = variant
    AmarTheme {
        val icon = painterResource(R.drawable.ds_ic_default_16)
        Column(
            Modifier
                .background(AmarTheme.colors.background.base)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S8),
        ) {
            AmarBadgeSize.entries.forEach { size ->
                Row(horizontalArrangement = Arrangement.spacedBy(AmarSpace.S8), verticalAlignment = Alignment.CenterVertically) {
                    AmarBadge("Label", type = type, size = size, solid = solid)
                    AmarBadge("Label", type = type, size = size, solid = solid, onClose = {})
                    AmarBadge("Label", type = type, size = size, solid = solid, count = 1)
                    AmarBadge("Label", type = type, size = size, solid = solid, leftIcon = icon)
                }
            }
        }
    }
}
