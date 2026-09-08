package com.example.stompflow.data.repository

import com.example.stompflow.data.local.SessionDao
import com.example.stompflow.data.local.SessionEntity
import com.example.stompflow.data.model.EffectNode
import com.example.stompflow.data.model.Patch
import com.example.stompflow.data.model.RhythmPattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class StompFlowRepository(private val sessionDao: SessionDao) {

    private val _patches = MutableStateFlow<List<Patch>>(
        listOf(Patch.DEFAULT_PATCH) + Patch.PRESETS
    )
    val patches: StateFlow<List<Patch>> = _patches.asStateFlow()

    private val _patterns = MutableStateFlow<List<RhythmPattern>>(
        listOf(RhythmPattern.DEFAULT_PATTERN) + RhythmPattern.PRESETS
    )
    val patterns: StateFlow<List<RhythmPattern>> = _patterns.asStateFlow()

    val sessions: Flow<List<SessionEntity>> = sessionDao.getAllSessions()

    fun addCustomPatch(name: String, basePatch: Patch): Patch {
        val newPatch = basePatch.copy(
            id = UUID.randomUUID().toString(),
            name = name,
            isPreset = false
        )
        _patches.value = _patches.value + newPatch
        return newPatch
    }

    fun updatePatch(updatedPatch: Patch) {
        _patches.value = _patches.value.map {
            if (it.id == updatedPatch.id) updatedPatch else it
        }
    }

    fun addCustomPattern(name: String, basePattern: RhythmPattern): RhythmPattern {
        val newPattern = basePattern.copy(
            id = UUID.randomUUID().toString(),
            name = name,
            isPreset = false
        )
        _patterns.value = _patterns.value + newPattern
        return newPattern
    }

    fun updatePattern(updatedPattern: RhythmPattern) {
        _patterns.value = _patterns.value.map {
            if (it.id == updatedPattern.id) updatedPattern else it
        }
    }

    suspend fun saveSession(
        name: String,
        patchId: String,
        patchName: String,
        patternId: String,
        patternName: String
    ): Long {
        return sessionDao.insertSession(
            SessionEntity(
                name = name,
                patchId = patchId,
                patchName = patchName,
                patternId = patternId,
                patternName = patternName
            )
        )
    }

    suspend fun deleteSession(id: Long) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun clearAllData() {
        sessionDao.clearAll()
        _patches.value = listOf(Patch.DEFAULT_PATCH) + Patch.PRESETS
        _patterns.value = listOf(RhythmPattern.DEFAULT_PATTERN) + RhythmPattern.PRESETS
    }
}
