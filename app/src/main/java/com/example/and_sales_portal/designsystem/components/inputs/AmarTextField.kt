package com.example.and_sales_portal.designsystem.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTextFieldTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/** Figma variant property `Type`. */
enum class AmarTextFieldType { Text, Currency, Password, Phone, Search }

/** Masks every character as "• " (bullet + space), as in the Figma Password variants. */
private object SpacedBulletTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val masked = "• ".repeat(text.length)
        return TransformedText(
            AnnotatedString(masked),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = offset * 2
                override fun transformedToOriginal(offset: Int) = ((offset + 1) / 2).coerceAtMost(text.length)
            },
        )
    }
}

/**
 * Groups digits with "." (id-ID), e.g. 24000000 → 24.000.000. Not defined in Figma; the format follows the
 * project's content guidelines (`Rp 24.000.000`).
 */
private object ThousandsTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val out = StringBuilder()
        digits.forEachIndexed { i, ch ->
            if (i > 0 && (digits.length - i) % 3 == 0) out.append('.')
            out.append(ch)
        }
        val dotsBefore = { originalOffset: Int ->
            (1 until originalOffset).count { i -> (digits.length - i) % 3 == 0 }
        }
        return TransformedText(
            AnnotatedString(out.toString()),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = offset + dotsBefore(offset)
                override fun transformedToOriginal(offset: Int): Int {
                    var original = 0
                    while (original < digits.length && originalToTransformed(original + 1) <= offset) original++
                    return original
                }
            },
        )
    }
}

/**
 * Input (text field).
 *
 * Figma: [Input 236:235](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=236-235)
 * (Type × State × Error = 60 variants).
 *
 * Figma `State` is derived: Default (empty), Active (focused, empty), Typing (focused, with text),
 * Filled (unfocused, with text), Disabled / Disabled - Filled ([enabled] = false).
 *
 * Type behaviour: Currency shows the "Rp" prefix and accepts digits, grouped as 24.000.000; Phone shows "+62"
 * and accepts digits; Password masks as "• " with an eye toggle; Search shows the search icon.
 *
 * @param label Figma `Show Label` + label text; `null` hides it.
 * @param required asterisk on the label (`Input / Label` `Required`).
 * @param placeholder Figma `Edit Text` when empty.
 * @param hint Figma `Show Hint` + hint text; shown in the error colour with an icon when [error].
 * @param error Figma `Error`.
 * @param leftIcon Figma `Left Icon` + `Change Left` (Type=Text only).
 * @param rightIcon Figma `Right Icon` + `Change Right` (Type=Text only).
 */
@Composable
fun AmarTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    type: AmarTextFieldType = AmarTextFieldType.Text,
    label: String? = null,
    required: Boolean = false,
    placeholder: String = "",
    hint: String? = null,
    error: Boolean = false,
    enabled: Boolean = true,
    leftIcon: Painter? = null,
    rightIcon: Painter? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focused by interactionSource.collectIsFocusedAsState()
    val colors = fieldColors(enabled, focused, error)
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val digitsOnly = type == AmarTextFieldType.Currency || type == AmarTextFieldType.Phone
    val transformation = when {
        type == AmarTextFieldType.Password && !passwordVisible -> SpacedBulletTransformation
        type == AmarTextFieldType.Currency -> ThousandsTransformation
        else -> VisualTransformation.None
    }
    val keyboard = when (type) {
        AmarTextFieldType.Currency -> keyboardOptions.copy(keyboardType = KeyboardType.Number)
        AmarTextFieldType.Phone -> keyboardOptions.copy(keyboardType = KeyboardType.Phone)
        AmarTextFieldType.Password -> keyboardOptions.copy(keyboardType = KeyboardType.Password)
        else -> keyboardOptions
    }
    val textStyle = AmarTheme.typography.bodyLg
    val iconSize = AmarTheme.iconSizes.lg

    FieldLayout(modifier, label, required, hint, error) {
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(if (digitsOnly) it.filter(Char::isDigit) else it) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            textStyle = textStyle.copy(color = colors.text),
            cursorBrush = SolidColor(colors.cursor),
            visualTransformation = transformation,
            keyboardOptions = keyboard,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fieldFrame(colors, focused),
                    // Figma uses 4 instead of 8 while focused; kept at 8 so the text does not shift on focus.
                    horizontalArrangement = Arrangement.spacedBy(AmarTextFieldTokens.GapInput),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (type) {
                        AmarTextFieldType.Search -> Icon(
                            painterResource(R.drawable.ds_ic_search_24), null,
                            Modifier.size(iconSize), tint = colors.iconStrong,
                        )
                        AmarTextFieldType.Currency -> Text(stringResource(R.string.ds_input_prefix_currency), style = textStyle, color = colors.text)
                        AmarTextFieldType.Phone -> Text(stringResource(R.string.ds_input_prefix_phone), style = textStyle, color = colors.text)
                        AmarTextFieldType.Text -> leftIcon?.let { Icon(it, null, Modifier.size(iconSize), tint = colors.icon) }
                        AmarTextFieldType.Password -> Unit
                    }
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(placeholder, style = textStyle, color = colors.placeholder, maxLines = 1)
                        }
                        innerTextField()
                    }
                    when (type) {
                        AmarTextFieldType.Password -> Icon(
                            painter = painterResource(
                                if (passwordVisible) R.drawable.ds_ic_eye_hide_24 else R.drawable.ds_ic_eye_reveal_24,
                            ),
                            contentDescription = stringResource(
                                if (passwordVisible) R.string.ds_input_password_hide else R.string.ds_input_password_show,
                            ),
                            tint = colors.icon,
                            modifier = Modifier
                                .size(iconSize)
                                .clickable(enabled = enabled, role = Role.Button) { passwordVisible = !passwordVisible },
                        )
                        AmarTextFieldType.Text -> rightIcon?.let { Icon(it, null, Modifier.size(iconSize), tint = colors.icon) }
                        else -> Unit
                    }
                }
            },
        )
    }
}

private class TextFieldTypeProvider : PreviewParameterProvider<AmarTextFieldType> {
    override val values = AmarTextFieldType.entries.asSequence()
}

/**
 * One preview per Figma `Type` (5). Static states shown: Default, Filled, Disabled, Disabled - Filled,
 * each with Error off and on (Active / Typing need focus and show in the interactive preview).
 */
@Preview(name = "Input", widthDp = 360)
@Composable
private fun AmarTextFieldPreview(@PreviewParameter(TextFieldTypeProvider::class) type: AmarTextFieldType) {
    val filled = when (type) {
        AmarTextFieldType.Currency -> "24000000"
        AmarTextFieldType.Phone -> "81234567890"
        else -> "Placeholder text"
    }
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
                    listOf("", filled).forEach { value ->
                        AmarTextField(
                            value = value, onValueChange = {}, type = type, label = "Change Label",
                            placeholder = "Placeholder text", enabled = enabled, error = error,
                            hint = if (error) "This is a hint text to help user." else null,
                        )
                    }
                }
            }
        }
    }
}
