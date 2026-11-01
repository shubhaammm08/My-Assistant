from typing import List, Optional, Dict, Any
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import update, delete
from datetime import datetime, timezone
import uuid

from app.models.db_models import TaskModel, get_utc_now
from app.models.schemas import TaskCreate, TaskUpdate, TaskResponse, TaskStatusEnum, PriorityEnum

class TaskManager:
    async def create_task(self, session: AsyncSession, task_in: TaskCreate) -> TaskResponse:
        db_task = TaskModel(
            id=str(uuid.uuid4()),
            title=task_in.title,
            description=task_in.description,
            due_date=task_in.due_date,
            priority=task_in.priority.value,
            status=TaskStatusEnum.PENDING.value,
            recurrence=task_in.recurrence,
            category=task_in.category or "general",
            created_at=get_utc_now(),
            updated_at=get_utc_now()
        )
        session.add(db_task)
        await session.commit()
        await session.refresh(db_task)
        return self._to_response(db_task)

    async def get_tasks(
        self,
        session: AsyncSession,
        status: Optional[TaskStatusEnum] = None
    ) -> List[TaskResponse]:
        query = select(TaskModel)
        if status:
            query = query.where(TaskModel.status == status.value)
        query = query.order_by(TaskModel.created_at.desc())
        result = await session.execute(query)
        tasks = result.scalars().all()
        return [self._to_response(t) for t in tasks]

    async def get_task_by_id(self, session: AsyncSession, task_id: str) -> Optional[TaskResponse]:
        result = await session.execute(select(TaskModel).where(TaskModel.id == task_id))
        task = result.scalar_one_or_none()
        return self._to_response(task) if task else None

    async def update_task(
        self,
        session: AsyncSession,
        task_id: str,
        task_in: TaskUpdate
    ) -> Optional[TaskResponse]:
        result = await session.execute(select(TaskModel).where(TaskModel.id == task_id))
        db_task = result.scalar_one_or_none()
        if not db_task:
            return None

        update_data = task_in.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            if hasattr(db_task, key):
                setattr(db_task, key, value.value if hasattr(value, "value") else value)
        
        db_task.updated_at = get_utc_now()
        await session.commit()
        await session.refresh(db_task)
        return self._to_response(db_task)

    async def delete_task(self, session: AsyncSession, task_id: str) -> bool:
        result = await session.execute(select(TaskModel).where(TaskModel.id == task_id))
        db_task = result.scalar_one_or_none()
        if not db_task:
            return False
        await session.delete(db_task)
        await session.commit()
        return True

    async def sync_client_tasks(
        self,
        session: AsyncSession,
        client_tasks: List[TaskResponse]
    ) -> List[TaskResponse]:
        """
        Merges tasks from the Android Room client with server tasks.
        Uses latest updated_at timestamp to resolve conflicts.
        """
        for ct in client_tasks:
            result = await session.execute(select(TaskModel).where(TaskModel.id == ct.id))
            server_task = result.scalar_one_or_none()
            if not server_task:
                # Insert client task to server
                new_task = TaskModel(
                    id=ct.id,
                    title=ct.title,
                    description=ct.description,
                    due_date=ct.due_date,
                    priority=ct.priority.value,
                    status=ct.status.value,
                    recurrence=ct.recurrence,
                    category=ct.category,
                    created_at=datetime.fromisoformat(ct.created_at) if ct.created_at else get_utc_now(),
                    updated_at=datetime.fromisoformat(ct.updated_at) if ct.updated_at else get_utc_now()
                )
                session.add(new_task)
            else:
                # Merge if client task is newer
                client_dt = datetime.fromisoformat(ct.updated_at) if ct.updated_at else datetime.min
                if server_task.updated_at is None or client_dt > server_task.updated_at:
                    server_task.title = ct.title
                    server_task.description = ct.description
                    server_task.due_date = ct.due_date
                    server_task.priority = ct.priority.value
                    server_task.status = ct.status.value
                    server_task.recurrence = ct.recurrence
                    server_task.category = ct.category
                    server_task.updated_at = client_dt

        await session.commit()
        # Return all merged tasks
        return await self.get_tasks(session)

    def _to_response(self, model: TaskModel) -> TaskResponse:
        return TaskResponse(
            id=model.id,
            title=model.title,
            description=model.description,
            due_date=model.due_date,
            priority=PriorityEnum(model.priority) if model.priority in PriorityEnum._value2member_map_ else PriorityEnum.MEDIUM,
            status=TaskStatusEnum(model.status) if model.status in TaskStatusEnum._value2member_map_ else TaskStatusEnum.PENDING,
            recurrence=model.recurrence,
            category=model.category,
            created_at=model.created_at.isoformat() if model.created_at else get_utc_now().isoformat(),
            updated_at=model.updated_at.isoformat() if model.updated_at else get_utc_now().isoformat()
        )

task_manager = TaskManager()

