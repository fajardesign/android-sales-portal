package com.example.and_sales_portal.designsystem.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarCodeVerificationTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTextFieldTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/** Figma variant property `State` of `Element / Digit`. */
enum class AmarDigitState { Default, DefaultError, Active, Disabled, Filled, FilledError }

/**
 * One code digit box.
 *
 * Figma: [Element / Digit 317:1499](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=317-1499)
 * (State × Masking = 12 variants).
 *
 * @param digit the character shown when filled.
 * @param masking Figma `Masking`: filled digits show a 20dp dot instead of the character.
 * Error states use a 1.5 border, as in the Figma master.
 */
@Composable
fun AmarDigit(
    state: AmarDigitState,
    modifier: Modifier = Modifier,
    digit: Char? = null,
    masking: Boolean = false,
) = DigitBox(state, modifier, digit, masking, errorBorderWidth = AmarTextFieldTokens.BorderWidthFocus)

@Composable
private fun DigitBox(
    state: AmarDigitState,
    modifier: Modifier,
    digit: Char?,
    masking: Boolean,
    errorBorderWidth: Dp,
) {
    val c = AmarTheme.colors
    val enabled = state != AmarDigitState.Disabled
    val error = state == AmarDigitState.DefaultError || state == AmarDigitState.FilledError
    val focused = state == AmarDigitState.Active
    val colors = fieldColors(enabled, focused, error)
    val width = when {
        focused -> AmarTextFieldTokens.BorderWidthFocus
        error -> errorBorderWidth
        else -> AmarTheme.borderWidths.subtle
    }
    val shape = RoundedCornerShape(AmarTextFieldTokens.Radius)
    Box(
        modifier = modifier
            .size(AmarCodeVerificationTokens.DigitSize)
            .background(colors.background, shape)
            .border(width, colors.border, shape)
            .padding(horizontal = AmarCodeVerificationTokens.DigitPaddingX, vertical = AmarCodeVerificationTokens.DigitPaddingY),
        contentAlignment = Alignment.Center,
    ) {
        val filled = state == AmarDigitState.Filled || state == AmarDigitState.FilledError ||
            (state == AmarDigitState.Disabled && digit != null)
        when {
            // Figma shows a static "|" caret in State=Active.
            focused -> Text("|", style = AmarTheme.typography.headingSm, color = c.content.primary, textAlign = TextAlign.Center)
            filled && masking -> Box(
                Modifier
                    .size(AmarTheme.iconSizes.md)
                    .background(if (enabled) c.icon.primary else c.content.disabled, AmarTheme.shapes.full),
            )
            filled -> Text(
                digit.toString(),
                style = AmarTheme.typography.headingSm,
                color = if (enabled) c.content.primary else c.content.disabled,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Figma variant property `Type`. PIN masks the digits. */
enum class AmarCodeType { OTP, PIN }

/**
 * Code Verification: 6 digit boxes for an OTP or PIN, typed with the numeric keyboard.
 *
 * Figma: [Code Verification 321:502](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=321-502)
 * (Type × State = 16 variants).
 *
 * Figma `State` is derived from [value], focus, [error] and [enabled]: filled digits before the cursor, the
 * next digit Active while focused, the rest Default.
 *
 * @param hint Figma `Show Hint`: `Caption/MD` + `Helper/Default`, or `Caption/SM` + `Helper/Error` when [error].
 * @param hintIcon Figma `Hint Icon` (non-error hint only).
 */
@Composable
fun AmarCodeVerification(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    type: AmarCodeType = AmarCodeType.OTP,
    hint: String? = null,
    hintIcon: Boolean = false,
    error: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focused by interactionSource.collectIsFocusedAsState()
    val length = AmarCodeVerificationTokens.Length
    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(length)) },
        modifier = modifier,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            // The real text field is invisible; the digit boxes draw the value.
            Box(Modifier.size(AmarSpace.S0).alpha(0f)) { innerTextField() }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AmarCodeVerificationTokens.Gap),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(AmarCodeVerificationTokens.Gap)) {
                    repeat(length) { i ->
                        val state = when {
                            !enabled -> AmarDigitState.Disabled
                            i < value.length -> if (error) AmarDigitState.FilledError else AmarDigitState.Filled
                            i == value.length && focused && !error -> AmarDigitState.Active
                            error -> AmarDigitState.DefaultError
                            else -> AmarDigitState.Default
                        }
                        // Code Verification overrides the error border of its digits to 1 (master: 1.5).
                        DigitBox(
                            state, Modifier, value.getOrNull(i), masking = type == AmarCodeType.PIN,
                            errorBorderWidth = AmarTheme.borderWidths.subtle,
                        )
                    }
                }
                hint?.let { CodeHint(it, error, hintIcon) }
            }
        },
    )
}

@Composable
private fun CodeHint(text: String, error: Boolean, icon: Boolean) {
    val color = if (error) AmarTheme.colors.feedback.error.border else AmarTheme.colors.content.tertiary
    Row(horizontalArrangement = Arrangement.spacedBy(AmarSpace.S4), verticalAlignment = Alignment.CenterVertically) {
        if (icon && !error) {
            Icon(painterResource(R.drawable.ds_ic_info_2_24), null, Modifier.size(AmarTheme.iconSizes.md), tint = color)
        }
        Text(
            text,
            style = if (error) AmarTheme.typography.captionSm else AmarTheme.typography.captionMd,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
}

/** All 12 `Element / Digit` variants. */
@Preview(name = "Digit")
@Composable
private fun AmarDigitPreview() {
    AmarTheme {
        Column(
            Modifier
                .background(AmarTheme.colors.background.base)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S8),
        ) {
            listOf(false, true).forEach { masking ->
                Row(horizontalArrangement = Arrangement.spacedBy(AmarSpace.S8)) {
                    AmarDigitState.entries.forEach { AmarDigit(it, digit = '1', masking = masking) }
                }
            }
        }
    }
}

/** Static Figma states of Code Verification for OTP and PIN (Active / Typing need focus). */
@Preview(name = "Code Verification")
@Composable
private fun AmarCodeVerificationPreview() {
    AmarTheme {
        Column(
            Modifier
                .background(AmarTheme.colors.background.base)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S16),
        ) {
            AmarCodeType.entries.forEach { type ->
                AmarCodeVerification("", {}, type = type)
                AmarCodeVerification("123456", {}, type = type)
                AmarCodeVerification("123456", {}, type = type, enabled = false)
                AmarCodeVerification("", {}, type = type, error = true, hint = "This is a hint text to help user.")
                AmarCodeVerification("123456", {}, type = type, error = true, hint = "This is a hint text to help user.")
            }
        }
    }
}
