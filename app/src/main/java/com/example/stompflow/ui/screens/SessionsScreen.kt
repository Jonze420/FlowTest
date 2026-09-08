package com.example.stompflow.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.stompflow.data.local.SessionEntity
import com.example.stompflow.data.model.Patch
import com.example.stompflow.data.model.RhythmPattern
import com.example.stompflow.ui.theme.AccentRed
import com.example.stompflow.ui.theme.DarkBackground
import com.example.stompflow.ui.theme.DarkBorder
import com.example.stompflow.ui.theme.DarkSurface
import com.example.stompflow.ui.theme.DarkSurfaceElevated
import com.example.stompflow.ui.theme.OrangePrimary
import com.example.stompflow.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionsScreen(
    currentPatch: Patch,
    currentPattern: RhythmPattern,
    sessions: List<SessionEntity>,
    onSaveSession: (String) -> Unit,
    onLoadSession: (SessionEntity) -> Unit,
    onDeleteSession: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var sessionNameInput by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Active Setup Snapshot Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Song Setup",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Bundle active Patch and Rhythm together",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                sessionNameInput = "${currentPatch.name} + ${currentPattern.name}"
                                showSaveDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_session_button")
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Save Setup")
                        }
                    }

                    // Setup specs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ACTIVE PATCH", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(currentPatch.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OrangePrimary)
                            Text("${currentPatch.chain.size} Pedals", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        Box(modifier = Modifier.size(1.dp, 36.dp).background(DarkBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ACTIVE RHYTHM", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(currentPattern.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OrangePrimary)
                            Text("${currentPattern.bpm} BPM", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Saved Sessions list title
        item {
            Text(
                text = "SAVED SONG SETUPS (${sessions.size})",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        if (sessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No saved song setups yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Save your current Patch and Rhythm settings above to recall them instantly during rehearsals or live shows.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        } else {
            items(sessions) { session ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                        .testTag("session_card_${session.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = session.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Patch: ${session.patchName}  ·  Rhythm: ${session.patternName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OrangePrimary
                            )
                            Text(
                                text = dateFormat.format(Date(session.createdAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onLoadSession(session) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Load", fontSize = 12.sp)
                            }

                            IconButton(
                                onClick = { onDeleteSession(session.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Session",
                                    tint = AccentRed.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Save Session Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Song Setup", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = sessionNameInput,
                    onValueChange = { sessionNameInput = it },
                    label = { Text("Setup Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (sessionNameInput.isNotBlank()) {
                            onSaveSession(sessionNameInput.trim())
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
