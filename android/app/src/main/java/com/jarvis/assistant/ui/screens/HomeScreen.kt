package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.R
import com.jarvis.assistant.ui.components.ArcReactorOrb
import com.jarvis.assistant.ui.components.AssistantOrbState
import com.jarvis.assistant.ui.components.VoiceWaveVisualizer
import com.jarvis.assistant.ui.theme.*
import com.jarvis.assistant.viewmodel.JarvisViewModel

@Composable
fun HomeScreen(
    viewModel: JarvisViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.orbState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val amplitude by viewModel.audioAmplitude.collectAsState()

    val quickCommands = listOf(
        "Kal 8 baje uthana",
        "उद्या सकाळी आठ वाजता उठव",
        "Open YouTube",
        "Research edge AI models",
        "Set timer for 5 minutes"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBlack)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top HUD / Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "JARVIS CORE v1.0",
                    style = MaterialTheme.typography.headlineMedium,
                    color = JarvisCyan
                )
                Text(
                    text = "NEURAL AI OPERATING ASSISTANT",
                    style = MaterialTheme.typography.labelSmall,
                    color = JarvisTextSecondary
                )
            }

            // Online / Offline Status Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isOnline) JarvisCardBg else JarvisCrimsonAlert.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isOnline) JarvisNeonGreen else JarvisCrimsonAlert
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) JarvisNeonGreen else JarvisCrimsonAlert)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) "ONLINE" else "OFFLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) JarvisNeonGreen else JarvisCrimsonAlert
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Centerpiece: Arc Reactor Orb
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            ArcReactorOrb(
                state = orbState,
                size = 240.dp
            )
        }

        // 3. Audio Waveform Visualizer
        VoiceWaveVisualizer(
            isActive = orbState == AssistantOrbState.LISTENING || orbState == AssistantOrbState.SPEAKING,
            amplitude = amplitude,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // 4. Quick Command Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            items(quickCommands) { cmd ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = JarvisCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCardBorder),
                    modifier = Modifier.clickable {
                        viewModel.processUserMessage(cmd)
                        onNavigateToChat()
                    }
                ) {
                    Text(
                        text = cmd,
                        color = JarvisTextPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 5. Large Voice Activation Button (Push to Talk)
        val isListening = orbState == AssistantOrbState.LISTENING
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = if (isListening) listOf(JarvisCrimsonAlert, JarvisNeonPurple)
                        else listOf(JarvisCyan, JarvisBlue)
                    )
                )
                .clickable {
                    if (isListening) {
                        viewModel.stopListening()
                    } else {
                        viewModel.startListening()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = "Voice Control",
                tint = JarvisBlack,
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = if (isListening) "Listening... Speak now" else "Tap microphone to speak",
            style = MaterialTheme.typography.bodyMedium,
            color = JarvisTextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
    }
}

