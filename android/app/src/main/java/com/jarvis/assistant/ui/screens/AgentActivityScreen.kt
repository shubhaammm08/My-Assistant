package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.ui.components.FuturisticCard
import com.jarvis.assistant.ui.theme.*

data class AgentStepDisplay(
    val stepNumber: Int,
    val description: String,
    val status: String,
    val result: String?
)

@Composable
fun AgentActivityScreen(modifier: Modifier = Modifier) {
    var objectiveInput by remember { mutableStateOf("Research the best edge AI models for my Android device") }
    var isExecuting by remember { mutableStateOf(false) }

    val sampleSteps = listOf(
        AgentStepDisplay(1, "Scan hardware profile & LiteRT compatibility", "COMPLETED", "Target architecture: arm64-v8a, 8GB RAM"),
        AgentStepDisplay(2, "Fetch benchmark scores across Gemma 2B, Phi-3, Qwen 1.5B", "COMPLETED", "Qwen 1.5B & Gemma 2B yield ~18 tok/s"),
        AgentStepDisplay(3, "Formulate trade-off matrix & memory footprint", "IN_PROGRESS", "Memory ceiling: 1.4GB peak allocation"),
        AgentStepDisplay(4, "Synthesize strategic deployment report", "PENDING", null)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBlack)
            .padding(16.dp)
    ) {
        Text(
            text = "AUTONOMOUS AGENT ORCHESTRATOR",
            style = MaterialTheme.typography.headlineMedium,
            color = JarvisCyan
        )
        Text(
            text = "MULTI-STEP REASONING & BACKGROUND WORKERS",
            style = MaterialTheme.typography.labelSmall,
            color = JarvisTextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Objective Input Card
        FuturisticCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "Goal Objective",
                    fontSize = 12.sp,
                    color = JarvisTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = objectiveInput,
                    onValueChange = { objectiveInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { isExecuting = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisBlack)
                ) {
                    Text(if (isExecuting) "Agent Executing..." else "Dispatch Autonomous Agent")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Subtask Execution Pipeline",
            style = MaterialTheme.typography.titleLarge,
            color = JarvisTextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sampleSteps) { step ->
                val statusColor = when (step.status) {
                    "COMPLETED" -> JarvisNeonGreen
                    "IN_PROGRESS" -> JarvisBlue
                    else -> JarvisTextMuted
                }

                FuturisticCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = statusColor.copy(alpha = 0.5f)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STEP ${step.stepNumber}",
                                color = JarvisCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = step.status,
                                    color = statusColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = step.description,
                            color = JarvisTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        if (!step.result.isNullOrBlank()) {
                            Text(
                                text = "Result: ${step.result}",
                                color = JarvisTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

