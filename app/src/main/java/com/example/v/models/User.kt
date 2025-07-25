package com.example.v.models

data class User(
    val id: String,
    val email: String,
    val name: String,
    val isEmailVerified: Boolean = false,
    val subscriptionType: SubscriptionType = SubscriptionType.FREE,
    val subscriptionExpiryDate: String? = null
)

enum class SubscriptionType {
    FREE,
    PREMIUM,
    PREMIUM_PLUS
}

enum class AuthProvider {
    EMAIL,
    GOOGLE
}