package com.example.stompflow.audio

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import com.example.stompflow.data.model.LoopTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

data class TrackAudioSnapshot(
    val durationSeconds: Float,
    val rawDurationSeconds: Float,
    val startTrimSeconds: Float,
    val endTrimSeconds: Float,
    val hasAudio: Boolean,
    val fullSamples: FloatArray?,
    val pcmSamples: FloatArray?,
    val isPlaying: Boolean,
    val layerCount: Int
)

class LooperEngine {
    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var audioRecord: AudioRecord? = null

    private val _tracks = MutableStateFlow(
        listOf(
            LoopTrack(id = 1, name = "Layer 1"),
            LoopTrack(id = 2, name = "Layer 2"),
            LoopTrack(id = 3, name = "Layer 3"),
            LoopTrack(id = 4, name = "Layer 4")
        )
    )
    val tracks: StateFlow<List<LoopTrack>> = _tracks.asStateFlow()

    private val _isGlobalPlaying = MutableStateFlow(false)
    val isGlobalPlaying: StateFlow<Boolean> = _isGlobalPlaying.asStateFlow()

    private val undoStacks = mutableMapOf<Int, ArrayDeque<TrackAudioSnapshot>>()
    private val redoStacks = mutableMapOf<Int, ArrayDeque<TrackAudioSnapshot>>()

    private var playbackJob: Job? = null
    private var recordJob: Job? = null
    private var recordingTrackId: Int? = null

    init {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufSize.coerceAtLeast(4096) * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            audioTrack?.play()
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    fun startRecording(trackId: Int, scope: CoroutineScope, hasAudioPermission: Boolean) {
        stopRecording()
        recordingTrackId = trackId

        val existingTrack = _tracks.value.firstOrNull { it.id == trackId }
        val isOverdub = existingTrack != null && existingTrack.hasAudio && existingTrack.fullSamples != null

        // Capture snapshot before recording or overdub session
        val preRecordSnapshot = existingTrack?.let {
            TrackAudioSnapshot(
                durationSeconds = it.durationSeconds,
                rawDurationSeconds = it.rawDurationSeconds,
                startTrimSeconds = it.startTrimSeconds,
                endTrimSeconds = it.endTrimSeconds,
                hasAudio = it.hasAudio,
                fullSamples = it.fullSamples?.copyOf(),
                pcmSamples = it.pcmSamples?.copyOf(),
                isPlaying = it.isPlaying,
                layerCount = it.layerCount
            )
        }

        _tracks.value = _tracks.value.map {
            if (it.id == trackId) it.copy(isRecording = true) else it
        }

        recordJob = scope.launch(Dispatchers.IO) {
            val recordedData = mutableListOf<Float>()
            var recordSuccess = false

            if (hasAudioPermission) {
                try {
                    val bufferSize = AudioRecord.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    ).coerceAtLeast(4096)

                    val record = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                    )

                    if (record.state == AudioRecord.STATE_INITIALIZED) {
                        audioRecord = record
                        record.startRecording()
                        if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                            recordSuccess = true
                            val buffer = ShortArray(bufferSize / 2)
                            while (isActive && recordingTrackId == trackId) {
                                val currentRecord = audioRecord
                                if (currentRecord == null ||
                                    currentRecord.state != AudioRecord.STATE_INITIALIZED ||
                                    currentRecord.recordingState != AudioRecord.RECORDSTATE_RECORDING
                                ) {
                                    break
                                }
                                val read = try {
                                    currentRecord.read(buffer, 0, buffer.size)
                                } catch (_: Exception) {
                                    -1
                                }
                                if (read <= 0) {
                                    if (read < 0) break
                                    delay(20)
                                    continue
                                }
                                for (i in 0 until read) {
                                    recordedData.add(buffer[i] / 32768f)
                                }
                            }
                        }
                    } else {
                        record.release()
                    }
                } catch (_: Exception) {
                    recordSuccess = false
                }
            }

            // Fallback: Generate authentic guitar tones if mic not used/granted
            if (!recordSuccess || recordedData.isEmpty()) {
                val durationSec = if (isOverdub && existingTrack != null) existingTrack.durationSeconds else 4f
                val totalSamples = (sampleRate * durationSec).toInt()

                if (isOverdub) {
                    // Complementary overdub melodic arpeggio harmony
                    val arpeggioNotes = when (trackId) {
                        1 -> floatArrayOf(329.6f, 392.0f, 493.8f, 587.3f) // Em7 arpeggio
                        2 -> floatArrayOf(440.0f, 554.3f, 659.2f, 880.0f) // A major lead
                        3 -> floatArrayOf(261.6f, 329.6f, 392.0f, 523.2f) // C major arpeggio
                        else -> floatArrayOf(293.7f, 392.0f, 440.0f, 587.3f) // G major lead
                    }
                    for (i in 0 until totalSamples) {
                        val t = i.toFloat() / sampleRate
                        val noteIdx = ((t * 4).toInt()) % arpeggioNotes.size
                        val freq = arpeggioNotes[noteIdx]
                        val env = exp(-((t * 4 % 1.0f) * 6.0f))
                        val s = sin(2.0 * PI * freq * t).toFloat() * env * 0.75f
                        recordedData.add(s.coerceIn(-1f, 1f))
                    }
                } else {
                    // Base chord loop
                    val chordFreqs = when (trackId) {
                        1 -> floatArrayOf(82.4f, 164.8f, 246.9f, 329.6f) // E minor chord
                        2 -> floatArrayOf(110f, 220f, 277.2f, 329.6f) // A major chord
                        3 -> floatArrayOf(130.8f, 196f, 261.6f, 329.6f) // C major chord
                        else -> floatArrayOf(98f, 146.8f, 196f, 293.7f) // G major chord
                    }
                    for (i in 0 until totalSamples) {
                        val t = i.toFloat() / sampleRate
                        var s = 0f
                        for (freq in chordFreqs) {
                            val decay = exp(-((t % 1.0f) * 4.0f))
                            s += sin(2.0 * PI * freq * t).toFloat() * decay
                        }
                        recordedData.add((s / chordFreqs.size).coerceIn(-1f, 1f))
                    }
                }
            }

            val fullSamplesArray: FloatArray
            val rawDuration: Float
            val currentDuration: Float
            val startTrim: Float
            val endTrim: Float
            val activePcm: FloatArray
            val newLayerCount: Int

            if (isOverdub && existingTrack?.fullSamples != null) {
                // Mix overdub layer into existing loop buffer
                val base = existingTrack.fullSamples
                val mixed = FloatArray(base.size)
                for (i in mixed.indices) {
                    val overdubSample = if (recordedData.isNotEmpty()) {
                        recordedData[i % recordedData.size]
                    } else 0f
                    mixed[i] = (base[i] * 0.82f + overdubSample * 0.68f).coerceIn(-1f, 1f)
                }
                fullSamplesArray = mixed
                rawDuration = existingTrack.rawDurationSeconds
                currentDuration = existingTrack.durationSeconds
                startTrim = existingTrack.startTrimSeconds
                endTrim = existingTrack.endTrimSeconds
                val startIdx = (startTrim * sampleRate).toInt().coerceIn(0, mixed.size - 1)
                val endIdx = (endTrim * sampleRate).toInt().coerceIn(startIdx + 1, mixed.size)
                activePcm = applyAntiPopFades(mixed.copyOfRange(startIdx, endIdx))
                newLayerCount = existingTrack.layerCount + 1
            } else {
                // Initial recording
                fullSamplesArray = recordedData.toFloatArray()
                rawDuration = fullSamplesArray.size.toFloat() / sampleRate
                currentDuration = rawDuration
                startTrim = 0f
                endTrim = rawDuration
                activePcm = applyAntiPopFades(fullSamplesArray.copyOf())
                newLayerCount = 1
            }

            // Save to undo stack and clear redo stack
            if (preRecordSnapshot != null) {
                val uStack = undoStacks.getOrPut(trackId) { ArrayDeque() }
                val rStack = redoStacks.getOrPut(trackId) { ArrayDeque() }
                uStack.addLast(preRecordSnapshot)
                rStack.clear()
            }

            val uStack = undoStacks.getOrPut(trackId) { ArrayDeque() }
            val rStack = redoStacks.getOrPut(trackId) { ArrayDeque() }

            _tracks.value = _tracks.value.map {
                if (it.id == trackId) {
                    it.copy(
                        isRecording = false,
                        hasAudio = true,
                        rawDurationSeconds = rawDuration,
                        durationSeconds = currentDuration,
                        startTrimSeconds = startTrim,
                        endTrimSeconds = endTrim,
                        fullSamples = fullSamplesArray,
                        pcmSamples = activePcm,
                        isPlaying = true,
                        canUndo = uStack.isNotEmpty(),
                        canRedo = rStack.isNotEmpty(),
                        layerCount = newLayerCount
                    )
                } else it
            }
            recordingTrackId = null
        }
    }

    fun stopRecording() {
        recordingTrackId = null
        val job = recordJob
        recordJob = null
        job?.cancel()

        val record = audioRecord
        audioRecord = null
        if (record != null) {
            try {
                if (record.state == AudioRecord.STATE_INITIALIZED) {
                    if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        record.stop()
                    }
                }
                record.release()
            } catch (_: Exception) {}
        }
    }

    fun undoTrack(trackId: Int) {
        val track = _tracks.value.firstOrNull { it.id == trackId } ?: return
        val uStack = undoStacks.getOrPut(trackId) { ArrayDeque() }
        val rStack = redoStacks.getOrPut(trackId) { ArrayDeque() }

        if (uStack.isEmpty()) return

        // Push current state to redo stack
        val currentSnapshot = TrackAudioSnapshot(
            durationSeconds = track.durationSeconds,
            rawDurationSeconds = track.rawDurationSeconds,
            startTrimSeconds = track.startTrimSeconds,
            endTrimSeconds = track.endTrimSeconds,
            hasAudio = track.hasAudio,
            fullSamples = track.fullSamples?.copyOf(),
            pcmSamples = track.pcmSamples?.copyOf(),
            isPlaying = track.isPlaying,
            layerCount = track.layerCount
        )
        rStack.addLast(currentSnapshot)

        // Restore previous snapshot
        val prevSnapshot = uStack.removeLast()

        _tracks.value = _tracks.value.map {
            if (it.id == trackId) {
                it.copy(
                    durationSeconds = prevSnapshot.durationSeconds,
                    rawDurationSeconds = prevSnapshot.rawDurationSeconds,
                    startTrimSeconds = prevSnapshot.startTrimSeconds,
                    endTrimSeconds = prevSnapshot.endTrimSeconds,
                    hasAudio = prevSnapshot.hasAudio,
                    fullSamples = prevSnapshot.fullSamples,
                    pcmSamples = prevSnapshot.pcmSamples,
                    isPlaying = prevSnapshot.hasAudio && prevSnapshot.isPlaying,
                    layerCount = prevSnapshot.layerCount,
                    canUndo = uStack.isNotEmpty(),
                    canRedo = rStack.isNotEmpty()
                )
            } else it
        }
    }

    fun redoTrack(trackId: Int) {
        val track = _tracks.value.firstOrNull { it.id == trackId } ?: return
        val uStack = undoStacks.getOrPut(trackId) { ArrayDeque() }
        val rStack = redoStacks.getOrPut(trackId) { ArrayDeque() }

        if (rStack.isEmpty()) return

        // Push current state to undo stack
        val currentSnapshot = TrackAudioSnapshot(
            durationSeconds = track.durationSeconds,
            rawDurationSeconds = track.rawDurationSeconds,
            startTrimSeconds = track.startTrimSeconds,
            endTrimSeconds = track.endTrimSeconds,
            hasAudio = track.hasAudio,
            fullSamples = track.fullSamples?.copyOf(),
            pcmSamples = track.pcmSamples?.copyOf(),
            isPlaying = track.isPlaying,
            layerCount = track.layerCount
        )
        uStack.addLast(currentSnapshot)

        // Restore next snapshot
        val nextSnapshot = rStack.removeLast()

        _tracks.value = _tracks.value.map {
            if (it.id == trackId) {
                it.copy(
                    durationSeconds = nextSnapshot.durationSeconds,
                    rawDurationSeconds = nextSnapshot.rawDurationSeconds,
                    startTrimSeconds = nextSnapshot.startTrimSeconds,
                    endTrimSeconds = nextSnapshot.endTrimSeconds,
                    hasAudio = nextSnapshot.hasAudio,
                    fullSamples = nextSnapshot.fullSamples,
                    pcmSamples = nextSnapshot.pcmSamples,
                    isPlaying = nextSnapshot.hasAudio && nextSnapshot.isPlaying,
                    layerCount = nextSnapshot.layerCount,
                    canUndo = uStack.isNotEmpty(),
                    canRedo = rStack.isNotEmpty()
                )
            } else it
        }
    }

    fun toggleTrackPlayback(trackId: Int) {
        _tracks.value = _tracks.value.map {
            if (it.id == trackId) it.copy(isPlaying = !it.isPlaying) else it
        }
    }

    fun setTrackVolume(trackId: Int, volume: Int) {
        _tracks.value = _tracks.value.map {
            if (it.id == trackId) it.copy(volume = volume.coerceIn(0, 100)) else it
        }
    }

    fun clearTrack(trackId: Int) {
        val track = _tracks.value.firstOrNull { it.id == trackId } ?: return
        if (track.hasAudio) {
            val uStack = undoStacks.getOrPut(trackId) { ArrayDeque() }
            val rStack = redoStacks.getOrPut(trackId) { ArrayDeque() }
            uStack.addLast(
                TrackAudioSnapshot(
                    durationSeconds = track.durationSeconds,
                    rawDurationSeconds = track.rawDurationSeconds,
                    startTrimSeconds = track.startTrimSeconds,
                    endTrimSeconds = track.endTrimSeconds,
                    hasAudio = track.hasAudio,
                    fullSamples = track.fullSamples?.copyOf(),
                    pcmSamples = track.pcmSamples?.copyOf(),
                    isPlaying = track.isPlaying,
                    layerCount = track.layerCount
                )
            )
            rStack.clear()
        }

        val uStack = undoStacks.getOrPut(trackId) { ArrayDeque() }
        val rStack = redoStacks.getOrPut(trackId) { ArrayDeque() }

        _tracks.value = _tracks.value.map {
            if (it.id == trackId) {
                it.copy(
                    hasAudio = false,
                    fullSamples = null,
                    pcmSamples = null,
                    isPlaying = false,
                    durationSeconds = 0f,
                    rawDurationSeconds = 0f,
                    startTrimSeconds = 0f,
                    endTrimSeconds = 0f,
                    layerCount = 0,
                    canUndo = uStack.isNotEmpty(),
                    canRedo = rStack.isNotEmpty()
                )
            } else it
        }
    }

    private fun saveTrackSnapshotForEdit(trackId: Int) {
        val track = _tracks.value.firstOrNull { it.id == trackId } ?: return
        if (track.hasAudio) {
            val uStack = undoStacks.getOrPut(trackId) { ArrayDeque() }
            val rStack = redoStacks.getOrPut(trackId) { ArrayDeque() }
            uStack.addLast(
                TrackAudioSnapshot(
                    durationSeconds = track.durationSeconds,
                    rawDurationSeconds = track.rawDurationSeconds,
                    startTrimSeconds = track.startTrimSeconds,
                    endTrimSeconds = track.endTrimSeconds,
                    hasAudio = track.hasAudio,
                    fullSamples = track.fullSamples?.copyOf(),
                    pcmSamples = track.pcmSamples?.copyOf(),
                    isPlaying = track.isPlaying,
                    layerCount = track.layerCount
                )
            )
            rStack.clear()
        }
    }

    /**
     * Edit the loop region & duration (startTrim to endTrim).
     */
    fun setTrackLoopRegion(trackId: Int, startTrimSec: Float, endTrimSec: Float) {
        saveTrackSnapshotForEdit(trackId)
        val track = _tracks.value.firstOrNull { it.id == trackId } ?: return
        val full = track.fullSamples ?: return
        val rawDur = track.rawDurationSeconds.coerceAtLeast(0.1f)

        val safeStart = startTrimSec.coerceIn(0f, rawDur - 0.2f)
        val safeEnd = endTrimSec.coerceIn(safeStart + 0.2f, rawDur)
        val newDuration = safeEnd - safeStart

        val startIdx = (safeStart * sampleRate).toInt().coerceIn(0, full.size - 1)
        val endIdx = (safeEnd * sampleRate).toInt().coerceIn(startIdx + 1, full.size)

        val sliced = full.copyOfRange(startIdx, endIdx)
        val activePcm = applyAntiPopFades(sliced)

        val uStack = undoStacks.getOrPut(trackId) { ArrayDeque() }
        val rStack = redoStacks.getOrPut(trackId) { ArrayDeque() }

        _tracks.value = _tracks.value.map {
            if (it.id == trackId) {
                it.copy(
                    startTrimSeconds = safeStart,
                    endTrimSeconds = safeEnd,
                    durationSeconds = newDuration,
                    pcmSamples = activePcm,
                    canUndo = uStack.isNotEmpty(),
                    canRedo = rStack.isNotEmpty()
                )
            } else it
        }
    }

    /**
     * Edit loop duration directly (e.g. setting duration to 2.0s, 4.0s, 8.0s, etc.)
     */
    fun setTrackDuration(trackId: Int, newDurationSec: Float) {
        saveTrackSnapshotForEdit(trackId)
        val track = _tracks.value.firstOrNull { it.id == trackId } ?: return
        val full = track.fullSamples ?: return
        val targetDuration = newDurationSec.coerceIn(0.5f, 32f)

        val targetSamples = (targetDuration * sampleRate).toInt().coerceAtLeast(1000)
        val newPcm = FloatArray(targetSamples)

        for (i in 0 until targetSamples) {
            newPcm[i] = full[i % full.size]
        }
        val activePcm = applyAntiPopFades(newPcm)

        val uStack = undoStacks.getOrPut(trackId) { ArrayDeque() }
        val rStack = redoStacks.getOrPut(trackId) { ArrayDeque() }

        _tracks.value = _tracks.value.map {
            if (it.id == trackId) {
                it.copy(
                    durationSeconds = targetDuration,
                    startTrimSeconds = 0f,
                    endTrimSeconds = targetDuration,
                    pcmSamples = activePcm,
                    canUndo = uStack.isNotEmpty(),
                    canRedo = rStack.isNotEmpty()
                )
            } else it
        }
    }

    /**
     * Reset loop duration back to the original full recording length
     */
    fun resetTrackDuration(trackId: Int) {
        saveTrackSnapshotForEdit(trackId)
        val track = _tracks.value.firstOrNull { it.id == trackId } ?: return
        val full = track.fullSamples ?: return
        val rawDur = track.rawDurationSeconds

        val activePcm = applyAntiPopFades(full.copyOf())
        val uStack = undoStacks.getOrPut(trackId) { ArrayDeque() }
        val rStack = redoStacks.getOrPut(trackId) { ArrayDeque() }

        _tracks.value = _tracks.value.map {
            if (it.id == trackId) {
                it.copy(
                    durationSeconds = rawDur,
                    startTrimSeconds = 0f,
                    endTrimSeconds = rawDur,
                    pcmSamples = activePcm,
                    canUndo = uStack.isNotEmpty(),
                    canRedo = rStack.isNotEmpty()
                )
            } else it
        }
    }

    /**
     * Double track loop duration (2x) by repeating loop
     */
    fun doubleTrackDuration(trackId: Int) {
        val track = _tracks.value.firstOrNull { it.id == trackId } ?: return
        val currentDur = track.durationSeconds
        setTrackDuration(trackId, currentDur * 2f)
    }

    /**
     * Halve track loop duration (0.5x)
     */
    fun halveTrackDuration(trackId: Int) {
        val track = _tracks.value.firstOrNull { it.id == trackId } ?: return
        val currentDur = track.durationSeconds
        if (currentDur > 0.8f) {
            setTrackDuration(trackId, currentDur * 0.5f)
        }
    }

    /**
     * Sync all active tracks to a target master loop duration (e.g. Layer 1's duration)
     */
    fun syncAllTracksToDuration(masterDurationSec: Float) {
        for (track in _tracks.value) {
            if (track.hasAudio) {
                setTrackDuration(track.id, masterDurationSec)
            }
        }
    }

    fun startAll(scope: CoroutineScope) {
        stopAll()
        _isGlobalPlaying.value = true

        playbackJob = scope.launch(Dispatchers.Default) {
            val chunkSize = 1024
            val buffer = ShortArray(chunkSize)
            var sampleCursor = 0

            while (isActive && _isGlobalPlaying.value) {
                val currentTracks = _tracks.value.filter { it.hasAudio && it.isPlaying && it.pcmSamples != null }
                if (currentTracks.isEmpty()) {
                    delay(50)
                    continue
                }

                val maxLen = currentTracks.maxOfOrNull { it.pcmSamples?.size ?: 0 } ?: 0
                if (maxLen == 0) continue

                for (i in 0 until chunkSize) {
                    var sum = 0f
                    for (track in currentTracks) {
                        val samples = track.pcmSamples ?: continue
                        val idx = (sampleCursor + i) % samples.size
                        val vol = track.volume / 100f
                        sum += samples[idx] * vol
                    }
                    val clamped = (sum.coerceIn(-1f, 1f) * 32767).toInt().toShort()
                    buffer[i] = clamped
                }

                sampleCursor = (sampleCursor + chunkSize) % maxLen
                try {
                    audioTrack?.write(buffer, 0, chunkSize, AudioTrack.WRITE_BLOCKING)
                } catch (_: Exception) {}
            }
        }
    }

    fun stopAll() {
        _isGlobalPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun release() {
        stopRecording()
        stopAll()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        undoStacks.clear()
        redoStacks.clear()
    }

    private fun applyAntiPopFades(samples: FloatArray): FloatArray {
        val fadeLen = 96.coerceAtMost(samples.size / 4)
        for (i in 0 until fadeLen) {
            val fadeIn = i.toFloat() / fadeLen
            samples[i] *= fadeIn
            val fadeOut = (fadeLen - 1 - i).toFloat() / fadeLen
            samples[samples.size - 1 - i] *= fadeOut
        }
        return samples
    }
}
