package com.example.and_sales_portal.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import com.example.and_sales_portal.data.auth.AccessInfo
import com.example.and_sales_portal.data.auth.AppUser
import com.example.and_sales_portal.data.auth.SalesRole
import com.example.and_sales_portal.data.auth.SalesSession
import com.example.and_sales_portal.designsystem.components.extended.AmarBottomNavigation
import com.example.and_sales_portal.designsystem.components.extended.AmarNavItem
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.account.AccountScreen
import com.example.and_sales_portal.ui.shell.SalesTab
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Splash, Login (MLG-00, MLG-01), Akun and the bottom navigation per role. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h760dp-mdpi")
class AuthScreensScreenshotTest {

    private fun capture(name: String, content: @Composable () -> Unit) =
        captureRoboImage("build/outputs/roborazzi/app_$name.png") { CompositionLocalProvider(LocalInspectionMode provides true) { AmarTheme { content() } } }

    private fun login(state: LoginFormState) = @Composable { LoginScreen(state, "1.0", {}, {}, {}, {}) }

    @Test
    fun splashAndLogin() {
        capture("splash") { SplashScreen("1.0") }
        capture("login_empty", login(LoginFormState()))
        capture("login_filled", login(LoginFormState("andi.pratama", "Demo1234")))
        capture("login_invalid", login(LoginFormState("andi.pratama", "salah", invalid = true)))
        capture("login_loading", login(LoginFormState("andi.pratama", "Demo1234", loading = true)))
    }

    @Test
    fun account() {
        val tl = AppUser(6, "Andi Pratama", "TL", "andi.pratama", "andi.pratama@amarbank.co.id", "81234567806", "SENIOR", listOf(1), 3, status = "ACTIVE")
        capture("account_tl") { AccountScreen(SalesSession(tl, AccessInfo("TL", "sales-app-access", emptyList())), {}, {}) }
        val sa = AppUser(15, "Eko Saputra", "SA", "eko.saputra", "eko.saputra@amarbank.co.id", null, null, listOf(1), 6, status = "ACTIVE")
        capture("account_sa") { AccountScreen(SalesSession(sa, AccessInfo("SA", "sales-app-access", emptyList())), {}, {}) }
    }

    @Test
    fun bottomNavigation() {
        listOf(SalesRole.TL, SalesRole.SA).forEach { role ->
            capture("bottom_nav_${role.name.lowercase()}") {
                val tabs = SalesTab.forRole(role)
                val items = tabs.map { AmarNavItem(stringResource(it.label), it.icon, it.activeIcon, it.inactiveIcon) }
                Column { tabs.indices.forEach { AmarBottomNavigation(items, it, {}, windowInsets = WindowInsets(0)) } }
            }
        }
    }
}
