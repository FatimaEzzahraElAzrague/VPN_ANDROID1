package com.myapp.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class VPNConnectionRequest(
    val location: String,
    val adBlockEnabled: Boolean = false,
    val antiMalwareEnabled: Boolean = false,
    val familySafeModeEnabled: Boolean = false
)

@Serializable
data class VPNConnectionResponse(
    val privateKey: String,
    val publicKey: String,
    val serverPublicKey: String,
    val serverEndpoint: String,
    val allowedIPs: String,
    val internalIP: String,
    val dns: String,
    val mtu: Int = 1420,
    val presharedKey: String? = null,
    val internalIPv6: String? = null,
    val clientConfig: String? = null
)

@Serializable
data class VPNConnectionError(
    val error: String,
    val message: String
)

@Serializable
data class VPNPreferences(
    val adBlockEnabled: Boolean = false,
    val antiMalwareEnabled: Boolean = false,
    val familySafeModeEnabled: Boolean = false,
    val killSwitchEnabled: Boolean = true,
    val dnsLeakProtectionEnabled: Boolean = true,
    val autoConnectEnabled: Boolean = false,
    val preferredLocation: String? = null
)

@Serializable
data class ServerStatus(
    val serverId: String,
    val status: String, // "online", "offline", "maintenance"
    val uptime: Long,
    val load: Double,
    val connections: Int,
    val lastChecked: Long
)

@Serializable
data class PingResult(
    val serverId: String,
    val latency: Int, // in milliseconds
    val timestamp: Long,
    val method: String // "udp", "http", "ping"
)

@Serializable
data class ActiveConnection(
    val id: String,
    val serverId: String,
    val clientIP: String,
    val connectedAt: Long,
    val bytesReceived: Long,
    val bytesSent: Long
)
