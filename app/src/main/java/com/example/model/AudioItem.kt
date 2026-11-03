package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AudioFormat(val extension: String, val mimeType: String, val displayName: String) {
    MP3("mp3", "audio/mpeg", "MP3"),
    M4A("m4a", "audio/mp4", "M4A (AAC)"),
    WAV("wav", "audio/wav", "WAV (Lossless)"),
    OGG("ogg", "audio/ogg", "OGG Vorbis"),
    AAC("aac", "audio/aac", "AAC");

    companion object {
        fun fromExtension(ext: String): AudioFormat {
            return entries.find { it.extension.equals(ext, ignoreCase = true) } ?: MP3
        }
    }
}

enum class AudioBitrate(val kbps: Int, val label: String) {
    KBPS_128(128, "128 kbps (Standard)"),
    KBPS_192(192, "192 kbps (High Quality)"),
    KBPS_256(256, "256 kbps (Very High)"),
    KBPS_320(320, "320 kbps (Lossless/Ultra)");

    companion object {
        fun fromKbps(kbps: Int): AudioBitrate {
            return entries.find { it.kbps == kbps } ?: KBPS_192
        }
    }
}

enum class SyncStatus {
    LOCAL_ONLY,
    PENDING,
    SYNCING,
    SYNCED,
    FAILED
}

@Entity(tableName = "audio_items")
data class AudioItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "AudioMorph Exports",
    val genre: String = "Various",
    val durationMs: Long = 0L,
    val fileSize: Long = 0L,
    val format: String = AudioFormat.M4A.extension,
    val bitrateKbps: Int = 192,
    val filePath: String,
    val sourceVideoName: String? = null,
    val isTrimmed: Boolean = false,
    val isFavorite: Boolean = false,
    val isPrivate: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val syncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
