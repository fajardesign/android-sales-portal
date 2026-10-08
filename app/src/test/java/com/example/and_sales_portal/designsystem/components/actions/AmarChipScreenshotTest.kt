package com.example.and_sales_portal.designsystem.components.actions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Renders the 11 Chip variants at 1x with the Figma defaults (default icon, "Chip Label", counter "0"). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarChipScreenshotTest {

    private fun capture(name: String, supporting: AmarChipSupporting, selected: Boolean, enabled: Boolean = true) {
        captureRoboImage("build/outputs/roborazzi/chip_${name}_$selected.png") {
            AmarTheme {
                Box(Modifier.background(AmarTheme.colors.background.base)) {
                    AmarChip(
                        "Chip Label", selected, {}, supporting = supporting, enabled = enabled,
                        icon = painterResource(R.drawable.ds_ic_default_20),
                    )
                }
            }
        }
    }

    @Test
    fun allVariants() {
        AmarChipSupporting.entries.forEach { supporting ->
            listOf(false, true).forEach { selected ->
                if (!(supporting == AmarChipSupporting.Delete && selected)) capture(supporting.name, supporting, selected)
            }
        }
        listOf(false, true).forEach { capture("Disabled", AmarChipSupporting.Default, it, enabled = false) }
    }
}
