package com.example.and_sales_portal.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBar
import com.example.and_sales_portal.designsystem.components.navigation.AmarAppBarType
import com.example.and_sales_portal.designsystem.theme.AmarFormTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.account.AccountScreen
import com.example.and_sales_portal.ui.common.ComposeFragment
import com.example.and_sales_portal.ui.common.ConfirmDialog
import com.example.and_sales_portal.ui.common.WithToast
import kotlinx.coroutines.launch

/** Spec: AKN-01 Akun (all roles). */
class AccountFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val current = session ?: return
        var confirmLogout by remember { mutableStateOf(false) }
        var confirmReset by remember { mutableStateOf(false) }
        var toast by remember { mutableStateOf<String?>(null) }
        val resetDone = stringResource(R.string.account_reset_demo_done)
        val scope = rememberCoroutineScope()
        WithToast(toast, { toast = null }) {
            AccountScreen(current, onLogout = { confirmLogout = true }, onResetDemo = { confirmReset = true })
        }
        if (confirmLogout) {
            ConfirmDialog(
                title = stringResource(R.string.account_logout_confirm_title),
                message = stringResource(R.string.account_logout_confirm),
                confirmLabel = stringResource(R.string.account_logout),
                onConfirm = {
                    confirmLogout = false
                    scope.launch {
                        container.auth.logout()
                        ShellNavigator(this@AccountFragment).toLogin(expired = false)
                    }
                },
                onDismiss = { confirmLogout = false },
            )
        }
        if (confirmReset) {
            ConfirmDialog(
                title = stringResource(R.string.account_reset_demo_confirm_title),
                message = stringResource(R.string.account_reset_demo_confirm),
                confirmLabel = stringResource(R.string.account_reset_demo),
                onConfirm = {
                    confirmReset = false
                    container.demo.reset(current.user.id)
                    toast = resetDone
                },
                onDismiss = { confirmReset = false },
            )
        }
    }
}

/**
 * Tab not built yet in this stage of the prototype (build order: part 2 Absensi & Kunjungan, part 3 Partner,
 * part 4 Tim & Jadwal, part 5 Beranda & Kinerja). TODO: replace with the real screens.
 */
abstract class PendingTabFragment(private val title: Int) : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val c = AmarTheme.colors
        Column(Modifier.fillMaxSize().background(c.background.canvas)) {
            AmarAppBar(title = stringResource(title), type = AmarAppBarType.Primary)
            Column(
                Modifier.fillMaxSize().padding(AmarFormTokens.PaddingHorizontal),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.wip_screen), Modifier.fillMaxWidth(), c.content.secondary, style = AmarTheme.typography.bodyMd, textAlign = TextAlign.Center)
            }
        }
    }
}

class TlHomeFragment : PendingTabFragment(R.string.nav_beranda)
class SalesHomeFragment : PendingTabFragment(R.string.nav_beranda)
class PartnerTabFragment : PendingTabFragment(R.string.nav_partner)
class TeamTabFragment : PendingTabFragment(R.string.nav_tim)
class ActivityTabFragment : PendingTabFragment(R.string.nav_aktivitas)
class VisitsTabFragment : PendingTabFragment(R.string.nav_kunjungan)
class PerformanceTabFragment : PendingTabFragment(R.string.nav_kinerja)
