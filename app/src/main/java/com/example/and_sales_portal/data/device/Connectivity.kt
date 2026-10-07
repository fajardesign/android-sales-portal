package com.example.and_sales_portal.data.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Online state of the device, or offline when the simulation says so. */
class Connectivity(context: Context, simulation: SimulationSettings, scope: CoroutineScope) {
    private val manager = context.getSystemService(ConnectivityManager::class.java)
    private val network = MutableStateFlow(isConnected())

    init {
        manager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(n: Network) { network.value = true }
            override fun onLost(n: Network) { network.value = isConnected() }
            override fun onCapabilitiesChanged(n: Network, caps: NetworkCapabilities) {
                network.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        })
    }

    val online: StateFlow<Boolean> = combine(network, simulation.state) { net, sim -> net && !sim.offline }
        .stateIn(scope, SharingStarted.Eagerly, network.value && !simulation.value.offline)

    private fun isConnected(): Boolean =
        manager.getNetworkCapabilities(manager.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}
