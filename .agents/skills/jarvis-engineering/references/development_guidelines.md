# JARVIS Development Guidelines & Coding Standards

## 1. Android Engineering Standards (Kotlin & Jetpack Compose)

* **Architecture**: Unidirectional Data Flow (UDF) with MVVM.
  * State is held in Kotlin `StateFlow` within ViewModels.
  * Composables are stateless or hoist state cleanly.
* **Coroutines & Thread Safety**:
  * Network/IO operations on `Dispatchers.IO`.
  * Database operations wrapped in Room DAO suspend functions or `Flow`.
  * UI state changes strictly dispatched on `Dispatchers.Main`.
* **Futuristic UI Style**:
  * Dark theme: deep blacks (`#0A0E17`), cyan/neon accents (`#00F2FE`, `#4FACFE`), electric purple highlights (`#7F00FF`).
  * Smooth animations using Compose `animateFloatAsState`, `Animatable`, and canvas-based glowing arcs.
* **Permissions Protocol**:
  * Always verify permissions before calling sensitive APIs (`RECORD_AUDIO`, `READ_CONTACTS`, `CALL_PHONE`, `POST_NOTIFICATIONS`).
  * Gracefully degrade if permission is denied, explaining why it is needed.

---

## 2. Python Backend Standards (FastAPI & Gemini)

* **Framework**: FastAPI with asynchronous route handlers (`async def`).
* **Validation**: Strict Pydantic v2 schemas for all request payloads and responses.
* **Gemini Client**:
  * Use official `google-genai` SDK or `google-generativeai`.
  * Pass structured system prompts that maintain the Japanese female personality and multilingual awareness.
  * Implement Function/Tool Calling for device commands, task actions, and web lookups.
* **Persistence & Job Queue**:
  * SQLite / PostgreSQL for task and conversation state.
  * Async background workers (`asyncio.create_task` or Celery/Redis queue for heavy tasks).
* **Security**:
  * API keys and secrets stored strictly in environment variables.
  * Rate limiting and CORS middleware configured securely.

---

## 3. Multilingual Handling Protocol

* Every text incoming from STT is processed with automatic language detection.
* The system prompt instructs Gemini to detect user language (English, Hindi, Marathi, Japanese, or mixed Hinglish/Marathi-English) and reply in the matching language while maintaining polite Japanese conversational nuance where requested.
* Voice synthesizer sets locale appropriate to the response text:
  * English responses -> Japanese-accented female English voice (e.g. `ja_JP` with English phonetic or high-quality English voice with tuned pitch/speed).
  * Hindi responses -> `hi_IN` female voice.
  * Marathi responses -> `mr_IN` female voice.
  * Japanese responses -> `ja_JP` female voice.

