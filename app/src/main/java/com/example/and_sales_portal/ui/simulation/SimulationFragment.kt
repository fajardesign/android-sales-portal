package com.example.and_sales_portal.ui.simulation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.content.ActivityNotFoundException
import android.content.Intent
import com.example.and_sales_portal.R
import com.example.and_sales_portal.data.demo.DemoDirectory
import com.example.and_sales_portal.designsystem.components.actions.AmarButton
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonSize
import com.example.and_sales_portal.designsystem.components.actions.AmarButtonType
import com.example.and_sales_portal.ui.common.WithToast
import com.example.and_sales_portal.data.device.LocationMode
import com.example.and_sales_portal.data.device.Simulation
import com.example.and_sales_portal.designsystem.components.actions.AmarChip
import com.example.and_sales_portal.designsystem.components.inputs.AmarToggle
import com.example.and_sales_portal.designsystem.components.layout.AmarCard
import com.example.and_sales_portal.designsystem.components.layout.AmarCardStyle
import com.example.and_sales_portal.designsystem.theme.AmarActivityTokens
import com.example.and_sales_portal.designsystem.theme.AmarTheme
import com.example.and_sales_portal.ui.common.ComposeFragment
import com.example.and_sales_portal.ui.common.ScreenScaffold

/**
 * "Simulasi (debug)" (decision 2026-10-07, debug builds only, from Akun): device conditions for the ATT branches that an
 * emulator cannot produce on demand: location at or away from the toko, mock location, offline, liveness failure.
 */
class SimulationFragment : ComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val settings = container.simulation
        val sim by settings.state.collectAsState()
        val partners by container.partners.partners.collectAsState()
        var toast by remember { mutableStateOf<String?>(null) }
        val admin = remember(partners, toast) {
            AdminSimulation(
                tlActive = DemoDirectory.person(TL_ANDI)?.status != "DISABLED",
                saActive = DemoDirectory.person(SA_EKO)?.status != "DISABLED",
                onDisableTl = {
                    val n = container.directory.disableTl(TL_ANDI, TL_BAGUS)
                    toast = getString(R.string.sim_admin_disable_tl_done, n)
                },
                onDisableSa = {
                    val n = container.directory.disableSales(SA_EKO)
                    toast = getString(R.string.sim_admin_disable_sa_done, n)
                },
                onAplAssignTl = {
                    val ok = container.partners.aplAssignTl(STORE_TAMALANREA, TL_BAGUS, APL_HASAN)
                    toast = getString(if (ok) R.string.sim_apl_assign_done else R.string.sim_apl_assign_failed)
                },
                onCancelStoreRequest = {
                    val st = container.partners.latestStoreRequest()
                    toast = if (st != null && container.partners.adminCancelStoreRequest(st.id, getString(R.string.sim_cancel_store_reason))) {
                        getString(R.string.sim_admin_cancel_store_done, st.name)
                    } else {
                        getString(R.string.sim_admin_cancel_store_none)
                    }
                },
                onAplReleaseTl = {
                    val released = container.partners.aplReleaseTl(STORE_SUDIANG, APL_HASAN)
                    toast = if (released == null) getString(R.string.sim_apl_release_failed) else getString(R.string.sim_apl_release_done, released.size)
                },
                onMoveSubordinate = {
                    val n = container.directory.moveSubordinate(SR_SITI, TL_BAGUS)
                    toast = getString(R.string.sim_admin_move_done, n)
                },
            )
        }
        WithToast(toast, { toast = null }) { SimulationScreen(sim, ::back, admin, onCatalog = ::openCatalog) { f -> settings.update(f) } }
    }

    /** The design system catalog (debug builds only; no launcher icon since 2026-10-13). */
    private fun openCatalog() {
        val intent = Intent().setClassName(requireContext(), "com.example.and_sales_portal.designsystem.catalog.DesignSystemCatalogActivity")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Not in this build.
        }
    }

    private companion object {
        /** Same as the web presets disable-tl (Andi Pratama → Bagus Wicaksono) and disable-sa (Eko Saputra). */
        const val TL_ANDI = 6L
        const val TL_BAGUS = 22L
        const val SA_EKO = 15L
        const val SR_SITI = 11L
        /** Jaya Abadi Cellular Sudiang: TL Andi, held by Siti. */
        const val STORE_SUDIANG = "REG2026-0139-3"
        const val STORE_TAMALANREA = "REG2026-0132-2"
        const val APL_HASAN = "Hasan Basri (APL)"
    }
}

/** Web ACC-05 / APL effects tried on the device (DD review points 5 and 7, PRD Scope 1 v2 C4); no admin screen on Android. */
class AdminSimulation(
    val tlActive: Boolean,
    val saActive: Boolean,
    val onDisableTl: () -> Unit,
    val onDisableSa: () -> Unit,
    val onAplAssignTl: () -> Unit = {},
    val onAplReleaseTl: () -> Unit = {},
    /** C7 (decision 2026-10-20): the Admin cancels the latest Permintaan tambah toko. */
    val onCancelStoreRequest: () -> Unit = {},
    val onMoveSubordinate: () -> Unit = {},
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimulationScreen(
    sim: Simulation,
    onBack: () -> Unit,
    admin: AdminSimulation? = null,
    onCatalog: (() -> Unit)? = null,
    onChange: ((Simulation) -> Simulation) -> Unit,
) {
    val c = AmarTheme.colors
    ScreenScaffold(stringResource(R.string.sim_title), onBack) {
        Text(stringResource(R.string.sim_desc), color = c.content.secondary, style = AmarTheme.typography.bodySm)
        AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
            Text(stringResource(R.string.sim_location), color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap), verticalArrangement = Arrangement.spacedBy(AmarActivityTokens.TileGap)) {
                listOf(
                    LocationMode.AT_STORE to R.string.sim_location_store,
                    LocationMode.DEVICE to R.string.sim_location_device,
                    LocationMode.OUT_OF_RADIUS to R.string.sim_location_out,
                    LocationMode.LOW_ACCURACY to R.string.sim_location_low_accuracy,
                ).forEach { (mode, label) ->
                    AmarChip(stringResource(label), selected = sim.location == mode, onClick = { onChange { it.copy(location = mode) } })
                }
            }
            Switch(R.string.sim_mock, sim.mockLocation) { v -> onChange { it.copy(mockLocation = v) } }
            Switch(R.string.sim_offline, sim.offline) { v -> onChange { it.copy(offline = v) } }
        }
        admin?.let { a ->
            AmarCard(Modifier.fillMaxWidth(), style = AmarCardStyle.Border) {
                Text(stringResource(R.string.sim_admin_accounts), color = c.content.primary, style = AmarTheme.typography.labelMdSemibold)
                Text(stringResource(R.string.sim_admin_accounts_desc), color = c.content.secondary, style = AmarTheme.typography.bodySm)
                AmarButton(stringResource(R.string.sim_admin_disable_tl), a.onDisableTl, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium, enabled = a.tlActive)
                AmarButton(stringResource(R.string.sim_admin_disable_sa), a.onDisableSa, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium, enabled = a.saActive)
                AmarButton(stringResource(R.string.sim_apl_assign), a.onAplAssignTl, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium)
                AmarButton(stringResource(R.string.sim_apl_release), a.onAplReleaseTl, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium)
                Text(stringResource(R.string.sim_apl_note), color = c.content.tertiary, style = AmarTheme.typography.bodySm)
                AmarButton(stringResource(R.string.sim_admin_cancel_store), a.onCancelStoreRequest, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium)
                AmarButton(stringResource(R.string.sim_admin_move), a.onMoveSubordinate, Modifier.fillMaxWidth(), type = AmarButtonType.Secondary, size = AmarButtonSize.Medium)
            }
        }
        onCatalog?.let { AmarButton(stringResource(R.string.sim_open_catalog), it, Modifier.fillMaxWidth(), type = AmarButtonType.Naked, size = AmarButtonSize.Medium) }
    }
}

@Composable
private fun Switch(label: Int, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(label), Modifier.weight(1f), AmarTheme.colors.content.primary, style = AmarTheme.typography.bodyMd)
        AmarToggle(checked, onChange)
    }
}
