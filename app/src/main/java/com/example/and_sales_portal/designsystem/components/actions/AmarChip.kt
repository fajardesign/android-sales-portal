package com.example.and_sales_portal.designsystem.components.actions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarChipTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/**
 * Figma variant property `Supporting`. `Disabled` is not here: it is [AmarChip]'s `enabled = false`.
 * `Delete` exists only with `Selected=False`.
 */
enum class AmarChipSupporting { Default, Close, Counter, Dropdown, Delete }

private class ChipColors(val container: Color, val border: Color?, val content: Color)

@Composable
private fun chipColors(supporting: AmarChipSupporting, selected: Boolean): ChipColors {
    val c = AmarTheme.colors
    return when {
        supporting == AmarChipSupporting.Delete ->
            // Component/Text Field/Border/Error for border, label and icons.
            ChipColors(c.background.base, c.feedback.error.border, c.feedback.error.border)
        // Figma: `Component/Chip/Selected Text` → Content/Link (Primary 700) as the fill; label `Content/Inverse`.
        selected -> ChipColors(c.content.link, null, c.content.inverse)
        // Border `Component/Chip/Selected Background` → Action/Ghost/Hover (#E6E8EA, same primitive as Background/Subtle).
        else -> ChipColors(c.background.base, c.background.subtle, c.content.secondary)
    }
}

/**
 * Chip: a pill-shaped choice with an optional leading icon and a trailing element per `Supporting`.
 *
 * Figma: [Chip 3241:2467](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3241-2467)
 * (Supporting × Selected = 11 variants; `Delete + Selected=True` does not exist and is rejected).
 *
 * Height 32 (padding 6 + 20), at least 80 wide, radius full. The visual height is below the 48dp touch target
 * "by design" (Figma description); Compose extends the hit area of the clickable chip to the minimum touch target
 * without changing its layout size, so rows keep their 8 spacing.
 *
 * @param label Figma `Label` (`Label/MD Semibold`).
 * @param selected Figma `Selected`.
 * @param onClick Toggles / picks the chip.
 * @param supporting Figma `Supporting`.
 * @param enabled `false` = Figma `Supporting=Disabled` (whole chip at `Component/Chip/Disabled Opacity` 50 %).
 * @param icon Figma `Show Icon` + `Icon Slot` (20, tinted like the label); `null` hides it. Not shown for Counter,
 * whose variant has no icon slot.
 * @param counterValue Figma `Counter Value` (Counter only).
 * @param onTrailingClick Click on the close icon (Close, Delete); `null` = the icon is part of the chip's click.
 */
@Composable
fun AmarChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: AmarChipSupporting = AmarChipSupporting.Default,
    enabled: Boolean = true,
    icon: Painter? = null,
    counterValue: String = "0",
    onTrailingClick: (() -> Unit)? = null,
) {
    require(!(supporting == AmarChipSupporting.Delete && selected)) { "Chip Supporting=Delete has no Selected=True in Figma" }
    val colors = chipColors(supporting, selected)
    val shape = AmarTheme.shapes.full
    val t = AmarChipTokens
    val gap = if (supporting == AmarChipSupporting.Counter || supporting == AmarChipSupporting.Dropdown) t.DropdownGap else t.Gap
    var m = modifier
        .alpha(if (enabled) 1f else t.DisabledOpacity)
        .defaultMinSize(minWidth = t.MinWidth)
        .background(colors.container, shape)
    colors.border?.let { m = m.border(AmarTheme.borderWidths.subtle, it, shape) }
    Row(
        m
            .selectable(selected = selected, interactionSource = null, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = t.PaddingX, vertical = t.PaddingY),
        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val leading: @Composable () -> Unit = {
            if (icon != null && supporting != AmarChipSupporting.Counter) {
                Icon(icon, null, Modifier.size(AmarTheme.iconSizes.md), tint = colors.content)
            }
            Text(label, color = colors.content, style = AmarTheme.typography.labelMdSemibold, maxLines = 1)
        }
        if (supporting == AmarChipSupporting.Dropdown) {
            // Figma `Content` frame: icon ↔ label at `Component/Chip/Gap`, then the chevron at `Dropdown Gap`.
            Row(horizontalArrangement = Arrangement.spacedBy(t.Gap), verticalAlignment = Alignment.CenterVertically) { leading() }
        } else {
            leading()
        }
        when (supporting) {
            AmarChipSupporting.Close, AmarChipSupporting.Delete -> {
                val closeAlpha = if (supporting == AmarChipSupporting.Close && !selected) t.CloseOpacityUnselected else 1f
                val closeLabel = stringResource(R.string.ds_alert_dismiss)
                Icon(
                    painterResource(R.drawable.ds_chip_close_16),
                    contentDescription = if (onTrailingClick != null) closeLabel else null,
                    tint = colors.content.copy(alpha = colors.content.alpha * closeAlpha),
                    modifier = Modifier
                        .size(AmarTheme.iconSizes.sm)
                        .then(
                            if (onTrailingClick != null && enabled) {
                                Modifier.clickable(role = Role.Button, onClickLabel = closeLabel, onClick = onTrailingClick)
                            } else {
                                Modifier
                            },
                        ),
                )
            }
            AmarChipSupporting.Dropdown ->
                Icon(painterResource(R.drawable.ds_chip_chevron_down_16), null, Modifier.size(AmarTheme.iconSizes.sm), tint = colors.content)
            AmarChipSupporting.Counter -> ChipCounter(counterValue, selected)
            AmarChipSupporting.Default -> Unit
        }
    }
}

/**
 * Counter circle 20 with `Label/SM`. Unselected: `Component/Chip/Counter Background` (Background/Subtle) with the number
 * in Primary 700 (Figma renders Primary 600, which fails text contrast; CLAUDE.md rule 6). Selected: white circle with the
 * number "cut out" (Figma: Exclude), drawn in the chip's fill colour.
 */
@Composable
private fun ChipCounter(value: String, selected: Boolean) {
    val c = AmarTheme.colors
    Box(
        Modifier
            .size(AmarChipTokens.CounterSize)
            .background(if (selected) c.background.base else c.background.subtle, AmarTheme.shapes.full),
        contentAlignment = Alignment.Center,
    ) {
        Text(value, color = c.content.link, style = AmarTheme.typography.labelSm, maxLines = 1)
    }
}

/** All 11 Figma variants: each `Supporting` unselected and selected (Delete unselected only), plus Disabled. */
@OptIn(ExperimentalLayoutApi::class)
@Preview(name = "Chip", widthDp = 360)
@Composable
private fun AmarChipPreview() {
    AmarTheme {
        Column(
            Modifier
                .background(AmarTheme.colors.background.base)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S12),
        ) {
            val icon = painterResource(R.drawable.ds_ic_default_20)
            AmarChipSupporting.entries.forEach { supporting ->
                FlowRow(horizontalArrangement = Arrangement.spacedBy(AmarSpace.S8)) {
                    listOf(false, true).forEach { selected ->
                        if (!(supporting == AmarChipSupporting.Delete && selected)) {
                            AmarChip("Chip Label", selected, {}, supporting = supporting, icon = icon)
                        }
                    }
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AmarSpace.S8)) {
                AmarChip("Chip Label", false, {}, enabled = false, icon = icon)
                AmarChip("Chip Label", true, {}, enabled = false, icon = icon)
            }
        }
    }
}
