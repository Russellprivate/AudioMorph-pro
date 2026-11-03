package com.example.model

data class ConversionSettings(
    val format: AudioFormat = AudioFormat.M4A,
    val bitrate: AudioBitrate = AudioBitrate.KBPS_192,
    val sampleRate: Int = 44100,
    val channels: Int = 2, // 1 = Mono, 2 = Stereo
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val autoTrim: Boolean = false,
    val startMs: Long = 0L,
    val endMs: Long = 0L
)

data class TrimSettings(
    val startMs: Long = 0L,
    val endMs: Long = 0L,
    val fadeInSec: Float = 0f,
    val fadeOutSec: Float = 0f,
    val volumeBoost: Float = 1.0f, // 0.5f to 2.0f
    val speed: Float = 1.0f,
    val outputFormat: AudioFormat = AudioFormat.M4A,
    val saveAsRingtone: Boolean = false
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED
}

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    BENGALI("bn", "Bengali", "বাংলা"),
    SPANISH("es", "Spanish", "Español"),
    HINDI("hi", "Hindi", "हिन्दी")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val biometricEnabled: Boolean = false,
    val pinCode: String = "",
    val autoSyncEnabled: Boolean = false,
    val syncWifiOnly: Boolean = true,
    val cloudProvider: String = "Google Drive",
    val defaultFormat: AudioFormat = AudioFormat.M4A,
    val defaultBitrate: AudioBitrate = AudioBitrate.KBPS_192,
    val lastSyncTime: Long? = null
)
