# Development Log — 2026-10-05: Audio Input & Output Menus (stomp-flow.org Alignment)

**Date:** 2026-10-05  
**Author:** StompFlow Engineering Team  
**Status:** Completed  
**Milestone:** Top Bar Audio I/O Quick Menus & Comprehensive Audio Setup DSP Engine  

---

## 1. Overview

Integrated the audio input and output management architecture from the official web application at [https://stomp-flow.org/](https://stomp-flow.org/) directly into the StompFlow Android app. Specifically added fast-access **Input (`IN`)** and **Output (`OUT`)** device menus directly onto the **Top Studio Bar beside the Panic Stop button**, coupled with a comprehensive **Audio Setup Dialog** and **Options Screen** integration.

---

## 2. Implemented Features

### 2.1 Top Bar Quick-Access Menus (Beside Panic Stop Button)
- **Input Device Menu (`IN`)**:
  - Displays microphone icon, "IN" label, and dropdown caret in signature Orange theme.
  - Tapping opens an instant DropdownMenu enumerating available input hardware (Default Input, USB Hi-Z Audio Interface, Built-in Mic, Bluetooth Headsets).
  - Shows active selection checkmark and sub-labels.
  - Direct shortcut to full Audio Setup & DSP dialog.
- **Output Device Menu (`OUT`)**:
  - Displays headphone icon, "OUT" label, and dropdown caret in Cyan theme.
  - Tapping opens an instant DropdownMenu enumerating available output hardware (Default Output, Wired Headphones / Studio Monitors, USB Stereo Out, Built-in Speaker, Bluetooth Audio).
  - Shows active selection checkmark and device descriptions.
- **Audio Setup Quick Launcher**:
  - Dedicated studio tuning button with status illumination that opens the complete modal setup sheet.

### 2.2 stomp-flow.org Audio Setup Specification
- **INPUT DEVICE Section**:
  - Dropdown selector for detected inputs.
  - Guitar guidance banner: *"For guitar, use an audio interface with Hi-Z input. Disable system echo cancellation for best results."*
  - Real-time Input Level VU Peak Meter with dynamic status tags (`SILENT`, `OK`, `GOOD`, `HOT`).
  - Preamp Gain slider (0 dB to +24 dB).
  - Raw instrument signal toggles:
    - Echo Cancellation (Disabled by default)
    - Noise Suppression (Disabled by default)
    - Auto Gain Control (Disabled by default)
- **OUTPUT DEVICE Section**:
  - Dropdown selector for detected outputs.
  - Guidance banner: *"Headphones or wired monitors are recommended for live playing to avoid feedback."*
  - Master Output Volume slider (0% to 100%).
  - Direct Hardware Monitoring toggle for zero-latency interface listening.
- **DSP ENGINE & LATENCY Section**:
  - Presets: `Default`, `Low Latency 48k` (recommended for guitar processing), `Low Latency 44.1k`, `Low Latency 96k`, `Balanced 48k`, `Balanced 44.1k`, `Playback 44.1k`.
  - Explanatory note: *"Lower latency presets are better for live playing but may use more CPU. Disconnect before changing."*
  - 4-Card Real-time Telemetry Grid:
    - **BASE LATENCY**: 5.3 ms
    - **OUTPUT LATENCY**: 10.7 ms
    - **SAMPLE RATE**: 48.0 kHz
    - **CONTEXT**: Running / Standby
- **CONNECT / DISCONNECT AUDIO Button**:
  - Toggles live audio pass-through stream with animated indicator.

---

## 3. Engineering Architecture

- **`AudioDeviceManager.kt`**:
  - Queries `android.media.AudioManager` and `AudioDeviceInfo` (API 23+) for physical hardware enumeration.
  - Emits reactive `AudioSetupState` with device lists, DSP engine profiles, and real-time studio telemetry.
- **`AudioSetupDialog.kt`**:
  - Material 3 dialog adhering to stomp-flow.org layouts and styling.
- **`TopStudioBar.kt`**:
  - Compact responsive layout positioning Input and Output quick-selectors right by the Panic Stop button.
