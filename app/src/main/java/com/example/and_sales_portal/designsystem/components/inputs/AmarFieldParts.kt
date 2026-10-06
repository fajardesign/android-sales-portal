package com.example.and_sales_portal.designsystem.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTextFieldTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/**
 * `Component/Text Field/...` colours, resolved to semantic tokens.
 * Shared by Input, Dropdown, Area and Code Verification.
 */
internal class FieldColors(
    val background: Color,
    val border: Color,
    val text: Color,
    val placeholder: Color,
    val icon: Color,
    val iconStrong: Color,
    val cursor: Color,
)

/**
 * Field colours per Figma state. Notes from the Figma variants:
 * - `Disabled - Filled` keeps `Text/Input` for the value at 70% layer opacity; the empty placeholder uses
 *   `Text/Disabled` at 60%. Prefixes ("Rp", "+62") follow the value colour.
 * - `Error` only changes the border, also when disabled.
 */
@Composable
internal fun fieldColors(enabled: Boolean, focused: Boolean, error: Boolean): FieldColors {
    val c = AmarTheme.colors
    return FieldColors(
        background = if (enabled) c.surface.base else c.background.subtle,
        border = when {
            error -> c.feedback.error.border
            !enabled -> c.border.disabled
            focused -> c.border.focus
            else -> c.border.strong
        },
        text = if (enabled) c.content.primary else c.content.primary.copy(alpha = AmarTextFieldTokens.DisabledValueOpacity),
        placeholder = if (enabled) {
            c.content.quaternary
        } else {
            c.content.disabled.copy(alpha = AmarTextFieldTokens.DisabledPlaceholderOpacity)
        },
        icon = if (enabled) c.icon.tertiary else c.icon.disabled,
        iconStrong = c.icon.primary,
        cursor = c.border.focus,
    )
}

/** Field border width: 1.5 when focused (`Border/Width Focus`), otherwise 1 (`Border/Width`). */
@Composable
internal fun fieldBorderWidth(focused: Boolean): Dp =
    if (focused) AmarTextFieldTokens.BorderWidthFocus else AmarTheme.borderWidths.subtle

/** Background, border and padding of the Figma `Field` frame. */
@Composable
internal fun Modifier.fieldFrame(colors: FieldColors, focused: Boolean): Modifier {
    val shape = RoundedCornerShape(AmarTextFieldTokens.Radius)
    return this
        .background(colors.background, shape)
        .border(fieldBorderWidth(focused), colors.border, shape)
        .padding(
            start = AmarTextFieldTokens.PaddingLeft,
            end = AmarTextFieldTokens.PaddingRight,
            top = AmarTextFieldTokens.PaddingVertical,
            bottom = AmarTextFieldTokens.PaddingVertical,
        )
}

/** Label above the field, the field itself, and the hint below it (`Layout/Gap` between them). */
@Composable
internal fun FieldLayout(
    modifier: Modifier,
    label: String?,
    required: Boolean,
    hint: String?,
    error: Boolean,
    field: @Composable () -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AmarTextFieldTokens.Gap)) {
        // Every Input / Dropdown / Area instance overrides the label style to Label/Button/MD.
        label?.let { AmarInputLabel(it, required = required, style = AmarTheme.typography.labelButtonMd) }
        field()
        hint?.let { AmarInputHint(it, error = error) }
    }
}

/**
 * Field label.
 *
 * Figma: [Input / Label 200:539](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=200-539).
 *
 * @param required Figma `Required`: red asterisk (`Label/Error`).
 * @param subLabel Figma `Sub Label` + `Sub Label Text`, e.g. "(Jika ada)"; `null` hides it.
 * @param showIcon Figma `Icon`: info icon after the label.
 * @param style the master uses `Label/MD Medium`; Input, Dropdown and Area instances override it to `Label/Button/MD`.
 */
@Composable
fun AmarInputLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    subLabel: String? = null,
    showIcon: Boolean = false,
    style: TextStyle = AmarTheme.typography.labelMdMedium,
) {
    val c = AmarTheme.colors
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(AmarTextFieldTokens.LabelGap)) {
            Text(text, style = style, color = c.content.primary, modifier = Modifier.alignByBaseline())
            if (required) {
                Text(
                    stringResource(R.string.ds_input_required),
                    style = style,
                    color = c.feedback.error.border,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            subLabel?.let {
                Text(it, style = AmarTheme.typography.captionSm, color = c.content.tertiary, modifier = Modifier.alignByBaseline())
            }
        }
        if (showIcon) {
            Icon(
                painterResource(R.drawable.ds_ic_info_sm_20),
                contentDescription = null,
                tint = c.icon.primary, // master `info sm` 200:561: Icon/Primary
                modifier = Modifier.size(AmarTheme.iconSizes.md),
            )
        }
    }
}

/**
 * Helper / error text below a field.
 *
 * Figma: [Input / Hint 206:511](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=206-511).
 *
 * @param error uses `Helper/Error` instead of `Helper/Default` for text and icon.
 * @param showIcon Figma `Icon`. The Input error variants show it.
 */
@Composable
fun AmarInputHint(
    text: String,
    modifier: Modifier = Modifier,
    error: Boolean = false,
    showIcon: Boolean = error,
) {
    val color = if (error) AmarTheme.colors.feedback.error.border else AmarTheme.colors.content.tertiary
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(AmarSpace.S4)) {
        if (showIcon) {
            Icon(
                painterResource(R.drawable.ds_ic_info_2_24),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(AmarTheme.iconSizes.sm),
            )
        }
        Text(text, style = AmarTheme.typography.captionSm, color = color)
    }
}

/** Label options and hint, default and error. */
@Preview(name = "Input label & hint")
@Composable
private fun AmarFieldPartsPreview() {
    AmarTheme {
        Column(
            Modifier
                .background(AmarTheme.colors.background.base)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S12),
        ) {
            AmarInputLabel("Change Label")
            AmarInputLabel("Change Label", required = true, subLabel = "(Jika ada)", showIcon = true)
            AmarInputHint("This is a hint text to help user.")
            AmarInputHint("This is a hint text to help user.", error = true)
        }
    }
}
