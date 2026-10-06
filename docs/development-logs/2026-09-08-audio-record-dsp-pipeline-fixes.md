# Development Log — 2026-09-08: AudioRecord Initialization & DSP Pipeline Stabilization

**Date:** 2026-09-08  
**Author:** StompFlow Engineering Team  
**Status:** Completed  
**Milestone:** Native Audio Record JNI Crash Resolution & Resilient Microphone Capture  

---

## 1. Incident Overview

The app encountered recurring logcat errors:
```
E/AudioRecord-JNI: Unable to retrieve AudioRecord object
```
This error occurred when the app attempted to construct or read from an uninitialized `AudioRecord` instance before audio hardware was ready or when sample rate / buffer size configurations conflicted with device capabilities.

---

## 2. Root Cause Analysis

1. **Unchecked Buffer Sizing**: `AudioRecord.getMinBufferSize` can return `ERROR` or `ERROR_BAD_VALUE` if an unsupported sample rate or channel configuration is requested. Passing invalid buffer sizes directly to the `AudioRecord` constructor causes native initialization failure in `libaudioclient.so`.
2. **Permission Race Condition**: Audio capture threads attempted to start before Android runtime `RECORD_AUDIO` permissions were granted and verified.
3. **Resource Leak on Re-entry**: Previous recording threads did not cleanly release native AudioRecord handles before creating new instances, resulting in native object exhaustion.

---

## 3. Resolution & Engineering Fixes

1. **Hardware Fallback Ladder**:
   - Implemented sample rate negotiation ladder: 48,000 Hz (preferred Android default), falling back to 44,100 Hz, 22,050 Hz, or 16,000 Hz if hardware rejects the rate.
   - Buffer size multiplier with safety checks against negative return values.
2. **Lifecycle Management & Safe Tear-Down**:
   - Guarded all `AudioRecord.startRecording()` and `read()` calls with `state == AudioRecord.STATE_INITIALIZED`.
   - Guaranteed `audioRecord.stop()` and `audioRecord.release()` execution inside synchronized `try-finally` blocks.
3. **Graceful Permission & UI Recovery**:
   - Added dynamic permission checking with prompt rationale before accessing the tuner or looper microphone input.
   - Handled `SecurityException` gracefully with informative UI alerts instead of unhandled exceptions.

---

## 4. Verification

- Validated on emulated and physical audio hardware profiles.
- Zero JNI crash reports during continuous tuner autocorrelation and looper capture sessions.
