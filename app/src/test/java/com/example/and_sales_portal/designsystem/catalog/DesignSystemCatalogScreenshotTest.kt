package com.example.and_sales_portal.designsystem.catalog

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Smoke test: every catalog section composes and renders (one image per section). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h4000dp-mdpi")
class DesignSystemCatalogScreenshotTest {
    @Test
    fun sections() {
        CatalogSection.entries.forEach { section ->
            captureRoboImage("build/outputs/roborazzi/catalog_${section.name.lowercase()}.png") {
                // Inspection mode keeps spinners static; infinite animations never let the capture go idle.
                CompositionLocalProvider(LocalInspectionMode provides true) {
                    AmarTheme { CatalogSectionContent(section) }
                }
            }
        }
    }
}
