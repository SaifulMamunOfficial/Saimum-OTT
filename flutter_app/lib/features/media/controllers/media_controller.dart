import 'dart:async';

import 'package:audio_service/audio_service.dart';
import 'package:encrypt/encrypt.dart' as enc;
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/models/download_manifest.dart';
import '../../../core/utils/db_service.dart';
import '../../../core/utils/decryption_audio_source.dart';
import '../audio/saimum_audio_handler.dart';
import '../shared/media_session_orchestrator.dart';
import '../video/video_player_controller.dart';

// ---------------------------------------------------------------------------
// Local State (audio-engine-specific)
// ---------------------------------------------------------------------------

class MediaState {
  final bool isPlaying;
  final bool isLoading;
  final Duration position;
  final String? currentUrl;
  final double speed;

  const MediaState({
    this.isPlaying = false,
    this.isLoading = false,
    this.position = Duration.zero,
    this.currentUrl,
    this.speed = 1.0,
  });

  MediaState copyWith({
    bool? isPlaying,
    bool? isLoading,
    Duration? position,
    String? currentUrl,
    double? speed,
  }) {
    return MediaState(
      isPlaying: isPlaying ?? this.isPlaying,
      isLoading: isLoading ?? this.isLoading,
      position: position ?? this.position,
      currentUrl: currentUrl ?? this.currentUrl,
      speed: speed ?? this.speed,
    );
  }
}

// ---------------------------------------------------------------------------
// Providers
// ---------------------------------------------------------------------------

/// Overridden in main() with the live [SaimumAudioHandler] from AudioService.
final audioHandlerProvider = Provider<SaimumAudioHandler>(
  (_) => throw StateError('audioHandlerProvider not overridden in main()'),
);

final mediaControllerProvider =
    StateNotifierProvider<MediaController, MediaState>(
  (ref) => MediaController(
    ref.watch(audioHandlerProvider),
    ref.read(mediaSessionOrchestratorProvider.notifier),
    ref,
  ),
);

// ---------------------------------------------------------------------------
// Controller
// ---------------------------------------------------------------------------

class MediaController extends StateNotifier<MediaState> {
  final SaimumAudioHandler _handler;
  final MediaSessionOrchestrator _orchestrator;
  final Ref _ref;
  late final StreamSubscription<PlaybackState> _playbackSub;

  MediaController(this._handler, this._orchestrator, this._ref)
      : super(const MediaState()) {
    _playbackSub = _handler.playbackState.listen(_onPlaybackStateChanged);
  }

  void _onPlaybackStateChanged(PlaybackState ps) {
    final loading = ps.processingState == AudioProcessingState.loading ||
        ps.processingState == AudioProcessingState.buffering;

    state = state.copyWith(
      isPlaying: ps.playing,
      isLoading: loading,
      position: ps.updatePosition,
    );

    // Derive and push status to global orchestrator
    final PlaybackStatus orchStatus;
    if (ps.processingState == AudioProcessingState.idle) {
      orchStatus = PlaybackStatus.idle;
    } else if (loading) {
      orchStatus = PlaybackStatus.loading;
    } else if (ps.playing) {
      orchStatus = PlaybackStatus.playing;
    } else {
      orchStatus = PlaybackStatus.paused;
    }
    _orchestrator.syncStatus(orchStatus, ps.updatePosition);
  }

  Future<void> play(
    String url, {
    String title = 'Saimum Music',
    String? artist,
    String? artworkUrl,
  }) async {
    // Stop video engine before taking audio focus — prevents re-entrancy crash.
    final orchState = _ref.read(mediaSessionOrchestratorProvider);
    if (orchState.isVideoActive) {
      try {
        await _ref.read(videoPlayerControllerProvider.notifier).stop();
        await Future<void>.delayed(const Duration(milliseconds: 100));
      } catch (e) {
        debugPrint('DEBUG: Video stop (audio switch) error: $e');
      }
    }

    state = state.copyWith(isLoading: true, currentUrl: url);

    _orchestrator.switchToSource(
      NowPlayingItem(
        id: url,
        title: title,
        sourceUrl: url,
        artist: artist,
        artworkUrl: artworkUrl,
      ),
      PlaybackMode.audio,
    );

    await _handler.playFromUrl(
      url,
      mediaItem: MediaItem(
        id: url,
        title: title,
        artist: artist ?? 'Streaming',
      ),
    );
  }

  Future<void> pause() => _handler.pause();

  Future<void> stop() async {
    await _handler.stop();
    state = const MediaState();
    _orchestrator.onStopped();
  }

  Future<void> resume() => _handler.play();

  Future<void> seek(Duration position) => _handler.seek(position);

  Future<void> setSpeed(double speed) async {
    await _handler.setSpeed(speed);
    state = state.copyWith(speed: speed);
  }

  /// Plays a fully downloaded + encrypted song without any network request.
  Future<void> playOffline(
    String mediaId, {
    String title = 'Saimum Music',
    String? artist,
    String? artworkUrl,
  }) async {
    final isar = _ref.read(isarProvider);
    final manifest = await isar.downloadManifests.getByMediaId(mediaId);

    if (manifest == null || !manifest.isCompleted) {
      debugPrint('DEBUG: playOffline — manifest missing or incomplete for $mediaId');
      return;
    }

    final iv = enc.IV.fromBase64(manifest.encryptionIv!);
    final source = DecryptionAudioSource(
      mediaId: mediaId,
      chunksDir: manifest.localPath,
      totalChunks: manifest.totalChunks,
      fileSize: manifest.fileSize,
      iv: iv,
    );

    // Stop video engine before taking audio focus.
    final orchState = _ref.read(mediaSessionOrchestratorProvider);
    if (orchState.isVideoActive) {
      try {
        await _ref.read(videoPlayerControllerProvider.notifier).stop();
        await Future<void>.delayed(const Duration(milliseconds: 100));
      } catch (e) {
        debugPrint('DEBUG: Video stop (offline switch) error: $e');
      }
    }

    state = state.copyWith(isLoading: true, currentUrl: 'offline:$mediaId');

    _orchestrator.switchToSource(
      NowPlayingItem(
        id: mediaId,
        title: title,
        sourceUrl: 'offline:$mediaId',
        artist: artist,
        artworkUrl: artworkUrl,
      ),
      PlaybackMode.audio,
    );

    await _handler.playFromSource(
      source,
      mediaItem: MediaItem(
        id: mediaId,
        title: title,
        artist: artist ?? 'Offline',
      ),
    );
  }

  @override
  void dispose() {
    _playbackSub.cancel();
    super.dispose();
  }
}
