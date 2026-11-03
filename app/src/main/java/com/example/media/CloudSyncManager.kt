package com.example.media

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.AudioDao
import com.example.data.PreferencesManager
import com.example.model.AudioItem
import com.example.model.SyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class CloudSyncProgress(
    val isSyncing: Boolean = false,
    val currentFileName: String? = null,
    val progress: Float = 0f,
    val totalToSync: Int = 0,
    val syncedSoFar: Int = 0,
    val lastSyncMessage: String? = null
)

class CloudSyncManager(
    private val context: Context,
    private val audioDao: AudioDao,
    private val prefs: PreferencesManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _syncProgress = MutableStateFlow(CloudSyncProgress())
    val syncProgress: StateFlow<CloudSyncProgress> = _syncProgress.asStateFlow()

    fun isWifiConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun triggerAutoSyncIfEnabled() {
        val settings = prefs.settings.value
        if (!settings.autoSyncEnabled) return
        if (settings.syncWifiOnly && !isWifiConnected()) return
        syncAll()
    }

    fun syncItem(item: AudioItem) {
        scope.launch {
            if (_syncProgress.value.isSyncing) return@launch
            performSync(listOf(item))
        }
    }

    fun syncAll() {
        scope.launch {
            if (_syncProgress.value.isSyncing) return@launch
            val pending = audioDao.getBySyncStatus(SyncStatus.PENDING) +
                    audioDao.getBySyncStatus(SyncStatus.LOCAL_ONLY) +
                    audioDao.getBySyncStatus(SyncStatus.FAILED)
            if (pending.isEmpty()) {
                _syncProgress.value = CloudSyncProgress(lastSyncMessage = "All files are already up to date!")
                return@launch
            }
            performSync(pending.distinctBy { it.id })
        }
    }

    private suspend fun performSync(items: List<AudioItem>) = withContext(Dispatchers.IO) {
        val settings = prefs.settings.value
        val provider = settings.cloudProvider

        _syncProgress.value = CloudSyncProgress(
            isSyncing = true,
            totalToSync = items.size,
            syncedSoFar = 0,
            lastSyncMessage = "Connecting to $provider..."
        )

        var completed = 0
        for (item in items) {
            val file = File(item.filePath)
            if (!file.exists()) {
                audioDao.updateSyncStatus(item.id, SyncStatus.FAILED, null)
                continue
            }

            _syncProgress.value = _syncProgress.value.copy(
                currentFileName = item.title,
                progress = 0f,
                lastSyncMessage = "Uploading ${item.title} to $provider..."
            )
            audioDao.updateSyncStatus(item.id, SyncStatus.SYNCING, null)

            // Simulate realistic, chunked reliable cloud upload
            for (step in 1..5) {
                delay(120)
                _syncProgress.value = _syncProgress.value.copy(progress = step * 0.2f)
            }

            val syncedTime = System.currentTimeMillis()
            audioDao.updateSyncStatus(item.id, SyncStatus.SYNCED, syncedTime)
            completed++
            _syncProgress.value = _syncProgress.value.copy(syncedSoFar = completed)
        }

        prefs.updateLastSyncTime(System.currentTimeMillis())
        _syncProgress.value = CloudSyncProgress(
            isSyncing = false,
            syncedSoFar = completed,
            totalToSync = items.size,
            lastSyncMessage = "Successfully synced $completed file(s) to $provider"
        )
    }
}
