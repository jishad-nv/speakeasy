package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

private const val TAG = "AudioClipRecorder"

class AudioClipRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var currentFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _hasBeforeClip = MutableStateFlow(false)
    val hasBeforeClip: StateFlow<Boolean> = _hasBeforeClip.asStateFlow()

    private val _hasAfterClip = MutableStateFlow(false)
    val hasAfterClip: StateFlow<Boolean> = _hasAfterClip.asStateFlow()

    private val _playingSlot = MutableStateFlow<String?>(null)
    val playingSlot: StateFlow<String?> = _playingSlot.asStateFlow()

    private fun slotFile(slot: String): File = File(context.cacheDir, "accent_$slot.m4a")

    fun startRecording(slot: String = "before"): Boolean {
        stopPlayback()
        stopRecordingInternal(save = false)
        val out = slotFile(slot)
        if (out.exists()) out.delete()
        currentFile = out

        return runCatching {
            val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mr.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setAudioEncodingBitRate(64000)
                setMaxDuration(25_000)
                setMaxFileSize(4_000_000)
                setOutputFile(out.absolutePath)
                prepare()
                start()
            }
            recorder = mr
            _isRecording.value = true
            true
        }.getOrElse { err ->
            Log.e(TAG, "Failed to start MediaRecorder", err)
            _isRecording.value = false
            false
        }
    }

    fun stopRecording(slot: String = "before"): ByteArray? {
        stopRecordingInternal(save = true)
        val file = slotFile(slot)
        if (file.exists() && file.length() > 256L) {
            if (slot == "before") _hasBeforeClip.value = true
            if (slot == "after") _hasAfterClip.value = true
            return runCatching { file.readBytes() }.getOrNull()
        }
        return null
    }

    private fun stopRecordingInternal(save: Boolean) {
        val mr = recorder ?: return
        recorder = null
        _isRecording.value = false
        runCatching {
            mr.stop()
        }.onFailure {
            if (!save) currentFile?.delete()
        }
        runCatching { mr.release() }
    }

    fun playSlot(slot: String) {
        val file = slotFile(slot)
        if (!file.exists()) return
        stopPlayback()
        runCatching {
            player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    _playingSlot.value = null
                }
                _playingSlot.value = slot
                start()
            }
        }.onFailure {
            Log.e(TAG, "Playback failed for $slot", it)
            _playingSlot.value = null
        }
    }

    fun stopPlayback() {
        _playingSlot.value = null
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
    }

    fun clearClips() {
        stopPlayback()
        slotFile("before").delete()
        slotFile("after").delete()
        _hasBeforeClip.value = false
        _hasAfterClip.value = false
    }

    fun release() {
        stopRecordingInternal(save = false)
        stopPlayback()
    }
}
