package com.example.and_sales_portal.designsystem.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
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
 * Renders the 4 Bottom Sheet variants (360 wide) and the Option / Selection sub-elements (328 wide)
 * at 1x on white, with the Figma defaults and sample copy.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-mdpi")
class AmarBottomSheetScreenshotTest {

    /** Width of the sheet sub-elements in Figma (sample size, not a token). */
    private val FigmaElementWidth = 328.dp

    private val title = "Bottom Sheet Title"
    private val description =
        "Provide a concise message that helps users understand the purpose of this dialog and the action they can take."

    private fun capture(name: String, content: @Composable () -> Unit) {
        captureRoboImage("build/outputs/roborazzi/bottom_sheet_$name.png") {
            AmarTheme { Box(Modifier.background(AmarTheme.colors.background.base)) { content() } }
        }
    }

    @Test
    fun sheetVariants() {
        capture("Default") {
            AmarBottomSheetContent(title = title, description = description, onClose = {}, primaryLabel = "Button", windowInsets = WindowInsets(0))
        }
        capture("Default_Illustration") {
            AmarBottomSheetContent(
                title = title, description = description, onClose = {}, primaryLabel = "Button",
                illustration = { Box(Modifier.size(AmarSpace.S80)) }, windowInsets = WindowInsets(0),
            )
        }
        capture("Compact") {
            AmarBottomSheetContent(
                type = AmarBottomSheetType.Compact, title = title, description = description, onClose = {},
                primaryLabel = "Button", windowInsets = WindowInsets(0),
            )
        }
        capture("Compact_Scroll") {
            AmarBottomSheetContent(
                type = AmarBottomSheetType.Compact, scroll = true, title = title, description = description, onClose = {},
                primaryLabel = "Button", windowInsets = WindowInsets(0),
            )
        }
    }

    @Test
    fun sheetElements() {
        listOf(false, true).forEach { selected ->
            capture("Option_$selected") {
                AmarSheetOption("Option Label", selected = selected, onClick = {}, modifier = Modifier.width(FigmaElementWidth))
            }
        }
        capture("Selection") {
            AmarSheetSelection("Title", onClick = {}, description = "Description", modifier = Modifier.width(FigmaElementWidth))
        }
    }
}
