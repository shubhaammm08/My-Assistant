from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
import uuid

from app.db.database import get_db
from app.models.schemas import ChatRequest, ChatResponse, VoiceProfileEnum
from app.services.gemini_service import gemini_service
from app.services.voice_service import voice_service
from app.models.db_models import ConversationModel, get_utc_now

router = APIRouter(prefix="/api", tags=["Chat & Reasoning"])

@router.post("/chat", response_model=ChatResponse)
async def chat_endpoint(request: ChatRequest, db: AsyncSession = Depends(get_db)):
    if not request.message.strip():
        raise HTTPException(status_code=400, detail="Message cannot be empty.")

    # 1. Log incoming user message
    user_entry = ConversationModel(
        id=str(uuid.uuid4()),
        session_id=request.session_id or "default",
        role="user",
        content=request.message,
        timestamp=get_utc_now()
    )
    db.add(user_entry)
    await db.commit()

    # 2. Process message via Gemini or local semantic fallback
    result = await gemini_service.process_message(
        message=request.message,
        session_id=request.session_id or "default",
        user_locale=request.user_locale or "en-US"
    )

    detected_lang = result["detected_language"]
    voice_settings = voice_service.get_voice_settings(
        detected_lang=detected_lang,
        profile=request.voice_profile or VoiceProfileEnum.JAPANESE_FEMALE
    )

    # 3. Log assistant reply
    asst_entry = ConversationModel(
        id=str(uuid.uuid4()),
        session_id=request.session_id or "default",
        role="assistant",
        content=result["reply_text"],
        detected_language=detected_lang,
        timestamp=get_utc_now()
    )
    db.add(asst_entry)
    await db.commit()

    return ChatResponse(
        reply_text=result["reply_text"],
        detected_language=detected_lang,
        voice_settings=voice_settings,
        tool_calls=result["tool_calls"],
        requires_confirmation=result["requires_confirmation"],
        confirmation_action="make_phone_call" if result["requires_confirmation"] else None,
        session_id=request.session_id or "default"
    )

