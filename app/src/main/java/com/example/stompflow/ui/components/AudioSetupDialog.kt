package com.example.stompflow.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.stompflow.audio.AudioDeviceItem
import com.example.stompflow.audio.AudioDeviceManager
import com.example.stompflow.audio.AudioSetupState
import com.example.stompflow.ui.theme.AccentCyan
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
fun AudioSetupDialog(
    setupState: AudioSetupState,
    onSelectInput: (String) -> Unit,
    onSelectOutput: (String) -> Unit,
    onSelectEngine: (String) -> Unit,
    onToggleAudioConnect: () -> Unit,
    onSetEchoCancellation: (Boolean) -> Unit,
    onSetNoiseSuppression: (Boolean) -> Unit,
    onSetAutoGainControl: (Boolean) -> Unit,
    onSetInputGainDb: (Float) -> Unit,
    onSetOutputVolume: (Float) -> Unit,
    onSetDirectMonitoring: (Boolean) -> Unit,
    onDismissRequest: () -> Unit
) {
    var isInputDropdownOpen by remember { mutableStateOf(false) }
    var isOutputDropdownOpen by remember { mutableStateOf(false) }
    var isEngineDropdownOpen by remember { mutableStateOf(false) }

    val activeInput = setupState.inputDevices.firstOrNull { it.id == setupState.selectedInputId }
        ?: setupState.inputDevices.firstOrNull()
    val activeOutput = setupState.outputDevices.firstOrNull { it.id == setupState.selectedOutputId }
        ?: setupState.outputDevices.firstOrNull()
    val activeEngine = AudioDeviceManager.DSP_ENGINES.firstOrNull { it.id == setupState.selectedEngineId }
        ?: AudioDeviceManager.DSP_ENGINES[1]

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .testTag("audio_setup_dialog"),
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with title and close button
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
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(OrangePrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Audio Setup",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Configure devices, routing & latency",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                HorizontalDivider(color = DarkBorder)

                // ==========================================
                // 1. INPUT DEVICE SECTION (stomp-flow.org)
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBackground.copy(alpha = 0.7f))
                        .border(1.dp, DarkBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "INPUT DEVICE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                        }

                        // Dropdown Selector Button
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                    .clickable { isInputDropdownOpen = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("input_device_selector"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeInput?.name ?: "Select input...",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                    if (activeInput != null) {
                                        Text(
                                            text = activeInput.typeDescription,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }

                            DropdownMenu(
                                expanded = isInputDropdownOpen,
                                onDismissRequest = { isInputDropdownOpen = false },
                                modifier = Modifier
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorder)
                            ) {
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
                                                        color = if (isSelected) OrangePrimary else Color.White,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    Text(
                                                        text = device.typeDescription,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TextSecondary
                                                    )
                                                }
                                                if (isSelected) {
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
                                            isInputDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        // Guidance note from stomp-flow.org
                        Text(
                            text = "For guitar, use an audio interface with Hi-Z input. Disable system echo cancellation for best results.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        // Input Level VU Meter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "LEVEL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = TextSecondary,
                                modifier = Modifier.width(36.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DarkSurfaceElevated)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(setupState.inputLevel)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when {
                                                setupState.inputLevel > 0.8f -> AccentRed
                                                setupState.inputLevel > 0.5f -> AccentYellow
                                                setupState.inputLevel > 0.05f -> AccentGreen
                                                else -> DarkBorder
                                            }
                                        )
                                )
                            }
                            Text(
                                text = when {
                                    setupState.inputLevel > 0.8f -> "HOT"
                                    setupState.inputLevel > 0.5f -> "GOOD"
                                    setupState.inputLevel > 0.05f -> "OK"
                                    else -> "SILENT"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = when {
                                    setupState.inputLevel > 0.8f -> AccentRed
                                    setupState.inputLevel > 0.5f -> AccentGreen
                                    else -> TextSecondary
                                },
                                modifier = Modifier.width(44.dp)
                            )
                        }

                        // Input Gain Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Preamp Gain: +${setupState.inputGainDb.toInt()} dB",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                        Slider(
                            value = setupState.inputGainDb,
                            onValueChange = onSetInputGainDb,
                            valueRange = 0f..24f,
                            colors = SliderDefaults.colors(
                                thumbColor = OrangePrimary,
                                activeTrackColor = OrangePrimary,
                                inactiveTrackColor = DarkSurfaceElevated
                            )
                        )

                        // Toggles for Echo Cancellation, Noise Suppression, AGC
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SettingToggleRow(
                                title = "Echo Cancellation",
                                subtitle = "Disable for direct guitar capture",
                                checked = setupState.echoCancellation,
                                onCheckedChange = onSetEchoCancellation
                            )
                            SettingToggleRow(
                                title = "Noise Suppression",
                                subtitle = "Disable for raw instrument dynamics",
                                checked = setupState.noiseSuppression,
                                onCheckedChange = onSetNoiseSuppression
                            )
                            SettingToggleRow(
                                title = "Auto Gain Control",
                                subtitle = "Disable for natural pick attacks",
                                checked = setupState.autoGainControl,
                                onCheckedChange = onSetAutoGainControl
                            )
                        }
                    }
                }

                // ==========================================
                // 2. OUTPUT DEVICE SECTION (stomp-flow.org)
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBackground.copy(alpha = 0.7f))
                        .border(1.dp, DarkBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "OUTPUT DEVICE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                        }

                        // Dropdown Selector Button
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                    .clickable { isOutputDropdownOpen = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("output_device_selector"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeOutput?.name ?: "Select output...",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                    if (activeOutput != null) {
                                        Text(
                                            text = activeOutput.typeDescription,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }

                            DropdownMenu(
                                expanded = isOutputDropdownOpen,
                                onDismissRequest = { isOutputDropdownOpen = false },
                                modifier = Modifier
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorder)
                            ) {
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
                                                        color = if (isSelected) OrangePrimary else Color.White,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    Text(
                                                        text = device.typeDescription,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TextSecondary
                                                    )
                                                }
                                                if (isSelected) {
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
                                            onSelectOutput(device.id)
                                            isOutputDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        // Guidance note from stomp-flow.org
                        Text(
                            text = "Headphones or wired monitors are recommended for live playing to avoid feedback.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        // Master Output Volume Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Master Output Level: ${(setupState.outputVolume * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                        Slider(
                            value = setupState.outputVolume,
                            onValueChange = onSetOutputVolume,
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCyan,
                                activeTrackColor = AccentCyan,
                                inactiveTrackColor = DarkSurfaceElevated
                            )
                        )

                        SettingToggleRow(
                            title = "Direct Monitoring",
                            subtitle = "Zero-latency audio interface pass-through",
                            checked = setupState.directMonitoring,
                            onCheckedChange = onSetDirectMonitoring
                        )
                    }
                }

                // ==========================================
                // 3. DSP ENGINE & LATENCY (stomp-flow.org)
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBackground.copy(alpha = 0.7f))
                        .border(1.dp, DarkBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "DSP ENGINE & LATENCY",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                        }

                        // Engine Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                    .clickable { isEngineDropdownOpen = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("engine_selector"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeEngine.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${activeEngine.sampleRate} Hz · ${activeEngine.latencyHint}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }

                            DropdownMenu(
                                expanded = isEngineDropdownOpen,
                                onDismissRequest = { isEngineDropdownOpen = false },
                                modifier = Modifier
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorder)
                            ) {
                                AudioDeviceManager.DSP_ENGINES.forEach { engine ->
                                    val isSelected = engine.id == setupState.selectedEngineId
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = engine.name,
                                                        color = if (isSelected) OrangePrimary else Color.White,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    Text(
                                                        text = "${engine.sampleRate} Hz · ${engine.description}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TextSecondary
                                                    )
                                                }
                                                if (isSelected) {
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
                                            onSelectEngine(engine.id)
                                            isEngineDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Lower latency presets are better for live playing but may use more CPU. Disconnect before changing.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        // 4-card Telemetry Grid from stomp-flow.org
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TelemetryCard(
                                label = "BASE LATENCY",
                                value = String.format(Locale.US, "%.1f ms", setupState.baseLatencyMs),
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryCard(
                                label = "OUTPUT LATENCY",
                                value = String.format(Locale.US, "%.1f ms", setupState.outputLatencyMs),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TelemetryCard(
                                label = "SAMPLE RATE",
                                value = String.format(Locale.US, "%.1f kHz", setupState.currentSampleRateKhz),
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryCard(
                                label = "CONTEXT",
                                value = setupState.contextState,
                                isAccent = setupState.isAudioConnected,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ==========================================
                // 4. CONNECT / DISCONNECT AUDIO BUTTON
                // ==========================================
                Button(
                    onClick = onToggleAudioConnect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (setupState.isAudioConnected) AccentRed else OrangePrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("toggle_connect_audio_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cable,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (setupState.isAudioConnected) "Disconnect Audio" else "Connect Audio",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceElevated.copy(alpha = 0.8f))
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = if (isAccent) AccentGreen else Color.White
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceElevated.copy(alpha = 0.4f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OrangePrimary,
                checkedTrackColor = OrangePrimary.copy(alpha = 0.5f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = DarkSurfaceElevated
            )
        )
    }
}
