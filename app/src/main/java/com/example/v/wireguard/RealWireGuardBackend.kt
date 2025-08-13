package com.example.v.wireguard

import android.content.Context
import android.util.Log
import com.example.v.models.ClientConfig
import com.example.v.models.Server
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java.io.BufferedReader
import java.io.StringReader
import kotlinx.coroutines.runBlocking

class RealWireGuardBackend(private val context: Context) {
    companion object { private const val TAG = "RealWireGuardBackend" }

    enum class TunnelState { UP, DOWN }

    private var backend: Backend? = null
    private var tunnel: Tunnel? = null

    init {
        try {
            backend = GoBackend(context)
            Log.d(TAG, "GoBackend initialized")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to initialize GoBackend", t)
            backend = null
        }
    }

    fun isAvailable(): Boolean = backend != null

    private fun createTunnelIfNeeded(name: String) {
        if (tunnel != null) return
        tunnel = object : Tunnel {
            @Volatile private var s: Tunnel.State = Tunnel.State.DOWN
            override fun getName(): String = name
            override fun onStateChange(newState: Tunnel.State) { s = newState }
        }
    }

    fun setState(server: Server, client: ClientConfig, state: TunnelState): Boolean {
        val b = backend ?: return false
        val tState = when (state) { TunnelState.UP -> Tunnel.State.UP; TunnelState.DOWN -> Tunnel.State.DOWN }
        return try {
            val cfg = buildConfig(server, client)
            createTunnelIfNeeded(server.name)
            val t = tunnel!!
            runBlocking { b.setState(t, tState, cfg) }
            Log.d(TAG, "Tunnel state set to $tState")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting tunnel state: ${e.message}", e)
            false
        }
    }

    fun disconnect(): Boolean = setStateDummy(TunnelState.DOWN)

    private fun setStateDummy(state: TunnelState): Boolean {
        val b = backend ?: return false
        val t = tunnel ?: return true
        val tState = when (state) { TunnelState.UP -> Tunnel.State.UP; TunnelState.DOWN -> Tunnel.State.DOWN }
        return try {
            runBlocking { b.setState(t, tState, null) }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
            false
        }
    }

    private fun buildConfig(server: Server, client: ClientConfig): Config {
        val wg = server.wireGuardConfig
        // Force IPv4-only for reliability on mobile networks (IPv6 routing/NAT often missing on servers)
        // If you later need IPv6, we can make this dynamic again.
        val ipv4Only = true
        
        // Define variables outside the StringBuilder scope for logging
        val dns = (wg?.dns?.takeIf { it.isNotBlank() } ?: client.dns)
        val mtu = wg?.mtu ?: 1280
        val defaultAllowed = if (ipv4Only) "0.0.0.0/0" else "0.0.0.0/0, ::/0"
        // If IPv4-only is enabled, force IPv4 route regardless of server-provided value
        val allowed = if (ipv4Only) {
            "0.0.0.0/0"
        } else {
            (wg?.allowedIPs?.takeIf { it.isNotBlank() } ?: defaultAllowed)
        }
        val ka = if (wg?.keepAlive != null && wg.keepAlive > 0) wg.keepAlive else 25
        
        val sb = StringBuilder().apply {
            appendLine("[Interface]")
            appendLine("PrivateKey = ${client.privateKey}")
            appendLine("Address = ${client.address}")
            // Prefer server/client DNS if provided, else use public resolvers
            appendLine("DNS = ${dns.ifBlank { "1.1.1.1, 8.8.8.8" }}")
            // MTU tuning to reduce fragmentation issues on mobile networks
            appendLine("MTU = $mtu")
            appendLine()
            appendLine("[Peer]")
            appendLine("PublicKey = ${wg?.serverPublicKey ?: ""}")
            wg?.presharedKey?.let { appendLine("PresharedKey = $it") }
            // Route all v4+v6 by default; optionally force IPv4-only
            appendLine("AllowedIPs = $allowed")
            // Ensure endpoint has no accidental spaces and port is correct
            val endpointHost = (wg?.serverEndpoint ?: "").trim()
            val endpointPort = (wg?.serverPort ?: 51820)
            appendLine("Endpoint = $endpointHost:$endpointPort")
            appendLine("PersistentKeepalive = $ka")
        }
        val configString = sb.toString()
        Log.d(TAG, "=== WireGuard Config Generated ===")
        Log.d(TAG, configString)
        Log.d(TAG, "=== End Config ===")
        
        // Debug specific values
        Log.d(TAG, "🔍 DEBUG Config Details:")
        Log.d(TAG, "  Client Address: ${client.address}")
        Log.d(TAG, "  Server Endpoint: ${wg?.serverEndpoint}:${wg?.serverPort}")
        Log.d(TAG, "  AllowedIPs: $allowed")
        Log.d(TAG, "  DNS: ${dns.ifBlank { "1.1.1.1, 8.8.8.8" }}")
        Log.d(TAG, "  MTU: $mtu")

        // Try to resolve endpoint early to catch DNS issues
        try {
            val host = (wg?.serverEndpoint ?: "").trim()
            val port = wg?.serverPort ?: 51820
            if (host.isNotBlank()) {
                val resolved = java.net.InetAddress.getAllByName(host).joinToString { it.hostAddress }
                Log.d(TAG, "Resolved endpoint $host:$port -> $resolved")
            } else {
                Log.w(TAG, "Endpoint host is blank")
            }
        } catch (dns: Exception) {
            Log.e(TAG, "Failed to resolve endpoint ${wg?.serverEndpoint}:${wg?.serverPort}: ${dns.message}")
        }

        val reader = BufferedReader(StringReader(configString))
        return Config.parse(reader)
    }
}