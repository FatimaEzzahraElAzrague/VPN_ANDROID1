package com.myapp.backend.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.myapp.backend.models.ProfileResponse
import com.myapp.backend.models.UpdatePasswordRequest
import com.myapp.backend.models.UpdateProfileRequest
import com.myapp.backend.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.format.DateTimeFormatter

fun Route.profileRoutes() {
    val users = UserRepository()
    val dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    authenticate("auth-jwt") {
        route("/profile") {
            get {
                val userId = call.principal<JWTPrincipal>()!!.subject!!.toInt()
                val user = users.findById(userId)
                if (user == null || user.isDeleted) { call.respond(HttpStatusCode.NotFound); return@get }
                val res = ProfileResponse(
                    id = user.id,
                    email = user.email,
                    username = user.username,
                    full_name = user.fullName,
                    created_at = user.createdAt.format(dtf),
                    updated_at = user.updatedAt.format(dtf),
                    last_login = user.lastLogin?.format(dtf),
                )
                call.respond(res)
            }

            put {
                val userId = call.principal<JWTPrincipal>()!!.subject!!.toInt()
                val body = runCatching { call.receive<UpdateProfileRequest>() }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_json")); return@put
                }
                body.username?.let {
                    val existing = users.findByUsername(it)
                    if (existing != null && existing.id != userId) {
                        call.respond(HttpStatusCode.Conflict, mapOf("error" to "username_exists")); return@put
                    }
                }
                users.updateProfile(userId, body.username, body.full_name)
                val updated = users.findById(userId)!!
                val res = ProfileResponse(
                    id = updated.id,
                    email = updated.email,
                    username = updated.username,
                    full_name = updated.fullName,
                    created_at = updated.createdAt.format(dtf),
                    updated_at = updated.updatedAt.format(dtf),
                    last_login = updated.lastLogin?.format(dtf),
                )
                call.respond(res)
            }

            patch("/password") {
                val userId = call.principal<JWTPrincipal>()!!.subject!!.toInt()
                val body = runCatching { call.receive<UpdatePasswordRequest>() }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_json")); return@patch
                }
                val user = users.findById(userId) ?: run { call.respond(HttpStatusCode.NotFound); return@patch }
                val verify = BCrypt.verifyer().verify(body.old_password.toCharArray(), user.passwordHash)
                if (!verify.verified) { call.respond(HttpStatusCode.Forbidden, mapOf("error" to "invalid_old_password")); return@patch }
                val newHash = BCrypt.withDefaults().hashToString(12, body.new_password.toCharArray())
                users.updatePassword(userId, newHash)
                call.respond(HttpStatusCode.OK, mapOf("message" to "password_updated"))
            }

            delete {
                val userId = call.principal<JWTPrincipal>()!!.subject!!.toInt()
                users.softDelete(userId)
                call.respond(HttpStatusCode.OK, mapOf("message" to "deleted"))
            }
        }
    }
}


