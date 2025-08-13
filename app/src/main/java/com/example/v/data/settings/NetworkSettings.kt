package com.example.v.data.settings

import android.content.Context

object NetworkSettings {
    private const val PREFS = "vpn_preferences"
    private const val KEY_IPV4_ONLY = "ipv4_only_mode"

    fun isIpv4Only(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_IPV4_ONLY, false)
    }

    fun setIpv4Only(context: Context, enabled: Boolean) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_IPV4_ONLY, enabled).apply()
    }
}


