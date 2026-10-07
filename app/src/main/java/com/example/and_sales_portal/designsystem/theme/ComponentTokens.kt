package com.example.and_sales_portal.designsystem.theme

import androidx.compose.ui.graphics.Color
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

/** `Component/Text Field/...` tokens shared by Input, Dropdown, Area and Code Verification. */
object AmarTextFieldTokens {
    /** `Layout/Height` → Space/48. */
    val Height = AmarSpace.S48
    /** `Layout/Radius` → Radius/Field. */
    val Radius = AmarRadius.R10
    /** `Layout/Padding Left` → Spacing/Padding/SM. */
    val PaddingLeft = AmarSpace.S12
    /** `Layout/Padding Right` → Spacing/Padding/XS. */
    val PaddingRight = AmarSpace.S10
    /** `Layout/Padding Y` → Spacing/Padding/SM. */
    val PaddingVertical = AmarSpace.S12
    /** `Layout/Gap Input` → Spacing/Gap/SM: between items inside the field. */
    val GapInput = AmarSpace.S8
    /** `Layout/Gap` → Spacing/Gap/XS: between label, field and hint. */
    val Gap = AmarSpace.S4
    /** `Border/Width Focus` (raw value in Figma). */
    val BorderWidthFocus = 1.5.dp
    /** Gap between label, asterisk and sub label in `Input / Label` (raw value in Figma). */
    val LabelGap = 1.dp
    /** Layer opacity of the value text in `Disabled - Filled` (raw value in Figma). */
    const val DisabledValueOpacity = 0.7f
    /** Layer opacity of the placeholder in `Disabled` (raw value in Figma). */
    const val DisabledPlaceholderOpacity = 0.6f
    /** Fixed field height of `Area` (raw value in Figma). */
    val AreaHeight = 96.dp
}

/** `Code Verification` / `Element / Digit` tokens. */
object AmarCodeVerificationTokens {
    /** Number of digits in every Figma variant. */
    const val Length = 6
    /** `Element / Digit` box size (raw value in Figma). */
    val DigitSize = 44.dp
    /** Digit padding: Spacing/Padding/2XS × Spacing/Padding/SM. */
    val DigitPaddingX = AmarSpace.S8
    val DigitPaddingY = AmarSpace.S12
    /** Gap between digits (`Layout/Gap Input`) and between digits and hint (Spacing/Gap/SM). */
    val Gap = AmarSpace.S8
}

/** `Info Box` layout tokens. */
object AmarInfoBoxTokens {
    /** Spacing/Padding/SM on all sides; bottom becomes Spacing/Padding/MD when the Button is shown. */
    val Padding = AmarSpace.S12
    val PaddingBottomWithButton = AmarSpace.S16
    /** Spacing/Gap/MD: icon ↔ content ↔ close, and text ↔ button. */
    val Gap = AmarSpace.S12
    /** Spacing/Gap/XS: label ↔ description. */
    val TextGap = AmarSpace.S4
}

/** Badge padding for one `Size` × `Supporting` combination (raw `Component/Badge/Size/...` values). */
class AmarBadgePadding internal constructor(val start: Dp, val top: Dp, val end: Dp, val bottom: Dp, val gap: Dp)

/** `Component/Badge/...` layout tokens, per Figma `Size`. */
class AmarBadgeSizeTokens internal constructor(
    val default: AmarBadgePadding,
    val close: AmarBadgePadding,
    val counter: AmarBadgePadding,
    val leftIcon: AmarBadgePadding,
)

object AmarBadgeTokens {
    val Small = AmarBadgeSizeTokens(
        default = AmarBadgePadding(8.dp, 3.dp, 8.dp, 3.dp, 2.dp),
        close = AmarBadgePadding(8.dp, 3.dp, 3.dp, 3.dp, 2.dp),
        counter = AmarBadgePadding(8.dp, 3.dp, 3.dp, 3.dp, 4.dp),
        leftIcon = AmarBadgePadding(6.dp, 2.dp, 8.dp, 2.dp, 2.dp),
    )
    val Medium = AmarBadgeSizeTokens(
        default = AmarBadgePadding(10.dp, 2.dp, 10.dp, 2.dp, 2.dp),
        close = AmarBadgePadding(10.dp, 2.dp, 4.dp, 2.dp, 2.dp),
        counter = AmarBadgePadding(10.dp, 2.dp, 4.dp, 2.dp, 6.dp),
        leftIcon = AmarBadgePadding(8.dp, 2.dp, 10.dp, 2.dp, 2.dp),
    )
    val Large = AmarBadgeSizeTokens(
        default = AmarBadgePadding(12.dp, 4.dp, 12.dp, 4.dp, 2.dp),
        close = AmarBadgePadding(12.dp, 4.dp, 6.dp, 4.dp, 2.dp),
        counter = AmarBadgePadding(12.dp, 4.dp, 6.dp, 4.dp, 6.dp),
        leftIcon = AmarBadgePadding(10.dp, 4.dp, 12.dp, 4.dp, 4.dp),
    )
    /** Close frame (16) with Spacing/Padding/3XS around the 12dp icon (`Badge/Icon Size`). */
    val CloseFrame = AmarSpace.S16
    val CloseIcon = AmarSpace.S12
    /** Counter circle and left icon size. */
    val CounterSize = AmarSpace.S16
    val LeftIconSize = AmarSpace.S16
}

/** `Component/Modal/Default/...` and `Element / Illustration Modal` tokens. */
object AmarModalTokens {
    /** Width of the Modal / Default component (the `Modal/Default/Width` variable says 342; agreed: 328). */
    val Width = 328.dp
    /** `Modal/Default/Padding` → Space/16. */
    val Padding = AmarSpace.S16
    /** `Modal/Default/Gap/Action` → Space/24: content ↔ buttons. */
    val GapAction = AmarSpace.S24
    /** `Modal/Default/Gap/Section` → Space/16: text ↔ slot. */
    val GapSection = AmarSpace.S16
    /** Title ↔ description. */
    val TextGap = AmarSpace.S8
    /** Height of the top edge fade shown once the content is scrolled (raw value in Figma). */
    val ScrollEdgeHeight = 40.dp
    /** Opacity of the edge fade layer (`Blur` rectangle, raw value in Figma). */
    const val ScrollEdgeOpacity = 0.9f
    /** Illustration (not fullscreen): padding top Spacing/Padding/LG, bottom Spacing/Gap/SM, image 80. */
    val IllustrationPaddingTop = AmarSpace.S24
    val IllustrationPaddingBottom = AmarSpace.S8
    val IllustrationSize = AmarSpace.S80
}

/** `Component/Tab/...` tokens. */
object AmarTabTokens {
    /** `Tab/Height`. */
    val Height = AmarSpace.S48
    /** `Tab/Container/Padding X` → Space/16. */
    val ContainerPaddingX = AmarSpace.S16
    /** `Tab/Indicator/Height`. */
    val IndicatorHeight = AmarSpace.S2
    /** Gap between icon and label inside a Tab Item (raw value in Figma; `Tab/Gap` = 4 is not used there). */
    val ContentGap = 6.dp
    /** Scrim (loading) placeholder inside a Tab Item (raw values in Figma). */
    val ScrimWidth = 109.dp
    val ScrimHeight = AmarSpace.S20
    val ScrimRadius = AmarRadius.R16
    /** Scrim gradient stops (raw, left → right): #F1EFEF, #F9F8F8 at 53%, #E7E5E5. */
    val ScrimColors = listOf(Color(0xFFF1EFEF), Color(0xFFF9F8F8), Color(0xFFE7E5E5))
    val ScrimStops = listOf(0f, 0.53f, 0.99f)
}

/** `Component/App Bar/...` layout tokens (the status bar part of the Figma frame is the system status bar). */
object AmarAppBarTokens {
    /** Header height (`Navigation/Top/Height`); Primary + Transparent Inverse grows to fit title + subtitle. */
    val HeaderHeight = AmarSpace.S56
    /** `App Bar/Padding Horizontal`, `App Bar/Padding Vertical` → Space/16. */
    val Padding = AmarSpace.S16
    /** `App Bar/Gap` → Space/8; Primary + Transparent Inverse uses 16. */
    val Gap = AmarSpace.S8
    val GapTransparentInverse = AmarSpace.S16
    /** Max title width in Figma (raw). */
    val TitleMaxWidth = 294.dp
    /** `Icon/Close Sheet/Padding` → 2: circular icon button (28 = 2 + 24 + 2). */
    val RoundButtonPadding = AmarSpace.S2
}

/** `Navigation Bar - Partner Dashboard` tokens (`Component/Navigation Bar/...`). */
object AmarNavigationBarTokens {
    /** Height of the component in Figma (the `Navigation Bar/Height` = 117 and `Navigation/Bottom/Height` = 64 variables are not used). */
    val Height = AmarSpace.S56
    /** `Navigation Bar/Item Padding Top`, item bottom padding (raw 4) and `Item Gap`. */
    val ItemPaddingTop = AmarSpace.S8
    val ItemPaddingBottom = AmarSpace.S4
    val ItemGap = AmarSpace.S4
    /** Top indicator of the active item (raw stroke weight in Figma). */
    val IndicatorHeight = AmarSpace.S4
}

/** `Card` layout tokens. Radius is `Radius/Container` ([AmarShapes.container]). */
object AmarCardTokens {
    /** `Card/Padding` → Spacing/Padding/MD → Space/16, all sides. */
    val Padding = AmarSpace.S16
    /** Spacing/Gap/LG → Space/16: heading ↔ slot ↔ button, and heading ↔ action chevron. */
    val Gap = AmarSpace.S16
    /** Icon slot ↔ text in the heading (raw value in Figma). */
    val HeadingGap = AmarSpace.S12
    /** Label ↔ description (raw value in Figma). */
    val TextGap = AmarSpace.S4
}

/** `Component/Upload/...` tokens for `Element / Progress Photos` (and later `Element / Upload`). */
object AmarUploadTokens {
    /** `Component/Upload/Padding/Vertical` (Uploaded, Uploaded Default, Failed). */
    val PaddingVertical = AmarSpace.S2
    /** Spacing/Gap/SM: leading icon or thumbnail ↔ content ↔ action, size ↔ status, text ↔ retry button. */
    val Gap = AmarSpace.S8
    /** Spacing/Gap/XS: file name ↔ progress, file name ↔ hint, spinner ↔ status text. */
    val ContentGap = AmarSpace.S4
    /** `Component/Upload/Gap/Progress`: progress bar ↔ size and status row. */
    val ProgressGap = 6.dp
    /** Progress bar height (raw value in Figma). */
    val ProgressHeight = 6.dp
    /** Thumbnail of `Status=Uploaded` and its corner radius (raw values in Figma). */
    val ThumbnailSize = AmarSpace.S56
    val ThumbnailRadius = AmarRadius.R8
    /** Spinner next to the status text (raw value in Figma). */
    val SpinnerSize = AmarSpace.S14

    /** `Element / Upload`: Spacing/Gap/MD label ↔ content, Spacing/Gap/SM section ↔ supporting label and helper ↔ dropzone. */
    val LabelGap = AmarSpace.S12
    val SectionGap = AmarSpace.S8
    /** Spacing/Gap/LG between file rows and between the rows and the dropzone (Multiple Files). */
    val FileGap = AmarSpace.S16
    /** Dropzone (Button instance override): `Radius/CTA`, dashed `Component/Upload/Dropzone/Border` 1.5 inside, dash 6 / gap 6 (raw values). */
    val DropzoneRadius = AmarRadius.R16
    val DropzoneBorderWidth = 1.5.dp
    val DropzoneDash = 6.dp
    val DropzoneDashGap = 6.dp
}

/** `Component/Bottom Sheet/...` tokens and the `Element / Sheet / …` sub-elements. Colours come from [AmarColors]. */
object AmarBottomSheetTokens {
    /** `Bottom Sheet/Height/Max`. */
    val MaxHeight = 600.dp
    /** `Bottom Sheet/Radius` → Radius/Container (top corners only). */
    val Radius = AmarRadius.R16
    /** `Padding/Horizontal`, `Padding/Top Handle` (top without illustration, and bottom), `Padding/Top` (below an illustration). */
    val PaddingHorizontal = AmarSpace.S16
    val PaddingTopHandle = AmarSpace.S24
    val PaddingTop = AmarSpace.S16
    val PaddingBottom = AmarSpace.S24
    /** `Gap/Content To Action`, `Gap/Heading` (title ↔ description), `Modal/Default/Gap/Section` (heading ↔ slot), Spacing/Gap/MD (inside the slot). */
    val GapContentToAction = AmarSpace.S24
    val GapHeading = AmarSpace.S8
    val GapSection = AmarSpace.S16
    val SlotGap = AmarSpace.S12
    /**
     * Handle bar: 34 × 5, `Handle Radius` → Radius/2. 6 from the top in every variant (design decision 2026-10-06, K6;
     * Figma still has 5 in three variants).
     */
    val HandleWidth = 34.dp
    val HandleHeight = 5.dp
    val HandleTop = 6.dp
    val HandleRadius = AmarRadius.R2
    /** `close sheet` 28, 16 from the top and right edge in Type=Default (raw position). */
    val CloseSize = 28.dp
    val CloseInset = AmarSpace.S16
    /** Spacer above the title in Type=Default without illustration, keeping the title clear of the close button (raw). */
    val DefaultTitleSpacer = AmarSpace.S20
    /** Spacing/Gap/LG: title ↔ close in Type=Compact. */
    val TitleCloseGap = AmarSpace.S16
    /** Scroll=True: heading bottom padding, slot bottom padding and action top padding (raw). */
    val ScrollHeadingPaddingBottom = AmarSpace.S8
    val ScrollSlotPaddingBottom = AmarSpace.S8
    val ScrollActionPaddingTop = AmarSpace.S16

    /** `Element / Sheet / Illustration` (Fullscreen=False): padding top Spacing/Padding/LG, bottom Spacing/Gap/SM, sides `Modal/Default/Padding`. */
    val IllustrationPaddingTop = AmarSpace.S24
    val IllustrationPaddingBottom = AmarSpace.S8
    val IllustrationPaddingHorizontal = AmarSpace.S16

    /**
     * `Element / Sheet / Option`: fixed height 52, gap 16 (raw). The radio is the Medium Selection Control scaled to 28
     * (design decision 2026-10-06, K7; Figma still shows 28.8). Figma also sets padding 14 top/bottom, which the fixed
     * height overrides.
     */
    val OptionHeight = 52.dp
    val OptionGap = AmarSpace.S16
    val OptionRadioSize = 28.dp

    /** `Element / Sheet / Selection`: padding Spacing/Padding/SM vertical, gap Spacing/Gap/LG; icon ↔ text 12, title ↔ description 4 (raw). */
    val SelectionPaddingVertical = AmarSpace.S12
    val SelectionGap = AmarSpace.S16
    val SelectionHeadingGap = AmarSpace.S12
    val SelectionTextGap = AmarSpace.S4
}

/** `Component/Chip/...` tokens. Colours come from [AmarColors] (see `AmarChip`). */
object AmarChipTokens {
    /** `Component/Chip/Padding X` → Spacing/Padding/SM. */
    val PaddingX = AmarSpace.S12
    /** Vertical padding 6: bound in Figma to `Component/Selection/Radius Medium` (a radius variable; the value is used). */
    val PaddingY = 6.dp
    /** `Component/Chip/Gap` → Spacing/Gap/XS (Default, Close, Delete; also icon ↔ label inside Dropdown). */
    val Gap = AmarSpace.S4
    /** `Component/Chip/Dropdown Gap` (Counter and Dropdown). */
    val DropdownGap = 6.dp
    /** `Component/Chip/Min Width`. */
    val MinWidth = AmarSpace.S80
    /** Counter circle (raw). */
    val CounterSize = AmarSpace.S20
    /** `Component/Chip/Disabled Opacity` = 50 %. */
    const val DisabledOpacity = 0.5f
    /** Close icon opacity in `Supporting=Close, Selected=False` only (raw layer opacity in Figma). */
    const val CloseOpacityUnselected = 0.65f
}

/**
 * Progress bar of the TL Dashboard (Figma "Sales - Partner Dashboard - Android" › Incentive hero › `Progress track`,
 * node 161:16014): height 8, fully rounded. Not in the DS library (extended component).
 */
object AmarProgressBarTokens {
    val Height = AmarSpace.S8
}
