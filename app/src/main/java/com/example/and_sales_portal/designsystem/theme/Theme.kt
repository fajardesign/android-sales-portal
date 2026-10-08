package com.example.and_sales_portal.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalAmarColors = staticCompositionLocalOf { AmarLightColors }
internal val LocalAmarTypography = staticCompositionLocalOf { AmarTypography() }
internal val LocalAmarShapes = staticCompositionLocalOf { AmarShapes() }
internal val LocalAmarSpacing = staticCompositionLocalOf { AmarSpacing() }
internal val LocalAmarBorderWidths = staticCompositionLocalOf { AmarBorderWidths() }
internal val LocalAmarIconSizes = staticCompositionLocalOf { AmarIconSizes() }
internal val LocalAmarElevation = staticCompositionLocalOf { AmarElevation() }

/**
 * Root theme of the Amar Bank Android design system
 * ([Figma: Internal Project DS Lib - Android](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android)).
 *
 * Light only: every Figma variable collection has a single mode. Also maps the tokens onto
 * [MaterialTheme] so stock Material 3 composables do not fall back to the default purple palette.
 */
@Composable
fun AmarTheme(content: @Composable () -> Unit) {
    val colors = AmarLightColors
    val typography = AmarTypography()
    CompositionLocalProvider(
        LocalAmarColors provides colors,
        LocalAmarTypography provides typography,
        LocalAmarShapes provides AmarShapes(),
        LocalAmarSpacing provides AmarSpacing(),
        LocalAmarBorderWidths provides AmarBorderWidths(),
        LocalAmarIconSizes provides AmarIconSizes(),
        LocalAmarElevation provides AmarElevation(),
    ) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = colors.action.primary.background,
                onPrimary = colors.action.primary.text,
                secondary = colors.action.secondary.text,
                onSecondary = colors.content.inverse,
                background = colors.background.base,
                onBackground = colors.content.primary,
                surface = colors.surface.base,
                onSurface = colors.content.primary,
                surfaceVariant = colors.background.subtle,
                onSurfaceVariant = colors.content.secondary,
                outline = colors.border.strong,
                outlineVariant = colors.border.default,
                error = colors.feedback.error.border,
                onError = colors.feedback.error.solidText,
                scrim = colors.overlay.default,
            ),
            typography = Typography(
                displayLarge = typography.display,
                headlineLarge = typography.heading2xl,
                headlineMedium = typography.headingXl,
                headlineSmall = typography.headingLg,
                titleLarge = typography.headingMd,
                titleMedium = typography.headingSm,
                titleSmall = typography.labelMdSemibold,
                bodyLarge = typography.bodyLg,
                bodyMedium = typography.bodyMd,
                bodySmall = typography.bodySm,
                labelLarge = typography.labelButtonMd,
                labelMedium = typography.labelMdMedium,
                labelSmall = typography.labelSm,
            ),
            content = content,
        )
    }
}

/** Token accessors: `AmarTheme.colors.content.primary`, `AmarTheme.typography.bodyMd`, … */
object AmarTheme {
    val colors: AmarColors
        @Composable @ReadOnlyComposable get() = LocalAmarColors.current
    val typography: AmarTypography
        @Composable @ReadOnlyComposable get() = LocalAmarTypography.current
    val shapes: AmarShapes
        @Composable @ReadOnlyComposable get() = LocalAmarShapes.current
    val spacing: AmarSpacing
        @Composable @ReadOnlyComposable get() = LocalAmarSpacing.current
    val borderWidths: AmarBorderWidths
        @Composable @ReadOnlyComposable get() = LocalAmarBorderWidths.current
    val iconSizes: AmarIconSizes
        @Composable @ReadOnlyComposable get() = LocalAmarIconSizes.current
    val elevation: AmarElevation
        @Composable @ReadOnlyComposable get() = LocalAmarElevation.current
}
