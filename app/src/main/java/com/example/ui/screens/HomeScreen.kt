package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.FuturisticHudCard
import com.example.ui.components.YesminAvatar
import com.example.ui.theme.BackgroundObsidian
import com.example.ui.theme.SurfaceCard
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
    val persona = viewModel.settingsManager.getPersona()
    val model = viewModel.settingsManager.getSelectedModel()
    val memories by viewModel.allMemories.collectAsState()
    var showTermuxDialog by remember { mutableStateOf(false) }

    if (showTermuxDialog) {
        TermuxGuideDialog(onDismiss = { showTermuxDialog = false })
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundObsidian),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. JARVIS Futuristic HUD Bar ---
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
                        text = "OG SAHID AI ONLINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = YesminCyan
                    )
                    Text(
                        text = "AI Companion + Assistant",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Status chip
                Surface(
                    color = if (apiKey.isNotBlank()) Color(0x2200E5FF) else Color(0x22FFAB00),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (apiKey.isNotBlank()) YesminCyan else Color(0xFFFFAB00),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (apiKey.isNotBlank()) "ACTIVE" else "SETUP KEY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (apiKey.isNotBlank()) YesminCyan else Color(0xFFFFAB00)
                        )
                    }
                }
            }
        }

        // --- 2. Central Avatar & Greeting ---
        item {
            FuturisticHudCard(
                glowColor = YesminPurple,
                modifier = Modifier.testTag("avatar_card")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    YesminAvatar(
                        size = 150.dp,
                        state = VoiceCallState.IDLE
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "\"Aapka din kaisa raha? Main hamesha aapke saath hoon!\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = YesminPink,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Persona: $persona • Memory Bank: ${memories.size} facts remembered",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dedicated Call Button
                    Button(
                        onClick = {
                            viewModel.navigateTo(Screen.CALLING)
                            viewModel.startCalling()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_voice_call_button"),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(YesminCyan, YesminPurple, YesminPink)
                                    ),
                                    RoundedCornerShape(26.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = Color(0xFF001F28),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Start Real-Time Voice Call",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF001F28)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Grid of Main Modules ---
        item {
            Text(
                text = "SYSTEM MODULES",
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
                ModuleQuickCard(
                    title = "AI Chat",
                    subtitle = "Hinglish, Hindi, English",
                    icon = Icons.Default.ChatBubble,
                    color = YesminCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.CHAT) }
                )
                ModuleQuickCard(
                    title = "Coding Assistant",
                    subtitle = "Code & Termux builds",
                    icon = Icons.Default.Code,
                    color = YesminPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.CODING) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModuleQuickCard(
                    title = "Phone Automation",
                    subtitle = "Apps, Alarms, YouTube",
                    icon = Icons.Default.PhoneAndroid,
                    color = YesminPink,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.AUTOMATION) }
                )
                ModuleQuickCard(
                    title = "Memory Vault",
                    subtitle = "${memories.size} Saved facts (Room DB)",
                    icon = Icons.Default.Memory,
                    color = YesminCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.MEMORY) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModuleQuickCard(
                    title = "Tools & Vision",
                    subtitle = "Camera, OCR, Translate",
                    icon = Icons.Default.AutoAwesome,
                    color = YesminPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(Screen.TOOLS) }
                )
                ModuleQuickCard(
                    title = "Termux Guide",
                    subtitle = "Build APK on phone",
                    icon = Icons.Default.Terminal,
                    color = YesminPink,
                    modifier = Modifier.weight(1f),
                    onClick = { showTermuxDialog = true }
                )
            }
        }

        // --- 4. Gemini Diagnostics Banner ---
        item {
            FuturisticHudCard(
                title = "AI Engine Status",
                icon = Icons.Default.Engineering,
                glowColor = YesminCyan
            ) {
                Text(
                    text = "Active Brain: $model",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (apiKey.isNotBlank()) "API key configured securely in private storage."
                           else "No Gemini API key set. Open Settings to configure.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Configure in Settings →",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = YesminCyan,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.SETTINGS) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun ModuleQuickCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(115.dp)
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
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
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
