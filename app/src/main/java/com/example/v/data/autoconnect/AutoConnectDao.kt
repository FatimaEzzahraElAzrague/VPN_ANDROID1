package com.example.v.data.autoconnect

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoConnectDao {
    @Query("SELECT * FROM auto_connect_settings WHERE id = 1")
    fun observeSettings(): Flow<AutoConnectSettingsEntity?>

    @Query("SELECT * FROM auto_connect_settings WHERE id = 1")
    suspend fun getSettings(): AutoConnectSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AutoConnectSettingsEntity)
}


