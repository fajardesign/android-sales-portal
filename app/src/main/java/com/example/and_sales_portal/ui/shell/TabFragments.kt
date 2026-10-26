package com.example.and_sales_portal.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.os.bundleOf
import com.example.and_sales_portal.BuildConfig
import com.example.and_sales_portal.R
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.account.AccountScreen
import com.example.and_sales_portal.ui.attendance.AttendanceCard
import com.example.and_sales_portal.ui.attendance.AttendanceFragment
import com.example.and_sales_portal.ui.attendance.CaptureFragment
import com.example.and_sales_portal.ui.attendance.CaptureMode
import com.example.and_sales_portal.ui.common.ComposeFragment
import com.example.and_sales_portal.ui.common.ConfirmDialog
import com.example.and_sales_portal.ui.common.ScreenScaffold
import com.example.and_sales_portal.ui.common.SectionTitle
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
        val directory by container.directory.changes.collectAsState()
        WithToast(toast, { toast = null }) {
            // Redrawn when an account change (Simulasi, Reset data demo) changes the leader shown.
            key(directory) {
                AccountScreen(
                    current,
                    onLogout = { confirmLogout = true },
                    onResetDemo = { confirmReset = true },
                    onSimulation = if (BuildConfig.DEBUG) ({ navigate(R.id.simulation) }) else null,
                )
            }
        }
        if (confirmLogout) {
            ConfirmDialog(
                title = stringResource(R.string.account_logout_confirm_title),
                message = stringResource(R.string.account_logout_confirm),
                confirmLabel = stringResource(R.string.account_logout),
                onConfirm = {
                    confirmLogout = false
                    scope.launch {
                        container.demo.deleteDraft(current.user.id) // FR-PA-009: draft deleted on logout
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
                    container.directory.reset()
                    container.partners.reset()
                    container.operations.reset()
                    container.demo.reset(current.user.id)
                    toast = resetDone
                },
                onDismiss = { confirmReset = false },
            )
        }
    }
}

/** TL tab Aktivitas (decision 2026-10-07): own attendance and visits, team planning, reports. */
class ActivityTabFragment : ComposeFragment() {
    private class Entry(val title: Int, val description: Int, val open: () -> Unit)

    @Composable
    override fun ScreenContent() {
        val sections = listOf(
            R.string.act_mine to listOf(
                Entry(R.string.att_title, R.string.act_attendance_desc) { navigate(R.id.attendance) },
                Entry(R.string.vis_title_own, R.string.act_visits_desc) { navigate(R.id.my_visits) },
            ),
            R.string.act_team to listOf(
                Entry(R.string.act_schedule, R.string.act_schedule_desc) { navigate(R.id.schedule) },
                Entry(R.string.act_assign, R.string.act_assign_desc) { navigate(R.id.assign) },
            ),
            R.string.act_reports to listOf(
                Entry(R.string.act_productivity, R.string.act_productivity_desc) { navigate(R.id.team_productivity) },
            ),
        )
        ScreenScaffold(stringResource(R.string.nav_aktivitas), onBack = null) {
            sections.forEach { (title, entries) ->
                SectionTitle(stringResource(title))
                Column(verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.ListGap)) {
                    entries.forEach { e ->
                        AmarCard(
                            label = stringResource(e.title),
                            modifier = Modifier.fillMaxWidth(),
                            style = AmarCardStyle.Border,
                            description = stringResource(e.description),
                            showAction = true,
                            onClick = e.open,
                        )
                    }
                }
            }
        }
    }
}
