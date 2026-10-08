package com.example.and_sales_portal.designsystem.components.inputs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Toggle and Selection Control renders at 1x, laid out like the Figma frames / nodes. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarInputsScreenshotTest {

    /** Same positions as the variants inside Figma frame 671:2227 (304×100). */
    @Test
    fun toggleFrame() {
        data class Pos(val x: Int, val y: Int, val size: AmarToggleSize, val checked: Boolean, val enabled: Boolean)
        val positions = listOf(
            Pos(244, 16, AmarToggleSize.Medium, true, true), Pos(92, 16, AmarToggleSize.Small, true, true),
            Pos(244, 60, AmarToggleSize.Medium, true, false), Pos(92, 60, AmarToggleSize.Small, true, false),
            Pos(168, 16, AmarToggleSize.Medium, false, true), Pos(16, 16, AmarToggleSize.Small, false, true),
            Pos(168, 60, AmarToggleSize.Medium, false, false), Pos(16, 60, AmarToggleSize.Small, false, false),
        )
        captureRoboImage("build/outputs/roborazzi/toggle_frame.png") {
            AmarTheme {
                Box(Modifier.size(304.dp, 100.dp)) {
                    positions.forEach { p ->
                        AmarToggle(p.checked, {}, Modifier.offset(p.x.dp, p.y.dp), p.size, p.enabled)
                    }
                }
            }
        }
    }

    @Test
    fun selectionControls() {
        data class Case(val name: String, val checked: Boolean, val type: AmarSelectionType, val size: AmarSelectionSize, val enabled: Boolean)
        listOf(
            Case("md_checkbox_checked", true, AmarSelectionType.Checkbox, AmarSelectionSize.Medium, true),
            Case("md_checkbox_unchecked", false, AmarSelectionType.Checkbox, AmarSelectionSize.Medium, true),
            Case("md_radio_checked", true, AmarSelectionType.Radio, AmarSelectionSize.Medium, true),
            Case("sm_checkbox_checked", true, AmarSelectionType.Checkbox, AmarSelectionSize.Small, true),
            Case("md_checkbox_checked_disabled", true, AmarSelectionType.Checkbox, AmarSelectionSize.Medium, false),
            Case("md_checkbox_unchecked_disabled", false, AmarSelectionType.Checkbox, AmarSelectionSize.Medium, false),
        ).forEach { c ->
            captureRoboImage("build/outputs/roborazzi/selection_${c.name}.png") {
                AmarTheme {
                    AmarSelectionControl(
                        checked = c.checked, onCheckedChange = {}, type = c.type, size = c.size, enabled = c.enabled,
                        label = "Label", description = "Insert the description here.",
                    )
                }
            }
        }
    }
}
