package com.example.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import com.example.data.local.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class NaturalVoiceEngine(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var mediaPlayer: MediaPlayer? = null
    private var isEnginePlaying = false

    suspend fun synthesizeAndPlay(
        text: String,
        settingsManager: SettingsManager,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        stop()

        val provider = settingsManager.getVoiceProvider()
        when (provider) {
            SettingsManager.PROVIDER_ELEVENLABS -> {
                val apiKey = settingsManager.getElevenLabsApiKey()
                if (apiKey.isBlank()) {
                    onError("ElevenLabs API Key is missing. Please enter it in Settings > Voice Studio.")
                    return@withContext
                }
                synthesizeElevenLabs(text, settingsManager, onStart, onComplete, onError)
            }
            SettingsManager.PROVIDER_GEMINI -> {
                val apiKey = settingsManager.getApiKey()
                if (apiKey.isBlank()) {
                    onError("Gemini API Key is missing. Please enter it in Settings > AI Configuration.")
                    return@withContext
                }
                synthesizeGeminiTts(text, apiKey, onStart, onComplete, onError)
            }
            else -> {
                onError("System fallback requested")
            }
        }
    }

    private fun synthesizeElevenLabs(
        text: String,
        settings: SettingsManager,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val apiKey = settings.getElevenLabsApiKey()
        val voiceId = settings.getElevenLabsVoiceId()
        val modelId = settings.getElevenLabsModelId()
        val stability = settings.getElevenLabsStability()
        val similarity = settings.getElevenLabsSimilarity()

        val endpoint = "https://api.elevenlabs.io/v1/text-to-speech/$voiceId"

        try {
            val jsonBody = JSONObject().apply {
                put("text", text)
                put("model_id", modelId)
                put("voice_settings", JSONObject().apply {
                    put("stability", stability)
                    put("similarity_boost", similarity)
                    put("style", 0.0)
                    put("use_speaker_boost", true)
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("xi-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "audio/mpeg")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: "HTTP ${response.code}"
                onError("ElevenLabs error (${response.code}): $errBody")
                return
            }

            val bytes = response.body?.bytes()
            if (bytes == null || bytes.isEmpty()) {
                onError("Received empty audio response from ElevenLabs.")
                return
            }

            // Save to temp cache file
            val tempAudioFile = File(context.cacheDir, "elevenlabs_speech.mp3")
            FileOutputStream(tempAudioFile).use { it.write(bytes) }

            playAudioFile(tempAudioFile, onStart, onComplete, onError)
        } catch (e: Exception) {
            onError("ElevenLabs audio generation failed: ${e.localizedMessage}")
        }
    }

    private fun synthesizeGeminiTts(
        text: String,
        apiKey: String,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-tts:generateContent?key=$apiKey"

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Say with a warm, natural female tone: $text")
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply {
                        put("AUDIO")
                    })
                    put("speechConfig", JSONObject().apply {
                        put("voiceConfig", JSONObject().apply {
                            put("prebuiltVoiceConfig", JSONObject().apply {
                                put("voiceName", "Kore") // Natural female voice
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: "HTTP ${response.code}"
                onError("Gemini TTS error (${response.code}): $errBody")
                return
            }

            val respString = response.body?.string() ?: ""
            val jsonResp = JSONObject(respString)
            val candidates = jsonResp.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var audioBase64: String? = null
            var mimeType = "audio/mp3"

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.optJSONObject(i)
                    val inlineData = p?.optJSONObject("inlineData") ?: p?.optJSONObject("inline_data")
                    if (inlineData != null) {
                        audioBase64 = inlineData.optString("data")
                        mimeType = inlineData.optString("mimeType", inlineData.optString("mime_type", "audio/mp3"))
                        break
                    }
                }
            }

            if (audioBase64.isNullOrBlank()) {
                onError("Gemini TTS returned no audio data.")
                return
            }

            val audioBytes = Base64.decode(audioBase64, Base64.DEFAULT)
            val ext = if (mimeType.contains("wav")) "wav" else "mp3"
            val tempAudioFile = File(context.cacheDir, "gemini_speech.$ext")
            FileOutputStream(tempAudioFile).use { it.write(audioBytes) }

            playAudioFile(tempAudioFile, onStart, onComplete, onError)
        } catch (e: Exception) {
            onError("Gemini TTS generation failed: ${e.localizedMessage}")
        }
    }

    private fun playAudioFile(
        file: File,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            stop()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .build()
                )
                setDataSource(file.absolutePath)
                setOnPreparedListener { mp ->
                    isEnginePlaying = true
                    onStart()
                    mp.start()
                }
                setOnCompletionListener {
                    isEnginePlaying = false
                    stop()
                    onComplete()
                }
                setOnErrorListener { _, what, extra ->
                    isEnginePlaying = false
                    stop()
                    onError("MediaPlayer error: what=$what, extra=$extra")
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            isEnginePlaying = false
            onError("Audio playback error: ${e.localizedMessage}")
        }
    }

    fun stop() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.stop()
                }
                mediaPlayer!!.reset()
                mediaPlayer!!.release()
                mediaPlayer = null
            }
        } catch (_: Exception) {}
        isEnginePlaying = false
    }

    fun isSpeaking(): Boolean = isEnginePlaying
}
