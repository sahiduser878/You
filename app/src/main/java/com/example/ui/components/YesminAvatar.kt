package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
fun YesminAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    state: VoiceCallState = VoiceCallState.IDLE,
    audioLevel: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_anim")

    // Rotation for outer holographic rings
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rot"
    )

    // Breathing glow animation
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    // Active color determined by state
    val coreGlowColor = when (state) {
        VoiceCallState.LISTENING -> YesminCyan
        VoiceCallState.THINKING -> YesminPurple
        VoiceCallState.SPEAKING -> YesminPink
        VoiceCallState.MUTED -> Color(0xFFFF5252)
        VoiceCallState.IDLE -> YesminCyan
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2f) * 0.85f

            // Dynamic reactive radius based on audio level
            val dynamicRadius = baseRadius * breathScale * (1f + (audioLevel * 0.15f))

            // 1. Ambient outer aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(coreGlowColor.copy(alpha = 0.35f), Color.Transparent),
                    center = center,
                    radius = dynamicRadius * 1.25f
                ),
                radius = dynamicRadius * 1.25f,
                center = center
            )

            // 2. Rotating orbital neural ring with arc gaps
            val radians = Math.toRadians(ringRotation.toDouble())
            drawArc(
                color = YesminPurple.copy(alpha = 0.7f),
                startAngle = ringRotation,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(center.x - dynamicRadius, center.y - dynamicRadius),
                size = androidx.compose.ui.geometry.Size(dynamicRadius * 2, dynamicRadius * 2),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                color = YesminCyan.copy(alpha = 0.8f),
                startAngle = ringRotation + 120f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(center.x - dynamicRadius, center.y - dynamicRadius),
                size = androidx.compose.ui.geometry.Size(dynamicRadius * 2, dynamicRadius * 2),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                color = YesminPink.copy(alpha = 0.7f),
                startAngle = ringRotation + 240f,
                sweepAngle = 60f,
                useCenter = false,
                topLeft = Offset(center.x - dynamicRadius, center.y - dynamicRadius),
                size = androidx.compose.ui.geometry.Size(dynamicRadius * 2, dynamicRadius * 2),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Orbital particle dots
            val p1X = center.x + (dynamicRadius * cos(radians)).toFloat()
            val p1Y = center.y + (dynamicRadius * sin(radians)).toFloat()
            drawCircle(color = YesminCyan, radius = 4.dp.toPx(), center = Offset(p1X, p1Y))

            val p2X = center.x + (dynamicRadius * cos(radians + Math.PI)).toFloat()
            val p2Y = center.y + (dynamicRadius * sin(radians + Math.PI)).toFloat()
            drawCircle(color = YesminPink, radius = 4.dp.toPx(), center = Offset(p2X, p2Y))

            // 3. Central Dark Glass Core
            val innerRadius = baseRadius * 0.72f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E2842), Color(0xFF0F1524)),
                    center = center,
                    radius = innerRadius
                ),
                radius = innerRadius,
                center = center
            )

            // 4. Stylized Futuristic Hologram Female AI Silhouette
            val headRadius = innerRadius * 0.32f
            val headCenter = Offset(center.x, center.y - (innerRadius * 0.22f))

            // Hair/Crown Hologram Aura
            drawArc(
                color = YesminPurple.copy(alpha = 0.6f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(headCenter.x - headRadius * 1.3f, headCenter.y - headRadius * 1.2f),
                size = androidx.compose.ui.geometry.Size(headRadius * 2.6f, headRadius * 2.4f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Head silhouette
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2A3654), Color(0xFF151D2F)),
                    center = headCenter,
                    radius = headRadius
                ),
                radius = headRadius,
                center = headCenter
            )

            // Glowing Neural Tiara / Eyes
            val eyeOffsetY = headCenter.y - (headRadius * 0.05f)
            val eyeSpacing = headRadius * 0.45f
            drawCircle(color = coreGlowColor, radius = 2.5.dp.toPx(), center = Offset(headCenter.x - eyeSpacing, eyeOffsetY))
            drawCircle(color = coreGlowColor, radius = 2.5.dp.toPx(), center = Offset(headCenter.x + eyeSpacing, eyeOffsetY))

            // Forehead neural core crystal
            drawCircle(color = YesminPink, radius = 2.dp.toPx(), center = Offset(headCenter.x, headCenter.y - (headRadius * 0.55f)))

            // Shoulders & Chest line
            val shoulderPath = Path().apply {
                moveTo(center.x - innerRadius * 0.65f, center.y + innerRadius * 0.85f)
                quadraticTo(
                    center.x - innerRadius * 0.35f,
                    center.y + innerRadius * 0.25f,
                    center.x,
                    center.y + innerRadius * 0.3f
                )
                quadraticTo(
                    center.x + innerRadius * 0.35f,
                    center.y + innerRadius * 0.25f,
                    center.x + innerRadius * 0.65f,
                    center.y + innerRadius * 0.85f
                )
            }
            drawPath(
                path = shoulderPath,
                color = coreGlowColor.copy(alpha = 0.75f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Subtle heart/neural node at center of chest
            drawCircle(
                color = YesminPink,
                radius = 3.dp.toPx(),
                center = Offset(center.x, center.y + innerRadius * 0.48f)
            )
        }
    }
}
