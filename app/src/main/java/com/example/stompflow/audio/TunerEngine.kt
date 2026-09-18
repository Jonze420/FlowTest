package com.example.stompflow.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt

data class TunerState(
    val isListening: Boolean = false,
    val frequency: Float = 0f,
    val noteName: String = "--",
    val octave: Int = 0,
    val cents: Float = 0f,
    val isInTune: Boolean = false
)

class TunerEngine {
    private val sampleRate = 44100
    private val bufferSize = 4096
    private var audioRecord: AudioRecord? = null
    private var tunerJob: Job? = null

    private val _tunerState = MutableStateFlow(TunerState())
    val tunerState: StateFlow<TunerState> = _tunerState.asStateFlow()

    private val noteStrings = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    @SuppressLint("MissingPermission")
    fun startListening(scope: CoroutineScope, hasAudioPermission: Boolean) {
        stopListening()
        if (!hasAudioPermission) {
            _tunerState.value = TunerState(isListening = false, noteName = "No Mic")
            return
        }

        try {
            val minBufSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(bufferSize)

            val record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufSize
            )

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                record.release()
                _tunerState.value = TunerState(isListening = false, noteName = "Mic Err")
                return
            }

            audioRecord = record
            record.startRecording()
            if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                stopListening()
                _tunerState.value = TunerState(isListening = false, noteName = "Mic Err")
                return
            }

            _tunerState.value = TunerState(isListening = true)

            tunerJob = scope.launch(Dispatchers.Default) {
                val buffer = ShortArray(bufferSize)
                while (isActive && _tunerState.value.isListening) {
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
                        if (read < 0) {
                            // Negative return means error (e.g. ERROR_INVALID_OPERATION). Break immediately!
                            break
                        }
                        delay(20)
                        continue
                    }

                    if (read > 1024) {
                        val freq = detectPitchAutocorrelation(buffer, read, sampleRate)
                        if (freq in 60.0..1200.0) {
                            processPitch(freq.toFloat())
                        }
                    }
                }
            }
        } catch (_: Exception) {
            stopListening()
            _tunerState.value = TunerState(isListening = false)
        }
    }

    fun stopListening() {
        _tunerState.value = TunerState(isListening = false)
        val job = tunerJob
        tunerJob = null
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

    private fun processPitch(freq: Float) {
        // A4 = 440 Hz -> MIDI note 69
        val midiNote = 69 + 12 * log2(freq / 440.0)
        val roundedMidi = midiNote.roundToInt()
        val cents = ((midiNote - roundedMidi) * 100.0).toFloat()

        val noteIndex = (roundedMidi % 12 + 12) % 12
        val octave = (roundedMidi / 12) - 1
        val noteName = noteStrings[noteIndex]
        val inTune = abs(cents) < 5f

        _tunerState.value = TunerState(
            isListening = true,
            frequency = freq,
            noteName = noteName,
            octave = octave,
            cents = cents.coerceIn(-50f, 50f),
            isInTune = inTune
        )
    }

    private fun detectPitchAutocorrelation(samples: ShortArray, length: Int, sRate: Int): Double {
        val minPeriod = sRate / 1000 // 1000 Hz upper limit
        val maxPeriod = sRate / 60   // 60 Hz lower limit (guitar low B/E)

        var bestLag = -1
        var bestCorrelation = 0.0

        for (lag in minPeriod..maxPeriod.coerceAtMost(length / 2)) {
            var sum = 0.0
            for (i in 0 until (length - lag)) {
                sum += samples[i] * samples[i + lag]
            }
            if (sum > bestCorrelation) {
                bestCorrelation = sum
                bestLag = lag
            }
        }

        return if (bestLag > 0) sRate.toDouble() / bestLag else 0.0
    }
}
