package com.example.and_sales_portal.designsystem.components.actions

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Renders both Button Group variants at the Figma sample widths (Stack 140, Side 296), 1x. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarButtonGroupScreenshotTest {

    @Test
    fun bothLayouts() {
        captureRoboImage("build/outputs/roborazzi/buttongroup_stack.png") {
            AmarTheme {
                AmarButtonGroup(
                    "Button", {}, Modifier.width(140.dp),
                    secondLabel = "Button", thirdLabel = "Button",
                )
            }
        }
        captureRoboImage("build/outputs/roborazzi/buttongroup_side.png") {
            AmarTheme {
                AmarButtonGroup(
                    "Button", {}, Modifier.width(296.dp),
                    layout = AmarButtonGroupLayout.Side, secondLabel = "Button",
                )
            }
        }
    }
}
