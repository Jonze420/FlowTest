package com.example.stompflow.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.stompflow.data.model.EffectNode
import com.example.stompflow.data.model.EffectType
import com.example.stompflow.data.model.Patch
import com.example.stompflow.ui.components.EffectCardView
import com.example.stompflow.ui.theme.DarkBackground
import com.example.stompflow.ui.theme.DarkBorder
import com.example.stompflow.ui.theme.DarkSurface
import com.example.stompflow.ui.theme.DarkSurfaceElevated
import com.example.stompflow.ui.theme.OrangePrimary
import com.example.stompflow.ui.theme.TextSecondary
import java.util.UUID

@Composable
fun PatchesScreen(
    currentPatch: Patch,
    allPatches: List<Patch>,
    isPlayingPreview: Boolean,
    onSelectPatch: (Patch) -> Unit,
    onSaveNewPatch: (String) -> Unit,
    onUpdatePatch: (Patch) -> Unit,
    onPlayAudition: () -> Unit,
    onStopAudition: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddEffectDialog by remember { mutableStateOf(false) }
    var showSavePatchDialog by remember { mutableStateOf(false) }
    var newPatchName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Preset / Patch Selector Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE PATCH",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    TextButton(
                        onClick = {
                            newPatchName = "${currentPatch.name} Copy"
                            showSavePatchDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Save Preset", color = OrangePrimary, style = MaterialTheme.typography.labelSmall)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allPatches) { patch ->
                        val isSelected = patch.id == currentPatch.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) OrangePrimary else DarkSurfaceElevated)
                                .border(1.dp, if (isSelected) OrangePrimary else DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { onSelectPatch(patch) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("patch_chip_${patch.id}")
                        ) {
                            Text(
                                text = patch.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Audition & Master Output Card
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
                                text = "Tone Audition Preview",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Listen to current pedals with guitar riff",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                if (isPlayingPreview) onStopAudition() else onPlayAudition()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlayingPreview) Color.DarkGray else OrangePrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("audition_button")
                        ) {
                            Icon(
                                imageVector = if (isPlayingPreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(if (isPlayingPreview) "Playing..." else "Audition")
                        }
                    }

                    // Master Level Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Master Level",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "${currentPatch.master}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangePrimary
                        )
                    }

                    Slider(
                        value = currentPatch.master.toFloat(),
                        onValueChange = { onUpdatePatch(currentPatch.copy(master = it.toInt())) },
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

        // Section Title: Pedal Chain
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SIGNAL CHAIN (${currentPatch.chain.size} PEDALS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Button(
                    onClick = { showAddEffectDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_effect_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Add Pedal", fontSize = 12.sp)
                }
            }
        }

        // List of Effect Cards
        itemsIndexed(currentPatch.chain) { index, effect ->
            EffectCardView(
                effect = effect,
                index = index,
                totalCount = currentPatch.chain.size,
                onToggleEnabled = { enabled ->
                    val updatedChain = currentPatch.chain.toMutableList().apply {
                        this[index] = effect.copy(enabled = enabled)
                    }
                    onUpdatePatch(currentPatch.copy(chain = updatedChain))
                },
                onParamChange = { param ->
                    val updatedChain = currentPatch.chain.toMutableList().apply {
                        this[index] = effect.copy(param = param)
                    }
                    onUpdatePatch(currentPatch.copy(chain = updatedChain))
                },
                onMoveUp = {
                    if (index > 0) {
                        val updatedChain = currentPatch.chain.toMutableList().apply {
                            val temp = this[index]
                            this[index] = this[index - 1]
                            this[index - 1] = temp
                        }
                        onUpdatePatch(currentPatch.copy(chain = updatedChain))
                    }
                },
                onMoveDown = {
                    if (index < currentPatch.chain.size - 1) {
                        val updatedChain = currentPatch.chain.toMutableList().apply {
                            val temp = this[index]
                            this[index] = this[index + 1]
                            this[index + 1] = temp
                        }
                        onUpdatePatch(currentPatch.copy(chain = updatedChain))
                    }
                },
                onDelete = {
                    val updatedChain = currentPatch.chain.toMutableList().apply {
                        removeAt(index)
                    }
                    onUpdatePatch(currentPatch.copy(chain = updatedChain))
                }
            )
        }
    }

    // Add Effect Dialog
    if (showAddEffectDialog) {
        AlertDialog(
            onDismissRequest = { showAddEffectDialog = false },
            title = { Text("Choose Stompbox Pedal", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EffectType.entries.forEach { type ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, type.color.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clickable {
                                    val newEffect = EffectNode(
                                        uid = UUID.randomUUID().toString(),
                                        effectType = type,
                                        enabled = true,
                                        param = type.defaultParam
                                    )
                                    onUpdatePatch(currentPatch.copy(chain = currentPatch.chain + newEffect))
                                    showAddEffectDialog = false
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(type.color)
                                )
                                Text(
                                    text = type.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = type.paramName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddEffectDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Save Patch Preset Dialog
    if (showSavePatchDialog) {
        AlertDialog(
            onDismissRequest = { showSavePatchDialog = false },
            title = { Text("Save Patch Preset", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newPatchName,
                    onValueChange = { newPatchName = it },
                    label = { Text("Patch Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPatchName.isNotBlank()) {
                            onSaveNewPatch(newPatchName.trim())
                            showSavePatchDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePatchDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
