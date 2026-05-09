import 'package:audio_service/audio_service.dart';
import 'package:audio_session/audio_session.dart';
import 'package:flutter/foundation.dart';
import 'package:just_audio/just_audio.dart';

class SaimumAudioHandler extends BaseAudioHandler with SeekHandler {
  final _player = AudioPlayer();

  SaimumAudioHandler() {
    _configureSession();
    _player.playbackEventStream
        .map(_buildPlaybackState)
        .pipe(playbackState);
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

    // Pause on headphone unplug / Bluetooth disconnect
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
      systemActions: const {MediaAction.seek},
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
    );
  }

  Future<void> playFromUrl(String url, {MediaItem? mediaItem}) async {
    // Always publish metadata before loading so the notification has a title
    this.mediaItem.add(
      mediaItem ??
          MediaItem(
            id: url,
            title: 'Saimum Music',
            artist: 'Streaming',
            album: 'Saimum Music',
          ),
    );
    await _player.setAudioSource(AudioSource.uri(Uri.parse(url)));
    await _player.play();
  }

  /// Plays from a custom [AudioSource] (e.g., [DecryptionAudioSource]).
  /// Publishes [mediaItem] to the notification / lock screen.
  Future<void> playFromSource(AudioSource source, {MediaItem? mediaItem}) async {
    this.mediaItem.add(
      mediaItem ??
          MediaItem(
            id: 'offline',
            title: 'Saimum Music',
            artist: 'Offline',
            album: 'Saimum Music',
          ),
    );
    await _player.setAudioSource(source);
    await _player.play();
  }

  @override
  Future<void> play() => _player.play();

  @override
  Future<void> pause() => _player.pause();

  /// True when the underlying player is fully idle — used as a guard
  /// in VideoPlayerController to skip redundant stop calls.
  bool get isStopped =>
      _player.processingState == ProcessingState.idle && !_player.playing;

  @override
  Future<void> stop() async {
    debugPrint('DEBUG: AudioHandler stop initiated');
    await _player.stop();
    // 50ms flush window: lets playbackEventStream drain all pending events into
    // the BehaviorSubject before super.stop() broadcasts its idle state and
    // closes downstream listeners — eliminates "Bad state: cannot add to closed
    // stream" RxDart re-entrancy.
    await Future<void>.delayed(const Duration(milliseconds: 50));
    await super.stop();
    debugPrint('DEBUG: AudioHandler stop finished');
  }

  @override
  Future<void> seek(Duration position) => _player.seek(position);

  @override
  Future<void> setSpeed(double speed) => _player.setSpeed(speed);

  @override
  Future<void> onTaskRemoved() async {
    await _player.stop();
    await super.onTaskRemoved();
  }
}
