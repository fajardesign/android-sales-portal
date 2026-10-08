package com.example.and_sales_portal.designsystem.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.theme.AmarCardTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.designsystem.theme.amarShadow

/** Figma variant property `Style`. `Selected` exists only for `Type=Custom`. */
enum class AmarCardStyle { Default, Border, Shadow, Selected }

/**
 * Card container shared by both `Type`s: background, border, shadow, padding and click.
 * Clicks have no ripple, as agreed for Button (Figma defines no pressed state for Card).
 */
@Composable
private fun CardContainer(
    style: AmarCardStyle,
    onClick: (() -> Unit)?,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = AmarTheme.colors
    val shape = AmarTheme.shapes.container
    var m = modifier
    if (style == AmarCardStyle.Shadow) m = m.amarShadow(AmarTheme.elevation.floating, shape)
    m = m.background(if (style == AmarCardStyle.Selected) c.background.brandSubtle else c.surface.base, shape)
    when (style) {
        AmarCardStyle.Border -> m = m.border(AmarTheme.borderWidths.subtle, c.border.strong, shape)
        AmarCardStyle.Selected -> m = m.border(AmarTheme.borderWidths.subtle, c.action.focus, shape).semantics { selected = true }
        else -> Unit
    }
    if (onClick != null) {
        m = m.clickable(interactionSource = null, indication = null, role = Role.Button, onClick = onClick)
    }
    Column(
        m.padding(AmarCardTokens.Padding),
        verticalArrangement = Arrangement.spacedBy(AmarCardTokens.Gap),
        content = content,
    )
}

/**
 * Card, `Type=Default`: heading (icon, label, description, chevron), optional slot and a full-width button.
 *
 * Figma: [Card 954:2473](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=954-2473)
 * (Type × Style = 7 variants; `Type=Default, Style=Selected` does not exist and is rejected).
 * The card does not lock its width (328 in Figma); give it one through [modifier].
 *
 * @param label Heading label (`Heading/SM`, `Content/Primary`).
 * @param style Figma `Style`: Default, Border or Shadow.
 * @param description Figma `Description` + `Edit Description` (`Label/MD`, `Content/Secondary`); `null` hides it.
 * @param icon Figma `Show Icon` + `Icon` slot (32 in Figma); `null` hides it.
 * @param showAction Figma `Show Action`: chevron right at the end of the heading.
 * @param slot Figma `Show Slot` + `Slot`, between the heading and the button; `null` hides it.
 * @param buttonLabel Figma `Button`: Primary Medium button, full width; shown when set with [onButtonClick].
 * @param onClick Makes the whole card clickable (not a Figma property).
 */
@Composable
fun AmarCard(
    label: String,
    modifier: Modifier = Modifier,
    style: AmarCardStyle = AmarCardStyle.Default,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    showAction: Boolean = false,
    slot: (@Composable ColumnScope.() -> Unit)? = null,
    buttonLabel: String? = null,
    onButtonClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    require(style != AmarCardStyle.Selected) { "Card Type=Default has no Style=Selected in Figma" }
    val c = AmarTheme.colors
    CardContainer(style, onClick, modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AmarCardTokens.Gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(AmarCardTokens.HeadingGap)) {
                icon?.invoke()
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AmarCardTokens.TextGap)) {
                    Text(label, style = AmarTheme.typography.headingSm, color = c.content.primary)
                    description?.let { Text(it, style = AmarTheme.typography.labelMd, color = c.content.secondary) }
                }
            }
            if (showAction) {
                Icon(
                    painterResource(R.drawable.ds_ic_chevron_right_24),
                    contentDescription = null,
                    tint = c.icon.primary,
                    modifier = Modifier.size(AmarTheme.iconSizes.lg),
                )
            }
        }
        slot?.let { Column(Modifier.fillMaxWidth(), content = it) }
        if (buttonLabel != null && onButtonClick != null) {
            AmarButton(
                label = buttonLabel,
                onClick = onButtonClick,
                size = AmarButtonSize.Medium,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Card, `Type=Custom`: the container only, with free content (Figma `Slot`).
 *
 * Figma: [Card 954:2473](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=954-2473).
 * Children are stacked with Spacing/Gap/LG (16).
 *
 * @param style Figma `Style`, including Selected (`Background/Brand Subtle` + `Action/Focus` border).
 * @param onClick Makes the whole card clickable (not a Figma property).
 * @param content Figma `Slot`.
 */
@Composable
fun AmarCard(
    modifier: Modifier = Modifier,
    style: AmarCardStyle = AmarCardStyle.Default,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    CardContainer(style, onClick, modifier, content)
}

private class CardStyleProvider : PreviewParameterProvider<AmarCardStyle> {
    override val values = AmarCardStyle.entries.asSequence()
}

/** Placeholder for the 32 icon slot and the Custom slot (dashed frames in Figma). */
@Composable
private fun PreviewPlaceholder(modifier: Modifier) {
    Box(modifier.background(AmarTheme.colors.background.subtle, AmarTheme.shapes.card))
}

/** One preview per `Style`: Type=Default (not for Selected) with all options on, then Type=Custom. All 7 variants. */
@Preview(name = "Card", widthDp = 360)
@Composable
private fun AmarCardPreview(@PreviewParameter(CardStyleProvider::class) style: AmarCardStyle) {
    AmarTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(AmarTheme.colors.background.canvas)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S16),
        ) {
            if (style != AmarCardStyle.Selected) {
                AmarCard(
                    label = "Label",
                    style = style,
                    description = "Insert the description here.",
                    icon = { PreviewPlaceholder(Modifier.size(AmarSpace.S32)) },
                    showAction = true,
                    buttonLabel = "Button",
                    onButtonClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            AmarCard(style = style, modifier = Modifier.fillMaxWidth()) {
                PreviewPlaceholder(Modifier.fillMaxWidth().padding(AmarSpace.S16))
            }
        }
    }
}
