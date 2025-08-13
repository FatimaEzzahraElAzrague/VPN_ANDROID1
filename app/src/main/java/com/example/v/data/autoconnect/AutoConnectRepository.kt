package com.example.v.data.autoconnect

import android.content.Context
import android.util.Log
import com.example.v.data.AppDatabase
import com.example.v.data.VPNFeaturesApiClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AutoConnectRepository(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val dao = AppDatabase.get(context).autoConnectDao()
    private val api = VPNFeaturesApiClient.getInstance()

    fun observe(): Flow<AutoConnectSettingsEntity?> = dao.observeSettings()

    suspend fun get(): AutoConnectSettingsEntity? = dao.getSettings()

    suspend fun set(enabled: Boolean, mode: AutoConnectMode, userId: String? = null): AutoConnectSettingsEntity =
        withContext(ioDispatcher) {
            val entity = AutoConnectSettingsEntity(id = 1, enabled = enabled, mode = mode)
            dao.upsert(entity)

            // Best effort sync to backend when userId is available
            if (userId != null) {
                try {
                    // Use Ktor client directly to hit /settings/auto-connect if needed later
                    // Placeholder: sync via VPNFeaturesApiClient once route is added to client
                } catch (t: Throwable) {
                    Log.w("AutoConnectRepository", "Failed to sync auto-connect settings: ${t.message}")
                }
            }
            entity
        }
}


 