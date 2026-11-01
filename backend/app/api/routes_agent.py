from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.db.database import get_db
from app.models.schemas import AgentJobSubmit, AgentJobResponse
from app.services.autonomous_agent import autonomous_agent

router = APIRouter(prefix="/api/agent", tags=["Autonomous AI Agent"])

@router.post("/jobs", response_model=AgentJobResponse)
async def submit_job(job_in: AgentJobSubmit, db: AsyncSession = Depends(get_db)):
    if not job_in.objective.strip():
        raise HTTPException(status_code=400, detail="Objective cannot be empty.")
    return await autonomous_agent.submit_job(db, job_in)

@router.get("/jobs/{job_id}", response_model=AgentJobResponse)
async def get_job_status(job_id: str, db: AsyncSession = Depends(get_db)):
    job = await autonomous_agent.get_job(db, job_id)
    if not job:
        raise HTTPException(status_code=404, detail="Job not found")
    return job

