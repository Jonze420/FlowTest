package com.example.stompflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val patchId: String,
    val patchName: String,
    val patternId: String,
    val patternName: String,
    val createdAt: Long = System.currentTimeMillis()
)
