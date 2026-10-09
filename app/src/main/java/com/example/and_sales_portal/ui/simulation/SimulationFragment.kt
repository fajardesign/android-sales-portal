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
import com.example.and_sales_portal.R
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
        SimulationScreen(sim, ::back) { f -> settings.update(f) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimulationScreen(sim: Simulation, onBack: () -> Unit, onChange: ((Simulation) -> Simulation) -> Unit) {
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
                ).forEach { (mode, label) ->
                    AmarChip(stringResource(label), selected = sim.location == mode, onClick = { onChange { it.copy(location = mode) } })
                }
            }
            Switch(R.string.sim_mock, sim.mockLocation) { v -> onChange { it.copy(mockLocation = v) } }
            Switch(R.string.sim_offline, sim.offline) { v -> onChange { it.copy(offline = v) } }
            Switch(R.string.sim_reg_consent, sim.regConsent) { v -> onChange { it.copy(regConsent = v) } }
        }
    }
}

@Composable
private fun Switch(label: Int, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(label), Modifier.weight(1f), AmarTheme.colors.content.primary, style = AmarTheme.typography.bodyMd)
        AmarToggle(checked, onChange)
    }
}
