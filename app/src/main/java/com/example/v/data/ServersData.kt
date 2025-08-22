package com.example.v.data

import com.example.v.models.Server

object ServersData {
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
            latency = 25,
            isConnected = false,
            countryCode = "FR",
            ping = 25,
            load = 45
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
            latency = 45,
            isConnected = false,
            countryCode = "JP",
            ping = 45,
            load = 35
        )
    )
}