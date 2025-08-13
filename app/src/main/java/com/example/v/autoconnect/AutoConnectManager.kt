package com.example.v.autoconnect

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.example.v.data.autoconnect.AutoConnectMode
import com.example.v.data.autoconnect.AutoConnectRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AutoConnectManager(
    private val context: Context,
    private val repo: AutoConnectRepository,
    private val startVpnTunnel: () -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun start() {
        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { checkAndConnect() }
            override fun onCapabilitiesChanged(network: Network, nc: NetworkCapabilities) { checkAndConnect() }
            override fun onLost(network: Network) { /* no-op */ }
        })
        // Initial check
        checkAndConnect()
    }

    private fun checkAndConnect() {
        scope.launch {
            val settings = repo.get() ?: return@launch
            if (!settings.enabled) return@launch

            val network = cm.activeNetwork ?: return@launch
            val caps = cm.getNetworkCapabilities(network) ?: return@launch

            val onWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val onCell = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

            val shouldConnect = when (settings.mode) {
                AutoConnectMode.UNSECURED_WIFI_ONLY -> onWifi && isCurrentWifiUnsecured()
                AutoConnectMode.ANY_WIFI -> onWifi
                AutoConnectMode.ANY_WIFI_OR_CELLULAR -> onWifi || onCell
            }

            if (shouldConnect) {
                Log.d("AutoConnect", "Conditions met for ${settings.mode}, starting VPN")
                startVpnTunnel()
            }
        }
    }

    private fun isCurrentWifiUnsecured(): Boolean {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wm.connectionInfo ?: return false
        val ssid = info.ssid?.trim('"') ?: return false
        val scanResults = wm.scanResults
        val current = scanResults.firstOrNull { it.SSID == ssid } ?: return false
        val caps = current.capabilities ?: ""
        val secured = caps.contains("WEP", true) || caps.contains("WPA", true) || caps.contains("EAP", true)
        return !secured
    }
}


