package com.example.and_sales_portal.designsystem.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.designsystem.theme.AmarToggleTokens
import com.example.and_sales_portal.designsystem.theme.amarShadow

/** Figma variant property `Size`. */
enum class AmarToggleSize { Small, Medium }

/**
 * Toggle (switch).
 *
 * Figma: [Element / Toggle 671:2227](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=671-2227)
 * (State × Size × Active = 8 variants).
 *
 * The knob moves without animation (Figma defines no motion).
 *
 * @param checked Figma `Active`.
 * @param onCheckedChange `null` makes the toggle read-only.
 * @param size Figma `Size`.
 * @param enabled `false` = Figma `State=Disabled` (whole toggle at 50% opacity).
 */
@Composable
fun AmarToggle(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    size: AmarToggleSize = AmarToggleSize.Medium,
    enabled: Boolean = true,
) {
    val tokens = when (size) {
        AmarToggleSize.Medium -> AmarToggleTokens.Medium
        AmarToggleSize.Small -> AmarToggleTokens.Small
    }
    val colors = AmarTheme.colors
    // Component/Toggle/Background/(Disabled) Active|Inactive: disabled tokens resolve to the same colours.
    val track = if (checked) colors.action.primary.background else colors.background.subtle
    val toggleModifier = if (onCheckedChange != null) {
        Modifier.toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .then(toggleModifier)
            .alpha(if (enabled) 1f else AmarToggleTokens.DisabledOpacity)
            .size(tokens.width, tokens.height)
            .background(track, AmarTheme.shapes.full)
            .padding(AmarToggleTokens.Padding),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .size(tokens.knob)
                .amarShadow(AmarTheme.elevation.floating, AmarTheme.shapes.full)
                .background(colors.surface.base, AmarTheme.shapes.full),
        )
    }
}

/** All 8 Figma variants. */
@Preview(name = "Toggle")
@Composable
private fun AmarToggleVariantsPreview() {
    AmarTheme {
        Column(
            modifier = Modifier
                .background(AmarTheme.colors.background.base)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S16),
        ) {
            listOf(true, false).forEach { enabled ->
                Row(horizontalArrangement = Arrangement.spacedBy(AmarSpace.S16)) {
                    AmarToggleSize.entries.forEach { size ->
                        AmarToggle(checked = true, onCheckedChange = {}, size = size, enabled = enabled)
                        AmarToggle(checked = false, onCheckedChange = {}, size = size, enabled = enabled)
                    }
                }
            }
        }
    }
}
