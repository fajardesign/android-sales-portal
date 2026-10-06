package com.example.and_sales_portal.designsystem.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTextFieldTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/**
 * Area (multi-line text field) with a fixed 96dp field and an optional character counter.
 *
 * Figma: [Area 311:744](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=311-744)
 * (State × Error = 12 variants). States are derived as in [AmarTextField].
 *
 * @param maxLength Figma `Counter`: shows "n/maxLength" (`Text/Counter`, right aligned) and limits input; `null` hides it.
 */
@Composable
fun AmarTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    required: Boolean = false,
    placeholder: String = "",
    hint: String? = null,
    error: Boolean = false,
    enabled: Boolean = true,
    maxLength: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focused by interactionSource.collectIsFocusedAsState()
    val colors = fieldColors(enabled, focused, error)
    val textStyle = AmarTheme.typography.bodyLg
    FieldLayout(modifier, label, required, hint, error) {
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(if (maxLength != null) it.take(maxLength) else it) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            textStyle = textStyle.copy(color = colors.text),
            cursorBrush = SolidColor(colors.cursor),
            keyboardOptions = keyboardOptions,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Column(
                    modifier = Modifier
                        .height(AmarTextFieldTokens.AreaHeight)
                        .fieldFrame(colors, focused),
                    verticalArrangement = Arrangement.spacedBy(AmarTextFieldTokens.GapInput),
                ) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        if (value.isEmpty()) Text(placeholder, style = textStyle, color = colors.placeholder)
                        innerTextField()
                    }
                    maxLength?.let {
                        Text(
                            text = "${value.length}/$it",
                            style = AmarTheme.typography.captionSm,
                            color = AmarTheme.colors.content.tertiary, // Text Field/Text/Counter
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
        )
    }
}

/** Static Figma states (Default, Filled, Disabled, Disabled Filled) × Error, with the counter on. */
@Preview(name = "Area", widthDp = 360)
@Composable
private fun AmarTextAreaPreview() {
    AmarTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(AmarTheme.colors.background.base)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S16),
        ) {
            listOf(false, true).forEach { error ->
                listOf(true, false).forEach { enabled ->
                    listOf("", "Placeholder text").forEach { value ->
                        AmarTextArea(
                            value = value, onValueChange = {}, label = "Change Label", placeholder = "Placeholder text",
                            error = error, enabled = enabled, maxLength = 200,
                            hint = if (error) "This is a hint text to help user." else null,
                        )
                    }
                }
            }
        }
    }
}
