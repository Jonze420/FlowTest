# Changelog

All notable changes to the **StompFlow** project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.2.1] - 2026-10-05

### Added
- **Audio Input & Output Menus from stomp-flow.org**:
  - Quick-access **Input Device Menu (`IN`)** and **Output Device Menu (`OUT`)** directly on the Top Studio Bar beside the Panic Stop button.
  - Dedicated Audio Setup Dialog and full Options Screen controls:
    - Input hardware device selector, guitar Hi-Z interface support, preamp gain slider (0 to +24 dB), live VU peak level meter (`SILENT`/`OK`/`GOOD`/`HOT`), and toggles for echo cancellation, noise suppression, and auto gain control.
    - Output hardware device selector, master output volume slider, direct monitoring pass-through switch.
    - DSP engine & latency presets (`Low Latency 48k`, `Low Latency 44.1k`, `Low Latency 96k`, `Balanced 48k`, `Playback 44.1k`).
    - Real-time 4-card telemetry grid for base latency, output latency, sample rate, and context status.
    - Connect / Disconnect live audio stream toggle button.
  - New dated development log: `docs/development-logs/2026-10-05-audio-input-output-menus.md`.
- **GitHub Actions Production CI/CD**:
  - `.github/workflows/deploy-production.yml` for automated release packaging, release APK and AAB signing, SHA-256 integrity checks, and GitHub Releases asset distribution.
  - `.github/workflows/publish-google-play.yml` for direct automated deployment to Google Play Store tracks (`internal`, `alpha`, `beta`, `production`) with localized release notes and ProGuard mapping file upload.
  - `.github/workflows/ci.yml` continuous integration pipeline for lint, unit tests, and compilation checks on PRs and commits.
  - `.github/DEPLOYMENT.md` deployment operations and secrets configuration guide.
- **Dated Markdown Documentation & Development Logs**:
  - `docs/development-logs/2026-10-05-github-actions-ci-cd-deployment.md`
  - `docs/development-logs/2026-09-15-undo-redo-looper-engine.md`
  - `docs/development-logs/2026-09-08-audio-record-dsp-pipeline-fixes.md`
  - Root `DEVELOPMENT_LOG.md` consolidating engineering architecture milestones.

---

## [1.2.0] - 2026-09-15

### Added
- **Undo and Redo Engine for Looper**:
  - Per-track non-destructive history stack (`ArrayDeque<TrackAudioSnapshot>`).
  - Deep copy audio buffer preservation across record, overdub, trim, and clear actions.
  - Interactive Undo (warm amber) and Redo (cool cyan) buttons with disabled visual states.
  - Contextual dynamic Stomp button switching between "Record", "Overdub" (with active layer counter), and "Stop Rec".

### Changed
- Improved memory allocation during live overdub layering with bounded history depth.
- Optimized waveform peak normalization rendering during active loop playback.

---

## [1.1.0] - 2026-09-08

### Fixed
- **AudioRecord JNI Initialization Error**:
  - Resolved `Unable to retrieve AudioRecord object` crash loops.
  - Added sample rate fallback ladder (48kHz -> 44.1kHz -> 22.05kHz -> 16kHz).
  - Ensured safe hardware release inside `try-finally` blocks.
  - Protected `AudioRecord.STATE_INITIALIZED` state verification before starting recording threads.

### Added
- Auto-correlation chromatic tuner needle smoothing and in-tune indicator.
- Dynamic runtime permission checking for `android.permission.RECORD_AUDIO`.

---

## [1.0.0] - 2026-08-20

### Added
- Initial release of **StompFlow**:
  - 8-pedal modular guitar multi-effects signal chain (Overdrive, Distortion, Delay, Reverb, Chorus, Phaser, Tremolo, Compressor, EQ).
  - 16-step rhythm machine with 5 synthesized drum voices and 8 genre groove presets.
  - 4-track real-time sound looper with visual waveforms.
  - Chromatic guitar tuner with cents meter and frequency detection.
  - Local session storage using Android Room.
