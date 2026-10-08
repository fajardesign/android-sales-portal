package com.example.and_sales_portal.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.and_sales_portal.designsystem.components.feedback.AmarAlert
import com.example.and_sales_portal.designsystem.components.feedback.AmarAlertType
import com.example.and_sales_portal.designsystem.theme.AmarAlertTokens
import kotlinx.coroutines.delay

/**
 * Success toast (PRD v3: "every status change has … a success toast"): DS Alert (Success) at the bottom of the screen,
 * hidden after [durationMillis] or on dismiss.
 */
@Composable
fun BoxScope.Toast(message: String?, onDone: () -> Unit, type: AmarAlertType = AmarAlertType.Success, durationMillis: Long = 3_000) {
    if (message == null) return
    LaunchedEffect(message) {
        delay(durationMillis)
        onDone()
    }
    AmarAlert(
        message = message,
        type = type,
        onDismiss = onDone,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(start = AmarAlertTokens.MarginHorizontal, end = AmarAlertTokens.MarginHorizontal, bottom = AmarAlertTokens.MarginBottom),
    )
}

/** Screen content with a [Toast] on top. */
@Composable
fun WithToast(message: String?, onDone: () -> Unit, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        content()
        Toast(message, onDone)
    }
}
