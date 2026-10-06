package com.example.and_sales_portal.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Layout tokens of form screens in Figma "Sales - Partner Dashboard - Android" (Partner Registration REG-02 … REG-06).
 * Values bound to variables in Figma name the variable; the others are raw values in the frames.
 */
object AmarFormTokens {
    /** `Scrollable form`: padding Spacing/Padding/LG (top, bottom) and Spacing/Padding/MD (sides), gap Spacing/Padding/MD. */
    val PaddingVertical = AmarSpace.S24
    val PaddingHorizontal = AmarSpace.S16
    val FieldGap = AmarSpace.S16
    /** `Form introduction`: step ↔ title ↔ description (raw). */
    val IntroGap = AmarSpace.S4
    /** `Choice field`: label ↔ chips ↔ hint (raw 5), chips gap 8 (raw). */
    val ChoiceFieldGap = 5.dp
    val ChoicesGap = AmarSpace.S8
    /** Two fields side by side (RT and RW, account number and "Periksa"), raw. */
    val SideBySideGap = AmarSpace.S12
    /**
     * `Persistent action`: padding Spacing/Padding/MD with `Shadow/Bottom`. Figma's bottom padding (24, bound to
     * `Status Bar/Height`) stands in for the device safe area; on Android it is 16 plus the navigation bar inset.
     */
    val ActionPadding = AmarSpace.S16
}

/** `06 | Partner Registration | Success | Submission Sent` layout (raw values). */
object AmarSuccessTokens {
    /** `Content`: padding 32 top, 16 sides and bottom; gap 20 between the message and the summary. */
    val ContentPaddingTop = AmarSpace.S32
    val ContentPadding = AmarSpace.S16
    val ContentGap = AmarSpace.S20
    /** `Success message`: gap 14 between illustration, title, description and badge. */
    val MessageGap = AmarSpace.S14
    /** `Submission summary`: padding 16, gap 12 between rows; label ↔ value 8. */
    val SummaryPadding = AmarSpace.S16
    val SummaryGap = AmarSpace.S12
    val RowGap = AmarSpace.S8
}
