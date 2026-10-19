package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.local.SettingsManager
import kotlin.math.sin

@Composable
fun NisuEdgeGlowBorder(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    mode: String = SettingsManager.MODE_RAINBOW_WAVE,
    palette: String = SettingsManager.PALETTE_RAINBOW,
    speed: Float = 1.0f,
    brightness: Float = 0.9f,
    strokeWidth: Dp = 4.dp,
    audioRms: Float = 0f
) {
    if (!enabled) return

    val colors = when (palette) {
        SettingsManager.PALETTE_NEON_PINK -> listOf(
            Color(0xFFFF1493), Color(0xFFFF69B4), Color(0xFFFF007F), Color(0xFFFF80BF), Color(0xFFFF1493)
        )
        SettingsManager.PALETTE_CYBERPUNK_BLUE -> listOf(
            Color(0xFF00E5FF), Color(0xFF0077FF), Color(0xFF7000FF), Color(0xFF00E5FF)
        )
        SettingsManager.PALETTE_WARM_SUNSET -> listOf(
            Color(0xFFFF512F), Color(0xFFDD2476), Color(0xFFFF8800), Color(0xFFFF512F)
        )
        SettingsManager.PALETTE_ELECTRIC_GREEN -> listOf(
            Color(0xFF00FF87), Color(0xFF60EFFF), Color(0xFF00E676), Color(0xFF00FF87)
        )
        else -> listOf(
            Color(0xFFFF0055),
            Color(0xFFFF9900),
            Color(0xFFFFFF00),
            Color(0xFF00FF66),
            Color(0xFF00F0FF),
            Color(0xFF8000FF),
            Color(0xFFFF0055)
        )
    }

    val transition = rememberInfiniteTransition(label = "edge_glow")

    val duration = (4000 / speed.coerceIn(0.5f, 3f)).toInt()
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_cycle"
    )

    val breathingProgress by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_breathe"
    )

    val effectiveAlpha = when (mode) {
        SettingsManager.MODE_BREATHING_GLOW -> (brightness * breathingProgress).coerceIn(0.1f, 1f)
        SettingsManager.MODE_PULSE -> {
            val pulse = (sin(progress * Math.PI * 4) + 1f) / 2f
            (brightness * (0.3f + 0.7f * pulse.toFloat())).coerceIn(0.1f, 1f)
        }
        SettingsManager.MODE_SOUND_REACTIVE -> {
            (brightness * (0.35f + audioRms * 0.65f)).coerceIn(0.1f, 1f)
        }
        else -> brightness.coerceIn(0.1f, 1f)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokePx = strokeWidth.toPx()

            val offsetFactor = progress * w
            val brush = when (mode) {
                SettingsManager.MODE_STATIC_GRADIENT -> Brush.linearGradient(
                    colors = colors.map { it.copy(alpha = effectiveAlpha) },
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
                else -> Brush.sweepGradient(
                    colors = colors.map { it.copy(alpha = effectiveAlpha) },
                    center = Offset(w / 2f, h / 2f)
                )
            }

            // Draw perimeter edge glow
            drawRoundRect(
                brush = brush,
                size = Size(w - strokePx, h - strokePx),
                topLeft = Offset(strokePx / 2f, strokePx / 2f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Outer diffused neon glow
            drawRoundRect(
                brush = brush,
                size = Size(w - (strokePx * 2f), h - (strokePx * 2f)),
                topLeft = Offset(strokePx, strokePx),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),
                style = Stroke(width = strokePx * 1.5f, cap = StrokeCap.Round)
            )
        }
    }
}
