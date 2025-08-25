package com.myapp.backend.auth.models

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.UUID

object Users : UUIDTable("users") {
    val email: Column<String> = varchar("email", 255).uniqueIndex()
    val passwordHash: Column<String?> = varchar("password_hash", 255).nullable()
    val username: Column<String> = varchar("username", 50).uniqueIndex()
    val fullName: Column<String> = varchar("full_name", 255)
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
    val lastLogin = timestamp("last_login").nullable()
    val isActive = bool("is_active").default(false)
    val isDeleted = bool("is_deleted").default(false)
    val oauthProvider = varchar("oauth_provider", 50).nullable()
    val oauthId = varchar("oauth_id", 255).nullable()
    val profilePictureUrl = text("profile_picture_url").nullable()
}

class User(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<User>(Users)

    var email by Users.email
    var passwordHash by Users.passwordHash
    var username by Users.username
    var fullName by Users.fullName
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt
    var lastLogin by Users.lastLogin
    var isActive by Users.isActive
    var isDeleted by Users.isDeleted
    var oauthProvider by Users.oauthProvider
    var oauthId by Users.oauthId
    var profilePictureUrl by Users.profilePictureUrl

    fun toDTO(): UserDTO = UserDTO(
        id = id.value.toString(),
        email = email,
        username = username,
        fullName = fullName,
        createdAt = createdAt,
        lastLogin = lastLogin,
        isActive = isActive,
        profilePictureUrl = profilePictureUrl
    )
}

data class UserDTO(
    val id: String,
    val email: String,
    val username: String,
    val fullName: String,
    val createdAt: Instant,
    val lastLogin: Instant?,
    val isActive: Boolean,
    val profilePictureUrl: String?
)
