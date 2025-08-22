package com.myapp.backend.services

import com.myapp.backend.config.ServerConfig
import com.myapp.backend.models.VPNConnectionRequest
import com.myapp.backend.models.VPNConnectionResponse
import com.myapp.backend.config.Env
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import mu.KotlinLogging
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import java.security.SecureRandom
import java.util.*
import kotlin.text.Charsets.UTF_8

private val logger = KotlinLogging.logger {}

class VPNService {
    private val client = HttpClient()
    private val secureRandom = SecureRandom()

    suspend fun createConnection(request: VPNConnectionRequest): VPNConnectionResponse {
        logger.info { "Creating VPN connection for location: ${request.location}" }
        
        try {
            // Get server configuration
            val server = ServerConfig.getServer(request.location) ?: throw IllegalArgumentException("Invalid location: ${request.location}")
            logger.debug { "Using server: ${server.name} (${server.ip}:${server.listenPort})" }
        
        // Generate WireGuard keys
        val (privateKey, publicKey) = generateWireGuardKeys()
        logger.debug { "Generated WireGuard keys" }
        
        // Generate preshared key
        val presharedKey = generatePresharedKey()
        logger.debug { "Generated preshared key" }
        
        // Get next available IP
        val usedIPs = getUsedIPs(server.ip, server.agentPort)
        val internalIP = getNextAvailableIP(usedIPs, server.subnet)
        logger.debug { "Assigned internal IP: $internalIP" }
        
        // Generate IPv6 address
        val ipv6 = "${server.ipv6Base}${internalIP.split('.').last()}"
        logger.debug { "Assigned internal IPv6: $ipv6" }
        
        // Add peer to WireGuard server
        addPeerToAgent(
            serverIP = server.ip,
            agentPort = server.agentPort,
            publicKey = publicKey,
            internalIP = internalIP,
            internalIPv6 = ipv6,
            presharedKey = presharedKey
        )
        
        // Determine DNS servers based on filtering options
        val dns = when {
            request.adBlockEnabled && request.antiMalwareEnabled && request.familySafeModeEnabled -> {
                ServerConfig.FilteringDNS.FULL_FILTERING
            }
            request.antiMalwareEnabled -> {
                ServerConfig.FilteringDNS.ANTI_MALWARE
            }
            request.adBlockEnabled -> {
                ServerConfig.FilteringDNS.AD_BLOCK
            }
            request.familySafeModeEnabled -> {
                ServerConfig.FilteringDNS.FAMILY_SAFE
            }
            else -> {
                ServerConfig.FilteringDNS.DEFAULT
            }
        }
        
        // Build client configuration
        val clientConfig = buildClientConfig(
            privateKey = privateKey,
            internalIP = internalIP,
            internalIPv6 = ipv6,
            dns = dns,
            serverPublicKey = server.serverPublicKey,
            presharedKey = presharedKey,
            serverEndpoint = "${server.ip}:${server.listenPort}"
        )
        
        val response = VPNConnectionResponse(
            privateKey = privateKey,
            publicKey = publicKey,
            serverPublicKey = server.serverPublicKey,
            serverEndpoint = "${server.ip}:${server.listenPort}",
            allowedIPs = "0.0.0.0/0,::/0",
            internalIP = internalIP,
            dns = dns,
            mtu = 1420,
            presharedKey = presharedKey,
            internalIPv6 = ipv6,
            clientConfig = clientConfig
        )
        logger.info { "VPN connection created successfully for ${request.location}" }
        return response
        } catch (e: Exception) {
            logger.error(e) { "Failed to create VPN connection for ${request.location}" }
            throw e
        }
    }

    private fun generateWireGuardKeys(): Pair<String, String> {
        val generator = X25519KeyPairGenerator()
        generator.init(X25519KeyGenerationParameters(secureRandom))
        val keyPair = generator.generateKeyPair()
        
        val privateKey = keyPair.private as X25519PrivateKeyParameters
        val publicKey = keyPair.public as X25519PublicKeyParameters
        
        return Base64.getEncoder().encodeToString(privateKey.encoded) to
               Base64.getEncoder().encodeToString(publicKey.encoded)
    }

    private fun generatePresharedKey(): String {
        val key = ByteArray(32)
        secureRandom.nextBytes(key)
        return Base64.getEncoder().encodeToString(key)
    }

    private suspend fun getUsedIPs(serverIP: String, agentPort: Int): Set<String> {
        return try {
            val response = client.get("http://$serverIP:$agentPort/api/used-ips") {
                header("Authorization", "Bearer ${Env.agentToken}")
            }
            
            if (response.status == HttpStatusCode.OK) {
                val json = Json.parseToJsonElement(response.bodyAsText())
                json.jsonObject["used_ips"]?.jsonArray
                    ?.mapNotNull { it.jsonPrimitive.contentOrNull?.split('/')?.first() }
                    ?.toSet() ?: emptySet()
            } else {
                logger.warn { "Failed to get used IPs from agent: ${response.status}" }
                emptySet()
            }
        } catch (e: Exception) {
            logger.error(e) { "Error getting used IPs from agent" }
            emptySet()
        }
    }

    private fun getNextAvailableIP(usedIPs: Set<String>, subnet: String): String {
        val parts = subnet.split('/')
        val baseIP = parts[0].split('.')
        val prefix = baseIP.take(3).joinToString(".")
        
        // Start from .10 to avoid network, gateway, and DNS server addresses
        for (i in 10..254) {
            val candidate = "$prefix.$i"
            if (candidate !in usedIPs) {
                return candidate
            }
        }
        throw IllegalStateException("No available IPs in subnet")
    }

    private suspend fun addPeerToAgent(
        serverIP: String,
        agentPort: Int,
        publicKey: String,
        internalIP: String,
        internalIPv6: String,
        presharedKey: String
    ) {
        val response = client.post("http://$serverIP:$agentPort/api/peers") {
            header("Authorization", "Bearer ${Env.agentToken}")
            contentType(ContentType.Application.Json)
            setBody(
                if (agentPort == 8000) {
                    // Legacy API format
                    """{"pubkey":"$publicKey","preshared_key":"$presharedKey","ip":"$internalIP/32"}"""
                } else {
                    // New API format
                    """{"pubkey":"$publicKey","preshared_key":"$presharedKey","ips":["$internalIP/32","$internalIPv6/128"]}"""
                }
            )
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Failed to add peer to agent: ${response.status}")
        }
    }

    private fun buildClientConfig(
        privateKey: String,
        internalIP: String,
        internalIPv6: String,
        dns: String,
        serverPublicKey: String,
        presharedKey: String,
        serverEndpoint: String,
        allowedIPs: String = "0.0.0.0/0,::/0"
    ): String {
        val gatewayIP = internalIP.split('.').take(3).joinToString(".") + ".1"
        
        return """
            [Interface]
            PrivateKey = $privateKey
            Address = $internalIP/32,$internalIPv6/128
            DNS = $dns
            MTU = 1420

            [Peer]
            PublicKey = $serverPublicKey
            PresharedKey = $presharedKey
            Endpoint = $serverEndpoint
            AllowedIPs = $allowedIPs
            PersistentKeepalive = 25
        """.trimIndent()
    }

    companion object {
        private var instance: VPNService? = null
        
        fun getInstance(): VPNService {
            if (instance == null) {
                instance = VPNService()
            }
            return instance!!
        }
    }
}
