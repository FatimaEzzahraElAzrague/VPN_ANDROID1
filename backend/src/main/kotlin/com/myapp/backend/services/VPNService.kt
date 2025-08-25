package com.myapp.backend.services

import com.myapp.backend.models.*
import com.myapp.backend.repositories.VPNRepository
import com.myapp.backend.config.Env
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * Production-ready VPN Service
 * Handles VPN configuration generation and connection management
 */
class VPNService {
    
    companion object {
        private val logger = LoggerFactory.getLogger(VPNService::class.java)
        
        @Volatile
        private var INSTANCE: VPNService? = null
        
        fun getInstance(): VPNService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VPNService().also { INSTANCE = it }
            }
        }
    }
    
    private val repository = VPNRepository()
    
    init {
        // Initialize default VPN servers
        repository.initializeDefaultVPNServers()
        logger.info("VPNService initialized with default servers")
    }
    
    /**
     * Get VPN configuration for a user and server
     */
    fun getVPNConfiguration(userId: String, serverId: String): VPNConfigResponse {
        logger.info("Getting VPN configuration for user: $userId, server: $serverId")
        
        try {
            // Get VPN server
            val server = repository.getVPNServerById(serverId)
            if (server == null) {
                return VPNConfigResponse(
                    success = false,
                    message = "VPN server not found",
                    error = "Server $serverId does not exist or is inactive"
                )
            }
            
            // Get or create client configuration
            val clientConfig = repository.getOrCreateClientConfig(userId, serverId)
            
            // Generate WireGuard configuration
            val wireguardConfig = generateWireGuardConfig(server, clientConfig)
            
            // Create connection configuration
            val connectionConfig = VPNConnectionConfig(
                server = server,
                clientConfig = clientConfig,
                wireguardConfig = wireguardConfig,
                connectionTimeout = 30000,
                keepAlive = 25,
                persistentKeepalive = 25
            )
            
            logger.info("VPN configuration generated successfully for user: $userId, server: $serverId")
            
            return VPNConfigResponse(
                success = true,
                message = "VPN configuration generated successfully",
                config = connectionConfig
            )
            
        } catch (e: Exception) {
            logger.error("Error generating VPN configuration for user: $userId, server: $serverId", e)
            return VPNConfigResponse(
                success = false,
                message = "Failed to generate VPN configuration",
                error = e.message
            )
        }
    }
    
    /**
     * Get VPN configurations for both Osaka and Paris servers for a user
     */
    fun getVPNConfigurationsForUser(userId: String): VPNConfigResponse {
        logger.info("Getting VPN configurations for user: $userId (Osaka and Paris)")
        
        try {
            // Get both Osaka and Paris servers
            val osakaServer = repository.getVPNServerById("osaka")
            val parisServer = repository.getVPNServerById("paris")
            
            if (osakaServer == null || parisServer == null) {
                return VPNConfigResponse(
                    success = false,
                    message = "VPN servers not found",
                    error = "Osaka or Paris server is not available"
                )
            }
            
            // Get or create client configurations for both servers
            val osakaClientConfig = repository.getOrCreateClientConfig(userId, "osaka")
            val parisClientConfig = repository.getOrCreateClientConfig(userId, "paris")
            
            // Generate WireGuard configurations
            val osakaWireguardConfig = generateWireGuardConfig(osakaServer, osakaClientConfig)
            val parisWireguardConfig = generateWireGuardConfig(parisServer, parisClientConfig)
            
            // Create connection configurations
            val osakaConnectionConfig = VPNConnectionConfig(
                server = osakaServer,
                clientConfig = osakaClientConfig,
                wireguardConfig = osakaWireguardConfig,
                connectionTimeout = 30000,
                keepAlive = 25,
                persistentKeepalive = 25
            )
            
            val parisConnectionConfig = VPNConnectionConfig(
                server = parisServer,
                clientConfig = parisClientConfig,
                wireguardConfig = parisWireguardConfig,
                connectionTimeout = 30000,
                keepAlive = 25,
                persistentKeepalive = 25
            )
            
            logger.info("VPN configurations generated successfully for user: $userId")
            
            return VPNConfigResponse(
                success = true,
                message = "VPN configurations generated successfully",
                configs = mapOf(
                    "osaka" to osakaConnectionConfig,
                    "paris" to parisConnectionConfig
                )
            )
            
        } catch (e: Exception) {
            logger.error("Error generating VPN configurations for user: $userId", e)
            return VPNConfigResponse(
                success = false,
                message = "Failed to generate VPN configurations",
                error = e.message
            )
        }
    }
    
    /**
     * Get available VPN servers
     */
    fun getAvailableVPNServers(): List<VPNServer> {
        logger.debug("Getting available VPN servers")
        return repository.getAvailableVPNServers()
    }
    
    /**
     * Get VPN server by ID
     */
    fun getVPNServerById(serverId: String): VPNServer? {
        logger.debug("Getting VPN server by ID: $serverId")
        return repository.getVPNServerById(serverId)
    }
    
    /**
     * Update connection status
     */
    fun updateConnectionStatus(
        userId: String,
        serverId: String,
        status: String,
        error: String? = null,
        bytesReceived: Long = 0,
        bytesSent: Long = 0
    ) {
        logger.info("Updating connection status for user: $userId, server: $serverId, status: $status")
        repository.updateConnectionStatus(userId, serverId, status, error, bytesReceived, bytesSent)
    }
    
    /**
     * Get connection status
     */
    fun getConnectionStatus(userId: String, serverId: String): VPNConnectionStatus? {
        logger.debug("Getting connection status for user: $userId, server: $serverId")
        return repository.getConnectionStatus(userId, serverId)
    }
    
    /**
     * Generate WireGuard configuration file content
     */
    private fun generateWireGuardConfig(server: VPNServer, clientConfig: VPNClientConfig): String {
        val dnsServers = server.dnsServers.joinToString(", ")
        
        return """
            [Interface]
            PrivateKey = ${clientConfig.privateKey}
            Address = ${clientConfig.address}/32
            DNS = $dnsServers
            MTU = ${server.mtu}
            
            [Peer]
            PublicKey = ${server.wireguardPublicKey}
            Endpoint = ${server.wireguardEndpoint}
            AllowedIPs = ${server.allowedIPs}
            PersistentKeepalive = 25
        """.trimIndent()
    }
    
    /**
     * Validate WireGuard configuration
     */
    fun validateWireGuardConfig(config: String): Boolean {
        return try {
            val lines = config.lines()
            val hasInterface = lines.any { it.startsWith("[Interface]") }
            val hasPeer = lines.any { it.startsWith("[Peer]") }
            val hasPrivateKey = lines.any { it.startsWith("PrivateKey = ") }
            val hasPublicKey = lines.any { it.startsWith("PublicKey = ") }
            val hasEndpoint = lines.any { it.startsWith("Endpoint = ") }
            val hasAddress = lines.any { it.startsWith("Address = ") }
            
            hasInterface && hasPeer && hasPrivateKey && hasPublicKey && hasEndpoint && hasAddress
        } catch (e: Exception) {
            logger.error("Error validating WireGuard configuration", e)
            false
        }
    }
    
    /**
     * Get VPN server statistics
     */
    fun getVPNServerStatistics(): Map<String, Any> {
        logger.debug("Getting VPN server statistics")
        
        val servers = repository.getAvailableVPNServers()
        
        return mapOf(
            "totalServers" to servers.size,
            "activeServers" to servers.count { it.isActive },
            "servers" to servers.map { server ->
                mapOf(
                    "id" to server.id,
                    "name" to server.name,
                    "city" to server.city,
                    "country" to server.country,
                    "ip" to server.ip,
                    "port" to server.port,
                    "isActive" to server.isActive,
                    "priority" to server.priority
                )
            }
        )
    }

    /**
     * Get minimal VPN configuration for Android app (exact format needed)
     */
    fun getMinimalVPNConfigForUser(userId: String): VPNConfigResponse {
        logger.info("Getting minimal VPN configuration for user: $userId")
        
        try {
            // Get or create client configurations for both servers
            val osakaClientConfig = repository.getOrCreateClientConfig(userId, "osaka")
            val parisClientConfig = repository.getOrCreateClientConfig(userId, "paris")
            
            // Create minimal configs in the exact format needed
            val osakaConfig = mapOf(
                "server_ip" to Env.osakaServerIp,
                "server_port" to Env.osakaServerPort,
                "server_public_key" to Env.osakaServerPublicKey,
                "client_private_key" to osakaClientConfig.privateKey,
                "client_public_key" to osakaClientConfig.publicKey,
                "allowed_ips" to "0.0.0.0/0",
                "dns" to "8.8.8.8"
            )
            
            val parisConfig = mapOf(
                "server_ip" to Env.parisServerIp,
                "server_port" to Env.parisServerPort,
                "server_public_key" to Env.parisServerPublicKey,
                "client_private_key" to parisClientConfig.privateKey,
                "client_public_key" to parisClientConfig.publicKey,
                "allowed_ips" to "0.0.0.0/0",
                "dns" to "8.8.8.8"
            )
            
            logger.info("Minimal VPN configurations generated successfully for user: $userId")
            
            return VPNConfigResponse(
                success = true,
                message = "Minimal VPN configurations generated successfully",
                configs = mapOf(
                    "osaka" to osakaConfig,
                    "paris" to parisConfig
                )
            )
            
        } catch (e: Exception) {
            logger.error("Error generating minimal VPN configuration for user: $userId", e)
            return VPNConfigResponse(
                success = false,
                message = "Failed to generate minimal VPN configuration",
                error = e.message
            )
        }
    }
}
