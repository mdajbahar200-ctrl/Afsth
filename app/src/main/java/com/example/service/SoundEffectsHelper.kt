package com.example.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundEffectsHelper {

    private val soundScope = CoroutineScope(Dispatchers.Default)

    /**
     * Plays the signature 'Fahh' deep resonant low synth tone
     */
    fun playFahhSound() {
        soundScope.launch {
            try {
                val sampleRate = 44100
                val durationSeconds = 0.6
                val numSamples = (durationSeconds * sampleRate).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    // Pitch slides from 240 Hz down to 90 Hz for 'fahh' sigh effect
                    val freq = 240.0 * (1.0 - (time / durationSeconds) * 0.65)
                    val envelope = if (time < 0.05) {
                        time / 0.05
                    } else {
                        (1.0 - time / durationSeconds)
                    }
                    val value = (sin(2.0 * Math.PI * freq * time) * envelope * Short.MAX_VALUE * 0.75).toInt()
                    samples[i] = value.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playPcmBuffer(samples, sampleRate)
            } catch (_: Exception) {}
        }
    }

    /**
     * Plays the 'Rag korla !' energetic warning double buzz
     */
    fun playRagKorlaSound() {
        soundScope.launch {
            try {
                val sampleRate = 44100
                val durationSeconds = 0.5
                val numSamples = (durationSeconds * sampleRate).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    val isFirstBeep = time < 0.2
                    val isSecondBeep = time in 0.28..0.48

                    val envelope = when {
                        isFirstBeep -> 1.0 - (time / 0.2)
                        isSecondBeep -> 1.0 - ((time - 0.28) / 0.2)
                        else -> 0.0
                    }
                    val freq = if (isFirstBeep) 580.0 else 720.0
                    val value = (sin(2.0 * Math.PI * freq * time) * envelope * Short.MAX_VALUE * 0.7).toInt()
                    samples[i] = value.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playPcmBuffer(samples, sampleRate)
            } catch (_: Exception) {}
        }
    }

    /**
     * Plays an audible alert siren for hardcore addiction urge breaks
     */
    fun playSirenSound() {
        soundScope.launch {
            try {
                val sampleRate = 44100
                val durationSeconds = 0.8
                val numSamples = (durationSeconds * sampleRate).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    // Frequency oscillates rapidly between 600Hz and 1100Hz
                    val freq = 850.0 + 250.0 * sin(2.0 * Math.PI * 8.0 * time)
                    val envelope = if (time < 0.05) time / 0.05 else (1.0 - time / durationSeconds)
                    val value = (sin(2.0 * Math.PI * freq * time) * envelope * Short.MAX_VALUE * 0.75).toInt()
                    samples[i] = value.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playPcmBuffer(samples, sampleRate)
            } catch (_: Exception) {}
        }
    }

    /**
     * Plays a soothing mindfulness detox singing bowl bell
     */
    fun playDetoxBellSound() {
        soundScope.launch {
            try {
                val sampleRate = 44100
                val durationSeconds = 1.2
                val numSamples = (durationSeconds * sampleRate).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    // Harmonic bell: 432 Hz fundamental + 864 Hz octave
                    val decay = Math.exp(-3.0 * time)
                    val tone1 = sin(2.0 * Math.PI * 432.0 * time) * 0.7
                    val tone2 = sin(2.0 * Math.PI * 864.0 * time) * 0.3
                    val value = ((tone1 + tone2) * decay * Short.MAX_VALUE * 0.8).toInt()
                    samples[i] = value.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playPcmBuffer(samples, sampleRate)
            } catch (_: Exception) {}
        }
    }

    private fun playPcmBuffer(samples: ShortArray, sampleRate: Int) {
        var audioTrack: AudioTrack? = null
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, samples.size * 2)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack.play()
            audioTrack.write(samples, 0, samples.size)
        } catch (_: Exception) {
        } finally {
            try {
                // Ensure the track is cleanly stopped and released after playing
                val durationMs = (samples.size.toDouble() / sampleRate * 1000).toLong() + 100L
                Thread.sleep(durationMs.coerceAtLeast(100L))
                audioTrack?.stop()
                audioTrack?.release()
            } catch (_: Exception) {}
        }
    }
}
