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

/** Login (`02 | Login`) and splash (`01 | Splash Screen | Loading`) layout, raw values from Figma "Sales - Partner Dashboard - Android". */
object AmarAuthTokens {
    /** `login-content`: padding top 60, sides 16; logo ↔ form 52. */
    val ContentPaddingTop = 60.dp
    val ContentPadding = AmarSpace.S16
    val LogoToForm = 52.dp
    /** Form ↔ actions 24; fields 16; title ↔ subtitle 4; button ↔ "Lupa password?" 16. */
    val SectionGap = AmarSpace.S24
    val FieldGap = AmarSpace.S16
    val IntroGap = AmarSpace.S4
    val ActionGap = AmarSpace.S16
    /** Footer with the version: padding 16. */
    val FooterPadding = AmarSpace.S16
    /** Splash: logo ↔ title ↔ loading 48; spinner ↔ "Memuat..." 16. */
    val SplashGap = 48.dp
    val SplashLoadingGap = AmarSpace.S16
}

/**
 * Absensi card (Figma "01 | Team Leader Dashboard" › `Attendance hero`) and the ATT screens. Figma has no frames for
 * the clock-in steps or the visit screens; their sizes reuse spacing steps (noted per value).
 */
object AmarActivityTokens {
    /** `Attendance sequence` gap Spacing/Gap/SM; `Time sequence` gap Spacing/Gap/SM. */
    val SequenceGap = AmarSpace.S8
    val TileGap = AmarSpace.S8
    /** `Time step`: padding Spacing/Padding/SM, radius 8 (raw), label ↔ time Spacing/Gap/XS. */
    val TilePadding = AmarSpace.S12
    val TileRadius = AmarRadius.R8
    val TileTextGap = AmarSpace.S4
    /** Not in Figma: map preview and selfie preview height (Space 128 + 32). */
    val PreviewHeight = AmarSpace.S128 + AmarSpace.S32
    /** Not in Figma: gap between cards in lists (Spacing/Gap/MD). */
    val ListGap = AmarSpace.S12
    /** `Daily activity metrics` tile: padding Spacing/Gap/XS (sides) and Spacing/Gap/SM (top, bottom). */
    val MetricPaddingX = AmarSpace.S4
    val MetricPaddingY = AmarSpace.S8
    /** `Incentive hero content` gap Spacing/Gap/MD. */
    val HeroGap = AmarSpace.S12
    /** Not in Figma: sample QR of the Kode Referral on the toko card (Space 128). */
    val QrSize = AmarSpace.S128
    /**
     * Not in Figma (UI refresh 2026-10-17, layout only): quick action tile of the Beranda (icon in a Space 48 circle,
     * label under it, Spacing/Gap/SM) and a list row (Spacing/Padding/SM top and bottom).
     */
    val QuickActionBox = AmarSpace.S48
    val QuickActionGap = AmarSpace.S8
    val ListRowPadding = AmarSpace.S12
    /** Not in Figma: selfie area of the capture step, taller than the map (Space 128 × 2). */
    val SelfiePreviewHeight = AmarSpace.S128 + AmarSpace.S128
}
