# AI-Agent Execution Plan: OTT Media Platform Rebuild

This document breaks down the `note.md` master architecture into **AI-executable tasks**. Development will be driven by AI (Cursor IDE / Autonomous Agents). Follow the tasks strictly in order.

---

## 🛑 GLOBAL GUARDRAILS (Strict AI Constraints)
**1. Rollback Strategy:**
If any phase breaks playback:
- Revert the last commit immediately.
- Disable the broken feature via flag.
- Fallback to the previous stable audio engine state.

**2. Feature Flag Control Layer:**
All major features MUST be toggled via local variables during development.
- `const bool video_enabled = false;`
- `const bool encryption_enabled = false;`
- `const bool download_enabled = false;`
*(Only enable them when their specific phase begins).*

**3. Error Recovery Rules (Media Core):**
- Stream fails → Retry 2 times automatically.
- Fails again → Auto-switch to low-quality URL.
- Fails again → Use fallback URL (if available).
- Fails again → Auto-skip to the next track.

**4. Performance Guardrails (Strict Limits):**
- **MAX** 1 active audio session at any time.
- **MAX** 1 active video controller in memory.
- **MAX** Cache size: 500MB (auto-delete oldest if exceeded).
- **MAX** Queue prefetch: 3 items ahead.

**5. Fail Fast Rule (No Hacky Workarounds):**
- If a task cannot be completed cleanly, **STOP**.
- Do NOT create messy workaround code or silent catches.
- Escalate to the human developer or simplify the approach.

**6. State Source of Truth Rule:**
- All playback state MUST come from: `PlaybackSnapshot` (Isar DB).
- UI, player, and session manager must NOT store independent state to prevent sync bugs.

**7. No Duplicate Engine Rule:**
- Only **ONE** audio engine instance is allowed (`just_audio` singleton ONLY).

**8. Boot Strategy (Strict Cold Start Order):**
1. Load Secure Storage (Tokens).
2. Load Isar Snapshot (Last played state).
3. Init Audio Engine.
4. Restore Session / Queue.
5. Render UI.

---

## Phase 1: Foundation (Migration, Storage, Security)
**Goal:** Setup the project safely and secure legacy user sessions.

- [ ] **Task 1.1: Clean Architecture Shell & Security**
  - **Action:** Run `flutter create`. Set up folder structure (core/, features/, services/). Setup Riverpod.
  - **DoD (Definition of Done):** Project compiles. Linter shows zero errors.

- [ ] **Task 1.2: MethodChannel Migration Script**
  - **Action:** Write native Kotlin code to extract legacy `SharedPreferences` token. Grab it in Flutter, encrypt, and save to `flutter_secure_storage`.
  - **DoD:** Token is successfully migrated once. `migration_completed` flag works.

- [ ] **Task 1.3: Isar DB Initialization**
  - **Action:** Install Isar. Define initial schemas (`PlaybackSnapshots`, `DownloadManifest`).
  - **DoD:** DB opens without errors on cold boot.

---

## Phase 2: Audio Core (NO UI)
**Goal:** Build the robust audio engine strictly without UI dependencies.

- [ ] **Task 2.1: `just_audio` + `audio_service` Wrapper**
  - **Action:** Implement singleton background audio handler. Configure lock-screen controls.
  - **DoD:** Music plays via terminal/mock commands. Background playback works when app is minimized.

---

## Phase 3: API & Cache Layer
**Goal:** Connect to the legacy `api.php`.

- [ ] **Task 3.1: Dio Client & Legacy API Adapter**
  - **Action:** Setup Dio with HMAC-SHA256 signature interceptor. Create `ApiAdapter` to map REST calls to `api.php?helper_name=XYZ`.
  - **DoD:** Network requests succeed with proper signature.

- [ ] **Task 3.2: CacheInterceptor (Stale-while-revalidate)**
  - **Action:** Intercept requests to return local cached JSON when offline.
  - **DoD:** Turning off Wi-Fi still returns cached homepage data instantly.

---

## Phase 4: Playback Recovery & Session Manager
**Goal:** Handle interruptions and state preservation.

- [ ] **Task 4.1: Media Session Orchestrator**
  - **Action:** Create `media_session_manager.dart`. Handle audio ducking for calls/navigation. Set Android 14 `foregroundServiceType`.
  - **DoD:** Audio pauses correctly on incoming phone call and resumes after.

- [ ] **Task 4.2: Playback Recovery Snapshot**
  - **Action:** Save media ID and queue to Isar DB every 5 seconds. Restore state on boot.
  - **DoD:** Force-closing the app and reopening restores the exact track and timestamp.

---

## Phase 5: Premium UI & Mini Player
**Goal:** Implement the "Never-Closing" Mini Player.

- [ ] **Task 5.1: `go_router` ShellRoute & Global Mini Player**
  - **Action:** Create the `AppShell`. Place `GlobalMiniPlayer` above `Router`.
  - **DoD:** Navigating between screens does not stutter or recreate the Mini Player.

- [ ] **Task 5.2: UI Modals (Speed & Timer)**
  - **Action:** Implement Sleep Timer and Playback Speed selector.
  - **DoD:** Changing speed alters audio pitch/speed instantly. Timer pauses audio when reached.

---

## Phase 6: Video Integration
**Goal:** Introduce video safely.

- [ ] **Task 6.1: `media_kit` Setup & Auto-Pause Rules**
  - **Action:** Initialize `media_kit`. Set `video_enabled = true`. When video plays, command `audio_service` to pause.
  - **DoD:** Video plays smoothly. Playing video absolutely mutes/pauses background audio.

---

## Phase 7: Downloads & Encryption
**Goal:** Secure offline media strictly.

- [ ] **Task 7.1: Background Downloader & AES Encryption**
  - **Action:** Set `download_enabled = true`. Encrypt chunks via AES-256 and generate `DownloadManifest`.
  - **DoD:** Downloaded files are unplayable outside the app. Decryption stream works smoothly without lag.

---

## Phase 8: Stability & Telemetry
**Goal:** Make the app unkillable.

- [ ] **Task 8.1: Device Matrix & Telemetry**
  - **Action:** Add Battery Optimization dialogs for Samsung/Xiaomi. Setup Firebase Crashlytics and custom `playback_metrics.dart`.
  - **DoD:** Dropped frames and buffering duration are successfully logged in console/Firebase.
