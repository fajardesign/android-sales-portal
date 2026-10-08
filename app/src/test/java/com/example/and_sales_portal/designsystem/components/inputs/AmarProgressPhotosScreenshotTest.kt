package com.example.and_sales_portal.designsystem.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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

/** Renders the 5 Progress Photos variants at 1x, 296 wide as in Figma, with the Figma sample copy. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarProgressPhotosScreenshotTest {

    /** Width of the component in Figma (sample size, not a token). */
    private val FigmaWidth = 296.dp

    /** Progress shown in the Figma Loading variant (24.2 of 240). */
    private val FigmaProgress = 24.2f / 240f

    @Test
    fun allVariants() {
        AmarProgressPhotosStatus.entries.forEach { status ->
            captureRoboImage("build/outputs/roborazzi/progress_photos_${status.name}.png") {
                AmarTheme {
                    Box(Modifier.background(AmarTheme.colors.background.base)) {
                        AmarProgressPhotos(
                            fileName = "filename.jpg",
                            status = status,
                            fileSize = "3.2 MB",
                            progress = FigmaProgress,
                            onRetry = {},
                            onDelete = {},
                            modifier = Modifier.width(FigmaWidth),
                        )
                    }
                }
            }
        }
    }
}
