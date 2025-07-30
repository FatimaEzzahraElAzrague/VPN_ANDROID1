package com.example.v.data

import com.example.v.models.Server

object ServersData {
    val servers = listOf(
        // US Servers
        Server(
            id = "us-east-1",
            country = "United States",
            city = "N. Virginia",
            flag = "🇺🇸",
            ping = 25,
            load = 15,
            isOptimal = true,
            isPremium = false,
            latitude = 38.95,
            longitude = -77.46
        ),
        Server(
            id = "us-west-1",
            country = "United States",
            city = "Oregon",
            flag = "🇺🇸",
            ping = 35,
            load = 22,
            isOptimal = false,
            isPremium = false,
            latitude = 45.52,
            longitude = -122.68
        ),
        
        // Europe Servers
        Server(
            id = "eu-london-1",
            country = "United Kingdom",
            city = "London",
            flag = "🇬🇧",
            ping = 45,
            load = 18,
            isOptimal = false,
            isPremium = false,
            latitude = 51.5074,
            longitude = -0.1278
        ),
        Server(
            id = "eu-zurich-1",
            country = "Switzerland",
            city = "Zurich",
            flag = "🇨🇭",
            ping = 42,
            load = 12,
            isOptimal = false,
            isPremium = true,
            latitude = 47.3769,
            longitude = 8.5417
        ),
        Server(
            id = "eu-paris-1",
            country = "France",
            city = "Paris",
            flag = "🇫🇷",
            ping = 38,
            load = 25,
            isOptimal = false,
            isPremium = false,
            latitude = 48.85,
            longitude = 2.35
        ),
        Server(
            id = "eu-frankfurt-1",
            country = "Germany",
            city = "Frankfurt",
            flag = "🇩🇪",
            ping = 40,
            load = 20,
            isOptimal = false,
            isPremium = false,
            latitude = 50.1109,
            longitude = 8.6821
        ),
        Server(
            id = "eu-spain-1",
            country = "Spain",
            city = "Madrid",
            flag = "🇪🇸",
            ping = 48,
            load = 28,
            isOptimal = false,
            isPremium = false,
            latitude = 40.4168,
            longitude = -3.7038
        ),
        Server(
            id = "eu-milan-1",
            country = "Italy",
            city = "Milan",
            flag = "🇮🇹",
            ping = 45,
            load = 30,
            isOptimal = false,
            isPremium = false,
            latitude = 45.4642,
            longitude = 9.19
        ),
        
        // Canada
        Server(
            id = "ca-west-1",
            country = "Canada",
            city = "Calgary",
            flag = "🇨🇦",
            ping = 30,
            load = 18,
            isOptimal = false,
            isPremium = false,
            latitude = 51.0447,
            longitude = -114.0719
        ),
        
        // Middle East
        Server(
            id = "me-uae-1",
            country = "UAE",
            city = "Dubai",
            flag = "🇦🇪",
            ping = 85,
            load = 35,
            isOptimal = false,
            isPremium = true,
            latitude = 25.2048,
            longitude = 55.2708
        ),
        
        // Africa
        Server(
            id = "af-cape-town-1",
            country = "South Africa",
            city = "Cape Town",
            flag = "🇿🇦",
            ping = 120,
            load = 45,
            isOptimal = false,
            isPremium = true,
            latitude = -33.9249,
            longitude = 18.4241
        ),
        
        // Asia Pacific
        Server(
            id = "ap-hong-kong-1",
            country = "Hong Kong",
            city = "Hong Kong",
            flag = "🇭🇰",
            ping = 95,
            load = 40,
            isOptimal = false,
            isPremium = true,
            latitude = 22.3193,
            longitude = 114.1694
        ),
        Server(
            id = "ap-mumbai-1",
            country = "India",
            city = "Mumbai",
            flag = "🇮🇳",
            ping = 110,
            load = 50,
            isOptimal = false,
            isPremium = false,
            latitude = 19.076,
            longitude = 72.8777
        ),
        Server(
            id = "ap-singapore-1",
            country = "Singapore",
            city = "Singapore",
            flag = "🇸🇬",
            ping = 90,
            load = 32,
            isOptimal = false,
            isPremium = false,
            latitude = 1.3521,
            longitude = 103.8198
        ),
        Server(
            id = "ap-sydney-1",
            country = "Australia",
            city = "Sydney",
            flag = "🇦🇺",
            ping = 130,
            load = 38,
            isOptimal = false,
            isPremium = false,
            latitude = -33.8688,
            longitude = 151.2093
        ),
        Server(
            id = "ap-jakarta-1",
            country = "Indonesia",
            city = "Jakarta",
            flag = "🇮🇩",
            ping = 115,
            load = 55,
            isOptimal = false,
            isPremium = false,
            latitude = -6.2088,
            longitude = 106.8456
        ),
        Server(
            id = "ap-malaysia-1",
            country = "Malaysia",
            city = "Kuala Lumpur",
            flag = "🇲🇾",
            ping = 105,
            load = 42,
            isOptimal = false,
            isPremium = false,
            latitude = 3.139,
            longitude = 101.6869
        ),
        Server(
            id = "ap-seoul-1",
            country = "South Korea",
            city = "Seoul",
            flag = "🇰🇷",
            ping = 100,
            load = 35,
            isOptimal = false,
            isPremium = false,
            latitude = 37.5665,
            longitude = 126.978
        ),
        Server(
            id = "ap-taipei-1",
            country = "Taiwan",
            city = "Taipei",
            flag = "🇹🇼",
            ping = 105,
            load = 40,
            isOptimal = false,
            isPremium = false,
            latitude = 25.0329,
            longitude = 121.5654
        ),
        Server(
            id = "ap-tokyo-1",
            country = "Japan",
            city = "Tokyo",
            flag = "🇯🇵",
            ping = 95,
            load = 30,
            isOptimal = false,
            isPremium = false,
            latitude = 35.6895,
            longitude = 139.6917
        ),
        
        // South America
        Server(
            id = "sa-sao-paulo-1",
            country = "Brazil",
            city = "Sao Paulo",
            flag = "🇧🇷",
            ping = 140,
            load = 60,
            isOptimal = false,
            isPremium = true,
            latitude = -23.5505,
            longitude = -46.6333
        ),
        
        // Mexico
        Server(
            id = "mx-central-1",
            country = "Mexico",
            city = "Mexico City",
            flag = "🇲🇽",
            ping = 50,
            load = 25,
            isOptimal = false,
            isPremium = false,
            latitude = 19.4326,
            longitude = -99.1332
        ),
        
        // Israel
        Server(
            id = "il-tel-aviv-1",
            country = "Israel",
            city = "Tel Aviv",
            flag = "🇮🇱",
            ping = 80,
            load = 35,
            isOptimal = false,
            isPremium = true,
            latitude = 32.0853,
            longitude = 34.7818
        )
    )
    
    fun getOptimalServer(): Server? {
        return servers.find { it.isOptimal }
    }
    
    fun getServersByRegion(region: String): List<Server> {
        return when (region.lowercase()) {
            "us" -> servers.filter { it.id.startsWith("us-") }
            "europe" -> servers.filter { it.id.startsWith("eu-") }
            "asia" -> servers.filter { it.id.startsWith("ap-") }
            "canada" -> servers.filter { it.id.startsWith("ca-") }
            "middle east" -> servers.filter { it.id.startsWith("me-") }
            "africa" -> servers.filter { it.id.startsWith("af-") }
            "south america" -> servers.filter { it.id.startsWith("sa-") }
            "mexico" -> servers.filter { it.id.startsWith("mx-") }
            "israel" -> servers.filter { it.id.startsWith("il-") }
            else -> servers
        }
    }
}