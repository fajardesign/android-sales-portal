package com.example.and_sales_portal.designsystem.theme

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
