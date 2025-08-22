package com.myapp.backend.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val username = varchar("username", 100).uniqueIndex()
    val fullName = varchar("full_name", 255).nullable()
    val googleId = varchar("google_id", 255).nullable().uniqueIndex() // Google's unique user ID
    val createdAt = timestamp("created_at").default(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").default(CurrentTimestamp)
    val lastLogin = timestamp("last_login").nullable()
    val isActive = bool("is_active").default(true)
    val isDeleted = bool("is_deleted").default(false)

    override val primaryKey = PrimaryKey(id)
}


