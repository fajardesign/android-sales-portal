package com.example.and_sales_portal.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
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

/**
 * Draws a Figma-style drop shadow behind the content, following [shape].
 *
 * Figma blur radius = 2σ; Android's shadow-layer radius is converted so σ matches.
 * Shadow layers on shapes need hardware support from API 28; on API 24–27 no shadow is drawn.
 * [AmarShadow.spread] is not supported (all current tokens use 0).
 */
fun Modifier.amarShadow(shadow: AmarShadow, shape: Shape): Modifier = drawBehind {
    val sigma = shadow.blur.toPx() / 2f
    val layerRadius = if (sigma > 0.5f) (sigma - 0.5f) / 0.57735f else 0f
    val outline = shape.createOutline(size, layoutDirection, this)
    drawIntoCanvas { canvas ->
        val paint = Paint()
        paint.asFrameworkPaint().apply {
            color = Color.Transparent.toArgb()
            setShadowLayer(
                layerRadius,
                shadow.offsetX.toPx(),
                shadow.offsetY.toPx(),
                shadow.color.toArgb(),
            )
        }
        canvas.drawOutline(outline, paint)
    }
}
