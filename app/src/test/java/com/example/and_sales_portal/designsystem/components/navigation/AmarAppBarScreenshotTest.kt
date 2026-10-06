package com.example.and_sales_portal.designsystem.components.navigation

import androidx.compose.foundation.layout.WindowInsets
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

/** App Bar headers (no status bar inset) at 1x, compared with the header part of the Figma variants. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarAppBarScreenshotTest {
    @Test
    fun appBars() {
        val cases = listOf(
            Triple("secondary_default", AmarAppBarType.Secondary, AmarAppBarState.Default),
            Triple("primary_default", AmarAppBarType.Primary, AmarAppBarState.Default),
            Triple("primary_ti", AmarAppBarType.Primary, AmarAppBarState.TransparentInverse),
            Triple("secondary_ti", AmarAppBarType.Secondary, AmarAppBarState.TransparentInverse),
        )
        cases.forEach { (name, type, state) ->
            captureRoboImage("build/outputs/roborazzi/nav_appbar_$name.png") {
                AmarTheme {
                    AmarAppBar(
                        title = "Title", modifier = Modifier.width(360.dp), type = type, state = state,
                        subtitle = if (name == "primary_ti") "Sub" else null,
                        onNavigationClick = if (type == AmarAppBarType.Secondary) ({}) else null,
                        onActionClick = {}, windowInsets = WindowInsets(0),
                    )
                }
            }
        }
    }
}
