package com.example.and_sales_portal.designsystem.components.inputs

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

/** Input, Dropdown, Area and Code Verification at 1x with the Figma sample width (328), for comparison. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-mdpi")
class AmarFieldsScreenshotTest {

    private val width = Modifier.width(328.dp)
    private val label = "Change Label"
    private val placeholder = "Placeholder text"
    private val hintText = "This is a hint text to help user."

    private fun shot(name: String, content: @Composable () -> Unit) =
        captureRoboImage("build/outputs/roborazzi/field_$name.png") { AmarTheme { content() } }

    @Test
    fun fields() {
        shot("input_text_default") { AmarTextField("", {}, width, label = label, placeholder = placeholder) }
        shot("input_text_filled_error") { AmarTextField(placeholder, {}, width, label = label, error = true, hint = hintText) }
        shot("input_password_filled") { AmarTextField("1234567890", {}, width, AmarTextFieldType.Password, label = label) }
        shot("input_currency_default") { AmarTextField("", {}, width, AmarTextFieldType.Currency, label = label, placeholder = placeholder) }
        shot("input_phone_filled") { AmarTextField("81234567890", {}, width, AmarTextFieldType.Phone, label = label) }
        shot("input_search_default") { AmarTextField("", {}, width, AmarTextFieldType.Search, label = label, placeholder = placeholder) }
        shot("input_text_disabled") { AmarTextField("", {}, width, label = label, placeholder = placeholder, enabled = false) }
        shot("input_text_disabled_filled") { AmarTextField(placeholder, {}, width, label = label, enabled = false) }
        shot("dropdown_default") { AmarDropdown(placeholder, {}, width, label = label) }
        shot("dropdown_disabled_filled_error") { AmarDropdown(placeholder, {}, width, label = label, enabled = false, error = true, hint = hintText) }
        shot("area_default") { AmarTextArea("", {}, width, label = label, placeholder = placeholder) }
        shot("area_filled_error") { AmarTextArea(placeholder, {}, width, label = label, error = true, hint = hintText) }
        shot("code_otp_filled") { AmarCodeVerification("111111", {}) }
        shot("code_pin_filled") { AmarCodeVerification("111111", {}, type = AmarCodeType.PIN) }
        shot("code_otp_default_error") { AmarCodeVerification("", {}, error = true, hint = hintText) }
    }
}
