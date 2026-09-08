package com.example.stompflow.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM sessions")
    suspend fun clearAll()
}
