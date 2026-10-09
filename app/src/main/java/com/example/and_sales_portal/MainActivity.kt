package com.example.and_sales_portal

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.and_sales_portal.designsystem.components.extended.AmarBottomNavigation
import com.example.and_sales_portal.designsystem.components.extended.AmarNavItem
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.shell.SalesTab
import com.example.and_sales_portal.ui.shell.ShellNavigator
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

/**
 * Shell of the sales app (TL, SR, SA): hosts `nav_main` and shows the role's bottom navigation on tab destinations.
 * When the backend ends the session while the app is in use (account disabled or password reset, MLG-02), the user
 * returns to the login with "Sesi tidak dapat diperbarui".
 */
class MainActivity : AppCompatActivity() {

    private val currentTab = mutableStateOf<SalesTab?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nav = (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
        nav.addOnDestinationChangedListener { _, destination, _ -> currentTab.value = SalesTab.of(destination.id) }

        findViewById<ComposeView>(R.id.bottom_nav).setContent {
            AmarTheme {
                val session by appContainer.auth.session.collectAsState()
                val tab = currentTab.value
                val role = session?.role
                if (tab != null && role != null) {
                    val tabs = SalesTab.forRole(role)
                    AmarBottomNavigation(
                        items = tabs.map { AmarNavItem(stringResource(it.label), it.icon, it.activeIcon, it.inactiveIcon) },
                        selectedIndex = tabs.indexOf(tab),
                        onSelect = { ShellNavigator(nav).toTab(tabs[it], tabs.first()) },
                    )
                }
            }
        }
        watchSession(nav)
    }

    private fun watchSession(nav: NavController) {
        lifecycleScope.launch {
            appContainer.supabase.auth.sessionStatus.collect { status ->
                val onTab = SalesTab.of(nav.currentDestination?.id ?: 0) != null
                if (status is SessionStatus.NotAuthenticated && !status.isSignOut && onTab && appContainer.auth.session.value != null) {
                    appContainer.auth.session.value?.let { appContainer.demo.deleteDraft(it.user.id) }
                    appContainer.auth.logout()
                    ShellNavigator(nav).toLogin(expired = true)
                }
            }
        }
    }
}
