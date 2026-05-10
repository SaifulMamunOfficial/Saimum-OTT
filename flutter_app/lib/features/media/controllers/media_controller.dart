import 'dart:async';

import 'package:audio_service/audio_service.dart';
import 'package:encrypt/encrypt.dart' as enc;
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api_adapter/home_repository.dart';
import '../../../core/models/download_manifest.dart';
import '../../../core/utils/db_service.dart';
import '../../../core/utils/decryption_audio_source.dart';
import '../audio/saimum_audio_handler.dart';
import '../shared/media_session_orchestrator.dart';
import '../video/video_player_controller.dart';
import '../../../core/api_adapter/models/song_model.dart';

// ---------------------------------------------------------------------------
// Local State (audio-engine-specific)
// ---------------------------------------------------------------------------

class MediaState {
  final bool isPlaying;
  final bool isLoading;
  final Duration position;
  final String? currentUrl;
  final double speed;

  /// Catalog song id when playing a track from the API (for play-count).
  final int? currentSongId;

  const MediaState({
    this.isPlaying = false,
    this.isLoading = false,
    this.position = Duration.zero,
    this.currentUrl,
    this.speed = 1.0,
    this.currentSongId,
  });

  MediaState copyWith({
    bool? isPlaying,
    bool? isLoading,
    Duration? position,
    String? currentUrl,
    double? speed,
    int? currentSongId,
    bool clearCurrentSongId = false,
  }) {
    return MediaState(
      isPlaying: isPlaying ?? this.isPlaying,
      isLoading: isLoading ?? this.isLoading,
      position: position ?? this.position,
      currentUrl: currentUrl ?? this.currentUrl,
      speed: speed ?? this.speed,
      currentSongId:
          clearCurrentSongId ? null : (currentSongId ?? this.currentSongId),
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

final playbackStateProvider = StreamProvider<PlaybackState>((ref) {
  final handler = ref.watch(audioHandlerProvider);
  return handler.playbackState;
});

// ---------------------------------------------------------------------------
// Controller
// ---------------------------------------------------------------------------

class MediaController extends StateNotifier<MediaState> {
  MediaController(this._handler, this._orchestrator, this._ref)
      : super(const MediaState()) {
    _playbackSub = _handler.playbackState.listen(_onPlaybackStateChanged);
    _mediaItemSub = _handler.mediaItem.listen(_onMediaItemChanged);
  }

  final SaimumAudioHandler _handler;
  final MediaSessionOrchestrator _orchestrator;
  final Ref _ref;
  late final StreamSubscription<PlaybackState> _playbackSub;
  late final StreamSubscription<MediaItem?> _mediaItemSub;

  Timer? _playCountTimer;
  int? _playCountScheduledForId;
  int? _playCountConsumedForId;

  static const _playCountDebounce = Duration(seconds: 8);

  void _cancelPlayCountTimer() {
    _playCountTimer?.cancel();
    _playCountTimer = null;
    _playCountScheduledForId = null;
  }

  void _syncPlayCountTimer({
    required bool playing,
    required bool loading,
    required bool idle,
  }) {
    final songId = state.currentSongId;
    if (songId == null || songId <= 0) {
      _cancelPlayCountTimer();
      return;
    }
    if (idle) {
      _cancelPlayCountTimer();
      return;
    }
    if (_playCountConsumedForId == songId) {
      return;
    }
    if (!playing || loading) {
      _cancelPlayCountTimer();
      return;
    }
    if (_playCountTimer?.isActive == true &&
        _playCountScheduledForId == songId) {
      return;
    }
    _cancelPlayCountTimer();
    _playCountScheduledForId = songId;
    _playCountTimer = Timer(_playCountDebounce, () {
      unawaited(_tryIncrementPlayCount(songId));
    });
  }

  Future<void> _tryIncrementPlayCount(int songId) async {
    _playCountTimer = null;
    _playCountScheduledForId = null;

    if (state.currentSongId != songId || !state.isPlaying) {
      return;
    }
    if (_playCountConsumedForId == songId) {
      return;
    }

    _playCountConsumedForId = songId;

    try {
      await _ref.read(homeRepositoryProvider).incrementSongViews(songId);
    } catch (e) {
      debugPrint('DEBUG: incrementSongViews failed: $e');
    }
  }

  void _onPlaybackStateChanged(PlaybackState ps) {
    final loading = ps.processingState == AudioProcessingState.loading ||
        ps.processingState == AudioProcessingState.buffering;
    final idle = ps.processingState == AudioProcessingState.idle;

    state = state.copyWith(
      isPlaying: ps.playing,
      isLoading: loading,
      position: ps.updatePosition,
    );

    _syncPlayCountTimer(
      playing: ps.playing,
      loading: loading,
      idle: idle,
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

  void _onMediaItemChanged(MediaItem? item) {
    if (item == null) {
      state = state.copyWith(clearCurrentSongId: true, currentUrl: '');
      return;
    }

    final songId = item.extras?['songId'] as int?;
    state = state.copyWith(
      currentUrl: item.id,
      currentSongId: songId,
    );

    // Sync orchestrator when item changes (e.g. next in queue)
    _orchestrator.switchToSource(
      NowPlayingItem(
        id: item.id,
        title: item.title,
        sourceUrl: item.id,
        artist: item.artist,
        artworkUrl: item.artUri?.toString(),
        extras: item.extras,
      ),
      PlaybackMode.audio,
    );
  }

  Future<void> play(
    String url, {
    String title = 'Saimum Stream',
    String? artist,
    String? artworkUrl,
    int? songId,
  }) async {
    _cancelPlayCountTimer();
    _playCountConsumedForId = null;

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

    state = state.copyWith(
      isLoading: true,
      currentUrl: url,
      currentSongId: songId,
    );

    _orchestrator.switchToSource(
      NowPlayingItem(
        id: url,
        title: title,
        sourceUrl: url,
        artist: artist,
        artworkUrl: artworkUrl,
        extras: songId != null ? {'songId': songId} : null,
      ),
      PlaybackMode.audio,
    );

    await _handler.playFromUrl(
      url,
      mediaItem: MediaItem(
        id: url,
        title: title,
        artist: artist ?? 'Streaming',
        artUri: artworkUrl != null ? Uri.parse(artworkUrl) : null,
        extras: songId != null ? {'songId': songId} : null,
      ),
    );
  }

  Future<void> playQueue(List<SongModel> songs, {int initialIndex = 0}) async {
    if (songs.isEmpty) return;
    _cancelPlayCountTimer();
    _playCountConsumedForId = null;

    final items = songs.map<MediaItem>((s) => MediaItem(
      id: s.audioUrl,
      title: s.title,
      artist: s.artist,
      artUri: s.thumbnail.isNotEmpty ? Uri.parse(s.thumbnail) : null,
      extras: {'songId': s.id},
    )).toList();

    state = state.copyWith(isLoading: true);
    await _handler.updateQueue(items);
    if (initialIndex > 0 && initialIndex < items.length) {
      await _handler.skipToQueueItem(initialIndex);
    }
    await _handler.play();
  }

  Future<void> skipToNext() => _handler.skipToNext();
  Future<void> skipToPrevious() => _handler.skipToPrevious();

  Future<void> toggleShuffle() async {
    final isShuffle = _handler.playbackState.value.shuffleMode == AudioServiceShuffleMode.all;
    await _handler.setShuffleMode(isShuffle ? AudioServiceShuffleMode.none : AudioServiceShuffleMode.all);
  }

  Future<void> toggleRepeat() async {
    final current = _handler.playbackState.value.repeatMode;
    final next = switch (current) {
      AudioServiceRepeatMode.none => AudioServiceRepeatMode.all,
      AudioServiceRepeatMode.all => AudioServiceRepeatMode.one,
      _ => AudioServiceRepeatMode.none,
    };
    await _handler.setRepeatMode(next);
  }

  Future<void> pause() => _handler.pause();

  Future<void> stop() async {
    _cancelPlayCountTimer();
    _playCountConsumedForId = null;
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
    String title = 'Saimum Stream',
    String? artist,
    String? artworkUrl,
  }) async {
    _cancelPlayCountTimer();
    _playCountConsumedForId = null;

    final isar = _ref.read(isarProvider);
    final manifest = await isar.downloadManifests.getByMediaId(mediaId);

    if (manifest == null || !manifest.isCompleted) {
      debugPrint(
          'DEBUG: playOffline — manifest missing or incomplete for $mediaId');
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

    final offlineSongId = int.tryParse(mediaId);

    state = state.copyWith(
      isLoading: true,
      currentUrl: 'offline:$mediaId',
      currentSongId: offlineSongId,
      clearCurrentSongId: offlineSongId == null,
    );

    _orchestrator.switchToSource(
      NowPlayingItem(
        id: mediaId,
        title: title,
        sourceUrl: 'offline:$mediaId',
        artist: artist,
        artworkUrl: artworkUrl,
        extras: offlineSongId != null ? {'songId': offlineSongId} : null,
      ),
      PlaybackMode.audio,
    );

    await _handler.playFromSource(
      source,
      mediaItem: MediaItem(
        id: mediaId,
        title: title,
        artist: artist ?? 'Offline',
        artUri: artworkUrl != null ? Uri.parse(artworkUrl) : null,
        extras: offlineSongId != null ? {'songId': offlineSongId} : null,
      ),
    );
  }

  @override
  void dispose() {
    _cancelPlayCountTimer();
    _playbackSub.cancel();
    _mediaItemSub.cancel();
    super.dispose();
  }
}
