package com.myapp.backend.services

import com.myapp.backend.config.Env
import redis.clients.jedis.JedisPooled
import java.net.URI

object RedisClient {
    val client: JedisPooled by lazy {
        val uri = URI(Env.redisUrl)
        if (uri.userInfo != null) {
            JedisPooled(uri)
        } else {
            JedisPooled(uri.host, uri.port)
        }
    }
}


