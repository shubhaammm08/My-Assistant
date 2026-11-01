package com.jarvis.assistant.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey
    val key: String,
    val value: String,
    val category: String,
    val updatedAt: String
)

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY updatedAt DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE key = :key LIMIT 1")
    suspend fun getMemory(key: String): MemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE key = :key")
    suspend fun deleteMemory(key: String)
}

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionType: String, // 'task_create', 'task_update', 'chat_message', 'agent_job'
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface PendingSyncDao {
    @Query("SELECT * FROM pending_sync ORDER BY timestamp ASC")
    suspend fun getAllPending(): List<PendingSyncEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun queueSync(item: PendingSyncEntity)

    @Query("DELETE FROM pending_sync WHERE id = :id")
    suspend fun removeSync(id: Long)

    @Query("DELETE FROM pending_sync")
    suspend fun clearAll()
}

