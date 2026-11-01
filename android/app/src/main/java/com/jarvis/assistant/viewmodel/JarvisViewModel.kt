package com.jarvis.assistant.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.automation.DeviceActionController
import com.jarvis.assistant.data.remote.JarvisApiClient
import com.jarvis.assistant.data.remote.RemoteChatRequest
import com.jarvis.assistant.data.remote.RemoteToolCall
import com.jarvis.assistant.offline.OfflineBrain
import com.jarvis.assistant.offline.SyncWorker
import com.jarvis.assistant.ui.components.AssistantOrbState
import com.jarvis.assistant.voice.JapaneseTtsEngine
import com.jarvis.assistant.voice.SpeechRecognizerHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "jarvis"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCalls: List<RemoteToolCall> = emptyList(),
    val requiresConfirmation: Boolean = false
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "JarvisViewModel"

    private val _orbState = MutableStateFlow(AssistantOrbState.IDLE)
    val orbState: StateFlow<AssistantOrbState> = _orbState.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0.1f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _messages = MutableStateFlow<List<UiChatMessage>>(listOf(
        UiChatMessage(
            sender = "jarvis",
            text = "Systems initialized. JARVIS is ready. How may I serve you today?"
        )
    ))
    val messages: StateFlow<List<UiChatMessage>> = _messages.asStateFlow()

    private val offlineBrain = OfflineBrain(application)
    private val deviceController = DeviceActionController(application)

    private val ttsEngine = JapaneseTtsEngine(
        context = application,
        onInitComplete = { ready ->
            Log.i(TAG, "TTS Ready: $ready")
        },
        onSpeakingStateChanged = { speaking ->
            _orbState.value = if (speaking) AssistantOrbState.SPEAKING else AssistantOrbState.IDLE
        }
    )

    private val sttHelper = SpeechRecognizerHelper(
        context = application,
        onResult = { recognizedText ->
            processUserMessage(recognizedText)
        },
        onError = { errMsg ->
            _orbState.value = AssistantOrbState.IDLE
            Log.w(TAG, "STT Error: $errMsg")
        },
        onAmplitudeChanged = { amp ->
            _audioAmplitude.value = amp
        },
        onListeningStateChanged = { listening ->
            _orbState.value = if (listening) AssistantOrbState.LISTENING else AssistantOrbState.IDLE
        }
    )

    init {
        monitorNetwork(application)
        SyncWorker.schedulePeriodicSync(application)
    }

    private fun monitorNetwork(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
                Log.i(TAG, "Network restored. Triggering sync.")
                SyncWorker.triggerImmediateSync(context)
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
                Log.w(TAG, "Network disconnected. Switched to offline mode.")
            }
        })
    }

    fun startListening() {
        sttHelper.startListening("en-US")
    }

    fun stopListening() {
        sttHelper.stopListening()
    }

    fun processUserMessage(text: String) {
        if (text.isBlank()) return

        // Add user message to UI
        val userMsg = UiChatMessage(sender = "user", text = text)
        _messages.value = _messages.value + userMsg
        _orbState.value = AssistantOrbState.PROCESSING

        viewModelScope.launch {
            if (_isOnline.value) {
                try {
                    val response = JarvisApiClient.getService().sendChatMessage(
                        RemoteChatRequest(message = text)
                    )

                    // Execute tools
                    for (tool in response.toolCalls) {
                        deviceController.executeToolCall(tool) { _, _ -> }
                    }

                    // Add assistant response
                    val asstMsg = UiChatMessage(
                        sender = "jarvis",
                        text = response.replyText,
                        toolCalls = response.toolCalls,
                        requiresConfirmation = response.requiresConfirmation
                    )
                    _messages.value = _messages.value + asstMsg

                    // Speak reply with Japanese female persona
                    ttsEngine.speak(response.replyText, response.voiceSettings.locale)

                } catch (e: Exception) {
                    Log.w(TAG, "Online request failed: ${e.message}. Falling back to offline brain.")
                    handleOfflineFallback(text)
                }
            } else {
                handleOfflineFallback(text)
            }
        }
    }

    private suspend fun handleOfflineFallback(text: String) {
        val offlineResult = offlineBrain.processOffline(text)

        val asstMsg = UiChatMessage(
            sender = "jarvis",
            text = offlineResult.replyText
        )
        _messages.value = _messages.value + asstMsg

        // Speak reply using offline TTS
        ttsEngine.speak(offlineResult.replyText, offlineResult.voiceSettings.locale)
    }

    override fun onCleared() {
        super.onCleared()
        sttHelper.destroy()
        ttsEngine.shutdown()
    }
}

