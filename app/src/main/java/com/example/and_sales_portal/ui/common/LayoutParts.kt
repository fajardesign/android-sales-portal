package com.example.and_sales_portal.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.extended.AmarProgressBar
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/**
 * Layout pieces of the UI refresh (2026-10-17), built only from DS tokens and components; they are screen layout, not new
 * DS components (see docs/design-system/components.md › "Tata letak layar").
 */
data class QuickAction(@DrawableRes val icon: Int, val label: String, val onClick: () -> Unit)

/** Beranda shortcuts as a row of tiles: icon in a brand-subtle circle and a label under it (like a banking "menu cepat"). */
@Composable
fun QuickActions(items: List<QuickAction>, modifier: Modifier = Modifier) {
    val c = AmarTheme.colors
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.QuickActionGap)) {
        items.forEach { a ->
            Column(
                Modifier.weight(1f).clickable(onClick = a.onClick),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.QuickActionGap),
            ) {
                Box(Modifier.size(AmarActivityTokens.QuickActionBox).background(c.background.brandSubtle, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(painterResource(a.icon), null, Modifier.size(AmarTheme.iconSizes.lg), tint = c.icon.brand)
                }
                Text(a.label, color = c.content.primary, style = AmarTheme.typography.captionSm, textAlign = TextAlign.Center, maxLines = 2)
            }
        }
    }
}

/** A tappable row: title, optional subtitle, optional trailing content (a badge), and a chevron when it opens something. */
@Composable
fun ListRow(title: String, subtitle: String? = null, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    val c = AmarTheme.colors
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(vertical = AmarActivityTokens.ListRowPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
            Text(title, color = c.content.primary, style = AmarTheme.typography.labelMdSemibold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            subtitle?.let { Text(it, color = c.content.secondary, style = AmarTheme.typography.bodySm, maxLines = 2, overflow = TextOverflow.Ellipsis) }
        }
        trailing?.invoke()
        if (onClick != null) Icon(painterResource(R.drawable.ds_ic_chevron_right_24), null, Modifier.size(AmarTheme.iconSizes.md), tint = c.icon.secondary)
    }
}

/** "1 dari 2 toko dikunjungi" over a progress bar. */
@Composable
fun ProgressSummary(text: String, done: Int, total: Int, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileTextGap)) {
        Text(text, color = AmarTheme.colors.content.primary, style = AmarTheme.typography.bodyMd)
        AmarProgressBar(if (total == 0) 0f else done.toFloat() / total)
    }
}
