package com.example.v.data

import com.example.v.models.Server
import com.example.v.models.WireGuardConfig

object ServersData {
    val servers = listOf(
        // Europe (Paris) - Real Paris Backend
        Server(
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
        ),
        // Asia Pacific (Osaka) - Real Osaka Backend
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
    
    fun getOptimalServer(): Server? {
        return servers.find { it.isOptimal }
    }
    
    fun getServersByRegion(region: String): List<Server> {
        return when (region.lowercase()) {
            "us" -> servers.filter { it.id.startsWith("us-") }
            "europe" -> servers.filter { it.id.startsWith("europe-") }
            "asia" -> servers.filter { it.id.startsWith("asia-pacific-") }
            "canada" -> servers.filter { it.id.startsWith("canada-") }
            "middle east" -> servers.filter { it.id.startsWith("middle-east-") }
            "africa" -> servers.filter { it.id.startsWith("africa-") }
            "south america" -> servers.filter { it.id.startsWith("south-america-") }
            "mexico" -> servers.filter { it.id.startsWith("mexico-") }
            "israel" -> servers.filter { it.id.startsWith("israel-") }
            else -> servers
        }
    }
}