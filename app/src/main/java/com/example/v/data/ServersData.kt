package com.example.v.data

import com.example.v.models.Server
import com.example.v.models.WireGuardConfig

object ServersData {
    val servers = listOf(
        // US East (N. Virginia)
        Server(
            id = "us-east-virginia",
            name = "US East (N. Virginia)",
            country = "United States",
            countryCode = "US",
            city = "N. Virginia",
            flag = "🇺🇸",
            ping = 25,
            load = 45,
            isOptimal = true,
            isPremium = true,
            isFavorite = false,
            latitude = 38.9072,
            longitude = -77.0369,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_us_east_virginia",
                serverEndpoint = "us-east-virginia.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // US West (Oregon)
        Server(
            id = "us-west-oregon",
            name = "US West (Oregon)",
            country = "United States",
            countryCode = "US",
            city = "Oregon",
            flag = "🇺🇸",
            ping = 35,
            load = 30,
            isOptimal = true,
            isPremium = false,
            isFavorite = false,
            latitude = 44.0582,
            longitude = -121.3153,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_us_west_oregon",
                serverEndpoint = "us-west-oregon.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Europe (London)
        Server(
            id = "europe-london",
            name = "Europe (London)",
            country = "United Kingdom",
            countryCode = "GB",
            city = "London",
            flag = "🇬🇧",
            ping = 40,
            load = 55,
            isOptimal = false,
            isPremium = true,
            isFavorite = false,
            latitude = 51.5074,
            longitude = -0.1278,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_europe_london",
                serverEndpoint = "europe-london.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Europe (Zurich)
        Server(
            id = "europe-zurich",
            name = "Europe (Zurich)",
            country = "Switzerland",
            countryCode = "CH",
            city = "Zurich",
            flag = "🇨🇭",
            ping = 45,
            load = 25,
            isOptimal = false,
            isPremium = true,
            isFavorite = false,
            latitude = 47.3769,
            longitude = 8.5417,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_europe_zurich",
                serverEndpoint = "europe-zurich.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Canada West (Calgary)
        Server(
            id = "canada-west-calgary",
            name = "Canada West (Calgary)",
            country = "Canada",
            countryCode = "CA",
            city = "Calgary",
            flag = "🇨🇦",
            ping = 50,
            load = 60,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 51.0447,
            longitude = -114.0719,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_canada_west_calgary",
                serverEndpoint = "canada-west-calgary.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
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
                serverPublicKey = "YOUR_PARIS_SERVER_PUBLIC_KEY", // Replace with your actual Paris server public key
                serverEndpoint = "13.38.83.180", // Your Paris EC2 IP address
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Europe (Frankfurt)
        Server(
            id = "europe-frankfurt",
            name = "Europe (Frankfurt)",
            country = "Germany",
            countryCode = "DE",
            city = "Frankfurt",
            flag = "🇩🇪",
            ping = 60,
            load = 35,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 50.1109,
            longitude = 8.6821,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_europe_frankfurt",
                serverEndpoint = "europe-frankfurt.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Europe (Spain)
        Server(
            id = "europe-spain",
            name = "Europe (Spain)",
            country = "Spain",
            countryCode = "ES",
            city = "Madrid",
            flag = "🇪🇸",
            ping = 65,
            load = 30,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 40.4168,
            longitude = -3.7038,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_europe_spain",
                serverEndpoint = "europe-spain.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Europe (Milan)
        Server(
            id = "europe-milan",
            name = "Europe (Milan)",
            country = "Italy",
            countryCode = "IT",
            city = "Milan",
            flag = "🇮🇹",
            ping = 70,
            load = 45,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 45.4642,
            longitude = 9.1900,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_europe_milan",
                serverEndpoint = "europe-milan.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Middle East (UAE)
        Server(
            id = "middle-east-uae",
            name = "Middle East (UAE)",
            country = "United Arab Emirates",
            countryCode = "AE",
            city = "Dubai",
            flag = "🇦🇪",
            ping = 75,
            load = 25,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 25.2048,
            longitude = 55.2708,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_middle_east_uae",
                serverEndpoint = "middle-east-uae.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Africa (Cape Town)
        Server(
            id = "africa-cape-town",
            name = "Africa (Cape Town)",
            country = "South Africa",
            countryCode = "ZA",
            city = "Cape Town",
            flag = "🇿🇦",
            ping = 80,
            load = 20,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = -33.9249,
            longitude = 18.4241,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_africa_cape_town",
                serverEndpoint = "africa-cape-town.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Asia Pacific (Hong Kong)
        Server(
            id = "asia-pacific-hong-kong",
            name = "Asia Pacific (Hong Kong)",
            country = "Hong Kong",
            countryCode = "HK",
            city = "Hong Kong",
            flag = "🇭🇰",
            ping = 85,
            load = 40,
            isOptimal = false,
            isPremium = true,
            isFavorite = false,
            latitude = 22.3193,
            longitude = 114.1694,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_asia_pacific_hong_kong",
                serverEndpoint = "asia-pacific-hong-kong.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Asia Pacific (Mumbai)
        Server(
            id = "asia-pacific-mumbai",
            name = "Asia Pacific (Mumbai)",
            country = "India",
            countryCode = "IN",
            city = "Mumbai",
            flag = "🇮🇳",
            ping = 90,
            load = 60,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 19.0760,
            longitude = 72.8777,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_asia_pacific_mumbai",
                serverEndpoint = "asia-pacific-mumbai.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Asia Pacific (Singapore)
        Server(
            id = "asia-pacific-singapore",
            name = "Asia Pacific (Singapore)",
            country = "Singapore",
            countryCode = "SG",
            city = "Singapore",
            flag = "🇸🇬",
            ping = 95,
            load = 35,
            isOptimal = false,
            isPremium = true,
            isFavorite = false,
            latitude = 1.3521,
            longitude = 103.8198,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_asia_pacific_singapore",
                serverEndpoint = "asia-pacific-singapore.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Mexico (Central)
        Server(
            id = "mexico-central",
            name = "Mexico (Central)",
            country = "Mexico",
            countryCode = "MX",
            city = "Mexico City",
            flag = "🇲🇽",
            ping = 100,
            load = 50,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 19.4326,
            longitude = -99.1332,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_mexico_central",
                serverEndpoint = "mexico-central.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Asia Pacific (Sydney)
        Server(
            id = "asia-pacific-sydney",
            name = "Asia Pacific (Sydney)",
            country = "Australia",
            countryCode = "AU",
            city = "Sydney",
            flag = "🇦🇺",
            ping = 105,
            load = 20,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = -33.8688,
            longitude = 151.2093,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_asia_pacific_sydney",
                serverEndpoint = "asia-pacific-sydney.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Asia Pacific (Jakarta)
        Server(
            id = "asia-pacific-jakarta",
            name = "Asia Pacific (Jakarta)",
            country = "Indonesia",
            countryCode = "ID",
            city = "Jakarta",
            flag = "🇮🇩",
            ping = 110,
            load = 55,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = -6.2088,
            longitude = 106.8456,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_asia_pacific_jakarta",
                serverEndpoint = "asia-pacific-jakarta.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Asia Pacific (Malaysia)
        Server(
            id = "asia-pacific-malaysia",
            name = "Asia Pacific (Malaysia)",
            country = "Malaysia",
            countryCode = "MY",
            city = "Kuala Lumpur",
            flag = "🇲🇾",
            ping = 115,
            load = 45,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 3.1390,
            longitude = 101.6869,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_asia_pacific_malaysia",
                serverEndpoint = "asia-pacific-malaysia.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Asia Pacific (Seoul)
        Server(
            id = "asia-pacific-seoul",
            name = "Asia Pacific (Seoul)",
            country = "South Korea",
            countryCode = "KR",
            city = "Seoul",
            flag = "🇰🇷",
            ping = 120,
            load = 30,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 37.5665,
            longitude = 126.9780,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_asia_pacific_seoul",
                serverEndpoint = "asia-pacific-seoul.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Asia Pacific (Taipei)
        Server(
            id = "asia-pacific-taipei",
            name = "Asia Pacific (Taipei)",
            country = "Taiwan",
            countryCode = "TW",
            city = "Taipei",
            flag = "🇹🇼",
            ping = 125,
            load = 35,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 25.0330,
            longitude = 121.5654,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_asia_pacific_taipei",
                serverEndpoint = "asia-pacific-taipei.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Asia Pacific (Tokyo)
        Server(
            id = "asia-pacific-tokyo",
            name = "Asia Pacific (Tokyo)",
            country = "Japan",
            countryCode = "JP",
            city = "Tokyo",
            flag = "🇯🇵",
            ping = 130,
            load = 25,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 35.6762,
            longitude = 139.6503,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_asia_pacific_tokyo",
                serverEndpoint = "asia-pacific-tokyo.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Asia Pacific (Osaka)
        Server(
            id = "asia-pacific-osaka",
            name = "Asia Pacific (Osaka)",
            country = "Japan",
            countryCode = "JP",
            city = "Osaka",
            flag = "🇯🇵",
            ping = 135,
            load = 30,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 34.6937,
            longitude = 135.5023,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_asia_pacific_osaka",
                serverEndpoint = "asia-pacific-osaka.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // South America (Sao Paulo)
        Server(
            id = "south-america-sao-paulo",
            name = "South America (Sao Paulo)",
            country = "Brazil",
            countryCode = "BR",
            city = "São Paulo",
            flag = "🇧🇷",
            ping = 135,
            load = 40,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = -23.5505,
            longitude = -46.6333,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_south_america_sao_paulo",
                serverEndpoint = "south-america-sao-paulo.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
            )
        ),
        // Israel (Tel Aviv)
        Server(
            id = "israel-tel-aviv",
            name = "Israel (Tel Aviv)",
            country = "Israel",
            countryCode = "IL",
            city = "Tel Aviv",
            flag = "🇮🇱",
            ping = 140,
            load = 30,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = 32.0853,
            longitude = 34.7818,
            wireGuardConfig = WireGuardConfig(
                serverPublicKey = "public_key_israel_tel_aviv",
                serverEndpoint = "israel-tel-aviv.vpn.example.com",
                serverPort = 51820,
                allowedIPs = "0.0.0.0/0",
                dns = "1.1.1.1, 8.8.8.8"
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