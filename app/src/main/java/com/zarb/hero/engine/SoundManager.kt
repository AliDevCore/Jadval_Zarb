package com.zarb.hero.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlin.math.sin

class SoundManager(private val context: Context) {
    var isSoundEnabled: Boolean = true

    fun playCorrect() {
        if (!isSoundEnabled) return
        tryPlayResourceOrTone(isCorrect = true)
    }

    fun playWrong() {
        if (!isSoundEnabled) return
        tryPlayResourceOrTone(isCorrect = false)
    }

    private fun tryPlayResourceOrTone(isCorrect: Boolean) {
        val resId = if (isCorrect) {
            context.resources.getIdentifier("sound_correct", "raw", context.packageName)
        } else {
            context.resources.getIdentifier("sound_wrong", "raw", context.packageName)
        }

        if (resId != 0) {
            try {
                val mp = MediaPlayer.create(context, resId)
                mp?.setOnCompletionListener { it.release() }
                mp?.start()
                return
            } catch (_: Exception) {}
        }

        Thread {
            try {
                playFallbackTone(if (isCorrect) 659 else 220, if (isCorrect) 180 else 250)
            } catch (_: Exception) {}
        }.start()
    }

    private fun playFallbackTone(freqHz: Int, durationMs: Int) {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i / (sampleRate.toDouble() / freqHz)
            buffer[i] = (sin(angle) * 32767).toInt().toShort()
        }
        val track = AudioTrack.Builder()
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
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(buffer, 0, buffer.size)
        track.play()
        Thread.sleep(durationMs.toLong() + 50)
        track.release()
    }
}
