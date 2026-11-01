package com.jarvis.assistant.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class JapaneseTtsEngine(
    private val context: Context,
    private val onInitComplete: (Boolean) -> Unit,
    private val onSpeakingStateChanged: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private val TAG = "JapaneseTtsEngine"
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    // Default Japanese female persona settings
    var pitch: Float = 1.22f
        set(value) {
            field = value
            tts?.setPitch(value)
        }

    var speechRate: Float = 1.0f
        set(value) {
            field = value
            tts?.setSpeechRate(value)
        }

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setPitch(pitch)
            tts?.setSpeechRate(speechRate)

            // Select optimal Japanese or high-pitch female voice if available
            configureVoicePersona()

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeakingStateChanged(true)
                }

                override fun onDone(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }
            })

            Log.i(TAG, "Japanese TTS Engine initialized successfully")
            onInitComplete(true)
        } else {
            Log.e(TAG, "Failed to initialize TTS engine. Code: $status")
            onInitComplete(false)
        }
    }

    private fun configureVoicePersona() {
        tts?.let { engine ->
            val voices = engine.voices
            if (!voices.isNullOrEmpty()) {
                // Try finding a Japanese voice or female voice
                val femaleVoice = voices.firstOrNull { v ->
                    (v.name.contains("female", ignoreCase = true) || v.name.contains("ja", ignoreCase = true)) &&
                    !v.isNetworkConnectionRequired
                }
                if (femaleVoice != null) {
                    engine.voice = femaleVoice
                    Log.i(TAG, "Selected TTS voice: ${femaleVoice.name}")
                }
            }
        }
    }

    fun speak(text: String, languageLocale: String = "en-US") {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not ready yet")
            return
        }

        // Set locale matching the detected response language
        val targetLocale = when {
            languageLocale.startsWith("ja") -> Locale.JAPAN
            languageLocale.startsWith("hi") -> Locale("hi", "IN")
            languageLocale.startsWith("mr") -> Locale("mr", "IN")
            else -> Locale.US
        }

        val result = tts?.setLanguage(targetLocale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Target locale $targetLocale not supported, falling back to English")
            tts?.setLanguage(Locale.US)
        }

        tts?.setPitch(pitch)
        tts?.setSpeechRate(speechRate)

        val utteranceId = "JARVIS_MSG_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        onSpeakingStateChanged(false)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

