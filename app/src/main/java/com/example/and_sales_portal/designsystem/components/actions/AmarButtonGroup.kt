package com.example.and_sales_portal.designsystem.components.actions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.designsystem.theme.AmarButtonGroupTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/** Figma variant property `State`. */
enum class AmarButtonGroupLayout { Stack, Side }

/**
 * Button Group: one Primary button plus up to two Secondary buttons, all Large and without icons.
 *
 * Figma: [Elements / Button Group 455:432](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=455-432)
 * (State = Stack / Side, booleans `2nd Button`, `3rd Button`).
 *
 * - [AmarButtonGroupLayout.Stack]: vertical, full width, top to bottom: 2nd, 3rd, Primary.
 * - [AmarButtonGroupLayout.Side]: horizontal, equal widths: 2nd, Primary. The 3rd button does not exist in this layout.
 *
 * @param primaryLabel label of the Primary button (always shown).
 * @param secondLabel Figma `2nd Button`; `null` hides it.
 * @param thirdLabel Figma `3rd Button` (Stack only); `null` hides it.
 */
@Composable
fun AmarButtonGroup(
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    layout: AmarButtonGroupLayout = AmarButtonGroupLayout.Stack,
    secondLabel: String? = null,
    onSecondClick: () -> Unit = {},
    thirdLabel: String? = null,
    onThirdClick: () -> Unit = {},
) {
    when (layout) {
        AmarButtonGroupLayout.Stack -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(AmarButtonGroupTokens.Gap),
        ) {
            val full = Modifier.fillMaxWidth()
            secondLabel?.let { AmarButton(it, onSecondClick, full, type = AmarButtonType.Secondary) }
            thirdLabel?.let { AmarButton(it, onThirdClick, full, type = AmarButtonType.Secondary) }
            AmarButton(primaryLabel, onPrimaryClick, full, type = AmarButtonType.Primary)
        }
        AmarButtonGroupLayout.Side -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(AmarButtonGroupTokens.Gap),
        ) {
            secondLabel?.let {
                AmarButton(it, onSecondClick, Modifier.weight(1f), type = AmarButtonType.Secondary)
            }
            AmarButton(primaryLabel, onPrimaryClick, Modifier.weight(1f), type = AmarButtonType.Primary)
        }
    }
}

/** Both Figma variants with every boolean combination. */
@Preview(name = "Button Group", widthDp = 360)
@Composable
private fun AmarButtonGroupPreview() {
    AmarTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AmarTheme.colors.background.base)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S32),
        ) {
            AmarButtonGroup("Button", {}, secondLabel = "Button", thirdLabel = "Button")
            AmarButtonGroup("Button", {}, secondLabel = "Button")
            AmarButtonGroup("Button", {})
            AmarButtonGroup("Button", {}, layout = AmarButtonGroupLayout.Side, secondLabel = "Button")
            AmarButtonGroup("Button", {}, layout = AmarButtonGroupLayout.Side)
        }
    }
}
