package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SettingsManager
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.FuturisticHudCard
import com.example.ui.theme.BackgroundObsidian
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDarkNavy
import com.example.ui.theme.YesminCyan
import com.example.ui.theme.YesminPink
import com.example.ui.theme.YesminPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.HOME)
    }

    val context = LocalContext.current
    val settings = viewModel.settingsManager

    // Voice Pack & Tuning
    var selectedVoicePack by remember { mutableStateOf(settings.getVoicePack()) }
    var selectedVoiceProvider by remember { mutableStateOf(settings.getVoiceProvider()) }
    var voiceSpeed by remember { mutableFloatStateOf(settings.getVoiceSpeed()) }
    var voicePitch by remember { mutableFloatStateOf(settings.getVoicePitch()) }
    var voiceVolume by remember { mutableFloatStateOf(settings.getVoiceVolume()) }
    var selectedLanguage by remember { mutableStateOf(settings.getLanguage()) }
    var languageDropdownExpanded by remember { mutableStateOf(false) }

    // Gemini states
    var apiKeyInput by remember { mutableStateOf(settings.getApiKey()) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(settings.getSelectedModel()) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    // ElevenLabs states
    var elevenKeyInput by remember { mutableStateOf(settings.getElevenLabsApiKey()) }
    var isElevenKeyVisible by remember { mutableStateOf(false) }
    var elevenVoiceId by remember { mutableStateOf(settings.getElevenLabsVoiceId()) }
    var elevenModelId by remember { mutableStateOf(settings.getElevenLabsModelId()) }
    var elevenDropdownExpanded by remember { mutableStateOf(false) }

    // General
    var isAutoTtsOn by remember { mutableStateOf(settings.isAutoTtsEnabled()) }
    var isSoundEffectsOn by remember { mutableStateOf(settings.isSoundEffectsEnabled()) }
    var isVibrationOn by remember { mutableStateOf(settings.isVibrationEnabled()) }

    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isTesting by viewModel.isTestingConnection.collectAsState()
    val voiceTestStatus by viewModel.voiceTestStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NISU AI Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = YesminCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDarkNavy
                )
            )
        },
        containerColor = BackgroundObsidian
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ==========================================
            // 1. BEAUTIFUL FEMALE VOICE SETTINGS
            // ==========================================
            item {
                FuturisticHudCard(
                    title = "1. Beautiful Female Voice",
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    glowColor = YesminPink
                ) {
                    Text(
                        text = "Soft, sweet, warm, gentle, expressive female voice with clear Hindi & Hinglish pronunciation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Voice Pack Selector
                    Text(
                        text = "Voice Pack",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = YesminPink
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SettingsManager.AVAILABLE_VOICE_PACKS.forEach { pack ->
                            val isSelected = selectedVoicePack == pack
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedVoicePack = pack
                                        settings.setVoicePack(pack)
                                        voicePitch = settings.getVoicePitch()
                                        voiceSpeed = settings.getVoiceSpeed()
                                    },
                                color = if (isSelected) YesminPink.copy(alpha = 0.15f) else SurfaceDarkNavy,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) YesminPink else Color(0x22FFFFFF)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = pack,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) YesminPink else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = when (pack) {
                                                SettingsManager.PACK_ROMANTIC_SWEET -> "Sweet, loving, gentle melodic tone"
                                                SettingsManager.PACK_GENTLE_SOFT -> "Calm, soft, comforting whisper"
                                                SettingsManager.PACK_WARM_CONVERSATIONAL -> "Natural, friendly, engaging"
                                                else -> "Expressive, melodic & playful"
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = YesminPink,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sliders: Speech Rate, Pitch, Volume
                    Text(
                        text = "Speech Rate (Pacing): ${"%.2f".format(voiceSpeed)}x",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = voiceSpeed,
                        onValueChange = {
                            voiceSpeed = it
                            settings.setVoiceSpeed(it)
                        },
                        valueRange = 0.7f..1.4f,
                        colors = SliderDefaults.colors(thumbColor = YesminPink, activeTrackColor = YesminPink)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Voice Pitch (Tone): ${"%.2f".format(voicePitch)}x",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = voicePitch,
                        onValueChange = {
                            voicePitch = it
                            settings.setVoicePitch(it)
                        },
                        valueRange = 0.8f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = YesminPurple, activeTrackColor = YesminPurple)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Voice Volume: ${(voiceVolume * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = voiceVolume,
                        onValueChange = {
                            voiceVolume = it
                            settings.setVoiceVolume(it)
                        },
                        valueRange = 0.2f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = YesminCyan, activeTrackColor = YesminCyan)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preview Button
                    Button(
                        onClick = {
                            viewModel.testVoicePreview(
                                "Hello! I am NISU, your personal assistant. Aapka din kaisa raha? Main hamesha aapke saath hoon!"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preview_voice_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YesminPink)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Preview Sweet Female Voice", fontWeight = FontWeight.Bold)
                    }

                    voiceTestStatus?.let { status ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodySmall,
                            color = YesminPink
                        )
                    }
                }
            }

            // ==========================================
            // 2. AI BRAIN CONFIGURATION (Gemini Engine)
            // ==========================================
            item {
                FuturisticHudCard(
                    title = "2. AI Brain Configuration",
                    icon = Icons.Default.Key,
                    glowColor = YesminCyan
                ) {
                    Text(
                        text = "Conversational reasoning & screen understanding powered by Google Gemini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Gemini API Key") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_api_key_input"),
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility",
                                    tint = YesminCyan
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YesminCyan,
                            unfocusedBorderColor = SurfaceBorder
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Model Selection Dropdown
                    Text(
                        text = "Selected Gemini Model:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { modelDropdownExpanded = true },
                            color = SurfaceDarkNavy,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedModel,
                                    color = YesminCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text("Change ▼", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }

                        DropdownMenu(
                            expanded = modelDropdownExpanded,
                            onDismissRequest = { modelDropdownExpanded = false }
                        ) {
                            SettingsManager.AVAILABLE_MODELS.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model) },
                                    onClick = {
                                        selectedModel = model
                                        settings.setSelectedModel(model)
                                        modelDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                settings.saveApiKey(apiKeyInput)
                                Toast.makeText(context, "API Key Saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = YesminCyan)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFF001F28), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Key", color = Color(0xFF001F28), fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.testGeminiConnection() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isTesting
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Ping")
                            }
                        }
                    }

                    connectionStatus?.let { status ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (status.contains("Error") || status.contains("failed")) Color(0xFFFF5252) else Color(0xFF00E676)
                        )
                    }
                }
            }

            // ==========================================
            // 3. ELEVENLABS HIGH-QUALITY VOICE (OPTIONAL)
            // ==========================================
            item {
                FuturisticHudCard(
                    title = "3. ElevenLabs TTS Studio (Optional)",
                    icon = Icons.Default.Tune,
                    glowColor = YesminPurple
                ) {
                    Text(
                        text = "For studio-grade ultra-realistic human voices. If unconfigured, NISU AI uses high-quality local female voice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = elevenKeyInput,
                        onValueChange = { elevenKeyInput = it },
                        label = { Text("ElevenLabs API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (isElevenKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isElevenKeyVisible = !isElevenKeyVisible }) {
                                Icon(
                                    imageVector = if (isElevenKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility",
                                    tint = YesminPurple
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YesminPurple,
                            unfocusedBorderColor = SurfaceBorder
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Female Voices
                    Text(
                        text = "Popular Female Voices:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    SettingsManager.POPULAR_ELEVENLABS_VOICES.forEach { (id, label) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    elevenVoiceId = id
                                    settings.setElevenLabsVoiceId(id)
                                    settings.setVoiceProvider(SettingsManager.PROVIDER_ELEVENLABS)
                                },
                            color = if (elevenVoiceId == id) YesminPurple.copy(alpha = 0.15f) else SurfaceDarkNavy,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (elevenVoiceId == id) YesminPurple else Color(0x22FFFFFF)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = if (elevenVoiceId == id) YesminPurple else MaterialTheme.colorScheme.onSurface
                                )
                                if (elevenVoiceId == id) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = YesminPurple, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            settings.saveElevenLabsApiKey(elevenKeyInput)
                            settings.setElevenLabsVoiceId(elevenVoiceId)
                            settings.setVoiceProvider(SettingsManager.PROVIDER_ELEVENLABS)
                            Toast.makeText(context, "ElevenLabs Settings Saved!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YesminPurple)
                    ) {
                        Text("Save & Enable ElevenLabs", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ==========================================
            // 4. GENERAL PREFERENCES & LANGUAGE
            // ==========================================
            item {
                FuturisticHudCard(
                    title = "4. General & Language Preferences",
                    icon = Icons.Default.Language,
                    glowColor = YesminCyan
                ) {
                    // Language Dropdown
                    Text(
                        text = "Preferred Language (Voice & AI):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { languageDropdownExpanded = true },
                            color = SurfaceDarkNavy,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedLanguage,
                                    color = YesminCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text("Change ▼", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }

                        DropdownMenu(
                            expanded = languageDropdownExpanded,
                            onDismissRequest = { languageDropdownExpanded = false }
                        ) {
                            SettingsManager.AVAILABLE_LANGUAGES.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang) },
                                    onClick = {
                                        selectedLanguage = lang
                                        settings.setLanguage(lang)
                                        languageDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto TTS Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Speak Responses",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Read AI answers aloud using sweet female voice",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAutoTtsOn,
                            onCheckedChange = {
                                isAutoTtsOn = it
                                settings.setAutoTtsEnabled(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = YesminCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sound Effects Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sound Effects",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Play audio chime on recognition and completed actions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isSoundEffectsOn,
                            onCheckedChange = {
                                isSoundEffectsOn = it
                                settings.setSoundEffectsEnabled(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = YesminCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vibration Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Haptic Vibration",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Vibrate when mic listening starts and stops",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isVibrationOn,
                            onCheckedChange = {
                                isVibrationOn = it
                                settings.setVibrationEnabled(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = YesminCyan)
                        )
                    }
                }
            }

            // ==========================================
            // 5. ABOUT NISU AI
            // ==========================================
            item {
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FF4081))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = YesminPink, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "About NISU AI v2.0",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = YesminPink
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "NISU AI is a production-grade female voice assistant designed for seamless Android control, natural romantic-style conversational voice, real-time screen understanding, and RGB edge lighting.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Crafted with Jetpack Compose & Google Gemini",
                            style = MaterialTheme.typography.labelSmall,
                            color = YesminCyan
                        )
                    }
                }
            }
        }
    }
}
