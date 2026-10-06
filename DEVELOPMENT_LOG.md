# StompFlow Development Logs & Engineering Notes

This document provides a chronological index of engineering notes, architectural decisions, and release milestones for the **StompFlow** Android project.

---

## Log Entries Index

| Date | Topic / Milestone | Scope | Detailed Document |
| :--- | :--- | :--- | :--- |
| **2026-10-05** | **Audio Input & Output Menus (stomp-flow.org Alignment)** | Top Bar I/O & Audio Hardware Setup | [`docs/development-logs/2026-10-05-audio-input-output-menus.md`](docs/development-logs/2026-10-05-audio-input-output-menus.md) |
| **2026-10-05** | **GitHub Actions CI/CD Deployment & Google Play Publication** | DevOps & CI/CD Pipelines | [`docs/development-logs/2026-10-05-github-actions-ci-cd-deployment.md`](docs/development-logs/2026-10-05-github-actions-ci-cd-deployment.md) |
| **2026-09-15** | **Looper Engine Undo & Redo Architecture** | Audio DSP & State Management | [`docs/development-logs/2026-09-15-undo-redo-looper-engine.md`](docs/development-logs/2026-09-15-undo-redo-looper-engine.md) |
| **2026-09-08** | **AudioRecord JNI Initialization & Crash Remediation** | Native Android Audio HAL | [`docs/development-logs/2026-09-08-audio-record-dsp-pipeline-fixes.md`](docs/development-logs/2026-09-08-audio-record-dsp-pipeline-fixes.md) |

---

## Architecture Milestones Summary

### 1. Audio Processing & Synthesis Architecture
- **Low Latency AudioTrack**: Real-time synthesis for drum machine and tone auditions using 16-bit PCM buffers at 48,000 Hz.
- **Microphone Capture**: Resilient multi-rate sample negotiation (`AudioRecord`) with safe state initialization checks and deterministic hardware disposal.
- **Non-Destructive Sound Looper**: Independent 4-channel audio matrix with per-track snapshot history, dynamic layer count, waveform rendering, and undo/redo stacks.

### 2. UI & Design System
- Built with **Jetpack Compose** and **Material Design 3**.
- Dark studio theme inspired by professional pedalboards and hardware synthesizers.
- Dynamic responsive indicators for stomp switches, tuning needle cents meter, step sequencers, and track controls.

### 3. Local Persistence
- **Android Room** database with KSP compiler code generation.
- Full offline persistence of song setups, preset pedal configurations, and rhythm patterns without external network dependencies.

### 4. CI/CD & Automated Publication
- Automated release workflow compiling production APK and Android App Bundle (`.aab`).
- Secure signing integration utilizing GitHub Secrets (`SIGNING_KEY`, `KEY_STORE_PASSWORD`, `ALIAS`, `KEY_PASSWORD`).
- Continuous Google Play Store track publication (`internal`, `alpha`, `beta`, `production`) via Google Play Developer API.
- SHA-256 integrity digest generation and GitHub Releases automated publishing.
