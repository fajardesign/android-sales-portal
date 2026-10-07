package com.example.and_sales_portal.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        WithToast(toast, { toast = null }) {
            AccountScreen(
                current,
                onLogout = { confirmLogout = true },
                onResetDemo = { confirmReset = true },
                onSimulation = if (BuildConfig.DEBUG) ({ navigate(R.id.simulation) }) else null,
            )
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

/**
 * Beranda (PRD v3 C1, D1). This stage only has the Absensi card (Figma "01 | Team Leader Dashboard"); the rest of
 * Beranda comes in part 5. TODO: summaries, Pengajuan Partner, this month, shortcuts.
 */
abstract class HomeFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val repo = activityRepo
        val data by repo.data.collectAsState()
        val online by container.connectivity.online.collectAsState()
        val (toast, clearToast) = incomingToast()
        val today = remember(data, online) { AttendanceFragment.attendanceState(repo, online).today }
        WithToast(toast, clearToast) {
            ScreenScaffold(stringResource(R.string.nav_beranda), onBack = null) {
                AttendanceCard(
                    today,
                    online,
                    onClockIn = { navigate(R.id.capture, CaptureFragment.args(CaptureMode.CLOCK_IN)) },
                    onClockOut = { navigate(R.id.capture, CaptureFragment.args(CaptureMode.CLOCK_OUT)) },
                    onHistory = { navigate(R.id.attendance) },
                )
                WipText()
            }
        }
    }
}

class TlHomeFragment : HomeFragment()
class SalesHomeFragment : HomeFragment()

/** TL tab Aktivitas (decision 2026-10-07): own attendance and visits, team planning, reports. */
class ActivityTabFragment : ComposeFragment() {
    private class Entry(val title: Int, val description: Int, val open: () -> Unit)

    @Composable
    override fun ScreenContent() {
        val wip = { title: Int -> { navigate(R.id.wip, WipFragment.args(title)) } }
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
                Entry(R.string.act_productivity, R.string.act_productivity_desc, wip(R.string.act_productivity)),
                Entry(R.string.act_sales, R.string.act_sales_desc, wip(R.string.act_sales)),
                Entry(R.string.act_loans, R.string.act_loans_desc, wip(R.string.act_loans)),
                Entry(R.string.act_incentive, R.string.act_incentive_desc, wip(R.string.act_incentive)),
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

/**
 * Screen not built yet in this stage of the prototype (build order: part 3 Partner, part 4 Tim & Jadwal, part 5
 * Beranda & Kinerja). Every entry leads somewhere with a way back (no dead ends). TODO: replace with the real screens.
 */
class WipFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        ScreenScaffold(stringResource(requireArguments().getInt(ARG_TITLE)), ::back) { WipText() }
    }

    companion object {
        private const val ARG_TITLE = "title"
        fun args(title: Int) = bundleOf(ARG_TITLE to title)
    }
}

/** Tab not built yet (same as [WipFragment], without back). */
abstract class PendingTabFragment(private val title: Int) : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        ScreenScaffold(stringResource(title), onBack = null) { WipText() }
    }
}

class PerformanceTabFragment : PendingTabFragment(R.string.nav_kinerja)

@Composable
private fun WipText() {
    Text(
        stringResource(R.string.wip_screen),
        Modifier.fillMaxWidth(),
        AmarTheme.colors.content.secondary,
        style = AmarTheme.typography.bodyMd,
        textAlign = TextAlign.Center,
    )
}
