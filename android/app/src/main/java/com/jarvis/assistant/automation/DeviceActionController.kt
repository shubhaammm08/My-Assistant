package com.jarvis.assistant.automation

import android.content.Context
import android.media.AudioManager
import android.util.Log
import com.jarvis.assistant.data.local.JarvisDatabase
import com.jarvis.assistant.data.local.TaskEntity
import com.jarvis.assistant.data.remote.RemoteToolCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class DeviceActionController(private val context: Context) {
    private val TAG = "DeviceActionController"
    private val appLauncher = AppLauncher(context)
    private val alarmController = AlarmController(context)
    private val telephonyManager = TelephonyManager(context)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val db = JarvisDatabase.getDatabase(context)

    fun executeToolCall(toolCall: RemoteToolCall, onComplete: (Boolean, String) -> Unit) {
        val args = toolCall.arguments
        Log.i(TAG, "Executing tool: ${toolCall.tool} with args: $args")

        when (toolCall.tool) {
            "open_application" -> {
                val appName = args["app_name"] as? String ?: ""
                val success = appLauncher.launchAppByName(appName)
                onComplete(success, if (success) "Opened $appName" else "Could not open $appName")
            }

            "set_alarm" -> {
                val hour = (args["hour"] as? Number)?.toInt() ?: 8
                val minute = (args["minute"] as? Number)?.toInt() ?: 0
                val message = args["message"] as? String ?: "JARVIS Alarm"
                val success = alarmController.setAlarm(hour, minute, message)
                onComplete(success, if (success) "Alarm set for $hour:$minute" else "Failed to set alarm")
            }

            "set_timer" -> {
                val seconds = (args["seconds"] as? Number)?.toInt() ?: 60
                val label = args["label"] as? String ?: "JARVIS Timer"
                val success = alarmController.setTimer(seconds, label)
                onComplete(success, if (success) "Timer set for $seconds seconds" else "Failed to set timer")
            }

            "make_phone_call" -> {
                val phone = (args["phone_number"] as? String) ?: (args["contact_name"] as? String) ?: ""
                val success = telephonyManager.dialPhoneNumber(phone)
                onComplete(success, if (success) "Dialing $phone" else "Unable to dial")
            }

            "draft_sms" -> {
                val phone = (args["phone_number"] as? String) ?: (args["contact_name"] as? String) ?: ""
                val body = args["message"] as? String ?: ""
                val success = telephonyManager.prepareSmsDraft(phone, body)
                onComplete(success, if (success) "Drafted SMS for $phone" else "Unable to draft SMS")
            }

            "adjust_volume" -> {
                val action = args["action"] as? String ?: "up"
                audioManager?.let { am ->
                    val stream = AudioManager.STREAM_MUSIC
                    val flag = AudioManager.FLAG_SHOW_UI
                    when (action) {
                        "up" -> am.adjustStreamVolume(stream, AudioManager.ADJUST_RAISE, flag)
                        "down" -> am.adjustStreamVolume(stream, AudioManager.ADJUST_LOWER, flag)
                        "mute" -> am.adjustStreamVolume(stream, AudioManager.ADJUST_MUTE, flag)
                        "unmute" -> am.adjustStreamVolume(stream, AudioManager.ADJUST_UNMUTE, flag)
                    }
                    onComplete(true, "Volume adjusted ($action)")
                } ?: onComplete(false, "Audio service unavailable")
            }

            "create_task" -> {
                val title = args["title"] as? String ?: "New Task"
                val dueDate = args["due_date"] as? String
                val priority = args["priority"] as? String ?: "MEDIUM"

                CoroutineScope(Dispatchers.IO).launch {
                    val task = TaskEntity(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = "Created via JARVIS Assistant",
                        dueDate = dueDate,
                        priority = priority,
                        status = "PENDING",
                        recurrence = null,
                        category = "voice",
                        createdAt = System.currentTimeMillis().toString(),
                        updatedAt = System.currentTimeMillis().toString(),
                        isSynced = false
                    )
                    db.taskDao().insertTask(task)
                }
                onComplete(true, "Created task: $title")
            }

            else -> {
                Log.w(TAG, "Unhandled tool: ${toolCall.tool}")
                onComplete(false, "Tool not supported on device")
            }
        }
    }
}

