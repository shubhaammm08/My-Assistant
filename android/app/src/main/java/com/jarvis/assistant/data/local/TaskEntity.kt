package com.jarvis.assistant.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val dueDate: String?,
    val priority: String, // LOW, MEDIUM, HIGH, URGENT
    val status: String,   // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    val recurrence: String?,
    val category: String,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean = false
)

