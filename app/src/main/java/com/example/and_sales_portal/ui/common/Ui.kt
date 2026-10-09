package com.example.and_sales_portal.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBar
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBarType
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarFormTokens
import com.example.and_sales_portal.designsystem.theme.AmarSuccessTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.designsystem.theme.amarShadow
import java.util.Locale

/**
 * Screen frame of the sales app: App Bar (Primary on a tab without back, Secondary with back on inner screens, as in
 * REG and Akun), scrollable content on `Background/Canvas` and an optional persistent action.
 */
@Composable
fun ScreenScaffold(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    background: Color = AmarTheme.colors.background.canvas,
    action: (@Composable () -> Unit)? = null,
    /** Shows the notification icon of the Primary app bar (SA/SR Beranda). */
    onNotifications: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize().background(background)) {
        AmarAppBar(
            title = title,
            type = if (onBack == null) AmarAppBarType.Primary else AmarAppBarType.Secondary,
            onNavigationClick = onBack,
            onActionClick = onNotifications,
            actionContentDescription = onNotifications?.let { stringResource(R.string.notif_title) },
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AmarFormTokens.PaddingHorizontal, vertical = AmarFormTokens.PaddingVertical),
            verticalArrangement = Arrangement.spacedBy(AmarFormTokens.FieldGap),
            content = content,
        )
        action?.invoke()
    }
}

/** Figma `Persistent action` (same as REG): full-width Primary Large button on `Background/Base` with `Shadow/Bottom`. */
@Composable
fun PersistentAction(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
    type: AmarButtonType = AmarButtonType.Primary,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .amarShadow(AmarTheme.elevation.bottom, RectangleShape)
            .background(AmarTheme.colors.background.base)
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            .padding(AmarFormTokens.ActionPadding),
    ) {
        AmarButton(label, onClick, Modifier.fillMaxWidth(), type = type, size = AmarButtonSize.Large, enabled = enabled, loading = loading)
    }
}

/** `Form introduction` of REG: "LANGKAH n DARI m" (when [step] is set), title and description. */
@Composable
fun StepIntro(step: Int?, total: Int, title: String, description: String) {
    val c = AmarTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AmarFormTokens.IntroGap)) {
        if (step != null) Text(stringResource(R.string.cap_step, step, total), color = c.content.link, style = AmarTheme.typography.labelSm)
        Text(title, color = c.content.primary, style = AmarTheme.typography.headingSm)
        Text(description, color = c.content.secondary, style = AmarTheme.typography.bodySm)
    }
}

/** Label on the left, value on the right (Akun, visit detail, summaries). */
@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    val c = AmarTheme.colors
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AmarSuccessTokens.RowGap), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = c.content.secondary, style = AmarTheme.typography.bodySm)
        Text(value, Modifier.weight(1f), c.content.primary, style = AmarTheme.typography.bodyMdMedium, textAlign = TextAlign.End)
    }
}

/** Section title above a group of cards. */
@Composable
fun SectionTitle(text: String) {
    Text(text, color = AmarTheme.colors.content.primary, style = AmarTheme.typography.headingSm)
}

/** Icon drawn as stacked layers (`_l0` primary, `_l1` inverse), as in the DS catalog. */
@Composable
fun LayeredIcon(@DrawableRes base: Int, @DrawableRes top: Int, tint: Color = AmarTheme.colors.icon.primary) {
    val size = AmarTheme.iconSizes.lg
    Box {
        Icon(painterResource(base), null, Modifier.size(size), tint = tint)
        Icon(painterResource(top), null, Modifier.size(size), tint = AmarTheme.colors.icon.inverse)
    }
}

/**
 * Map preview: no map SDK in the prototype (decision 2026-10-07), so a placeholder with the pin, the coordinates and
 * "Buka di Maps" (opens the maps app).
 */
@Composable
fun MapPreview(lat: Double, lng: Double, onOpenMaps: () -> Unit, modifier: Modifier = Modifier) {
    val c = AmarTheme.colors
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.SequenceGap)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(AmarActivityTokens.PreviewHeight)
                .clip(RoundedCornerShape(AmarActivityTokens.TileRadius))
                .background(c.background.brandSubtle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ds_ic_location_24), stringResource(R.string.cap_map), Modifier.size(AmarTheme.iconSizes.lg), tint = c.icon.brand)
        }
        InfoRow(stringResource(R.string.cap_coordinates), coordinates(lat, lng))
        AmarButton(stringResource(R.string.cap_open_maps), onOpenMaps, type = AmarButtonType.Naked, size = AmarButtonSize.Small, leftIcon = painterResource(R.drawable.ds_ic_share_24))
    }
}

fun coordinates(lat: Double, lng: Double) = String.format(Locale.US, "%.6f, %.6f", lat, lng)
