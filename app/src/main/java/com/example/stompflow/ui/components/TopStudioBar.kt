package com.example.stompflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.stompflow.audio.AudioSetupState
import com.example.stompflow.ui.theme.AccentCyan
import com.example.stompflow.ui.theme.AccentGreen
import com.example.stompflow.ui.theme.AccentRed
import com.example.stompflow.ui.theme.DarkBorder
import com.example.stompflow.ui.theme.DarkSurface
import com.example.stompflow.ui.theme.DarkSurfaceElevated
import com.example.stompflow.ui.theme.OrangePrimary
import com.example.stompflow.ui.theme.TextSecondary

@Composable
fun TopStudioBar(
    statusText: String,
    isActiveAudio: Boolean,
    setupState: AudioSetupState,
    onSelectInput: (String) -> Unit,
    onSelectOutput: (String) -> Unit,
    onOpenAudioSetup: () -> Unit,
    onPanicStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isInputMenuOpen by remember { mutableStateOf(false) }
    var isOutputMenuOpen by remember { mutableStateOf(false) }

    val activeInput = setupState.inputDevices.firstOrNull { it.id == setupState.selectedInputId }
        ?: setupState.inputDevices.firstOrNull()
    val activeOutput = setupState.outputDevices.firstOrNull { it.id == setupState.selectedOutputId }
        ?: setupState.outputDevices.firstOrNull()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(1.dp, DarkBorder)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Identity
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(OrangePrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "StompFlow",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White
            )
        }

        // Input & Output Menus, Status and Panic Stop on the right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ==========================================
            // INPUT DEVICE MENU (Top Bar)
            // ==========================================
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, if (isInputMenuOpen) OrangePrimary else DarkBorder, RoundedCornerShape(8.dp))
                        .clickable { isInputMenuOpen = true }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("top_bar_input_menu_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Input Menu",
                        tint = OrangePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "IN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = OrangePrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }

                DropdownMenu(
                    expanded = isInputMenuOpen,
                    onDismissRequest = { isInputMenuOpen = false },
                    modifier = Modifier
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder)
                ) {
                    Text(
                        text = "INPUT DEVICE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = OrangePrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    HorizontalDivider(color = DarkBorder)

                    setupState.inputDevices.forEach { device ->
                        val isSelected = device.id == setupState.selectedInputId
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = device.name,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) OrangePrimary else Color.White
                                        )
                                        Text(
                                            text = device.typeDescription,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = TextSecondary
                                        )
                                    }
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = OrangePrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSelectInput(device.id)
                                isInputMenuOpen = false
                            }
                        )
                    }

                    HorizontalDivider(color = DarkBorder)
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Audio Setup & DSP Settings...",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            }
                        },
                        onClick = {
                            isInputMenuOpen = false
                            onOpenAudioSetup()
                        }
                    )
                }
            }

            // ==========================================
            // OUTPUT DEVICE MENU (Top Bar)
            // ==========================================
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, if (isOutputMenuOpen) AccentCyan else DarkBorder, RoundedCornerShape(8.dp))
                        .clickable { isOutputMenuOpen = true }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("top_bar_output_menu_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Output Menu",
                        tint = AccentCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "OUT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentCyan
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }

                DropdownMenu(
                    expanded = isOutputMenuOpen,
                    onDismissRequest = { isOutputMenuOpen = false },
                    modifier = Modifier
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder)
                ) {
                    Text(
                        text = "OUTPUT DEVICE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AccentCyan,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    HorizontalDivider(color = DarkBorder)

                    setupState.outputDevices.forEach { device ->
                        val isSelected = device.id == setupState.selectedOutputId
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = device.name,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) AccentCyan else Color.White
                                        )
                                        Text(
                                            text = device.typeDescription,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = TextSecondary
                                        )
                                    }
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = AccentCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSelectOutput(device.id)
                                isOutputMenuOpen = false
                            }
                        )
                    }

                    HorizontalDivider(color = DarkBorder)
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Audio Setup & DSP Settings...",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            }
                        },
                        onClick = {
                            isOutputMenuOpen = false
                            onOpenAudioSetup()
                        }
                    )
                }
            }

            // Audio Setup Icon Button
            IconButton(
                onClick = onOpenAudioSetup,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                    .testTag("top_bar_audio_setup_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Audio Setup",
                    tint = if (setupState.isAudioConnected) AccentGreen else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Panic Stop Button
            IconButton(
                onClick = onPanicStop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentRed.copy(alpha = 0.2f))
                    .border(1.dp, AccentRed.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .testTag("panic_stop_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop All Audio",
                    tint = AccentRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
