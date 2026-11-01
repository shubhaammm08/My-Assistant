package com.jarvis.assistant.offline

import android.content.Context
import android.util.Log
import androidx.work.*
import com.jarvis.assistant.data.local.JarvisDatabase
import com.jarvis.assistant.data.remote.JarvisApiClient
import com.jarvis.assistant.data.remote.RemoteTaskItem
import com.jarvis.assistant.data.remote.RemoteTaskSyncRequest
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val TAG = "JarvisSyncWorker"
    private val db = JarvisDatabase.getDatabase(context)

    override suspend fun doWork(): Result {
        Log.i(TAG, "Starting JARVIS Background Sync Worker...")
        return try {
            val unsynced = db.taskDao().getUnsyncedTasks()
            if (unsynced.isNotEmpty()) {
                val remoteTasks = unsynced.map { t ->
                    RemoteTaskItem(
                        id = t.id,
                        title = t.title,
                        description = t.description,
                        dueDate = t.dueDate,
                        priority = t.priority,
                        status = t.status,
                        recurrence = t.recurrence,
                        category = t.category,
                        createdAt = t.createdAt,
                        updatedAt = t.updatedAt
                    )
                }

                val response = JarvisApiClient.getService().syncTasks(
                    RemoteTaskSyncRequest(clientTasks = remoteTasks, lastSyncTimestamp = null)
                )

                // Mark local tasks as synced
                unsynced.forEach { t ->
                    db.taskDao().updateTask(t.copy(isSynced = true))
                }
                Log.i(TAG, "Successfully synced ${unsynced.size} tasks with backend.")
            }

            // Also check for pending offline chat/agent requests
            val pendingSyncList = db.pendingSyncDao().getAllPending()
            for (pending in pendingSyncList) {
                try {
                    // Send queued request
                    if (pending.actionType == "chat_message") {
                        val client = JarvisApiClient.getService()
                        client.sendChatMessage(
                            com.jarvis.assistant.data.remote.RemoteChatRequest(
                                message = pending.payloadJson
                            )
                        )
                    }
                    db.pendingSyncDao().removeSync(pending.id)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed syncing item ${pending.id}: ${e.message}")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "JarvisSyncWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncRequest)
        }
    }
}

