package com.jarvis.assistant.ui.components

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.ui.theme.*

enum class AssistantOrbState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING
}

@Composable
fun ArcReactorOrb(
    state: AssistantOrbState,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransition")

    // Slow rotation for outer ring
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OuterRingRotation"
    )

    // Counter rotation for inner segmented ring
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == AssistantOrbState.PROCESSING) 3000 else 8000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "InnerRingRotation"
    )

    // Breathing pulse for core glow
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (state == AssistantOrbState.LISTENING) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantOrbState.LISTENING -> 800
                    AssistantOrbState.SPEAKING -> 1000
                    AssistantOrbState.PROCESSING -> 600
                    AssistantOrbState.IDLE -> 2500
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CorePulse"
    )

    // Colors mapped to state
    val primaryGlow = when (state) {
        AssistantOrbState.LISTENING -> JarvisNeonGreen
        AssistantOrbState.SPEAKING -> JarvisNeonPurple
        AssistantOrbState.PROCESSING -> JarvisBlue
        AssistantOrbState.IDLE -> JarvisCyan
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val radius = size.toPx() / 2f

            // 1. Central Ambient Radial Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlow.copy(alpha = 0.45f * pulseScale),
                        primaryGlow.copy(alpha = 0.15f * pulseScale),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * pulseScale
                ),
                radius = radius * pulseScale,
                center = center
            )

            // 2. Outer Segmented Ring (Clockwise)
            rotate(outerRotation, pivot = center) {
                val outerRadius = radius * 0.90f
                val strokeWidth = 2.5.dp.toPx()
                // 3 segmented arcs
                for (i in 0 until 3) {
                    drawArc(
                        color = primaryGlow.copy(alpha = 0.7f),
                        startAngle = i * 120f + 15f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // 3. Middle High-Tech Tick Ring (Counter-Clockwise)
            rotate(innerRotation, pivot = center) {
                val midRadius = radius * 0.72f
                val strokeWidth = 3.dp.toPx()
                // 6 futuristic brackets
                for (i in 0 until 6) {
                    drawArc(
                        color = JarvisBlue.copy(alpha = 0.85f),
                        startAngle = i * 60f + 10f,
                        sweepAngle = 40f,
                        useCenter = false,
                        topLeft = Offset(center.x - midRadius, center.y - midRadius),
                        size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // 4. Inner Arc Reactor Core
            val coreRadius = radius * 0.42f * pulseScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ArcReactorCore,
                        primaryGlow,
                        primaryGlow.copy(alpha = 0.2f)
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // 5. High-Energy Center Dot
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx() * pulseScale,
                center = center
            )
        }
    }
}

