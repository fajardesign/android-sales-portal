package com.example.and_sales_portal.designsystem.components.brand

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/** Figma variant property `Type` of `Logo / Amar Bank`. */
enum class AmarLogoType { Horizontal, Vertical, Logo }

/**
 * Logo / Amar Bank: lettermark with or without the "amarbank" name.
 *
 * Figma: [Logo / Amar Bank 33:177](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=33-177)
 * (Color × Type = 6 variants). The nested `Logo / Lettermark` and `Logo / Name` are part of the drawables.
 *
 * Brand artwork: the colours (gradients, #1253A5) are part of the vector drawable, not theme tokens, and it is not tinted.
 * Natural size as in Figma: Horizontal 165 × 30, Vertical 64 × 32, Logo 32 × 24. Give it another height through
 * [modifier]; the aspect ratio is kept.
 *
 * @param type Figma `Type`.
 * @param color Figma `Color`: `true` = full colour (light backgrounds), `false` = white name for dark backgrounds.
 * In Horizontal and Vertical, Color=False keeps the coloured lettermark; in Logo it is white with `Background/Subtle`.
 * @param contentDescription Accessibility label; "Amar Bank" by default, `null` when the logo is decorative.
 */
@Composable
fun AmarLogo(
    modifier: Modifier = Modifier,
    type: AmarLogoType = AmarLogoType.Horizontal,
    color: Boolean = true,
    contentDescription: String? = stringResource(R.string.ds_logo_amarbank),
) {
    val res = when (type) {
        AmarLogoType.Horizontal -> if (color) R.drawable.ds_logo_amarbank_color_horizontal else R.drawable.ds_logo_amarbank_white_horizontal
        AmarLogoType.Vertical -> if (color) R.drawable.ds_logo_amarbank_color_vertical else R.drawable.ds_logo_amarbank_white_vertical
        AmarLogoType.Logo -> if (color) R.drawable.ds_logo_amarbank_color_logo else R.drawable.ds_logo_amarbank_white_logo
    }
    Image(painterResource(res), contentDescription, modifier, contentScale = ContentScale.Fit)
}

/** All 6 Figma variants: Color=True on `Background/Base`, Color=False on `Background/Inverse`. */
@Preview(name = "Logo / Amar Bank")
@Composable
private fun AmarLogoPreview() {
    AmarTheme {
        Column {
            listOf(true, false).forEach { color ->
                Row(
                    Modifier
                        .background(if (color) AmarTheme.colors.background.base else AmarTheme.colors.background.inverse)
                        .padding(AmarSpace.S16),
                    horizontalArrangement = Arrangement.spacedBy(AmarSpace.S24),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AmarLogoType.entries.forEach { AmarLogo(type = it, color = color) }
                }
            }
        }
    }
}
