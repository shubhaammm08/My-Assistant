package com.jarvis.assistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.ui.theme.JarvisCardBg
import com.jarvis.assistant.ui.theme.JarvisCardBorder
import com.jarvis.assistant.ui.theme.JarvisCyan

@Composable
fun FuturisticCard(
    modifier: Modifier = Modifier,
    borderColor: Color = JarvisCardBorder,
    glowAccent: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val borderBrush = if (glowAccent) {
        Brush.linearGradient(
            colors = listOf(JarvisCyan.copy(alpha = 0.8f), JarvisCardBorder)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(borderColor, borderColor.copy(alpha = 0.5f))
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = JarvisCardBg,
        border = BorderStroke(1.2.dp, borderBrush),
        shadowElevation = if (glowAccent) 8.dp else 2.dp
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

