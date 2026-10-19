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
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.accessibility.OgSahidAccessibilityService
import com.example.data.local.SettingsManager
import com.example.overlay.RgbBorderOverlayService
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

    // Gemini states
    var apiKeyInput by remember { mutableStateOf(settings.getApiKey()) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(settings.getSelectedModel()) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    // Voice Studio states
    var selectedVoiceProvider by remember { mutableStateOf(settings.getVoiceProvider()) }
    var elevenKeyInput by remember { mutableStateOf(settings.getElevenLabsApiKey()) }
    var isElevenKeyVisible by remember { mutableStateOf(false) }
    var elevenVoiceId by remember { mutableStateOf(settings.getElevenLabsVoiceId()) }
    var elevenModelId by remember { mutableStateOf(settings.getElevenLabsModelId()) }
    var elevenStability by remember { mutableFloatStateOf(settings.getElevenLabsStability()) }
    var elevenSimilarity by remember { mutableFloatStateOf(settings.getElevenLabsSimilarity()) }
    var voiceSpeed by remember { mutableFloatStateOf(settings.getVoiceSpeed()) }
    val voiceTestStatus by viewModel.voiceTestStatus.collectAsState()

    // Accessibility states
    var isAccessibilityEnabled by remember { mutableStateOf(viewModel.isAccessibilityServiceActive()) }

    // Overlay RGB states
    var isOverlayGranted by remember { mutableStateOf(viewModel.isOverlayPermissionActive()) }
    var isRgbBorderOn by remember { mutableStateOf(settings.isRgbBorderEnabled()) }
    var rgbWidth by remember { mutableFloatStateOf(settings.getRgbBorderWidth()) }
    var rgbBrightness by remember { mutableFloatStateOf(settings.getRgbBorderBrightness()) }
    var rgbSpeed by remember { mutableFloatStateOf(settings.getRgbBorderSpeed()) }

    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isTesting by viewModel.isTestingConnection.collectAsState()

    // Refresh permissions on resume / active
    LaunchedEffect(Unit) {
        isAccessibilityEnabled = viewModel.isAccessibilityServiceActive()
        isOverlayGranted = viewModel.isOverlayPermissionActive()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "OG SAHID AI Settings",
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
            // SECTION A: AI CONFIGURATION (Gemini Engine)
            // ==========================================
            item {
                FuturisticHudCard(
                    title = "A. AI Brain Configuration",
                    icon = Icons.Default.Key,
                    glowColor = YesminCyan
                ) {
                    Text(
                        text = "Primary AI intelligence powered by Google Gemini models.",
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
                            .testTag("api_key_input_field"),
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = YesminCyan
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YesminCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedContainerColor = Color(0xFF0F1524),
                            unfocusedContainerColor = Color(0xFF0F1524)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                settings.saveApiKey(apiKeyInput)
                                Toast.makeText(context, "Gemini API Key saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = YesminCyan, contentColor = Color(0xFF001F28))
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Key")
                        }

                        Button(
                            onClick = {
                                settings.saveApiKey(apiKeyInput)
                                viewModel.testApiConnection()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = YesminCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, YesminCyan.copy(alpha = 0.4f)),
                            enabled = !isTesting
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = YesminCyan)
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Connection")
                            }
                        }
                    }

                    if (connectionStatus != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFF0A101C),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = connectionStatus ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (connectionStatus!!.startsWith("Connection successful")) YesminCyan else Color(0xFFFFAB00),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Model selection
                    Text(text = "Active Gemini Model:", style = MaterialTheme.typography.labelSmall, color = YesminCyan)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            color = Color(0xFF0F1524),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { modelDropdownExpanded = true }
                                .padding(10.dp)
                        ) {
                            Text(text = selectedModel, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
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
                }
            }

            // ==========================================
            // SECTION B: VOICE STUDIO (Natural Voice Engine)
            // ==========================================
            item {
                FuturisticHudCard(
                    title = "B. Voice Studio (Natural Voice)",
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    glowColor = YesminPink
                ) {
                    Text(
                        text = "Prioritized natural, expressive female voice synthesis with realistic pauses and authentic Hindi/Hinglish/English pacing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Provider Tabs
                    Text(text = "Voice Engine Provider:", style = MaterialTheme.typography.labelSmall, color = YesminPink)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SettingsManager.AVAILABLE_VOICE_PROVIDERS.forEach { provider ->
                            FilterChip(
                                selected = selectedVoiceProvider == provider,
                                onClick = {
                                    selectedVoiceProvider = provider
                                    settings.setVoiceProvider(provider)
                                },
                                label = { Text(provider, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = YesminPink,
                                    selectedLabelColor = Color.Black,
                                    containerColor = SurfaceCard,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ElevenLabs Configuration
                    if (selectedVoiceProvider == SettingsManager.PROVIDER_ELEVENLABS) {
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
                                        contentDescription = "Toggle",
                                        tint = YesminPink
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YesminPink,
                                focusedContainerColor = Color(0xFF0F1524),
                                unfocusedContainerColor = Color(0xFF0F1524)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = elevenVoiceId,
                            onValueChange = { elevenVoiceId = it },
                            label = { Text("Voice ID (e.g. 21m00Tcm4TlvDq8ikWAM for Rachel)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YesminPink,
                                focusedContainerColor = Color(0xFF0F1524),
                                unfocusedContainerColor = Color(0xFF0F1524)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = elevenModelId,
                            onValueChange = { elevenModelId = it },
                            label = { Text("Model ID (eleven_multilingual_v2)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = YesminPink,
                                focusedContainerColor = Color(0xFF0F1524),
                                unfocusedContainerColor = Color(0xFF0F1524)
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Voice Sliders
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Voice Stability:", style = MaterialTheme.typography.bodySmall)
                            Text(text = "%.2f".format(elevenStability), color = YesminPink, style = MaterialTheme.typography.bodySmall)
                        }
                        Slider(
                            value = elevenStability,
                            onValueChange = {
                                elevenStability = it
                                settings.setElevenLabsStability(it)
                            },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = YesminPink, activeTrackColor = YesminPink)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Similarity Boost:", style = MaterialTheme.typography.bodySmall)
                            Text(text = "%.2f".format(elevenSimilarity), color = YesminPink, style = MaterialTheme.typography.bodySmall)
                        }
                        Slider(
                            value = elevenSimilarity,
                            onValueChange = {
                                elevenSimilarity = it
                                settings.setElevenLabsSimilarity(it)
                            },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = YesminPink, activeTrackColor = YesminPink)
                        )
                    }

                    // Save and Preview Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                settings.saveElevenLabsApiKey(elevenKeyInput)
                                settings.setElevenLabsVoiceId(elevenVoiceId)
                                settings.setElevenLabsModelId(elevenModelId)
                                Toast.makeText(context, "Voice settings saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = YesminPink, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Voice", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                settings.saveElevenLabsApiKey(elevenKeyInput)
                                settings.setElevenLabsVoiceId(elevenVoiceId)
                                settings.setElevenLabsModelId(elevenModelId)
                                viewModel.testVoicePreview("Namaste! Main OG SAHID AI hoon. Aapki natural voice assistant.")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = YesminPink),
                            border = androidx.compose.foundation.BorderStroke(1.dp, YesminPink.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Voice Preview", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (voiceTestStatus != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = voiceTestStatus ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = YesminPink
                        )
                    }
                }
            }

            // ==========================================
            // SECTION C: ACCESSIBILITY & PHONE CONTROL
            // ==========================================
            item {
                FuturisticHudCard(
                    title = "C. Accessibility & Phone Control",
                    icon = Icons.Default.AccessibilityNew,
                    glowColor = YesminPurple
                ) {
                    Text(
                        text = "Allows OG SAHID AI to interact with other apps (open WhatsApp chats, scroll, tap buttons, navigate back/home).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Permission Status Badge
                    Surface(
                        color = if (isAccessibilityEnabled) Color(0x2200E676) else Color(0x22FF5252),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAccessibilityEnabled) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isAccessibilityEnabled) Color(0xFF00E676) else Color(0xFFFF5252),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isAccessibilityEnabled) "Accessibility Service: ENABLED" else "Accessibility Service: DISABLED",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAccessibilityEnabled) Color(0xFF00E676) else Color(0xFFFF5252),
                                    fontSize = 13.sp
                                )
                            }
                            IconButton(onClick = {
                                isAccessibilityEnabled = viewModel.isAccessibilityServiceActive()
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = YesminPurple)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                OgSahidAccessibilityService.openAccessibilitySettings(context)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = YesminPurple, contentColor = Color.Black)
                        ) {
                            Text("Enable Service", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val res = viewModel.checkAndRunAutomationCommand("go back")
                                Toast.makeText(context, res?.message ?: "Testing Back action", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = YesminPurple),
                            border = androidx.compose.foundation.BorderStroke(1.dp, YesminPurple.copy(alpha = 0.4f)),
                            enabled = isAccessibilityEnabled
                        ) {
                            Text("Test Action (Back)")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Troubleshooting: In Android Accessibility settings, scroll to 'Downloaded apps', tap 'OG SAHID AI Phone Assistant', and turn it ON.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ==========================================
            // SECTION D: OVERLAY & RAINBOW RGB EFFECTS
            // ==========================================
            item {
                FuturisticHudCard(
                    title = "D. Floating Overlay & RGB Border",
                    icon = Icons.Default.ColorLens,
                    glowColor = Color(0xFF00FF66)
                ) {
                    Text(
                        text = "Thin animated neon rainbow gradient border around all 4 screen edges that stays visible over other apps. Never blocks touches.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Overlay permission badge
                    Surface(
                        color = if (isOverlayGranted) Color(0x2200E676) else Color(0x22FFAB00),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isOverlayGranted) "Overlay Permission: GRANTED" else "Overlay Permission: MISSING",
                                fontWeight = FontWeight.Bold,
                                color = if (isOverlayGranted) Color(0xFF00E676) else Color(0xFFFFAB00),
                                fontSize = 12.sp
                            )
                            if (!isOverlayGranted) {
                                Button(
                                    onClick = {
                                        RgbBorderOverlayService.openOverlaySettings(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFAB00), contentColor = Color.Black),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // RGB Border Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Rainbow RGB Edge Border", fontWeight = FontWeight.Bold)
                            Text(text = "Display glowing rainbow aura over apps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isRgbBorderOn,
                            onCheckedChange = { newState ->
                                isRgbBorderOn = viewModel.toggleRgbBorder(newState)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FF66), checkedTrackColor = Color(0x6600FF66))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Width Slider
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Border Width:", style = MaterialTheme.typography.bodySmall)
                        Text(text = "%.1f dp".format(rgbWidth), style = MaterialTheme.typography.bodySmall, color = Color(0xFF00FF66))
                    }
                    Slider(
                        value = rgbWidth,
                        onValueChange = {
                            rgbWidth = it
                            settings.setRgbBorderWidth(it)
                            if (isRgbBorderOn) RgbBorderOverlayService.start(context)
                        },
                        valueRange = 1.5f..8.0f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF00FF66), activeTrackColor = Color(0xFF00FF66))
                    )

                    // Brightness Slider
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Glow Brightness:", style = MaterialTheme.typography.bodySmall)
                        Text(text = "%.0f%%".format(rgbBrightness * 100), style = MaterialTheme.typography.bodySmall, color = Color(0xFF00FF66))
                    }
                    Slider(
                        value = rgbBrightness,
                        onValueChange = {
                            rgbBrightness = it
                            settings.setRgbBorderBrightness(it)
                            if (isRgbBorderOn) RgbBorderOverlayService.start(context)
                        },
                        valueRange = 0.2f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF00FF66), activeTrackColor = Color(0xFF00FF66))
                    )

                    // Animation Speed Slider
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Animation Speed:", style = MaterialTheme.typography.bodySmall)
                        Text(text = "%.1fx".format(rgbSpeed), style = MaterialTheme.typography.bodySmall, color = Color(0xFF00FF66))
                    }
                    Slider(
                        value = rgbSpeed,
                        onValueChange = {
                            rgbSpeed = it
                            settings.setRgbBorderSpeed(it)
                            if (isRgbBorderOn) RgbBorderOverlayService.start(context)
                        },
                        valueRange = 0.5f..3.0f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF00FF66), activeTrackColor = Color(0xFF00FF66))
                    )
                }
            }

            // ==========================================
            // SECTION E: PERMISSIONS & DIAGNOSTICS
            // ==========================================
            item {
                FuturisticHudCard(
                    title = "E. System Diagnostics & Hardware",
                    icon = Icons.Default.Security,
                    glowColor = YesminCyan
                ) {
                    Text(
                        text = "Real-time verification of phone integration status.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DiagnosticRow("Gemini API Brain", if (settings.getApiKey().isNotBlank()) "Configured" else "Missing Key", settings.getApiKey().isNotBlank())
                    DiagnosticRow("Voice Studio Provider", selectedVoiceProvider, true)
                    DiagnosticRow("Accessibility Service", if (isAccessibilityEnabled) "Running" else "Disabled", isAccessibilityEnabled)
                    DiagnosticRow("Overlay (Draw over apps)", if (isOverlayGranted) "Authorized" else "Denied", isOverlayGranted)

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.navigateTo(Screen.PERMISSIONS)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard, contentColor = YesminCyan),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, YesminCyan.copy(alpha = 0.4f))
                    ) {
                        Text("Open Full Permissions Guard")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@Composable
fun DiagnosticRow(title: String, status: String, ok: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (ok) Color(0xFF00E676) else Color(0xFFFF5252), RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = if (ok) Color(0xFF00E676) else Color(0xFFFF5252)
            )
        }
    }
}
