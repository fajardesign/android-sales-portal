package com.example.and_sales_portal.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Primitive colour palette: Figma variable collection **Foundation** (`Colour/...`).
 *
 * Internal on purpose. Components must use the semantic [AmarColors] via `AmarTheme.colors`.
 * Source: https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android
 */
internal object AmarPalette {
    // Colour/Brand/Primary
    val Primary50 = Color(0xFFF0FDFF)
    val Primary100 = Color(0xFFCCF7FA)
    val Primary200 = Color(0xFF99EEF4)
    val Primary300 = Color(0xFF66E2EB)
    val Primary400 = Color(0xFF33D0DC)
    val Primary500 = Color(0xFF009FAF)
    val Primary600 = Color(0xFF00808D)
    val Primary700 = Color(0xFF00707C)
    val Primary800 = Color(0xFF005A64)
    val Primary900 = Color(0xFF00484F)
    val Primary950 = Color(0xFF002F34)

    // Colour/Brand/Secondary
    val Secondary50 = Color(0xFFF4F5F6)
    val Secondary100 = Color(0xFFE6E8EA)
    val Secondary200 = Color(0xFFC3C7CA)
    val Secondary300 = Color(0xFF9BA2A7)
    val Secondary400 = Color(0xFF727D84)
    val Secondary500 = Color(0xFF5E6A72)
    val Secondary600 = Color(0xFF36454F)
    val Secondary700 = Color(0xFF2B373F)
    val Secondary800 = Color(0xFF1A232C)
    val Secondary900 = Color(0xFF0F172A)
    val Secondary950 = Color(0xFF000A14)

    // Colour/Neutral
    val Neutral0 = Color(0xFFFFFFFF)
    val Neutral25 = Color(0xFFFCFCFD)
    val Neutral50 = Color(0xFFF8FAFB)
    val Neutral100 = Color(0xFFF1F4F6)
    val Neutral200 = Color(0xFFE2E8EC)
    val Neutral300 = Color(0xFFCDD5DC)
    val Neutral400 = Color(0xFF98A2AE)
    val Neutral500 = Color(0xFF667281)
    val Neutral600 = Color(0xFF4B5563)
    val Neutral700 = Color(0xFF344054)
    val Neutral800 = Color(0xFF1F2937)
    val Neutral900 = Color(0xFF111827)
    val Neutral950 = Color(0xFF0B1220)
    val Neutral1000 = Color(0xFF030712)

    // Colour/Feedback/Error
    val Error50 = Color(0xFFFEF2F2)
    val Error100 = Color(0xFFFEE2E2)
    val Error500 = Color(0xFFEF4444)
    val Error600 = Color(0xFFDC2626)
    val Error700 = Color(0xFFB91C1C)
    val Error800 = Color(0xFF991B1B)

    // Colour/Feedback/Warning
    val Warning50 = Color(0xFFFEFCE8)
    val Warning100 = Color(0xFFFEF9C3)
    val Warning400 = Color(0xFFF2CF53)
    val Warning500 = Color(0xFFEAB308)
    val Warning600 = Color(0xFFCA8A04)
    val Warning700 = Color(0xFFA16207)
    val Warning800 = Color(0xFF854D0E)

    // Colour/Feedback/Success
    val Success50 = Color(0xFFF0FDF4)
    val Success100 = Color(0xFFDCFCE7)
    val Success500 = Color(0xFF22C55E)
    val Success600 = Color(0xFF16A34A)
    val Success700 = Color(0xFF15803D)
    val Success800 = Color(0xFF166534)

    // Colour/Feedback/Info
    val Info50 = Color(0xFFEFF6FF)
    val Info100 = Color(0xFFDBEAFE)
    val Info500 = Color(0xFF3B82F6)
    val Info600 = Color(0xFF2563EB)
    val Info700 = Color(0xFF1D4ED8)
    val Info800 = Color(0xFF1E40AF)

    // Colour/Base
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
    val Transparent = Color(0x00FFFFFF)
}

/** Foundation `Opacity/…`. */
object AmarOpacity {
    const val O0 = 0f
    const val O20 = 0.2f
    const val O40 = 0.4f
    const val O60 = 0.6f
    const val O80 = 0.8f
}

@Immutable
data class AmarBackgroundColors(
    val base: Color,
    val canvas: Color,
    val subtle: Color,
    val inverse: Color,
    val transparent: Color,
    val stepper: Color,
    val brandSubtle: Color,
)

@Immutable
data class AmarSurfaceColors(
    val base: Color,
    val raised: Color,
    val floating: Color,
    val inverse: Color,
    val modalBase: Color,
    val modalOverlay: Color,
)

/** Semantic `Content/…`. Rule: text uses Primary 700 or darker ([link]); Primary 500 is fill/icon only. */
@Immutable
data class AmarContentColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val quaternary: Color,
    val disabled: Color,
    val inverse: Color,
    val link: Color,
)

/** One `Action/<Type>` group. Fields absent in Figma for a type are [Color.Unspecified]. */
@Immutable
data class AmarActionColors(
    val background: Color = Color.Unspecified,
    val pressed: Color = Color.Unspecified,
    val disabled: Color = Color.Unspecified,
    val border: Color = Color.Unspecified,
    val borderPressed: Color = Color.Unspecified,
    val borderDisabled: Color = Color.Unspecified,
    val text: Color = Color.Unspecified,
    val textDisabled: Color = Color.Unspecified,
    val icon: Color = Color.Unspecified,
    val iconDisabled: Color = Color.Unspecified,
)

@Immutable
data class AmarActionGroup(
    val focus: Color,
    val primary: AmarActionColors,
    val primaryInverse: AmarActionColors,
    val secondary: AmarActionColors,
    val secondaryInverse: AmarActionColors,
    val ghost: AmarActionColors,
    val ghostInverse: AmarActionColors,
    val naked: AmarActionColors,
    val nakedInverse: AmarActionColors,
    val destructive: AmarActionColors,
    val destructiveInverse: AmarActionColors,
)

/** One `Feedback/<Type>` group (Success, Warning, Error, Info). */
@Immutable
data class AmarFeedbackColors(
    val background: Color,
    val surface: Color,
    val icon: Color,
    val iconSubtle: Color,
    val iconStrong: Color,
    val border: Color,
    val borderSubtle: Color,
    val text: Color,
    val textStrong: Color,
    val solidBackground: Color,
    val solidText: Color,
)

/** Semantic `Feedback/Neutral/…` (subset defined in Figma). */
@Immutable
data class AmarFeedbackNeutralColors(
    val surface: Color,
    val icon: Color,
    val border: Color,
    val text: Color,
)

@Immutable
data class AmarFeedbackGroup(
    val success: AmarFeedbackColors,
    val warning: AmarFeedbackColors,
    val error: AmarFeedbackColors,
    val info: AmarFeedbackColors,
    val neutral: AmarFeedbackNeutralColors,
)

@Immutable
data class AmarBorderColors(
    val default: Color,
    val subtle: Color,
    val strong: Color,
    val focus: Color,
    val disabled: Color,
)

@Immutable
data class AmarIconColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val quaternary: Color,
    val disabled: Color,
    val inverse: Color,
    val brand: Color,
    val danger: Color,
    val warning: Color,
    val positive: Color,
    val information: Color,
)

@Immutable
data class AmarOverlayColors(
    val default: Color,
    val modal: Color,
    val scrimDark: Color,
)

/** Semantic colours: Figma variable collection **Semantic**. Single mode ("Value"), no dark theme in Figma. */
@Immutable
data class AmarColors(
    val background: AmarBackgroundColors,
    val surface: AmarSurfaceColors,
    val content: AmarContentColors,
    val action: AmarActionGroup,
    val feedback: AmarFeedbackGroup,
    val border: AmarBorderColors,
    val icon: AmarIconColors,
    val overlay: AmarOverlayColors,
)

internal val AmarLightColors: AmarColors = with(AmarPalette) {
    AmarColors(
        background = AmarBackgroundColors(
            base = Neutral0,
            canvas = Neutral50,
            subtle = Secondary100,
            inverse = Secondary950,
            transparent = Transparent,
            stepper = Secondary800,
            brandSubtle = Primary50,
        ),
        surface = AmarSurfaceColors(
            base = Neutral0,
            raised = Neutral25,
            floating = Neutral50,
            inverse = Neutral800,
            modalBase = Color(0xFF262626),
            modalOverlay = Color(0xCCF5F5F5),
        ),
        content = AmarContentColors(
            primary = Neutral900,
            secondary = Neutral700,
            tertiary = Neutral500,
            quaternary = Neutral300,
            disabled = Neutral400,
            inverse = Neutral0,
            link = Primary700,
        ),
        action = AmarActionGroup(
            focus = Primary600,
            primary = AmarActionColors(
                background = Primary600, pressed = Primary700, disabled = Neutral200,
                text = Neutral0, textDisabled = Neutral400, icon = Neutral0, iconDisabled = Neutral400,
            ),
            primaryInverse = AmarActionColors(
                background = Primary400, pressed = Primary500, disabled = Neutral700,
                text = Primary950, textDisabled = Neutral500, icon = Primary950, iconDisabled = Neutral500,
            ),
            secondary = AmarActionColors(
                background = Transparent, pressed = Secondary200, disabled = Neutral200,
                border = Neutral500, borderPressed = Neutral200, borderDisabled = Neutral200,
                text = Secondary700, textDisabled = Neutral400, icon = Secondary700, iconDisabled = Neutral400,
            ),
            secondaryInverse = AmarActionColors(
                background = Color(0x00FFFFFF), pressed = Color(0x33FFFFFF), disabled = Neutral700,
                border = Neutral0, borderDisabled = Neutral400,
                text = Neutral0, textDisabled = Neutral500, icon = Neutral0, iconDisabled = Neutral500,
            ),
            ghost = AmarActionColors(
                background = Transparent, pressed = Neutral200, disabled = Neutral200,
                text = Secondary700, textDisabled = Neutral400, icon = Secondary700, iconDisabled = Neutral400,
            ),
            ghostInverse = AmarActionColors(
                background = Color(0x00FFFFFF), pressed = Color(0x33FFFFFF),
                text = Neutral0, textDisabled = Neutral500, icon = Neutral0, iconDisabled = Neutral500,
            ),
            naked = AmarActionColors(
                pressed = Primary900,
                text = Primary700, textDisabled = Neutral400, icon = Primary700, iconDisabled = Neutral400,
            ),
            // Exception to the Primary-700 text rule: used only on dark (Inverse) backgrounds.
            nakedInverse = AmarActionColors(
                pressed = Primary500,
                text = Primary400, textDisabled = Neutral500, icon = Primary400, iconDisabled = Neutral500,
            ),
            destructive = AmarActionColors(
                background = Error600, pressed = Error800, disabled = Neutral200,
                text = Neutral0, textDisabled = Neutral400, icon = Neutral0, iconDisabled = Neutral400,
            ),
            destructiveInverse = AmarActionColors(
                background = Error600, pressed = Error500, disabled = Neutral700,
                text = Neutral0, textDisabled = Neutral500, icon = Neutral0, iconDisabled = Neutral500,
            ),
        ),
        feedback = AmarFeedbackGroup(
            success = AmarFeedbackColors(
                background = Success50, surface = Success100,
                icon = Success500, iconSubtle = Color(0xFF51BE79), iconStrong = Success700,
                border = Success600, borderSubtle = Color(0xFFB4EAC8),
                text = Success700, textStrong = Success800,
                solidBackground = Success600, solidText = Neutral0,
            ),
            warning = AmarFeedbackColors(
                background = Warning50, surface = Warning100,
                icon = Warning500, iconSubtle = Color(0xFFDAAB3D), iconStrong = Warning700,
                border = Warning600, borderSubtle = Color(0xFFF4E39D),
                text = Warning700, textStrong = Warning800,
                solidBackground = Warning400, solidText = Warning800,
            ),
            error = AmarFeedbackColors(
                background = Error50, surface = Error100,
                icon = Error500, iconSubtle = Color(0xFFE65E5E), iconStrong = Error700,
                border = Error600, borderSubtle = Color(0xFFF7BCBC),
                text = Error700, textStrong = Error800,
                solidBackground = Error600, solidText = Neutral0,
            ),
            info = AmarFeedbackColors(
                background = Info50, surface = Info100,
                icon = Info500, iconSubtle = Color(0xFF5C8CF1), iconStrong = Info700,
                border = Info600, borderSubtle = Color(0xFFB7CFFA),
                text = Info700, textStrong = Info800,
                solidBackground = Info600, solidText = Neutral0,
            ),
            neutral = AmarFeedbackNeutralColors(
                surface = Neutral100, icon = Neutral500, border = Neutral600, text = Neutral700,
            ),
        ),
        border = AmarBorderColors(
            default = Neutral200,
            subtle = Neutral100,
            strong = Neutral300,
            focus = Primary600,
            disabled = Neutral100,
        ),
        icon = AmarIconColors(
            primary = Neutral900,
            secondary = Neutral700,
            tertiary = Neutral500,
            quaternary = Neutral300,
            disabled = Neutral400,
            inverse = Neutral0,
            brand = Primary600,
            danger = Error500,
            warning = Warning500,
            positive = Success500,
            information = Info500,
        ),
        overlay = AmarOverlayColors(
            default = Color(0x660B1220),
            modal = Color(0x3B29293A),
            scrimDark = Color(0x4D000000),
        ),
    )
}
