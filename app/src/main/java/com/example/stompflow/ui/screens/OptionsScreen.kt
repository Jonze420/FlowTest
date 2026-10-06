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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stompflow.audio.AudioDeviceManager
import com.example.stompflow.audio.AudioSetupState
import com.example.stompflow.audio.TunerState
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
fun OptionsScreen(
    tunerState: TunerState,
    setupState: AudioSetupState,
    hasAudioPermission: Boolean,
    onRequestAudioPermission: () -> Unit,
    onToggleTuner: () -> Unit,
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
    onResetAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var isInputDropdownOpen by remember { mutableStateOf(false) }
    var isOutputDropdownOpen by remember { mutableStateOf(false) }
    var isEngineDropdownOpen by remember { mutableStateOf(false) }

    val activeInput = setupState.inputDevices.firstOrNull { it.id == setupState.selectedInputId }
        ?: setupState.inputDevices.firstOrNull()
    val activeOutput = setupState.outputDevices.firstOrNull { it.id == setupState.selectedOutputId }
        ?: setupState.outputDevices.firstOrNull()
    val activeEngine = AudioDeviceManager.DSP_ENGINES.firstOrNull { it.id == setupState.selectedEngineId }
        ?: AudioDeviceManager.DSP_ENGINES[1]

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // ==========================================
        // 1. INPUT DEVICE SECTION (stomp-flow.org)
        // ==========================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
                    .testTag("options_input_device_section")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "INPUT DEVICE",
                            style = MaterialTheme.typography.titleSmall.copy(
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
                                .padding(horizontal = 12.dp, vertical = 10.dp),
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

                    Text(
                        text = "For guitar, use an audio interface with Hi-Z input. Disable system echo cancellation for best results.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    // Input Preamp Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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

                    // Toggle controls
                    SettingToggleRow(
                        title = "Echo Cancellation",
                        subtitle = "Disable for transparent guitar tone",
                        checked = setupState.echoCancellation,
                        onCheckedChange = onSetEchoCancellation
                    )
                    SettingToggleRow(
                        title = "Noise Suppression",
                        subtitle = "Disable for raw dynamics and harmonics",
                        checked = setupState.noiseSuppression,
                        onCheckedChange = onSetNoiseSuppression
                    )
                    SettingToggleRow(
                        title = "Auto Gain Control",
                        subtitle = "Disable for natural picking dynamics",
                        checked = setupState.autoGainControl,
                        onCheckedChange = onSetAutoGainControl
                    )
                }
            }
        }

        // ==========================================
        // 2. OUTPUT DEVICE SECTION (stomp-flow.org)
        // ==========================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
                    .testTag("options_output_device_section")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "OUTPUT DEVICE",
                            style = MaterialTheme.typography.titleSmall.copy(
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
                                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                                    color = if (isSelected) AccentCyan else Color.White,
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
                                                    tint = AccentCyan,
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

                    Text(
                        text = "Headphones or wired monitors are recommended for live playing to avoid feedback.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    // Output Volume Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Master Volume: ${(setupState.outputVolume * 100).toInt()}%",
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
                        subtitle = "Hardware audio interface pass-through",
                        checked = setupState.directMonitoring,
                        onCheckedChange = onSetDirectMonitoring
                    )
                }
            }
        }

        // ==========================================
        // 3. DSP ENGINE & LATENCY (stomp-flow.org)
        // ==========================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
                    .testTag("options_dsp_engine_section")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "DSP ENGINE & LATENCY",
                            style = MaterialTheme.typography.titleSmall.copy(
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
                                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                    text = "${activeEngine.sampleRate} Hz · ${activeEngine.description}",
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
                        text = "Lower latency presets are better for live playing but may use more CPU.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )

                    // Telemetry 4 Cards
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

                    // Connect/Disconnect Audio button
                    Button(
                        onClick = onToggleAudioConnect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (setupState.isAudioConnected) AccentRed else OrangePrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Cable, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(if (setupState.isAudioConnected) "Disconnect Audio Stream" else "Connect Audio Stream")
                    }
                }
            }
        }

        // ==========================================
        // 4. CHROMATIC GUITAR TUNER CARD
        // ==========================================
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
                            .size(110.dp)
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
                                    fontSize = 42.sp,
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

                        drawLine(
                            color = AccentGreen,
                            start = Offset(centerX, 2f),
                            end = Offset(centerX, size.height - 2f),
                            strokeWidth = 3f
                        )

                        drawLine(
                            color = DarkBorder,
                            start = Offset(10f, centerY),
                            end = Offset(size.width - 10f, centerY),
                            strokeWidth = 1f
                        )

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

        // ==========================================
        // 5. RESET APP DATA
        // ==========================================
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
