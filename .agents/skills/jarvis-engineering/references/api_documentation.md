# JARVIS API & Tool Calling Specification

## 1. REST Endpoints

### Health & Capabilities
* `GET /api/health`: Returns service health, online model status, and agent readiness.
* `GET /api/capabilities`: Lists available tools, voice engines, and supported languages.

### Conversational & Reasoning API
* `POST /api/chat`:
  * **Payload**:
    ```json
    {
      "message": "Kal mujhe subah 8 baje uthana",
      "session_id": "session-123",
      "user_locale": "hi-IN",
      "voice_profile": "japanese_female",
      "client_capabilities": ["open_app", "set_alarm", "create_task", "send_sms"]
    }
    ```
  * **Response**:
    ```json
    {
      "reply_text": "Zaroor! Maine kal subah 8:00 AM ke liye alarm aur reminder set kar diya hai.",
      "detected_language": "hi",
      "voice_settings": {
        "pitch": 1.15,
        "speed": 1.0,
        "locale": "hi-IN"
      },
      "tool_calls": [
        {
          "tool": "set_alarm",
          "arguments": {
            "hour": 8,
            "minute": 0,
            "message": "Subah uthana"
          }
        },
        {
          "tool": "create_task",
          "arguments": {
            "title": "Subah uthana",
            "due_date": "2026-11-02T08:00:00+05:30",
            "priority": "HIGH"
          }
        }
      ],
      "requires_confirmation": false
    }
    ```

### Task Synchronization
* `GET /api/tasks`: Retrieve all cloud-synced tasks.
* `POST /api/tasks`: Create or update a task.
* `POST /api/tasks/sync`: Bulk synchronization between Android local Room DB and Cloud store.

### Autonomous Agent Jobs
* `POST /api/agent/jobs`: Submit a multi-step research or execution objective.
* `GET /api/agent/jobs/{job_id}`: Stream or poll execution status, subtasks, and outputs.

---

## 2. Standard Tool Calling Definitions

JARVIS exposes deterministic tool schemas to Gemini:
* `set_alarm(hour: int, minute: int, message: str)`
* `set_timer(seconds: int, label: str)`
* `open_application(app_name: str, package_name: Optional[str])`
* `make_phone_call(contact_name: str, phone_number: str)`
* `draft_sms(contact_name: str, phone_number: str, message: str)`
* `create_task(title: str, due_date: Optional[str], priority: str)`
* `search_web(query: str)`
* `save_memory(key: str, value: str, category: str)`

