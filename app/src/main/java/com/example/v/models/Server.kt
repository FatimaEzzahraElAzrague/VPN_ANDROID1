package com.example.v.models

data class Server(
    val id: String,
    val country: String,
    val city: String,
    val flagEmoji: String,
    val ping: Int = 0,
    val load: Int = 0
)

val mockServers = listOf(
    Server("fr-paris", "France", "Paris", "🇫🇷", 25, 45),
    Server("us-ny", "United States", "New York", "🇺🇸", 35, 60),
    Server("uk-london", "United Kingdom", "London", "🇬🇧", 20, 30),
    Server("de-berlin", "Germany", "Berlin", "🇩🇪", 15, 25),
    Server("jp-tokyo", "Japan", "Tokyo", "🇯🇵", 120, 40),
    Server("ca-toronto", "Canada", "Toronto", "🇨🇦", 45, 35),
    Server("au-sydney", "Australia", "Sydney", "🇦🇺", 180, 50),
    Server("nl-amsterdam", "Netherlands", "Amsterdam", "🇳🇱", 18, 20),
    Server("se-stockholm", "Sweden", "Stockholm", "🇸🇪", 22, 15),
    Server("ch-zurich", "Switzerland", "Zurich", "🇨🇭", 28, 30)
)