package com.example.v.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.net.HttpURLConnection
import java.net.URL

data class SignupRequest(
    val email: String,
    val password: String,
    val username: String,
    @SerializedName("full_name")
    val fullName: String
)

data class SignupResponse(
    val message: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleAuthRequest(
    @SerializedName("id_token")
    val idToken: String
)

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String? = null,
    val user: UserResponse? = null,
    val message: String? = null,
    val email: String? = null,
    @SerializedName("requires_verification")
    val requiresVerification: Boolean? = null
)

data class UserResponse(
    val id: Int,
    val email: String,
    val username: String,
    @SerializedName("full_name")
    val fullName: String?,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("last_login")
    val lastLogin: String?
)

data class ProfileUpdateRequest(
    val username: String? = null,
    @SerializedName("full_name")
    val fullName: String? = null
)

data class PasswordUpdateRequest(
    @SerializedName("old_password")
    val oldPassword: String,
    @SerializedName("new_password")
    val newPassword: String
)

object ApiClient {
    // Try different IPs - uncomment the one that works for your network
    private const val BASE_URL = "http://192.168.100.190:8080" // Your current IP
    // private const val BASE_URL = "http://192.168.56.1:8080" // Alternative IP
    // private const val BASE_URL = "http://172.22.96.1:8080" // Alternative IP
    private val gson = Gson()
    
    private suspend fun makeRequest(
        endpoint: String,
        method: String = "GET",
        body: String? = null,
        token: String? = null
    ): String = withContext(Dispatchers.IO) {
        val url = URL("$BASE_URL$endpoint")
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = method
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "Android-App")
            
            // Set timeouts
            connection.connectTimeout = 10000 // 10 seconds
            connection.readTimeout = 10000 // 10 seconds
            
            if (token != null) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }
            
            if (body != null) {
                connection.doOutput = true
                connection.outputStream.use { os ->
                    os.write(body.toByteArray())
                }
            }
            
            val responseCode = connection.responseCode
            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            
            val response = inputStream?.use { stream ->
                stream.bufferedReader().use { reader ->
                    reader.readText()
                }
            } ?: ""
            
            if (responseCode !in 200..299) {
                throw Exception("HTTP $responseCode: $response")
            }
            
            response
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }
    
    suspend fun signup(request: SignupRequest): SignupResponse {
        val body = gson.toJson(request)
        val response = makeRequest("/signup", "POST", body)
        return gson.fromJson(response, SignupResponse::class.java)
    }
    
    suspend fun verifyOtp(request: VerifyOtpRequest): AuthResponse {
        val body = gson.toJson(request)
        val response = makeRequest("/verify-otp", "POST", body)
        return gson.fromJson(response, AuthResponse::class.java)
    }
    
    suspend fun login(request: LoginRequest): AuthResponse {
        val body = gson.toJson(request)
        val response = makeRequest("/login", "POST", body)
        return gson.fromJson(response, AuthResponse::class.java)
    }
    
    suspend fun googleAuth(request: GoogleAuthRequest): AuthResponse {
        val body = gson.toJson(request)
        val response = makeRequest("/google-auth", "POST", body)
        return gson.fromJson(response, AuthResponse::class.java)
    }
    
    suspend fun getProfile(token: String): UserResponse {
        val response = makeRequest("/profile", "GET", token = token)
        return gson.fromJson(response, UserResponse::class.java)
    }
    
    suspend fun updateProfile(request: ProfileUpdateRequest, token: String): UserResponse {
        val body = gson.toJson(request)
        val response = makeRequest("/profile", "PUT", body, token)
        return gson.fromJson(response, UserResponse::class.java)
    }
    
    suspend fun updatePassword(request: PasswordUpdateRequest, token: String): Map<String, String> {
        val body = gson.toJson(request)
        val response = makeRequest("/profile/password", "PATCH", body, token)
        return gson.fromJson(response, Map::class.java) as Map<String, String>
    }
    
    suspend fun deleteProfile(token: String): Map<String, String> {
        val response = makeRequest("/profile", "DELETE", token = token)
        return gson.fromJson(response, Map::class.java) as Map<String, String>
    }
}
