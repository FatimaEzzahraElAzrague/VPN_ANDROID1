package com.myapp.backend.routes

import com.myapp.backend.db.AutoConnectTable
import com.myapp.backend.models.AutoConnectModeDTO
import com.myapp.backend.models.AutoConnectSettingsDTO
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.autoConnectRoutes() {
    authenticate("auth-jwt") {
        route("/settings/auto-connect") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.subject!!.toLong()
                val row = transaction {
                    AutoConnectTable.select { AutoConnectTable.userId eq userId }.singleOrNull()
                }
                if (row == null) {
                    call.respond(AutoConnectSettingsDTO(false, AutoConnectModeDTO.ANY_WIFI_OR_CELLULAR))
                } else {
                    call.respond(
                        AutoConnectSettingsDTO(
                            enabled = row[AutoConnectTable.enabled],
                            mode = AutoConnectModeDTO.valueOf(row[AutoConnectTable.mode])
                        )
                    )
                }
            }
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.subject!!.toLong()
                val body = call.receive<AutoConnectSettingsDTO>()
                transaction {
                    val exists = AutoConnectTable.select { AutoConnectTable.userId eq userId }.any()
                    if (exists) {
                        AutoConnectTable.update({ AutoConnectTable.userId eq userId }) {
                            it[enabled] = body.enabled
                            it[mode] = body.mode.name
                        }
                    } else {
                        AutoConnectTable.insert {
                            it[AutoConnectTable.userId] = userId
                            it[enabled] = body.enabled
                            it[mode] = body.mode.name
                        }
                    }
                }
                call.respond(body)
            }
        }
    }
}


