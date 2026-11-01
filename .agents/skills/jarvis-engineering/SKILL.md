---
name: jarvis-engineering
description: >-
  Elite engineering guidelines, architectural blueprint, development workflows, and quality standards
  for building, maintaining, and extending the JARVIS Advanced Personal AI Operating Assistant (Android + FastAPI Backend + Edge/Offline AI).
---

# JARVIS Engineering Skill

This skill governs the end-to-end development, architecture, testing, and operation of **JARVIS** – an advanced personal AI operating assistant featuring a Jetpack Compose Android client, a high-performance Python FastAPI backend, Gemini-powered multilingual reasoning, on-device offline intelligence, Japanese-style multilingual voice synthesis, phone automation, and autonomous task execution.

## Core Responsibilities

1. **Understand JARVIS Architecture**: Master the interplay between the Android client (Jetpack Compose, Room, WorkManager, Coroutines), the Python backend (FastAPI, Gemini API, SQLite/PostgreSQL, Async workers), and local edge intelligence.
2. **Follow Strict Coding Conventions**: Enforce clean architecture, MVVM on Android, domain-driven service layers on backend, and robust type safety.
3. **Build Android Features**: Native Android Kotlin features with Jetpack Compose, Room persistence, WorkManager background tasks, intents, and permissions.
4. **Build Backend APIs**: FastAPI endpoints with Pydantic validation, async execution, streaming SSE/WebSocket responses, and tool calling.
5. **Implement AI Tool Calling**: Deterministic, structured tool calling for device actions, task manipulation, communications, and research.
6. **Implement Voice Features**: Multilingual Speech-to-Text (STT) and Japanese female-style Text-to-Speech (TTS) with configurable pitch, speed, and emotion.
7. **Implement Multilingual Functionality**: Real-time cross-language comprehension and synthesis across English, Hindi, Marathi, Japanese, and mixed colloquial speech (Hinglish, Marathi-English).
8. **Implement Offline Functionality**: On-device speech recognition, local speech synthesis, rule/local model fallback, offline task creation, and automatic bi-directional sync upon reconnect.
9. **Integrate Android Phone Controls**: Real Android APIs for app launching, alarms, contacts, telephony, messaging drafts, volume, and notification management.
10. **Build Autonomous Agents**: Multi-step goal decomposition, subtask scheduling, persistent state execution, verification, and human-in-the-loop approvals.
11. **Perform Testing & Security**: Unit testing, API validation, device integration testing, secret protection, permission auditing, and rate limiting.
12. **Update Technical Documentation**: Continuously keep `JARVIS_PROGRESS.md` and architecture references up to date.

---

## Skill Instructions & Workflow

Whenever working on JARVIS:

1. **Inspect Before Editing**: Read existing code, schema definitions, and configs. Never assume empty state without checking.
2. **Respect Existing Architecture**: Extend established patterns (Repository pattern in Android, Service/Router pattern in FastAPI).
3. **Never Rewrite Working Code Unnecessarily**: Prefer additive, non-breaking modifications and well-tested refactoring.
4. **Implement Real Functionality**: Every tool, button, and command must connect to real operational logic or actual system APIs. Never provide fake mocks or placeholder toasts as completed features.
5. **Add Comprehensive Error Handling**: Always catch platform exceptions (e.g., `ActivityNotFoundException`, `SecurityException`, network timeouts, JSON parsing errors) and provide graceful, friendly user feedback.
6. **Add Automated Tests**: Provide unit and integration test scripts for all backend routes, agent planning steps, and Android logic.
7. **Verify Implementation**: Execute tests and run code verification before declaring any task complete.
8. **Update Progress Documentation**: Log all changes in `JARVIS_PROGRESS.md`.
9. **Report Transparently**: State exactly what was implemented, what was verified, and what remains pending or requires user action.

---

## Mandatory Quality Rules

* **Zero Placeholder Implementations**: No `// TODO: Implement later` or stub buttons pretending to be functional.
* **No Hardcoded AI Replies**: All AI responses must flow through the AI engine (online Gemini or offline fallback engine).
* **Zero Credential Exposure**: Never commit API keys or hardcode secrets in code. Use `.env` or system environment variables.
* **Verified State Transitions**: Background tasks and multi-step jobs must persist state and report real verification.
* **Safe Operations**: Sensitive operations (sending SMS/messages, deleting files, placing calls) must request confirmation before dispatch.
* **Android Background Restrictions**: Strictly follow Android Doze, battery optimization, and WorkManager guidelines.

---

## References & Deep Dives

* [Architecture Blueprint](./references/architecture.md)
* [Development Guidelines & Conventions](./references/development_guidelines.md)
* [Testing & Verification Guidelines](./references/testing_guidelines.md)
* [API & Tool Calling Specification](./references/api_documentation.md)

