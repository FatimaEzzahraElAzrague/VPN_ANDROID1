package com.myapp.backend.db

import com.myapp.backend.config.Env
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import java.net.URI

object DatabaseFactory {
    lateinit var dataSource: HikariDataSource
        private set

    fun init() {
        val config = HikariConfig()

        val rawUrl = Env.databaseUrl
        if (rawUrl.startsWith("jdbc:")) {
            config.jdbcUrl = rawUrl
        } else if (rawUrl.startsWith("postgresql://")) {
            val uri = URI(rawUrl)
            val host = uri.host
            val port = if (uri.port == -1) 5432 else uri.port
            val path = uri.path // includes leading /
            val query = uri.rawQuery?.let { "?$it" } ?: ""
            val jdbcUrl = "jdbc:postgresql://$host:$port$path$query"
            config.jdbcUrl = jdbcUrl
            val userInfo = uri.userInfo
            if (!userInfo.isNullOrBlank()) {
                val parts = userInfo.split(":", limit = 2)
                config.username = parts.getOrNull(0)
                config.password = parts.getOrNull(1)
            }
        } else {
            // Fallback: assume it's already a proper JDBC URL
            config.jdbcUrl = rawUrl
        }

        config.maximumPoolSize = 10
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()

        dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
        
        // Create tables if they don't exist
        createTables()
    }
    
    private fun createTables() {
        transaction {
            try {
                // Check if google_id column exists
                val hasGoogleIdColumn = try {
                    exec("SELECT google_id FROM users LIMIT 1") { }
                    true
                } catch (e: Exception) {
                    false
                }
                
                if (!hasGoogleIdColumn) {
                    println("🔄 Adding google_id column to users table...")
                    exec("ALTER TABLE users ADD COLUMN google_id VARCHAR(255) UNIQUE")
                    println("✅ google_id column added successfully")
                }
                
                SchemaUtils.create(Users, AutoConnectTable)
                println("✅ Database tables created successfully")
            } catch (e: Exception) {
                println("⚠️ Tables might already exist: ${e.message}")
            }
        }
    }
}


