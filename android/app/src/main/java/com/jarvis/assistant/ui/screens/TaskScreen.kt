package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.data.local.TaskEntity
import com.jarvis.assistant.ui.components.FuturisticCard
import com.jarvis.assistant.ui.theme.*
import com.jarvis.assistant.viewmodel.TaskViewModel

@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val filter by viewModel.selectedFilter.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = JarvisCyan,
                contentColor = JarvisBlack
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        containerColor = JarvisBlack,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "TASK & AGENDAS DASHBOARD",
                style = MaterialTheme.typography.headlineMedium,
                color = JarvisCyan
            )
            Text(
                text = "LOCAL PERSISTENCE // SYNC ENGINE READY",
                style = MaterialTheme.typography.labelSmall,
                color = JarvisTextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                listOf("ALL", "PENDING", "COMPLETED").forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(f, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JarvisBlue,
                            selectedLabelColor = JarvisBlack,
                            containerColor = JarvisCardBg,
                            labelColor = JarvisTextPrimary
                        )
                    )
                }
            }

            // Task List
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks found in this view.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisTextMuted
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggle = { viewModel.toggleTaskCompletion(task) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        CreateTaskDialog(
            onDismiss = { showDialog = false },
            onConfirm = { title, due, priority ->
                viewModel.createTask(title, due, priority)
                showDialog = false
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = task.status == "COMPLETED"
    val priorityColor = when (task.priority) {
        "URGENT" -> JarvisCrimsonAlert
        "HIGH" -> Color(0xFFFF9900)
        "MEDIUM" -> JarvisCyan
        else -> JarvisBlue
    }

    FuturisticCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isCompleted) JarvisCardBorder else priorityColor.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = "Toggle Complete",
                    tint = if (isCompleted) JarvisNeonGreen else JarvisTextMuted
                )
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (isCompleted) JarvisTextMuted else JarvisTextPrimary
                )
                if (!task.dueDate.isNullOrBlank()) {
                    Text(
                        text = "Due: ${task.dueDate}",
                        fontSize = 12.sp,
                        color = JarvisTextSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = priorityColor.copy(alpha = 0.2f),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = task.priority,
                    color = priorityColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = JarvisTextMuted)
            }
        }
    }
}

@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Task", color = JarvisCyan) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") }
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date / Time (Optional)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title, dueDate.ifEmpty { null }, priority) },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisBlack)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = JarvisTextSecondary)
            }
        },
        containerColor = JarvisCardBg
    )
}

