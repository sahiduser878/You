package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.VoicePulseVisualizer
import com.example.ui.components.YesminAvatar
import com.example.ui.theme.BackgroundObsidian
import com.example.ui.theme.YesminCyan
import com.example.ui.theme.YesminPink
import com.example.ui.theme.YesminPurple
import com.example.voice.VoiceCallState

@Composable
fun CallScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.stopCalling()
        viewModel.navigateTo(Screen.HOME)
    }

    val callState by viewModel.voiceManager.voiceState.collectAsState()
    val rmsLevel by viewModel.voiceManager.rmsLevel.collectAsState()
    val durationSeconds by viewModel.callDurationSeconds.collectAsState()
    val subtitles by viewModel.callSubtitles.collectAsState()
    var isMuted by remember { mutableStateOf(false) }

    val stateText = when (callState) {
        VoiceCallState.LISTENING -> "Listening to you..."
        VoiceCallState.THINKING -> "Thinking..."
        VoiceCallState.SPEAKING -> "OG SAHID AI speaking..."
        VoiceCallState.MUTED -> "Microphone muted"
        VoiceCallState.IDLE -> "Connecting..."
    }

    val stateColor = when (callState) {
        VoiceCallState.LISTENING -> YesminCyan
        VoiceCallState.THINKING -> YesminPurple
        VoiceCallState.SPEAKING -> YesminPink
        VoiceCallState.MUTED -> Color(0xFFFF5252)
        VoiceCallState.IDLE -> YesminCyan
    }

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val durationFormatted = "%02d:%02d".format(minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F1524),
                        BackgroundObsidian,
                        Color(0xFF080D1A)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- 1. Call Header ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "OG SAHID AI LIVE CALL",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Black
                ),
                color = YesminCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = durationFormatted,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = stateColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(stateColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stateText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = stateColor
                    )
                }
            }
        }

        // --- 2. Central Avatar with Glowing Circular Visualizer ---
        Box(
            modifier = Modifier
                .size(260.dp)
                .testTag("voice_visualizer_box"),
            contentAlignment = Alignment.Center
        ) {
            VoicePulseVisualizer(
                size = 260.dp,
                state = callState,
                rmsLevel = rmsLevel
            )
            YesminAvatar(
                size = 140.dp,
                state = callState,
                audioLevel = rmsLevel
            )
        }

        // --- 3. Live Subtitles Box ---
        Surface(
            color = Color(0xFF141C2E),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, stateColor.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LIVE CONVERSATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = YesminCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitles,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 4
                )
            }
        }

        // --- 4. Call Controls ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute / Unmute
            IconButton(
                onClick = {
                    isMuted = viewModel.toggleCallMute()
                },
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        if (isMuted) Color(0xFFFF5252).copy(alpha = 0.2f) else Color(0xFF1E2842),
                        CircleShape
                    )
                    .testTag("call_mute_button")
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute Toggle",
                    tint = if (isMuted) Color(0xFFFF5252) else YesminCyan,
                    modifier = Modifier.size(28.dp)
                )
            }

            // End Call Button (Primary Red)
            IconButton(
                onClick = {
                    viewModel.stopCalling()
                    viewModel.navigateTo(Screen.HOME)
                },
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFFFF3B30), CircleShape)
                    .testTag("call_end_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Barge-in / Interrupt Button
            IconButton(
                onClick = {
                    viewModel.interruptCall()
                },
                modifier = Modifier
                    .size(60.dp)
                    .background(YesminPurple.copy(alpha = 0.2f), CircleShape)
                    .testTag("call_interrupt_button")
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Interrupt & Speak",
                    tint = YesminPurple,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
