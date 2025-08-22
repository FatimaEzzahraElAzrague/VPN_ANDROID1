package com.example.v.config

import com.example.v.data.models.ServerLocation

/**
 * VPN Server configurations with locations and connection details
 * Only real VPN servers - Paris and Osaka
 */
object ServerConfig {
    // Server configurations - UPDATED TO MATCH DESKTOP APP EXACTLY
    private val SERVERS = mapOf(
        "paris" to ServerLocation(
            id = "paris",
            name = "Paris",
            country = "France",
            ip = "52.47.190.220", // Matches desktop main.py
            subnet = "10.77.26.0/24", // Matches desktop main.py
            agentPort = 8000, // Matches desktop main.py
            serverPublicKey = "yvB7acu9ncFFEyzw5n8L7kpLazTgQonML1PuhoStjjg=", // Matches desktop main.py
            listenPort = "51820", // Matches desktop main.py
            ipv6Base = "fd42:42:20::",
            region = "Europe"
        ),
        "osaka" to ServerLocation(
            id = "osaka",
            name = "Osaka",
            country = "Japan",
            ip = "15.168.240.118", // Matches desktop main.py
            subnet = "10.77.27.0/24", // Matches desktop main.py
            agentPort = 8000, // Matches desktop main.py
            serverPublicKey = "Hr1B3sNsDSxFpR+zO34qLGxutUK3wgaPwrsWoY2ViAM=", // Matches desktop main.py
            listenPort = "51820", // Matches desktop main.py
            ipv6Base = "fd42:42:42::",
            region = "Asia"
        )
    )

    // Constants
    const val AGENT_TOKEN = "vpn-agent-secret-token-2024"
    
    // DNS servers for Paris and Osaka servers only (internal VPN DNS)
    object FilteringDNS {
        const val DEFAULT = "10.0.2.1"          // Default DNS (no filtering)
        const val AD_BLOCK = "10.0.2.2"         // Ad blocking
        const val ANTI_MALWARE = "10.0.2.3"     // Anti-malware
        const val FULL_FILTERING = "10.0.2.4"   // All security features
        const val FAMILY_SAFE = "10.0.2.5"      // Family safe mode
    }

    // Public methods
    fun getAllServers(): List<ServerLocation> = SERVERS.values.toList()
    fun getServer(id: String): ServerLocation? = SERVERS[id]
    fun getServerIds(): List<String> = SERVERS.keys.toList()
}
