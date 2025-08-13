package com.myapp.backend.models

import kotlinx.serialization.Serializable

@Serializable
enum class AutoConnectModeDTO { UNSECURED_WIFI_ONLY, ANY_WIFI, ANY_WIFI_OR_CELLULAR }

@Serializable
data class AutoConnectSettingsDTO(
    val enabled: Boolean,
    val mode: AutoConnectModeDTO
)


