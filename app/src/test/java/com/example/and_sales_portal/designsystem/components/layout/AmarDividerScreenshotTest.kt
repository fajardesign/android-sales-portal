package com.example.and_sales_portal.designsystem.components.layout

import androidx.compose.foundation.layout.height
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

/** Renders the 6 Divider variants at 1x with Figma node sizes (328×1 horizontal, 1×80 vertical). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarDividerScreenshotTest {

    /** Width of the horizontal variants in Figma (sample size, not a token). */
    private val FigmaHorizontalWidth = 328.dp

    @Test
    fun allVariants() {
        AmarDividerOrientation.entries.forEach { orientation ->
            AmarDividerWeight.entries.forEach { weight ->
                captureRoboImage("build/outputs/roborazzi/divider_${orientation.name}_${weight.name}.png") {
                    AmarTheme {
                        AmarDivider(
                            orientation = orientation,
                            weight = weight,
                            modifier = if (orientation == AmarDividerOrientation.Horizontal) {
                                Modifier.width(FigmaHorizontalWidth)
                            } else {
                                Modifier.height(AmarSpace.S80)
                            },
                        )
                    }
                }
            }
        }
    }
}
