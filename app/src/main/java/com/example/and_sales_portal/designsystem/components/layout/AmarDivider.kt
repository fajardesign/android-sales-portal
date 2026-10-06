package com.example.and_sales_portal.designsystem.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/** Figma variant property `Orientation`. */
enum class AmarDividerOrientation { Horizontal, Vertical }

/** Figma variant property `Weight`: the line colour (`Border/Color/…`), not its thickness. */
enum class AmarDividerWeight { Subtle, Default, Strong }

/**
 * Divider: a 1dp line that fills the width (horizontal) or height (vertical) of its parent.
 *
 * Figma: [Divider 1123:142](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1123-142)
 * (Orientation × Weight = 6 variants).
 *
 * @param orientation Figma `Orientation`.
 * @param weight Figma `Weight`.
 */
@Composable
fun AmarDivider(
    modifier: Modifier = Modifier,
    orientation: AmarDividerOrientation = AmarDividerOrientation.Horizontal,
    weight: AmarDividerWeight = AmarDividerWeight.Default,
) {
    val border = AmarTheme.colors.border
    val color: Color = when (weight) {
        AmarDividerWeight.Subtle -> border.subtle
        AmarDividerWeight.Default -> border.default
        AmarDividerWeight.Strong -> border.strong
    }
    val thickness = AmarTheme.borderWidths.subtle
    val size = when (orientation) {
        AmarDividerOrientation.Horizontal -> Modifier.fillMaxWidth().height(thickness)
        AmarDividerOrientation.Vertical -> Modifier.fillMaxHeight().width(thickness)
    }
    Box(modifier.then(size).background(color, AmarTheme.shapes.full))
}

/** All 6 Figma variants: horizontal 328 wide (360 − 2 × 16) and vertical 80 tall, as in Figma. */
@Preview(name = "Divider", widthDp = 360)
@Composable
private fun AmarDividerPreview() {
    AmarTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AmarTheme.colors.background.base)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S16),
        ) {
            AmarDividerWeight.entries.forEach { weight ->
                AmarDivider(weight = weight)
            }
            Row(
                modifier = Modifier.height(AmarSpace.S80),
                horizontalArrangement = Arrangement.spacedBy(AmarSpace.S16),
            ) {
                AmarDividerWeight.entries.forEach { weight ->
                    AmarDivider(orientation = AmarDividerOrientation.Vertical, weight = weight)
                }
            }
        }
    }
}
