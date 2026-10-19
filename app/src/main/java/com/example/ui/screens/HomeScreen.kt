package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.VoiceCommandStatus
import com.example.ui.components.FuturisticHudCard
import com.example.ui.components.VoicePulseVisualizer
import com.example.ui.theme.BackgroundObsidian
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDarkNavy
import com.example.ui.theme.YesminCyan
import com.example.ui.theme.YesminPink
import com.example.ui.theme.YesminPurple
import com.example.voice.VoiceCallState

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val apiKey by viewModel.settingsManager.apiKeyFlow.collectAsState()
    val commandStatus by viewModel.commandStatus.collectAsState()
    val currentActionText by viewModel.currentActionText.collectAsState()
    val recentActivities by viewModel.recentActivity.collectAsState()
    val pendingConfirmation by viewModel.pendingConfirmationMessage.collectAsState()
    val voiceState by viewModel.voiceManager.voiceState.collectAsState()
    val audioRms by viewModel.voiceManager.rmsLevel.collectAsState()

    var showTermuxDialog by remember { mutableStateOf(false) }

    if (showTermuxDialog) {
        TermuxGuideDialog(onDismiss = { showTermuxDialog = false })
    }

    val isListening = commandStatus == VoiceCommandStatus.LISTENING || voiceState == VoiceCallState.LISTENING

    val infiniteTransition = rememberInfiniteTransition(label = "mic_glow")
    val micPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.25f else 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 700 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundObsidian),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Header with NISU AI Branding & Status Badge ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NISU AI",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = YesminPink
                    )
                    Text(
                        text = "Next-Gen Female Voice Assistant",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge ("Ready" / "Listening" / "Processing" / "Speaking")
                val (badgeBg, badgeText, badgeColor) = when (commandStatus) {
                    VoiceCommandStatus.LISTENING -> Triple(Color(0x3300E5FF), "LISTENING", YesminCyan)
                    VoiceCommandStatus.PROCESSING -> Triple(Color(0x33B388FF), "PROCESSING", YesminPurple)
                    VoiceCommandStatus.EXECUTING -> Triple(Color(0x3300E676), "EXECUTING", Color(0xFF00E676))
                    VoiceCommandStatus.SPEAKING -> Triple(Color(0x33FF4081), "SPEAKING", YesminPink)
                    VoiceCommandStatus.COMPLETED -> Triple(Color(0x3300E676), "COMPLETED", Color(0xFF00E676))
                    VoiceCommandStatus.READY -> Triple(Color(0x2200E5FF), "READY", YesminCyan)
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(badgeColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = badgeColor
                        )
                    }
                }
            }
        }

        // --- 2. Central Voice Assistant Card with Waveform & Glowing Orb ---
        item {
            FuturisticHudCard(
                glowColor = when (commandStatus) {
                    VoiceCommandStatus.SPEAKING -> YesminPink
                    VoiceCommandStatus.LISTENING -> YesminCyan
                    VoiceCommandStatus.PROCESSING -> YesminPurple
                    else -> YesminPink
                },
                modifier = Modifier.testTag("voice_assistant_card")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        VoicePulseVisualizer(
                            size = 150.dp,
                            state = when (commandStatus) {
                                VoiceCommandStatus.LISTENING -> VoiceCallState.LISTENING
                                VoiceCommandStatus.PROCESSING, VoiceCommandStatus.EXECUTING -> VoiceCallState.THINKING
                                VoiceCommandStatus.SPEAKING -> VoiceCallState.SPEAKING
                                else -> VoiceCallState.IDLE
                            },
                            rmsLevel = audioRms
                        )

                        // Central Glowing Orb
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(YesminPink, YesminPurple, Color(0xFF3B0764))
                                    )
                                )
                                .border(2.dp, YesminPink.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (commandStatus == VoiceCommandStatus.SPEAKING) Icons.Default.VolumeUp
                                else if (isListening) Icons.Default.Mic
                                else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentActionText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Voice Pack: ${viewModel.settingsManager.getVoicePack()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = YesminPink
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Prominent Microphone Action Button with Glow / Ripple
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer pulse ring
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .scale(micPulse)
                                .clip(CircleShape)
                                .background(
                                    if (isListening) YesminCyan.copy(alpha = 0.25f)
                                    else YesminPink.copy(alpha = 0.15f)
                                )
                        )

                        // Main Circle Button
                        Button(
                            onClick = {
                                if (isListening) {
                                    viewModel.stopListeningInstant()
                                } else {
                                    viewModel.startListeningInstant()
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .testTag("home_mic_button"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListening) YesminCyan else YesminPink
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = if (isListening) Color(0xFF001F28) else Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isListening) "Tap to finish speaking" else "Tap microphone to speak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- 3. Sensitive Action Confirmation Banner (e.g. WhatsApp Message) ---
        pendingConfirmation?.let { message ->
            item {
                Surface(
                    color = SurfaceDarkNavy,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, YesminPink)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = YesminPink,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Action Confirmation Required",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = YesminPink
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.confirmPendingAction() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF001F28), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Confirm & Send", color = Color(0xFF001F28), fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.cancelPendingAction() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }

        // --- 4. Quick Action Cards ---
        item {
            Text(
                text = "QUICK ACTIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard(
                    title = "Voice Chat",
                    subtitle = "Conversational AI",
                    icon = Icons.Default.ChatBubble,
                    color = YesminCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.CHAT) }
                )
                QuickCard(
                    title = "Screen Share",
                    subtitle = "Visual Understanding",
                    icon = Icons.Default.ScreenShare,
                    color = YesminPink,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.SCREEN_SHARE) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard(
                    title = "Edge Glow",
                    subtitle = "RGB Screen Border",
                    icon = Icons.Default.ColorLens,
                    color = YesminPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.EDGE_GLOW) }
                )
                QuickCard(
                    title = "Accessibility",
                    subtitle = "Phone Automation",
                    icon = Icons.Default.AccessibilityNew,
                    color = Color(0xFF00E676),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.ACCESSIBILITY) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard(
                    title = "Settings",
                    subtitle = "Voice & AI Config",
                    icon = Icons.Default.Settings,
                    color = YesminCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.SETTINGS) }
                )
                QuickCard(
                    title = "Voice Call",
                    subtitle = "Real-time hands-free",
                    icon = Icons.Default.Call,
                    color = YesminPink,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.navigateTo(Screen.CALLING)
                        viewModel.startCalling()
                    }
                )
            }
        }

        // --- 5. Recent Activity / Command History Preview ---
        item {
            FuturisticHudCard(
                title = "Recent Command Activity",
                icon = Icons.Default.AutoAwesome,
                glowColor = YesminCyan
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentActivities.take(4).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceDarkNavy, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (item.isSuccess) Color(0xFF00E676) else Color(0xFFFF5252),
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                            Text(
                                text = item.timeAgo,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun QuickCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(105.dp)
            .clickable { onClick() },
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
