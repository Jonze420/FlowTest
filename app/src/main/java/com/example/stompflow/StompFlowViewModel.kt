package com.example.stompflow

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stompflow.audio.AudioDeviceManager
import com.example.stompflow.audio.AudioSetupState
import com.example.stompflow.audio.GuitarDspEngine
import com.example.stompflow.audio.LooperEngine
import com.example.stompflow.audio.RhythmEngine
import com.example.stompflow.audio.TunerEngine
import com.example.stompflow.audio.TunerState
import com.example.stompflow.data.local.SessionEntity
import com.example.stompflow.data.local.StompFlowDatabase
import com.example.stompflow.data.model.LoopTrack
import com.example.stompflow.data.model.Patch
import com.example.stompflow.data.model.RhythmPattern
import com.example.stompflow.data.repository.StompFlowRepository
import com.example.stompflow.ui.components.StudioTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StompFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StompFlowRepository
    val rhythmEngine = RhythmEngine()
    val looperEngine = LooperEngine()
    val tunerEngine = TunerEngine()
    val guitarDspEngine = GuitarDspEngine()
    val audioDeviceManager = AudioDeviceManager(application)

    private val _selectedTab = MutableStateFlow(StudioTab.PATCHES)
    val selectedTab: StateFlow<StudioTab> = _selectedTab.asStateFlow()

    private val _hasAudioPermission = MutableStateFlow(false)
    val hasAudioPermission: StateFlow<Boolean> = _hasAudioPermission.asStateFlow()

    val patches: StateFlow<List<Patch>>
    val patterns: StateFlow<List<RhythmPattern>>
    val sessions: StateFlow<List<SessionEntity>>

    private val _activePatch = MutableStateFlow(Patch.DEFAULT_PATCH)
    val activePatch: StateFlow<Patch> = _activePatch.asStateFlow()

    private val _activePattern = MutableStateFlow(RhythmPattern.DEFAULT_PATTERN)
    val activePattern: StateFlow<RhythmPattern> = _activePattern.asStateFlow()

    // Engine UI States
    val isRhythmPlaying: StateFlow<Boolean> = rhythmEngine.isPlaying
    val rhythmCurrentStep: StateFlow<Int> = rhythmEngine.currentStep
    val loopTracks: StateFlow<List<LoopTrack>> = looperEngine.tracks
    val isLooperPlaying: StateFlow<Boolean> = looperEngine.isGlobalPlaying
    val isAuditionPlaying: StateFlow<Boolean> = guitarDspEngine.isPlayingPreview
    val tunerState: StateFlow<TunerState> = tunerEngine.tunerState
    val audioSetupState: StateFlow<AudioSetupState> = audioDeviceManager.setupState

    init {
        val db = StompFlowDatabase.getDatabase(application)
        repository = StompFlowRepository(db.sessionDao())

        patches = repository.patches
        patterns = repository.patterns
        sessions = repository.sessions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        audioDeviceManager.startTelemetry(viewModelScope)
    }

    fun selectTab(tab: StudioTab) {
        _selectedTab.value = tab
    }

    fun setAudioPermissionGranted(granted: Boolean) {
        _hasAudioPermission.value = granted
    }

    fun selectPatch(patch: Patch) {
        _activePatch.value = patch
    }

    fun saveNewPatch(name: String) {
        val created = repository.addCustomPatch(name, _activePatch.value)
        _activePatch.value = created
    }

    fun updateActivePatch(patch: Patch) {
        _activePatch.value = patch
        repository.updatePatch(patch)
    }

    fun playAuditionRiff() {
        guitarDspEngine.playAuditionRiff(_activePatch.value, viewModelScope)
    }

    fun stopAudition() {
        guitarDspEngine.stopAudition()
    }

    fun selectPattern(pattern: RhythmPattern) {
        _activePattern.value = pattern
        rhythmEngine.updatePattern(pattern)
    }

    fun saveNewPattern(name: String) {
        val created = repository.addCustomPattern(name, _activePattern.value)
        _activePattern.value = created
        rhythmEngine.updatePattern(created)
    }

    fun updateActivePattern(pattern: RhythmPattern) {
        _activePattern.value = pattern
        repository.updatePattern(pattern)
        rhythmEngine.updatePattern(pattern)
    }

    fun toggleRhythmPlayback() {
        if (rhythmEngine.isPlaying.value) {
            rhythmEngine.stop()
        } else {
            rhythmEngine.start(_activePattern.value, viewModelScope)
        }
    }

    fun startLoopRecording(trackId: Int) {
        looperEngine.startRecording(trackId, viewModelScope, _hasAudioPermission.value)
    }

    fun stopLoopRecording() {
        looperEngine.stopRecording()
    }

    fun toggleLoopTrackPlayback(trackId: Int) {
        looperEngine.toggleTrackPlayback(trackId)
    }

    fun setLoopTrackVolume(trackId: Int, volume: Int) {
        looperEngine.setTrackVolume(trackId, volume)
    }

    fun clearLoopTrack(trackId: Int) {
        looperEngine.clearTrack(trackId)
    }

    fun undoLoopTrack(trackId: Int) {
        looperEngine.undoTrack(trackId)
    }

    fun redoLoopTrack(trackId: Int) {
        looperEngine.redoTrack(trackId)
    }

    fun setTrackDuration(trackId: Int, durationSeconds: Float) {
        looperEngine.setTrackDuration(trackId, durationSeconds)
    }

    fun setTrackLoopRegion(trackId: Int, startTrimSeconds: Float, endTrimSeconds: Float) {
        looperEngine.setTrackLoopRegion(trackId, startTrimSeconds, endTrimSeconds)
    }

    fun resetTrackDuration(trackId: Int) {
        looperEngine.resetTrackDuration(trackId)
    }

    fun doubleTrackDuration(trackId: Int) {
        looperEngine.doubleTrackDuration(trackId)
    }

    fun halveTrackDuration(trackId: Int) {
        looperEngine.halveTrackDuration(trackId)
    }

    fun syncAllTracksToMasterDuration(trackId: Int = 1) {
        val masterTrack = loopTracks.value.firstOrNull { it.id == trackId && it.hasAudio }
            ?: loopTracks.value.firstOrNull { it.hasAudio }
        if (masterTrack != null) {
            looperEngine.syncAllTracksToDuration(masterTrack.durationSeconds)
        }
    }

    fun toggleLooperGlobalPlay() {
        if (looperEngine.isGlobalPlaying.value) {
            looperEngine.stopAll()
        } else {
            looperEngine.startAll(viewModelScope)
        }
    }

    fun resetLooperAll() {
        looperEngine.stopAll()
        for (i in 1..4) {
            looperEngine.clearTrack(i)
        }
    }

    fun toggleTuner() {
        if (tunerEngine.tunerState.value.isListening) {
            tunerEngine.stopListening()
        } else {
            tunerEngine.startListening(viewModelScope, _hasAudioPermission.value)
        }
    }

    fun saveCurrentSession(name: String) {
        viewModelScope.launch {
            repository.saveSession(
                name = name,
                patchId = _activePatch.value.id,
                patchName = _activePatch.value.name,
                patternId = _activePattern.value.id,
                patternName = _activePattern.value.name
            )
        }
    }

    fun loadSession(session: SessionEntity) {
        // Find matching patch
        val matchedPatch = patches.value.firstOrNull { it.id == session.patchId }
            ?: patches.value.firstOrNull { it.name == session.patchName }
        if (matchedPatch != null) {
            _activePatch.value = matchedPatch
        }

        // Find matching pattern
        val matchedPattern = patterns.value.firstOrNull { it.id == session.patternId }
            ?: patterns.value.firstOrNull { it.name == session.patternName }
        if (matchedPattern != null) {
            _activePattern.value = matchedPattern
            rhythmEngine.updatePattern(matchedPattern)
        }

        // Navigate to Patches screen
        _selectedTab.value = StudioTab.PATCHES
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun panicStopAll() {
        rhythmEngine.stop()
        looperEngine.stopAll()
        looperEngine.stopRecording()
        guitarDspEngine.stopAudition()
        tunerEngine.stopListening()
    }

    fun resetAllData() {
        panicStopAll()
        viewModelScope.launch {
            repository.clearAllData()
            _activePatch.value = Patch.DEFAULT_PATCH
            _activePattern.value = RhythmPattern.DEFAULT_PATTERN
            resetLooperAll()
        }
    }

    fun selectInputDevice(deviceId: String) {
        audioDeviceManager.selectInputDevice(deviceId)
    }

    fun selectOutputDevice(deviceId: String) {
        audioDeviceManager.selectOutputDevice(deviceId)
    }

    fun selectDspEngine(engineId: String) {
        audioDeviceManager.selectDspEngine(engineId)
    }

    fun toggleAudioConnection() {
        audioDeviceManager.toggleAudioConnection()
    }

    fun setEchoCancellation(enabled: Boolean) {
        audioDeviceManager.setEchoCancellation(enabled)
    }

    fun setNoiseSuppression(enabled: Boolean) {
        audioDeviceManager.setNoiseSuppression(enabled)
    }

    fun setAutoGainControl(enabled: Boolean) {
        audioDeviceManager.setAutoGainControl(enabled)
    }

    fun setInputGainDb(gainDb: Float) {
        audioDeviceManager.setInputGainDb(gainDb)
    }

    fun setOutputVolume(volume: Float) {
        audioDeviceManager.setOutputVolume(volume)
    }

    fun setDirectMonitoring(enabled: Boolean) {
        audioDeviceManager.setDirectMonitoring(enabled)
    }

    override fun onCleared() {
        super.onCleared()
        audioDeviceManager.stopTelemetry()
        rhythmEngine.release()
        looperEngine.release()
        tunerEngine.stopListening()
        guitarDspEngine.release()
    }
}
