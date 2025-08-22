package com.myapp.backend.services

import com.myapp.backend.config.Env
import redis.clients.jedis.JedisPooled
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule

object RedisClient {
    private val inMemoryStore = ConcurrentHashMap<String, String>()
    private val inMemoryExpiry = ConcurrentHashMap<String, Long>()
    
    val client: JedisPooled by lazy {
        try {
            val uri = URI(Env.redisUrl)
            if (uri.userInfo != null) {
                JedisPooled(uri)
            } else {
                JedisPooled(uri.host, uri.port)
            }
        } catch (e: Exception) {
            println("⚠️ Redis connection failed, using in-memory fallback: ${e.message}")
            // Return a dummy client that will use in-memory storage
            object : JedisPooled("localhost", 6379) {
                override fun setex(key: String, seconds: Int, value: String): String {
                    inMemoryStore[key] = value
                    inMemoryExpiry[key] = System.currentTimeMillis() + (seconds * 1000L)
                    return "OK"
                }
                
                override fun get(key: String): String? {
                    val expiry = inMemoryExpiry[key]
                    return if (expiry != null && System.currentTimeMillis() < expiry) {
                        inMemoryStore[key]
                    } else {
                        inMemoryStore.remove(key)
                        inMemoryExpiry.remove(key)
                        null
                    }
                }
                
                override fun exists(vararg keys: String): Long {
                    return keys.count { key ->
                        val expiry = inMemoryExpiry[key]
                        expiry != null && System.currentTimeMillis() < expiry
                    }.toLong()
                }
                
                override fun del(vararg keys: String): Long {
                    var deleted = 0L
                    keys.forEach { key ->
                        if (inMemoryStore.remove(key) != null) {
                            inMemoryExpiry.remove(key)
                            deleted++
                        }
                    }
                    return deleted
                }
                
                override fun incr(key: String): Long {
                    val current = inMemoryStore[key]?.toLongOrNull() ?: 0L
                    val newValue = current + 1
                    inMemoryStore[key] = newValue.toString()
                    return newValue
                }
                
                override fun expire(key: String, seconds: Int): Long {
                    inMemoryExpiry[key] = System.currentTimeMillis() + (seconds * 1000L)
                    return 1L
                }
            }
        }
    }
    
    // Clean up expired keys periodically
    init {
        // Simple cleanup without external scheduling
        Thread {
            while (true) {
                try {
                    Thread.sleep(60000L) // Every minute
                    val now = System.currentTimeMillis()
                    inMemoryExpiry.entries.removeIf { (key, expiry) ->
                        if (now >= expiry) {
                            inMemoryStore.remove(key)
                            true
                        } else {
                            false
                        }
                    }
                } catch (e: InterruptedException) {
                    break
                }
            }
        }.start()
    }
}


