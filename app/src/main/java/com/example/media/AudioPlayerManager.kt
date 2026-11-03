package com.example.media

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.example.model.AudioItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val isLooping: Boolean = false,
    val currentItem: AudioItem? = null,
    val playbackSpeed: Float = 1.0f
)

class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private var segmentEndMs: Long? = null

    fun playItem(item: AudioItem) {
        val file = File(item.filePath)
        if (!file.exists()) return

        stop()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    if (_state.value.isLooping) {
                        seekTo(0)
                        start()
                    } else {
                        _state.value = _state.value.copy(isPlaying = false, currentPositionMs = 0L)
                        stopProgressTracking()
                    }
                }
                start()
            }
            _state.value = _state.value.copy(
                isPlaying = true,
                currentItem = item,
                totalDurationMs = mediaPlayer?.duration?.toLong() ?: item.durationMs,
                currentPositionMs = 0L
            )
            segmentEndMs = null
            startProgressTracking()
        } catch (e: Exception) {
            _state.value = _state.value.copy(isPlaying = false)
        }
    }

    fun playPreviewSegment(filePath: String, startMs: Long, endMs: Long, speed: Float = 1.0f) {
        val file = File(filePath)
        if (!file.exists()) return

        stop()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                seekTo(startMs.toInt())
                try {
                    playbackParams = playbackParams.setSpeed(speed)
                } catch (ignored: Exception) {}
                start()
            }
            segmentEndMs = endMs
            _state.value = _state.value.copy(
                isPlaying = true,
                currentPositionMs = startMs,
                totalDurationMs = mediaPlayer?.duration?.toLong() ?: (endMs - startMs),
                playbackSpeed = speed
            )
            startProgressTracking()
        } catch (e: Exception) {
            _state.value = _state.value.copy(isPlaying = false)
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _state.value = _state.value.copy(isPlaying = false)
            stopProgressTracking()
        } else {
            player.start()
            _state.value = _state.value.copy(isPlaying = true)
            startProgressTracking()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
        _state.value = _state.value.copy(currentPositionMs = positionMs)
    }

    fun setSpeed(speed: Float) {
        try {
            mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed) ?: return
            _state.value = _state.value.copy(playbackSpeed = speed)
        } catch (ignored: Exception) {}
    }

    fun toggleLoop() {
        val newLoop = !_state.value.isLooping
        mediaPlayer?.isLooping = newLoop
        _state.value = _state.value.copy(isLooping = newLoop)
    }

    fun stop() {
        stopProgressTracking()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (ignored: Exception) {}
        mediaPlayer = null
        _state.value = _state.value.copy(isPlaying = false, currentPositionMs = 0L)
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = scope.launch {
            while (isActive && mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                val current = mediaPlayer?.currentPosition?.toLong() ?: 0L
                _state.value = _state.value.copy(currentPositionMs = current)

                segmentEndMs?.let { end ->
                    if (current >= end) {
                        if (_state.value.isLooping) {
                            seekTo(0)
                        } else {
                            mediaPlayer?.pause()
                            _state.value = _state.value.copy(isPlaying = false)
                            return@launch
                        }
                    }
                }
                delay(100)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stop()
    }
}
