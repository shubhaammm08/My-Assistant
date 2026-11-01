from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession
from typing import List, Optional
from datetime import datetime, timezone

from app.db.database import get_db
from app.models.schemas import (
    TaskCreate, TaskUpdate, TaskResponse, TaskSyncRequest, TaskSyncResponse, TaskStatusEnum
)
from app.services.task_manager import task_manager

router = APIRouter(prefix="/api/tasks", tags=["Task Management"])

@router.get("", response_model=List[TaskResponse])
async def list_tasks(
    status: Optional[TaskStatusEnum] = None,
    db: AsyncSession = Depends(get_db)
):
    return await task_manager.get_tasks(db, status=status)

@router.post("", response_model=TaskResponse)
async def create_task(task_in: TaskCreate, db: AsyncSession = Depends(get_db)):
    return await task_manager.create_task(db, task_in)

@router.get("/{task_id}", response_model=TaskResponse)
async def get_task(task_id: str, db: AsyncSession = Depends(get_db)):
    task = await task_manager.get_task_by_id(db, task_id)
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")
    return task

@router.patch("/{task_id}", response_model=TaskResponse)
async def update_task(task_id: str, task_in: TaskUpdate, db: AsyncSession = Depends(get_db)):
    task = await task_manager.update_task(db, task_id, task_in)
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")
    return task

@router.delete("/{task_id}")
async def delete_task(task_id: str, db: AsyncSession = Depends(get_db)):
    success = await task_manager.delete_task(db, task_id)
    if not success:
        raise HTTPException(status_code=404, detail="Task not found")
    return {"status": "deleted", "id": task_id}

@router.post("/sync", response_model=TaskSyncResponse)
async def sync_tasks(payload: TaskSyncRequest, db: AsyncSession = Depends(get_db)):
    merged = await task_manager.sync_client_tasks(db, payload.client_tasks)
    return TaskSyncResponse(
        synced_tasks=merged,
        server_timestamp=datetime.now(timezone.utc).isoformat()
    )

