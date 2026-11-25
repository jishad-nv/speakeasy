package com.example.audio

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object CelebrationSoundPlayer {
    fun playSuccessChime(enabled: Boolean) {
        if (!enabled) return
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 65)
                tone.startTone(ToneGenerator.TONE_PROP_ACK, 120)
                delay(140)
                tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
                delay(220)
                tone.release()
            }
        }
    }

    fun playLevelUpFanfare(enabled: Boolean) {
        if (!enabled) return
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
                tone.startTone(ToneGenerator.TONE_DTMF_1, 100)
                delay(110)
                tone.startTone(ToneGenerator.TONE_DTMF_5, 100)
                delay(110)
                tone.startTone(ToneGenerator.TONE_DTMF_9, 220)
                delay(250)
                tone.release()
            }
        }
    }
}
