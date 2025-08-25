package com.example.v.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.v.data.ApiClient
import com.example.v.data.LoginRequest
import com.example.v.data.SignupRequest
import com.example.v.data.AuthResponse
import com.example.v.data.SignupResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * JWT Authentication Manager
 * Handles secure token storage, validation, and refresh
 */
class JWTAuthManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "JWTAuthManager"
        private const val PREFS_NAME = "jwt_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        
        @Volatile
        private var INSTANCE: JWTAuthManager? = null
        
        fun getInstance(context: Context): JWTAuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: JWTAuthManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Secure shared preferences for token storage
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        val token = getAccessToken()
        val expiry = getTokenExpiry()
        
        if (token.isNullOrEmpty() || expiry == 0L) {
            return false
        }
        
        // Check if token is expired
        val currentTime = System.currentTimeMillis()
        if (currentTime >= expiry) {
            Log.d(TAG, "Token expired, clearing authentication")
            clearAuthentication()
            return false
        }
        
        return true
    }
    
    /**
     * Get current access token
     */
    fun getAccessToken(): String? {
        return securePrefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    /**
     * Get current refresh token
     */
    private fun getRefreshToken(): String? {
        return securePrefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * Get current user ID
     */
    fun getUserId(): String? {
        return securePrefs.getString(KEY_USER_ID, null)
    }
    
    /**
     * Get token expiry time
     */
    private fun getTokenExpiry(): Long {
        return securePrefs.getLong(KEY_TOKEN_EXPIRY, 0L)
    }
    
    /**
     * Store authentication tokens securely
     */
    private fun storeAuthentication(
        accessToken: String,
        refreshToken: String?,
        userId: String,
        expiresInSeconds: Long
    ) {
        val expiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000)
        
        securePrefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_USER_ID, userId)
            putLong(KEY_TOKEN_EXPIRY, expiryTime)
        }.apply()
        
        Log.d(TAG, "Authentication stored for user: $userId, expires: ${Date(expiryTime)}")
    }
    
    /**
     * Clear all authentication data
     */
    fun clearAuthentication() {
        securePrefs.edit().clear().apply()
        Log.d(TAG, "Authentication cleared")
    }
    
    /**
     * Login user and store JWT tokens
     */
    suspend fun login(email: String, password: String): AuthResponse {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email, password)
                val response = ApiClient.login(request)
                
                if (response.accessToken != null) {
                    // Store tokens securely
                    storeAuthentication(
                        accessToken = response.accessToken,
                        refreshToken = null, // Backend doesn't provide refresh token yet
                        userId = response.user?.id?.toString() ?: "",
                        expiresInSeconds = 3600L // Default 1 hour
                    )
                    Log.d(TAG, "Login successful for user: ${response.user?.email}")
                }
                
                response
            } catch (e: Exception) {
                Log.e(TAG, "Login failed: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * Signup user and store JWT tokens
     */
    suspend fun signup(email: String, password: String, username: String, fullName: String): SignupResponse {
        return withContext(Dispatchers.IO) {
            try {
                val request = SignupRequest(email, password, username, fullName)
                val response = ApiClient.signup(request)
                
                Log.d(TAG, "Signup successful for user: $email")
                response
            } catch (e: Exception) {
                Log.e(TAG, "Signup failed: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * Logout user and clear tokens
     */
    fun logout() {
        clearAuthentication()
        Log.d(TAG, "User logged out")
    }
    
    /**
     * Get valid access token for API calls
     * Returns null if not authenticated or token expired
     */
    fun getValidAccessToken(): String? {
        return if (isAuthenticated()) {
            getAccessToken()
        } else {
            null
        }
    }
    
    /**
     * Check if token needs refresh (within 5 minutes of expiry)
     */
    fun needsTokenRefresh(): Boolean {
        val expiry = getTokenExpiry()
        val currentTime = System.currentTimeMillis()
        val fiveMinutes = 5 * 60 * 1000L
        
        return (expiry - currentTime) <= fiveMinutes
    }
}
