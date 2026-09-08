package com.example.stompflow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stompflow.ui.components.BottomNavigationBar
import com.example.stompflow.ui.components.StudioTab
import com.example.stompflow.ui.components.TopStudioBar
import com.example.stompflow.ui.screens.LooperScreen
import com.example.stompflow.ui.screens.OptionsScreen
import com.example.stompflow.ui.screens.PatchesScreen
import com.example.stompflow.ui.screens.RhythmScreen
import com.example.stompflow.ui.screens.SessionsScreen
import com.example.stompflow.ui.theme.DarkBackground
import com.example.stompflow.ui.theme.StompFlowTheme

class MainActivity : ComponentActivity() {

    private val viewModel: StompFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StompFlowTheme {
                MainApp(viewModel = viewModel, onCheckAudioPermission = {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                })
            }
        }
    }
}

@Composable
fun MainApp(
    viewModel: StompFlowViewModel,
    onCheckAudioPermission: () -> Boolean
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val hasAudioPermission by viewModel.hasAudioPermission.collectAsStateWithLifecycle()

    val activePatch by viewModel.activePatch.collectAsStateWithLifecycle()
    val patches by viewModel.patches.collectAsStateWithLifecycle()
    val activePattern by viewModel.activePattern.collectAsStateWithLifecycle()
    val patterns by viewModel.patterns.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()

    val isRhythmPlaying by viewModel.isRhythmPlaying.collectAsStateWithLifecycle()
    val rhythmCurrentStep by viewModel.rhythmCurrentStep.collectAsStateWithLifecycle()
    val loopTracks by viewModel.loopTracks.collectAsStateWithLifecycle()
    val isLooperPlaying by viewModel.isLooperPlaying.collectAsStateWithLifecycle()
    val isAuditionPlaying by viewModel.isAuditionPlaying.collectAsStateWithLifecycle()
    val tunerState by viewModel.tunerState.collectAsStateWithLifecycle()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setAudioPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        viewModel.setAudioPermissionGranted(onCheckAudioPermission())
    }

    val isAnyRecording = loopTracks.any { it.isRecording }
    val isActiveAudio = isRhythmPlaying || isLooperPlaying || isAuditionPlaying || tunerState.isListening || isAnyRecording

    val statusText = when {
        isAnyRecording -> "Recording"
        tunerState.isListening -> "Tuning"
        isRhythmPlaying && isLooperPlaying -> "Rhythm + Looper"
        isRhythmPlaying -> "Rhythm Playing"
        isLooperPlaying -> "Looper Playing"
        isAuditionPlaying -> "Auditioning"
        else -> "Standby"
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopStudioBar(
                statusText = statusText,
                isActiveAudio = isActiveAudio,
                onPanicStop = { viewModel.panicStopAll() },
                modifier = Modifier.padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding())
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onSelectTab = { viewModel.selectTab(it) },
                modifier = Modifier.padding(bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding())
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                StudioTab.PATCHES -> PatchesScreen(
                    currentPatch = activePatch,
                    allPatches = patches,
                    isPlayingPreview = isAuditionPlaying,
                    onSelectPatch = { viewModel.selectPatch(it) },
                    onSaveNewPatch = { viewModel.saveNewPatch(it) },
                    onUpdatePatch = { viewModel.updateActivePatch(it) },
                    onPlayAudition = { viewModel.playAuditionRiff() },
                    onStopAudition = { viewModel.stopAudition() }
                )
                StudioTab.RHYTHM -> RhythmScreen(
                    currentPattern = activePattern,
                    allPatterns = patterns,
                    isPlaying = isRhythmPlaying,
                    currentStep = rhythmCurrentStep,
                    onSelectPattern = { viewModel.selectPattern(it) },
                    onTogglePlay = { viewModel.toggleRhythmPlayback() },
                    onUpdatePattern = { viewModel.updateActivePattern(it) },
                    onSavePattern = { viewModel.saveNewPattern(it) }
                )
                StudioTab.LOOPER -> LooperScreen(
                    tracks = loopTracks,
                    isGlobalPlaying = isLooperPlaying,
                    hasAudioPermission = hasAudioPermission,
                    onRequestAudioPermission = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onStartRecording = { trackId ->
                        viewModel.startLoopRecording(trackId)
                    },
                    onStopRecording = {
                        viewModel.stopLoopRecording()
                    },
                    onToggleTrackPlayback = { trackId ->
                        viewModel.toggleLoopTrackPlayback(trackId)
                    },
                    onSetTrackVolume = { trackId, vol ->
                        viewModel.setLoopTrackVolume(trackId, vol)
                    },
                    onClearTrack = { trackId ->
                        viewModel.clearLoopTrack(trackId)
                    },
                    onToggleGlobalPlay = {
                        viewModel.toggleLooperGlobalPlay()
                    },
                    onResetAll = {
                        viewModel.resetLooperAll()
                    }
                )
                StudioTab.SESSIONS -> SessionsScreen(
                    currentPatch = activePatch,
                    currentPattern = activePattern,
                    sessions = sessions,
                    onSaveSession = { name ->
                        viewModel.saveCurrentSession(name)
                    },
                    onLoadSession = { session ->
                        viewModel.loadSession(session)
                    },
                    onDeleteSession = { sessionId ->
                        viewModel.deleteSession(sessionId)
                    }
                )
                StudioTab.OPTIONS -> OptionsScreen(
                    tunerState = tunerState,
                    hasAudioPermission = hasAudioPermission,
                    onRequestAudioPermission = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onToggleTuner = {
                        viewModel.toggleTuner()
                    },
                    onResetAllData = {
                        viewModel.resetAllData()
                    }
                )
            }
        }
    }
}
