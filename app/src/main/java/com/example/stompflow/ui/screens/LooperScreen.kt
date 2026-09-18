package com.example.stompflow.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.stompflow.ui.theme.AccentBlue
import com.example.stompflow.ui.theme.AccentGreen
import com.example.stompflow.ui.theme.AccentPurple
import com.example.stompflow.ui.theme.AccentRed
import com.example.stompflow.ui.theme.AccentYellow
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
    onSetTrackDuration: (Int, Float) -> Unit,
    onSetTrackLoopRegion: (Int, Float, Float) -> Unit,
    onResetTrackDuration: (Int) -> Unit,
    onDoubleTrackDuration: (Int) -> Unit,
    onHalveTrackDuration: (Int) -> Unit,
    onSyncAllTracksToMaster: (Int) -> Unit,
    onUndoTrack: (Int) -> Unit,
    onRedoTrack: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var trackForDurationEdit by remember { mutableStateOf<LoopTrack?>(null) }
    val activeTrackCount = tracks.count { it.hasAudio }
    val masterTrack = tracks.firstOrNull { it.hasAudio }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Master Looper Dashboard Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Title and Global Transport
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "4-Track Looper Studio",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = if (hasAudioPermission) "Live mic & line input active" else "Tap record to grant mic access",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Play All / Stop All
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
                                Text(if (isGlobalPlaying) "Stop All" else "Play All")
                            }

                            // Reset All Button
                            IconButton(
                                onClick = onResetAll,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset All Layers", tint = TextSecondary)
                            }
                        }
                    }

                    // Dashboard telemetry & Master Loop Sync Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column {
                                Text("ACTIVE TRACKS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text("$activeTrackCount / 4", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }

                            Box(modifier = Modifier.size(1.dp, 28.dp).background(DarkBorder))

                            Column {
                                Text("MASTER CYCLE", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                val cycle = masterTrack?.durationSeconds ?: 0f
                                Text(
                                    if (cycle > 0f) String.format(Locale.US, "%.2fs", cycle) else "--",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OrangePrimary
                                )
                            }
                        }

                        // Master Sync Button
                        if (activeTrackCount > 1 && masterTrack != null) {
                            Button(
                                onClick = { onSyncAllTracksToMaster(masterTrack.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("sync_all_loops_button")
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Sync Lengths", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "TRACK CHANNEL STRIPS",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        // 4 Track Channel Strip Cards
        items(tracks) { track ->
            val trackColor = when (track.id) {
                1 -> OrangePrimary
                2 -> AccentBlue
                3 -> AccentPurple
                else -> AccentGreen
            }

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
                            track.hasAudio -> trackColor.copy(alpha = 0.6f)
                            else -> DarkBorder
                        },
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("loop_card_${track.id}")
            ) {
                Column {
                    // Colored Channel Header Strip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                when {
                                    track.isRecording -> AccentRed
                                    track.isPlaying -> AccentGreen
                                    track.hasAudio -> trackColor
                                    else -> Color.DarkGray
                                }
                            )
                    )

                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Channel Top: Name, Status LED, Duration Badge & Edit Duration Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Glowing LED
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                track.isRecording -> AccentRed
                                                track.isPlaying -> AccentGreen
                                                track.hasAudio -> trackColor
                                                else -> Color.DarkGray
                                            }
                                        )
                                        .border(1.dp, Color.Black, CircleShape)
                                )

                                Text(
                                    text = track.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )

                                if (track.layerCount > 1) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(AccentPurple.copy(alpha = 0.25f))
                                            .border(1.dp, AccentPurple.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Layer ${track.layerCount} (Dub)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = AccentPurple
                                        )
                                    }
                                } else if (track.hasAudio) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(trackColor.copy(alpha = 0.15f))
                                            .border(1.dp, trackColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Base Loop",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = trackColor
                                        )
                                    }
                                }

                                // Duration Chip (Clickable to edit duration)
                                if (track.hasAudio) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(DarkSurface)
                                            .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                                            .clickable { trackForDurationEdit = track }
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .testTag("duration_chip_${track.id}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = String.format(Locale.US, "%.2fs", track.durationSeconds),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = OrangePrimary
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = "Edit Duration",
                                                tint = OrangePrimary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick Edit Duration / Region Button
                            if (track.hasAudio) {
                                Button(
                                    onClick = { trackForDurationEdit = track },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                                        .testTag("edit_duration_button_${track.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCut,
                                        contentDescription = null,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text("Edit Loop", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }

                        // Waveform Visualizer Canvas with Trim Regions
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkBackground)
                        ) {
                            val centerY = size.height / 2
                            val barCount = 56
                            val barSpacing = size.width / barCount
                            val samples = track.pcmSamples

                            for (i in 0 until barCount) {
                                val x = i * barSpacing + barSpacing / 2
                                val progress = i.toFloat() / barCount
                                val amp = if (samples != null && samples.isNotEmpty()) {
                                    val sIdx = (progress * samples.size).toInt().coerceIn(0, samples.size - 1)
                                    abs(samples[sIdx]).coerceIn(0.12f, 1.0f)
                                } else if (track.hasAudio) {
                                    (abs(sin(i * 0.35f)) * 0.7f + 0.2f)
                                } else {
                                    0.05f
                                }

                                val barHeight = amp * (size.height * 0.85f)
                                drawLine(
                                    color = when {
                                        track.isRecording -> AccentRed
                                        track.isPlaying -> AccentGreen
                                        track.hasAudio -> trackColor
                                        else -> Color.DarkGray
                                    },
                                    start = Offset(x, centerY - barHeight / 2),
                                    end = Offset(x, centerY + barHeight / 2),
                                    strokeWidth = barSpacing * 0.65f
                                )
                            }
                        }

                        // Track Action Buttons (Record/Overdub, Play, Undo, Redo, Mute, Clear)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Record / Overdub Stomp Button
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
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            if (track.isRecording) AccentRed else if (track.hasAudio) AccentPurple.copy(alpha = 0.6f) else AccentRed.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .testTag("record_track_${track.id}")
                                ) {
                                    Icon(
                                        imageVector = when {
                                            track.isRecording -> Icons.Default.Stop
                                            track.hasAudio -> Icons.Default.Layers
                                            else -> Icons.Default.FiberManualRecord
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            track.isRecording -> Color.White
                                            track.hasAudio -> AccentPurple
                                            else -> AccentRed
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text(
                                        when {
                                            track.isRecording -> "Stop Rec"
                                            track.hasAudio -> "Overdub"
                                            else -> "Record"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                // Play / Pause Button
                                if (track.hasAudio) {
                                    Button(
                                        onClick = { onToggleTrackPlayback(track.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (track.isPlaying) AccentGreen else DarkSurface
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .border(1.dp, if (track.isPlaying) AccentGreen else DarkBorder, RoundedCornerShape(8.dp))
                                            .testTag("play_track_${track.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (track.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.size(4.dp))
                                        Text(if (track.isPlaying) "Playing" else "Play", fontSize = 13.sp)
                                    }
                                }
                            }

                            // Right controls: Undo, Redo, Mute & Clear
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Undo Action Button
                                IconButton(
                                    onClick = { onUndoTrack(track.id) },
                                    enabled = track.canUndo,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (track.canUndo) DarkSurface else DarkSurface.copy(alpha = 0.35f))
                                        .border(
                                            1.dp,
                                            if (track.canUndo) OrangePrimary.copy(alpha = 0.5f) else DarkBorder.copy(alpha = 0.3f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .testTag("undo_track_${track.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Undo,
                                        contentDescription = "Undo Last Recording / Overdub",
                                        tint = if (track.canUndo) OrangePrimary else Color.Gray.copy(alpha = 0.35f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Redo Action Button
                                IconButton(
                                    onClick = { onRedoTrack(track.id) },
                                    enabled = track.canRedo,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (track.canRedo) DarkSurface else DarkSurface.copy(alpha = 0.35f))
                                        .border(
                                            1.dp,
                                            if (track.canRedo) AccentBlue.copy(alpha = 0.5f) else DarkBorder.copy(alpha = 0.3f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .testTag("redo_track_${track.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Redo,
                                        contentDescription = "Redo Last Recording / Overdub",
                                        tint = if (track.canRedo) AccentBlue else Color.Gray.copy(alpha = 0.35f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (track.hasAudio) {
                                    // Quick Mute / Unmute
                                    IconButton(
                                        onClick = {
                                            if (track.volume > 0) onSetTrackVolume(track.id, 0)
                                            else onSetTrackVolume(track.id, 80)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (track.volume == 0) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                            contentDescription = "Mute",
                                            tint = if (track.volume == 0) AccentYellow else TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Clear Track
                                    IconButton(
                                        onClick = { onClearTrack(track.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Clear Layer",
                                            tint = AccentRed.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Volume Control Strip
                        if (track.hasAudio) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Volume", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Text("${track.volume}%", style = MaterialTheme.typography.labelSmall, color = trackColor)
                            }

                            Slider(
                                value = track.volume.toFloat(),
                                onValueChange = { onSetTrackVolume(track.id, it.toInt()) },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = trackColor,
                                    activeTrackColor = trackColor,
                                    inactiveTrackColor = DarkBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("volume_slider_${track.id}")
                            )
                        }
                    }
                }
            }
        }
    }

    // Loop Region & Duration Editor Modal Dialog
    trackForDurationEdit?.let { targetTrack ->
        LoopDurationRegionDialog(
            track = targetTrack,
            onDismiss = { trackForDurationEdit = null },
            onApplyDuration = { durationSec ->
                onSetTrackDuration(targetTrack.id, durationSec)
                trackForDurationEdit = null
            },
            onApplyRegion = { startSec, endSec ->
                onSetTrackLoopRegion(targetTrack.id, startSec, endSec)
                trackForDurationEdit = null
            },
            onResetDuration = {
                onResetTrackDuration(targetTrack.id)
                trackForDurationEdit = null
            },
            onDoubleDuration = {
                onDoubleTrackDuration(targetTrack.id)
                trackForDurationEdit = null
            },
            onHalveDuration = {
                onHalveTrackDuration(targetTrack.id)
                trackForDurationEdit = null
            },
            onSyncOthersToThis = {
                onSyncAllTracksToMaster(targetTrack.id)
                trackForDurationEdit = null
            }
        )
    }
}

@Composable
fun LoopDurationRegionDialog(
    track: LoopTrack,
    onDismiss: () -> Unit,
    onApplyDuration: (Float) -> Unit,
    onApplyRegion: (Float, Float) -> Unit,
    onResetDuration: () -> Unit,
    onDoubleDuration: () -> Unit,
    onHalveDuration: () -> Unit,
    onSyncOthersToThis: () -> Unit
) {
    var editDuration by remember { mutableFloatStateOf(track.durationSeconds) }
    var startTrim by remember { mutableFloatStateOf(track.startTrimSeconds) }
    var endTrim by remember { mutableFloatStateOf(if (track.endTrimSeconds > 0f) track.endTrimSeconds else track.durationSeconds) }
    val maxRaw = track.rawDurationSeconds.coerceAtLeast(track.durationSeconds).coerceAtLeast(1.0f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.ContentCut, contentDescription = null, tint = OrangePrimary)
                Text(
                    text = "${track.name}: Edit Loop Duration",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Duration Stats Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ACTIVE LOOP DURATION", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = String.format(Locale.US, "%.2f seconds", editDuration),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = OrangePrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ORIGINAL RECORDED", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = String.format(Locale.US, "%.2fs", maxRaw),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                // Quick Multiplier & Bar Presets
                Text("QUICK DURATION PRESETS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            editDuration = (editDuration * 0.5f).coerceAtLeast(0.5f)
                            endTrim = (startTrim + editDuration).coerceAtMost(maxRaw)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                    ) {
                        Text("1/2x", fontSize = 11.sp, color = OrangePrimary)
                    }

                    Button(
                        onClick = {
                            editDuration = (editDuration * 2.0f).coerceAtMost(32.0f)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                    ) {
                        Text("2x", fontSize = 11.sp, color = OrangePrimary)
                    }

                    Button(
                        onClick = {
                            editDuration = 2.0f
                            endTrim = (startTrim + 2.0f).coerceAtMost(maxRaw)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                    ) {
                        Text("2.0s", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            editDuration = 4.0f
                            endTrim = (startTrim + 4.0f).coerceAtMost(maxRaw)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                    ) {
                        Text("4.0s", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            editDuration = 8.0f
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f).border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                    ) {
                        Text("8.0s", fontSize = 11.sp, color = Color.White)
                    }
                }

                // Fine-tune Slider & Stepper Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Duration Adjustment", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                editDuration = (editDuration - 0.25f).coerceAtLeast(0.5f)
                            },
                            modifier = Modifier.size(28.dp).background(DarkSurfaceElevated, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Minus", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = {
                                editDuration = (editDuration + 0.25f).coerceAtMost(32f)
                            },
                            modifier = Modifier.size(28.dp).background(DarkSurfaceElevated, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Plus", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Slider(
                    value = editDuration,
                    onValueChange = { editDuration = it },
                    valueRange = 0.5f..16.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = OrangePrimary,
                        activeTrackColor = OrangePrimary,
                        inactiveTrackColor = DarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("duration_slider")
                )

                // Region Start & End Trim Controls
                Text("REGION BOUNDARIES (TRIM)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Start Trim", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(String.format(Locale.US, "%.2fs", startTrim), style = MaterialTheme.typography.labelSmall, color = OrangePrimary)
                    }
                    Slider(
                        value = startTrim,
                        onValueChange = {
                            startTrim = it.coerceAtMost(endTrim - 0.2f)
                            editDuration = endTrim - startTrim
                        },
                        valueRange = 0f..maxRaw,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentBlue,
                            activeTrackColor = AccentBlue,
                            inactiveTrackColor = DarkBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("End Trim", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(String.format(Locale.US, "%.2fs", endTrim), style = MaterialTheme.typography.labelSmall, color = AccentGreen)
                    }
                    Slider(
                        value = endTrim,
                        onValueChange = {
                            endTrim = it.coerceAtLeast(startTrim + 0.2f)
                            editDuration = endTrim - startTrim
                        },
                        valueRange = 0f..maxRaw,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentGreen,
                            activeTrackColor = AccentGreen,
                            inactiveTrackColor = DarkBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Sync All Layers to this Duration
                Button(
                    onClick = onSyncOthersToThis,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentBlue)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Sync Other Layers to ${String.format(Locale.US, "%.2fs", editDuration)}", color = Color.White, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (startTrim > 0f || endTrim < maxRaw) {
                        onApplyRegion(startTrim, endTrim)
                    } else {
                        onApplyDuration(editDuration)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                modifier = Modifier.testTag("apply_duration_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text("Apply Duration")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onResetDuration) {
                    Text("Reset Raw", color = TextSecondary)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", color = TextSecondary)
                }
            }
        },
        containerColor = DarkSurface
    )
}
