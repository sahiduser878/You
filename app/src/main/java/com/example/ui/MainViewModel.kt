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
import com.example.screen.ScreenCaptureHelper
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
    SCREEN_SHARE,
    EDGE_GLOW,
    ACCESSIBILITY,
    AUTOMATION,
    CODING,
    MEMORY,
    TOOLS,
    SETTINGS,
    PERMISSIONS,
    ABOUT
}

enum class VoiceCommandStatus {
    READY,
    LISTENING,
    PROCESSING,
    EXECUTING,
    SPEAKING,
    COMPLETED
}

data class CommandActivityItem(
    val title: String,
    val description: String,
    val timeAgo: String,
    val isSuccess: Boolean
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val settingsManager = SettingsManager(application)
    private val database = AppDatabase.getInstance(application)
    val memoryRepository = MemoryRepository(database.yesminDao())
    private val geminiService = GeminiService()

    // Screen navigation state
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Screen understanding & capture helper
    val screenCaptureHelper = ScreenCaptureHelper(application, settingsManager, viewModelScope)

    // Voice Command Status Pipeline
    private val _commandStatus = MutableStateFlow(VoiceCommandStatus.READY)
    val commandStatus: StateFlow<VoiceCommandStatus> = _commandStatus.asStateFlow()

    private val _currentActionText = MutableStateFlow("Ready for voice commands")
    val currentActionText: StateFlow<String> = _currentActionText.asStateFlow()

    // Recent activity list
    private val _recentActivity = MutableStateFlow(
        listOf(
            CommandActivityItem("NISU AI Initialized", "Fast voice engine ready with Sweet Romantic pack", "Just now", true),
            CommandActivityItem("Accessibility Ready", "System automation & phone control active", "1m ago", true),
            CommandActivityItem("RGB Edge Lighting", "Customizable multi-color border loaded", "2m ago", true)
        )
    )
    val recentActivity: StateFlow<List<CommandActivityItem>> = _recentActivity.asStateFlow()

    // Chat / Calling UI states
    val chatMessages: StateFlow<List<ChatMessageEntity>> = memoryRepository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMemories: StateFlow<List<MemoryEntity>> = memoryRepository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _callSubtitles = MutableStateFlow("Tap Start Call to talk with NISU AI.")
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

    fun dismissAutomationBanner() {
        _lastAutomationResult.value = null
    }

    // Pending confirmation for sensitive actions (e.g. WhatsApp send)
    private var pendingAction: (() -> AutomationResult)? = null
    private val _pendingConfirmationMessage = MutableStateFlow<String?>(null)
    val pendingConfirmationMessage: StateFlow<String?> = _pendingConfirmationMessage.asStateFlow()

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
                when (state) {
                    VoiceCallState.LISTENING -> {
                        _commandStatus.value = VoiceCommandStatus.LISTENING
                        _currentActionText.value = "Listening to you..."
                    }
                    VoiceCallState.THINKING -> {
                        _commandStatus.value = VoiceCommandStatus.PROCESSING
                        _currentActionText.value = "Processing command..."
                    }
                    VoiceCallState.SPEAKING -> {
                        _commandStatus.value = VoiceCommandStatus.SPEAKING
                        _currentActionText.value = "NISU speaking..."
                    }
                    VoiceCallState.IDLE -> {
                        if (_commandStatus.value != VoiceCommandStatus.EXECUTING) {
                            _commandStatus.value = VoiceCommandStatus.READY
                            _currentActionText.value = "Ready"
                        }
                    }
                    VoiceCallState.MUTED -> {
                        _currentActionText.value = "Microphone muted"
                    }
                }
            },
            onVoiceError = { errorMsg ->
                _voiceTestStatus.value = "Voice note: $errorMsg (playing sweet native voice)"
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

    // --- Real-time Fast Microphone Trigger ---
    fun startListeningInstant() {
        _commandStatus.value = VoiceCommandStatus.LISTENING
        _currentActionText.value = "Listening... Speak your command"
        voiceManager.startListeningOnce()
    }

    fun stopListeningInstant() {
        voiceManager.stopListening()
        _commandStatus.value = VoiceCommandStatus.READY
        _currentActionText.value = "Ready"
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
        _commandStatus.value = VoiceCommandStatus.LISTENING
    }

    private fun onUserSpeechReceived(speechText: String) {
        _callSubtitles.value = "You: $speechText"
        sendChatMessage(speechText, isVoiceCall = true)
    }

    // --- Chat & Real-Time Command Pipeline ---
    fun sendChatMessage(prompt: String, bitmap: Bitmap? = null, isVoiceCall: Boolean = false) {
        if (prompt.isBlank() && bitmap == null) return

        viewModelScope.launch {
            _isGenerating.value = true
            _commandStatus.value = VoiceCommandStatus.PROCESSING
            _currentActionText.value = "Analyzing: \"$prompt\""
            memoryRepository.insertChatMessage("user", prompt)

            val apiKey = settingsManager.getApiKey()
            val model = settingsManager.getSelectedModel()
            val persona = settingsManager.getPersona()
            val language = settingsManager.getLanguage()
            val temperature = settingsManager.getTemperature()

            // 1. FAST LOCAL AUTOMATION / ACCESSIBILITY CHECK (0ms latency!)
            val autoResult = checkAndRunAutomationCommand(prompt)
            if (autoResult != null) {
                _commandStatus.value = VoiceCommandStatus.EXECUTING
                _lastAutomationResult.value = autoResult

                if (autoResult.requiresUserConfirmation) {
                    pendingAction = autoResult.pendingAction
                    _pendingConfirmationMessage.value = autoResult.message
                } else {
                    pendingAction = null
                    _pendingConfirmationMessage.value = null
                }

                val responseMsg = autoResult.message
                memoryRepository.insertChatMessage("assistant", responseMsg)
                _currentActionText.value = autoResult.actionName
                _commandStatus.value = VoiceCommandStatus.COMPLETED

                addRecentActivity(
                    title = autoResult.actionName,
                    description = autoResult.message,
                    success = autoResult.success
                )

                if (isVoiceCall || settingsManager.isAutoTtsEnabled()) {
                    _callSubtitles.value = "NISU AI: $responseMsg"
                    voiceManager.speak(responseMsg)
                }
                _isGenerating.value = false
                return@launch
            }

            // 2. CONVERSATIONAL AI (GEMINI)
            if (apiKey.isBlank()) {
                val placeholderMsg = "Hello! I am NISU AI. To enable full reasoning, please enter your Gemini API Key in Settings > AI Configuration."
                memoryRepository.insertChatMessage("assistant", placeholderMsg)
                _commandStatus.value = VoiceCommandStatus.COMPLETED
                _currentActionText.value = "Awaiting API Key"
                if (isVoiceCall || settingsManager.isAutoTtsEnabled()) {
                    _callSubtitles.value = "NISU AI: $placeholderMsg"
                    voiceManager.speak(placeholderMsg)
                }
                _isGenerating.value = false
                return@launch
            }

            val recentMemories = memoryRepository.getRecentMemoriesForContext()
            val history = memoryRepository.getRecentChatMessages(6).map { it.sender to it.text }

            _commandStatus.value = VoiceCommandStatus.PROCESSING
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
                    _callSubtitles.value = "NISU AI: $reply"
                    _commandStatus.value = VoiceCommandStatus.SPEAKING
                    _currentActionText.value = "Responding"
                    if (isVoiceCall || settingsManager.isAutoTtsEnabled()) {
                        voiceManager.speak(reply)
                    }
                    detectAndStoreMemory(prompt, reply)
                    addRecentActivity("AI Response", prompt.take(30), true)
                },
                onFailure = { err ->
                    val errorText = "Encountered an issue: ${err.message}"
                    memoryRepository.insertChatMessage("assistant", errorText)
                    _callSubtitles.value = "NISU AI: $errorText"
                    _commandStatus.value = VoiceCommandStatus.COMPLETED
                    _currentActionText.value = "Error"
                }
            )

            _isGenerating.value = false
        }
    }

    private fun addRecentActivity(title: String, description: String, success: Boolean) {
        val current = _recentActivity.value.toMutableList()
        current.add(0, CommandActivityItem(title, description, "Just now", success))
        if (current.size > 8) {
            _recentActivity.value = current.take(8)
        } else {
            _recentActivity.value = current
        }
    }

    // --- Action Confirmation Workflow ---
    fun confirmPendingAction() {
        val action = pendingAction
        if (action != null) {
            val result = action.invoke()
            _lastAutomationResult.value = result
            pendingAction = null
            _pendingConfirmationMessage.value = null
            _commandStatus.value = VoiceCommandStatus.COMPLETED

            viewModelScope.launch {
                memoryRepository.insertChatMessage("assistant", "Confirmed: ${result.message}")
            }
            addRecentActivity("Confirmed Action", result.message, result.success)
            if (settingsManager.isAutoTtsEnabled()) {
                voiceManager.speak("Confirmed. ${result.message}")
            }
        }
    }

    fun cancelPendingAction() {
        pendingAction = null
        _pendingConfirmationMessage.value = null
        _lastAutomationResult.value = null
        _commandStatus.value = VoiceCommandStatus.READY
        val msg = "Action cancelled."
        viewModelScope.launch {
            memoryRepository.insertChatMessage("assistant", msg)
        }
        addRecentActivity("Cancelled Action", "User aborted action", false)
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
            _pendingConfirmationMessage.value = null
            return res ?: AutomationResult(true, "Action Confirmed", "Dispatched requested action.")
        }

        return when {
            // WhatsApp message workflow (e.g. "WhatsApp kholo, Mom ki chat select karo aur Hi bhejo" or "Send Hi to Mom on WhatsApp")
            lower.contains("whatsapp") && (lower.contains("send") || lower.contains("bhejo") || lower.contains("type") || lower.contains("message")) -> {
                val contact = extractContactName(lower)
                val msg = extractMessageText(lower)
                PhoneAutomationHelper.prepareWhatsAppMessage(context, contact, msg, confirmed = !settingsManager.isConfirmSensitiveEnabled())
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
            lower.contains("recent apps") || lower.contains("recents") -> {
                OgSahidAccessibilityService.instance?.let {
                    val ok = it.performRecents()
                    AutomationResult(ok, "Recent Apps", if (ok) "Opened recent apps." else "Accessibility action failed.")
                } ?: AutomationResult(false, "Recent Apps", "Accessibility service not active.")
            }
            lower.contains("notifications") -> {
                OgSahidAccessibilityService.instance?.let {
                    val ok = it.performNotifications()
                    AutomationResult(ok, "Notifications", if (ok) "Expanded notifications panel." else "Accessibility action failed.")
                } ?: AutomationResult(false, "Notifications", "Accessibility service not active.")
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

            // Brightness controls
            lower.contains("brightness") -> {
                PhoneAutomationHelper.openDisplaySettings(context)
            }

            // Volume controls
            lower.contains("increase volume") || lower.contains("volume up") || lower.contains("awaz badhao") -> {
                PhoneAutomationHelper.adjustVolume(context, raise = true)
            }
            lower.contains("decrease volume") || lower.contains("volume down") || lower.contains("awaz kam karo") -> {
                PhoneAutomationHelper.adjustVolume(context, raise = false)
            }
            lower.contains("mute") || lower.contains("chup ho jao") -> {
                PhoneAutomationHelper.setMuteVolume(context, mute = true)
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
                PhoneAutomationHelper.setTimer(context, seconds, "NISU AI Timer")
            }

            // Alarm
            lower.contains("alarm") -> {
                PhoneAutomationHelper.setAlarm(context, 7, 0, "Morning with NISU AI")
            }

            // Call
            lower.startsWith("call ") -> {
                val number = Regex("""\d+""").find(lower)?.value ?: ""
                if (number.isNotBlank()) PhoneAutomationHelper.openDialer(context, number) else null
            }

            // Generic Open App command (e.g. "open chrome", "open maps", "open calculator")
            lower.startsWith("open ") || lower.startsWith("launch ") -> {
                val targetApp = lower.removePrefix("open ").removePrefix("launch ").trim()
                PhoneAutomationHelper.openApp(context, targetApp)
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
    fun testVoicePreview(sampleText: String = "Hello sweetheart, I am NISU AI. Aapka din kaisa raha? Main hamesha aapke saath hoon!") {
        viewModelScope.launch {
            _voiceTestStatus.value = "Playing sweet female voice preview..."
            voiceManager.speak(sampleText)
            _voiceTestStatus.value = "Active: ${settingsManager.getVoicePack()} (${settingsManager.getVoiceProvider()})"
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
                    "Please grant 'Display over other apps' permission to enable the RGB Border system overlay."
                )
                return false
            }
            settingsManager.setRgbBorderEnabled(true)
            RgbBorderOverlayService.start(context)
            _lastAutomationResult.value = AutomationResult(
                true,
                "RGB Border Enabled",
                "RGB border active around screen edges."
            )
            return true
        } else {
            settingsManager.setRgbBorderEnabled(false)
            RgbBorderOverlayService.stop(context)
            _lastAutomationResult.value = AutomationResult(
                true,
                "RGB Border Disabled",
                "RGB border system overlay stopped."
            )
            return true
        }
    }

    // --- Gemini Connection Testing ---
    fun testGeminiConnection() {
        val apiKey = settingsManager.getApiKey()
        val model = settingsManager.getSelectedModel()
        _isTestingConnection.value = true
        _connectionStatus.value = "Connecting to Gemini ($model)..."

        viewModelScope.launch {
            val result = geminiService.testConnection(apiKey, model)
            result.fold(
                onSuccess = { msg ->
                    _connectionStatus.value = msg
                },
                onFailure = { err ->
                    _connectionStatus.value = "Error: ${err.message}"
                }
            )
            _isTestingConnection.value = false
        }
    }

    // --- Coding Assistant Integration ---
    fun generateCode(prompt: String, language: String, isDebugging: Boolean = false) {
        val apiKey = settingsManager.getApiKey()
        if (apiKey.isBlank()) {
            _generatedCode.value = "// Error: Gemini API Key missing. Please set your API key in Settings."
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            _generatedCode.value = if (isDebugging) "// NISU AI is debugging $language code..." else "// NISU AI is generating $language code..."
            val codingPrompt = if (isDebugging) {
                """
                You are NISU AI Coding Studio.
                Inspect, debug, and fix the following $language code:
                $prompt
                Provide fixed executable code and explain the root cause and solution.
                """.trimIndent()
            } else {
                """
                You are NISU AI Coding Studio.
                Write production-grade, bug-free, clean $language code for:
                $prompt
                Return ONLY the raw executable code without markdown wrappers if possible, or markdown code block.
                """.trimIndent()
            }

            val result = geminiService.sendMessage(
                apiKey = apiKey,
                model = settingsManager.getSelectedModel(),
                userPrompt = codingPrompt,
                recentHistory = emptyList(),
                memories = emptyList(),
                persona = "Coding & Tech Mentor",
                language = "English"
            )

            result.fold(
                onSuccess = { code ->
                    _generatedCode.value = code
                },
                onFailure = { err ->
                    _generatedCode.value = "// Generation error: ${err.message}"
                }
            )
            _isGenerating.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
        screenCaptureHelper.stopCapture()
    }
}
