package com.example.and_sales_portal.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** One Figma DROP_SHADOW effect. [blur] is the Figma blur radius. */
@Immutable
data class AmarShadow(
    val offsetX: Dp,
    val offsetY: Dp,
    val blur: Dp,
    val spread: Dp,
    val color: Color,
)

/**
 * Figma local effect styles `Shadow/…` (chosen over the CSS-string `Shadow/XS–XL` variables,
 * because the effect styles are what nodes actually use).
 * `Overlay/Subtle` and `Overlay/Strong` (0 offset, 0 blur) are tint layers, not shadows, and are not included.
 */
@Immutable
data class AmarElevation(
    val subtle: AmarShadow = AmarShadow(0.dp, 1.dp, 2.dp, 0.dp, Color.Black.copy(alpha = 0.04f)),
    val surface: AmarShadow = AmarShadow(0.dp, 2.dp, 6.dp, 0.dp, Color.Black.copy(alpha = 0.06f)),
    val floating: AmarShadow = AmarShadow(0.dp, 4.dp, 12.dp, 0.dp, Color.Black.copy(alpha = 0.08f)),
    val modal: AmarShadow = AmarShadow(0.dp, 8.dp, 24.dp, 0.dp, Color.Black.copy(alpha = 0.10f)),
    val overlay: AmarShadow = AmarShadow(0.dp, 16.dp, 40.dp, 0.dp, Color.Black.copy(alpha = 0.12f)),
    val bottom: AmarShadow = AmarShadow(0.dp, (-4).dp, 4.dp, 0.dp, Color.Black.copy(alpha = 0.10f)),
)
