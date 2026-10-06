package com.example.and_sales_portal.designsystem.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonGroup
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonGroupLayout
import com.example.and_sales_portal.designsystem.theme.AmarModalTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.designsystem.theme.amarShadow

/**
 * Illustration area at the top of a Modal.
 *
 * Figma: [Element / Illustration Modal 486:1867](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=486-1867)
 * (Fullscreen = False / True).
 *
 * @param fullscreen Figma `Fullscreen`: the image spans the modal width without padding. Otherwise an 80dp
 * image is centred with 24dp top and 8dp bottom padding.
 * @param image Figma `Image` slot.
 */
@Composable
fun AmarModalIllustration(
    modifier: Modifier = Modifier,
    fullscreen: Boolean = false,
    image: @Composable () -> Unit,
) {
    if (fullscreen) {
        Box(modifier.fillMaxWidth()) { image() }
    } else {
        Box(
            modifier
                .fillMaxWidth()
                .padding(
                    start = AmarModalTokens.Padding,
                    end = AmarModalTokens.Padding,
                    top = AmarModalTokens.IllustrationPaddingTop,
                    bottom = AmarModalTokens.IllustrationPaddingBottom,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(AmarModalTokens.IllustrationSize), contentAlignment = Alignment.Center) { image() }
        }
    }
}

/**
 * Modal (dialog card) with title, description, optional slot and a side-by-side Button Group.
 * Place it in a `Dialog` or above an [AmarOverlay].
 *
 * Figma: [Modal / Default 1034:656](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=1034-656)
 * (Scroll State = Default / Scroll - Top / Scroll - Bottom).
 *
 * Figma `Scroll State` is derived from the content: when it does not fit, the content scrolls and the buttons
 * move to a footer with a `Shadow/Modal` while more content is below (Scroll - Top); once scrolled, a 40dp fade
 * covers the top edge (Scroll - Bottom). Figma blurs that edge (background blur); Compose cannot blur what lies
 * behind a layer, so only the white fade (`Background/Base` at 90%) is drawn.
 *
 * @param title Figma `Show Title` + `TItle` (`Heading/MD`); `null` hides it.
 * @param description Figma `Show Description` + `Description` (`Body/LG`); `null` hides it.
 * @param illustration Figma `Show Illustration`: usually an [AmarModalIllustration].
 * @param slot Figma `Show Slot` + `Slot`: extra content below the text.
 * @param secondLabel Secondary button of the Button Group; `null` hides it.
 */
@Composable
fun AmarModal(
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    secondLabel: String? = null,
    onSecondClick: () -> Unit = {},
    illustration: (@Composable () -> Unit)? = null,
    slot: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val c = AmarTheme.colors
    val shape = AmarTheme.shapes.container
    val scroll = rememberScrollState()
    val scrollable = scroll.maxValue > 0
    Column(
        modifier = modifier
            .width(AmarModalTokens.Width)
            .amarShadow(AmarTheme.elevation.modal, shape)
            .clip(shape)
            .background(c.background.base),
    ) {
        Box(Modifier.weight(1f, fill = false)) {
            Column(Modifier.verticalScroll(scroll)) {
                illustration?.invoke()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = AmarModalTokens.Padding,
                            end = AmarModalTokens.Padding,
                            top = AmarModalTokens.Padding,
                            bottom = if (scrollable) AmarModalTokens.Padding else AmarModalTokens.GapAction,
                        ),
                    verticalArrangement = Arrangement.spacedBy(AmarModalTokens.GapSection),
                ) {
                    if (title != null || description != null) {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AmarModalTokens.TextGap)) {
                            title?.let {
                                Text(it, Modifier.fillMaxWidth(), c.content.primary, style = AmarTheme.typography.headingMd, textAlign = TextAlign.Center)
                            }
                            description?.let {
                                Text(it, Modifier.fillMaxWidth(), c.content.secondary, style = AmarTheme.typography.bodyLg, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    slot?.let { Column(Modifier.fillMaxWidth(), content = it) }
                }
            }
            if (scroll.value > 0) {
                // Scroll - Bottom: top edge fade (Figma's background blur is not reproducible in Compose).
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(AmarModalTokens.ScrollEdgeHeight)
                        .background(
                            Brush.verticalGradient(
                                listOf(c.background.base.copy(alpha = AmarModalTokens.ScrollEdgeOpacity), c.background.transparent),
                            ),
                        ),
                )
            }
        }
        AmarButtonGroup(
            primaryLabel = primaryLabel,
            onPrimaryClick = onPrimaryClick,
            layout = AmarButtonGroupLayout.Side,
            secondLabel = secondLabel,
            onSecondClick = onSecondClick,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    // Scroll - Top: footer lifted with Shadow/Modal while more content is below.
                    if (scroll.canScrollForward) Modifier.amarShadow(AmarTheme.elevation.modal, AmarTheme.shapes.none) else Modifier,
                )
                .background(c.background.base)
                .padding(
                    start = AmarModalTokens.Padding,
                    end = AmarModalTokens.Padding,
                    bottom = AmarModalTokens.Padding,
                    top = if (scrollable) AmarModalTokens.Padding else AmarSpace.S0,
                ),
        )
    }
}

/** Scroll State = Default, and the scrollable states with long content in a limited height. */
@Preview(name = "Modal", widthDp = 360, heightDp = 640)
@Composable
private fun AmarModalPreview() {
    AmarTheme {
        Column(
            Modifier
                .background(AmarTheme.colors.background.canvas)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S24),
        ) {
            AmarModal("Button", {}, title = "Modal Title", description = "Description text goes here.", secondLabel = "Button")
            AmarModal(
                "Button", {}, Modifier.heightIn(max = AmarSpace.S128 + AmarSpace.S96 + AmarSpace.S40),
                title = "Modal Title",
                description = "Description text goes here. ".repeat(12),
                secondLabel = "Button",
            )
        }
    }
}
