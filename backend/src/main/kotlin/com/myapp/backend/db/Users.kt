package com.myapp.backend.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val username = varchar("username", 255).uniqueIndex()
    val fullName = varchar("full_name", 255).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    val lastLogin = datetime("last_login").nullable()
    val isActive = bool("is_active")
    val isDeleted = bool("is_deleted")

    override val primaryKey = PrimaryKey(id)
}


