package com.example.and_sales_portal.designsystem.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarAppBarTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.designsystem.theme.amarShadow

/** Figma variant property `Type`: Primary = title left (root screens); Secondary = back + centred title. */
enum class AmarAppBarType { Primary, Secondary }

/** Figma variant property `State`. */
enum class AmarAppBarState { Default, Transparent, TransparentInverse }

private class AppBarColors(val background: Color, val title: Color, val icon: Color)

/**
 * Colours as rendered in Figma (and used on the product screens): a white bar with #000A14 content.
 * Note: in Figma the bar fill is bound to the `.../Title` variable and the title to `.../Background`;
 * the variable names are swapped, the rendered result is what is implemented here.
 */
@Composable
private fun appBarColors(type: AmarAppBarType, state: AmarAppBarState): AppBarColors {
    val c = AmarTheme.colors
    return when (state) {
        AmarAppBarState.Default -> AppBarColors(c.content.inverse, c.background.inverse, c.background.inverse)
        // Primary: `Transparent/Background Scrolled` (#000A14); Secondary: `Transparent/Title|Icon Color` (white).
        AmarAppBarState.Transparent -> if (type == AmarAppBarType.Primary) {
            AppBarColors(c.background.transparent, c.background.inverse, c.background.inverse)
        } else {
            AppBarColors(c.background.transparent, c.content.inverse, c.content.inverse)
        }
        // `Transparent Inverse/Title Color` = Neutral 900, `Icon Color` = Neutral 700.
        AmarAppBarState.TransparentInverse -> AppBarColors(c.background.transparent, c.content.primary, c.icon.secondary)
    }
}

/**
 * App Bar. The Figma frame includes a mock status bar; here the bar draws behind the system status bar
 * via [windowInsets] and only the 56dp header is laid out.
 *
 * Figma: [App Bar 133:32](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=133-32)
 * (Type × State × Stepper = 8 variants).
 *
 * Layouts: Primary = title left + action; Secondary = back + centred title + action; Secondary + Transparent
 * Inverse = back + left title + action; Primary + Transparent Inverse = title + [subtitle] + round action button.
 *
 * @param title Figma `Title` + `Change Label`; `null` hides it.
 * @param onNavigationClick Figma `Show Navigation` (Secondary): back arrow shown when set.
 * @param navigationIcon Figma `Change Navigation`.
 * @param actionIcon Figma `Right Icon` + `Change Icon`; defaults: notification (Primary), help (Secondary),
 * search in a round button (Primary + Transparent Inverse). Shown when [onActionClick] is set.
 * @param actionContentDescription What the action does, read by screen readers (no visual change).
 * @param slot Figma `Stepper=True` + `Slot`: content below the header (e.g. a stepper). Figma only defines it for
 * Secondary; the Primary + Stepper variant is identical to Stepper=False.
 */
@Composable
fun AmarAppBar(
    title: String?,
    modifier: Modifier = Modifier,
    type: AmarAppBarType = AmarAppBarType.Secondary,
    state: AmarAppBarState = AmarAppBarState.Default,
    subtitle: String? = null,
    onNavigationClick: (() -> Unit)? = null,
    navigationIcon: Painter = painterResource(R.drawable.ds_ic_arrow_left_24),
    onActionClick: (() -> Unit)? = null,
    actionIcon: Painter? = null,
    actionContentDescription: String? = null,
    windowInsets: WindowInsets = WindowInsets.statusBars,
    slot: (@Composable () -> Unit)? = null,
) {
    val colors = appBarColors(type, state)
    val iconSize = AmarTheme.iconSizes.lg
    val titleStyle = AmarTheme.typography.headingSm
    val roundAction = type == AmarAppBarType.Primary && state == AmarAppBarState.TransparentInverse
    val action = actionIcon ?: painterResource(
        when {
            roundAction -> R.drawable.ds_ic_search_2_24
            type == AmarAppBarType.Primary -> R.drawable.ds_ic_notification_24
            else -> R.drawable.ds_ic_help_24
        },
    )
    val bar = Modifier
        .fillMaxWidth()
        .then(if (state == AmarAppBarState.Default) Modifier.amarShadow(AmarTheme.elevation.floating, AmarTheme.shapes.none) else Modifier)
        .background(colors.background)
        .windowInsetsPadding(windowInsets)

    @Composable
    fun ActionIcon() {
        val click = onActionClick ?: return
        if (roundAction) {
            Box(
                Modifier
                    .background(AmarTheme.colors.background.subtle, AmarTheme.shapes.full) // Icon/Close Sheet/Background
                    .clickable(role = Role.Button, onClick = click)
                    .padding(AmarAppBarTokens.RoundButtonPadding),
            ) { Icon(action, actionContentDescription, Modifier.size(iconSize), tint = AmarTheme.colors.icon.primary) }
        } else {
            Icon(action, actionContentDescription, Modifier.size(iconSize).clickable(role = Role.Button, onClick = click), tint = colors.icon)
        }
    }

    @Composable
    fun NavigationIcon() {
        onNavigationClick?.let {
            Icon(navigationIcon, null, Modifier.size(iconSize).clickable(role = Role.Button, onClick = it), tint = colors.icon)
        }
    }

    Column(modifier.then(bar)) {
        val header = Modifier
            .fillMaxWidth()
            .heightIn(min = AmarAppBarTokens.HeaderHeight)
            .padding(AmarAppBarTokens.Padding)
        when {
            roundAction -> Row(
                header,
                horizontalArrangement = Arrangement.spacedBy(AmarAppBarTokens.GapTransparentInverse),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    title?.let { Text(it, style = titleStyle, color = colors.title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    subtitle?.let { Text(it, style = AmarTheme.typography.bodySm, color = colors.title, maxLines = 1) }
                }
                ActionIcon()
            }
            type == AmarAppBarType.Secondary && state != AmarAppBarState.TransparentInverse -> Box(header, contentAlignment = Alignment.Center) {
                Box(Modifier.align(Alignment.CenterStart)) { NavigationIcon() }
                title?.let {
                    Text(
                        it, Modifier.widthIn(max = AmarAppBarTokens.TitleMaxWidth), colors.title,
                        style = titleStyle, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(Modifier.align(Alignment.CenterEnd)) { ActionIcon() }
            }
            else -> Row(
                header,
                horizontalArrangement = Arrangement.spacedBy(AmarAppBarTokens.Gap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavigationIcon()
                Box(Modifier.weight(1f)) {
                    title?.let {
                        Text(
                            it, Modifier.widthIn(max = AmarAppBarTokens.TitleMaxWidth), colors.title,
                            style = titleStyle, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                ActionIcon()
            }
        }
        slot?.invoke()
    }
}

/** All Figma variants except the duplicate Primary + Stepper (no insets in preview). */
@Preview(name = "App Bar", widthDp = 360)
@Composable
private fun AmarAppBarPreview() {
    AmarTheme {
        Column(
            Modifier.background(AmarTheme.colors.background.canvas),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S16),
        ) {
            val none = WindowInsets(0)
            AmarAppBarType.entries.forEach { type ->
                AmarAppBarState.entries.forEach { state ->
                    Box(if (type == AmarAppBarType.Secondary && state == AmarAppBarState.Transparent) Modifier.background(AmarTheme.colors.background.inverse) else Modifier) {
                        AmarAppBar(
                            title = "Title", type = type, state = state, windowInsets = none,
                            subtitle = if (state == AmarAppBarState.TransparentInverse) "Sub" else null,
                            onNavigationClick = if (type == AmarAppBarType.Secondary) ({}) else null,
                            onActionClick = {},
                        )
                    }
                }
            }
        }
    }
}
