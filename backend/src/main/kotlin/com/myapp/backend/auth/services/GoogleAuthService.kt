package com.myapp.backend.auth.services

import com.myapp.backend.auth.config.OAuthConfig
import com.myapp.backend.auth.models.User
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class GoogleAuthService {
    private val httpClient = OAuthConfig.httpClient
    private val config = OAuthConfig.googleConfig

    suspend fun verifyAndGetUserInfo(idToken: String, isAndroid: Boolean = false): GoogleUserInfo {
        val clientId = if (isAndroid) config.clientIdAndroid else config.clientIdWeb
        
        // Verify the token
        val tokenInfo = verifyIdToken(idToken, clientId)
        
        // Get user info
        val userInfo = getUserInfo(idToken)
        
        return userInfo
    }

    private suspend fun verifyIdToken(idToken: String, clientId: String): GoogleTokenInfo {
        val response = httpClient.get(GoogleOAuthConfig.GOOGLE_TOKEN_INFO_URL) {
            parameter("id_token", idToken)
        }
        
        if (!response.status.isSuccess()) {
            throw IllegalArgumentException("Invalid ID token")
        }
        
        val tokenInfo: GoogleTokenInfo = response.body()
        
        // Verify the token is intended for our app
        if (tokenInfo.aud != clientId) {
            throw IllegalArgumentException("Token was not intended for this app")
        }
        
        return tokenInfo
    }

    private suspend fun getUserInfo(idToken: String): GoogleUserInfo {
        val response = httpClient.get(GoogleOAuthConfig.GOOGLE_USER_INFO_URL) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $idToken")
            }
        }
        
        if (!response.status.isSuccess()) {
            throw IllegalArgumentException("Failed to get user info")
        }
        
        return response.body()
    }

    fun findOrCreateUser(userInfo: GoogleUserInfo): User = transaction {
        // Try to find existing user by OAuth ID
        User.find { Users.email eq userInfo.email }.firstOrNull()?.apply {
            // Update existing user
            lastLogin = Instant.now()
            fullName = userInfo.name
            profilePictureUrl = userInfo.picture
            oauthProvider = "google"
            oauthId = userInfo.sub
            isActive = true // Google accounts are pre-verified
        } ?: User.new {
            // Create new user
            email = userInfo.email
            username = generateUsername(userInfo.email)
            fullName = userInfo.name
            createdAt = Instant.now()
            updatedAt = Instant.now()
            lastLogin = Instant.now()
            isActive = true
            oauthProvider = "google"
            oauthId = userInfo.sub
            profilePictureUrl = userInfo.picture
        }
    }

    private fun generateUsername(email: String): String {
        val baseUsername = email.substringBefore("@").replace(Regex("[^a-zA-Z0-9]"), "")
        var username = baseUsername
        var counter = 1
        
        while (transaction { User.find { Users.username eq username }.firstOrNull() } != null) {
            username = "$baseUsername$counter"
            counter++
        }
        
        return username
    }
}

@Serializable
data class GoogleTokenInfo(
    val iss: String,
    val azp: String,
    val aud: String,
    val sub: String,
    val email: String,
    val email_verified: Boolean,
    val name: String? = null,
    val picture: String? = null,
    val given_name: String? = null,
    val family_name: String? = null,
    val locale: String? = null,
    val iat: Long,
    val exp: Long
)

@Serializable
data class GoogleUserInfo(
    val sub: String,
    val name: String,
    val given_name: String? = null,
    val family_name: String? = null,
    val picture: String? = null,
    val email: String,
    val email_verified: Boolean,
    val locale: String? = null
)
