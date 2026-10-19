package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.YesminCyan
import com.example.ui.theme.YesminPink
import com.example.ui.theme.YesminPurple
import com.example.voice.VoiceCallState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VoicePulseVisualizer(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    state: VoiceCallState,
    rmsLevel: Float
) {
    val transition = rememberInfiniteTransition(label = "pulse_rings")

    val pulseScale1 by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )

    val pulseScale2 by transition.animateFloat(
        initialValue = 0.70f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )

    val color = when (state) {
        VoiceCallState.LISTENING -> YesminCyan
        VoiceCallState.THINKING -> YesminPurple
        VoiceCallState.SPEAKING -> YesminPink
        VoiceCallState.MUTED -> Color(0xFFFF5252)
        VoiceCallState.IDLE -> YesminCyan.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseR = (this.size.minDimension / 2f) * 0.55f

            if (state == VoiceCallState.LISTENING || state == VoiceCallState.SPEAKING) {
                // Expanding wave 1
                val r1 = baseR * (pulseScale1 + rmsLevel * 0.3f)
                val alpha1 = ((1.25f - pulseScale1) / 0.4f).coerceIn(0f, 0.8f)
                drawCircle(
                    color = color.copy(alpha = alpha1 * 0.4f),
                    radius = r1,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Expanding wave 2
                val r2 = baseR * (pulseScale2 + rmsLevel * 0.2f)
                val alpha2 = ((1.10f - pulseScale2) / 0.4f).coerceIn(0f, 0.9f)
                drawCircle(
                    color = color.copy(alpha = alpha2 * 0.6f),
                    radius = r2,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // Radial soundwave ticks around the circle
            val tickCount = 36
            val stepAngle = 360f / tickCount
            for (i in 0 until tickCount) {
                val angleDeg = i * stepAngle
                val angleRad = Math.toRadians(angleDeg.toDouble())

                // Modulate tick height based on audio level & index
                val waveHeight = if (state == VoiceCallState.LISTENING || state == VoiceCallState.SPEAKING) {
                    val factor = ((sin(i * 1.5) + 1f) / 2f).toFloat()
                    8.dp.toPx() + (factor * rmsLevel * 30.dp.toPx())
                } else if (state == VoiceCallState.THINKING) {
                    val factor = ((sin(i * 0.8 + System.currentTimeMillis() * 0.005) + 1f) / 2f).toFloat()
                    6.dp.toPx() + (factor * 14.dp.toPx())
                } else {
                    4.dp.toPx()
                }

                val startR = baseR + 4.dp.toPx()
                val endR = startR + waveHeight

                val startX = center.x + (startR * cos(angleRad)).toFloat()
                val startY = center.y + (startR * sin(angleRad)).toFloat()

                val endX = center.x + (endR * cos(angleRad)).toFloat()
                val endY = center.y + (endR * sin(angleRad)).toFloat()

                drawLine(
                    color = color.copy(alpha = 0.85f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
