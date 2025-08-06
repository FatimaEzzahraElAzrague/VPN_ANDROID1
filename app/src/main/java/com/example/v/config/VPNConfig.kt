package com.example.v.config

import com.example.v.models.Server
import com.example.v.models.WireGuardConfig
import com.example.v.models.ClientConfig

/**
 * VPN Configuration for EC2 Paris Server
 * This class contains the server configuration for connecting to your EC2 instance in Paris
 */
object VPNConfig {
    
    /**
     * EC2 Paris Server Configuration
     * Real WireGuard configuration for Paris server
     */
    val parisServer = Server(
        id = "france-paris",
        name = "Europe (Paris)",
        country = "France",
        countryCode = "FR",
        city = "Paris",
        flag = "🇫🇷",
        ping = 25,
        load = 45,
        isOptimal = true,
        isPremium = true,
        isFavorite = true,
        latitude = 48.8566,
        longitude = 2.3522,
        wireGuardConfig = WireGuardConfig(
            serverPublicKey = "4R7eAjIIiG3Nr0DhzdLd6CQW4PeWXSJLBwaEKWxYqnA=", // Replace with your actual Paris server public key
            serverEndpoint = "13.38.83.180", // Your Paris EC2 IP address
            serverPort = 51820,
            allowedIPs = "0.0.0.0/0",
            dns = "1.1.1.1, 8.8.8.8"
        )
    )
    
    /**
     * Client Configuration for Paris Server
     * Real client configuration from WireGuard
     */
    val parisClientConfig = ClientConfig(
        privateKey = "OGzZVICGhTW+GGUW42Lq7/TpsMSlIphWhIDcTP9M6E8=",
        publicKey = "4R7eAjIIiG3Nr0DhzdLd6CQW4PeWXSJLBwaEKWxYqnA=", // Will be generated from private key
        address = "10.0.2.18/32,fd42:42:42::18/128",
        dns = "1.1.1.1, 8.8.8.8"
    )
    
    /**
     * List of available servers (you can add more servers here)
     */
    val availableServers = listOf(
        parisServer,
        // Add more servers as needed
        Server(
            id = "demo-server",
            name = "Demo Server",
            country = "Demo",
            countryCode = "DM",
            city = "Demo City",
            flag = "🌐",
            ping = 0,
            load = 0,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 0.0,
            longitude = 0.0
        )
    )
    
    /**
     * Default server (Paris EC2)
     */
    val defaultServer = parisServer
    
    /**
     * Application settings
     */
    object Settings {
        const val AUTO_CONNECT_ON_BOOT = true
        const val KILL_SWITCH_ENABLED = true
        const val USE_BIOMETRIC_AUTH = true
        const val CONNECTION_TIMEOUT_SECONDS = 30
        const val RECONNECT_ATTEMPTS = 3
        const val LOG_LEVEL = "INFO"
    }
}