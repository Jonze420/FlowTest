package com.example.stompflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stompflow.data.model.LoopTrack
import com.example.stompflow.ui.theme.AccentGreen
import com.example.stompflow.ui.theme.AccentRed
import com.example.stompflow.ui.theme.DarkBackground
import com.example.stompflow.ui.theme.DarkBorder
import com.example.stompflow.ui.theme.DarkSurface
import com.example.stompflow.ui.theme.DarkSurfaceElevated
import com.example.stompflow.ui.theme.OrangePrimary
import com.example.stompflow.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun LooperScreen(
    tracks: List<LoopTrack>,
    isGlobalPlaying: Boolean,
    hasAudioPermission: Boolean,
    onRequestAudioPermission: () -> Unit,
    onStartRecording: (Int) -> Unit,
    onStopRecording: () -> Unit,
    onToggleTrackPlayback: (Int) -> Unit,
    onSetTrackVolume: (Int, Int) -> Unit,
    onClearTrack: (Int) -> Unit,
    onToggleGlobalPlay: () -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Global Transport Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "4-Track Looper",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = if (hasAudioPermission) "Live mic & guitar input ready" else "Tap record to request mic access",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onToggleGlobalPlay,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isGlobalPlaying) Color.DarkGray else AccentGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("looper_global_play")
                        ) {
                            Icon(
                                imageVector = if (isGlobalPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(if (isGlobalPlaying) "Stop" else "Play All")
                        }

                        IconButton(
                            onClick = onResetAll,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset All", tint = TextSecondary)
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "LOOP LAYERS",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        // Track Cards
        items(tracks) { track ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .border(
                        1.dp,
                        when {
                            track.isRecording -> AccentRed
                            track.isPlaying -> AccentGreen
                            track.hasAudio -> OrangePrimary.copy(alpha = 0.5f)
                            else -> DarkBorder
                        },
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
                    .testTag("loop_card_${track.id}")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Header: Track Name, Status, and Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            track.isRecording -> AccentRed
                                            track.isPlaying -> AccentGreen
                                            track.hasAudio -> OrangePrimary
                                            else -> Color.DarkGray
                                        }
                                    )
                            )
                            Text(
                                text = track.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            if (track.hasAudio) {
                                Text(
                                    text = String.format(Locale.US, "%.1fs", track.durationSeconds),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrangePrimary
                                )
                            }
                        }

                        // Record / Stop / Play Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Record Button
                            Button(
                                onClick = {
                                    if (track.isRecording) {
                                        onStopRecording()
                                    } else {
                                        if (!hasAudioPermission) {
                                            onRequestAudioPermission()
                                        }
                                        onStartRecording(track.id)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (track.isRecording) AccentRed else DarkSurface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .border(1.dp, if (track.isRecording) AccentRed else DarkBorder, RoundedCornerShape(8.dp))
                                    .testTag("record_track_${track.id}")
                            ) {
                                Icon(
                                    imageVector = if (track.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                                    contentDescription = null,
                                    tint = if (track.isRecording) Color.White else AccentRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    if (track.isRecording) "Stop Rec" else "Record",
                                    fontSize = 12.sp,
                                    color = if (track.isRecording) Color.White else TextSecondary
                                )
                            }

                            // Play/Mute toggle
                            if (track.hasAudio) {
                                Button(
                                    onClick = { onToggleTrackPlayback(track.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (track.isPlaying) AccentGreen else DarkSurface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        imageVector = if (track.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onClearTrack(track.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Clear Layer",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Simulated Waveform Visualization Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkBackground)
                    ) {
                        val centerY = size.height / 2
                        val barCount = 48
                        val barSpacing = size.width / barCount
                        val samples = track.pcmSamples

                        for (i in 0 until barCount) {
                            val x = i * barSpacing + barSpacing / 2
                            val amp = if (samples != null && samples.isNotEmpty()) {
                                val sIdx = ((i.toFloat() / barCount) * samples.size).toInt().coerceIn(0, samples.size - 1)
                                abs(samples[sIdx]).coerceIn(0.1f, 1.0f)
                            } else if (track.hasAudio) {
                                (abs(sin(i * 0.4f)) * 0.7f + 0.2f)
                            } else {
                                0.05f
                            }

                            val barHeight = amp * (size.height * 0.8f)
                            drawLine(
                                color = when {
                                    track.isRecording -> AccentRed
                                    track.isPlaying -> AccentGreen
                                    track.hasAudio -> OrangePrimary
                                    else -> Color.DarkGray
                                },
                                start = Offset(x, centerY - barHeight / 2),
                                end = Offset(x, centerY + barHeight / 2),
                                strokeWidth = barSpacing * 0.6f
                            )
                        }
                    }

                    // Volume Slider
                    if (track.hasAudio) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Layer Volume", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("${track.volume}%", style = MaterialTheme.typography.labelSmall, color = OrangePrimary)
                        }
                        Slider(
                            value = track.volume.toFloat(),
                            onValueChange = { onSetTrackVolume(track.id, it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = OrangePrimary,
                                activeTrackColor = OrangePrimary,
                                inactiveTrackColor = DarkBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
