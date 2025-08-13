package com.example.v.data.autoconnect

import androidx.room.TypeConverter

object AutoConnectConverters {
    @TypeConverter
    @JvmStatic
    fun fromMode(mode: AutoConnectMode?): String? = mode?.name

    @TypeConverter
    @JvmStatic
    fun toMode(name: String?): AutoConnectMode? = name?.let {
        try {
            AutoConnectMode.valueOf(it)
        } catch (t: Throwable) {
            null
        }
    }
}


