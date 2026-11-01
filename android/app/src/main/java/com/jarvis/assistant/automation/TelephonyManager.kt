package com.jarvis.assistant.automation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

class TelephonyManager(private val context: Context) {
    private val TAG = "JarvisTelephony"

    fun dialPhoneNumber(phoneNumber: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phoneNumber.trim()}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.i(TAG, "Launched dialer for $phoneNumber")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch dialer: ${e.message}")
            false
        }
    }

    fun prepareSmsDraft(recipient: String, messageBody: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${recipient.trim()}")
                putExtra("sms_body", messageBody)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.i(TAG, "Prepared SMS draft for $recipient")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare SMS draft: ${e.message}")
            false
        }
    }
}

