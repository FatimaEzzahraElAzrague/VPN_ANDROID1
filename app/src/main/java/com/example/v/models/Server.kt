package com.example.v.models

data class Server(
    val id: String,
    val country: String,
    val city: String,
    val flag: String,
    val ping: Int,
    val load: Int,
    val isOptimal: Boolean = false,
    val isPremium: Boolean = false,
    val latitude: Double,
    val longitude: Double
)