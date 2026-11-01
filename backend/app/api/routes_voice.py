from fastapi import APIRouter
from pydantic import BaseModel
from typing import Dict, Any, List

from app.services.voice_service import voice_service
from app.models.schemas import VoiceSettings, VoiceProfileEnum

router = APIRouter(prefix="/api/voice", tags=["Voice & Personality"])

class DetectLanguageRequest(BaseModel):
    text: str

class DetectLanguageResponse(BaseModel):
    text: str
    detected_language: str
    recommended_settings: VoiceSettings

@router.get("/profiles")
async def get_voice_profiles() -> Dict[str, Any]:
    return {
        "active_persona": "Japanese Female AI Assistant",
        "supported_languages": [
            {"code": "en", "name": "English (Japanese-accented female cadence)", "locale": "en-US"},
            {"code": "hi", "name": "Hindi / Hinglish (Polite soft female)", "locale": "hi-IN"},
            {"code": "mr", "name": "Marathi / Marathi-English (Clear pleasant female)", "locale": "mr-IN"},
            {"code": "ja", "name": "Japanese (Native polite Desu/Masu)", "locale": "ja-JP"}
        ],
        "default_pitch": 1.22,
        "default_speed": 1.0,
        "profiles": [
            {
                "id": "japanese_female",
                "label": "JARVIS Japanese Female (Default)",
                "description": "Soft, pleasant, intelligent, slightly playful, polite tone with Japanese inflection."
            },
            {
                "id": "professional",
                "label": "Professional Secretary",
                "description": "Concise, articulate, formal tone."
            }
        ]
    }

@router.post("/detect", response_model=DetectLanguageResponse)
async def detect_language_endpoint(payload: DetectLanguageRequest):
    lang = voice_service.detect_language(payload.text)
    settings = voice_service.get_voice_settings(lang, VoiceProfileEnum.JAPANESE_FEMALE)
    return DetectLanguageResponse(
        text=payload.text,
        detected_language=lang,
        recommended_settings=settings
    )

