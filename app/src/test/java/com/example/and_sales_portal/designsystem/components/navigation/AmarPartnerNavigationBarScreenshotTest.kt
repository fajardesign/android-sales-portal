package com.example.and_sales_portal.designsystem.components.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
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

/** Partner navigation bar variants at 1x, at the same offset as in the Figma set frame (20, 20). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h640dp-mdpi")
class AmarPartnerNavigationBarScreenshotTest {
    @Test
    fun variants() {
        AmarPartnerDestination.entries.forEach { d ->
            captureRoboImage("build/outputs/roborazzi/nav_partner_${d.name}.png") {
                AmarTheme {
                    AmarPartnerNavigationBar(d, {}, Modifier.padding(20.dp).width(360.dp), WindowInsets(0))
                }
            }
        }
    }
}
