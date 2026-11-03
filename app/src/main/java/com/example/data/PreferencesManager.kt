package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AudioBitrate
import com.example.model.AudioFormat
import com.example.model.AppLanguage
import com.example.model.AppSettings
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("audiomorph_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeStr = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val langStr = prefs.getString("language_code", AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
        val biometric = prefs.getBoolean("biometric_enabled", false)
        val pin = prefs.getString("pin_code", "") ?: ""
        val autoSync = prefs.getBoolean("auto_sync_enabled", false)
        val wifiOnly = prefs.getBoolean("sync_wifi_only", true)
        val cloudProvider = prefs.getString("cloud_provider", "Google Drive") ?: "Google Drive"
        val formatStr = prefs.getString("default_format", AudioFormat.M4A.name) ?: AudioFormat.M4A.name
        val bitrateKbps = prefs.getInt("default_bitrate", 192)
        val lastSync = if (prefs.contains("last_sync_time")) prefs.getLong("last_sync_time", 0L) else null

        return AppSettings(
            themeMode = try { ThemeMode.valueOf(themeStr) } catch (e: Exception) { ThemeMode.SYSTEM },
            language = AppLanguage.entries.find { it.code == langStr } ?: AppLanguage.ENGLISH,
            biometricEnabled = biometric,
            pinCode = pin,
            autoSyncEnabled = autoSync,
            syncWifiOnly = wifiOnly,
            cloudProvider = cloudProvider,
            defaultFormat = try { AudioFormat.valueOf(formatStr) } catch (e: Exception) { AudioFormat.M4A },
            defaultBitrate = AudioBitrate.fromKbps(bitrateKbps),
            lastSyncTime = lastSync
        )
    }

    fun updateTheme(themeMode: ThemeMode) {
        prefs.edit().putString("theme_mode", themeMode.name).apply()
        _settings.value = _settings.value.copy(themeMode = themeMode)
    }

    fun updateLanguage(language: AppLanguage) {
        prefs.edit().putString("language_code", language.code).apply()
        _settings.value = _settings.value.copy(language = language)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
        _settings.value = _settings.value.copy(biometricEnabled = enabled)
    }

    fun setPinCode(pin: String) {
        prefs.edit().putString("pin_code", pin).apply()
        _settings.value = _settings.value.copy(pinCode = pin)
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_sync_enabled", enabled).apply()
        _settings.value = _settings.value.copy(autoSyncEnabled = enabled)
    }

    fun setSyncWifiOnly(wifiOnly: Boolean) {
        prefs.edit().putBoolean("sync_wifi_only", wifiOnly).apply()
        _settings.value = _settings.value.copy(syncWifiOnly = wifiOnly)
    }

    fun setCloudProvider(provider: String) {
        prefs.edit().putString("cloud_provider", provider).apply()
        _settings.value = _settings.value.copy(cloudProvider = provider)
    }

    fun setDefaultFormat(format: AudioFormat) {
        prefs.edit().putString("default_format", format.name).apply()
        _settings.value = _settings.value.copy(defaultFormat = format)
    }

    fun setDefaultBitrate(bitrate: AudioBitrate) {
        prefs.edit().putInt("default_bitrate", bitrate.kbps).apply()
        _settings.value = _settings.value.copy(defaultBitrate = bitrate)
    }

    fun updateLastSyncTime(timestamp: Long) {
        prefs.edit().putLong("last_sync_time", timestamp).apply()
        _settings.value = _settings.value.copy(lastSyncTime = timestamp)
    }
}
