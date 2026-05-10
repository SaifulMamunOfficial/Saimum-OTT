import 'package:audio_service/audio_service.dart';
import 'package:audio_session/audio_session.dart';
import 'package:flutter/foundation.dart';
import 'package:just_audio/just_audio.dart';

class SaimumAudioHandler extends BaseAudioHandler with SeekHandler {
  final _player = AudioPlayer();
  final _playlist = ConcatenatingAudioSource(children: []);

  SaimumAudioHandler() {
    _configureSession();
    _player.playbackEventStream.map(_buildPlaybackState).pipe(playbackState);
    
    _player.currentIndexStream.listen((index) {
      if (index != null && index < queue.value.length) {
        mediaItem.add(queue.value[index]);
      }
    });
  }

  Future<void> _configureSession() async {
    final session = await AudioSession.instance;
    await session.configure(const AudioSessionConfiguration.music());

    session.interruptionEventStream.listen((event) {
      if (event.begin) {
        _player.pause();
      } else if (event.type == AudioInterruptionType.pause && !event.begin) {
        _player.play();
      }
    });

    session.becomingNoisyEventStream.listen((_) => _player.pause());
  }

  PlaybackState _buildPlaybackState(PlaybackEvent event) {
    return PlaybackState(
      controls: [
        MediaControl.skipToPrevious,
        if (_player.playing) MediaControl.pause else MediaControl.play,
        MediaControl.stop,
        MediaControl.skipToNext,
      ],
      systemActions: const {
        MediaAction.seek,
        MediaAction.skipToNext,
        MediaAction.skipToPrevious,
        MediaAction.setShuffleMode,
        MediaAction.setRepeatMode,
      },
      androidCompactActionIndices: const [0, 1, 3],
      processingState: const {
        ProcessingState.idle: AudioProcessingState.idle,
        ProcessingState.loading: AudioProcessingState.loading,
        ProcessingState.buffering: AudioProcessingState.buffering,
        ProcessingState.ready: AudioProcessingState.ready,
        ProcessingState.completed: AudioProcessingState.completed,
      }[_player.processingState]!,
      playing: _player.playing,
      updatePosition: _player.position,
      bufferedPosition: _player.bufferedPosition,
      speed: _player.speed,
      queueIndex: event.currentIndex,
      shuffleMode: _player.shuffleModeEnabled
          ? AudioServiceShuffleMode.all
          : AudioServiceShuffleMode.none,
      repeatMode: switch (_player.loopMode) {
        LoopMode.one => AudioServiceRepeatMode.one,
        LoopMode.all => AudioServiceRepeatMode.all,
        _ => AudioServiceRepeatMode.none,
      },
    );
  }

  /// True when the underlying player is fully idle — used as a guard
  /// in VideoPlayerController to skip redundant stop calls.
  bool get isStopped =>
      _player.processingState == ProcessingState.idle && !_player.playing;

  @override
  Future<void> addQueueItems(List<MediaItem> items) async {
    final sources = items.map((item) => AudioSource.uri(
      Uri.parse(item.id),
      tag: item,
    )).toList();
    
    await _playlist.addAll(sources);
    final newQueue = queue.value..addAll(items);
    queue.add(newQueue);
  }

  @override
  Future<void> updateQueue(List<MediaItem> items) async {
    await _playlist.clear();
    final sources = items.map((item) => AudioSource.uri(
      Uri.parse(item.id),
      tag: item,
    )).toList();
    
    await _playlist.addAll(sources);
    queue.add(items);
    
    if (_player.audioSource == null || _player.audioSource != _playlist) {
      await _player.setAudioSource(_playlist);
    }
  }

  Future<void> playFromUrl(String url, {MediaItem? mediaItem}) async {
    final item = mediaItem ?? MediaItem(
      id: url,
      title: 'Saimum Music',
      artist: 'Streaming',
    );

    await updateQueue([item]);
    await _player.play();
  }

  Future<void> playFromSource(AudioSource source, {MediaItem? mediaItem}) async {
    this.mediaItem.add(mediaItem ?? MediaItem(id: 'offline', title: 'Offline'));
    await _player.setAudioSource(source);
    await _player.play();
  }

  @override
  Future<void> skipToNext() => _player.seekToNext();

  @override
  Future<void> skipToPrevious() => _player.seekToPrevious();

  @override
  Future<void> setShuffleMode(AudioServiceShuffleMode mode) async {
    final enabled = mode != AudioServiceShuffleMode.none;
    await _player.setShuffleModeEnabled(enabled);
  }

  @override
  Future<void> setRepeatMode(AudioServiceRepeatMode mode) async {
    final loopMode = switch (mode) {
      AudioServiceRepeatMode.one => LoopMode.one,
      AudioServiceRepeatMode.all => LoopMode.all,
      _ => LoopMode.off,
    };
    await _player.setLoopMode(loopMode);
  }

  @override
  Future<void> play() => _player.play();

  @override
  Future<void> pause() => _player.pause();

  @override
  Future<void> stop() async {
    await _player.stop();
    await Future<void>.delayed(const Duration(milliseconds: 50));
    await super.stop();
  }

  @override
  Future<void> seek(Duration position) => _player.seek(position);

  @override
  Future<void> setSpeed(double speed) => _player.setSpeed(speed);
}
