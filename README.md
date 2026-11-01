# JARVIS – Advanced Personal AI Operating Assistant

<div align="center">

![JARVIS Banner](https://img.shields.io/badge/JARVIS-Personal%20AI%20Assistant-00F2FE?style=for-the-badge&logo=android&logoColor=black)
![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20FastAPI-4FACFE?style=for-the-badge)
![Languages](https://img.shields.io/badge/Languages-English%20%7C%20Hindi%20%7C%20Marathi%20%7C%20Japanese-7F00FF?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-00FF87?style=for-the-badge)

**A production-quality personal AI secretary and phone operating assistant for Android.**  
*Combines Gemini 2.5 online reasoning, on-device offline edge intelligence, natural Japanese female voice personality, phone automation, and autonomous multi-step agents.*

</div>

---

## 🌟 Key Features

### 1. 🎙️ Natural Japanese Female Voice Personality
- **Voice Profile**: Soft, pleasant, intelligent, friendly, slightly playful tone with a natural Japanese-influenced English cadence.
- **Multilingual Native Comprehension**: Seamlessly understands and switches across:
  - **English** (with Japanese-accented female cadence)
  - **Hindi** & **Hinglish** (e.g. *"Kal mujhe 8 baje uthana"*)
  - **Marathi** & **Marathi-English** (e.g. *"मला उद्या सकाळी आठ वाजता उठव"*)
  - **Japanese** (*"明日の朝8時に起こして"*)
- **Devanagari Numerals & Word Conversion**: Translates word numbers (*"आठ"* ➔ 8, *"दहा"* ➔ 10) into exact numeric parameters automatically.

### 2. 📱 Android Phone Automation & Tools
Real Android OS integration via native Android intents and platform APIs:
- **App Launcher**: Dynamically resolves and opens installed apps (*YouTube, WhatsApp, Camera, Settings, Spotify, etc.*) or redirects to Play Store.
- **Alarms & Timers**: Triggers exact `AlarmClock.ACTION_SET_ALARM` and `ACTION_SET_TIMER` with hour, minute, and labels.
- **Telephony & Messaging**: Prepares direct phone calls (`ACTION_DIAL`) and draft messages (`ACTION_SENDTO`) with user safety confirmation.
- **Audio Controls**: Adjusts media, ring, and alarm volume streams via `AudioManager`.

### 3. 🧠 Dual-Mode Intelligence (Offline-First)
- **Online Mode**: Full multi-turn conversational reasoning, tool calling, and deep web research powered by Google Gemini.
- **Offline Edge Mode**: On-device semantic slot-filling parser running completely offline. Handles alarms, timers, app launches, and local task creation with zero internet connection.
- **Automatic Reconnection Sync**: WorkManager monitors network status and flushes queued offline requests and tasks to the backend when connectivity returns.

### 4. 📋 Advanced Personal Task Manager
- Persistent local SQLite database (Room on Android, aiosqlite/SQLAlchemy on FastAPI).
- Task priorities (*LOW, MEDIUM, HIGH, URGENT*), due dates, recurrence rules, and completion status.
- Bi-directional synchronization with conflict resolution.

### 5. 🤖 Autonomous Multi-Step AI Agent
- Takes complex multi-step objectives (*"Research the best edge AI models for Android and prepare a comparison report"*).
- Decomposes goals into discrete execution subtasks.
- Runs background execution loops, tracks progress, handles retries, and synthesizes final reports.

### 6. 🎨 Futuristic Arc Reactor UI (Jetpack Compose)
- **Arc Reactor Orb**: Canvas-animated rotating segmented rings, counter-rotating tick rings, and pulsating luminous core responsive to states (*IDLE*, *LISTENING*, *PROCESSING*, *SPEAKING*).
- **Audio Waveform Visualizer**: Live sine-modulated equalizer bars responding to microphone input amplitude.
- **Screens**:
  - `HomeScreen`: Hero HUD, central Arc Reactor Orb, quick intent pills, push-to-talk mic button.
  - `ChatScreen`: Conversation console with tool execution badges.
  - `TaskScreen`: Filterable task and reminder dashboard with priority indicators.
  - `AgentActivityScreen`: Real-time inspector for autonomous multi-step jobs.
  - `SettingsScreen`: Japanese voice persona fine-tuning (pitch/speed sliders), backend server URL, and permission status.

---

## 🏗️ Architecture Blueprint

```
+-----------------------------------------------------------------------------------------+
|                              ANDROID CLIENT (KOTLIN)                                    |
|                                                                                         |
|  +-----------------------------------------------------------------------------------+  |
|  |                       UI LAYER (Jetpack Compose)                                  |  |
|  |  [Arc Reactor Orb (Pulsing Canvas)]   [Live Waveform Visualizer]                  |  |
|  |  [Futuristic Chat Console]             [Task & Agenda Dashboard]                   |  |
|  |  [Autonomous Agent Pipeline]           [System Configuration & Powers]             |  |
|  +-----------------------------------------------------------------------------------+  |
|                                           |                                             |
|  +-----------------------------------------------------------------------------------+  |
|  |                        VIEWMODELS & ORCHESTRATION (MVVM)                          |  |
|  |  JarvisViewModel | TaskViewModel | DeviceActionController                          |  |
|  +-----------------------------------------------------------------------------------+  |
|                       |                                       |                         |
|  +-------------------------------------+   +-------------------------------------+      |
|  |           VOICE SUBSYSTEM           |   |       DEVICE AUTOMATION LAYER       |      |
|  |  - Multilingual SpeechRecognizer    |   |  - Intent AppLauncher (Installed)   |      |
|  |  - Japanese Female TTS Engine       |   |  - AlarmClock Intent Controller     |      |
|  |  - Real-time Amplitude Extractor    |   |  - Telephony & SMS Draft Dispatcher |      |
|  |  - Pitch / Speech Rate Controller   |   |  - AudioManager Volume Controller   |      |
|  +-------------------------------------+   +-------------------------------------+      |
|                       |                                       |                         |
|  +-----------------------------------------------------------------------------------+  |
|  |                        OFFLINE ENGINE & WORKMANAGER SYNC                          |  |
|  |  - Network State Monitor (ConnectivityManager Callback)                           |  |
|  |  - OfflineBrain: Devanagari & phonetic rule-based slot parser                     |  |
|  |  - Room SQLite Database (Tasks, Memories, Pending Sync Queue)                     |  |
|  |  - SyncWorker: Automatic background bi-directional synchronization                |  |
|  +-----------------------------------------------------------------------------------+  |
+-----------------------------------------------------------------------------------------+
                                 |  ^                         |  ^
                  REST / SSE / WS|  |                         |  | Local Fallback
                                 v  |                         v  |
+--------------------------------------+       +--------------------------------------+
|       FASTAPI BACKEND (PYTHON)       |       |        ON-DEVICE EDGE BRAIN          |
|                                      |       |                                      |
| - FastAPI Router & Security          |       | - Deterministic Multilingual Parser  |
| - Gemini Pro/Flash Reasoning API     |       | - Offline Speech-to-Text             |
| - Function / Tool Calling Engine     |       | - Offline Android TTS                |
| - Multilingual Voice Service         |       | - Local Task & Calendar CRUD         |
| - Task Manager & Conflict Resolver   |       | - Pending Sync Persistence Queue     |
| - Autonomous Multi-Step Agent        |       +--------------------------------------+
| - Async SQLite Database & Memories   |
+--------------------------------------+
```

---

## 🚀 Getting Started

### 1. Prerequisites
- **Python**: 3.10+
- **Android Studio**: Ladybug / Koala or newer
- **Java**: JDK 17+
- **Android Device / Emulator**: Android 8.0+ (API 26+)

---

### 2. Backend Setup
```bash
# Clone the repository
git clone https://github.com/shubhaammm08/My-Assistant.git
cd My-Assistant/backend

# Install dependencies
pip install -r requirements.txt

# (Optional) Add your Gemini API key in .env
cp .env.example .env
# Edit .env and set GEMINI_API_KEY=your_key_here

# Start the server
python run_server.py
```
*Backend runs on `http://localhost:8000` (API docs at `http://localhost:8000/docs`).*

To run automated backend tests:
```bash
python -m pytest tests -v -o pythonpath=.
```

---

### 3. Android Client Setup
1. Open Android Studio.
2. Select **Open** and choose the `android` folder in this repository.
3. Allow Gradle to sync.
4. Connect an Android phone with USB Debugging enabled or start an emulator.
5. In the JARVIS Settings screen on your phone, verify the backend URL (`http://10.0.2.2:8000/` for emulators or `http://192.168.1.X:8000/` for physical devices on your Wi-Fi).
6. Press **Run** (`Shift + F10`) to deploy JARVIS directly to your phone.

---

## 🛠️ Reusable Antigravity Skill
This repository includes a reusable Antigravity skill located at [`.agents/skills/jarvis-engineering`](./.agents/skills/jarvis-engineering):
- `SKILL.md`: Core agent instructions and development standards.
- `references/architecture.md`: Full architectural blueprint.
- `references/development_guidelines.md`: Coding standards and multilingual handling protocol.
- `references/testing_guidelines.md`: Test strategies and validation checklists.
- `references/api_documentation.md`: Tool definitions and schema specs.

---

## 📄 License
MIT License. Developed with pride by the JARVIS Engineering Team.
