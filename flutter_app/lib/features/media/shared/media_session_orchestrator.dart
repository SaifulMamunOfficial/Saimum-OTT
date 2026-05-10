import 'package:flutter_riverpod/flutter_riverpod.dart';

// ---------------------------------------------------------------------------
// Enums
// ---------------------------------------------------------------------------

enum PlaybackMode { audio, video }

enum PlaybackStatus { idle, loading, playing, paused, error }

// ---------------------------------------------------------------------------
// Now Playing Metadata
// ---------------------------------------------------------------------------

class NowPlayingItem {
  final String id;
  final String title;
  final String? artist;
  final String? artworkUrl;
  final String sourceUrl;
  final Map<String, dynamic>? extras;

  const NowPlayingItem({
    required this.id,
    required this.title,
    required this.sourceUrl,
    this.artist,
    this.artworkUrl,
    this.extras,
  });
}

// ---------------------------------------------------------------------------
// Orchestrator State
// ---------------------------------------------------------------------------

class OrchestratorState {
  final NowPlayingItem? currentItem;
  final PlaybackMode mode;
  final PlaybackStatus status;
  final Duration position;
  final Duration duration;

  const OrchestratorState({
    this.currentItem,
    this.mode = PlaybackMode.audio,
    this.status = PlaybackStatus.idle,
    this.position = Duration.zero,
    this.duration = Duration.zero,
  });

  bool get hasActiveItem =>
      currentItem != null && status != PlaybackStatus.idle;

  /// True when audio engine owns the active session.
  bool get isAudioActive => mode == PlaybackMode.audio && hasActiveItem;

  /// True when video engine owns the active session.
  bool get isVideoActive => mode == PlaybackMode.video && hasActiveItem;

  OrchestratorState copyWith({
    NowPlayingItem? currentItem,
    PlaybackMode? mode,
    PlaybackStatus? status,
    Duration? position,
    Duration? duration,
    bool clearItem = false,
  }) {
    return OrchestratorState(
      currentItem: clearItem ? null : (currentItem ?? this.currentItem),
      mode: mode ?? this.mode,
      status: status ?? this.status,
      position: position ?? this.position,
      duration: duration ?? this.duration,
    );
  }
}

// ---------------------------------------------------------------------------
// Provider
// ---------------------------------------------------------------------------

final mediaSessionOrchestratorProvider =
    NotifierProvider<MediaSessionOrchestrator, OrchestratorState>(
  MediaSessionOrchestrator.new,
);

// ---------------------------------------------------------------------------
// Notifier
// ---------------------------------------------------------------------------

class MediaSessionOrchestrator extends Notifier<OrchestratorState> {
  @override
  OrchestratorState build() => const OrchestratorState();

  /// Primary entry point. Call this whenever a new media source starts.
  /// If switching Audio → Video (future Phase 6), callers must stop the
  /// audio engine before calling this.
  void switchToSource(NowPlayingItem item, PlaybackMode mode) {
    state = OrchestratorState(
      currentItem: item,
      mode: mode,
      status: PlaybackStatus.loading,
    );
  }

  /// Sync live playback state from the audio/video engine stream.
  void syncStatus(PlaybackStatus status, Duration position) {
    if (state.currentItem == null) return;
    state = state.copyWith(status: status, position: position);
  }

  /// Call when the user explicitly presses stop or engine becomes idle.
  void onStopped() => state = const OrchestratorState();

  void onError(String message) =>
      state = state.copyWith(status: PlaybackStatus.error);

  void updateDuration(Duration duration) =>
      state = state.copyWith(duration: duration);
}
