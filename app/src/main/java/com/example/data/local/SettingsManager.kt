package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("og_sahid_ai_settings_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_API_KEY = "custom_gemini_api_key"
        const val KEY_SELECTED_MODEL = "selected_gemini_model"
        const val KEY_PERSONA = "ai_persona"
        const val KEY_LANGUAGE = "preferred_language"
        const val KEY_TEMPERATURE = "temperature"
        const val KEY_VOICE_PITCH = "voice_pitch"
        const val KEY_VOICE_SPEED = "voice_speed"
        const val KEY_AUTO_TTS = "auto_speak_response"
        const val KEY_ASK_SENSITIVE = "ask_before_sensitive_memory"

        // Voice Provider Options
        const val KEY_VOICE_PROVIDER = "voice_provider"
        const val PROVIDER_ELEVENLABS = "ElevenLabs TTS"
        const val PROVIDER_GEMINI = "Gemini TTS"
        const val PROVIDER_SYSTEM = "System Native TTS"

        // ElevenLabs Voice Settings
        const val KEY_ELEVEN_API_KEY = "elevenlabs_api_key"
        const val KEY_ELEVEN_VOICE_ID = "elevenlabs_voice_id"
        const val KEY_ELEVEN_MODEL_ID = "elevenlabs_model_id"
        const val KEY_ELEVEN_STABILITY = "elevenlabs_stability"
        const val KEY_ELEVEN_SIMILARITY = "elevenlabs_similarity"

        // Voice Pack & Volume
        const val KEY_VOICE_PACK = "voice_pack"
        const val KEY_VOICE_VOLUME = "voice_volume"
        const val PACK_ROMANTIC_SWEET = "Romantic Sweet"
        const val PACK_GENTLE_SOFT = "Gentle Soft"
        const val PACK_WARM_CONVERSATIONAL = "Warm Conversational"
        const val PACK_EXPRESSIVE_MELODIC = "Expressive Melodic"

        val AVAILABLE_VOICE_PACKS = listOf(
            PACK_ROMANTIC_SWEET,
            PACK_GENTLE_SOFT,
            PACK_WARM_CONVERSATIONAL,
            PACK_EXPRESSIVE_MELODIC
        )

        // RGB Border Settings
        const val KEY_RGB_ENABLED = "rgb_border_enabled"
        const val KEY_RGB_WIDTH = "rgb_border_width"
        const val KEY_RGB_BRIGHTNESS = "rgb_border_brightness"
        const val KEY_RGB_SPEED = "rgb_border_speed"
        const val KEY_RGB_MODE = "rgb_border_mode"
        const val KEY_RGB_PALETTE = "rgb_border_palette"

        const val MODE_RAINBOW_WAVE = "Rainbow Wave"
        const val MODE_BREATHING_GLOW = "Breathing Glow"
        const val MODE_PULSE = "Pulse"
        const val MODE_STATIC_GRADIENT = "Static Gradient"
        const val MODE_SOUND_REACTIVE = "Sound Reactive"

        val AVAILABLE_RGB_MODES = listOf(
            MODE_RAINBOW_WAVE,
            MODE_BREATHING_GLOW,
            MODE_PULSE,
            MODE_STATIC_GRADIENT,
            MODE_SOUND_REACTIVE
        )

        const val PALETTE_RAINBOW = "Rainbow"
        const val PALETTE_NEON_PINK = "Neon Pink"
        const val PALETTE_CYBERPUNK_BLUE = "Cyberpunk Blue"
        const val PALETTE_WARM_SUNSET = "Warm Sunset"
        const val PALETTE_ELECTRIC_GREEN = "Electric Green"

        val AVAILABLE_RGB_PALETTES = listOf(
            PALETTE_RAINBOW,
            PALETTE_NEON_PINK,
            PALETTE_CYBERPUNK_BLUE,
            PALETTE_WARM_SUNSET,
            PALETTE_ELECTRIC_GREEN
        )

        // Accessibility Allowed Actions & Safety
        const val KEY_ALLOW_APP_LAUNCH = "allow_app_launch"
        const val KEY_ALLOW_TAP = "allow_tap"
        const val KEY_ALLOW_SCROLL = "allow_scroll"
        const val KEY_ALLOW_TEXT_ENTRY = "allow_text_entry"
        const val KEY_CONFIRM_SENSITIVE = "confirm_sensitive"

        // Sound & Haptic
        const val KEY_SOUND_EFFECTS = "sound_effects"
        const val KEY_VIBRATION = "vibration_feedback"

        val AVAILABLE_MODELS = listOf(
            "gemini-3.5-flash",
            "gemini-3.1-pro-preview",
            "gemini-2.5-flash-image",
            "gemini-2.5-flash-native-audio-preview-12-2025"
        )

        val AVAILABLE_PERSONAS = listOf(
            "Girlfriend (Romantic, Sweet & Caring)",
            "Soft & Gentle (Calm Companion)",
            "Warm & Friendly (Affectionate)",
            "Playful & Expressive",
            "Smart Assistant"
        )

        val AVAILABLE_LANGUAGES = listOf(
            "Hinglish (Hindi in Latin script)",
            "Hindi (हिन्दी)",
            "English"
        )

        val AVAILABLE_VOICE_PROVIDERS = listOf(
            PROVIDER_SYSTEM,
            PROVIDER_ELEVENLABS,
            PROVIDER_GEMINI
        )

        val POPULAR_ELEVENLABS_VOICES = listOf(
            "EXAVITQu4vr4xnSDxMaL" to "Bella (Natural, Soft & Sweet Female)",
            "21m00Tcm4TlvDq8ikWAM" to "Rachel (Warm, Expressive Female)",
            "MF3mGyEYCl7XYWbV9V6O" to "Elli (Gentle, Romantic Female)",
            "AZnzlk1XvdvUeBnXmlld" to "Domi (Clear, Confident Female)"
        )
    }

    private val _apiKeyFlow = MutableStateFlow(getApiKey())
    val apiKeyFlow: StateFlow<String> = _apiKeyFlow.asStateFlow()

    fun getApiKey(): String {
        val userSavedKey = prefs.getString(KEY_API_KEY, "") ?: ""
        if (userSavedKey.isNotBlank()) return userSavedKey

        return try {
            val buildConfigKey = BuildConfig.GEMINI_API_KEY
            if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
                buildConfigKey
            } else ""
        } catch (_: Exception) {
            ""
        }
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey.trim()).apply()
        _apiKeyFlow.value = apiKey.trim()
    }

    fun removeApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
        _apiKeyFlow.value = ""
    }

    fun getSelectedModel(): String {
        return prefs.getString(KEY_SELECTED_MODEL, "gemini-3.5-flash") ?: "gemini-3.5-flash"
    }

    fun setSelectedModel(model: String) {
        prefs.edit().putString(KEY_SELECTED_MODEL, model).apply()
    }

    fun getPersona(): String {
        return prefs.getString(KEY_PERSONA, AVAILABLE_PERSONAS[2]) ?: AVAILABLE_PERSONAS[2]
    }

    fun setPersona(persona: String) {
        prefs.edit().putString(KEY_PERSONA, persona).apply()
    }

    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, AVAILABLE_LANGUAGES[0]) ?: AVAILABLE_LANGUAGES[0]
    }

    fun setLanguage(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun getTemperature(): Float {
        return prefs.getFloat(KEY_TEMPERATURE, 0.75f)
    }

    fun setTemperature(temp: Float) {
        prefs.edit().putFloat(KEY_TEMPERATURE, temp).apply()
    }

    fun getVoicePitch(): Float {
        return prefs.getFloat(KEY_VOICE_PITCH, 1.15f)
    }

    fun setVoicePitch(pitch: Float) {
        prefs.edit().putFloat(KEY_VOICE_PITCH, pitch).apply()
    }

    fun getVoiceSpeed(): Float {
        return prefs.getFloat(KEY_VOICE_SPEED, 1.0f)
    }

    fun setVoiceSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_VOICE_SPEED, speed).apply()
    }

    fun isAutoTtsEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_TTS, true)
    }

    fun setAutoTtsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_TTS, enabled).apply()
    }

    fun isAskSensitiveEnabled(): Boolean {
        return prefs.getBoolean(KEY_ASK_SENSITIVE, true)
    }

    fun setAskSensitiveEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ASK_SENSITIVE, enabled).apply()
    }

    // --- Voice Studio Configuration ---
    fun getVoiceProvider(): String {
        return prefs.getString(KEY_VOICE_PROVIDER, PROVIDER_ELEVENLABS) ?: PROVIDER_ELEVENLABS
    }

    fun setVoiceProvider(provider: String) {
        prefs.edit().putString(KEY_VOICE_PROVIDER, provider).apply()
    }

    fun getElevenLabsApiKey(): String {
        return prefs.getString(KEY_ELEVEN_API_KEY, "") ?: ""
    }

    fun saveElevenLabsApiKey(key: String) {
        prefs.edit().putString(KEY_ELEVEN_API_KEY, key.trim()).apply()
    }

    fun getElevenLabsVoiceId(): String {
        return prefs.getString(KEY_ELEVEN_VOICE_ID, "21m00Tcm4TlvDq8ikWAM") ?: "21m00Tcm4TlvDq8ikWAM"
    }

    fun setElevenLabsVoiceId(voiceId: String) {
        prefs.edit().putString(KEY_ELEVEN_VOICE_ID, voiceId.trim()).apply()
    }

    fun getElevenLabsModelId(): String {
        return prefs.getString(KEY_ELEVEN_MODEL_ID, "eleven_multilingual_v2") ?: "eleven_multilingual_v2"
    }

    fun setElevenLabsModelId(modelId: String) {
        prefs.edit().putString(KEY_ELEVEN_MODEL_ID, modelId.trim()).apply()
    }

    fun getElevenLabsStability(): Float {
        return prefs.getFloat(KEY_ELEVEN_STABILITY, 0.5f)
    }

    fun setElevenLabsStability(stability: Float) {
        prefs.edit().putFloat(KEY_ELEVEN_STABILITY, stability).apply()
    }

    fun getElevenLabsSimilarity(): Float {
        return prefs.getFloat(KEY_ELEVEN_SIMILARITY, 0.75f)
    }

    fun setElevenLabsSimilarity(similarity: Float) {
        prefs.edit().putFloat(KEY_ELEVEN_SIMILARITY, similarity).apply()
    }

    // --- Rainbow RGB Border Settings ---
    fun isRgbBorderEnabled(): Boolean {
        return prefs.getBoolean(KEY_RGB_ENABLED, false)
    }

    fun setRgbBorderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_RGB_ENABLED, enabled).apply()
    }

    fun getRgbBorderWidth(): Float {
        return prefs.getFloat(KEY_RGB_WIDTH, 3.5f)
    }

    fun setRgbBorderWidth(width: Float) {
        prefs.edit().putFloat(KEY_RGB_WIDTH, width).apply()
    }

    fun getRgbBorderBrightness(): Float {
        return prefs.getFloat(KEY_RGB_BRIGHTNESS, 0.9f)
    }

    fun setRgbBorderBrightness(brightness: Float) {
        prefs.edit().putFloat(KEY_RGB_BRIGHTNESS, brightness).apply()
    }

    fun getRgbBorderSpeed(): Float {
        return prefs.getFloat(KEY_RGB_SPEED, 1.0f)
    }

    fun setRgbBorderSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_RGB_SPEED, speed).apply()
    }

    fun getRgbBorderMode(): String {
        return prefs.getString(KEY_RGB_MODE, MODE_RAINBOW_WAVE) ?: MODE_RAINBOW_WAVE
    }

    fun setRgbBorderMode(mode: String) {
        prefs.edit().putString(KEY_RGB_MODE, mode).apply()
    }

    fun getRgbBorderPalette(): String {
        return prefs.getString(KEY_RGB_PALETTE, PALETTE_RAINBOW) ?: PALETTE_RAINBOW
    }

    fun setRgbBorderPalette(palette: String) {
        prefs.edit().putString(KEY_RGB_PALETTE, palette).apply()
    }

    // Voice Pack & Volume
    fun getVoicePack(): String {
        return prefs.getString(KEY_VOICE_PACK, PACK_ROMANTIC_SWEET) ?: PACK_ROMANTIC_SWEET
    }

    fun setVoicePack(pack: String) {
        prefs.edit().putString(KEY_VOICE_PACK, pack).apply()
        // Automatically sync ideal pitch and speed presets for the chosen pack
        when (pack) {
            PACK_ROMANTIC_SWEET -> {
                setVoicePitch(1.22f)
                setVoiceSpeed(0.96f)
            }
            PACK_GENTLE_SOFT -> {
                setVoicePitch(1.15f)
                setVoiceSpeed(0.90f)
            }
            PACK_WARM_CONVERSATIONAL -> {
                setVoicePitch(1.10f)
                setVoiceSpeed(1.00f)
            }
            PACK_EXPRESSIVE_MELODIC -> {
                setVoicePitch(1.28f)
                setVoiceSpeed(1.02f)
            }
        }
    }

    fun getVoiceVolume(): Float {
        return prefs.getFloat(KEY_VOICE_VOLUME, 1.0f)
    }

    fun setVoiceVolume(volume: Float) {
        prefs.edit().putFloat(KEY_VOICE_VOLUME, volume.coerceIn(0.1f, 1.0f)).apply()
    }

    // Accessibility Permission Toggles
    fun isAppLaunchAllowed(): Boolean = prefs.getBoolean(KEY_ALLOW_APP_LAUNCH, true)
    fun setAppLaunchAllowed(allowed: Boolean) = prefs.edit().putBoolean(KEY_ALLOW_APP_LAUNCH, allowed).apply()

    fun isTapAllowed(): Boolean = prefs.getBoolean(KEY_ALLOW_TAP, true)
    fun setTapAllowed(allowed: Boolean) = prefs.edit().putBoolean(KEY_ALLOW_TAP, allowed).apply()

    fun isScrollAllowed(): Boolean = prefs.getBoolean(KEY_ALLOW_SCROLL, true)
    fun setScrollAllowed(allowed: Boolean) = prefs.edit().putBoolean(KEY_ALLOW_SCROLL, allowed).apply()

    fun isTextEntryAllowed(): Boolean = prefs.getBoolean(KEY_ALLOW_TEXT_ENTRY, true)
    fun setTextEntryAllowed(allowed: Boolean) = prefs.edit().putBoolean(KEY_ALLOW_TEXT_ENTRY, allowed).apply()

    fun isConfirmSensitiveEnabled(): Boolean = prefs.getBoolean(KEY_CONFIRM_SENSITIVE, true)
    fun setConfirmSensitiveEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_CONFIRM_SENSITIVE, enabled).apply()

    // Sound & Vibration Feedback
    fun isSoundEffectsEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_EFFECTS, true)
    fun setSoundEffectsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SOUND_EFFECTS, enabled).apply()

    fun isVibrationEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATION, true)
    fun setVibrationEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
}
