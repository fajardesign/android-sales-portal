package com.example.and_sales_portal.designsystem.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTextFieldTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/**
 * Dropdown field: shows the selected value and opens a picker (e.g. a Bottom Sheet) on tap.
 *
 * Figma: [Dropdown 640:1400](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=640-1400)
 * (State × Error = 8 variants).
 *
 * @param value selected value; `null` shows [placeholder].
 * @param expanded `true` = Figma `State=Active` (picker open): focus border.
 * @param enabled `false` = Figma `State=Disabled` / `Disabled - Filled`.
 * @param leftIcon Figma `Left Icon` + `Change Left`.
 */
@Composable
fun AmarDropdown(
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    required: Boolean = false,
    placeholder: String = "",
    hint: String? = null,
    error: Boolean = false,
    enabled: Boolean = true,
    expanded: Boolean = false,
    leftIcon: Painter? = null,
) {
    val colors = fieldColors(enabled, expanded, error)
    val textStyle = AmarTheme.typography.bodyLg
    val iconSize = AmarTheme.iconSizes.lg
    FieldLayout(modifier, label, required, hint, error) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = null,
                    indication = null, // feedback comes from [expanded] (State=Active)
                    enabled = enabled,
                    role = Role.DropdownList,
                    onClick = onClick,
                )
                .fieldFrame(colors, expanded),
            // Figma uses 4 in State=Active; kept at 8 so the content does not shift.
            horizontalArrangement = Arrangement.spacedBy(AmarTextFieldTokens.GapInput),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leftIcon?.let { Icon(it, null, Modifier.size(iconSize), tint = colors.icon) }
            Text(
                text = value ?: placeholder,
                style = textStyle,
                color = if (value == null) colors.placeholder else colors.text,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            // Figma: chevron uses Icon/Primary in every state, also when disabled.
            Icon(painterResource(R.drawable.ds_ic_chevron_down_24), null, Modifier.size(iconSize), tint = colors.iconStrong)
        }
    }
}

/** All 8 Figma variants (Default, Active, Disabled, Disabled - Filled × Error). */
@Preview(name = "Dropdown", widthDp = 360)
@Composable
private fun AmarDropdownPreview() {
    AmarTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(AmarTheme.colors.background.base)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S16),
        ) {
            listOf(false, true).forEach { error ->
                val hint = if (error) "This is a hint text to help user." else null
                AmarDropdown(null, {}, label = "Change Label", placeholder = "Placeholder text", hint = hint, error = error)
                AmarDropdown("Placeholder text", {}, label = "Change Label", hint = hint, error = error, expanded = true)
                AmarDropdown(null, {}, label = "Change Label", placeholder = "Placeholder text", hint = hint, error = error, enabled = false)
                AmarDropdown("Placeholder text", {}, label = "Change Label", hint = hint, error = error, enabled = false)
            }
        }
    }
}
