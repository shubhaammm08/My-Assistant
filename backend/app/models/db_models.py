from sqlalchemy import Column, String, Text, DateTime, Enum as SAEnum
from sqlalchemy.orm import declarative_base
from datetime import datetime, timezone
import uuid
from app.models.schemas import PriorityEnum, TaskStatusEnum, AgentJobStatus

Base = declarative_base()

def get_utc_now():
    return datetime.now(timezone.utc)

class TaskModel(Base):
    __tablename__ = "tasks"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    title = Column(String(255), nullable=False)
    description = Column(Text, nullable=True)
    due_date = Column(String(50), nullable=True)
    priority = Column(String(20), default=PriorityEnum.MEDIUM.value)
    status = Column(String(20), default=TaskStatusEnum.PENDING.value)
    recurrence = Column(String(50), nullable=True)
    category = Column(String(50), default="general")
    created_at = Column(DateTime, default=get_utc_now)
    updated_at = Column(DateTime, default=get_utc_now, onupdate=get_utc_now)

class MemoryModel(Base):
    __tablename__ = "memories"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    key = Column(String(100), unique=True, index=True, nullable=False)
    value = Column(Text, nullable=False)
    category = Column(String(50), default="user_preference")
    created_at = Column(DateTime, default=get_utc_now)
    updated_at = Column(DateTime, default=get_utc_now, onupdate=get_utc_now)

class ConversationModel(Base):
    __tablename__ = "conversations"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    session_id = Column(String(100), index=True, default="default-session")
    role = Column(String(20), nullable=False) # 'user', 'assistant', 'system'
    content = Column(Text, nullable=False)
    detected_language = Column(String(20), default="en")
    tool_calls_json = Column(Text, nullable=True)
    timestamp = Column(DateTime, default=get_utc_now)

class AgentJobModel(Base):
    __tablename__ = "agent_jobs"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    objective = Column(Text, nullable=False)
    status = Column(String(30), default=AgentJobStatus.QUEUED.value)
    steps_json = Column(Text, default="[]")
    final_report = Column(Text, nullable=True)
    created_at = Column(DateTime, default=get_utc_now)
    updated_at = Column(DateTime, default=get_utc_now, onupdate=get_utc_now)

