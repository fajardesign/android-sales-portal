package com.example.and_sales_portal.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Foundation `Radius/…` primitives. `Radius/Full` (999) is expressed as a pill shape in [AmarShapes.full]. */
object AmarRadius {
    val R0 = 0.dp
    val R2 = 2.dp
    val R4 = 4.dp
    val R6 = 6.dp
    val R8 = 8.dp
    val R10 = 10.dp
    val R12 = 12.dp
    val R16 = 16.dp
}

/**
 * Semantic `Radius/…` shapes.
 * `Radius/Native` (34) is not included: it only serves the iOS-style Native modal/button, which is not built.
 */
@Immutable
data class AmarShapes(
    val none: Shape = RectangleShape,
    val field: Shape = RoundedCornerShape(AmarRadius.R10),
    val control: Shape = RoundedCornerShape(AmarRadius.R12),
    val card: Shape = RoundedCornerShape(AmarRadius.R12),
    val container: Shape = RoundedCornerShape(AmarRadius.R16),
    val cta: Shape = RoundedCornerShape(AmarRadius.R16),
    val full: Shape = RoundedCornerShape(percent = 50),
)
