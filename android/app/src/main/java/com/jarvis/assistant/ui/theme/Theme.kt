package com.jarvis.assistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = JarvisCyan,
    secondary = JarvisBlue,
    tertiary = JarvisNeonPurple,
    background = JarvisBlack,
    surface = JarvisCardBg,
    onPrimary = JarvisBlack,
    onSecondary = JarvisBlack,
    onTertiary = JarvisBlack,
    onBackground = JarvisTextPrimary,
    onSurface = JarvisTextPrimary
)

@Composable
fun JARVISTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

