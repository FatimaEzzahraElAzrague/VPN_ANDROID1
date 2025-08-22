package com.myapp.backend.config

import com.myapp.backend.models.ServerLocation

/**
 * VPN Server configurations with locations and connection details
 * Only real VPN servers - Paris and Osaka
 */
object ServerConfig {
    private val SERVERS = mapOf(
        "paris" to ServerLocation(
            id = "paris",
            name = "Paris",
            country = "France",
            ip = "52.47.190.220",
            subnet = "10.77.26.0/24",
            agentPort = 8000,
            serverPublicKey = "yvB7acu9ncFFEyzw5n8L7kpLazTgQonML1PuhoStjjg=",
            listenPort = "51820",
            ipv6Base = "fd42:42:20::",
            region = "Europe"
        ),
        "osaka" to ServerLocation(
            id = "osaka",
            name = "Osaka",
            country = "Japan",
            ip = "15.168.240.118",
            subnet = "10.77.27.0/24",
            agentPort = 8000,
            serverPublicKey = "Hr1B3sNsDSxFpR+zO34qLGxutUK3wgaPwrsWoY2ViAM=",
            listenPort = "51820",
            ipv6Base = "fd42:42:42::",
            region = "Asia"
        )
    )

    // DNS servers for Paris and Osaka servers only (internal VPN DNS)
    object FilteringDNS {
        const val DEFAULT = "10.0.2.1"          // Default DNS (no filtering)
        const val AD_BLOCK = "10.0.2.2"         // Ad blocking
        const val ANTI_MALWARE = "10.0.2.3"     // Anti-malware
        const val FULL_FILTERING = "10.0.2.4"   // All security features
        const val FAMILY_SAFE = "10.0.2.5"      // Family safe mode
    }

    fun getServer(id: String): ServerLocation? = SERVERS[id]
    
    fun getAllServers(): List<ServerLocation> = SERVERS.values.toList()
    
    fun getServersByRegion(): Map<String, List<ServerLocation>> =
        SERVERS.values.groupBy { it.region }
}
