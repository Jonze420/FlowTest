# StompFlow Project Memory (`MEMORY.md`)

**Last Updated:** 2026-10-05  
**Current Version:** `1.2.1` (`versionCode = 4`)  
**Application ID:** `com.aistudio.stompflow.mfkpzr`  
**Namespace:** `com.example.stompflow`  

---

## 1. Project Overview & Identity
- **Project Name:** StompFlow Studio
- **Platform:** Android (API 26 to API 35 target)
- **UI Architecture:** Jetpack Compose with Material Design 3
- **Audio Processing:** Low-latency real-time PCM synthesis via `AudioTrack` & resilient hardware microphone capture via `AudioRecord`.
- **Local Persistence:** Android Room with KSP compiler.

---

## 2. Audio Architecture & stomp-flow.org Integration

### 2.1 Top Studio Bar Audio Menus (Beside Panic Stop Button)
- **Input Quick Menu (`IN`)**:
  - Located on the top bar right beside the Panic Stop button.
  - Displays microphone icon in Orange theme and opens instant device selector (Default Input, USB Hi-Z Audio Interface, Built-in Mic, Bluetooth Headsets).
  - Contains direct link to full Audio Setup & DSP dialog.
- **Output Quick Menu (`OUT`)**:
  - Located on the top bar with headphones icon in Cyan theme.
  - Opens instant output destination selector (Default Output, Wired Headphones / Studio Monitors, USB Stereo Out, Built-in Speaker, Bluetooth Audio).
  - Contains direct link to full Audio Setup.
- **Audio Setup Quick Launcher**:
  - Dedicated studio tuning button beside stop button to launch the comprehensive modal audio configuration sheet.

### 2.2 Audio Setup & DSP Architecture (from stomp-flow.org)
- **INPUT DEVICE Section**:
  - Hardware selector with Hi-Z guitar interface recommendations.
  - Preamp Gain slider (0 dB to +24 dB).
  - Live Input VU Peak Level Meter (`SILENT`, `OK`, `GOOD`, `HOT`).
  - Raw instrument signal toggles: Echo Cancellation (Off), Noise Suppression (Off), Auto Gain Control (Off).
- **OUTPUT DEVICE Section**:
  - Hardware destination selector with headphone/monitor guidance.
  - Master Output Volume slider (0% to 100%).
  - Direct Hardware Monitoring toggle for zero-latency pass-through.
- **DSP ENGINE & LATENCY Section**:
  - Presets: `Default`, `Low Latency 48k` (default recommendation for live guitar), `Low Latency 44.1k`, `Low Latency 96k`, `Balanced 48k`, `Balanced 44.1k`, `Playback 44.1k`.
  - 4-Card Real-Time Telemetry Grid:
    - Base Latency (5.3 ms)
    - Output Latency (10.7 ms)
    - Sample Rate (48.0 kHz)
    - Context (Running / Standby)
  - Live Audio Stream Connect / Disconnect button.

### 2.3 4-Track Sound Looper & Undo/Redo Engine
- 4 independent tracks with per-track audio snapshot history (`TrackAudioSnapshot`).
- Bounded `undoStack` and `redoStack` preserving PCM byte buffers, duration, and trim boundaries.
- Live overdubbing with dynamic layer counter and contextual Stomp button ("Record" / "Overdub" / "Stop Rec").
- Dedicated Undo (warm amber) and Redo (cool cyan) controls on each track.

### 2.4 Chromatic Guitar Tuner & 16-Step Rhythm Sequencer
- Real-time autocorrelation pitch detection with cents deviation needle (-50¢ to +50¢) and "IN TUNE" indicator.
- 5 drum voices (Kick, Snare, Hi-Hat, Clap, Tom) across 8 genre presets with swing timing.

---

## 3. Build & Release Artifacts for Upload

| Artifact File | Path | Size | Description |
| :--- | :--- | :--- | :--- |
| **Release AAB (Signed)** | `.build-outputs/app-release.aab`<br>`release-dist/StompFlow-v1.2.1-release.aab` | ~11.3 MB | Production Android App Bundle signed with Android Keystore. Verified with `jarsigner`. Ready for Google Play Console upload. |
| **Release APK (Signed)** | `.build-outputs/app-release.apk`<br>`release-dist/StompFlow-v1.2.1-release.apk` | ~11.6 MB | Release APK package signed with Android Keystore (v2 scheme). Verified with `apksigner`. Ready for device installation. |
| **SHA-256 Checksums** | `release-dist/SHA256SUMS.txt` | 190 B | Cryptographic checksums ensuring file integrity. |

---

## 4. Google Play Console Upload & Publishing Guide

### 4.1 Resolving Play Console First-Upload Permission Error
When uploading the bundle for `com.aistudio.stompflow.mfkpzr` for the first time:
1. Log into [Google Play Console](https://play.google.com/console).
2. Create the application entry if not yet registered:
   - App Name: **StompFlow**
   - Default Language: English (United States)
   - App or Game: App (Free)
3. Navigate to **Testing > Internal testing** (or Closed testing).
4. Create a new release and upload `.build-outputs/app-release.aab` (or `release-dist/StompFlow-v1.2.1-release.aab`).
5. Ensure Google Play App Signing is enabled (Google manages the app signing key, using your uploaded upload key).
6. Under **Setup > API access**, ensure your Service Account has "Release apps to testing tracks" and "Manage testing tracks" permissions enabled.

### 4.2 GitHub Actions Workflows
- `.github/workflows/deploy-production.yml`: Automated GitHub release creation upon pushing tags (`v*.*.*`).
- `.github/workflows/publish-google-play.yml`: Direct publishing to Google Play tracks via workflow dispatch.
- `.github/workflows/ci.yml`: Continuous integration test and build validation.
- Reference: Detailed guide available in [`.github/DEPLOYMENT.md`](.github/DEPLOYMENT.md).

---

## 5. Critical Constraints & Preservation Rules
- **Application ID:** MUST REMAIN `com.aistudio.stompflow.mfkpzr`.
- **Namespace:** MUST REMAIN `com.example.stompflow`.
- **Keystores:** `debug.keystore` and `debug.keystore.base64` must never be modified.
- **Signing Config:** `buildTypes.release` is configured with `signingConfig = signingConfigs.getByName("debug")` to ensure `bundleRelease` always outputs a signed AAB for Play Console.
