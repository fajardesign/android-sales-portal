package com.example.and_sales_portal.designsystem.components.feedback

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import com.example.and_sales_portal.designsystem.theme.AmarButtonTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import kotlin.math.floor

/**
 * Geometry of the Figma `Spinner` (24×24 frame): 8 rounded bars, 45° apart, clockwise from 12 o'clock.
 * Values are in 24-unit frame coordinates and scale with the spinner size.
 */
private object SpinnerGeometry {
    const val Frame = 24f
    const val BarWidth = 3.2727273f
    const val BarLength = 7.6363635f
    const val BarRadius = 1.6363639f
    /** Opacity per bar, clockwise from the top bar (Figma layers "0", "45", "90", … in that order). */
    val Opacity = floatArrayOf(1f, 0.15f, 0.27f, 0.39f, 0.51f, 0.63f, 0.75f, 0.87f)
}

/**
 * Spinner: 8-bar activity indicator, tinted with [color].
 *
 * Figma: [Spinner 536:1213](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=536-1213).
 * Figma only defines the static frame. It rotates in 45° steps, one turn per
 * [AmarButtonTokens.SpinnerTurnMillis]. Static in previews.
 *
 * @param animated set to false to draw a single frame (e.g. screenshot tests).
 */
@Composable
fun AmarSpinner(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = AmarTheme.iconSizes.lg,
    animated: Boolean = !LocalInspectionMode.current,
) {
    val step = if (animated) {
        val transition = rememberInfiniteTransition(label = "spinner")
        val progress = transition.animateFloat(
            initialValue = 0f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(AmarButtonTokens.SpinnerTurnMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "spinnerStep",
        )
        floor(progress.value)
    } else {
        0f
    }
    Canvas(modifier.size(size)) {
        val unit = this.size.minDimension / SpinnerGeometry.Frame
        val barSize = Size(SpinnerGeometry.BarWidth * unit, SpinnerGeometry.BarLength * unit)
        val radius = CornerRadius(SpinnerGeometry.BarRadius * unit)
        val topLeft = Offset((this.size.width - barSize.width) / 2f, 0f)
        rotate(step * 45f) {
            SpinnerGeometry.Opacity.forEachIndexed { index, alpha ->
                rotate(index * 45f) {
                    drawRoundRect(color = color, topLeft = topLeft, size = barSize, cornerRadius = radius, alpha = alpha)
                }
            }
        }
    }
}
