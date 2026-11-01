import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app

@pytest.mark.asyncio
async def test_task_lifecycle():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        # 1. Create Task
        create_payload = {
            "title": "Complete JARVIS Phase 1 Architecture",
            "description": "Establish backend, Android Compose shell, and voice bridge",
            "priority": "HIGH",
            "due_date": "2026-11-02T18:00:00Z"
        }
        res = await ac.post("/api/tasks", json=create_payload)
        assert res.status_code == 200
        task_data = res.json()
        task_id = task_data["id"]
        assert task_data["title"] == create_payload["title"]
        assert task_data["priority"] == "HIGH"
        assert task_data["status"] == "PENDING"

        # 2. Get Task by ID
        res_get = await ac.get(f"/api/tasks/{task_id}")
        assert res_get.status_code == 200
        assert res_get.json()["id"] == task_id

        # 3. Update Task Status
        update_payload = {"status": "COMPLETED"}
        res_patch = await ac.patch(f"/api/tasks/{task_id}", json=update_payload)
        assert res_patch.status_code == 200
        assert res_patch.json()["status"] == "COMPLETED"

        # 4. List Tasks
        res_list = await ac.get("/api/tasks")
        assert res_list.status_code == 200
        tasks = res_list.json()
        assert any(t["id"] == task_id for t in tasks)

        # 5. Delete Task
        res_del = await ac.delete(f"/api/tasks/{task_id}")
        assert res_del.status_code == 200
        assert res_del.json()["status"] == "deleted"

        # Verify deletion
        res_get_deleted = await ac.get(f"/api/tasks/{task_id}")
        assert res_get_deleted.status_code == 404

