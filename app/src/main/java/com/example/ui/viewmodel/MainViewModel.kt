package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.data.SampleMediaProvider
import com.example.media.AudioEditorEngine
import com.example.media.AudioPlayerManager
import com.example.media.CloudSyncManager
import com.example.media.VideoInfo
import com.example.media.VideoToAudioConverter
import com.example.model.AppLanguage
import com.example.model.AppSettings
import com.example.model.AudioBitrate
import com.example.model.AudioFormat
import com.example.model.AudioItem
import com.example.model.ConversionSettings
import com.example.model.SyncStatus
import com.example.model.ThemeMode
import com.example.model.TrimSettings
import com.example.security.BiometricAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class ScreenTab {
    CONVERT,
    STUDIO,
    LIBRARY,
    CLOUD_SYNC,
    SETTINGS
}

data class ConvertUiState(
    val selectedVideo: VideoInfo? = null,
    val isConverting: Boolean = false,
    val progress: Float = 0f,
    val statusText: String = "",
    val conversionSettings: ConversionSettings = ConversionSettings(),
    val lastConvertedItem: AudioItem? = null,
    val errorMessage: String? = null
)

data class StudioUiState(
    val selectedAudioItem: AudioItem? = null,
    val audioFile: File? = null,
    val totalDurationMs: Long = 0L,
    val startMs: Long = 0L,
    val endMs: Long = 0L,
    val waveform: List<Float> = emptyList(),
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val trimSettings: TrimSettings = TrimSettings(),
    val trimmedResultItem: AudioItem? = null,
    val errorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val audioDao = db.audioDao()
    val prefsManager = PreferencesManager(application)
    val playerManager = AudioPlayerManager(application)
    val biometricManager = BiometricAuthManager(application)
    val syncManager = CloudSyncManager(application, audioDao, prefsManager)
    private val converter = VideoToAudioConverter(application)
    private val editorEngine = AudioEditorEngine(application)

    val appSettings: StateFlow<AppSettings> = prefsManager.settings

    private val _currentTab = MutableStateFlow(ScreenTab.CONVERT)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _convertState = MutableStateFlow(ConvertUiState())
    val convertState: StateFlow<ConvertUiState> = _convertState.asStateFlow()

    private val _studioState = MutableStateFlow(StudioUiState())
    val studioState: StateFlow<StudioUiState> = _studioState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFormatFilter = MutableStateFlow<String?>("ALL")
    val selectedFormatFilter: StateFlow<String?> = _selectedFormatFilter.asStateFlow()

    // Library audio items combined with search and filter
    val libraryItems: StateFlow<List<AudioItem>> = combine(
        audioDao.getAllAudio(),
        _searchQuery,
        _selectedFormatFilter
    ) { items, query, format ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.artist.contains(query, ignoreCase = true)
            val matchesFormat = format == "ALL" || item.format.equals(format, ignoreCase = true)
            matchesQuery && matchesFormat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isLocked: StateFlow<Boolean> = biometricManager.isLocked

    init {
        // Initialize lock state if biometric or pin is enabled
        if (appSettings.value.biometricEnabled || appSettings.value.pinCode.isNotBlank()) {
            biometricManager.setLocked(true)
        }
    }

    fun selectTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFormatFilter(format: String?) {
        _selectedFormatFilter.value = format
    }

    // --- CONVERT ACTIONS ---

    fun onVideoPicked(uri: Uri) {
        viewModelScope.launch {
            val info = converter.extractVideoInfo(uri)
            _convertState.value = _convertState.value.copy(
                selectedVideo = info,
                conversionSettings = _convertState.value.conversionSettings.copy(
                    title = info.title
                ),
                errorMessage = null,
                lastConvertedItem = null
            )
        }
    }

    fun loadDemoVideo() {
        viewModelScope.launch {
            val demoFile = SampleMediaProvider.getOrCreateDemoVideo(getApplication())
            val uri = Uri.fromFile(demoFile)
            val info = VideoInfo(
                uri = uri,
                title = "Demo_Motion_AudioMorph",
                durationMs = 8000L,
                fileSize = demoFile.length().coerceAtLeast(1024 * 50),
                resolution = "1920x1080 (HD)",
                hasAudio = true
            )
            _convertState.value = _convertState.value.copy(
                selectedVideo = info,
                conversionSettings = _convertState.value.conversionSettings.copy(
                    title = "Demo_Track_${System.currentTimeMillis() % 1000}"
                ),
                errorMessage = null,
                lastConvertedItem = null
            )
        }
    }

    fun updateConversionSettings(settings: ConversionSettings) {
        _convertState.value = _convertState.value.copy(conversionSettings = settings)
    }

    fun startConversion() {
        val video = _convertState.value.selectedVideo ?: return
        if (_convertState.value.isConverting) return

        viewModelScope.launch {
            _convertState.value = _convertState.value.copy(
                isConverting = true,
                progress = 0f,
                statusText = "Extracting audio track...",
                errorMessage = null
            )

            try {
                val result = converter.convertVideoToAudio(
                    uri = video.uri,
                    settings = _convertState.value.conversionSettings,
                    onProgress = { p ->
                        _convertState.value = _convertState.value.copy(
                            progress = p,
                            statusText = if (p >= 0.9f) "Packaging audio container..." else "Processing audio stream (${(p * 100).toInt()}%)..."
                        )
                    }
                )

                val audioItem = AudioItem(
                    title = _convertState.value.conversionSettings.title.ifBlank { video.title },
                    artist = _convertState.value.conversionSettings.artist.ifBlank { "AudioMorph Artist" },
                    album = _convertState.value.conversionSettings.album.ifBlank { "Converted Audio" },
                    durationMs = result.durationMs,
                    fileSize = result.fileSize,
                    format = result.format.extension,
                    bitrateKbps = result.bitrateKbps,
                    filePath = result.outputFile.absolutePath,
                    sourceVideoName = video.title,
                    syncStatus = if (appSettings.value.autoSyncEnabled) SyncStatus.PENDING else SyncStatus.LOCAL_ONLY
                )

                val newId = audioDao.insert(audioItem)
                val savedItem = audioItem.copy(id = newId)

                _convertState.value = _convertState.value.copy(
                    isConverting = false,
                    progress = 1.0f,
                    statusText = "Conversion complete!",
                    lastConvertedItem = savedItem
                )

                // Trigger auto-sync if enabled
                if (appSettings.value.autoSyncEnabled) {
                    syncManager.triggerAutoSyncIfEnabled()
                }

            } catch (e: Exception) {
                _convertState.value = _convertState.value.copy(
                    isConverting = false,
                    errorMessage = e.message ?: "Conversion failed"
                )
            }
        }
    }

    // --- STUDIO / TRIMMING ACTIONS ---

    fun loadAudioForTrimming(item: AudioItem) {
        viewModelScope.launch {
            val file = File(item.filePath)
            val duration = item.durationMs.coerceAtLeast(1000L)
            _studioState.value = _studioState.value.copy(
                selectedAudioItem = item,
                audioFile = file,
                totalDurationMs = duration,
                startMs = 0L,
                endMs = duration,
                trimSettings = TrimSettings(
                    startMs = 0L,
                    endMs = duration,
                    outputFormat = AudioFormat.fromExtension(item.format)
                ),
                trimmedResultItem = null
            )
            _currentTab.value = ScreenTab.STUDIO

            // Extract waveform
            val amps = editorEngine.extractWaveform(file)
            _studioState.value = _studioState.value.copy(waveform = amps)
        }
    }

    fun updateTrimRange(startMs: Long, endMs: Long) {
        val total = _studioState.value.totalDurationMs
        val clampedStart = startMs.coerceIn(0L, (total - 500L).coerceAtLeast(0L))
        val clampedEnd = endMs.coerceIn(clampedStart + 500L, total)

        _studioState.value = _studioState.value.copy(
            startMs = clampedStart,
            endMs = clampedEnd,
            trimSettings = _studioState.value.trimSettings.copy(
                startMs = clampedStart,
                endMs = clampedEnd
            )
        )
    }

    fun updateTrimSettings(settings: TrimSettings) {
        _studioState.value = _studioState.value.copy(trimSettings = settings)
    }

    fun playTrimPreview() {
        val file = _studioState.value.audioFile ?: return
        val start = _studioState.value.startMs
        val end = _studioState.value.endMs
        val speed = _studioState.value.trimSettings.speed
        playerManager.playPreviewSegment(file.absolutePath, start, end, speed)
    }

    fun exportTrimmedAudio() {
        val current = _studioState.value.selectedAudioItem ?: return
        val sourceFile = _studioState.value.audioFile ?: return
        if (_studioState.value.isProcessing) return

        viewModelScope.launch {
            _studioState.value = _studioState.value.copy(isProcessing = true, progress = 0.2f)
            try {
                val outputFile = editorEngine.trimAndEditAudio(
                    sourceFile = sourceFile,
                    settings = _studioState.value.trimSettings,
                    outputFileName = "${current.title}_cut",
                    onProgress = { p ->
                        _studioState.value = _studioState.value.copy(progress = p)
                    }
                )

                val dur = (_studioState.value.endMs - _studioState.value.startMs).coerceAtLeast(500L)
                val trimmedItem = AudioItem(
                    title = "${current.title} (Trimmed)",
                    artist = current.artist,
                    album = current.album,
                    durationMs = dur,
                    fileSize = outputFile.length(),
                    format = _studioState.value.trimSettings.outputFormat.extension,
                    bitrateKbps = current.bitrateKbps,
                    filePath = outputFile.absolutePath,
                    sourceVideoName = current.sourceVideoName,
                    isTrimmed = true,
                    syncStatus = if (appSettings.value.autoSyncEnabled) SyncStatus.PENDING else SyncStatus.LOCAL_ONLY
                )

                val newId = audioDao.insert(trimmedItem)
                val saved = trimmedItem.copy(id = newId)
                _studioState.value = _studioState.value.copy(
                    isProcessing = false,
                    progress = 1.0f,
                    trimmedResultItem = saved
                )

                if (appSettings.value.autoSyncEnabled) {
                    syncManager.triggerAutoSyncIfEnabled()
                }
            } catch (e: Exception) {
                _studioState.value = _studioState.value.copy(
                    isProcessing = false,
                    errorMessage = e.message ?: "Trimming failed"
                )
            }
        }
    }

    // --- LIBRARY & PLAYBACK ACTIONS ---

    fun playAudio(item: AudioItem) {
        playerManager.playItem(item)
    }

    fun toggleFavorite(item: AudioItem) {
        viewModelScope.launch {
            audioDao.setFavorite(item.id, !item.isFavorite)
        }
    }

    fun togglePrivateLock(item: AudioItem) {
        viewModelScope.launch {
            audioDao.setPrivate(item.id, !item.isPrivate)
        }
    }

    fun deleteAudio(item: AudioItem) {
        viewModelScope.launch {
            if (playerManager.state.value.currentItem?.id == item.id) {
                playerManager.stop()
            }
            audioDao.delete(item)
            try {
                val f = File(item.filePath)
                if (f.exists()) f.delete()
            } catch (ignored: Exception) {}
        }
    }

    fun shareAudio(context: Context, item: AudioItem) {
        try {
            val file = File(item.filePath)
            if (!file.exists()) return

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = AudioFormat.fromExtension(item.format).mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, item.title)
                putExtra(Intent.EXTRA_TEXT, "Listen to \"${item.title}\" extracted with AudioMorph")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Audio via"))
        } catch (ignored: Exception) {}
    }

    // --- SECURITY & LOCK ACTIONS ---

    fun unlockWithPin(pin: String): Boolean {
        if (appSettings.value.pinCode.isBlank() || appSettings.value.pinCode == pin) {
            biometricManager.setLocked(false)
            return true
        }
        return false
    }

    fun promptBiometric(activity: FragmentActivity, onResult: (Boolean, String?) -> Unit) {
        biometricManager.showBiometricPrompt(
            activity = activity,
            title = "Unlock AudioMorph",
            subtitle = "Verify your biometric credential to continue",
            onSuccess = { onResult(true, null) },
            onError = { err -> onResult(false, err) }
        )
    }

    fun lockApp() {
        biometricManager.setLocked(true)
    }

    // --- SETTINGS ACTIONS ---

    fun setThemeMode(mode: ThemeMode) {
        prefsManager.updateTheme(mode)
    }

    fun setLanguage(language: AppLanguage) {
        prefsManager.updateLanguage(language)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefsManager.setBiometricEnabled(enabled)
    }

    fun setPin(pin: String) {
        prefsManager.setPinCode(pin)
    }

    fun setAutoSync(enabled: Boolean) {
        prefsManager.setAutoSyncEnabled(enabled)
        if (enabled) {
            syncManager.triggerAutoSyncIfEnabled()
        }
    }

    fun setWifiOnly(wifiOnly: Boolean) {
        prefsManager.setSyncWifiOnly(wifiOnly)
    }

    fun setCloudProvider(provider: String) {
        prefsManager.setCloudProvider(provider)
    }

    fun setDefaultFormat(format: AudioFormat) {
        prefsManager.setDefaultFormat(format)
    }

    fun setDefaultBitrate(bitrate: AudioBitrate) {
        prefsManager.setDefaultBitrate(bitrate)
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
