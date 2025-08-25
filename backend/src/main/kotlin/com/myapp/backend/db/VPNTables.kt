package com.myapp.backend.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object VPNServers : Table("vpn_servers") {
    val id = varchar("id", 100)
    val name = varchar("name", 255)
    val city = varchar("city", 255)
    val country = varchar("country", 255)
    val countryCode = varchar("country_code", 10)
    val flag = varchar("flag", 10)
    val ip = varchar("ip", 45) // IPv4/IPv6
    val port = integer("port")
    val subnet = varchar("subnet", 50)
    val serverIP = varchar("server_ip", 45)
    val dnsServers = text("dns_servers") // JSON array as text
    val wireguardPublicKey = varchar("wireguard_public_key", 255)
    val wireguardEndpoint = varchar("wireguard_endpoint", 255)
    val allowedIPs = varchar("allowed_ips", 255)
    val mtu = integer("mtu").default(1420)
    val isActive = bool("is_active").default(true)
    val priority = integer("priority").default(1)
    val createdAt = timestamp("created_at")
    
    override val primaryKey = PrimaryKey(id)
}

object VPNClientConfigs : Table("vpn_client_configs") {
    val id = uuid("id").default { UUID.randomUUID() }
    val userId = varchar("user_id", 255).index()
    val serverId = varchar("server_id", 100).index()
    val privateKey = varchar("private_key", 255)
    val publicKey = varchar("public_key", 255)
    val address = varchar("address", 50)
    val dns = varchar("dns", 255)
    val mtu = integer("mtu").default(1420)
    val allowedIPs = varchar("allowed_ips", 255)
    val createdAt = timestamp("created_at")
    val lastUsed = timestamp("last_used").nullable()
    
    override val primaryKey = PrimaryKey(id)
}

object VPNConnections : Table("vpn_connections") {
    val id = uuid("id").default { UUID.randomUUID() }
    val userId = varchar("user_id", 255).index()
    val serverId = varchar("server_id", 100).index()
    val status = varchar("status", 50) // CONNECTED, DISCONNECTED, CONNECTING, ERROR
    val connectedAt = timestamp("connected_at").nullable()
    val disconnectedAt = timestamp("disconnected_at").nullable()
    val lastError = text("last_error").nullable()
    val bytesReceived = long("bytes_received").default(0)
    val bytesSent = long("bytes_sent").default(0)
    val connectionDuration = long("connection_duration").default(0)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    
    override val primaryKey = PrimaryKey(id)
}
