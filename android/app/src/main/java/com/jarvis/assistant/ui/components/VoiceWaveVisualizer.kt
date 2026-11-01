package com.jarvis.assistant.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisNeonPurple

@Composable
fun VoiceWaveVisualizer(
    isActive: Boolean,
    amplitude: Float = 0.5f,
    modifier: Modifier = Modifier,
    barCount: Int = 24,
    height: Dp = 56.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnimation")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f, // 2*PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PhaseAnim"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        val width = size.width
        val canvasHeight = size.height
        val barWidth = (width / (barCount * 1.6f)).coerceAtLeast(3f)
        val spacing = (width - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)

        val brush = Brush.verticalGradient(
            colors = listOf(JarvisCyan, JarvisNeonPurple)
        )

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount.toFloat()
            // Sine modulation
            val sinVal = kotlin.math.sin((progress * 4 * Math.PI + phase).toDouble()).toFloat()
            val dynamicScale = if (isActive) {
                (0.2f + 0.8f * kotlin.math.abs(sinVal)) * amplitude.coerceIn(0.1f, 1.0f)
            } else {
                0.08f
            }

            val barHeight = (canvasHeight * dynamicScale).coerceAtLeast(4.dp.toPx())
            val x = i * (barWidth + spacing)
            val y = (canvasHeight - barHeight) / 2f

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

