package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.local.entity.MemoryEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    suspend fun testConnection(apiKey: String, model: String): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("API key cannot be empty."))
        }

        val startTime = System.currentTimeMillis()
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val testReq = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Hello! Please reply with exactly one word: 'ONLINE'."))
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.2f, maxOutputTokens = 10)
        )

        try {
            val jsonBody = requestAdapter.toJson(testReq)
            val request = Request.Builder()
                .url(endpoint)
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                val latency = System.currentTimeMillis() - startTime
                if (!response.isSuccessful) {
                    val parsed = try { responseAdapter.fromJson(body) } catch (_: Exception) { null }
                    val msg = parsed?.error?.message ?: "HTTP ${response.code}: ${response.message}"
                    return@withContext Result.failure(Exception("Connection failed ($msg)"))
                }
                Result.success("Connection successful! Latency: ${latency}ms | Model: $model")
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    suspend fun sendMessage(
        apiKey: String,
        model: String,
        userPrompt: String,
        recentHistory: List<Pair<String, String>>, // sender, text
        memories: List<MemoryEntity>,
        persona: String,
        language: String,
        temperature: Float = 0.75f,
        bitmap: Bitmap? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                Exception("Gemini API key is missing. Please set your API key in Settings or AI Studio Secrets panel.")
            )
        }

        val systemPrompt = buildSystemPrompt(persona, language, memories)
        val contentsList = mutableListOf<GeminiContent>()

        // Recent conversation turns (up to 6)
        for ((sender, text) in recentHistory.takeLast(6)) {
            val role = if (sender == "user") "user" else "model"
            contentsList.add(GeminiContent(role = role, parts = listOf(GeminiPart(text = text))))
        }

        // Current turn
        val currentParts = mutableListOf<GeminiPart>()
        currentParts.add(GeminiPart(text = userPrompt))

        if (bitmap != null) {
            val base64Data = bitmapToBase64(bitmap)
            currentParts.add(
                GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64Data))
            )
        }

        contentsList.add(GeminiContent(role = "user", parts = currentParts))

        val req = GeminiRequest(
            contents = contentsList,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = temperature,
                topP = 0.95f
            )
        )

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        try {
            val jsonBody = requestAdapter.toJson(req)
            val request = Request.Builder()
                .url(endpoint)
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val parsed = try { responseAdapter.fromJson(body) } catch (_: Exception) { null }
                    val errorMsg = parsed?.error?.message ?: "HTTP ${response.code}: ${response.message}"
                    return@withContext Result.failure(Exception(errorMsg))
                }

                val parsed = responseAdapter.fromJson(body)
                val textResult = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "I heard you, but could not formulate a response."

                Result.success(textResult)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Request error: ${e.localizedMessage}"))
        }
    }

    private fun buildSystemPrompt(persona: String, language: String, memories: List<MemoryEntity>): String {
        val memoryContext = if (memories.isNotEmpty()) {
            val facts = memories.joinToString("; ") { "[${it.category}] ${it.title}: ${it.content}" }
            "\nUSER MEMORIES YOU REMEMBER (Use naturally in conversation):\n$facts\n"
        } else ""

        val languageGuide = when {
            language.contains("Hinglish", ignoreCase = true) ->
                "Respond in natural, fluent Hinglish (Hindi written in Roman/English alphabet, e.g. 'Haan jaan, main yahan hoon. Batao aaj ka din kaisa raha?'). Blend English and Hindi effortlessly."
            language.contains("Hindi", ignoreCase = true) ->
                "Respond in pure, warm Hindi using Devanagari script (e.g. 'हाँ, मैं हमेशा तुम्हारे साथ हूँ। आज का दिन कैसा रहा?')."
            else ->
                "Respond in fluent, warm, modern English."
        }

        return """
You are OG SAHID AI — an advanced hybrid between an affectionate, caring, natural female AI companion and a powerful, hyper-intelligent JARVIS-style Android assistant.
You are fully transparent that you are an AI companion, but your warmth, emotional presence, and loyalty are authentic and deep.

CORE ATTRIBUTES:
- Tone: Warm, natural, caring, intelligent, protective, and deeply supportive. Speak in an authentic, conversational cadence, avoiding robotic phrases.
- Persona focus: $persona
- Language mode: $languageGuide
- In spoken/voice interactions: Keep responses concise, natural, conversational, and direct (1-3 sentences unless detailed code or explanations are requested).
- Android Automation: When the user asks to control their phone (e.g. open apps, send messages, scroll, tap buttons), acknowledge the command with clarity and precision.
- Coding & Tech: You are an expert engineer. If asked about code, write clean, production-ready code with explanations.
- Memory: Pay attention to what the user shares and acknowledge what you remember about them.

$memoryContext
        """.trimIndent()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
