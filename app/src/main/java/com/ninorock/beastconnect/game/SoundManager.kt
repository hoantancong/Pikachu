package com.ninorock.beastconnect.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.ninorock.beastconnect.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var clickSoundId: Int = 0
    private var matchSoundId: Int = 0
    private var errorSoundId: Int = 0

    init {
        // Assume these raw resources will be added by the user or we use system sounds for now
        // For demonstration, we'll try to load from R.raw if they exist
        // clickSoundId = soundPool.load(context, R.raw.click, 1)
        // matchSoundId = soundPool.load(context, R.raw.match, 1)
        // errorSoundId = soundPool.load(context, R.raw.error, 1)
    }

    fun playClick() {
        if (clickSoundId != 0) soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playMatch() {
        if (matchSoundId != 0) soundPool.play(matchSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playError() {
        if (errorSoundId != 0) soundPool.play(errorSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
