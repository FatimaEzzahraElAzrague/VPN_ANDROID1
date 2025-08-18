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
     * VPN API Configuration
     * Centralized VPN management system configuration
     */
    object VPNApiConfig {
        // VPN API server running on your desktop (same as desktop version)
        const val BASE_URL = "http://192.168.100.58:8000" // Your desktop IP
        const val AGENT_TOKEN = "vpn-agent-secret-token-2024" // Same as desktop version
    }
    
    /**
     * EC2 Paris Server Configuration
     * Real WireGuard configuration for Paris server
     */
    val parisServer = Server(
        id = "paris", // Changed to match API location format
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
            serverPublicKey = "yvB7acu9ncFFEyzw5n8L7kpLazTgQonML1PuhoStjjg=", // Matches desktop API
            serverEndpoint = "52.47.190.220", // Paris server IP from desktop API
            serverPort = 51820,
            allowedIPs = "0.0.0.0/0,::/0",
            dns = "1.1.1.1,8.8.8.8",
            presharedKey = "EgWKN/5Nryhv3F1F7NZVdzF1qOqK4XjyDRV/nlGaWM0="
        )
    )
    
    /**
     * Client Configuration for Paris Server
     * Real client configuration from WireGuard - FIXED to match server expectations
     */
    val parisClientConfig = ClientConfig(
        privateKey = "kE3EMeOqSOPbf4I7rA/CqkCQA8DShPQQbxJsPg5xfFY=",
        publicKey = "", // Not used by current implementation
        address = "10.0.2.2/32,fd42:42:42::2/128",
        dns = "1.1.1.1,8.8.8.8"
    )
    
    /**
     * Client Configuration for Osaka Server
     * Real client configuration from WireGuard
     */
    val osakaClientConfig = ClientConfig(
        privateKey = "4AS5cvcLvdzso8wbzgBrxUx223PVkHVe8UYphvVQGEI=",
        publicKey = "", // Not used by current implementation
        address = "10.77.27.2/32,fd42:42:21::2/128",
        dns = "1.1.1.1,8.8.8.8"
    )
    
    /**
     * List of available servers (you can add more servers here)
     */
    val availableServers = listOf(
        parisServer,
        // Add Osaka server
        Server(
            id = "osaka", // Changed to match API location format
            name = "Asia Pacific (Osaka)",
            country = "Japan",
            countryCode = "JP",
            city = "Osaka",
            flag = "🇯🇵",
            ping = 45,
            load = 35,
            isOptimal = false,
            isPremium = true,
            isFavorite = false,
            latitude = 34.6937,
            longitude = 135.5023,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "Hr1B3sNsDSxFpR+zO34qLGxutUK3wgaPwrsWoY2ViAM=", // Matches desktop API
                serverEndpoint = "15.168.240.118", // Osaka server IP from desktop API
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0,::/0",
                dns = "1.1.1.1,8.8.8.8",
                presharedKey = "BOJ/gcaaEa/o0RXY/gyUXBjcn2Wi5QtaNgS4L4gPd+o="
            )
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
        const val KILL_SWITCH_ENABLED = false  // Disabled to avoid conflicts with GoBackend
        const val USE_BIOMETRIC_AUTH = true
        const val CONNECTION_TIMEOUT_SECONDS = 30
        const val RECONNECT_ATTEMPTS = 3
        const val LOG_LEVEL = "INFO"
    }
}