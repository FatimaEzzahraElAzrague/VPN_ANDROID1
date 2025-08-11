package com.myapp.backend.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class SignupRequest(
    val email: String,
    val password: String,
    val username: String,
    val full_name: String? = null,
)

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val otp: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class GoogleAuthRequest(
    val id_token: String,
)

@Serializable
data class UpdateProfileRequest(
    val username: String? = null,
    val full_name: String? = null,
)

@Serializable
data class UpdatePasswordRequest(
    val old_password: String,
    val new_password: String,
)

@Serializable
data class TokenResponse(
    val access_token: String,
    val token_type: String = "Bearer",
    val expires_in: Long,
)

@Serializable
data class ProfileResponse(
    val id: Int,
    val email: String,
    val username: String,
    val full_name: String? = null,
    val created_at: String,
    val updated_at: String,
    val last_login: String? = null,
)


