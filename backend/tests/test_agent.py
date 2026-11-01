import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app

@pytest.mark.asyncio
async def test_agent_job_lifecycle():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        submit_payload = {
            "objective": "Research best AI models for edge Android devices and compile benchmark report",
            "require_user_approval": True
        }
        res = await ac.post("/api/agent/jobs", json=submit_payload)
        assert res.status_code == 200
        job_data = res.json()
        job_id = job_data["job_id"]
        assert len(job_data["steps"]) >= 3

        # Poll status
        res_poll = await ac.get(f"/api/agent/jobs/{job_id}")
        assert res_poll.status_code == 200
        assert res_poll.json()["job_id"] == job_id

