package com.example.and_sales_portal.designsystem.assets

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.and_sales_portal.R
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Renders each converted illustration at 1x for comparison with the Figma node screenshot. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarIllustrationScreenshotTest {
    private val illustrations = mapOf(
        "medium_stopper" to R.drawable.ds_ill_medium_stopper,
        "medium_wifi_problem" to R.drawable.ds_ill_medium_wifi_problem,
        "medium_system_problem" to R.drawable.ds_ill_medium_system_problem,
        "medium_rejected" to R.drawable.ds_ill_medium_rejected,
        "medium_otp" to R.drawable.ds_ill_medium_otp,
        "medium_in_process" to R.drawable.ds_ill_medium_in_process,
        "medium_email_notif" to R.drawable.ds_ill_medium_email_notif,
        "medium_permission_location" to R.drawable.ds_ill_medium_permission_location,
        "medium_permission_camera" to R.drawable.ds_ill_medium_permission_camera,
        "medium_permission_device_data" to R.drawable.ds_ill_medium_permission_device_data,
        "medium_empty_search" to R.drawable.ds_ill_medium_empty_search,
        "large_failed_get_data" to R.drawable.ds_ill_large_failed_get_data,
        "large_email_notif" to R.drawable.ds_ill_large_email_notif,
        "large_success" to R.drawable.ds_ill_large_success,
        "file_document" to R.drawable.ds_ill_file_document,
        "decorative_check" to R.drawable.ds_ill_decorative_check,
    )

    @Test
    fun illustrations() {
        illustrations.forEach { (name, res) ->
            captureRoboImage("build/outputs/roborazzi/ill_$name.png") { Image(painterResource(res), contentDescription = null) }
        }
    }
}
