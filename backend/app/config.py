import os
from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import Optional

class Settings(BaseSettings):
    APP_NAME: str = "JARVIS Personal AI Operating Assistant"
    VERSION: str = "1.0.0"
    DEBUG: bool = True
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    
    # Gemini API Key (loaded from environment or .env)
    GEMINI_API_KEY: Optional[str] = os.getenv("GEMINI_API_KEY", "")
    
    # Database
    DATABASE_URL: str = "sqlite+aiosqlite:///./jarvis.db"
    
    # Voice Persona
    VOICE_PERSONA: str = "japanese_female"
    DEFAULT_LOCALE: str = "en-US"
    
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

settings = Settings()
