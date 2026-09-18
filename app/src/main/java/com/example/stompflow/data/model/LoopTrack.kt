package com.example.stompflow.data.model

data class LoopTrack(
    val id: Int,
    val name: String = "Layer $id",
    val volume: Int = 80,
    val isPlaying: Boolean = false,
    val isRecording: Boolean = false,
    val durationSeconds: Float = 0f,
    val rawDurationSeconds: Float = 0f,
    val startTrimSeconds: Float = 0f,
    val endTrimSeconds: Float = 0f,
    val hasAudio: Boolean = false,
    val fullSamples: FloatArray? = null,
    val pcmSamples: FloatArray? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val layerCount: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LoopTrack
        return id == other.id &&
                name == other.name &&
                volume == other.volume &&
                isPlaying == other.isPlaying &&
                isRecording == other.isRecording &&
                durationSeconds == other.durationSeconds &&
                rawDurationSeconds == other.rawDurationSeconds &&
                startTrimSeconds == other.startTrimSeconds &&
                endTrimSeconds == other.endTrimSeconds &&
                hasAudio == other.hasAudio &&
                canUndo == other.canUndo &&
                canRedo == other.canRedo &&
                layerCount == other.layerCount
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + volume
        result = 31 * result + isPlaying.hashCode()
        result = 31 * result + isRecording.hashCode()
        result = 31 * result + durationSeconds.hashCode()
        result = 31 * result + rawDurationSeconds.hashCode()
        result = 31 * result + startTrimSeconds.hashCode()
        result = 31 * result + endTrimSeconds.hashCode()
        result = 31 * result + hasAudio.hashCode()
        result = 31 * result + canUndo.hashCode()
        result = 31 * result + canRedo.hashCode()
        result = 31 * result + layerCount
        return result
    }
}
