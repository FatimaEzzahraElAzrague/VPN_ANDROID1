package com.example.v.config

import com.example.v.models.Server

object VPNConfig {
    val servers = listOf(
        Server(
            id = "paris",
            name = "Paris",
            city = "Paris",
            country = "France",
            flag = "🇫🇷",
            ip = "52.47.190.220",
            port = 51820,
            subnet = "10.77.26.0/24",
            serverIP = "10.77.26.2",
            dnsServers = listOf("10.0.2.1", "10.0.2.2", "10.0.2.3", "10.0.2.4", "10.0.2.5"),
            latency = 0,
            isConnected = false
        ),
        Server(
            id = "osaka",
            name = "Osaka",
            city = "Osaka",
            country = "Japan",
            flag = "🇯🇵",
            ip = "15.168.240.118",
            port = 51820,
            subnet = "10.77.27.0/24",
            serverIP = "10.77.27.1",
            dnsServers = listOf("10.0.2.1", "10.0.2.2", "10.0.2.3", "10.0.2.4", "10.0.2.5"),
            latency = 0,
            isConnected = false
        )
    )
    
    fun getServerById(id: String): Server? {
        return servers.find { it.id == id }
    }
    
    fun getServerByLocation(location: String): Server? {
        return servers.find { it.city.equals(location, ignoreCase = true) || it.id.equals(location, ignoreCase = true) }
    }
    
    fun getAllServers(): List<Server> {
        return servers
    }
    
    fun getServersByRegion(region: String): List<Server> {
        return when (region.lowercase()) {
            "europe" -> servers.filter { it.id == "paris" }
            "asia" -> servers.filter { it.id == "osaka" }
            else -> servers
        }
    }
}