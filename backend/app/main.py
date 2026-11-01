from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import logging

from app.config import settings
from app.db.database import init_db
from app.api.routes_chat import router as chat_router
from app.api.routes_tasks import router as task_router
from app.api.routes_agent import router as agent_router
from app.api.routes_voice import router as voice_router

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("jarvis")

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Initializing JARVIS Database...")
    await init_db()
    logger.info("JARVIS Database initialized successfully.")
    yield
    logger.info("JARVIS Backend shutting down gracefully.")

app = FastAPI(
    title=settings.APP_NAME,
    version=settings.VERSION,
    description="Backend API and Autonomous AI Agent brain for JARVIS Personal AI Assistant.",
    lifespan=lifespan
)

# Enable CORS for Android client access
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include Routers
app.include_router(chat_router)
app.include_router(task_router)
app.include_router(agent_router)
app.include_router(voice_router)

@app.get("/")
async def root():
    return {
        "status": "online",
        "system": "JARVIS AI Assistant Core",
        "version": settings.VERSION,
        "persona": settings.VOICE_PERSONA,
        "endpoints": {
            "chat": "/api/chat",
            "tasks": "/api/tasks",
            "agent": "/api/agent/jobs",
            "voice": "/api/voice/profiles",
            "docs": "/docs"
        }
    }

@app.get("/api/health")
async def health_check():
    return {
        "status": "healthy",
        "gemini_connected": bool(settings.GEMINI_API_KEY),
        "database": "sqlite_async",
        "capabilities": [
            "multilingual_reasoning",
            "japanese_female_voice",
            "phone_automation_tools",
            "task_manager",
            "autonomous_research_agent",
            "offline_fallback_intelligence"
        ]
    }

