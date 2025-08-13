package com.example.v.data

import android.content.Context
import androidx.room.Database
import androidx.room.TypeConverters
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.v.data.autoconnect.AutoConnectDao
import com.example.v.data.autoconnect.AutoConnectSettingsEntity

@Database(
    entities = [AutoConnectSettingsEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    com.example.v.data.autoconnect.AutoConnectConverters::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun autoConnectDao(): AutoConnectDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "vpn_app.db"
            ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}


