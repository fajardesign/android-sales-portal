package com.example.and_sales_portal.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Layout tokens from the Figma **Component** collection (`Component/Alert/...`) that have no
 * semantic equivalent. Colours always come from [AmarColors].
 */
object AmarAlertTokens {
    /** `Position/Width`: width of an alert on a 360dp screen (360 − 2 × [MarginHorizontal]). */
    val Width = 328.dp
    /** `Position/Margin Horizontal` → Space/16. */
    val MarginHorizontal = AmarSpace.S16
    /** `Position/Margin Bottom` → Space/16. */
    val MarginBottom = AmarSpace.S16
    /** `Position/Stack Gap` → Space/8: gap between stacked alerts. */
    val StackGap = AmarSpace.S8
    /** Container padding (Space/16 × Space/14), item gap (Space/12) and radius (Radius/12) used by the variants. */
    val PaddingHorizontal = AmarSpace.S16
    val PaddingVertical = AmarSpace.S14
    val Gap = AmarSpace.S12
    val Radius = AmarRadius.R12
}

/** `Component/Button/Size/...` tokens for one Figma `Size`. */
class AmarButtonSizeTokens internal constructor(
    /** `Height` (also `Width Icon` for Icon Only). */
    val height: Dp,
    val paddingX: Dp,
    val paddingY: Dp,
    val gap: Dp,
)

/** `Component/Button/...` layout tokens. Colours come from [AmarColors.action]. */
object AmarButtonTokens {
    val Large = AmarButtonSizeTokens(height = AmarSpace.S56, paddingX = AmarSpace.S24, paddingY = AmarSpace.S12, gap = AmarSpace.S8)
    val Medium = AmarButtonSizeTokens(height = AmarSpace.S48, paddingX = AmarSpace.S16, paddingY = AmarSpace.S12, gap = AmarSpace.S8)
    val Small = AmarButtonSizeTokens(height = AmarSpace.S40, paddingX = AmarSpace.S12, paddingY = AmarSpace.S12, gap = AmarSpace.S4)

    /** Max width set on every Button variant in Figma. */
    val MaxWidth = 640.dp

    /** Spinner step animation (not specified in Figma; agreed default: 8 steps of 45°, 800 ms per turn). */
    const val SpinnerTurnMillis = 800
}

/** `Elements / Button Group` tokens. */
object AmarButtonGroupTokens {
    /** Gap between buttons: `Component/Modal/Default/Gap/Button` → Space/12. */
    val Gap = AmarSpace.S12
}

/** `Element / Toggle` sizes for one Figma `Size`. */
class AmarToggleSizeTokens internal constructor(val width: Dp, val height: Dp, val knob: Dp)

/** `Component/Toggle/...` tokens. */
object AmarToggleTokens {
    val Medium = AmarToggleSizeTokens(width = 44.dp, height = AmarSpace.S24, knob = 20.dp)
    val Small = AmarToggleSizeTokens(width = 36.dp, height = 20.dp, knob = AmarSpace.S16)
    /** `Toggle/Padding` → Spacing/Padding/3XS. */
    val Padding = AmarSpace.S2
    /** Layer opacity of `State=Disabled` (raw value in Figma, no token). */
    const val DisabledOpacity = 0.5f
}

/** `Element / Icon` + `Selection Control` sizes for one Figma `Size`. */
class AmarSelectionSizeTokens internal constructor(
    val box: Dp,
    val radius: Dp,
    /** Radio dot diameter. */
    val dot: Dp,
    /** `Selection/Gap/…`: gap between box and text. */
    val gap: Dp,
    /** Gap between label and description. */
    val contentGap: Dp,
)

/** `Component/Selection/...` tokens. */
object AmarSelectionTokens {
    val Medium = AmarSelectionSizeTokens(
        box = AmarSpace.S24, radius = AmarRadius.R6, dot = 9.6.dp,
        gap = AmarSpace.S12, contentGap = AmarSpace.S2,
    )
    val Small = AmarSelectionSizeTokens(
        box = AmarSpace.S16, radius = AmarRadius.R4, dot = 6.dp,
        gap = AmarSpace.S8, contentGap = AmarSpace.S0,
    )
    /** Vertical padding of a Selection Control row (Icon Only has none). */
    val PaddingVertical = AmarSpace.S12
    /** Top padding of the box frame, aligning it with the first text line. */
    val BoxOffsetTop = AmarSpace.S2
    /** Layer opacity of disabled `Element / Icon` (raw value in Figma, no token). */
    const val DisabledOpacity = 0.5f
}
