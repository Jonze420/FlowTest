package com.example.stompflow.data.model

data class LoopTrack(
    val id: Int,
    val name: String = "Layer $id",
    val volume: Int = 80,
    val isPlaying: Boolean = false,
    val isRecording: Boolean = false,
    val durationSeconds: Float = 0f,
    val hasAudio: Boolean = false,
    val pcmSamples: FloatArray? = null
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
                hasAudio == other.hasAudio
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + volume
        result = 31 * result + isPlaying.hashCode()
        result = 31 * result + isRecording.hashCode()
        result = 31 * result + durationSeconds.hashCode()
        result = 31 * result + hasAudio.hashCode()
        return result
    }
}
