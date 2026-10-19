package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.FuturisticHudCard
import com.example.ui.components.YesminAvatar
import com.example.ui.theme.BackgroundObsidian
import com.example.ui.theme.SurfaceDarkNavy
import com.example.ui.theme.YesminCyan
import com.example.ui.theme.YesminPink
import com.example.ui.theme.YesminPurple
import com.example.voice.VoiceCallState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(Screen.HOME)
    }

    var showTermuxDialog by remember { mutableStateOf(false) }

    if (showTermuxDialog) {
        TermuxGuideDialog(onDismiss = { showTermuxDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About NISU AI",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.HOME) },
                        modifier = Modifier.testTag("about_back_button")
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                YesminAvatar(size = 120.dp, state = VoiceCallState.IDLE)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "NISU AI v2.0",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Natural Female AI Companion & Android Phone Controller",
                    style = MaterialTheme.typography.bodySmall,
                    color = YesminCyan
                )
            }

            item {
                FuturisticHudCard(
                    title = "Identity & Core Philosophy",
                    icon = Icons.Default.Favorite,
                    glowColor = YesminPink
                ) {
                    Text(
                        text = "NISU AI is crafted as a premier Android AI companion & controller:\n\n" +
                               "• Natural Female Voice: Soft, sweet, warm, romantic-style voice in Hindi, Hinglish, and English with customizable Voice Packs.\n\n" +
                               "• Full Phone Control: Accessibility Service and Intent integrations to open apps, compose WhatsApp messages with confirmation, scroll, navigate back/home, and tap buttons.\n\n" +
                               "• Screen Sharing & Understanding: Visual inspection of active Android screens with multimodal AI extraction and guidance.\n\n" +
                               "• RGB Edge Glow: Animated glowing screen borders with multiple modes, palettes, speed, and thickness controls.\n\n" +
                               "• Real-Time Voice Commands: Fast event-driven speech recognition and zero-latency device action execution.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                FuturisticHudCard(
                    title = "Termux Build & Mobile Compilation",
                    icon = Icons.Default.Terminal,
                    glowColor = YesminCyan
                ) {
                    Text(
                        text = "This application is engineered to compile directly on Android phones using Termux with OpenJDK 17 and Gradle CLI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showTermuxDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = YesminCyan, contentColor = Color(0xFF001F28)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Termux APK Build Guide", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                FuturisticHudCard(
                    title = "Architecture & Technology",
                    icon = Icons.Default.Info,
                    glowColor = YesminPurple
                ) {
                    Text(
                        text = "• AI Brain: Google Gemini 3.5 Flash & 3.1 Pro via Direct REST API\n" +
                               "• Voice: Android SpeechRecognizer + TextToSpeech engine\n" +
                               "• Storage: Room SQLite Database with Repository pattern\n" +
                               "• UI Framework: Jetpack Compose with Material Design 3\n" +
                               "• Architecture: MVVM with Kotlin Coroutines & StateFlow",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
