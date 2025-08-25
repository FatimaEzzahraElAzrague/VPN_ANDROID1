package com.myapp.backend.repositories

import com.myapp.backend.db.SpeedTestResults
import com.myapp.backend.db.SpeedTestServers
import com.myapp.backend.models.SpeedTestResult
import com.myapp.backend.models.SpeedTestServer
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class SpeedTestRepository {
    
    fun getAvailableServers(): List<SpeedTestServer> = transaction {
        SpeedTestServers
            .select { SpeedTestServers.isActive eq true }
            .orderBy(SpeedTestServers.priority, SortOrder.ASC)
            .map { row ->
                SpeedTestServer(
                    id = row[SpeedTestServers.id],
                    name = row[SpeedTestServers.name],
                    host = row[SpeedTestServers.host],
                    port = row[SpeedTestServers.port],
                    location = row[SpeedTestServers.location],
                    country = row[SpeedTestServers.country],
                    ip = row[SpeedTestServers.ip],
                    isActive = row[SpeedTestServers.isActive],
                    priority = row[SpeedTestServers.priority]
                )
            }
    }
    
    fun getServerById(serverId: String): SpeedTestServer? = transaction {
        SpeedTestServers
            .select { SpeedTestServers.id eq serverId }
            .firstOrNull()
            ?.let { row ->
                SpeedTestServer(
                    id = row[SpeedTestServers.id],
                    name = row[SpeedTestServers.name],
                    host = row[SpeedTestServers.host],
                    port = row[SpeedTestServers.port],
                    location = row[SpeedTestServers.location],
                    country = row[SpeedTestServers.country],
                    ip = row[SpeedTestServers.ip],
                    isActive = row[SpeedTestServers.isActive],
                    priority = row[SpeedTestServers.priority]
                )
            }
    }
    
    fun initializeDefaultServers() = transaction {
        // Check if servers already exist
        if (SpeedTestServers.selectAll().count() > 0) return@transaction
        
        val defaultServers = listOf(
            mapOf(
                SpeedTestServers.id to "osaka",
                SpeedTestServers.name to "Osaka VPN Server",
                SpeedTestServers.host to "osaka.myvpn.com",
                SpeedTestServers.port to 443,
                SpeedTestServers.location to "Osaka",
                SpeedTestServers.country to "Japan",
                SpeedTestServers.ip to "15.168.240.118",
                SpeedTestServers.isActive to true,
                SpeedTestServers.priority to 1,
                SpeedTestServers.createdAt to LocalDateTime.now()
            ),
            mapOf(
                SpeedTestServers.id to "paris",
                SpeedTestServers.name to "Paris VPN Server",
                SpeedTestServers.host to "paris.myvpn.com",
                SpeedTestServers.port to 443,
                SpeedTestServers.location to "Paris",
                SpeedTestServers.country to "France",
                SpeedTestServers.ip to "52.47.190.220",
                SpeedTestServers.isActive to true,
                SpeedTestServers.priority to 2,
                SpeedTestServers.createdAt to LocalDateTime.now()
            )
        )
        
        defaultServers.forEach { serverData ->
            SpeedTestServers.insert(serverData)
        }
    }
    

}
