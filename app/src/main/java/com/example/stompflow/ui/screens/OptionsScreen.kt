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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stompflow.audio.TunerState
import com.example.stompflow.ui.theme.AccentGreen
import com.example.stompflow.ui.theme.AccentRed
import com.example.stompflow.ui.theme.AccentYellow
import com.example.stompflow.ui.theme.DarkBackground
import com.example.stompflow.ui.theme.DarkBorder
import com.example.stompflow.ui.theme.DarkSurface
import com.example.stompflow.ui.theme.DarkSurfaceElevated
import com.example.stompflow.ui.theme.OrangePrimary
import com.example.stompflow.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun OptionsScreen(
    tunerState: TunerState,
    hasAudioPermission: Boolean,
    onRequestAudioPermission: () -> Unit,
    onToggleTuner: () -> Unit,
    onResetAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Chromatic Guitar Tuner Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, if (tunerState.isInTune) AccentGreen else DarkBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
                    .testTag("tuner_card")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Chromatic Guitar Tuner",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Standard E · A · D · G · B · E",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                if (!hasAudioPermission) {
                                    onRequestAudioPermission()
                                }
                                onToggleTuner()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tunerState.isListening) AccentRed else OrangePrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("toggle_tuner_button")
                        ) {
                            Icon(
                                imageVector = if (tunerState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(if (tunerState.isListening) "Stop Tuner" else "Tune Guitar")
                        }
                    }

                    // Main Note Display Box
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(
                                3.dp,
                                when {
                                    !tunerState.isListening -> DarkBorder
                                    tunerState.isInTune -> AccentGreen
                                    else -> OrangePrimary
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (tunerState.isListening) tunerState.noteName else "--",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = when {
                                    !tunerState.isListening -> TextSecondary
                                    tunerState.isInTune -> AccentGreen
                                    else -> Color.White
                                }
                            )
                            if (tunerState.isListening && tunerState.octave > 0) {
                                Text(
                                    text = "Octave ${tunerState.octave}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Frequency & In-Tune status pill
                    if (tunerState.isListening) {
                        Text(
                            text = String.format(Locale.US, "%.1f Hz", tunerState.frequency),
                            style = MaterialTheme.typography.titleMedium,
                            color = OrangePrimary
                        )

                        Text(
                            text = if (tunerState.isInTune) "IN TUNE" else if (tunerState.cents < 0) "FLAT (${String.format(Locale.US, "%.1f", tunerState.cents)}¢)" else "SHARP (+${String.format(Locale.US, "%.1f", tunerState.cents)}¢)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (tunerState.isInTune) AccentGreen else AccentYellow
                        )
                    }

                    // Cents Meter Needle Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkBackground)
                    ) {
                        val centerY = size.height / 2
                        val centerX = size.width / 2

                        // Center in-tune mark
                        drawLine(
                            color = AccentGreen,
                            start = Offset(centerX, 2f),
                            end = Offset(centerX, size.height - 2f),
                            strokeWidth = 3f
                        )

                        // Baseline
                        drawLine(
                            color = DarkBorder,
                            start = Offset(10f, centerY),
                            end = Offset(size.width - 10f, centerY),
                            strokeWidth = 1f
                        )

                        // Needle indicator
                        if (tunerState.isListening) {
                            val ratio = (tunerState.cents / 50f).coerceIn(-1f, 1f)
                            val needleX = centerX + ratio * (centerX - 20f)
                            drawCircle(
                                color = if (tunerState.isInTune) AccentGreen else OrangePrimary,
                                radius = 6f,
                                center = Offset(needleX, centerY)
                            )
                            drawLine(
                                color = if (tunerState.isInTune) AccentGreen else OrangePrimary,
                                start = Offset(needleX, 4f),
                                end = Offset(needleX, size.height - 4f),
                                strokeWidth = 3f
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("-50¢", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("0¢", style = MaterialTheme.typography.labelSmall, color = AccentGreen)
                        Text("+50¢", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        }

        // Hardware Audio Setup Card
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
                        text = "AUDIO ROUTING & DSP",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "Input Stream: Android Microphone / USB Audio Interface (16-bit PCM 44.1kHz)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Output Stream: Low-latency AudioTrack Streaming Engine",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Audio Latency: Dynamic Buffer Synchronization (Non-blocking)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrangePrimary
                    )
                }
            }
        }

        // Reset App Data
        item {
            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AccentRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .testTag("reset_data_button")
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text("Reset All Patches & Sessions to Factory Defaults", color = AccentRed)
            }
        }
    }

    // Reset Confirm Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset to Factory Defaults?", color = Color.White) },
            text = {
                Text(
                    "This will restore all factory presets and remove custom patches, patterns, and song sessions.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
