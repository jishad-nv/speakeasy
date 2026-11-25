package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.SpeechInputLocale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

private const val TAG = "SpeechCoachEngine"

class SpeechCoachEngine(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeakingTts = MutableStateFlow(false)
    val isSpeakingTts: StateFlow<Boolean> = _isSpeakingTts.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    private val _activeLocale = MutableStateFlow(SpeechInputLocale.ENGLISH_IN)
    val activeLocale: StateFlow<SpeechInputLocale> = _activeLocale.asStateFlow()

    val isRecognitionAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val res = tts?.setLanguage(Locale.US)
                ttsReady = res != TextToSpeech.LANG_MISSING_DATA &&
                    res != TextToSpeech.LANG_NOT_SUPPORTED
                if (!ttsReady) {
                    tts?.setLanguage(Locale.ENGLISH)
                    ttsReady = true
                }
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeakingTts.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeakingTts.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeakingTts.value = false
                    }
                })
            } else {
                Log.w(TAG, "TextToSpeech init failed with status $status")
            }
        }
    }

    fun setInputLocale(locale: SpeechInputLocale) {
        _activeLocale.value = locale
    }

    fun speak(text: String, slow: Boolean = false) {
        speakWithVoice(
            text = text,
            pitch = 1.04f,
            rate = if (slow) 0.65f else 0.98f,
            localeTag = "en-US"
        )
    }

    fun speakMalayalam(text: String, slow: Boolean = false) {
        speakWithVoice(
            text = text,
            pitch = 1.0f,
            rate = if (slow) 0.72f else 0.94f,
            localeTag = "ml-IN"
        )
    }

    fun speakWithVoice(
        text: String,
        pitch: Float = 1.0f,
        rate: Float = 0.96f,
        localeTag: String = "en-US"
    ) {
        if (!ttsReady || text.isBlank()) return
        stopListening()
        val targetLocale = Locale.forLanguageTag(localeTag)
        val res = tts?.setLanguage(targetLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.setLanguage(Locale.US)
        }
        tts?.setSpeechRate(rate.coerceIn(0.5f, 1.5f))
        tts?.setPitch(pitch.coerceIn(0.6f, 1.5f))
        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeakingTts.value = false
    }

    fun startListening(
        localeOverride: SpeechInputLocale? = null,
        onFinalResult: (String) -> Unit
    ) {
        val chosenLocale = localeOverride ?: _activeLocale.value
        stopSpeaking()
        _speechError.value = null
        _transcript.value = ""

        if (!isRecognitionAvailable) {
            _speechError.value = "Speech recognition service is unavailable on this device. You can type your response below."
            return
        }

        runCatching {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
                        _rmsLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _rmsLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _rmsLevel.value = 0f
                        _speechError.value = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that clearly—tap the mic and try again!"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard—tap the mic when you're ready."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is needed to listen."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording issue—try again or use keyboard input."
                            else -> "Couldn't hear clearly (code $error). Try again or type below."
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _rmsLevel.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull().orEmpty().trim()
                        if (text.isNotBlank()) {
                            _transcript.value = text
                            onFinalResult(text)
                        } else {
                            _speechError.value = "No words recognized—try speaking closer to the mic."
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                        if (partial.isNotBlank()) {
                            _transcript.value = partial
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, chosenLocale.bcp47)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, chosenLocale.bcp47)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            _isListening.value = true
            speechRecognizer?.startListening(intent)
        }.onFailure { err ->
            Log.e(TAG, "Failed to start SpeechRecognizer", err)
            _isListening.value = false
            _speechError.value = "Could not start microphone. You can type your answer below."
        }
    }

    fun stopListening() {
        _isListening.value = false
        _rmsLevel.value = 0f
        runCatching { speechRecognizer?.stopListening() }
    }

    fun clearTranscript() {
        _transcript.value = ""
        _speechError.value = null
    }

    fun release() {
        runCatching { speechRecognizer?.destroy() }
        speechRecognizer = null
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
        tts = null
    }
}
