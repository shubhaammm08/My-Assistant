import asyncio
import sys
import httpx
from app.main import app
from app.db.database import init_db

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")

async def test_live():
    await init_db()
    async with httpx.AsyncClient(transport=httpx.ASGITransport(app=app), base_url="http://test") as client:
        r = await client.get("/api/health")
        print("HEALTH CHECK:", r.status_code, r.json())

        # Test Hindi Alarm Command
        r_chat = await client.post("/api/chat", json={
            "message": "Kal mujhe 8 baje uthana",
            "session_id": "live-test"
        })
        print("\nHINDI CHAT RESPONSE:", r_chat.status_code)
        print("Reply:", r_chat.json()["reply_text"])
        print("Voice Settings:", r_chat.json()["voice_settings"])
        print("Tool Calls:", r_chat.json()["tool_calls"])

        # Test Marathi Command
        r_mr = await client.post("/api/chat", json={
            "message": "मला उद्या सकाळी आठ वाजता उठव",
            "session_id": "live-test"
        })
        print("\nMARATHI CHAT RESPONSE:", r_mr.status_code)
        print("Reply:", r_mr.json()["reply_text"])
        print("Voice Settings:", r_mr.json()["voice_settings"])
        print("Tool Calls:", r_mr.json()["tool_calls"])

        # Test English App Command
        r_en = await client.post("/api/chat", json={
            "message": "Open YouTube",
            "session_id": "live-test"
        })
        print("\nENGLISH APP COMMAND:", r_en.status_code)
        print("Reply:", r_en.json()["reply_text"])
        print("Tool Calls:", r_en.json()["tool_calls"])

        # Test Task Creation
        r_task = await client.post("/api/tasks", json={
            "title": "Build JARVIS Android Client",
            "priority": "URGENT"
        })
        print("\nTASK CREATION:", r_task.status_code)
        print("Created Task ID:", r_task.json()["id"])
        print("Task Status:", r_task.json()["status"])

if __name__ == "__main__":
    asyncio.run(test_live())
