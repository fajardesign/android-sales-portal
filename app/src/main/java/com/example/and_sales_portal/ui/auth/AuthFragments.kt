package com.example.and_sales_portal.ui.auth

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.os.bundleOf
import com.example.and_sales_portal.BuildConfig
import com.example.and_sales_portal.data.auth.LoginError
import com.example.and_sales_portal.data.auth.LoginResult
import com.example.and_sales_portal.data.auth.RestoreResult
import com.example.and_sales_portal.ui.common.ComposeFragment
import com.example.and_sales_portal.ui.shell.ShellNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Spec: MLG-00 Splash. Checks the stored session (at most about 2 s, PRD) and routes to Beranda or Login. */
class SplashFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        SplashScreen(BuildConfig.VERSION_NAME)
        LaunchedEffect(Unit) {
            val started = System.currentTimeMillis()
            val result = withTimeoutOrNull(MAX_SPLASH_MS) { container.auth.restore() }
                ?: container.auth.session.value?.let { RestoreResult.SignedIn(it) }
                ?: RestoreResult.NoSession
            delay((MIN_SPLASH_MS - (System.currentTimeMillis() - started)).coerceAtLeast(0))
            val nav = ShellNavigator(this@SplashFragment)
            when (result) {
                is RestoreResult.SignedIn -> nav.toHome(result.session.role)
                RestoreResult.NoSession -> nav.toLogin(expired = false)
                RestoreResult.Expired -> nav.toLogin(expired = true)
            }
        }
    }

    private companion object {
        const val MAX_SPLASH_MS = 2_000L
        const val MIN_SPLASH_MS = 600L
    }
}

/** Spec: MLG-01 Login with the PRD error handling (inline INVALID, sheets for the rest). */
class LoginFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        var form by remember { mutableStateOf(LoginFormState()) }
        var error by remember { mutableStateOf<LoginError?>(null) }
        var expired by rememberSaveable { mutableStateOf(arguments?.getBoolean(ARG_EXPIRED) == true) }
        var forgot by rememberSaveable { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        fun submit() {
            if (!form.canSubmit) return
            form = form.copy(loading = true, invalid = false)
            scope.launch {
                when (val result = container.auth.login(form.identifier, form.password)) {
                    is LoginResult.Success -> {
                        // FR-PA-009: a registration draft of another account on this device is deleted.
                        container.demo.deleteDraftsExcept(result.session.user.id)
                        ShellNavigator(this@LoginFragment).toHome(result.session.role)
                    }
                    is LoginResult.Failure -> {
                        form = form.copy(loading = false, invalid = result.error == LoginError.Invalid)
                        if (result.error != LoginError.Invalid) error = result.error
                    }
                }
            }
        }

        LoginScreen(
            state = form,
            version = BuildConfig.VERSION_NAME,
            onIdentifier = { form = form.copy(identifier = it, invalid = false) },
            onPassword = { form = form.copy(password = it, invalid = false) },
            onSubmit = ::submit,
            onForgotPassword = { forgot = true },
        )
        error?.let { e ->
            val sheet = authSheetFor(e)
            AuthErrorSheet(sheet, onButton = { error = null; if (sheet.retry) submit() }, onDismiss = { error = null })
        }
        if (expired) AuthErrorSheet(expiredSheet, onButton = { expired = false }, onDismiss = { expired = false })
        if (forgot) ForgotPasswordSheet { forgot = false }
    }

    companion object {
        const val ARG_EXPIRED = "expired"
        fun args(expired: Boolean): Bundle = bundleOf(ARG_EXPIRED to expired)
    }
}
