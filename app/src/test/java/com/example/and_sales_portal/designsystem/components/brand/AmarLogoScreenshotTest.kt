package com.example.and_sales_portal.designsystem.components.brand

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Renders the 6 Logo / Amar Bank variants at 1x in their natural size, on `Surface/Base` white (as the Figma export). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarLogoScreenshotTest {

    @Test
    fun allVariants() {
        listOf(true, false).forEach { color ->
            AmarLogoType.entries.forEach { type ->
                captureRoboImage("build/outputs/roborazzi/logo_${type.name}_${if (color) "Color" else "White"}.png") {
                    AmarTheme { Box(Modifier.background(AmarTheme.colors.surface.base)) { AmarLogo(type = type, color = color) } }
                }
            }
        }
    }
}
