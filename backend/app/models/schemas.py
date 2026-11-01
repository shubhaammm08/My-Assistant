from pydantic import BaseModel, Field
from typing import List, Dict, Any, Optional
from datetime import datetime
from enum import Enum

class PriorityEnum(str, Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    URGENT = "URGENT"

class TaskStatusEnum(str, Enum):
    PENDING = "PENDING"
    IN_PROGRESS = "IN_PROGRESS"
    COMPLETED = "COMPLETED"
    CANCELLED = "CANCELLED"

class AgentJobStatus(str, Enum):
    PENDING = "PENDING"
    QUEUED = "QUEUED"
    PLANNING = "PLANNING"
    EXECUTING = "EXECUTING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    WAITING_FOR_CONFIRMATION = "WAITING_FOR_CONFIRMATION"

class VoiceProfileEnum(str, Enum):
    JAPANESE_FEMALE = "japanese_female"
    PROFESSIONAL = "professional"
    CASUAL = "casual"

class VoiceSettings(BaseModel):
    pitch: float = Field(default=1.2, description="Pitch multiplier (e.g. 1.2 for soft female tone)")
    speed: float = Field(default=1.0, description="Speech rate multiplier")
    volume: float = Field(default=1.0, description="Volume level from 0.0 to 1.0")
    locale: str = Field(default="en-US", description="BCP 47 language tag for TTS engine")
    voice_name: str = Field(default="ja-jp-x-female-soft", description="Specific voice identifier")
    accent_influence: str = Field(default="japanese_subtle", description="Voice accent styling")

class ToolCall(BaseModel):
    call_id: Optional[str] = None
    tool: str
    arguments: Dict[str, Any]

class ChatRequest(BaseModel):
    message: str
    session_id: Optional[str] = "default-session"
    user_locale: Optional[str] = "en-US"
    voice_profile: Optional[VoiceProfileEnum] = VoiceProfileEnum.JAPANESE_FEMALE
    client_capabilities: Optional[List[str]] = Field(
        default_factory=lambda: [
            "open_app", "set_alarm", "set_timer", "create_task", 
            "make_phone_call", "draft_sms", "adjust_volume", "search_web"
        ]
    )

class ChatResponse(BaseModel):
    reply_text: str
    detected_language: str
    voice_settings: VoiceSettings
    tool_calls: List[ToolCall] = Field(default_factory=list)
    requires_confirmation: bool = False
    confirmation_action: Optional[str] = None
    session_id: str

class TaskCreate(BaseModel):
    title: str
    description: Optional[str] = None
    due_date: Optional[str] = None  # ISO format string or local readable
    priority: PriorityEnum = PriorityEnum.MEDIUM
    recurrence: Optional[str] = None
    category: Optional[str] = "general"

class TaskUpdate(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    due_date: Optional[str] = None
    priority: Optional[PriorityEnum] = None
    status: Optional[TaskStatusEnum] = None
    recurrence: Optional[str] = None
    category: Optional[str] = None

class TaskResponse(BaseModel):
    id: str
    title: str
    description: Optional[str] = None
    due_date: Optional[str] = None
    priority: PriorityEnum
    status: TaskStatusEnum
    recurrence: Optional[str] = None
    category: str
    created_at: str
    updated_at: str

class TaskSyncRequest(BaseModel):
    client_tasks: List[TaskResponse]
    last_sync_timestamp: Optional[str] = None

class TaskSyncResponse(BaseModel):
    synced_tasks: List[TaskResponse]
    server_timestamp: str

class AgentJobSubmit(BaseModel):
    objective: str
    context: Optional[Dict[str, Any]] = None
    require_user_approval: bool = True

class AgentJobStep(BaseModel):
    step_number: int
    description: str
    tool_to_use: Optional[str] = None
    tool_arguments: Optional[Dict[str, Any]] = None
    status: AgentJobStatus = AgentJobStatus.PENDING
    result: Optional[str] = None

class AgentJobResponse(BaseModel):
    job_id: str
    objective: str
    status: AgentJobStatus
    steps: List[AgentJobStep] = Field(default_factory=list)
    final_report: Optional[str] = None
    created_at: str
    updated_at: str
