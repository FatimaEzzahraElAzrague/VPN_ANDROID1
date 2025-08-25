`package com.myapp.backend.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class VPNServer(
    val id: String,
    val name: String,
    val city: String,
    val country: String,
    val countryCode: String,
    val flag: String,
    val ip: String,
    val port: Int,
    val subnet: String,
    val serverIP: String,
    val dnsServers: List<String>,
    val wireguardPublicKey: String,
    val wireguardEndpoint: String,
    val allowedIPs: String,
    val mtu: Int = 1420,
    val isActive: Boolean = true,
    val priority: Int = 1,
    val createdAt: String = LocalDateTime.now().toString()
)

@Serializable
data class VPNClientConfig(
    val privateKey: String,
    val publicKey: String,
    val address: String,
    val dns: String = "1.1.1.1,8.8.8.8",
    val mtu: Int = 1420,
    val allowedIPs: String = "0.0.0.0/0,::/0"
)

@Serializable
data class VPNConnectionConfig(
    val server: VPNServer,
    val clientConfig: VPNClientConfig,
    val wireguardConfig: String, // Complete WireGuard config file content
    val connectionTimeout: Int = 30000, // 30 seconds
    val keepAlive: Int = 25, // 25 seconds
    val persistentKeepalive: Int = 25
)

@Serializable
data class VPNConfigRequest(
    val serverId: String,
    val userId: String,
    val clientPublicKey: String? = null, // If null, generate new key pair
    val preferredDNS: String? = null
)

@Serializable
data class VPNConfigResponse(
    val success: Boolean,
    val message: String,
    val config: VPNConnectionConfig? = null,
    val configs: Map<String, VPNConnectionConfig>? = null,
    val error: String? = null
)

@Serializable
data class VPNConnectionStatus(
    val userId: String,
    val serverId: String,
    val status: String, // CONNECTED, DISCONNECTED, CONNECTING, ERROR
    val connectedAt: String? = null,
    val disconnectedAt: String? = null,
    val lastError: String? = null,
    val bytesReceived: Long = 0,
    val bytesSent: Long = 0,
    val connectionDuration: Long = 0 // in seconds
)

@Serializable
data class VPNConnectionRequest(
    val userId: String,
    val serverId: String,
    val action: String // CONNECT, DISCONNECT, SWITCH
)

@Serializable
data class VPNConnectionResponse(
    val success: Boolean,
    val message: String,
    val status: VPNConnectionStatus? = null,
    val error: String? = null
)

@Serializable
data class WireGuardConfig(
    val interface: WireGuardInterface,
    val peer: WireGuardPeer
)

@Serializable
data class WireGuardInterface(
    val privateKey: String,
    val address: String,
    val dns: String,
    val mtu: Int = 1420
)

@Serializable
data class WireGuardPeer(
    val publicKey: String,
    val endpoint: String,
    val allowedIPs: String,
    val persistentKeepalive: Int = 25
)
