# Flutter App Migration & Rebuild Documentation (v5.0 - Ultimate OTT/Media Blueprint)

## 1. Project Overview & Vision
**Vision:** Transforming "Tamilaudiopro" from an outdated Android Java app into a premium, ad-free, completely free Audio & Video streaming platform. 
**Core Philosophy:** Avoid over-engineering before playback is stable. Focus on media lifecycle stability, Android fragmentation, and memory management.

## 2. Best Roadmap (Phased Execution)
**CRITICAL:** Do NOT build everything at once. Media apps fail due to over-engineering before stable playback.
- **Phase 1:** Audio only, basic player, no encryption, no video.
- **Phase 2:** Stable player, offline downloads (with Manifest), Smart Resume.
- **Phase 3:** Video integration (`media_kit`), YouTube fallback, Video feed.
- **Phase 4:** Chunk-based encryption, PIP (Picture-in-Picture), advanced UX, Crossfade.

---

## 3. Migration & User Retention
**Seamless User Migration (MethodChannel):**
- **Action:** Read legacy `SharedPreferences` (Java) to extract user token, encrypt, and migrate to `flutter_secure_storage`.
- **Safety:** Execute **ONE TIME ONLY**. Include a rollback-safe `migration_completed = true` flag to prevent duplicate runs.

---

## 4. Flutter Architecture: Unified Media Engine
**Folder Structure (Unified):**
Instead of separating audio and video entirely, we use a Unified Media Engine layer since queue, history, focus, and analytics are shared concerns.
```text
lib/
 ┣ core/
 ┃ ┣ api_adapter/   (Adapter layer for gradual API migration)
 ┃ ┗ utils/         (Encryption chunks, Manifest generator)
 ┣ features/
 ┃ ┣ media/         (Unified Media Engine)
 ┃ ┃ ┣ audio/       (Audio specific ui/logic)
 ┃ ┃ ┣ video/       (Video specific ui/logic)
 ┃ ┃ ┣ controllers/ (Global focus & queue controllers)
 ┃ ┃ ┗ shared/      (Queue, history, downloads, analytics)
 ┣ app_shell.dart   (GlobalMiniPlayer above Router)
 ┗ main.dart
```

---

## 5. Media Engineering Core
### A. Unified Audio Focus System
Must handle Android's aggressive fragmentation strictly:
- **Incoming Call:** Pause
- **Headphone Unplug:** Pause
- **Bluetooth Disconnect:** Pause
- **Video Start:** Duck or Pause
- **Voice Message/Navigation:** Duck (lower volume temporarily)
- **Foreground Service:** MUST use `foregroundServiceType="mediaPlayback"` for Android 14+ compatibility.

### B. Video Architecture (`media_kit`)
- **Engine:** Use `media_kit` (desktop-grade C++ engine) instead of `video_player + chewie` to guarantee hardware acceleration, better buffering, and native subtitle support.
- **YouTube Integration:** Avoid `youtube_player_flutter` (iframe instability). Use `youtube_explode_dart` for metadata extraction, and provide an official YouTube App/WebView fallback.

### C. Offline Downloads & Security
- **Chunk-Based AES Encryption:** Instead of full file encryption (which crashes low RAM phones), split media into chunks, encrypt them, and stream-decrypt on the fly.
- **Download Manifest System:** Create a JSON manifest for each download to detect corruption:
  ```json
  { "media_id": 12, "quality": "720p", "encrypted_path": "...", "checksum": "..." }
  ```

---

## 6. Premium UI & High-Value UX Features
- **AppShell Architecture:** The Mini Player MUST exist above the router lifecycle.
  ```text
  MaterialApp
   ┣ AppShell
   ┃ ┣ Router (Navigation)
   ┃ ┗ GlobalMiniPlayer (Independent provider)
  ```
- **MUST IMPLEMENT (Priority 1):** 
  - **Smart Resume (Continue Listening/Watching):** Save media id, position, and duration to Isar DB. Prompt user: "Resume from 12:41?" on app open.
  - **Playback Speed Control:** (0.75x, 1x, 1.25x, 1.5x, 2x) for both Audio & Video.
  - **Sleep Timer:** Critical for audio apps (e.g., 15 min, 30 min, end of track).
  - **Universal Search:** Search songs, videos, artists, playlists from one bar, with recent & trending searches.
- **SHOULD IMPLEMENT (Priority 2):** 
  - **Dynamic Theme Extraction:** Extract accent colors and blur backgrounds from media artwork (Spotify vibe).
  - **Offline-Aware UI:** Cached home screen and "You're offline" UX instead of a broken app.
  - **Smart Quality Switching & Network Detection:** Auto-switch quality (Low/Medium/High) based on network buffering and speed.
  - **Lyrics & Subtitle Support:** Timestamp synced lyrics for audio, `.srt`/`.vtt` for video.
- **AVOID FOR NOW:** AI chatbots, social feeds, live comments, full DRM. Focus strictly on playback experience.

---

## 7. Performance & Observability
- **Media Preloading:** For video feeds, preload ONLY the next 1 video to save bandwidth and RAM.
- **Thumbnail Pipeline:** Load tiny thumbnails first, then medium, then full artwork.
- **Playback Telemetry:** Crashlytics is not enough. Track buffering time, stream failures, bitrate, and codec failures per Android device model.

---

## 8. Backend Sustainability
- **Phase-Wise API Migration:** Keep `api.php + helper_name` for legacy features. Build new features ONLY on a new API architecture (e.g., Laravel). Use the Flutter `ApiAdapter` to abstract this routing.

---

## 9. QA & Testing Strategy (Critical)
Background media behaves differently depending on the phone manufacturer.
- **Mandatory Device Tests:** Android 10 to 15.
- **OEMs:** Samsung, Xiaomi, Vivo, Oppo, Huawei.
- **Focus:** Battery optimization kills, Doze mode, and background wake locks.
- **Automated Testing:** Implement Widget tests, Repository tests, Integration tests, and Playback lifecycle tests (pause/resume, route change, background kill).

---

## 10. Advanced Engineering & DevOps Mechanics
- **Session Orchestrator:** Implement `media_session_manager.dart` and `focus_orchestrator.dart` to handle audio ↔ video switching, interruptions, device reconnects, and PIP transitions centrally.
- **Playback Recovery Snapshot:** Store `playback_snapshot.json` in Isar. On app crash or restart, read the snapshot to instantly restore current media, position, queue, and speed.
- **Memory Lifecycle Strategy:** Aggressively dispose inactive controllers. Allow ONLY 1 active video controller at a time. Clear thumbnails outside the viewport and set strict artwork cache limits.
- **Visibility Playback Rules:** For scrolling video feeds, use `visibility_detector`. Auto-play only if >70% visible, pause when offscreen, and fully dispose after 2 screens away.
- **API Cache Layer:** Implement `CacheInterceptor` with Dio (Repository → RemoteDataSource → ApiAdapter → CacheInterceptor → Dio) for stale-while-revalidate logic and lowering PHP load.
- **Security:** Replace MD5 with **HMAC-SHA256**. Add 60-second request expiry and nonce to prevent replay attacks.
- **CI/CD Pipeline:** Use GitHub Actions to automate `flutter analyze`, unit tests, building APK/AAB, and artifact upload. Separate Dev, Staging, and Production flavors.

---

## 11. Final Polish & Long-Term Sustainability
- **Feature Flags / Remote Config:** Use Firebase Remote Config or Backend JSON to toggle video, experimental UI, or smart quality remotely without pushing an app update.
- **App Update Strategy:** Implement minimum supported version logic with Force Update and Soft Update prompts to elegantly handle legacy API deprecations.
- **Error Classification System:** Categorize errors (Network, Codec, Source Unavailable, Region Blocked, Timeout) for precise telemetry and better user-facing messages.
- **Media Source Abstraction:** Use an abstract `MediaSourceProvider` to unify future media formats (Local, Server, YouTube, Livestream, Podcast) under one interface.
- **Background Task & Storage Strategy:** 
  - Use `WorkManager` for reliable background downloads against strict OEM battery killers.
  - Implement a **Storage Cleanup Policy** (max cache size, delete orphaned downloads, clear old cache) to prevent app bloat.
- **Accessibility & Analytics:** 
  - Support screen readers, high contrast, and scalable text. 
  - Enforce strict Analytics Event Naming Conventions (e.g., `media_play`, `buffer_start`, `download_complete`) for clean tracking.

---

## 12. Ultimate Stability & Resilience (Final Polish)
- **Crash Resilience System:** 
  - **Auto-Retry:** Attempt playback up to 3 times on failure.
  - **Fallback Stream:** Auto-switch to lower quality if the main stream crashes.
  - **Cache Guard:** Detect corrupted cache files and auto-delete them.
- **Device Compatibility Fallback Matrix:**
  - *Samsung:* Implement explicit aggressive battery optimization warnings/handlers.
  - *Xiaomi:* Add "Auto Start Permission" fallback prompts.
  - *Huawei:* Special handling for background services without Google Play Services.
  - *Low RAM Devices:* Disable video preloading entirely and reduce max cache size dynamically.
- **Media Health Monitor (Runtime Metrics):**
  - Track real-time: buffering duration, dropped frames, codec failure rate, network instability score, and playback restart count. 
  - This provides the foundation for future adaptive streaming algorithms.
- **EXPLICIT EXCLUSIONS (Do NOT Build Now):** AI recommendations, social features, live chat, or DRM systems. Keep the core media engine strictly focused and resilient.
