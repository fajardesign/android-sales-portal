package com.example.and_sales_portal.designsystem.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarSelectionSizeTokens
import com.example.and_sales_portal.designsystem.theme.AmarSelectionTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/** Figma variant property `Type`. */
enum class AmarSelectionType { Checkbox, Radio }

/** Figma variant property `Size`. */
enum class AmarSelectionSize { Small, Medium }

private fun selectionTokens(size: AmarSelectionSize): AmarSelectionSizeTokens = when (size) {
    AmarSelectionSize.Medium -> AmarSelectionTokens.Medium
    AmarSelectionSize.Small -> AmarSelectionTokens.Small
}

/**
 * Selection box: the checkbox / radio graphic only, without label or interaction.
 *
 * Figma: [Element / Icon 463:320](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=463-320)
 * (Checked × Indeterminate × Size × Disabled × Type; 24 variants exist).
 *
 * @param indeterminate Figma `Indeterminate`: shows a minus in a checked checkbox. Figma defines it only with
 * `Checked=True`; for Radio it looks the same as checked.
 * @param enabled `false` = Figma `Disabled=True` (box at 50% opacity).
 */
@Composable
fun AmarSelectionIcon(
    checked: Boolean,
    modifier: Modifier = Modifier,
    type: AmarSelectionType = AmarSelectionType.Checkbox,
    size: AmarSelectionSize = AmarSelectionSize.Medium,
    enabled: Boolean = true,
    indeterminate: Boolean = false,
) {
    val tokens = selectionTokens(size)
    val colors = AmarTheme.colors
    val shape = if (type == AmarSelectionType.Radio) AmarTheme.shapes.full else RoundedCornerShape(tokens.radius)
    val box = Modifier
        .alpha(if (enabled) 1f else AmarSelectionTokens.DisabledOpacity)
        .size(tokens.box)
    Box(
        modifier = modifier.then(
            when {
                // Component/Selection/Background
                checked -> box.background(colors.action.primary.background, shape)
                // Component/Selection/Border Color, + Background Disabled when disabled
                enabled -> box.border(AmarTheme.borderWidths.subtle, colors.action.secondary.border, shape)
                else -> box
                    .background(colors.action.primary.disabled, shape)
                    .border(AmarTheme.borderWidths.subtle, colors.action.secondary.border, shape)
            },
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            val markColor = colors.action.primary.icon // Component/Selection/Icon
            if (type == AmarSelectionType.Radio) {
                // Drawn at the exact centre so the 9.6 / 6 dot is not snapped to whole pixels.
                Box(Modifier.size(tokens.box).drawBehind { drawCircle(markColor, radius = tokens.dot.toPx() / 2f) })
            } else {
                // Per-size drawables in box coordinates: Figma keeps the stroke width (2 / 1.67) when the
                // icon is resized, and box-sized drawables avoid rounding the 3.6dp offset.
                val mark = when {
                    size == AmarSelectionSize.Medium && indeterminate -> R.drawable.ds_selection_minus_md
                    size == AmarSelectionSize.Medium -> R.drawable.ds_selection_check_md
                    indeterminate -> R.drawable.ds_selection_minus_sm
                    else -> R.drawable.ds_selection_check_sm
                }
                Icon(
                    painter = painterResource(mark),
                    contentDescription = null,
                    tint = markColor,
                    modifier = Modifier.size(tokens.box),
                )
            }
        }
    }
}

/**
 * Selection Control: checkbox or radio with optional label and description.
 *
 * Figma: [Selection Control 466:277](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=466-277)
 * (Checked × Size × Type × Disabled × Icon Only = 32 variants).
 *
 * The whole row is the touch target. Checkbox toggles; Radio selects (`onCheckedChange(true)`).
 *
 * @param checked Figma `Checked`.
 * @param type Figma `Type`.
 * @param size Figma `Size`.
 * @param enabled `false` = Figma `Disabled=True`.
 * @param label Figma `Edit Label` + `Show Label`; `null` hides it.
 * @param description Figma `Edit Description` + `Show Description`; `null` hides it.
 * Both `null` = Figma `Icon Only=True`.
 */
@Composable
fun AmarSelectionControl(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    type: AmarSelectionType = AmarSelectionType.Checkbox,
    size: AmarSelectionSize = AmarSelectionSize.Medium,
    enabled: Boolean = true,
    label: String? = null,
    description: String? = null,
) {
    val tokens = selectionTokens(size)
    val colors = AmarTheme.colors
    val iconOnly = label == null && description == null
    val interaction = when {
        onCheckedChange == null -> Modifier
        type == AmarSelectionType.Radio ->
            Modifier.selectable(selected = checked, enabled = enabled, role = Role.RadioButton) { onCheckedChange(true) }
        else -> Modifier.toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheckedChange)
    }
    if (iconOnly) {
        AmarSelectionIcon(checked, modifier.then(interaction), type, size, enabled)
        return
    }
    val labelStyle = if (size == AmarSelectionSize.Medium) AmarTheme.typography.labelLgMedium else AmarTheme.typography.labelMdMedium
    val descriptionStyle = if (size == AmarSelectionSize.Medium) AmarTheme.typography.labelLg else AmarTheme.typography.labelMd
    Row(
        modifier = modifier
            .then(interaction)
            .padding(vertical = AmarSelectionTokens.PaddingVertical),
        horizontalArrangement = Arrangement.spacedBy(tokens.gap),
        verticalAlignment = Alignment.Top,
    ) {
        AmarSelectionIcon(
            checked = checked,
            type = type,
            size = size,
            enabled = enabled,
            modifier = Modifier.padding(top = AmarSelectionTokens.BoxOffsetTop),
        )
        Column(verticalArrangement = Arrangement.spacedBy(tokens.contentGap)) {
            label?.let {
                Text(it, style = labelStyle, color = if (enabled) colors.content.primary else colors.content.disabled)
            }
            description?.let {
                Text(it, style = descriptionStyle, color = if (enabled) colors.content.secondary else colors.content.disabled)
            }
        }
    }
}

/** All 24 Figma `Element / Icon` variants. */
@Preview(name = "Selection icon")
@Composable
private fun AmarSelectionIconPreview() {
    AmarTheme {
        Column(
            modifier = Modifier
                .background(AmarTheme.colors.background.base)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S12),
        ) {
            AmarSelectionSize.entries.forEach { size ->
                listOf(true, false).forEach { enabled ->
                    Row(horizontalArrangement = Arrangement.spacedBy(AmarSpace.S12)) {
                        AmarSelectionType.entries.forEach { type ->
                            AmarSelectionIcon(checked = false, type = type, size = size, enabled = enabled)
                            AmarSelectionIcon(checked = true, type = type, size = size, enabled = enabled)
                            AmarSelectionIcon(checked = true, indeterminate = true, type = type, size = size, enabled = enabled)
                        }
                    }
                }
            }
        }
    }
}

private class SelectionVariantProvider : PreviewParameterProvider<Pair<AmarSelectionType, AmarSelectionSize>> {
    override val values = AmarSelectionType.entries.flatMap { type ->
        AmarSelectionSize.entries.map { size -> type to size }
    }.asSequence()
}

/** One preview per Type × Size (4), each with Checked × Disabled × Icon Only (8): all 32 Figma variants. */
@Preview(name = "Selection Control")
@Composable
private fun AmarSelectionControlPreview(
    @PreviewParameter(SelectionVariantProvider::class) variant: Pair<AmarSelectionType, AmarSelectionSize>,
) {
    val (type, size) = variant
    AmarTheme {
        Column(
            modifier = Modifier
                .background(AmarTheme.colors.background.base)
                .padding(horizontal = AmarSpace.S16),
        ) {
            listOf(true, false).forEach { enabled ->
                listOf(false, true).forEach { checked ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AmarSpace.S24)) {
                        AmarSelectionControl(
                            checked = checked, onCheckedChange = {}, type = type, size = size, enabled = enabled,
                            label = "Label", description = "Insert the description here.",
                        )
                        AmarSelectionControl(checked = checked, onCheckedChange = {}, type = type, size = size, enabled = enabled)
                    }
                }
            }
        }
    }
}
