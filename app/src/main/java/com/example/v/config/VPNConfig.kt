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
            serverPublicKey = "MlQThMgSYAAT2cpNpeKj2Y4eZoC90NSK8Y5xzIF1IlE=",
            serverEndpoint = "13.38.83.180",
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
        privateKey = "kMphb0Ww8aGc1kxHfbt2FL+q6YkPOvoP9xOxJuK3dFQ=",
        publicKey = "", // Not used by current implementation
        address = "10.66.66.2/32,fd42:42:42::2/128",
        dns = "1.1.1.1,1.1.1.1"
    )
    
    /**
     * List of available servers (you can add more servers here)
     */
    val availableServers = listOf(
        parisServer,
        // Add Osaka server
        Server(
            id = "asia-pacific-osaka",
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
                serverPublicKey = "hvyFo271WqTOpw6sS0dgMfnRde2J0DKlkMSCl/3PFUo=",
                serverEndpoint = "56.155.92.31",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0,::/0",
                dns = "1.1.1.1,1.1.1.1",
                presharedKey = "OkW8zpjy57QcniepR66O0+awsoN+7/C3WVWnQxxhAK4="
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