package com.example.and_sales_portal.designsystem.components.extended

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarNavigationBarTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.designsystem.theme.amarShadow

/** One drawable layer of a navigation icon; [inverse] layers are tinted `Icon/Inverse` (white) when active. */
data class AmarNavIconLayer(@DrawableRes val res: Int, val inverse: Boolean = false)

/**
 * A navigation item: [icon] is drawn in `Icon/Disabled` when inactive; [activeIcon] layers in `Action/Focus`
 * (or `Icon/Inverse` for inverse layers) when active. Icons without a filled pair reuse the outline as active icon.
 */
data class AmarNavItem(
    val label: String,
    @DrawableRes val icon: Int,
    val activeIcon: List<AmarNavIconLayer> = listOf(AmarNavIconLayer(icon)),
    /** Inactive layers for multi-colour outline icons (inverse layers stay white); default: [icon] alone. */
    val inactiveIcon: List<AmarNavIconLayer> = listOf(AmarNavIconLayer(icon)),
)

/**
 * Bottom navigation with any number of items (extension, not in the Figma DS).
 *
 * Based on [Navigation Bar - Partner Dashboard 3306:2306](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=3306-2306),
 * which has exactly three fixed items. The sales app needs five per role (PRD v3: TL Beranda, Partner, Tim, Aktivitas,
 * Akun; SA/SR Beranda, Kunjungan, Partner, Kinerja, Akun), so this keeps the same tokens (height 56, top indicator
 * `Action/Focus`, `Label/Button/SM`, `Shadow/Overlay`) with a free item list.
 */
@Composable
fun AmarBottomNavigation(
    items: List<AmarNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.navigationBars,
) {
    val c = AmarTheme.colors
    val t = AmarNavigationBarTokens
    Row(
        modifier
            .fillMaxWidth()
            .amarShadow(AmarTheme.elevation.overlay, AmarTheme.shapes.none)
            .background(c.surface.base)
            .windowInsetsPadding(windowInsets)
            .height(t.Height),
    ) {
        items.forEachIndexed { index, item ->
            val active = index == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .selectable(selected = active, role = Role.Tab) { onSelect(index) },
            ) {
                if (active) Box(Modifier.fillMaxWidth().height(t.IndicatorHeight).background(c.action.focus))
                Column(
                    Modifier.fillMaxSize().padding(top = t.ItemPaddingTop, bottom = t.ItemPaddingBottom),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(t.ItemGap, Alignment.CenterVertically),
                ) {
                    Box(Modifier.size(AmarTheme.iconSizes.lg)) {
                        if (active) {
                            item.activeIcon.forEach { layer ->
                                Icon(painterResource(layer.res), null, Modifier.size(AmarTheme.iconSizes.lg), tint = if (layer.inverse) c.icon.inverse else c.action.focus)
                            }
                        } else {
                            item.inactiveIcon.forEach { layer ->
                                Icon(painterResource(layer.res), null, Modifier.size(AmarTheme.iconSizes.lg), tint = if (layer.inverse) c.icon.inverse else c.icon.disabled)
                            }
                        }
                    }
                    Text(
                        item.label,
                        style = AmarTheme.typography.labelButtonSm,
                        color = if (active) c.content.link else c.content.disabled,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Five items, each one selected once. */
@Preview(name = "Bottom navigation (5 items)", widthDp = 360)
@Composable
private fun AmarBottomNavigationPreview() {
    AmarTheme {
        val items = listOf(
            AmarNavItem("Beranda", R.drawable.ds_ic_menu_home_24, listOf(AmarNavIconLayer(R.drawable.ds_ic_menu_home_filled_24_l0), AmarNavIconLayer(R.drawable.ds_ic_menu_home_filled_24_l1, inverse = true))),
            AmarNavItem("Partner", R.drawable.ds_ic_menu_store_24),
            AmarNavItem("Tim", R.drawable.ds_ic_contact_24),
            AmarNavItem("Aktivitas", R.drawable.ds_ic_todo_line_24, listOf(AmarNavIconLayer(R.drawable.ds_ic_todo_fill_24))),
            AmarNavItem("Akun", R.drawable.ds_ic_menu_profile_24, listOf(AmarNavIconLayer(R.drawable.ds_ic_menu_profile_filled_24))),
        )
        Column {
            items.indices.forEach { AmarBottomNavigation(items, it, {}, windowInsets = WindowInsets(0)) }
        }
    }
}
