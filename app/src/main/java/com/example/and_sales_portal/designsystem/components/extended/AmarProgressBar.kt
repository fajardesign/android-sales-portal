package com.example.and_sales_portal.designsystem.components.extended

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.example.and_sales_portal.designsystem.theme.AmarProgressBarTokens
import com.example.and_sales_portal.designsystem.theme.AmarSpace
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/**
 * Horizontal progress bar (extended component: not in
 * [Internal Project DS Lib - Android](https://www.figma.com/design/NCtvxBNe1WdwkKeCpSeT0C/Internal-Project-DS-Lib---Android)).
 * Shape from Figma "Sales - Partner Dashboard -
 * Android" › 01 | Team Leader Dashboard › Incentive hero › Progress track:
 * [161:16014](https://www.figma.com/design/6ezrFIAonEuXZAgMhAbBlY/Sales---Partner-Dashboard---Android?node-id=161-16014).
 * The Figma colours there are raw values (#E6E8EA track, #168B5B value); here they map to the DS tokens
 * `Background/Subtle` and `Feedback/Success/Icon` (decision to report to the designer).
 *
 * @param progress 0..1 (clamped).
 * @param color Value colour; defaults to Feedback/Success/Icon.
 */
@Composable
fun AmarProgressBar(progress: Float, modifier: Modifier = Modifier, color: Color = AmarTheme.colors.feedback.success.icon) {
    val p = progress.coerceIn(0f, 1f)
    val shape = AmarTheme.shapes.full
    Box(
        modifier
            .fillMaxWidth()
            .height(AmarProgressBarTokens.Height)
            .clip(shape)
            .background(AmarTheme.colors.background.subtle)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(p, 0f..1f) },
    ) {
        Box(Modifier.fillMaxWidth(p).fillMaxHeight().clip(shape).background(color))
    }
}

/** Empty, partial, full; success and brand colours. */
@Preview(name = "Progress bar", widthDp = 328)
@Composable
private fun AmarProgressBarPreview() {
    AmarTheme {
        Column(Modifier.background(AmarTheme.colors.background.base).padding(AmarSpace.S16), verticalArrangement = Arrangement.spacedBy(AmarSpace.S16)) {
            AmarProgressBar(0f)
            AmarProgressBar(0.67f)
            AmarProgressBar(1f)
            AmarProgressBar(0.4f, color = AmarTheme.colors.icon.brand)
        }
    }
}
