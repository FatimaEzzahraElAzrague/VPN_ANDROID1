package com.example.v.data.models

import kotlinx.serialization.Serializable
import com.google.gson.annotations.SerializedName

@Serializable
data class ServerLocation(
    val id: String,
    val name: String,
    val country: String,
    val ip: String,
    val subnet: String,
    val agentPort: Int,
    val serverPublicKey: String,
    val listenPort: String,
    val ipv6Base: String,
    val region: String
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
data class VPNConnectionStatus(
    val userId: String,
    val serverId: String,
    val status: String,
    val connectedAt: String? = null,
    val disconnectedAt: String? = null,
    val lastError: String? = null,
    val bytesReceived: Long = 0,
    val bytesSent: Long = 0,
    val connectionDuration: Long = 0
)

@Serializable
data class VPNConnectionRequest(
    val userId: String,
    val serverId: String,
    val action: String
)

@Serializable
data class VPNLocationRequest(
    val location: String,
    val adBlockEnabled: Boolean = false,
    val antiMalwareEnabled: Boolean = false,
    val familySafeModeEnabled: Boolean = false
)

@Serializable
data class VPNConnectionResponse(
    @SerializedName("private_key") val privateKey: String?,
    @SerializedName("public_key") val publicKey: String?,
    @SerializedName("server_public_key") val serverPublicKey: String?,
    @SerializedName("server_endpoint") val serverEndpoint: String?,
    @SerializedName("allowed_ips") val allowedIPs: String?,
    @SerializedName("internal_ip") val internalIP: String?,
    val dns: String?,
    val mtu: Int = 1420,
    @SerializedName("preshared_key") val presharedKey: String? = null,
    @SerializedName("internal_ipv6") val internalIPv6: String? = null,
    @SerializedName("client_config") val clientConfig: String? = null
) {
    // Helper function to validate that required fields are not null
    fun validate(): Boolean {
        return privateKey != null && 
               publicKey != null && 
               serverPublicKey != null && 
               serverEndpoint != null && 
               allowedIPs != null && 
               internalIP != null && 
               dns != null
    }
    
    // Helper function to get non-null values or throw exception
    fun getValidatedConfig(): VPNConnectionResponse {
        if (!validate()) {
            throw IllegalStateException("VPN configuration is missing required fields: privateKey=$privateKey, publicKey=$publicKey, serverPublicKey=$serverPublicKey, serverEndpoint=$serverEndpoint, allowedIPs=$allowedIPs, internalIP=$internalIP, dns=$dns")
        }
        return this
    }
}

@Serializable
data class VPNStatusResponse(
    val success: Boolean,
    val message: String,
    val status: VPNConnectionStatus? = null,
    val error: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val message: String,
    val user: Map<String, String>
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)

@Serializable
data class VPNConfigRequest(
    val serverId: String,
    val userId: String,
    val clientPublicKey: String? = null,
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
data class VPNConnectionConfig(
    val server: VPNServer,
    val clientConfig: VPNClientConfig,
    val wireguardConfig: String,
    val connectionTimeout: Int = 30000,
    val keepAlive: Int = 25,
    val persistentKeepalive: Int = 25
)

@Serializable
data class VPNServer(
    val id: String,
    val name: String,
    val location: String,
    val country: String,
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
    val createdAt: String = ""
)

enum class VPNConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR
}
