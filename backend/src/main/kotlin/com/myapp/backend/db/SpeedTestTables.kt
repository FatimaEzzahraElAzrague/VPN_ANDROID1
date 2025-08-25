package com.myapp.backend.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID



object SpeedTestServers : Table("speed_test_servers") {
    val id = varchar("id", 100)
    val name = varchar("name", 255)
    val host = varchar("host", 255)
    val port = integer("port")
    val location = varchar("location", 255)
    val country = varchar("country", 100)
    val ip = varchar("ip", 45).default("") // IPv4/IPv6 address for Android compatibility
    val isActive = bool("is_active").default(true)
    val priority = integer("priority").default(1)
    val createdAt = timestamp("created_at")
    
    override val primaryKey = PrimaryKey(id)
}
