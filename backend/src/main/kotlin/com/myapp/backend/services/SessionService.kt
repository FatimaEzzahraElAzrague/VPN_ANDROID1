package com.myapp.backend.services

import com.myapp.backend.config.Env
import com.myapp.backend.config.JwtProvider
import com.myapp.backend.repositories.UserRepository
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Session Service for managing user sessions and "remember me" functionality
 */
object SessionService {
    // In-memory session storage (in production, use Redis or database)
    private val activeSessions = ConcurrentHashMap<String, UserSession>()
    private val userTokens = ConcurrentHashMap<Int, MutableSet<String>>()
    
    data class UserSession(
        val userId: Int,
        val email: String,
        val username: String,
        val fullName: String?,
        val createdAt: LocalDateTime,
        val lastActivity: LocalDateTime,
        val isGoogleUser: Boolean = false
    )
    
    /**
     * Create a new user session
     */
    fun createSession(userId: Int, email: String, username: String, fullName: String?, isGoogleUser: Boolean = false): String {
        val token = JwtProvider.createAccessToken(userId)
        val session = UserSession(
            userId = userId,
            email = email,
            username = username,
            fullName = fullName,
            createdAt = LocalDateTime.now(),
            lastActivity = LocalDateTime.now(),
            isGoogleUser = isGoogleUser
        )
        
        activeSessions[token] = session
        userTokens.getOrPut(userId) { mutableSetOf() }.add(token)
        
        logger.info { "✅ Session created for user: ID=$userId, Email=$email, Token=${token.take(20)}..." }
        return token
    }
    
    /**
     * Validate a user session
     */
    fun validateSession(token: String): UserSession? {
        return try {
            val userId = JwtProvider.getUserIdFromToken(token)
            val session = activeSessions[token]
            
            if (session != null && session.userId == userId) {
                // Update last activity
                val updatedSession = session.copy(lastActivity = LocalDateTime.now())
                activeSessions[token] = updatedSession
                logger.debug { "✅ Session validated for user: ID=$userId" }
                updatedSession
            } else {
                logger.warn { "❌ Invalid session token for user: ID=$userId" }
                null
            }
        } catch (e: Exception) {
            logger.warn { "❌ Session validation failed: ${e.message}" }
            null
        }
    }
    
    /**
     * Get user session by token
     */
    fun getSession(token: String): UserSession? {
        return activeSessions[token]
    }
    
    /**
     * Remove a user session (logout)
     */
    fun removeSession(token: String): Boolean {
        val session = activeSessions.remove(token)
        if (session != null) {
            userTokens[session.userId]?.remove(token)
            logger.info { "✅ Session removed for user: ID=${session.userId}" }
            return true
        }
        return false
    }
    
    /**
     * Remove all sessions for a user
     */
    fun removeAllUserSessions(userId: Int): Int {
        val tokens = userTokens[userId] ?: return 0
        var removedCount = 0
        
        tokens.forEach { token ->
            if (activeSessions.remove(token) != null) {
                removedCount++
            }
        }
        
        userTokens.remove(userId)
        logger.info { "✅ Removed $removedCount sessions for user: ID=$userId" }
        return removedCount
    }
    
    /**
     * Clean up expired sessions (call periodically)
     */
    fun cleanupExpiredSessions() {
        val now = LocalDateTime.now()
        val expiredTokens = mutableListOf<String>()
        
        activeSessions.forEach { (token, session) ->
            // Remove sessions older than 24 hours of inactivity
            if (session.lastActivity.plusHours(24).isBefore(now)) {
                expiredTokens.add(token)
            }
        }
        
        expiredTokens.forEach { token ->
            removeSession(token)
        }
        
        if (expiredTokens.isNotEmpty()) {
            logger.info { "🧹 Cleaned up ${expiredTokens.size} expired sessions" }
        }
    }
    
    /**
     * Get active session count
     */
    fun getActiveSessionCount(): Int = activeSessions.size
    
    /**
     * Get active sessions for a user
     */
    fun getUserActiveSessions(userId: Int): List<UserSession> {
        val tokens = userTokens[userId] ?: return emptyList()
        return tokens.mapNotNull { activeSessions[it] }
    }
}
