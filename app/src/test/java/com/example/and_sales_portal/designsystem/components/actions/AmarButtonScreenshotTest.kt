package com.example.and_sales_portal.designsystem.components.actions

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders representative Button variants at 1x for comparison with Figma node screenshots.
 * Inspection mode keeps the spinner on its first (static) frame, like Figma.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarButtonScreenshotTest {

    private data class Case(
        val name: String,
        val type: AmarButtonType,
        val iconOnly: Boolean = false,
        val enabled: Boolean = true,
        val loading: Boolean = false,
        val inverse: Boolean = false,
    )

    private val cases = listOf(
        Case("primary_default", AmarButtonType.Primary),
        Case("primary_loading", AmarButtonType.Primary, loading = true),
        Case("primary_disabled", AmarButtonType.Primary, enabled = false),
        Case("secondary_default", AmarButtonType.Secondary),
        Case("naked_default", AmarButtonType.Naked),
        Case("primary_icononly", AmarButtonType.Primary, iconOnly = true),
        Case("error_default", AmarButtonType.Error),
        Case("secondary_inverse", AmarButtonType.Secondary, inverse = true),
    )

    @Test
    fun representativeVariants() {
        cases.forEach { case ->
            captureRoboImage("build/outputs/roborazzi/button_${case.name}.png") {
                CompositionLocalProvider(LocalInspectionMode provides true) {
                    AmarTheme {
                        val icon = painterResource(R.drawable.ds_ic_default_24)
                        AmarButton(
                            label = if (case.iconOnly) null else "Button",
                            onClick = {},
                            type = case.type,
                            enabled = case.enabled,
                            loading = case.loading,
                            inverse = case.inverse,
                            leftIcon = icon,
                            rightIcon = icon,
                        )
                    }
                }
            }
        }
    }
}
