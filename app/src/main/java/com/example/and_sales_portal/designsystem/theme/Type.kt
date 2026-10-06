package com.example.and_sales_portal.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.and_sales_portal.R

/** `Typography/Family/Base` = Inter (bundled static TTFs, Inter 4.1, SIL OFL 1.1). */
val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

/** `Typography/Letter Spacing/…` (Figma px → sp). */
private object LetterSpacing {
    val Tighter = (-0.4).sp
    val Tight = (-0.2).sp
    val Normal = 0.sp
}

/** Matches Figma text boxes: no extra font padding, line height centred and not trimmed. */
private fun interStyle(
    weight: FontWeight,
    size: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit = LetterSpacing.Normal,
) = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

/**
 * Figma local text styles (22). Property names mirror the Figma style names,
 * e.g. `Label/Button/MD` → [labelButtonMd], `Body/MD Medium` → [bodyMdMedium].
 */
@Immutable
data class AmarTypography(
    val display: TextStyle = interStyle(FontWeight.Bold, 48.sp, 52.sp, LetterSpacing.Tighter),
    val heading2xl: TextStyle = interStyle(FontWeight.Bold, 36.sp, 40.sp, LetterSpacing.Tighter),
    val headingXl: TextStyle = interStyle(FontWeight.SemiBold, 30.sp, 40.sp, LetterSpacing.Tighter),
    val headingLg: TextStyle = interStyle(FontWeight.SemiBold, 24.sp, 32.sp, LetterSpacing.Tight),
    val headingMd: TextStyle = interStyle(FontWeight.SemiBold, 20.sp, 28.sp, LetterSpacing.Tight),
    val headingSm: TextStyle = interStyle(FontWeight.SemiBold, 16.sp, 24.sp),
    /** Identical to [headingSm] in Figma; kept so names match `Heading/2SM`. */
    val heading2sm: TextStyle = interStyle(FontWeight.SemiBold, 16.sp, 24.sp),
    val bodyLg: TextStyle = interStyle(FontWeight.Normal, 16.sp, 24.sp),
    val bodyMd: TextStyle = interStyle(FontWeight.Normal, 14.sp, 24.sp),
    val bodyMdMedium: TextStyle = interStyle(FontWeight.Medium, 14.sp, 24.sp),
    val bodySm: TextStyle = interStyle(FontWeight.Normal, 12.sp, 20.sp),
    val labelLg: TextStyle = interStyle(FontWeight.Normal, 16.sp, 24.sp),
    val labelLgMedium: TextStyle = interStyle(FontWeight.Medium, 16.sp, 24.sp),
    val labelButtonLg: TextStyle = interStyle(FontWeight.SemiBold, 16.sp, 24.sp),
    val labelButtonMd: TextStyle = interStyle(FontWeight.SemiBold, 14.sp, 20.sp),
    val labelButtonSm: TextStyle = interStyle(FontWeight.SemiBold, 12.sp, 16.sp),
    val labelMd: TextStyle = interStyle(FontWeight.Normal, 14.sp, 20.sp),
    val labelMdMedium: TextStyle = interStyle(FontWeight.Medium, 14.sp, 20.sp),
    val labelMdSemibold: TextStyle = interStyle(FontWeight.SemiBold, 14.sp, 20.sp),
    val labelSm: TextStyle = interStyle(FontWeight.Medium, 12.sp, 16.sp),
    /** Identical to [labelMd] in Figma; kept so names match `Caption/MD`. */
    val captionMd: TextStyle = interStyle(FontWeight.Normal, 14.sp, 20.sp),
    val captionSm: TextStyle = interStyle(FontWeight.Normal, 12.sp, 16.sp),
)
