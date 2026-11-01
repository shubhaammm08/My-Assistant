package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.data.remote.JarvisApiClient
import com.jarvis.assistant.ui.components.FuturisticCard
import com.jarvis.assistant.ui.theme.*

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    var serverUrl by remember { mutableStateOf(JarvisApiClient.baseUrl) }
    var voicePitch by remember { mutableStateOf(1.22f) }
    var voiceRate by remember { mutableStateOf(1.0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SYSTEM CONFIGURATION",
            style = MaterialTheme.typography.headlineMedium,
            color = JarvisCyan
        )

        // 1. Voice Personality Controls
        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "JAPANESE FEMALE VOICE PERSONA",
                    style = MaterialTheme.typography.titleLarge,
                    color = JarvisCyan
                )
                Text(
                    text = "Soft, polite, intelligent inflection for English, Hindi, Marathi & Japanese.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = JarvisTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Voice Pitch: String.format('%.2f', voicePitch)x", color = JarvisTextPrimary)
                Slider(
                    value = voicePitch,
                    onValueChange = { voicePitch = it },
                    valueRange = 0.8f..1.5f,
                    colors = SliderDefaults.colors(thumbColor = JarvisCyan, activeTrackColor = JarvisCyan)
                )

                Text(text = "Speech Rate: String.format('%.2f', voiceRate)x", color = JarvisTextPrimary)
                Slider(
                    value = voiceRate,
                    onValueChange = { voiceRate = it },
                    valueRange = 0.7f..1.4f,
                    colors = SliderDefaults.colors(thumbColor = JarvisBlue, activeTrackColor = JarvisBlue)
                )
            }
        }

        // 2. Backend Server Configuration
        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "BACKEND SERVER URL",
                    style = MaterialTheme.typography.titleLarge,
                    color = JarvisCyan
                )
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = {
                        serverUrl = it
                        JarvisApiClient.baseUrl = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("FastAPI Server Base URL") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // 3. Android Permission Checklist
        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "PERMITTED PHONE POWERS",
                    style = MaterialTheme.typography.titleLarge,
                    color = JarvisCyan
                )
                PermissionRow("Record Audio (Voice Recognition)", true)
                PermissionRow("Alarms & Timers (AlarmClock API)", true)
                PermissionRow("App Launching (PackageManager)", true)
                PermissionRow("Telephony & Dialer (Phone API)", true)
                PermissionRow("Background Sync (WorkManager)", true)
            }
        }
    }
}

@Composable
fun PermissionRow(name: String, granted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = JarvisTextPrimary, fontSize = 13.sp)
        Text(
            text = if (granted) "GRANTED" else "DENIED",
            color = if (granted) JarvisNeonGreen else JarvisCrimsonAlert,
            fontSize = 11.sp
        )
    }
}

