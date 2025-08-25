package com.example.v.data

/**
 * VPN connection options matching desktop version
 */
data class VPNOptions(
    val adBlockEnabled: Boolean = false,
    val antiMalwareEnabled: Boolean = false,
    val familySafeModeEnabled: Boolean = false
)
