package com.example.stompflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stompflow.data.model.DrumVoice
import com.example.stompflow.data.model.RhythmPattern
import com.example.stompflow.ui.theme.AccentGreen
import com.example.stompflow.ui.theme.AccentPurple
import com.example.stompflow.ui.theme.DarkBackground
import com.example.stompflow.ui.theme.DarkBorder
import com.example.stompflow.ui.theme.DarkSurface
import com.example.stompflow.ui.theme.DarkSurfaceElevated
import com.example.stompflow.ui.theme.OrangePrimary
import com.example.stompflow.ui.theme.TextSecondary
import kotlin.random.Random

@Composable
fun RhythmScreen(
    currentPattern: RhythmPattern,
    allPatterns: List<RhythmPattern>,
    isPlaying: Boolean,
    currentStep: Int,
    onSelectPattern: (RhythmPattern) -> Unit,
    onTogglePlay: () -> Unit,
    onUpdatePattern: (RhythmPattern) -> Unit,
    onSavePattern: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var savePatternName by remember { mutableStateOf("") }
    val gridScrollState = rememberScrollState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Pattern Presets Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RHYTHM PATTERNS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    TextButton(
                        onClick = {
                            savePatternName = "${currentPattern.name} Copy"
                            showSaveDialog = true
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Save Pattern", color = OrangePrimary, style = MaterialTheme.typography.labelSmall)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allPatterns) { pattern ->
                        val isSelected = pattern.id == currentPattern.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) OrangePrimary else DarkSurfaceElevated)
                                .border(1.dp, if (isSelected) OrangePrimary else DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { onSelectPattern(pattern) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("pattern_chip_${pattern.id}")
                        ) {
                            Text(
                                text = pattern.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Transport & Controls Card
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
                    // Play/Stop and Quick Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onTogglePlay,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlaying) Color.DarkGray else AccentGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("rhythm_play_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(if (isPlaying) "Stop" else "Start Drum Machine")
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Generate Idea Groove Button
                            Button(
                                onClick = {
                                    val randomGroove = currentPattern.steps.mapIndexed { vIdx, voiceSteps ->
                                        voiceSteps.mapIndexed { sIdx, _ ->
                                            when (vIdx) {
                                                0 -> sIdx % 4 == 0 || (sIdx == 10 && Random.nextBoolean())
                                                1 -> sIdx == 4 || sIdx == 12
                                                2 -> sIdx % 2 == 0 || (sIdx % 3 == 0 && Random.nextBoolean())
                                                3 -> sIdx == 4 || sIdx == 12
                                                else -> sIdx == 14 && Random.nextBoolean()
                                            }
                                        }
                                    }
                                    onUpdatePattern(currentPattern.copy(steps = randomGroove))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("generate_groove_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Remix", fontSize = 12.sp)
                            }

                            // Clear Pattern
                            Button(
                                onClick = {
                                    val emptySteps = List(5) { List(16) { false } }
                                    onUpdatePattern(currentPattern.copy(steps = emptySteps))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                            }
                        }
                    }

                    // BPM Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tempo (BPM)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("${currentPattern.bpm} BPM", style = MaterialTheme.typography.labelSmall, color = OrangePrimary)
                    }
                    Slider(
                        value = currentPattern.bpm.toFloat(),
                        onValueChange = { onUpdatePattern(currentPattern.copy(bpm = it.toInt())) },
                        valueRange = 50f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = OrangePrimary,
                            activeTrackColor = OrangePrimary,
                            inactiveTrackColor = DarkBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Swing Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Swing Groove", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("${currentPattern.swing}%", style = MaterialTheme.typography.labelSmall, color = OrangePrimary)
                    }
                    Slider(
                        value = currentPattern.swing.toFloat(),
                        onValueChange = { onUpdatePattern(currentPattern.copy(swing = it.toInt())) },
                        valueRange = 0f..80f,
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

        // 16-Step Grid
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "16-STEP SEQUENCER GRID",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )

                    // Scrollable Horizontal Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(gridScrollState)
                    ) {
                        // Voice Labels column
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.width(64.dp)
                        ) {
                            // Empty header space for step numbers
                            Box(modifier = Modifier.height(20.dp))
                            DrumVoice.entries.forEach { voice ->
                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = voice.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = voice.color
                                    )
                                }
                            }
                        }

                        // 16 Steps columns
                        for (step in 0 until 16) {
                            val isPlayhead = isPlaying && currentStep == step
                            val isQuarterNote = step % 4 == 0

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .width(28.dp)
                            ) {
                                // Step number header
                                Text(
                                    text = "${step + 1}",
                                    fontSize = 9.sp,
                                    color = if (isPlayhead) OrangePrimary else if (isQuarterNote) Color.White else TextSecondary,
                                    fontWeight = if (isPlayhead || isQuarterNote) FontWeight.Bold else FontWeight.Normal
                                )

                                // Step pads for each voice
                                DrumVoice.entries.forEachIndexed { vIdx, voice ->
                                    val isActive = vIdx < currentPattern.steps.size &&
                                            step < currentPattern.steps[vIdx].size &&
                                            currentPattern.steps[vIdx][step]

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp, 34.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when {
                                                    isActive && isPlayhead -> Color.White
                                                    isActive -> voice.color
                                                    isPlayhead -> OrangePrimary.copy(alpha = 0.3f)
                                                    isQuarterNote -> DarkSurfaceElevated
                                                    else -> DarkBackground
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                if (isPlayhead) OrangePrimary else DarkBorder,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable {
                                                val updated = currentPattern.steps.mapIndexed { rowIdx, row ->
                                                    if (rowIdx == vIdx) {
                                                        row.mapIndexed { colIdx, cell ->
                                                            if (colIdx == step) !cell else cell
                                                        }
                                                    } else row
                                                }
                                                onUpdatePattern(currentPattern.copy(steps = updated))
                                            }
                                            .testTag("pad_${voice.id}_$step")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Save Pattern Preset Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Rhythm Pattern", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = savePatternName,
                    onValueChange = { savePatternName = it },
                    label = { Text("Pattern Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (savePatternName.isNotBlank()) {
                            onSavePattern(savePatternName.trim())
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
