package com.example.and_sales_portal.designsystem.components.feedback

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Info Box (328 wide) and Badge renders at 1x, for comparison with Figma node screenshots. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarFeedbackScreenshotTest {

    private fun shot(name: String, content: @Composable () -> Unit) =
        captureRoboImage("build/outputs/roborazzi/fb_$name.png") { AmarTheme { content() } }

    @Test
    fun infoBoxAndBadge() {
        val w = Modifier.width(328.dp)
        val msg = "Insert the description here."
        shot("infobox_info_btn_label") { AmarInfoBox(msg, w, AmarInfoBoxType.Info, label = "Label", buttonLabel = "Button", onButtonClick = {}) }
        shot("infobox_neutral") { AmarInfoBox(msg, w, AmarInfoBoxType.Neutral) }
        shot("infobox_warning_label") { AmarInfoBox(msg, w, AmarInfoBoxType.Warning, label = "Label") }
        shot("infobox_error_btn") { AmarInfoBox(msg, w, AmarInfoBoxType.Error, buttonLabel = "Button", onButtonClick = {}) }
        shot("badge_info_md_counter") { AmarBadge("Label", count = 1) }
        shot("badge_info_sm_lefticon_solid") { AmarBadge("Label", size = AmarBadgeSize.Small, solid = true, leftIcon = painterResource(R.drawable.ds_ic_default_16)) }
        shot("badge_error_lg_close_solid") { AmarBadge("Label", type = AmarBadgeType.Error, size = AmarBadgeSize.Large, solid = true, onClose = {}) }
        shot("badge_success_md_close") { AmarBadge("Label", type = AmarBadgeType.Success, onClose = {}) }
        shot("badge_warning_lg_lefticon") { AmarBadge("Label", type = AmarBadgeType.Warning, size = AmarBadgeSize.Large, leftIcon = painterResource(R.drawable.ds_ic_default_16)) }
        shot("badge_error_sm_default") { AmarBadge("Label", type = AmarBadgeType.Error, size = AmarBadgeSize.Small) }
        shot("badge_warning_md_counter_solid") { AmarBadge("Label", type = AmarBadgeType.Warning, solid = true, count = 1) }
    }
}
