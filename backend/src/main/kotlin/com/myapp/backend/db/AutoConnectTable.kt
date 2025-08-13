package com.myapp.backend.db

import org.jetbrains.exposed.dao.id.LongIdTable

object AutoConnectTable : LongIdTable("auto_connect_settings") {
    val userId = long("user_id")
    val enabled = bool("enabled")
    val mode = varchar("mode", 64)
}


