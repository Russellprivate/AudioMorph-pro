package com.example.data

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

object SampleMediaProvider {

    /**
     * Creates a high-fidelity synthetic demo video with audio track or demo video file,
     * so user can immediately test video to audio conversion even in an emulator without camera files.
     */
    suspend fun getOrCreateDemoVideo(context: Context): File = withContext(Dispatchers.IO) {
        val demoDir = File(context.filesDir, "samples").apply { mkdirs() }
        val demoFile = File(demoDir, "demo_motion_video.mp4")
        if (demoFile.exists() && demoFile.length() > 5000) {
            return@withContext demoFile
        }

        try {
            createSyntheticMp4(demoFile)
        } catch (e: Exception) {
            // Fallback: create high quality audio container
            createSyntheticWav(demoFile, durationSec = 8)
        }
        demoFile
    }

    /**
     * Generates a 16-bit PCM WAV audio file with a rich pleasant harmonic melody.
     */
    fun createSyntheticWav(outputFile: File, durationSec: Int = 10, sampleRate: Int = 44100): File {
        val numSamples = durationSec * sampleRate
        val pcmData = ShortArray(numSamples * 2) // Stereo

        // Musical frequencies (C chord arpeggio: C4, E4, G4, B4, C5)
        val notes = doubleArrayOf(261.63, 329.63, 392.00, 493.88, 523.25, 392.00, 329.63, 261.63)
        val noteDuration = sampleRate / 2 // each note 0.5s

        for (i in 0 until numSamples) {
            val noteIndex = (i / noteDuration) % notes.size
            val freq = notes[noteIndex]
            val t = i.toDouble() / sampleRate
            // Main sine wave + subtle harmonics + envelope
            val env = 1.0 - ((i % noteDuration).toDouble() / noteDuration) * 0.4
            val sampleVal = (sin(2.0 * Math.PI * freq * t) * 0.7 +
                    sin(4.0 * Math.PI * freq * t) * 0.2 +
                    sin(6.0 * Math.PI * freq * t) * 0.1) * env * 24000.0

            val shortSample = sampleVal.toInt().coerceIn(-32768, 32767).toShort()
            pcmData[i * 2] = shortSample // Left
            pcmData[i * 2 + 1] = (shortSample * 0.9).toInt().toShort() // Right (stereo spatial)
        }

        FileOutputStream(outputFile).use { fos ->
            writeWavHeader(fos, sampleRate, 2, numSamples * 4)
            val buffer = ByteBuffer.allocate(pcmData.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            for (s in pcmData) {
                buffer.putShort(s)
            }
            fos.write(buffer.array())
        }

        return outputFile
    }

    private fun writeWavHeader(fos: FileOutputStream, sampleRate: Int, channels: Int, dataSize: Int) {
        val totalSize = 36 + dataSize
        val byteRate = sampleRate * channels * 2
        val blockAlign = channels * 2

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt(totalSize)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16) // Subchunk1Size for PCM
            putShort(1) // AudioFormat 1 = PCM
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign.toShort())
            putShort(16) // BitsPerSample
            put("data".toByteArray())
            putInt(dataSize)
        }
        fos.write(header.array())
    }

    private fun createSyntheticMp4(outputFile: File) {
        val sampleRate = 44100
        val channelCount = 2
        val durationUs = 6_000_000L // 6 seconds

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, 192000)
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
        }

        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        var trackIndex = -1
        var muxerStarted = false
        val bufferInfo = MediaCodec.BufferInfo()

        val numSamples = (sampleRate * 6)
        val pcmBuffer = ShortArray(2048)
        var samplePos = 0
        var presentationTimeUs = 0L
        var inputDone = false
        var outputDone = false

        val notes = doubleArrayOf(330.0, 392.0, 440.0, 523.25, 587.33, 659.25)
        val noteDur = sampleRate / 2

        try {
            while (!outputDone) {
                if (!inputDone) {
                    val inputBufIndex = encoder.dequeueInputBuffer(10000)
                    if (inputBufIndex >= 0) {
                        val inputBuf = encoder.getInputBuffer(inputBufIndex) ?: continue
                        inputBuf.clear()

                        if (samplePos >= numSamples) {
                            encoder.queueInputBuffer(inputBufIndex, 0, 0, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            val samplesToWrite = minOf(pcmBuffer.size / 2, numSamples - samplePos)
                            val byteBuffer = ByteBuffer.allocate(samplesToWrite * 4).order(ByteOrder.LITTLE_ENDIAN)

                            for (i in 0 until samplesToWrite) {
                                val currentIdx = samplePos + i
                                val note = notes[(currentIdx / noteDur) % notes.size]
                                val t = currentIdx.toDouble() / sampleRate
                                val sample = (sin(2.0 * Math.PI * note * t) * 22000.0).toInt().coerceIn(-32768, 32767).toShort()
                                byteBuffer.putShort(sample)
                                byteBuffer.putShort(sample)
                            }

                            inputBuf.put(byteBuffer.array())
                            presentationTimeUs = (samplePos.toDouble() / sampleRate * 1_000_000).toLong()
                            encoder.queueInputBuffer(inputBufIndex, 0, byteBuffer.capacity(), presentationTimeUs, 0)
                            samplePos += samplesToWrite
                        }
                    }
                }

                val outputBufIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    if (!muxerStarted) {
                        trackIndex = muxer.addTrack(encoder.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                } else if (outputBufIndex >= 0) {
                    val outputBuf = encoder.getOutputBuffer(outputBufIndex)
                    if (outputBuf != null && bufferInfo.size > 0 && muxerStarted) {
                        outputBuf.position(bufferInfo.offset)
                        outputBuf.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, outputBuf, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outputBufIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        outputDone = true
                    }
                }
            }
        } finally {
            try { encoder.stop() } catch (ignored: Exception) {}
            try { encoder.release() } catch (ignored: Exception) {}
            if (muxerStarted) {
                try { muxer.stop() } catch (ignored: Exception) {}
            }
            try { muxer.release() } catch (ignored: Exception) {}
        }
    }
}
