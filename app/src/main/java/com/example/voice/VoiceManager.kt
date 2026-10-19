package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.local.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

enum class VoiceCallState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    MUTED
}

class VoiceManager(
    private val context: Context,
    private val settingsManager: SettingsManager,
    private val onSpeechRecognized: (String) -> Unit,
    private val onVoiceStateChanged: (VoiceCallState) -> Unit,
    private val onVoiceError: (String) -> Unit = {}
) : RecognitionListener, TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val naturalVoiceEngine = NaturalVoiceEngine(context)
    private val voiceScope = CoroutineScope(Dispatchers.Main + Job())
    private var currentSpeechJob: Job? = null

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _voiceState = MutableStateFlow(VoiceCallState.IDLE)
    val voiceState: StateFlow<VoiceCallState> = _voiceState.asStateFlow()

    private var isCallingSessionActive = false
    private var isMuted = false

    init {
        textToSpeech = TextToSpeech(context.applicationContext, this)
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceManager)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            textToSpeech?.let { tts ->
                applyFemaleVoiceSettings(tts)
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        updateState(VoiceCallState.SPEAKING)
                    }

                    override fun onDone(utteranceId: String?) {
                        onSpeechFinished()
                    }

                    override fun onError(utteranceId: String?) {
                        onSpeechFinished()
                    }
                })
            }
        }
    }

    private fun applyFemaleVoiceSettings(tts: TextToSpeech) {
        try {
            val preferredLang = settingsManager.getLanguage()
            val targetLocale = when {
                preferredLang.contains("Hindi") -> Locale("hi", "IN")
                preferredLang.contains("Hinglish") -> Locale("en", "IN")
                else -> Locale.US
            }

            if (tts.isLanguageAvailable(targetLocale) >= TextToSpeech.LANG_AVAILABLE) {
                tts.language = targetLocale
            } else {
                tts.language = Locale.US
            }

            // Find best natural female voice
            val voices = tts.voices
            if (!voices.isNullOrEmpty()) {
                val femaleVoice = voices.firstOrNull { v ->
                    val name = v.name.lowercase()
                    (v.locale.language == targetLocale.language || v.locale.language == "en") &&
                            (name.contains("female") || name.contains("#female") || name.contains("woman") ||
                             name.contains("sfg") || name.contains("hie") || name.contains("cfl"))
                } ?: voices.firstOrNull { it.name.lowercase().contains("female") }

                if (femaleVoice != null) {
                    tts.voice = femaleVoice
                }
            }
        } catch (_: Exception) {}

        tts.setPitch(settingsManager.getVoicePitch())
        tts.setSpeechRate(settingsManager.getVoiceSpeed())
    }

    private fun onSpeechFinished() {
        if (isCallingSessionActive && !isMuted) {
            startListeningInternal()
        } else {
            updateState(VoiceCallState.IDLE)
        }
    }

    fun startCallingSession() {
        isCallingSessionActive = true
        isMuted = false
        startListeningInternal()
    }

    fun stopCallingSession() {
        isCallingSessionActive = false
        stopListening()
        stopSpeaking()
        updateState(VoiceCallState.IDLE)
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        if (isMuted) {
            stopListening()
            updateState(VoiceCallState.MUTED)
        } else {
            if (isCallingSessionActive) {
                startListeningInternal()
            }
        }
        return isMuted
    }

    fun interruptAndListen() {
        stopSpeaking()
        if (isCallingSessionActive && !isMuted) {
            startListeningInternal()
        }
    }

    private fun startListeningInternal() {
        if (speechRecognizer == null) {
            initRecognizer()
        }

        try {
            val lang = settingsManager.getLanguage()
            val localeTag = when {
                lang.contains("Hindi") -> "hi-IN"
                lang.contains("Hinglish") -> "en-IN"
                else -> "en-US"
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
                // Real-time responsiveness: short silence detection to avoid 10s wait
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 700L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
            }
            speechRecognizer?.startListening(intent)
            updateState(VoiceCallState.LISTENING)
        } catch (_: Exception) {
            updateState(VoiceCallState.IDLE)
        }
    }

    fun startListeningOnce() {
        isMuted = false
        startListeningInternal()
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (_: Exception) {}
    }

    fun speak(text: String) {
        stopListening()
        stopSpeaking()

        val provider = settingsManager.getVoiceProvider()
        if (provider == SettingsManager.PROVIDER_ELEVENLABS || provider == SettingsManager.PROVIDER_GEMINI) {
            currentSpeechJob = voiceScope.launch {
                updateState(VoiceCallState.THINKING)
                naturalVoiceEngine.synthesizeAndPlay(
                    text = text,
                    settingsManager = settingsManager,
                    onStart = {
                        updateState(VoiceCallState.SPEAKING)
                    },
                    onComplete = {
                        onSpeechFinished()
                    },
                    onError = { errorMsg ->
                        onVoiceError(errorMsg)
                        // Immediate fallback to sweet female system voice
                        speakViaSystemTts(text)
                    }
                )
            }
        } else {
            speakViaSystemTts(text)
        }
    }

    private fun speakViaSystemTts(text: String) {
        if (!isTtsReady || textToSpeech == null) {
            onSpeechFinished()
            return
        }
        updateState(VoiceCallState.SPEAKING)

        textToSpeech?.let { tts ->
            applyFemaleVoiceSettings(tts)
            val volume = settingsManager.getVoiceVolume()
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "nisu_utterance_${System.currentTimeMillis()}")
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
            }
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "nisu_utterance_${System.currentTimeMillis()}")
        }
    }

    fun stopSpeaking() {
        currentSpeechJob?.cancel()
        currentSpeechJob = null
        naturalVoiceEngine.stop()
        try {
            textToSpeech?.stop()
        } catch (_: Exception) {}
    }

    fun setThinkingState() {
        updateState(VoiceCallState.THINKING)
    }

    private fun updateState(newState: VoiceCallState) {
        _voiceState.value = newState
        onVoiceStateChanged(newState)
    }

    // --- RecognitionListener Callbacks ---
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _rmsLevel.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        updateState(VoiceCallState.THINKING)
    }

    override fun onError(error: Int) {
        if (isCallingSessionActive && !isMuted) {
            if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                startListeningInternal()
            } else {
                updateState(VoiceCallState.IDLE)
            }
        } else {
            updateState(VoiceCallState.IDLE)
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val recognizedText = matches?.firstOrNull()?.trim()
        if (!recognizedText.isNullOrBlank()) {
            updateState(VoiceCallState.THINKING)
            onSpeechRecognized(recognizedText)
        } else if (isCallingSessionActive && !isMuted) {
            startListeningInternal()
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        stopCallingSession()
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}
        try {
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (_: Exception) {}
        naturalVoiceEngine.stop()
    }
}
