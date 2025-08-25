package com.myapp.backend.auth.services

import com.myapp.backend.config.Env
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class RedisService {
    private val redisClient: RedisClient?
    private val connection: StatefulRedisConnection<String, String>?
    private val syncCommands: RedisCommands<String, String>?
    
    // Fallback in-memory storage if Redis is not available
    private val inMemoryStore = ConcurrentHashMap<String, Pair<String, Long>>()
    
    init {
        val redisUrl = Env.redisUrl
        if (!redisUrl.isNullOrBlank()) {
            try {
                redisClient = RedisClient.create(RedisURI.create(redisUrl))
                connection = redisClient.connect()
                syncCommands = connection.sync()
            } catch (e: Exception) {
                println("Failed to connect to Redis: ${e.message}. Using in-memory storage.")
                redisClient = null
                connection = null
                syncCommands = null
            }
        } else {
            println("No Redis URL configured. Using in-memory storage.")
            redisClient = null
            connection = null
            syncCommands = null
        }
    }

    fun setVerificationCode(email: String, code: String, ttlSeconds: Long) {
        if (syncCommands != null) {
            syncCommands.setex(getKey(email), ttlSeconds, code)
        } else {
            val expirationTime = System.currentTimeMillis() + (ttlSeconds * 1000)
            inMemoryStore[getKey(email)] = code to expirationTime
            cleanupExpiredCodes()
        }
    }

    fun getVerificationCode(email: String): String? {
        return if (syncCommands != null) {
            syncCommands.get(getKey(email))
        } else {
            val (code, expiration) = inMemoryStore[getKey(email)] ?: return null
            if (System.currentTimeMillis() > expiration) {
                inMemoryStore.remove(getKey(email))
                null
            } else {
                code
            }
        }
    }

    fun deleteVerificationCode(email: String) {
        if (syncCommands != null) {
            syncCommands.del(getKey(email))
        } else {
            inMemoryStore.remove(getKey(email))
        }
    }

    private fun getKey(email: String): String = "verification:$email"

    private fun cleanupExpiredCodes() {
        val currentTime = System.currentTimeMillis()
        inMemoryStore.entries.removeIf { (_, value) -> currentTime > value.second }
    }

    fun close() {
        connection?.close()
        redisClient?.shutdown()
    }

    companion object {
        const val DEFAULT_TTL_SECONDS = 300L // 5 minutes
        const val VERIFICATION_CODE_LENGTH = 6
    }
}
