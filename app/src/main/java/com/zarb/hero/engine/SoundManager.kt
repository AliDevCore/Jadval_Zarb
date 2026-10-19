package com.zarb.hero.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.zarb.hero.R
import java.util.concurrent.ConcurrentHashMap

/**
 * پخش افکت‌های صوتی با SoundPool (بدون مسدود کردن thread اصلی).
 * نمونه‌ها یک‌بار بارگذاری می‌شوند و با هر پاسخ فقط پخش می‌شوند.
 */
class SoundManager(context: Context) {
    @Volatile
    var isSoundEnabled: Boolean = true

    private val loadedIds: MutableSet<Int> = ConcurrentHashMap.newKeySet<Int>()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val correctId: Int
    private val wrongId: Int

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedIds.add(sampleId)
        }
        val appContext = context.applicationContext
        correctId = soundPool.load(appContext, R.raw.sound_correct, 1)
        wrongId = soundPool.load(appContext, R.raw.sound_wrong, 1)
    }

    fun playCorrect() = play(correctId)

    fun playWrong() = play(wrongId)

    private fun play(soundId: Int) {
        if (!isSoundEnabled || soundId !in loadedIds) return
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
