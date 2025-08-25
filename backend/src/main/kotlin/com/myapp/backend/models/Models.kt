package com.myapp.backend.models

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SignupRequest(
    val email: String,
    val password: String,
    val name: String
)

@Serializable
data class SignupResponse(
    val success: Boolean,
    val message: String,
    val email: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class GoogleAuthRequest(
    val idToken: String
)

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val accessToken: String? = null,
    val user: UserResponse? = null,
    val email: String? = null,
    val requiresVerification: Boolean = false
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val emailVerified: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class OtpResponse(
    val success: Boolean,
    val message: String,
    val email: String
)