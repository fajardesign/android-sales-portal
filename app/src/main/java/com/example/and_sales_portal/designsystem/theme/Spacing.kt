package com.example.and_sales_portal.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Foundation `Space/…` primitives. */
object AmarSpace {
    val S0 = 0.dp
    val S2 = 2.dp
    val S4 = 4.dp
    val S8 = 8.dp
    val S10 = 10.dp
    val S12 = 12.dp
    val S14 = 14.dp
    val S16 = 16.dp
    val S20 = 20.dp
    val S24 = 24.dp
    val S32 = 32.dp
    val S40 = 40.dp
    val S48 = 48.dp
    val S56 = 56.dp
    val S64 = 64.dp
    val S80 = 80.dp
    val S96 = 96.dp
    val S128 = 128.dp
}

/** Semantic `Spacing/Padding/…` and `Spacing/Gap/…`. */
@Immutable
data class AmarSpacing(
    val paddingNone: Dp = AmarSpace.S0,
    val padding3xs: Dp = AmarSpace.S2,
    val padding2xs: Dp = AmarSpace.S8,
    val paddingXs: Dp = AmarSpace.S10,
    val paddingSm: Dp = AmarSpace.S12,
    val paddingMd: Dp = AmarSpace.S16,
    val paddingMl: Dp = AmarSpace.S20,
    val paddingLg: Dp = AmarSpace.S24,
    val paddingXl: Dp = AmarSpace.S32,
    val padding2xl: Dp = AmarSpace.S40,
    val padding3xl: Dp = AmarSpace.S48,
    val gapNone: Dp = AmarSpace.S0,
    val gapXs: Dp = AmarSpace.S4,
    val gapSm: Dp = AmarSpace.S8,
    val gapMd: Dp = AmarSpace.S12,
    val gap2md: Dp = AmarSpace.S14,
    val gapLg: Dp = AmarSpace.S16,
    val gapXl: Dp = AmarSpace.S24,
    val gap2xl: Dp = AmarSpace.S32,
)

/** Foundation `Border/Width/…` + semantic `Border/Size/…`. */
@Immutable
data class AmarBorderWidths(
    val none: Dp = 0.dp,
    val subtle: Dp = 1.dp,
    val strong: Dp = 2.dp,
    val thick: Dp = 4.dp,
)

/** `Icon/Size/…`. */
@Immutable
data class AmarIconSizes(
    val xs: Dp = 12.dp,
    val sm: Dp = 16.dp,
    val md: Dp = 20.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
)
