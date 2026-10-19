package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.accessibility.OgSahidAccessibilityService
import com.example.automation.AutomationResult
import com.example.automation.PhoneAutomationHelper
import com.example.data.api.GeminiService
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsManager
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.repository.MemoryRepository
import com.example.overlay.RgbBorderOverlayService
import com.example.voice.VoiceCallState
import com.example.voice.VoiceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    CHAT,
    CALLING,
    CODING,
    AUTOMATION,
    MEMORY,
    TOOLS,
    SETTINGS,
    PERMISSIONS,
    ABOUT
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val settingsManager = SettingsManager(application)
    private val database = AppDatabase.getInstance(application)
    val memoryRepository = MemoryRepository(database.yesminDao())
    private val geminiService = GeminiService()

    // Screen navigation state
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Chat / Calling UI states
    val chatMessages: StateFlow<List<ChatMessageEntity>> = memoryRepository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMemories: StateFlow<List<MemoryEntity>> = memoryRepository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _callSubtitles = MutableStateFlow("Tap Start Call to talk with OG SAHID AI.")
    val callSubtitles: StateFlow<String> = _callSubtitles.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    private var callTimerJob: Job? = null

    // Connection testing state
    private val _connectionStatus = MutableStateFlow<String?>(null)
    val connectionStatus: StateFlow<String?> = _connectionStatus.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    // Voice test status
    private val _voiceTestStatus = MutableStateFlow<String?>(null)
    val voiceTestStatus: StateFlow<String?> = _voiceTestStatus.asStateFlow()

    // Last automation result for feedback banner
    private val _lastAutomationResult = MutableStateFlow<AutomationResult?>(null)
    val lastAutomationResult: StateFlow<AutomationResult?> = _lastAutomationResult.asStateFlow()

    // Pending confirmation for sensitive actions (e.g. WhatsApp send)
    private var pendingAction: (() -> AutomationResult)? = null

    // Coding Assistant states
    private val _generatedCode = MutableStateFlow<String?>(null)
    val generatedCode: StateFlow<String?> = _generatedCode.asStateFlow()

    private val _codeExplanation = MutableStateFlow<String?>(null)
    val codeExplanation: StateFlow<String?> = _codeExplanation.asStateFlow()

    // Voice Engine
    lateinit var voiceManager: VoiceManager
        private set

    init {
        initVoice()
        checkAndSyncRgbBorder()
    }

    private fun initVoice() {
        voiceManager = VoiceManager(
            context = getApplication(),
            settingsManager = settingsManager,
            onSpeechRecognized = { text ->
                onUserSpeechReceived(text)
            },
            onVoiceStateChanged = { state ->
                // State changed
            },
            onVoiceError = { errorMsg ->
                _voiceTestStatus.value = "Voice Warning: $errorMsg (falling back to native voice)"
            }
        )
    }

    private fun checkAndSyncRgbBorder() {
        if (settingsManager.isRgbBorderEnabled()) {
            if (RgbBorderOverlayService.isOverlayPermissionGranted(getApplication())) {
                RgbBorderOverlayService.start(getApplication())
            }
        }
    }

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value == Screen.CALLING && screen != Screen.CALLING) {
            stopCalling()
        }
        _currentScreen.value = screen
    }

    // --- Voice Calling Mode ---
    fun startCalling() {
        _callSubtitles.value = "Listening to you... speak naturally"
        _callDurationSeconds.value = 0
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _callDurationSeconds.value += 1
            }
        }
        voiceManager.startCallingSession()
    }

    fun stopCalling() {
        callTimerJob?.cancel()
        voiceManager.stopCallingSession()
        _callSubtitles.value = "Call ended."
    }

    fun toggleCallMute(): Boolean {
        val muted = voiceManager.toggleMute()
        _callSubtitles.value = if (muted) "Microphone muted" else "Microphone unmuted - listening..."
        return muted
    }

    fun interruptCall() {
        voiceManager.interruptAndListen()
        _callSubtitles.value = "Interrupted. Listening to you..."
    }

    private fun onUserSpeechReceived(speechText: String) {
        _callSubtitles.value = "You: $speechText"
        sendChatMessage(speechText, isVoiceCall = true)
    }

    // --- Chat & Gemini Integration ---
    fun sendChatMessage(prompt: String, bitmap: Bitmap? = null, isVoiceCall: Boolean = false) {
        if (prompt.isBlank() && bitmap == null) return

        viewModelScope.launch {
            _isGenerating.value = true
            memoryRepository.insertChatMessage("user", prompt)

            val apiKey = settingsManager.getApiKey()
            val model = settingsManager.getSelectedModel()
            val persona = settingsManager.getPersona()
            val language = settingsManager.getLanguage()
            val temperature = settingsManager.getTemperature()

            // Check if user gave a phone automation or accessibility command!
            val autoResult = checkAndRunAutomationCommand(prompt)
            if (autoResult != null) {
                _lastAutomationResult.value = autoResult
                pendingAction = autoResult.pendingAction

                val responseMsg = if (autoResult.requiresUserConfirmation) {
                    autoResult.message
                } else {
                    autoResult.message
                }

                memoryRepository.insertChatMessage("assistant", responseMsg)
                if (isVoiceCall || settingsManager.isAutoTtsEnabled()) {
                    _callSubtitles.value = "OG SAHID AI: $responseMsg"
                    voiceManager.speak(responseMsg)
                }
                _isGenerating.value = false
                return@launch
            }

            if (apiKey.isBlank()) {
                val placeholderMsg = "Hello! I am OG SAHID AI. To enable full reasoning and conversational intelligence, please enter your Gemini API Key in Settings > AI Configuration."
                memoryRepository.insertChatMessage("assistant", placeholderMsg)
                if (isVoiceCall || settingsManager.isAutoTtsEnabled()) {
                    _callSubtitles.value = "OG SAHID AI: $placeholderMsg"
                    voiceManager.speak(placeholderMsg)
                }
                _isGenerating.value = false
                return@launch
            }

            val recentMemories = memoryRepository.getRecentMemoriesForContext()
            val history = memoryRepository.getRecentChatMessages(6).map { it.sender to it.text }

            val result = geminiService.sendMessage(
                apiKey = apiKey,
                model = model,
                userPrompt = prompt,
                recentHistory = history,
                memories = recentMemories,
                persona = persona,
                language = language,
                temperature = temperature,
                bitmap = bitmap
            )

            result.fold(
                onSuccess = { reply ->
                    memoryRepository.insertChatMessage("assistant", reply)
                    _callSubtitles.value = "OG SAHID AI: $reply"
                    if (isVoiceCall || settingsManager.isAutoTtsEnabled()) {
                        voiceManager.speak(reply)
                    }
                    detectAndStoreMemory(prompt, reply)
                },
                onFailure = { err ->
                    val errorText = "Encountered an issue: ${err.message}"
                    memoryRepository.insertChatMessage("assistant", errorText)
                    _callSubtitles.value = "OG SAHID AI: $errorText"
                }
            )

            _isGenerating.value = false
        }
    }

    // --- Action Confirmation Workflow ---
    fun confirmPendingAction() {
        val action = pendingAction
        if (action != null) {
            val result = action.invoke()
            _lastAutomationResult.value = result
            pendingAction = null
            viewModelScope.launch {
                memoryRepository.insertChatMessage("assistant", "Confirmed: ${result.message}")
            }
            if (settingsManager.isAutoTtsEnabled()) {
                voiceManager.speak("Confirmed. ${result.message}")
            }
        }
    }

    fun cancelPendingAction() {
        pendingAction = null
        _lastAutomationResult.value = null
        val msg = "Action cancelled."
        viewModelScope.launch {
            memoryRepository.insertChatMessage("assistant", msg)
        }
        if (settingsManager.isAutoTtsEnabled()) {
            voiceManager.speak(msg)
        }
    }

    // --- Command Parsing (Full Phone Automation & Accessibility) ---
    fun checkAndRunAutomationCommand(prompt: String): AutomationResult? {
        val lower = prompt.lowercase().trim()
        val context = getApplication<Application>()

        // Check for confirmation word if pending action exists
        if (pendingAction != null && (lower == "confirm" || lower == "yes" || lower == "send" || lower == "bhejo" || lower == "haan" || lower == "send it")) {
            val res = pendingAction?.invoke()
            pendingAction = null
            return res ?: AutomationResult(true, "Action Confirmed", "Dispatched requested action.")
        }

        return when {
            // WhatsApp message workflow (e.g. "WhatsApp kholo, Mom ki chat select karo aur Hi bhejo" or "Send Hi to Mom on WhatsApp")
            lower.contains("whatsapp") && (lower.contains("send") || lower.contains("bhejo") || lower.contains("type") || lower.contains("message")) -> {
                val contact = extractContactName(lower)
                val msg = extractMessageText(lower)
                PhoneAutomationHelper.prepareWhatsAppMessage(context, contact, msg, confirmed = false)
            }

            // Open WhatsApp direct
            lower == "open whatsapp" || lower == "whatsapp kholo" || lower == "launch whatsapp" -> {
                PhoneAutomationHelper.openWhatsApp(context)
            }

            // Global Navigation via AccessibilityService
            lower == "go back" || lower == "back" || lower == "wapas jao" || lower == "close screen" || lower == "close current screen" -> {
                PhoneAutomationHelper.performBack()
            }
            lower == "go home" || lower == "home screen" || lower == "home" || lower == "ghar chalo" -> {
                PhoneAutomationHelper.performHome()
            }
            lower.contains("scroll down") || lower == "niche scroll karo" || lower == "scroll" -> {
                PhoneAutomationHelper.performScrollDown()
            }
            lower.contains("scroll up") || lower == "upar scroll karo" -> {
                PhoneAutomationHelper.performScrollUp()
            }

            // Tap on specific UI target
            lower.startsWith("tap ") || lower.startsWith("click ") || lower.startsWith("daba do ") -> {
                val target = lower.removePrefix("tap ").removePrefix("click ").removePrefix("daba do ").trim()
                PhoneAutomationHelper.tapTargetByText(target)
            }

            // Type text into input field
            lower.startsWith("type ") || lower.startsWith("likho ") -> {
                val text = lower.removePrefix("type ").removePrefix("likho ").trim()
                PhoneAutomationHelper.enterText(text)
            }

            // YouTube
            lower.startsWith("play ") && (lower.contains("on youtube") || lower.contains("song")) -> {
                val query = lower.removePrefix("play ").replace("on youtube", "").trim()
                PhoneAutomationHelper.openYouTubeSearch(context, query)
            }
            lower.contains("youtube") -> {
                val query = lower.replace("open youtube", "").replace("search youtube for", "").trim()
                PhoneAutomationHelper.openYouTubeSearch(context, if (query.isNotBlank()) query else "Trending Music")
            }

            // Camera
            lower.contains("camera") || lower == "take photo" || lower == "take picture" -> {
                PhoneAutomationHelper.openCamera(context)
            }

            // Settings
            lower == "open phone settings" || lower == "open settings" || lower == "settings kholo" -> {
                PhoneAutomationHelper.openSettings(context)
            }

            // Timer
            lower.contains("timer") -> {
                val seconds = Regex("""\d+""").find(lower)?.value?.toIntOrNull() ?: 60
                PhoneAutomationHelper.setTimer(context, seconds, "OG SAHID AI Timer")
            }

            // Alarm
            lower.contains("alarm") -> {
                PhoneAutomationHelper.setAlarm(context, 7, 0, "Morning with OG SAHID AI")
            }

            // Call
            lower.startsWith("call ") -> {
                val number = Regex("""\d+""").find(lower)?.value ?: ""
                if (number.isNotBlank()) PhoneAutomationHelper.openDialer(context, number) else null
            }

            // Web Search
            lower.startsWith("search ") || lower.startsWith("google ") -> {
                val query = lower.removePrefix("search ").removePrefix("google ").trim()
                PhoneAutomationHelper.openWebSearch(context, query)
            }

            else -> null
        }
    }

    private fun extractContactName(input: String): String {
        val toMatch = Regex("""(?:to|send|select)\s+([A-Za-z0-9_]+)(?:'s|\s+ki)?""").find(input)
        if (toMatch != null) {
            val raw = toMatch.groupValues[1]
            if (raw != "whatsapp" && raw != "chat" && raw != "message") return raw.replaceFirstChar { it.uppercase() }
        }
        val kiMatch = Regex("""([A-Za-z0-9_]+)\s+ki\s+chat""").find(input)
        if (kiMatch != null) {
            return kiMatch.groupValues[1].replaceFirstChar { it.uppercase() }
        }
        return "Mom"
    }

    private fun extractMessageText(input: String): String {
        val quotes = Regex("""["'](.*?)["']""").find(input)
        if (quotes != null) return quotes.groupValues[1]
        val aurBhejo = Regex("""aur\s+(.*?)\s+bhejo""").find(input)
        if (aurBhejo != null) return aurBhejo.groupValues[1]
        val sendMatch = Regex("""(?:say|send|type|message)\s+([A-Za-z0-9\s!?]+)""").find(input)
        if (sendMatch != null) return sendMatch.groupValues[1]
        return "Hi"
    }

    private fun detectAndStoreMemory(userPrompt: String, reply: String) {
        viewModelScope.launch {
            val lower = userPrompt.lowercase()
            if (lower.contains("i like ") || lower.contains("my favorite ") || lower.contains("i love ") || lower.contains("remember that ")) {
                val title = if (userPrompt.length > 30) userPrompt.take(28) + "..." else userPrompt
                memoryRepository.insertMemory(
                    title = "User Preference",
                    content = userPrompt,
                    category = "Preference",
                    isSensitive = false
                )
            }
        }
    }

    // --- Voice Studio Preview Test ---
    fun testVoicePreview(sampleText: String = "Hello! I am OG SAHID AI, your personal Android assistant.") {
        viewModelScope.launch {
            _voiceTestStatus.value = "Generating voice preview..."
            voiceManager.speak(sampleText)
            _voiceTestStatus.value = "Playing: ${settingsManager.getVoiceProvider()}"
        }
    }

    // --- Rainbow RGB Border Overlay Controls ---
    fun toggleRgbBorder(enable: Boolean): Boolean {
        val context = getApplication<Application>()
        if (enable) {
            if (!RgbBorderOverlayService.isOverlayPermissionGranted(context)) {
                _lastAutomationResult.value = AutomationResult(
                    false,
                    "Overlay Permission Required",
                    "Please grant 'Display over other apps' permission to enable the Rainbow RGB Border."
                )
                return false
            }
            settingsManager.setRgbBorderEnabled(true)
            RgbBorderOverlayService.start(context)
            _lastAutomationResult.value = AutomationResult(
                true,
                "RGB Border Enabled",
                "Rainbow RGB border active around screen edges."
            )
            return true
        } else {
            settingsManager.setRgbBorderEnabled(false)
            RgbBorderOverlayService.stop(context)
            _lastAutomationResult.value = AutomationResult(
                true,
                "RGB Border Disabled",
                "Rainbow RGB border stopped."
            )
            return false
        }
    }

    fun isAccessibilityServiceActive(): Boolean {
        return OgSahidAccessibilityService.isRunning ||
                OgSahidAccessibilityService.isAccessibilityPermissionGranted(getApplication())
    }

    fun isOverlayPermissionActive(): Boolean {
        return RgbBorderOverlayService.isOverlayPermissionGranted(getApplication())
    }

    // --- Coding Assistant ---
    fun generateCode(prompt: String, language: String, isDebugging: Boolean = false) {
        viewModelScope.launch {
            _isGenerating.value = true
            _generatedCode.value = null
            _codeExplanation.value = null

            val apiKey = settingsManager.getApiKey()
            val model = settingsManager.getSelectedModel()

            if (apiKey.isBlank()) {
                _generatedCode.value = "// Gemini API key is missing.\n// Please set your key in Settings."
                _codeExplanation.value = "Cannot generate code without a valid Gemini API key."
                _isGenerating.value = false
                return@launch
            }

            val systemInstr = """
You are a senior principal developer and code mentor in $language.
The user wants ${if (isDebugging) "to debug their code and fix errors" else "a complete, clean, tested implementation"}.
Format your response in two clear sections:
```$language
// Complete code here with comments
```
EXPLANATION:
Detailed beginner-friendly breakdown of how the code works, architecture, and Termux/Android build steps if applicable.
            """.trimIndent()

            val result = geminiService.sendMessage(
                apiKey = apiKey,
                model = model,
                userPrompt = "Language: $language\nTask: $prompt\nProvide production-grade code.",
                recentHistory = emptyList(),
                memories = emptyList(),
                persona = "Coding & Tech Mentor",
                language = "English",
                temperature = 0.2f
            )

            result.fold(
                onSuccess = { fullText ->
                    val codeRegex = Regex("""```(?:[a-zA-Z0-9_\-+]*)\n([\s\S]*?)```""")
                    val match = codeRegex.find(fullText)
                    if (match != null) {
                        _generatedCode.value = match.groupValues[1].trim()
                        val explanationPart = fullText.replace(match.value, "").trim()
                        _codeExplanation.value = explanationPart.ifBlank { "Code successfully generated." }
                    } else {
                        _generatedCode.value = fullText
                        _codeExplanation.value = "Review the generated solution above."
                    }
                },
                onFailure = { err ->
                    _generatedCode.value = "// Error: ${err.message}"
                    _codeExplanation.value = "Generation failed. Please verify your API key and network connection."
                }
            )

            _isGenerating.value = false
        }
    }

    // --- Settings Connection Test ---
    fun testApiConnection() {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _connectionStatus.value = "Testing Gemini API connection..."
            val key = settingsManager.getApiKey()
            val model = settingsManager.getSelectedModel()

            val result = geminiService.testConnection(key, model)
            result.fold(
                onSuccess = { msg ->
                    _connectionStatus.value = msg
                },
                onFailure = { err ->
                    _connectionStatus.value = "Failed: ${err.message}"
                }
            )
            _isTestingConnection.value = false
        }
    }

    fun dismissAutomationBanner() {
        _lastAutomationResult.value = null
    }

    override fun onCleared() {
        super.onCleared()
        callTimerJob?.cancel()
        voiceManager.destroy()
    }
}
