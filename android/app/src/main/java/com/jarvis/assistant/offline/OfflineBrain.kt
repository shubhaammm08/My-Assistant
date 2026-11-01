package com.jarvis.assistant.offline

import android.content.Context
import android.util.Log
import com.jarvis.assistant.automation.DeviceActionController
import com.jarvis.assistant.data.local.JarvisDatabase
import com.jarvis.assistant.data.local.PendingSyncEntity
import com.jarvis.assistant.data.local.TaskEntity
import com.jarvis.assistant.data.remote.RemoteToolCall
import com.jarvis.assistant.data.remote.RemoteVoiceSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class OfflineResult(
    val replyText: String,
    val detectedLanguage: String,
    val voiceSettings: RemoteVoiceSettings,
    val executedActions: List<String>
)

class OfflineBrain(private val context: Context) {
    private val TAG = "JarvisOfflineBrain"
    private val deviceController = DeviceActionController(context)
    private val db = JarvisDatabase.getDatabase(context)

    suspend fun processOffline(input: String): OfflineResult = withContext(Dispatchers.IO) {
        val normalized = normalizeNumbers(input)
        val textLower = normalized.trim().lowercase()
        val detectedLang = detectLanguage(input)
        val actions = mutableListOf<String>()
        var replyText = ""

        // 1. Alarm & Wake-up parsing (English, Hindi, Marathi)
        val isAlarm = listOf("alarm", "uthana", "uthav", "wake", "वाजता उठव", "baje").any { textLower.contains(it) }
        val timeRegex = Regex("""(\d{1,2})(?::(\d{2}))?\s*(am|pm|baje|वाजता)?""")
        val match = timeRegex.find(textLower)

        if (isAlarm && match != null) {
            var hour = match.groupValues[1].toIntOrNull() ?: 8
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            val meridiem = match.groupValues[3]

            if (meridiem == "pm" && hour < 12) hour += 12
            else if (meridiem == "am" && hour == 12) hour = 0
            else if ((meridiem.isEmpty() || meridiem in listOf("baje", "वाजता")) && 
                     (textLower.contains("shaam") || textLower.contains("night") || textLower.contains("संध्याकाळी")) && hour < 12) {
                hour += 12
            }

            // Execute alarm on device
            val toolCall = RemoteToolCall(
                callId = "offline-alarm",
                tool = "set_alarm",
                arguments = mapOf("hour" to hour, "minute" to minute, "message" to "Wake up")
            )
            deviceController.executeToolCall(toolCall) { success, msg ->
                if (success) actions.add(msg)
            }

            // Save task locally
            val task = TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Wake up at String.format('%02d:%02d', hour, minute)",
                description = "Offline scheduled alarm",
                dueDate = "Tomorrow String.format('%02d:%02d', hour, minute)",
                priority = "HIGH",
                status = "PENDING",
                recurrence = null,
                category = "alarm",
                createdAt = System.currentTimeMillis().toString(),
                updatedAt = System.currentTimeMillis().toString(),
                isSynced = false
            )
            db.taskDao().insertTask(task)

            replyText = when (detectedLang) {
                "hi" -> "Hai! Offline mode mein kal subah $hour:$minute ke liye alarm schedule kar diya hai."
                "mr" -> "हो, ऑफलाइन मोडमध्ये उद्या सकाळी $hour:$minute वाजताचा अलार्म सेट केला आहे."
                "ja" -> "はい、オフラインで${hour}時${minute}分にアラームを設定いたしました。"
                else -> "Understood! Operating offline: your alarm for $hour:$minute has been set."
            }

            return@withContext OfflineResult(
                replyText = replyText,
                detectedLanguage = detectedLang,
                voiceSettings = getSettings(detectedLang),
                executedActions = actions
            )
        }

        // 2. App Launch parsing
        val openAppPrefixes = listOf("open", "launch", "kholo", "khol", "ughad", "start")
        val isAppLaunch = openAppPrefixes.any { textLower.startsWith(it) || textLower.contains(it) }

        if (isAppLaunch) {
            val appName = textLower
                .replace("open", "")
                .replace("launch", "")
                .replace("kholo", "")
                .replace("ughad", "")
                .replace("please", "")
                .trim()
                .capitalize()

            val toolCall = RemoteToolCall(
                callId = "offline-app",
                tool = "open_application",
                arguments = mapOf("app_name" to appName)
            )
            deviceController.executeToolCall(toolCall) { success, msg ->
                if (success) actions.add(msg)
            }

            replyText = when (detectedLang) {
                "hi" -> "Zaroor! Main $appName open kar rahi hoon."
                "mr" -> "नक्कीच, मी $appName सुरू करत आहे."
                else -> "Opening $appName right away."
            }

            return@withContext OfflineResult(
                replyText = replyText,
                detectedLanguage = detectedLang,
                voiceSettings = getSettings(detectedLang),
                executedActions = actions
            )
        }

        // 3. Task & Reminder creation
        if (textLower.contains("remind") || textLower.contains("yaad") || textLower.contains("task") || textLower.contains("आठवण")) {
            val cleanTitle = input
                .replace("remind me to", "", ignoreCase = true)
                .replace("remind me", "", ignoreCase = true)
                .replace("yaad dila", "", ignoreCase = true)
                .trim()

            val task = TaskEntity(
                id = UUID.randomUUID().toString(),
                title = cleanTitle.ifEmpty { "New Task" },
                description = "Created in offline mode",
                dueDate = "Today",
                priority = "MEDIUM",
                status = "PENDING",
                recurrence = null,
                category = "general",
                createdAt = System.currentTimeMillis().toString(),
                updatedAt = System.currentTimeMillis().toString(),
                isSynced = false
            )
            db.taskDao().insertTask(task)
            actions.add("Task created: $cleanTitle")

            replyText = when (detectedLang) {
                "hi" -> "Maine offline reminder save kar liya hai: '$cleanTitle'."
                "mr" -> "मी ऑफलाइन नोंद घेतली आहे: '$cleanTitle'."
                else -> "Saved your task locally: '$cleanTitle'."
            }

            return@withContext OfflineResult(
                replyText = replyText,
                detectedLanguage = detectedLang,
                voiceSettings = getSettings(detectedLang),
                executedActions = actions
            )
        }

        // 4. Default: Queue internet-dependent request for reconnect sync
        db.pendingSyncDao().queueSync(
            PendingSyncEntity(
                actionType = "chat_message",
                payloadJson = input
            )
        )
        actions.add("Queued for network reconnection")

        replyText = when (detectedLang) {
            "hi" -> "Abhi internet uplabdh nahi hai. Maine aapki request queue kar li hai, connection aate hi execute kar dungi."
            "mr" -> "सध्या इंटरनेट उपलब्ध नाही. मी तुमची विनंती नोंदवली आहे, इंटरनेट सुरू होताच पूर्ण करेन."
            else -> "We are currently offline. I have saved and queued your request to execute as soon as the network returns."
        }

        OfflineResult(
            replyText = replyText,
            detectedLanguage = detectedLang,
            voiceSettings = getSettings(detectedLang),
            executedActions = actions
        )
    }

    private fun detectLanguage(text: String): String {
        // Devanagari detection
        if (text.any { it in '\u0900'..'\u097F' }) {
            val marathiWords = listOf("आहे", "उद्या", "मला", "करा", "वाजता", "आठवण", "उठव")
            if (marathiWords.any { text.contains(it) }) return "mr"
            return "hi"
        }
        val lower = text.lowercase()
        val hinglishWords = listOf("kal", "mujhe", "uthana", "baje", "karo", "kya", "hai")
        if (hinglishWords.any { lower.contains(it) }) return "hi"
        val marathiRom = listOf("udya", "uthav", "mala", "ahe", "vasta")
        if (marathiRom.any { lower.contains(it) }) return "mr"
        val jaWords = listOf("hai", "desu", "ohayo", "arigato", "anata")
        if (jaWords.any { lower.contains(it) }) return "ja"
        return "en"
    }

    private fun normalizeNumbers(text: String): String {
        var res = text
        val devMap = mapOf('०' to '0', '१' to '1', '२' to '2', '३' to '3', '४' to '4', '५' to '5', '६' to '6', '७' to '7', '८' to '8', '९' to '9')
        devMap.forEach { (k, v) -> res = res.replace(k, v) }
        val words = mapOf("एक" to "1", "दोन" to "2", "दो" to "2", "तीन" to "3", "चार" to "4", "पाच" to "5", "सहा" to "6", "सात" to "7", "आठ" to "8", "नऊ" to "9", "दहा" to "10")
        words.forEach { (w, d) -> res = res.replace(w, d) }
        return res
    }

    private fun getSettings(lang: String): RemoteVoiceSettings {
        return when (lang) {
            "hi" -> RemoteVoiceSettings(1.2f, 1.0f, 1.0f, "hi-IN", "hi-female", "japanese_polite")
            "mr" -> RemoteVoiceSettings(1.2f, 1.0f, 1.0f, "mr-IN", "mr-female", "japanese_polite")
            "ja" -> RemoteVoiceSettings(1.22f, 1.0f, 1.0f, "ja-JP", "ja-female", "japanese_native")
            else -> RemoteVoiceSettings(1.24f, 0.98f, 1.0f, "en-US", "en-female", "japanese_accented_english")
        }
    }
}

