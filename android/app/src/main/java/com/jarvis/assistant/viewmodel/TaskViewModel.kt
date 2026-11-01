package com.jarvis.assistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.data.local.JarvisDatabase
import com.jarvis.assistant.data.local.TaskEntity
import com.jarvis.assistant.offline.SyncWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val db = JarvisDatabase.getDatabase(application)
    private val taskDao = db.taskDao()

    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            taskDao.getAllTasks().collect { list ->
                _tasks.value = when (_selectedFilter.value) {
                    "PENDING" -> list.filter { it.status == "PENDING" }
                    "COMPLETED" -> list.filter { it.status == "COMPLETED" }
                    else -> list
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        loadTasks()
    }

    fun createTask(title: String, dueDate: String?, priority: String = "MEDIUM") {
        viewModelScope.launch {
            val task = TaskEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                description = null,
                dueDate = dueDate,
                priority = priority,
                status = "PENDING",
                recurrence = null,
                category = "user",
                createdAt = System.currentTimeMillis().toString(),
                updatedAt = System.currentTimeMillis().toString(),
                isSynced = false
            )
            taskDao.insertTask(task)
            SyncWorker.triggerImmediateSync(getApplication())
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = if (task.status == "COMPLETED") "PENDING" else "COMPLETED"
            taskDao.updateStatus(task.id, newStatus, System.currentTimeMillis().toString())
            SyncWorker.triggerImmediateSync(getApplication())
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskDao.deleteTask(taskId)
            SyncWorker.triggerImmediateSync(getApplication())
        }
    }
}

