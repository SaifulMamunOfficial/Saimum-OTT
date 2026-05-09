import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:media_kit/media_kit.dart';
import 'package:media_kit_video/media_kit_video.dart';

import '../audio/saimum_audio_handler.dart';
import '../controllers/media_controller.dart';
import '../shared/media_session_orchestrator.dart';

// ---------------------------------------------------------------------------
// State
// ---------------------------------------------------------------------------

class VideoState {
  final bool isPlaying;
  final bool isLoading;
  final Duration position;
  final Duration duration;

  const VideoState({
    this.isPlaying = false,
    this.isLoading = false,
    this.position = Duration.zero,
    this.duration = Duration.zero,
  });

  VideoState copyWith({
    bool? isPlaying,
    bool? isLoading,
    Duration? position,
    Duration? duration,
  }) {
    return VideoState(
      isPlaying: isPlaying ?? this.isPlaying,
      isLoading: isLoading ?? this.isLoading,
      position: position ?? this.position,
      duration: duration ?? this.duration,
    );
  }
}

// ---------------------------------------------------------------------------
// Provider — singleton, lives as long as the ProviderScope
// ---------------------------------------------------------------------------

final videoPlayerControllerProvider =
    StateNotifierProvider<VideoPlayerController, VideoState>(
  (ref) => VideoPlayerController(
    ref.read(mediaSessionOrchestratorProvider.notifier),
    ref.read(audioHandlerProvider),
  ),
);

// ---------------------------------------------------------------------------
// Controller
// ---------------------------------------------------------------------------

class VideoPlayerController extends StateNotifier<VideoState> {
  final Player _player;
  late final VideoController videoController;

  final MediaSessionOrchestrator _orchestrator;
  final SaimumAudioHandler _audioHandler;

  final List<StreamSubscription<dynamic>> _subs = [];
  bool _disposed = false;

  VideoPlayerController(this._orchestrator, this._audioHandler)
      : _player = Player(
          configuration: const PlayerConfiguration(
            logLevel: MPVLogLevel.error,
          ),
        ),
        super(const VideoState()) {
    videoController = VideoController(_player);
    _subscribeToStreams();
  }

  void _subscribeToStreams() {
    _subs.add(_player.stream.playing.listen((playing) {
      if (_disposed) return;
      state = state.copyWith(isPlaying: playing);
      _orchestrator.syncStatus(
        playing ? PlaybackStatus.playing : PlaybackStatus.paused,
        state.position,
      );
    }));

    _subs.add(_player.stream.buffering.listen((buffering) {
      if (_disposed) return;
      state = state.copyWith(isLoading: buffering);
      if (buffering) {
        _orchestrator.syncStatus(PlaybackStatus.loading, state.position);
      }
    }));

    _subs.add(_player.stream.position.listen((pos) {
      if (_disposed) return;
      state = state.copyWith(position: pos);
      _orchestrator.syncStatus(
        state.isPlaying ? PlaybackStatus.playing : PlaybackStatus.paused,
        pos,
      );
    }));

    _subs.add(_player.stream.duration.listen((dur) {
      if (_disposed) return;
      state = state.copyWith(duration: dur);
      _orchestrator.updateDuration(dur);
    }));
  }

  /// Stops audio (if active), then starts video.
  /// Architecture guardrail: only one engine holds focus at a time.
  Future<void> playVideo(
    String url, {
    String title = 'Video',
    String? artist,
    String? artworkUrl,
  }) async {
    // Guard: skip stop() when audio is already idle to avoid triggering the
    // RxDart re-entrancy bug on an already-closed BehaviorSubject.
    if (!_audioHandler.isStopped) {
      try {
        debugPrint('DEBUG: AudioHandler stop initiated');
        // Microtask break prevents synchronous re-entrancy on the audio
        // handler's BehaviorSubject during the engine switch.
        await Future.microtask(() => _audioHandler.stop());
        debugPrint('DEBUG: AudioHandler stop finished');
      } catch (e) {
        debugPrint('DEBUG: AudioHandler stop error (non-fatal, continuing): $e');
      }
    }

    // 200ms clear window after audio teardown before MediaKit requests focus.
    // Without this delay some Android OEMs deny the new audio-focus grant.
    await Future<void>.delayed(const Duration(milliseconds: 200));

    _orchestrator.switchToSource(
      NowPlayingItem(
        id: url,
        title: title,
        sourceUrl: url,
        artist: artist,
        artworkUrl: artworkUrl,
      ),
      PlaybackMode.video,
    );

    await _safeOpen(url);
  }

  /// Wraps `_player.open` to catch FFmpeg / platform errors gracefully.
  Future<void> _safeOpen(String url) async {
    try {
      await _player.open(Media(url));
    } catch (e) {
      if (mounted) {
        state = state.copyWith(isLoading: false);
        _orchestrator.onError('Video load failed: $e');
      }
    }
  }

  Future<void> pause() => _player.pause();

  Future<void> resume() => _player.play();

  Future<void> stop() async {
    await _player.stop();
    _orchestrator.onStopped();
  }

  Future<void> seek(Duration position) => _player.seek(position);

  @override
  void dispose() {
    _disposed = true;
    // Cancel all stream subscriptions BEFORE disposing the player.
    // This prevents the MPV C++ layer from invoking Dart callbacks that
    // have already been invalidated — avoids the "Callback invoked after
    // it has been deleted" SIGABRT crash on Xiaomi/aggressive-OEM devices.
    for (final sub in _subs) {
      sub.cancel();
    }
    _subs.clear();
    // Small delay allows MPV's internal threads to observe subscription
    // cancellations before the player is fully torn down.
    Future<void>.delayed(const Duration(milliseconds: 100), _player.dispose);
    super.dispose();
  }
}
