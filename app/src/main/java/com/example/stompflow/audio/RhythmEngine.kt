package com.example.stompflow.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.stompflow.data.model.DrumVoice
import com.example.stompflow.data.model.RhythmPattern
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
import kotlin.random.Random

class RhythmEngine {
    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentStep = MutableStateFlow(-1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private var sequencerJob: Job? = null
    private var activePattern: RhythmPattern? = null

    // Pre-rendered drum sound sample buffers (PCM 16-bit mono)
    private val kickSamples: ShortArray by lazy { generateKick() }
    private val snareSamples: ShortArray by lazy { generateSnare() }
    private val hihatSamples: ShortArray by lazy { generateHihat() }
    private val clapSamples: ShortArray by lazy { generateClap() }
    private val tomSamples: ShortArray by lazy { generateTom() }

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
        } catch (_: Exception) {
            // AudioTrack init fallback
        }
    }

    fun updatePattern(pattern: RhythmPattern) {
        activePattern = pattern
    }

    fun start(pattern: RhythmPattern, scope: CoroutineScope) {
        stop()
        activePattern = pattern
        _isPlaying.value = true

        sequencerJob = scope.launch(Dispatchers.Default) {
            var step = 0
            while (isActive && _isPlaying.value) {
                val pat = activePattern ?: pattern
                _currentStep.value = step

                // Mix active voices for this step
                mixAndPlayStep(pat, step)

                // Calculate step delay with BPM and swing
                val bpm = pat.bpm.coerceIn(40, 240)
                val baseStepMs = (60_000.0 / bpm / 4.0)
                val swingRatio = (pat.swing / 100.0) * 0.5
                val actualDelayMs = if (step % 2 == 0) {
                    baseStepMs * (1.0 + swingRatio)
                } else {
                    baseStepMs * (1.0 - swingRatio)
                }

                step = (step + 1) % 16
                delay(actualDelayMs.toLong().coerceAtLeast(10))
            }
        }
    }

    fun stop() {
        _isPlaying.value = false
        _currentStep.value = -1
        sequencerJob?.cancel()
        sequencerJob = null
    }

    fun release() {
        stop()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    private fun mixAndPlayStep(pattern: RhythmPattern, stepIndex: Int) {
        val activeVoices = mutableListOf<ShortArray>()

        DrumVoice.entries.forEachIndexed { trackIdx, voice ->
            if (trackIdx < pattern.steps.size && stepIndex < pattern.steps[trackIdx].size) {
                if (pattern.steps[trackIdx][stepIndex]) {
                    when (voice) {
                        DrumVoice.KICK -> activeVoices.add(kickSamples)
                        DrumVoice.SNARE -> activeVoices.add(snareSamples)
                        DrumVoice.HIHAT -> activeVoices.add(hihatSamples)
                        DrumVoice.CLAP -> activeVoices.add(clapSamples)
                        DrumVoice.TOM -> activeVoices.add(tomSamples)
                    }
                }
            }
        }

        if (activeVoices.isEmpty()) return

        val maxLen = activeVoices.maxOf { it.size }
        val mixed = ShortArray(maxLen)

        for (i in 0 until maxLen) {
            var sum = 0
            for (buf in activeVoices) {
                if (i < buf.size) {
                    sum += buf[i]
                }
            }
            // Soft clipping
            val clamped = sum.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            mixed[i] = clamped.toShort()
        }

        try {
            audioTrack?.write(mixed, 0, mixed.size, AudioTrack.WRITE_NON_BLOCKING)
        } catch (_: Exception) {}
    }

    private fun generateKick(): ShortArray {
        val durationMs = 280
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            // Pitch decay from 150 Hz to 45 Hz
            val freq = 45.0 + 105.0 * exp(-t * 22.0)
            phase += 2.0 * PI * freq / sampleRate
            val env = exp(-t * 12.0)
            val sample = sin(phase) * env
            buffer[i] = (sample * 30000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun generateSnare(): ShortArray {
        val durationMs = 200
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        val random = Random(42)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            // Tone component: 180 Hz sine pop
            phase += 2.0 * PI * 180.0 / sampleRate
            val tone = sin(phase) * exp(-t * 30.0) * 0.4
            // Noise component
            val noise = (random.nextDouble() * 2.0 - 1.0) * exp(-t * 18.0) * 0.7
            val sample = tone + noise
            buffer[i] = (sample * 26000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun generateHihat(): ShortArray {
        val durationMs = 70
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)
        val random = Random(123)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val noise = (random.nextDouble() * 2.0 - 1.0)
            val env = exp(-t * 45.0)
            val sample = noise * env * 0.5
            buffer[i] = (sample * 24000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun generateClap(): ShortArray {
        val durationMs = 180
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)
        val random = Random(789)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val noise = (random.nextDouble() * 2.0 - 1.0)
            // Multi-burst envelope
            val burstEnv = when {
                t < 0.010 -> 0.4
                t < 0.015 -> 0.1
                t < 0.025 -> 0.5
                t < 0.030 -> 0.1
                t < 0.040 -> 0.8
                else -> exp(-(t - 0.040) * 25.0)
            }
            val sample = noise * burstEnv * 0.6
            buffer[i] = (sample * 25000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun generateTom(): ShortArray {
        val durationMs = 300
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 90.0 + 130.0 * exp(-t * 14.0)
            phase += 2.0 * PI * freq / sampleRate
            val env = exp(-t * 8.0)
            val sample = sin(phase) * env * 0.8
            buffer[i] = (sample * 28000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }
}
