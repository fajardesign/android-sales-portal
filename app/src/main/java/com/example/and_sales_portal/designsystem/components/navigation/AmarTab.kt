package com.example.and_sales_portal.designsystem.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.layout.AmarDivider
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTabTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/**
 * Tab bar container: white background, 16dp side padding and a bottom divider. Put [AmarTabItem]s in [content];
 * they share the width equally.
 *
 * Figma: [Tab 1833:13848](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1833-13848)
 * (Scrim = False / True; Scrim is set per [AmarTabItem]).
 */
@Composable
fun AmarTab(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(AmarTabTokens.Height)
            .background(AmarTheme.colors.background.base),
    ) {
        // Tab/Divider/Color = Border/Color/Default, full width at the bottom, drawn behind the indicator.
        AmarDivider(Modifier.align(Alignment.BottomCenter))
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = AmarTabTokens.ContainerPaddingX),
            content = content,
        )
    }
}

/**
 * One tab.
 *
 * Figma: [Element / Tab Item 1825:10926](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1825-10926)
 * (Active × Scrim = 4 variants).
 *
 * @param selected Figma `Active`: Primary 700 label (`Label/MD Semibold`), Primary 600 icon and a 2dp indicator.
 * @param icon Figma `Icon` + `Edit Icon`; `null` hides it.
 * @param scrim Figma `Scrim`: loading placeholder (gradient pill) instead of the content.
 */
@Composable
fun RowScope.AmarTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = painterResource(R.drawable.ds_ic_default_20),
    scrim: Boolean = false,
) {
    val c = AmarTheme.colors
    Box(
        modifier
            .weight(1f)
            .fillMaxSize()
            .selectable(selected = selected, enabled = !scrim, role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (scrim) {
            Box(
                Modifier
                    .size(AmarTabTokens.ScrimWidth, AmarTabTokens.ScrimHeight)
                    .background(
                        Brush.horizontalGradient(*AmarTabTokens.ScrimStops.zip(AmarTabTokens.ScrimColors).toTypedArray()),
                        RoundedCornerShape(AmarTabTokens.ScrimRadius),
                    ),
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(AmarTabTokens.ContentGap), verticalAlignment = Alignment.CenterVertically) {
                icon?.let {
                    Icon(
                        it, null, Modifier.size(AmarTheme.iconSizes.md),
                        // Tab/Icon/Color Active = Primary 600 (icon only), Default = Icon/Tertiary
                        tint = if (selected) c.icon.brand else c.icon.tertiary,
                    )
                }
                Text(
                    label,
                    style = if (selected) AmarTheme.typography.labelMdSemibold else AmarTheme.typography.labelMdMedium,
                    color = if (selected) c.content.link else c.content.tertiary, // Primary 700 / Neutral 500
                    maxLines = 1,
                )
            }
            if (selected) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(AmarTabTokens.IndicatorHeight)
                        .background(c.border.focus), // Tab/Indicator/Color = Primary 600
                )
            }
        }
    }
}

/** Both Figma Tab variants (Scrim False / True), i.e. all 4 Tab Item variants. */
@Preview(name = "Tab", widthDp = 360)
@Composable
private fun AmarTabPreview() {
    AmarTheme {
        Column(
            Modifier.background(AmarTheme.colors.background.canvas),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S8),
        ) {
            listOf(false, true).forEach { scrim ->
                AmarTab {
                    AmarTabItem("Placeholder", selected = true, onClick = {}, scrim = scrim)
                    AmarTabItem("Placeholder", selected = false, onClick = {}, scrim = scrim)
                }
            }
        }
    }
}
