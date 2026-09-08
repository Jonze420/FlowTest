# StompFlow Studio (Android)

StompFlow Studio is a guitar multi-effects processor, 16-step rhythm drum sequencer, 4-track sound looper, and chromatic guitar tuner rewritten in **Kotlin** and **Jetpack Compose** for Android.

## Features

- **Patches & Signal Chain:**
  - Build custom pedalboards with overdrive, distortion, delay, reverb, chorus, phaser, tremolo, compressor, and EQ.
  - Interactive on/bypass switches, parameter sliders, reordering, and preset manager (Clean, Crunch, Lead, Ambient, Acoustic, Funk).
  - Tone Audition Preview: Hear guitar riffs processed live through your active pedalboard DSP chain.

- **16-Step Rhythm Studio:**
  - 5 synthesized drum voices: Punchy Kick, Snare, Hi-Hat, Clap, and Tom.
  - BPM tempo slider (50–180 BPM) and groove swing timing.
  - Presets: Four-on-the-Floor, Basic Rock, Pop Backbeat, Shuffle/Swing, Funk Groove, Boom-Bap, Trap Hats, and House.
  - "Remix" algorithmic groove generator.

- **4-Track Looper:**
  - Record live instrument or voice via device microphone.
  - Multi-layer simultaneous playback with waveform visualization and individual volume controls.
  - Emergency panic stop and layer clear.

- **Chromatic Guitar Tuner & Audio Options:**
  - Real-time pitch detection via audio autocorrelation.
  - Large note readout (E2, A2, D3, G3, B3, E4, etc.), Hz frequency, and cents offset needle meter (-50¢ to +50¢).
  - Clear "IN TUNE" indicator for precision tuning.

- **Song Sessions Persistence:**
  - Local database persistence via Android **Room**.
  - Save full song setups (Active Patch + Active Rhythm Pattern) and recall them with one tap.

## Tech Stack & Architecture

- **Language:** Kotlin 2.1
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architecture:** Clean MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Database:** Android Room with KSP
- **Audio Processing:** Low-latency `AudioTrack` real-time synthesis & `AudioRecord` PCM capture
- **Build System:** Gradle Kotlin DSL (`build.gradle.kts`, `libs.versions.toml`)
