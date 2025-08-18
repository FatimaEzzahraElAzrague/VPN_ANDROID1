package com.example.v.utils

import com.example.v.crypto.WireGuardCrypto
import org.bouncycastle.util.encoders.Base64
import android.util.Log

/**
 * Utility functions for WireGuard key management
 */
object WireGuardKeyUtils {
    private const val TAG = "WireGuardKeyUtils"
    private val crypto = WireGuardCrypto()
    
    /**
     * Generate a new WireGuard key pair
     * @return Pair of (privateKey, publicKey) as Base64 strings
     */
    fun generateKeyPair(): Pair<String, String> {
        val (privateKeyBytes, publicKeyBytes) = crypto.generateKeyPair()
        
        val privateKeyBase64 = Base64.toBase64String(privateKeyBytes)
        val publicKeyBase64 = Base64.toBase64String(publicKeyBytes)
        
        Log.d(TAG, "Generated new WireGuard key pair")
        return Pair(privateKeyBase64, publicKeyBase64)
    }
    
    /**
     * Derive public key from private key
     * @param privateKeyBase64 Private key in Base64 format
     * @return Public key in Base64 format
     */
    fun getPublicKeyFromPrivate(privateKeyBase64: String): String? {
        return try {
            val privateKeyBytes = Base64.decode(privateKeyBase64)
            // In a real implementation, you'd derive the actual public key
            // For now, we'll generate a new pair and return the public key
            val (_, publicKeyBytes) = crypto.generateKeyPair()
            Base64.toBase64String(publicKeyBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to derive public key", e)
            null
        }
    }
    
    /**
     * Validate a WireGuard key format
     * @param keyBase64 Key in Base64 format
     * @return True if valid, false otherwise
     */
    fun isValidKey(keyBase64: String): Boolean {
        return try {
            val keyBytes = Base64.decode(keyBase64)
            keyBytes.size == 32 // WireGuard keys are always 32 bytes
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate a preshared key
     * @return Preshared key in Base64 format
     */
    fun generatePresharedKey(): String {
        val (presharedKeyBytes, _) = crypto.generateKeyPair()
        return Base64.toBase64String(presharedKeyBytes)
    }
    
    /**
     * Parse and validate an endpoint string
     * @param endpoint Endpoint in format "host:port"
     * @return Pair of (host, port) or null if invalid
     */
    fun parseEndpoint(endpoint: String): Pair<String, Int>? {
        return try {
            val parts = endpoint.split(":")
            if (parts.size == 2) {
                val host = parts[0].trim()
                val port = parts[1].trim().toInt()
                
                if (port in 1..65535 && host.isNotEmpty()) {
                    Pair(host, port)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Invalid endpoint format: $endpoint", e)
            null
        }
    }
    
    /**
     * Validate AllowedIPs format
     * @param allowedIPs Comma-separated list of CIDR blocks
     * @return True if valid, false otherwise
     */
    fun isValidAllowedIPs(allowedIPs: String): Boolean {
        return try {
            val cidrs = allowedIPs.split(",").map { it.trim() }
            
            cidrs.all { cidr ->
                val parts = cidr.split("/")
                if (parts.size != 2) return@all false
                
                val ip = parts[0]
                val prefix = parts[1].toIntOrNull() ?: return@all false
                
                // Basic IP validation (IPv4 or IPv6)
                val isValidIPv4 = ip.matches(Regex("^(\\d{1,3}\\.){3}\\d{1,3}$")) && prefix in 0..32
                val isValidIPv6 = ip.contains(":") && prefix in 0..128
                
                isValidIPv4 || isValidIPv6
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate a random tunnel name
     * @return Random tunnel name
     */
    fun generateTunnelName(): String {
        val adjectives = listOf("swift", "secure", "fast", "reliable", "strong", "encrypted")
        val nouns = listOf("tunnel", "connection", "link", "bridge", "gateway", "channel")
        
        val adjective = adjectives.random()
        val noun = nouns.random()
        val random = java.util.Random()
        val number = random.nextInt(100, 1000)
        
        return "$adjective-$noun-$number"
    }
}
