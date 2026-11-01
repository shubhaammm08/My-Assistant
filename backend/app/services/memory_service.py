from typing import List, Optional, Dict, Any
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
import uuid

from app.models.db_models import MemoryModel, get_utc_now

class MemoryService:
    async def set_memory(
        self,
        session: AsyncSession,
        key: str,
        value: str,
        category: str = "user_preference"
    ) -> Dict[str, Any]:
        result = await session.execute(select(MemoryModel).where(MemoryModel.key == key))
        mem = result.scalar_one_or_none()
        if mem:
            mem.value = value
            mem.category = category
            mem.updated_at = get_utc_now()
        else:
            mem = MemoryModel(
                id=str(uuid.uuid4()),
                key=key,
                value=value,
                category=category,
                created_at=get_utc_now(),
                updated_at=get_utc_now()
            )
            session.add(mem)
        
        await session.commit()
        await session.refresh(mem)
        return {"key": mem.key, "value": mem.value, "category": mem.category}

    async def get_memory(self, session: AsyncSession, key: str) -> Optional[str]:
        result = await session.execute(select(MemoryModel).where(MemoryModel.key == key))
        mem = result.scalar_one_or_none()
        return mem.value if mem else None

    async def list_memories(
        self,
        session: AsyncSession,
        category: Optional[str] = None
    ) -> List[Dict[str, Any]]:
        query = select(MemoryModel)
        if category:
            query = query.where(MemoryModel.category == category)
        result = await session.execute(query)
        memories = result.scalars().all()
        return [
            {"key": m.key, "value": m.value, "category": m.category, "updated_at": m.updated_at.isoformat()}
            for m in memories
        ]

memory_service = MemoryService()

