package com.example.and_sales_portal.ui.auth

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.auth.LoginError
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.designsystem.components.brand.AmarLogo
import com.example.and_sales_portal.designsystem.components.feedback.AmarBottomSheet
import com.example.and_sales_portal.designsystem.components.feedback.AmarBottomSheetType
import com.example.and_sales_portal.designsystem.components.feedback.AmarOverlayState
import com.example.and_sales_portal.designsystem.components.feedback.AmarSpinner
import com.example.and_sales_portal.designsystem.components.inputs.AmarTextField
import com.example.and_sales_portal.designsystem.components.inputs.AmarTextFieldType
import com.example.and_sales_portal.designsystem.theme.AmarAuthTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme

/**
 * Spec: MLG-00 Splash (PRD v3 "Login on mobile"; Figma "01 | Splash Screen | Loading"): logo, product name,
 * "Memuat..." and the app version while the stored session is checked.
 * The Tunaiku logotype of Figma is not in the DS Lib Android yet; only the Amar Bank logo is shown.
 */
@Composable
fun SplashScreen(version: String) {
    val c = AmarTheme.colors
    val t = AmarAuthTokens
    Column(Modifier.fillMaxSize().background(c.background.base).windowInsetsPadding(WindowInsets.safeDrawing)) {
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = t.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(t.SplashGap, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AmarLogo()
            Text(stringResource(R.string.auth_product_title), color = c.content.primary, style = AmarTheme.typography.headingMd, textAlign = TextAlign.Center)
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(t.SplashLoadingGap)) {
                AmarSpinner(c.icon.brand)
                Text(stringResource(R.string.auth_loading), color = c.content.secondary, style = AmarTheme.typography.captionMd)
            }
        }
        VersionFooter(version)
    }
}

/** State of the login form (MLG-01). */
data class LoginFormState(
    val identifier: String = "",
    val password: String = "",
    val loading: Boolean = false,
    /** Inline error under the password (INVALID). */
    val invalid: Boolean = false,
) {
    /** PRD: "Masuk" is enabled once both fields are filled (prevents BAD_REQUEST). */
    val canSubmit: Boolean get() = identifier.isNotBlank() && password.isNotEmpty() && !loading
}

/**
 * Spec: MLG-01 Login (PRD v3 "Login on mobile"; Figma "02 | Login"). Field label follows the PRD ("Email atau Username";
 * Figma's "Email atau No. Handphone" is overridden because phone is contact-only).
 */
@Composable
fun LoginScreen(
    state: LoginFormState,
    version: String,
    onIdentifier: (String) -> Unit,
    onPassword: (String) -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    val c = AmarTheme.colors
    val t = AmarAuthTokens
    Column(
        Modifier
            .fillMaxSize()
            .background(c.background.base)
            .windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.ime)),
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = t.ContentPadding, end = t.ContentPadding, top = t.ContentPaddingTop),
            verticalArrangement = Arrangement.spacedBy(t.LogoToForm),
        ) {
            AmarLogo()
            Column(verticalArrangement = Arrangement.spacedBy(t.SectionGap)) {
                Column(verticalArrangement = Arrangement.spacedBy(t.FieldGap)) {
                    Column(verticalArrangement = Arrangement.spacedBy(t.IntroGap)) {
                        Text(stringResource(R.string.auth_product_title), color = c.content.primary, style = AmarTheme.typography.headingMd)
                        Text(stringResource(R.string.auth_login_subtitle), color = c.content.secondary, style = AmarTheme.typography.bodySm)
                    }
                    AmarTextField(
                        value = state.identifier,
                        onValueChange = onIdentifier,
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(R.string.auth_identifier),
                        placeholder = stringResource(R.string.auth_identifier_placeholder),
                        enabled = !state.loading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    )
                    AmarTextField(
                        value = state.password,
                        onValueChange = onPassword,
                        modifier = Modifier.fillMaxWidth(),
                        type = AmarTextFieldType.Password,
                        label = stringResource(R.string.auth_password),
                        placeholder = stringResource(R.string.auth_password_placeholder),
                        hint = if (state.invalid) stringResource(R.string.auth_error_invalid) else null,
                        error = state.invalid,
                        enabled = !state.loading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(t.ActionGap)) {
                    AmarButton(
                        stringResource(R.string.auth_login),
                        onSubmit,
                        Modifier.fillMaxWidth(),
                        size = AmarButtonSize.Large,
                        enabled = state.canSubmit || state.loading,
                        loading = state.loading,
                    )
                    AmarButton(
                        stringResource(R.string.auth_forgot_password),
                        onForgotPassword,
                        Modifier.fillMaxWidth(),
                        type = AmarButtonType.Naked,
                        size = AmarButtonSize.Large,
                        enabled = !state.loading,
                    )
                }
            }
        }
        VersionFooter(version)
    }
}

@Composable
private fun VersionFooter(version: String) {
    Text(
        stringResource(R.string.auth_version, version),
        Modifier.fillMaxWidth().padding(AmarAuthTokens.FooterPadding),
        AmarTheme.colors.content.secondary,
        style = AmarTheme.typography.captionMd,
        textAlign = TextAlign.Center,
    )
}

/** What a login problem shows (Figma "02 | Login | Error | …" sheets; messages from the PRD). */
class AuthSheet(
    @StringRes val title: Int,
    @StringRes val message: Int,
    @DrawableRes val illustration: Int?,
    @StringRes val button: Int,
    /** "Coba Lagi" repeats the login. */
    val retry: Boolean,
)

/** Sheet for each [LoginError] except [LoginError.Invalid], which is inline. */
fun authSheetFor(error: LoginError): AuthSheet = when (error) {
    LoginError.Locked -> AuthSheet(R.string.auth_error_locked_title, R.string.auth_error_locked, R.drawable.ds_ill_tunaiku_lock, R.string.auth_understood, false)
    LoginError.Disabled -> AuthSheet(R.string.auth_error_disabled_title, R.string.auth_error_disabled, R.drawable.ds_ill_tunaiku_task_stop, R.string.auth_understood, false)
    LoginError.NotActivated -> AuthSheet(R.string.auth_error_not_activated_title, R.string.auth_error_not_activated, R.drawable.ds_ill_tunaiku_task_stop, R.string.auth_understood, false)
    LoginError.ForbiddenPlatform -> AuthSheet(R.string.auth_error_forbidden_title, R.string.auth_error_forbidden, R.drawable.ds_ill_tunaiku_task_stop, R.string.auth_understood, false)
    LoginError.Offline -> AuthSheet(R.string.auth_error_offline_title, R.string.auth_error_offline, R.drawable.ds_ill_tunaiku_connection_error, R.string.auth_retry, true)
    LoginError.Timeout -> AuthSheet(R.string.auth_error_timeout_title, R.string.auth_error_timeout, R.drawable.ds_ill_tunaiku_task_stop, R.string.auth_retry, true)
    LoginError.Server, LoginError.BadRequest, LoginError.Invalid ->
        AuthSheet(R.string.auth_error_server_title, R.string.auth_error_server, R.drawable.ds_ill_tunaiku_internal_system_error, R.string.auth_retry, true)
}

/** Session that could not be refreshed (Figma "02 | Login | Error | Refresh Failed"). */
val expiredSheet = AuthSheet(R.string.auth_error_expired_title, R.string.auth_error_expired, null, R.string.auth_login_again, false)

/** Error sheet over the login (Bottom Sheet Default with illustration, Overlay Default as in the Figma login frames). */
@Composable
fun AuthErrorSheet(sheet: AuthSheet, onButton: () -> Unit, onDismiss: () -> Unit) {
    AmarBottomSheet(
        onDismissRequest = onDismiss,
        scrim = AmarOverlayState.Default,
        title = stringResource(sheet.title),
        description = stringResource(sheet.message),
        illustration = {
            if (sheet.illustration != null) {
                Image(painterResource(sheet.illustration), null)
            } else {
                Box(Modifier.size(AmarTheme.iconSizes.xl), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ds_ic_refresh_24), null, tint = AmarTheme.colors.icon.brand)
                }
            }
        },
        showHandle = false,
        primaryLabel = stringResource(sheet.button),
        onPrimaryClick = onButton,
    )
}

/** PRD: "Lupa password?" opens a bottom sheet "Hubungi Admin untuk mengatur ulang password Anda." */
@Composable
fun ForgotPasswordSheet(onDismiss: () -> Unit) {
    AmarBottomSheet(
        onDismissRequest = onDismiss,
        type = AmarBottomSheetType.Compact,
        title = stringResource(R.string.auth_forgot_password),
        description = stringResource(R.string.auth_forgot_password_message),
        showClose = true,
        primaryLabel = stringResource(R.string.auth_understood),
        onPrimaryClick = onDismiss,
    )
}
