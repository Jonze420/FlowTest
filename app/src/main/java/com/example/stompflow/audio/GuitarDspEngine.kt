package com.example.stompflow.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.stompflow.data.model.EffectNode
import com.example.stompflow.data.model.EffectType
import com.example.stompflow.data.model.Patch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.tanh

class GuitarDspEngine {
    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null

    private val _isPlayingPreview = MutableStateFlow(false)
    val isPlayingPreview: StateFlow<Boolean> = _isPlayingPreview.asStateFlow()

    private var previewJob: Job? = null

    init {
        try {
            val minBuf = AudioTrack.getMinBufferSize(
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
                .setBufferSizeInBytes(minBuf.coerceAtLeast(4096) * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            audioTrack?.play()
        } catch (_: Exception) {}
    }

    fun playAuditionRiff(patch: Patch, scope: CoroutineScope) {
        stopAudition()
        _isPlayingPreview.value = true

        previewJob = scope.launch(Dispatchers.Default) {
            // Generate a 2.5 second guitar lick riff (E, G, A, D, E)
            val durationSec = 2.5f
            val totalSamples = (sampleRate * durationSec).toInt()
            val dryBuffer = FloatArray(totalSamples)

            val notes = listOf(
                Pair(82.4f, 0.0f to 0.5f),   // E2
                Pair(98.0f, 0.5f to 0.9f),   // G2
                Pair(110.0f, 0.9f to 1.3f),  // A2
                Pair(146.8f, 1.3f to 1.7f),  // D3
                Pair(164.8f, 1.7f to 2.5f)   // E3
            )

            for (note in notes) {
                val freq = note.first
                val startSec = note.second.first
                val endSec = note.second.second
                val startIdx = (startSec * sampleRate).toInt()
                val endIdx = (endSec * sampleRate).toInt().coerceAtMost(totalSamples)

                for (i in startIdx until endIdx) {
                    val localT = (i - startIdx).toFloat() / sampleRate
                    // Rich harmonics for guitar timbre
                    val fundamental = sin(2.0 * PI * freq * localT).toFloat()
                    val h2 = 0.5f * sin(2.0 * PI * (freq * 2) * localT).toFloat()
                    val h3 = 0.25f * sin(2.0 * PI * (freq * 3) * localT).toFloat()
                    val env = exp(-localT * 3.2f)
                    dryBuffer[i] += (fundamental + h2 + h3) * env * 0.7f
                }
            }

            // Process through active pedal chain
            val wetBuffer = applyDspChain(dryBuffer, patch.chain, patch.master)

            // Convert to 16-bit PCM and stream to AudioTrack
            val pcm = ShortArray(wetBuffer.size)
            for (i in wetBuffer.indices) {
                pcm[i] = (wetBuffer[i].coerceIn(-1f, 1f) * 32000).toInt().toShort()
            }

            val chunkSize = 2048
            var offset = 0
            while (offset < pcm.size && _isPlayingPreview.value) {
                val length = (pcm.size - offset).coerceAtMost(chunkSize)
                audioTrack?.write(pcm, offset, length, AudioTrack.WRITE_BLOCKING)
                offset += length
            }

            delay(100)
            _isPlayingPreview.value = false
        }
    }

    fun stopAudition() {
        _isPlayingPreview.value = false
        previewJob?.cancel()
        previewJob = null
    }

    fun release() {
        stopAudition()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    private fun applyDspChain(input: FloatArray, chain: List<EffectNode>, masterVolume: Int): FloatArray {
        var current = input.copyOf()

        for (effect in chain) {
            if (!effect.enabled) continue
            val amount = effect.param / 100f

            when (effect.effectType) {
                EffectType.DRIVE -> {
                    val gain = 1f + amount * 7f
                    for (i in current.indices) {
                        current[i] = tanh(current[i] * gain)
                    }
                }
                EffectType.OVERDRIVE -> {
                    val gain = 1f + amount * 5f
                    for (i in current.indices) {
                        val x = current[i] * gain
                        // Asymmetric soft clipping
                        current[i] = if (x > 0f) {
                            tanh(x)
                        } else {
                            -tanh(-x * 0.7f) * 1.3f
                        }
                    }
                }
                EffectType.DELAY -> {
                    val delaySamples = (sampleRate * 0.28f).toInt()
                    val feedback = (amount * 0.65f).coerceIn(0f, 0.85f)
                    val delayed = FloatArray(current.size)
                    for (i in current.indices) {
                        val prev = if (i >= delaySamples) delayed[i - delaySamples] else 0f
                        delayed[i] = current[i] + prev * feedback
                    }
                    val mix = amount * 0.5f
                    for (i in current.indices) {
                        current[i] = current[i] * (1f - mix) + delayed[i] * mix
                    }
                }
                EffectType.REVERB -> {
                    val revBuf = FloatArray(current.size)
                    val delays = intArrayOf(
                        (sampleRate * 0.031f).toInt(),
                        (sampleRate * 0.043f).toInt(),
                        (sampleRate * 0.059f).toInt()
                    )
                    for (i in current.indices) {
                        var sum = 0f
                        for (d in delays) {
                            if (i >= d) sum += revBuf[i - d] * 0.35f
                        }
                        revBuf[i] = current[i] + sum * amount
                    }
                    val mix = amount * 0.45f
                    for (i in current.indices) {
                        current[i] = current[i] * (1f - mix) + revBuf[i] * mix
                    }
                }
                EffectType.TREMOLO -> {
                    val lfoFreq = 2f + amount * 7f
                    for (i in current.indices) {
                        val t = i.toFloat() / sampleRate
                        val lfo = 0.5f * (1f + sin(2.0 * PI * lfoFreq * t).toFloat())
                        current[i] *= (1f - amount) + amount * lfo
                    }
                }
                EffectType.CHORUS -> {
                    val chorusDelay = (sampleRate * 0.015f).toInt()
                    for (i in current.indices) {
                        val t = i.toFloat() / sampleRate
                        val mod = (sin(2.0 * PI * 1.5 * t) * (sampleRate * 0.003f)).toInt()
                        val tap = (i - chorusDelay + mod).coerceIn(0, current.size - 1)
                        current[i] = current[i] * 0.7f + current[tap] * 0.4f * amount
                    }
                }
                else -> {
                    // EQ / Comp / Phaser default gain shaping
                    for (i in current.indices) {
                        current[i] *= (0.9f + amount * 0.2f)
                    }
                }
            }
        }

        // Apply Master output volume
        val masterScale = masterVolume / 100f
        for (i in current.indices) {
            current[i] *= masterScale
        }

        return current
    }
}
