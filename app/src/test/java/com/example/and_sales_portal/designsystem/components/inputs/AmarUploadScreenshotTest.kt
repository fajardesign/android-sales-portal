package com.example.and_sales_portal.designsystem.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the 4 Element / Upload variants (296 wide) and the 4 Upload Files variants (328 wide)
 * at 1x, as in Figma, with the Figma sample copy.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarUploadScreenshotTest {

    /** Width of the component in Figma (sample size, not a token). */
    private val FigmaWidth = 296.dp

    /** Width of Upload Files in Figma (sample size, not a token). */
    private val FigmaFilesWidth = 328.dp

    /** Progress shown in the Figma Loading row (24.2 of 240). */
    private val FigmaProgress = 24.2f / 240f

    private val helper = "Kamu bisa upload foto sebanyak 3 kali"

    private fun capture(name: String, multiple: Boolean, files: (@Composable ColumnScope.() -> Unit)?) {
        captureRoboImage("build/outputs/roborazzi/upload_$name.png") {
            AmarTheme {
                Box(Modifier.background(AmarTheme.colors.background.base)) {
                    AmarUpload(
                        label = "Change Label",
                        buttonLabel = "Button",
                        onUploadClick = {},
                        multipleFiles = multiple,
                        helperText = if (multiple) helper else null,
                        files = files,
                        modifier = Modifier.width(FigmaWidth),
                    )
                }
            }
        }
    }

    private fun captureFiles(name: String, multiple: Boolean, files: (@Composable ColumnScope.() -> Unit)?) {
        captureRoboImage("build/outputs/roborazzi/upload_files_$name.png") {
            AmarTheme {
                Box(Modifier.background(AmarTheme.colors.background.base)) {
                    AmarUploadFiles(
                        label = "Change Label",
                        buttonLabel = "Button",
                        onUploadClick = {},
                        multipleFiles = multiple,
                        helperText = if (multiple) helper else null,
                        files = files,
                        modifier = Modifier.width(FigmaFilesWidth),
                    )
                }
            }
        }
    }

    @Test
    fun allVariants() {
        val loading: @Composable ColumnScope.() -> Unit = {
            AmarProgressPhotos("filename.jpg", AmarProgressPhotosStatus.Loading, fileSize = "3.2 MB", progress = FigmaProgress, onDelete = {})
        }
        capture("Single_Empty", multiple = false, files = null)
        capture("Single_Added", multiple = false, files = loading)
        capture("Multiple_Empty", multiple = true, files = null)
        capture("Multiple_Added", multiple = true) {
            loading()
            AmarProgressPhotos("filename.jpg", AmarProgressPhotosStatus.Waiting, fileSize = "3.2 MB", onDelete = {})
        }
        captureFiles("Single_Empty", multiple = false, files = null)
        captureFiles("Single_Added", multiple = false, files = loading)
        captureFiles("Multiple_Empty", multiple = true, files = null)
        captureFiles("Multiple_Added", multiple = true) {
            loading()
            AmarProgressPhotos("filename.jpg", AmarProgressPhotosStatus.Waiting, fileSize = "3.2 MB", onDelete = {})
        }
    }
}
