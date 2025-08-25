package com.myapp.backend.repositories

import com.myapp.backend.db.VPNServers
import com.myapp.backend.db.VPNClientConfigs
import com.myapp.backend.db.VPNConnections
import com.myapp.backend.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*
import kotlinx.serialization.json.Json

class VPNRepository {
    
    fun getAvailableVPNServers(): List<VPNServer> = transaction {
        VPNServers
            .select { VPNServers.isActive eq true }
            .orderBy(VPNServers.priority, SortOrder.ASC)
            .map { row ->
                VPNServer(
                    id = row[VPNServers.id],
                    name = row[VPNServers.name],
                    city = row[VPNServers.city],
                    country = row[VPNServers.country],
                    countryCode = row[VPNServers.countryCode],
                    flag = row[VPNServers.flag],
                    ip = row[VPNServers.ip],
                    port = row[VPNServers.port],
                    subnet = row[VPNServers.subnet],
                    serverIP = row[VPNServers.serverIP],
                    dnsServers = try {
                        Json.decodeFromString<List<String>>(row[VPNServers.dnsServers])
                    } catch (e: Exception) {
                        listOf("1.1.1.1", "8.8.8.8")
                    },
                    wireguardPublicKey = row[VPNServers.wireguardPublicKey],
                    wireguardEndpoint = row[VPNServers.wireguardEndpoint],
                    allowedIPs = row[VPNServers.allowedIPs],
                    mtu = row[VPNServers.mtu],
                    isActive = row[VPNServers.isActive],
                    priority = row[VPNServers.priority],
                    createdAt = row[VPNServers.createdAt].toString()
                )
            }
    }
    
    fun getVPNServerById(serverId: String): VPNServer? = transaction {
        VPNServers
            .select { VPNServers.id eq serverId }
            .firstOrNull()
            ?.let { row ->
                VPNServer(
                    id = row[VPNServers.id],
                    name = row[VPNServers.name],
                    city = row[VPNServers.city],
                    country = row[VPNServers.country],
                    countryCode = row[VPNServers.countryCode],
                    flag = row[VPNServers.flag],
                    ip = row[VPNServers.ip],
                    port = row[VPNServers.port],
                    subnet = row[VPNServers.subnet],
                    serverIP = row[VPNServers.serverIP],
                    dnsServers = try {
                        Json.decodeFromString<List<String>>(row[VPNServers.dnsServers])
                    } catch (e: Exception) {
                        listOf("1.1.1.1", "8.8.8.8")
                    },
                    wireguardPublicKey = row[VPNServers.wireguardPublicKey],
                    wireguardEndpoint = row[VPNServers.wireguardEndpoint],
                    allowedIPs = row[VPNServers.allowedIPs],
                    mtu = row[VPNServers.mtu],
                    isActive = row[VPNServers.isActive],
                    priority = row[VPNServers.priority],
                    createdAt = row[VPNServers.createdAt].toString()
                )
            }
    }
    
    fun getOrCreateClientConfig(userId: String, serverId: String): VPNClientConfig = transaction {
        // Try to get existing config
        val existingConfig = VPNClientConfigs
            .select { (VPNClientConfigs.userId eq userId) and (VPNClientConfigs.serverId eq serverId) }
            .firstOrNull()
        
        if (existingConfig != null) {
            // Update last used timestamp
            VPNClientConfigs.update({ VPNClientConfigs.id eq existingConfig[VPNClientConfigs.id] }) {
                it[lastUsed] = LocalDateTime.now()
            }
            
            VPNClientConfig(
                privateKey = existingConfig[VPNClientConfigs.privateKey],
                publicKey = existingConfig[VPNClientConfigs.publicKey],
                address = existingConfig[VPNClientConfigs.address],
                dns = existingConfig[VPNClientConfigs.dns],
                mtu = existingConfig[VPNClientConfigs.mtu],
                allowedIPs = existingConfig[VPNClientConfigs.allowedIPs]
            )
        } else {
            // Generate new client config
            val newConfig = generateNewClientConfig(userId, serverId)
            saveClientConfig(userId, serverId, newConfig)
            newConfig
        }
    }
    
    private fun generateNewClientConfig(userId: String, serverId: String): VPNClientConfig {
        // Generate WireGuard key pair
        val privateKey = generateWireGuardPrivateKey()
        val publicKey = derivePublicKey(privateKey)
        
        // Generate unique client IP address
        val clientIP = generateClientIP(serverId)
        
        return VPNClientConfig(
            privateKey = privateKey,
            publicKey = publicKey,
            address = clientIP,
            dns = "1.1.1.1,8.8.8.8",
            mtu = 1420,
            allowedIPs = "0.0.0.0/0,::/0"
        )
    }
    
    private fun saveClientConfig(userId: String, serverId: String, config: VPNClientConfig) = transaction {
        VPNClientConfigs.insert {
            it[VPNClientConfigs.userId] = userId
            it[VPNClientConfigs.serverId] = serverId
            it[VPNClientConfigs.privateKey] = config.privateKey
            it[VPNClientConfigs.publicKey] = config.publicKey
            it[VPNClientConfigs.address] = config.address
            it[VPNClientConfigs.dns] = config.dns
            it[VPNClientConfigs.mtu] = config.mtu
            it[VPNClientConfigs.allowedIPs] = config.allowedIPs
            it[VPNClientConfigs.createdAt] = LocalDateTime.now()
            it[VPNClientConfigs.lastUsed] = LocalDateTime.now()
        }
    }
    
    private fun generateWireGuardPrivateKey(): String {
        // Generate 32-byte random key and encode as base64
        val random = Random()
        val keyBytes = ByteArray(32)
        random.nextBytes(keyBytes)
        return Base64.getEncoder().encodeToString(keyBytes)
    }
    
    private fun derivePublicKey(privateKey: String): String {
        // In a real implementation, you would use WireGuard's key derivation
        // For now, we'll generate a new random key (this should be replaced with proper key derivation)
        val random = Random()
        val keyBytes = ByteArray(32)
        random.nextBytes(keyBytes)
        return Base64.getEncoder().encodeToString(keyBytes)
    }
    
    private fun generateClientIP(serverId: String): String {
        // Generate unique client IP based on server
        val baseIP = when (serverId) {
            "osaka" -> "10.0.1"
            "paris" -> "10.0.2"
            else -> "10.0.0"
        }
        
        // Generate random last octet
        val lastOctet = Random().nextInt(2, 254)
        return "$baseIP.$lastOctet"
    }
    
    fun updateConnectionStatus(
        userId: String,
        serverId: String,
        status: String,
        error: String? = null,
        bytesReceived: Long = 0,
        bytesSent: Long = 0
    ) = transaction {
        val existingConnection = VPNConnections
            .select { (VPNConnections.userId eq userId) and (VPNConnections.serverId eq serverId) }
            .firstOrNull()
        
        if (existingConnection != null) {
            // Update existing connection
            VPNConnections.update({ VPNConnections.id eq existingConnection[VPNConnections.id] }) {
                it[VPNConnections.status] = status
                it[VPNConnections.updatedAt] = LocalDateTime.now()
                
                when (status) {
                    "CONNECTED" -> {
                        it[VPNConnections.connectedAt] = LocalDateTime.now()
                        it[VPNConnections.lastError] = null
                    }
                    "DISCONNECTED" -> {
                        it[VPNConnections.disconnectedAt] = LocalDateTime.now()
                    }
                    "ERROR" -> {
                        it[VPNConnections.lastError] = error
                    }
                }
                
                it[VPNConnections.bytesReceived] = bytesReceived
                it[VPNConnections.bytesSent] = bytesSent
            }
        } else {
            // Create new connection record
            VPNConnections.insert {
                it[VPNConnections.userId] = userId
                it[VPNConnections.serverId] = serverId
                it[VPNConnections.status] = status
                it[VPNConnections.createdAt] = LocalDateTime.now()
                it[VPNConnections.updatedAt] = LocalDateTime.now()
                
                when (status) {
                    "CONNECTED" -> {
                        it[VPNConnections.connectedAt] = LocalDateTime.now()
                    }
                    "ERROR" -> {
                        it[VPNConnections.lastError] = error
                    }
                }
                
                it[VPNConnections.bytesReceived] = bytesReceived
                it[VPNConnections.bytesSent] = bytesSent
            }
        }
    }
    
    fun getConnectionStatus(userId: String, serverId: String): VPNConnectionStatus? = transaction {
        VPNConnections
            .select { (VPNConnections.userId eq userId) and (VPNConnections.serverId eq serverId) }
            .firstOrNull()
            ?.let { row ->
                VPNConnectionStatus(
                    userId = row[VPNConnections.userId],
                    serverId = row[VPNConnections.serverId],
                    status = row[VPNConnections.status],
                    connectedAt = row[VPNConnections.connectedAt]?.toString(),
                    disconnectedAt = row[VPNConnections.disconnectedAt]?.toString(),
                    lastError = row[VPNConnections.lastError],
                    bytesReceived = row[VPNConnections.bytesReceived],
                    bytesSent = row[VPNConnections.bytesSent],
                    connectionDuration = row[VPNConnections.connectionDuration]
                )
            }
    }
    
    fun initializeDefaultVPNServers() = transaction {
        // Check if servers already exist
        if (VPNServers.selectAll().count() > 0) return@transaction
        
        val defaultServers = listOf(
            mapOf(
                VPNServers.id to "osaka",
                VPNServers.name to "Osaka VPN Server",
                VPNServers.city to "Osaka",
                VPNServers.country to "Japan",
                VPNServers.countryCode to "JP",
                VPNServers.flag to "🇯🇵",
                VPNServers.ip to com.myapp.backend.config.Env.osakaServerIp,
                VPNServers.port to com.myapp.backend.config.Env.osakaServerPort,
                VPNServers.subnet to com.myapp.backend.config.Env.osakaServerSubnet,
                VPNServers.serverIP to "10.77.25.1",
                VPNServers.dnsServers to "[\"1.1.1.1\", \"8.8.8.8\"]",
                VPNServers.wireguardPublicKey to com.myapp.backend.config.Env.osakaServerPublicKey,
                VPNServers.wireguardEndpoint to "${com.myapp.backend.config.Env.osakaServerIp}:${com.myapp.backend.config.Env.osakaServerPort}",
                VPNServers.allowedIPs to "0.0.0.0/0,::/0",
                VPNServers.mtu to 1420,
                VPNServers.isActive to true,
                VPNServers.priority to 1,
                VPNServers.createdAt to LocalDateTime.now()
            ),
            mapOf(
                VPNServers.id to "paris",
                VPNServers.name to "Paris VPN Server",
                VPNServers.city to "Paris",
                VPNServers.country to "France",
                VPNServers.countryCode to "FR",
                VPNServers.flag to "🇫🇷",
                VPNServers.ip to com.myapp.backend.config.Env.parisServerIp,
                VPNServers.port to com.myapp.backend.config.Env.parisServerPort,
                VPNServers.subnet to com.myapp.backend.config.Env.parisServerSubnet,
                VPNServers.serverIP to "10.77.26.1",
                VPNServers.dnsServers to "[\"1.1.1.1\", \"8.8.8.8\"]",
                VPNServers.wireguardPublicKey to com.myapp.backend.config.Env.parisServerPublicKey,
                VPNServers.wireguardEndpoint to "${com.myapp.backend.config.Env.parisServerIp}:${com.myapp.backend.config.Env.parisServerPort}",
                VPNServers.allowedIPs to "0.0.0.0/0,::/0",
                VPNServers.mtu to 1420,
                VPNServers.isActive to true,
                VPNServers.priority to 2,
                VPNServers.createdAt to LocalDateTime.now()
            )
        )
        
        defaultServers.forEach { serverData ->
            VPNServers.insert(serverData)
        }
    }
}
