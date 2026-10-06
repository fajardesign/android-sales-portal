package com.example.and_sales_portal.designsystem.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the 7 Card variants at 1x, 328 wide as in Figma, on a canvas with 16 margin
 * so the shadow is visible. Default type: Figma defaults (empty 32 icon slot, label, description, button, no action).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarCardScreenshotTest {

    /** Width of the Card component in Figma (sample size, not a token). */
    private val FigmaWidth = 328.dp
    /** Height of the empty Custom slot in Figma. */
    private val FigmaSlotHeight = 34.dp

    @Test
    fun allVariants() {
        AmarCardStyle.entries.forEach { style ->
            if (style != AmarCardStyle.Selected) {
                captureRoboImage("build/outputs/roborazzi/card_Default_${style.name}.png") {
                    AmarTheme {
                        Box(Modifier.background(AmarTheme.colors.background.base).padding(AmarSpace.S16)) {
                            AmarCard(
                                label = "Label",
                                style = style,
                                description = "Insert the description here.",
                                icon = { Box(Modifier.size(AmarSpace.S32)) },
                                buttonLabel = "Button",
                                onButtonClick = {},
                                modifier = Modifier.width(FigmaWidth),
                            )
                        }
                    }
                }
            }
            captureRoboImage("build/outputs/roborazzi/card_Custom_${style.name}.png") {
                AmarTheme {
                    Box(Modifier.background(AmarTheme.colors.background.base).padding(AmarSpace.S16)) {
                        AmarCard(style = style, modifier = Modifier.width(FigmaWidth)) {
                            Box(Modifier.fillMaxWidth().height(FigmaSlotHeight))
                        }
                    }
                }
            }
        }
    }
}
