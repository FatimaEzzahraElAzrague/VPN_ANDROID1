package com.myapp.backend.repositories

import com.myapp.backend.db.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class UserRecord(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val username: String,
    val fullName: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val lastLogin: LocalDateTime?,
    val isActive: Boolean,
    val isDeleted: Boolean,
)

class UserRepository {
    fun findByEmail(email: String): UserRecord? = transaction {
        Users.select { Users.email eq email }.limit(1).firstOrNull()?.toRecord()
    }

    fun findById(id: Int): UserRecord? = transaction {
        Users.select { Users.id eq id }.limit(1).firstOrNull()?.toRecord()
    }

    fun findByUsername(username: String): UserRecord? = transaction {
        Users.select { Users.username eq username }.limit(1).firstOrNull()?.toRecord()
    }

    fun insertInactive(email: String, passwordHash: String, username: String, fullName: String?): Int = transaction {
        val userId = Users.insert {
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
            it[Users.username] = username
            it[Users.fullName] = fullName
            it[Users.createdAt] = LocalDateTime.now()
            it[Users.updatedAt] = LocalDateTime.now()
            it[Users.isActive] = false
            it[Users.isDeleted] = false
        } get Users.id
        
        logger.info { "✅ User created: ID=$userId, Email=$email, Username=$username, FullName=$fullName" }
        userId
    }

    fun activateUser(email: String): Boolean = transaction {
        val updated = Users.update({ Users.email eq email }) {
            it[isActive] = true
            it[updatedAt] = LocalDateTime.now()
        } > 0
        
        if (updated) {
            logger.info { "✅ User activated: Email=$email" }
        } else {
            logger.warn { "❌ Failed to activate user: Email=$email" }
        }
        updated
    }

    fun updateLastLogin(userId: Int) = transaction {
        Users.update({ Users.id eq userId }) {
            it[lastLogin] = LocalDateTime.now()
        }
    }

    fun updateProfile(userId: Int, username: String?, fullName: String?) = transaction {
        Users.update({ Users.id eq userId }) { row ->
            username?.let { row[Users.username] = it }
            fullName?.let { row[Users.fullName] = it }
            row[Users.updatedAt] = LocalDateTime.now()
        }
    }

    fun updatePassword(userId: Int, newHash: String) = transaction {
        Users.update({ Users.id eq userId }) {
            it[passwordHash] = newHash
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun softDelete(userId: Int) = transaction {
        Users.update({ Users.id eq userId }) {
            it[isDeleted] = true
            it[isActive] = false
            it[updatedAt] = LocalDateTime.now()
        }
    }

    fun getAllUsers(): List<Map<String, Any>> = transaction {
        Users.selectAll().map { row ->
            mapOf(
                "id" to row[Users.id],
                "email" to row[Users.email],
                "username" to row[Users.username],
                "full_name" to (row[Users.fullName] ?: ""),
                "is_active" to row[Users.isActive],
                "is_deleted" to row[Users.isDeleted],
                "created_at" to row[Users.createdAt].toString(),
                "updated_at" to row[Users.updatedAt].toString(),
                "last_login" to (row[Users.lastLogin]?.toString() ?: "")
            )
        }
    }

    private fun ResultRow.toRecord() = UserRecord(
        id = this[Users.id],
        email = this[Users.email],
        passwordHash = this[Users.passwordHash],
        username = this[Users.username],
        fullName = this[Users.fullName],
        createdAt = this[Users.createdAt],
        updatedAt = this[Users.updatedAt],
        lastLogin = this[Users.lastLogin],
        isActive = this[Users.isActive],
        isDeleted = this[Users.isDeleted],
    )
}


