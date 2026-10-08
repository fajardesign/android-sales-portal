package com.example.and_sales_portal.designsystem.components.navigation

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

/** Tab (360 wide) with and without scrim at 1x, for comparison with Figma 1825:10993 / 1833:13849. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarTabScreenshotTest {
    @Test
    fun tabs() {
        listOf(false, true).forEach { scrim ->
            captureRoboImage("build/outputs/roborazzi/nav_tab_scrim_$scrim.png") {
                AmarTheme {
                    AmarTab(Modifier.width(360.dp)) {
                        AmarTabItem("Placeholder", selected = true, onClick = {}, scrim = scrim)
                        AmarTabItem("Placeholder", selected = false, onClick = {}, scrim = scrim)
                    }
                }
            }
        }
    }
}
