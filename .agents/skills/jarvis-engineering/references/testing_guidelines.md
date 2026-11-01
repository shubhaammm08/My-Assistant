# JARVIS Testing & Verification Guidelines

## 1. Automated Testing Strategy

### A. Python Backend Tests
* **Framework**: `pytest`, `pytest-asyncio`, `httpx`.
* **Coverage Requirements**:
  * Health check and configuration endpoints.
  * Chat & reasoning endpoints (with mock & live tests).
  * Tool calling execution and validation.
  * Task CRUD endpoints and synchronization mechanics.
  * Agent planner step decomposition and task state transitions.

### B. Android Unit & Instrumentation Tests
* **Unit Tests**:
  * ViewModels: Verify state transitions on chat input, STT events, and network status changes.
  * Repositories: Room DAO tests using in-memory SQLite database.
  * Offline Intent Matcher: Verify regex/grammar parser on English, Hindi, and Marathi inputs ("Kal 8 baje uthana", "मला उद्या सकाळी आठ वाजता उठव", etc.).
* **Device / Robolectric Tests**:
  * Intent generation: Verify `ACTION_SET_ALARM`, `ACTION_DIAL`, `ACTION_SENDTO` parameters.
  * TTS Engine locale resolution.

---

## 2. Manual Verification Checklist

1. **Voice Verification**:
   * Speak in English: Confirm response in English with polite Japanese voice inflection.
   * Speak in Hindi: Confirm Hindi reply and `hi-IN` TTS synthesis.
   * Speak in Marathi: Confirm Marathi reply and `mr-IN` TTS synthesis.
2. **Device Action Verification**:
   * Say: *"Open YouTube"* -> Confirm app launch intent triggered.
   * Say: *"Set an alarm for 7:30 AM"* -> Confirm alarm intent or system alarm scheduled.
   * Say: *"Remind me to call John tomorrow at 5 PM"* -> Confirm task created locally with correct epoch timestamp.
3. **Offline Fallback Verification**:
   * Disable network -> Say: *"Turn on flashlight"* or *"Create task buy groceries"* -> Confirm handled locally.
   * Say: *"Research quantum computing trends"* -> Confirm queued with friendly offline advisory.
   * Re-enable network -> Confirm queued tasks trigger synchronization.

