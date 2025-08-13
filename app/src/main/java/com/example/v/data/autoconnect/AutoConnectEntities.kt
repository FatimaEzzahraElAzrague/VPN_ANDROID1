package com.example.v.data.autoconnect

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AutoConnectMode {
    UNSECURED_WIFI_ONLY,
    ANY_WIFI,
    ANY_WIFI_OR_CELLULAR
}

@Entity(tableName = "auto_connect_settings")
data class AutoConnectSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val enabled: Boolean,
    val mode: AutoConnectMode
)


