package com.example.and_sales_portal.designsystem.components.feedback

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import com.example.and_sales_portal.designsystem.theme.AmarAlertTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders every Alert variant at 1x (mdpi) framed like Figma node screenshots: 352×72 =
 * the 328×48 node plus the `Shadow/Floating` render bounds (blur 12, offset y 4 → 12/8/12/16).
 * Record with `./gradlew recordRoborazziDebug`; images land in `app/build/outputs/roborazzi/`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarAlertScreenshotTest {

    @Test
    fun allVariants() {
        AmarAlertStyle.entries.forEach { style ->
            AmarAlertType.entries.forEach { type ->
                captureRoboImage("build/outputs/roborazzi/alert_${style.name}_${type.name}.png") {
                    AmarTheme {
                        AmarAlert(
                            message = "Alert message goes here",
                            style = style,
                            type = type,
                            actionLabel = if (style == AmarAlertStyle.Inverse) "Button" else "Action",
                            onAction = {},
                            onDismiss = {},
                            modifier = Modifier
                                .padding(
                                    start = AmarSpace.S12,
                                    top = AmarSpace.S8,
                                    end = AmarSpace.S12,
                                    bottom = AmarSpace.S16,
                                )
                                .width(AmarAlertTokens.Width),
                        )
                    }
                }
            }
        }
    }
}
