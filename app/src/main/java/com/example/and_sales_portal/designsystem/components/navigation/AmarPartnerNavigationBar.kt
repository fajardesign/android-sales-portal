package com.example.and_sales_portal.designsystem.components.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarNavigationBarTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.designsystem.theme.amarShadow

/** Icon layer: drawable + whether it is a brand (Action/Focus) or inverse (white) layer. */
internal class IconLayer(@DrawableRes val res: Int, val inverse: Boolean = false)

/** Figma variant property `Active`. */
enum class AmarPartnerDestination(
    @StringRes internal val label: Int,
    @DrawableRes internal val icon: Int,
    internal val filledLayers: List<IconLayer>,
) {
    Beranda(
        R.string.ds_nav_partner_beranda, R.drawable.ds_ic_menu_home_24,
        listOf(IconLayer(R.drawable.ds_ic_menu_home_filled_24_l0), IconLayer(R.drawable.ds_ic_menu_home_filled_24_l1, inverse = true)),
    ),
    Partner(
        R.string.ds_nav_partner_partner, R.drawable.ds_ic_menu_store_24,
        listOf(
            IconLayer(R.drawable.ds_ic_menu_store_filled_24_l0), IconLayer(R.drawable.ds_ic_menu_store_filled_24_l1, inverse = true),
            IconLayer(R.drawable.ds_ic_menu_store_filled_24_l2), IconLayer(R.drawable.ds_ic_menu_store_filled_24_l3, inverse = true),
            IconLayer(R.drawable.ds_ic_menu_store_filled_24_l4), IconLayer(R.drawable.ds_ic_menu_store_filled_24_l5, inverse = true),
        ),
    ),
    Tim(R.string.ds_nav_partner_tim, R.drawable.ds_ic_menu_profile_24, listOf(IconLayer(R.drawable.ds_ic_menu_profile_filled_24))),
}

/**
 * Bottom navigation of the Partner Dashboard (Beranda, Partner, Tim).
 *
 * Figma: [Navigation Bar - Partner Dashboard 3306:2306](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3306-2306)
 * (Active = Beranda / Partner / Tim).
 *
 * Decisions (2026-10-06): height 56 as in the component; the menu icons in Figma use colour variables from
 * another library, mapped to the local tokens of the same name (`Icon/Disabled`, `Icon/Inverse`); the active
 * label uses Primary 700 (`Navigation/Bottom/Title`) instead of Figma's Primary 600 (text contrast rule).
 * The bar draws behind the system navigation bar via [windowInsets].
 */
@Composable
fun AmarPartnerNavigationBar(
    selected: AmarPartnerDestination,
    onSelect: (AmarPartnerDestination) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.navigationBars,
) {
    val c = AmarTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .amarShadow(AmarTheme.elevation.overlay, AmarTheme.shapes.none)
            .background(c.surface.base) // Navigation Bar/Background
            .windowInsetsPadding(windowInsets)
            .height(AmarNavigationBarTokens.Height),
    ) {
        AmarPartnerDestination.entries.forEach { destination ->
            val active = destination == selected
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .selectable(selected = active, role = Role.Tab) { onSelect(destination) },
            ) {
                if (active) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(AmarNavigationBarTokens.IndicatorHeight)
                            .background(c.action.focus),
                    )
                }
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(top = AmarNavigationBarTokens.ItemPaddingTop, bottom = AmarNavigationBarTokens.ItemPaddingBottom),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AmarNavigationBarTokens.ItemGap, Alignment.CenterVertically),
                ) {
                    Box(Modifier.size(AmarTheme.iconSizes.lg)) {
                        if (active) {
                            destination.filledLayers.forEach { layer ->
                                LayerIcon(layer.res, if (layer.inverse) c.icon.inverse else c.action.focus)
                            }
                        } else {
                            LayerIcon(destination.icon, c.icon.disabled)
                        }
                    }
                    Text(
                        stringResource(destination.label),
                        style = if (active) AmarTheme.typography.labelButtonSm else AmarTheme.typography.captionSm,
                        color = if (active) c.content.link else c.content.disabled, // Primary 700 / Navigation Bar/Label/Inactive
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun LayerIcon(@DrawableRes res: Int, tint: Color) {
    Icon(painterResource(res), null, Modifier.size(AmarTheme.iconSizes.lg), tint = tint)
}

/** All 3 Figma variants. */
@Preview(name = "Navigation Bar - Partner Dashboard", widthDp = 360)
@Composable
private fun AmarPartnerNavigationBarPreview() {
    AmarTheme {
        Column(
            Modifier.background(AmarTheme.colors.background.canvas),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S24),
        ) {
            AmarPartnerDestination.entries.forEach {
                AmarPartnerNavigationBar(selected = it, onSelect = {}, windowInsets = WindowInsets(0))
            }
        }
    }
}
