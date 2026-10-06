package com.example.and_sales_portal.designsystem.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/** Figma variant property `State`. */
enum class AmarOverlayState { Default, Modal }

/**
 * Overlay: full-screen scrim behind sheets and modals, optionally with a loading spinner.
 * It consumes touches so the content behind cannot be used.
 *
 * Figma: [Overlay 163:334](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android?node-id=163-334)
 * (State = Default / Modal).
 *
 * @param state Default = `Overlay/Default` (#0B1220, 40%); Modal = `Overlay/Modal` (#29293A, 23%).
 * @param spinner Figma `Spinner` (Default only): a centred 56dp spinner.
 * @param spinnerColor The Figma instance overrides the spinner colour on a hidden layer, so the value cannot be
 * read. Defaults to `Icon/Inverse` pending confirmation by design.
 * @param onClick called on a tap on the scrim (e.g. to dismiss a sheet); `null` only blocks touches.
 */
@Composable
fun AmarOverlay(
    modifier: Modifier = Modifier,
    state: AmarOverlayState = AmarOverlayState.Default,
    spinner: Boolean = false,
    spinnerColor: Color = AmarTheme.colors.icon.inverse,
    onClick: (() -> Unit)? = null,
) {
    val colors = AmarTheme.colors.overlay
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (state == AmarOverlayState.Modal) colors.modal else colors.default)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick ?: {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (spinner && state == AmarOverlayState.Default) {
            AmarSpinner(color = spinnerColor, size = AmarSpace.S56)
        }
    }
}

/** Both Figma states, plus the Spinner option. */
@Preview(name = "Overlay", widthDp = 360, heightDp = 200)
@Composable
private fun AmarOverlayPreview() {
    AmarTheme {
        Row(Modifier.background(AmarTheme.colors.background.base)) {
            AmarOverlay(Modifier.size(AmarSpace.S128))
            AmarOverlay(Modifier.size(AmarSpace.S128), spinner = true)
            AmarOverlay(Modifier.size(AmarSpace.S96), state = AmarOverlayState.Modal)
        }
    }
}
