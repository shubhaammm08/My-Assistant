import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app

@pytest.mark.asyncio
async def test_root_endpoint():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get("/")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "online"
    assert "JARVIS" in data["system"]

@pytest.mark.asyncio
async def test_health_endpoint():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get("/api/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert "multilingual_reasoning" in data["capabilities"]

@pytest.mark.asyncio
async def test_chat_multilingual_hindi():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        payload = {
            "message": "Kal mujhe 8 baje uthana",
            "session_id": "test-session",
            "user_locale": "hi-IN"
        }
        response = await ac.post("/api/chat", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["detected_language"] in ["hi", "hinglish"]
    assert len(data["tool_calls"]) > 0
    assert any(tc["tool"] == "set_alarm" for tc in data["tool_calls"])
    assert data["voice_settings"]["locale"] == "hi-IN"

@pytest.mark.asyncio
async def test_chat_multilingual_marathi():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        payload = {
            "message": "मला उद्या सकाळी आठ वाजता उठव",
            "session_id": "test-session",
            "user_locale": "mr-IN"
        }
        response = await ac.post("/api/chat", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["detected_language"] == "mr"
    assert len(data["tool_calls"]) > 0
    assert any(tc["tool"] == "set_alarm" for tc in data["tool_calls"])
    assert data["voice_settings"]["locale"] == "mr-IN"

@pytest.mark.asyncio
async def test_chat_english_with_japanese_persona():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        payload = {
            "message": "Jarvis, open YouTube please",
            "session_id": "test-session"
        }
        response = await ac.post("/api/chat", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert any(tc["tool"] == "open_application" for tc in data["tool_calls"])
    assert data["voice_settings"]["accent_influence"] == "japanese_accented_english"

