package com.example.and_sales_portal.designsystem.components.actions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.feedback.AmarSpinner
import com.example.and_sales_portal.designsystem.theme.AmarActionColors
import com.example.and_sales_portal.designsystem.theme.AmarButtonSizeTokens
import com.example.and_sales_portal.designsystem.theme.AmarButtonTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/** Figma variant property `Type`. [Error] uses the `Destructive` tokens. */
enum class AmarButtonType { Primary, Secondary, Ghost, Naked, Error }

/** Figma variant property `Size`. */
enum class AmarButtonSize { Large, Medium, Small }

private class ButtonColors(
    val container: Color,
    val border: Color,
    val content: Color,
    val spinner: Color,
)

@Composable
private fun actionGroup(type: AmarButtonType, inverse: Boolean): AmarActionColors {
    val action = AmarTheme.colors.action
    return when (type) {
        AmarButtonType.Primary -> if (inverse) action.primaryInverse else action.primary
        AmarButtonType.Secondary -> if (inverse) action.secondaryInverse else action.secondary
        AmarButtonType.Ghost -> if (inverse) action.ghostInverse else action.ghost
        AmarButtonType.Naked -> if (inverse) action.nakedInverse else action.naked
        AmarButtonType.Error -> if (inverse) action.destructiveInverse else action.destructive
    }
}

/** Colour rules per Figma variant (`Component/Button/<Type>[/Inverse]/...`). */
@Composable
private fun buttonColors(
    type: AmarButtonType,
    inverse: Boolean,
    enabled: Boolean,
    loading: Boolean,
    pressed: Boolean,
): ButtonColors {
    val g = actionGroup(type, inverse)
    val none = AmarTheme.colors.background.transparent
    val naked = type == AmarButtonType.Naked
    val outlined = type == AmarButtonType.Secondary
    return when {
        !enabled -> ButtonColors(
            container = when {
                naked -> none
                // Figma: Ghost/Disabled/Inverse uses Secondary/Inverse/Disabled/Background.
                type == AmarButtonType.Ghost && inverse -> AmarTheme.colors.action.secondaryInverse.disabled
                else -> g.disabled
            },
            border = if (outlined) g.borderDisabled else none,
            content = g.textDisabled,
            spinner = g.iconDisabled,
        )
        loading -> ButtonColors(
            // Figma: Loading uses the Pressed background for Primary, Error and Ghost/Inverse only.
            container = when {
                naked -> none
                type == AmarButtonType.Primary || type == AmarButtonType.Error -> g.pressed
                type == AmarButtonType.Ghost && inverse -> g.pressed
                else -> g.background
            },
            border = if (outlined) g.border else none,
            content = g.text,
            spinner = g.icon,
        )
        else -> ButtonColors(
            container = when {
                naked -> none
                pressed -> g.pressed
                else -> g.background
            },
            border = when {
                !outlined -> none
                pressed && g.borderPressed != Color.Unspecified -> g.borderPressed
                else -> g.border
            },
            content = if (naked && pressed) g.pressed else g.text,
            spinner = g.icon,
        )
    }
}

@Composable
private fun sizeTokens(size: AmarButtonSize): AmarButtonSizeTokens = when (size) {
    AmarButtonSize.Large -> AmarButtonTokens.Large
    AmarButtonSize.Medium -> AmarButtonTokens.Medium
    AmarButtonSize.Small -> AmarButtonTokens.Small
}

@Composable
private fun iconSize(size: AmarButtonSize): Dp = when (size) {
    AmarButtonSize.Large -> AmarTheme.iconSizes.lg
    AmarButtonSize.Medium -> AmarTheme.iconSizes.md
    AmarButtonSize.Small -> AmarTheme.iconSizes.sm
}

@Composable
private fun labelStyle(size: AmarButtonSize): TextStyle = when (size) {
    AmarButtonSize.Large -> AmarTheme.typography.labelButtonLg
    AmarButtonSize.Medium -> AmarTheme.typography.labelButtonMd
    AmarButtonSize.Small -> AmarTheme.typography.labelButtonSm
}

/**
 * Button.
 *
 * Figma: [Button 60:337](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=60-337)
 * (Type × Size × State × Icon Only × Inverse = 180 variants).
 *
 * Width hugs the content (Figma's fixed 140 is a sample width); use `Modifier.fillMaxWidth()` for full-width
 * buttons. Icon Only buttons are square (`Width Icon` = `Height`). Naked buttons have no container or padding.
 * While [loading], the button keeps its size, shows the spinner and ignores clicks.
 * Pressed colours come from the Figma `... Pressed` tokens (there is no Pressed variant).
 *
 * @param label Figma `Label`. `null` makes an Icon Only button (Figma `Icon Only=True`) showing [icon].
 * @param type Figma `Type`.
 * @param size Figma `Size`.
 * @param enabled `false` = Figma `State=Disabled`.
 * @param loading `true` = Figma `State=Loading`.
 * @param inverse Figma `Inverse`: for dark backgrounds.
 * @param leftIcon Figma `Left Icon` + `Change Left`; `null` hides it.
 * @param rightIcon Figma `Right Icon` + `Change Right`; `null` hides it.
 * @param icon Figma `Change Icon` for Icon Only buttons.
 * @param contentDescription accessibility label for Icon Only buttons.
 */
@Composable
fun AmarButton(
    label: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: AmarButtonType = AmarButtonType.Primary,
    size: AmarButtonSize = AmarButtonSize.Large,
    enabled: Boolean = true,
    loading: Boolean = false,
    inverse: Boolean = false,
    leftIcon: Painter? = null,
    rightIcon: Painter? = null,
    icon: Painter = painterResource(R.drawable.ds_ic_default_24),
    contentDescription: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val colors = buttonColors(type, inverse, enabled, loading, pressed)
    val tokens = sizeTokens(size)
    val iconSize = iconSize(size)
    val naked = type == AmarButtonType.Naked
    val iconOnly = label == null
    val shape: Shape = if (naked) AmarTheme.shapes.none else AmarTheme.shapes.cta

    val container = when {
        naked -> Modifier
        iconOnly -> Modifier.size(tokens.height)
        else -> Modifier.height(tokens.height).padding(horizontal = tokens.paddingX)
    }
    Box(
        modifier = modifier
            .widthIn(max = AmarButtonTokens.MaxWidth)
            .clip(shape)
            .background(colors.container, shape)
            .then(
                if (type == AmarButtonType.Secondary) {
                    Modifier.border(AmarTheme.borderWidths.subtle, colors.border, shape)
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !loading,
                role = Role.Button,
                onClick = onClick,
            )
            .then(container),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.alpha(if (loading) 0f else 1f),
            horizontalArrangement = Arrangement.spacedBy(tokens.gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (label == null) {
                Icon(icon, contentDescription, Modifier.size(iconSize), tint = colors.content)
            } else {
                leftIcon?.let { Icon(it, null, Modifier.size(iconSize), tint = colors.content) }
                Text(label, style = labelStyle(size), color = colors.content, maxLines = 1)
                rightIcon?.let { Icon(it, null, Modifier.size(iconSize), tint = colors.content) }
            }
        }
        if (loading) {
            AmarSpinner(color = colors.spinner, size = iconSize)
        }
    }
}

private class ButtonTypeProvider : PreviewParameterProvider<Pair<AmarButtonType, Boolean>> {
    override val values = AmarButtonType.entries.flatMap { type ->
        listOf(type to false, type to true)
    }.asSequence()
}

/**
 * One preview per Figma `Type` × `Inverse` (10). Each shows Size × State × Icon Only (18),
 * covering all 180 Figma variants. Default icons on, like Figma.
 */
@Preview(name = "Button", widthDp = 520)
@Composable
private fun AmarButtonPreview(
    @PreviewParameter(ButtonTypeProvider::class) variant: Pair<AmarButtonType, Boolean>,
) {
    val (type, inverse) = variant
    AmarTheme {
        val background = if (inverse) AmarTheme.colors.background.inverse else AmarTheme.colors.background.base
        val defaultIcon = painterResource(R.drawable.ds_ic_default_24)
        Column(
            modifier = Modifier
                .background(background)
                .padding(AmarSpace.S16),
            verticalArrangement = Arrangement.spacedBy(AmarSpace.S12),
        ) {
            AmarButtonSize.entries.forEach { size ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AmarSpace.S12),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listOf(true to false, false to false, true to true).forEach { (enabled, loading) ->
                        AmarButton(
                            label = "Button", onClick = {}, type = type, size = size,
                            enabled = enabled, loading = loading, inverse = inverse,
                            leftIcon = defaultIcon, rightIcon = defaultIcon,
                        )
                    }
                    listOf(true to false, false to false, true to true).forEach { (enabled, loading) ->
                        AmarButton(
                            label = null, onClick = {}, type = type, size = size,
                            enabled = enabled, loading = loading, inverse = inverse,
                        )
                    }
                }
            }
        }
    }
}
