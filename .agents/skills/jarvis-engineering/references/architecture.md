# JARVIS System Architecture Blueprint

## 1. High-Level Architecture Overview

JARVIS is engineered as an **offline-first, dual-engine intelligent operating assistant**. It harmonizes a native Android client with a Python FastAPI backend and on-device offline edge reasoning.

```
+-----------------------------------------------------------------------------------+
|                              ANDROID CLIENT (KOTLIN)                              |
|                                                                                   |
|  +-----------------------------------------------------------------------------+  |
|  |                       UI LAYER (Jetpack Compose)                            |  |
|  |  [Futuristic Arc Reactor Orb]  [Waveform Visualizer]  [Chat Console]        |  |
|  |  [Task & Schedule Dashboard]   [Memory View]          [Agent Inspector]     |  |
|  +-----------------------------------------------------------------------------+  |
|                                         |                                         |
|  +-----------------------------------------------------------------------------+  |
|  |                     VIEWMODELS & ORCHESTRATION (MVVM)                       |  |
|  |  JarvisViewModel | VoiceViewModel | TaskViewModel | DeviceActionViewModel   |  |
|  +-----------------------------------------------------------------------------+  |
|                      |                                       |                    |
|  +------------------------------------+   +------------------------------------+  |
|  |           VOICE SUBSYSTEM          |   |       DEVICE AUTOMATION LAYER      |  |
|  |  - Multilingual SpeechRecognizer   |   |  - Intent Dispatcher (Apps/Phone)  |  |
|  |  - Japanese Female TTS Engine      |   |  - Telephony & SMS Agent           |  |
|  |  - Audio Waveform Capture          |   |  - Notification Listener Service   |  |
|  |  - Pitch / Rate Controller         |   |  - Alarm & Calendar Interop        |  |
|  +------------------------------------+   +------------------------------------+  |
|                      |                                       |                    |
|  +-----------------------------------------------------------------------------+  |
|  |                       SYNC & NETWORK ENGINE (Dual Mode)                     |  |
|  |  - Network State Monitor (Online/Offline Detector)                          |  |
|  |  - WorkManager Sync Workers                                                 |  |
|  |  - Pending Job Queue                                                        |  |
|  |  - Room Local Database (Tasks, Memory, Transcripts, Offline Models)          |  |
|  +-----------------------------------------------------------------------------+  |
+-----------------------------------------------------------------------------------+
                               |  ^                       |  ^
                REST / SSE / WS|  |                       |  | Local Fallback
                               v  |                       v  |
+------------------------------------+       +------------------------------------+
|       FASTAPI BACKEND (PYTHON)     |       |       ON-DEVICE EDGE BRAIN         |
|                                    |       |                                    |
| - FastAPI Router & Security        |       | - Deterministic Rule & Intent Extr |
| - Gemini Pro/Flash Reasoning API   |       | - Offline Speech-to-Text           |
| - Autonomous Multi-Step Planner    |       | - Offline Android TTS              |
| - Tool Calling Engine              |       | - Local Task & Calendar CRUD       |
| - Memory & Context Store           |       | - Queue for Reconnection Sync      |
| - Persistent Job Worker Engine     |       +------------------------------------+
| - Google Calendar / Gmail Connect  |
+------------------------------------+
```

---

## 2. Core Subsystems

### A. Voice Subsystem
* **Natural Japanese Female Voice Personality**:
  * Default English accent modulated with soft, warm Japanese phoneme cadence.
  * Native Japanese voice for Japanese phrases and polite greetings (*"Hais, Jarvis desu"* / *"At your service, master"*).
  * Pitch: 1.15x - 1.25x; Speed: 0.95x - 1.05x for calm, polite, ultra-clear delivery.
  * Dynamic multilingual voice switching: Seamlessly speaks Hindi (*"Namaste, main aapki sahayata kar sakti hoon"*), Marathi (*"Namaskar, me tumchi madat karu shakte"*), and English with high intelligibility.
* **Speech-to-Text (STT)**:
  * Android native `SpeechRecognizer` configured for multilingual models (`en-US`, `hi-IN`, `mr-IN`, `ja-JP`).
  * Continuous listening toggle, wake-word activation pathway, and push-to-talk mic button.
  * Real-time audio waveform amplitude sampling for the UI visualizer.

### B. Dual-Mode Brain (Online & Offline)
* **Online Mode (Gemini 2.5/Flash & Pro API)**:
  * Full reasoning, code generation, web research, complex entity extraction, multi-language translation, and dynamic tool orchestration.
* **Offline Mode (On-Device Fallback Engine)**:
  * Fast regex, grammar, and semantic slot-filling parser.
  * Creates reminders, schedules alarms, toggles system utilities, opens apps, searches offline notes, and reads scheduled agendas without touching the internet.
  * Queues online-only tasks (e.g., deep research, email sending) into a persistent Room queue, auto-resuming when network connectivity is re-established.

### C. Android Device Automation Layer
* **App Launcher**: Resolves package names dynamically via Android `PackageManager`.
* **Telephony & Messaging**: Generates direct `ACTION_DIAL`, `ACTION_CALL` (with permission), and `ACTION_SENDTO` for SMS/WhatsApp, verifying recipient and body before user approval.
* **Alarms & Timers**: Triggers `AlarmClock.ACTION_SET_ALARM` and `ACTION_SET_TIMER` with exact hour, minute, and message.
* **Volume & System Controls**: Utilizes `AudioManager` for ring, music, and notification volume adjustments.

### D. Task Management & Memory Engine
* **Tasks**: Room-backed SQLite database with title, description, deadline, priority (LOW, MEDIUM, HIGH, URGENT), recurrence rule, and completion state.
* **Memory Store**: Semantic key-value memory recording user preferences, names, habits, and conversational context across sessions.
* **Bi-directional Sync**: WorkManager jobs synchronize offline tasks with the FastAPI backend database upon connectivity transitions.

