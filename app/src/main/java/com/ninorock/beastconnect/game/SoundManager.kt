package com.ninorock.beastconnect.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.ninorock.beastconnect.R

class SoundManager(context: Context) {
    private val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var clickSoundId = 0
    private var selectSoundId = 0
    private var matchSoundId = 0
    private var wrongSoundId = 0
    private var hintSoundId = 0
    private var shuffleSoundId = 0
    private var winSoundId = 0
    private var loseSoundId = 0
    private var beginningSoundId = 0
    private var bonusSoundId = 0
    private var skipSoundId = 0

    init {
        clickSoundId = soundPool.load(context, R.raw.click, 1)
        selectSoundId = soundPool.load(context, R.raw.select, 1)
        matchSoundId = soundPool.load(context, R.raw.match, 1)
        wrongSoundId = soundPool.load(context, R.raw.wrong, 1)
        hintSoundId = soundPool.load(context, R.raw.hint, 1)
        shuffleSoundId = soundPool.load(context, R.raw.shuffle, 1)
        winSoundId = soundPool.load(context, R.raw.win, 1)
        loseSoundId = soundPool.load(context, R.raw.lose, 1)
        beginningSoundId = soundPool.load(context, R.raw.beginning, 1)
        bonusSoundId = soundPool.load(context, R.raw.bonus, 1)
        skipSoundId = soundPool.load(context, R.raw.skip, 1)
    }

    private fun isSoundEnabled(): Boolean = prefs.getBoolean("sound_enabled", true)

    private fun play(soundId: Int) {
        if (isSoundEnabled() && soundId != 0) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playClick() = play(clickSoundId)
    fun playSelect() = play(selectSoundId)
    fun playMatch() = play(matchSoundId)
    fun playWrong() = play(wrongSoundId)
    fun playHint() = play(hintSoundId)
    fun playShuffle() = play(shuffleSoundId)
    fun playWin() = play(winSoundId)
    fun playLose() = play(loseSoundId)
    fun playBeginning() = play(beginningSoundId)
    fun playBonus() = play(bonusSoundId)
    fun playSkip() = play(skipSoundId)

    fun release() {
        soundPool.release()
    }
}
