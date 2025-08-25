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
                // Create users table
                exec("""
                    CREATE TABLE IF NOT EXISTS users (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        email VARCHAR(255) NOT NULL UNIQUE,
                        password_hash VARCHAR(255),
                        username VARCHAR(50) NOT NULL UNIQUE,
                        full_name VARCHAR(255) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        last_login TIMESTAMP,
                        is_active BOOLEAN DEFAULT FALSE,
                        is_deleted BOOLEAN DEFAULT FALSE,
                        oauth_provider VARCHAR(50),
                        oauth_id VARCHAR(255),
                        profile_picture_url TEXT
                    )
                """.trimIndent())
                
                // Create indexes
                exec("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email) WHERE is_deleted = FALSE")
                exec("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username) WHERE is_deleted = FALSE")
                exec("CREATE INDEX IF NOT EXISTS idx_users_oauth ON users(oauth_provider, oauth_id) WHERE is_deleted = FALSE")
                
                // Create update trigger function
                exec("""
                    CREATE OR REPLACE FUNCTION update_updated_at_column()
                    RETURNS TRIGGER AS $$
                    BEGIN
                        NEW.updated_at = CURRENT_TIMESTAMP;
                        RETURN NEW;
                    END;
                    $$ language 'plpgsql'
                """.trimIndent())
                
                // Create trigger
                exec("""
                    DROP TRIGGER IF EXISTS update_users_updated_at ON users;
                    CREATE TRIGGER update_users_updated_at
                        BEFORE UPDATE ON users
                        FOR EACH ROW
                        EXECUTE FUNCTION update_updated_at_column()
                """.trimIndent())
                
                SchemaUtils.create(Users)
                println("✅ Database tables created successfully")
            } catch (e: Exception) {
                println("⚠️ Tables might already exist: ${e.message}")
            }
        }
    }
}


