import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../features/media/controllers/media_controller.dart';

// ---------------------------------------------------------------------------
// State
// ---------------------------------------------------------------------------

class SleepTimerState {
  /// Null when no timer is active.
  final Duration? remaining;

  const SleepTimerState({this.remaining});

  bool get isActive => remaining != null;

  String get label {
    if (remaining == null) return '';
    final m = remaining!.inMinutes.remainder(60).toString().padLeft(2, '0');
    final s = remaining!.inSeconds.remainder(60).toString().padLeft(2, '0');
    return '$m:$s';
  }
}

// ---------------------------------------------------------------------------
// Provider
// ---------------------------------------------------------------------------

final sleepTimerProvider =
    StateNotifierProvider<SleepTimerController, SleepTimerState>(
  (ref) => SleepTimerController(ref),
);

// ---------------------------------------------------------------------------
// Controller
// ---------------------------------------------------------------------------

class SleepTimerController extends StateNotifier<SleepTimerState> {
  final Ref _ref;
  Timer? _timer;

  SleepTimerController(this._ref) : super(const SleepTimerState());

  /// Starts a countdown of [duration]. Stops playback when it expires.
  /// Cancels any previously running timer first.
  void setTimer(Duration duration) {
    cancelTimer();
    var remaining = duration;
    state = SleepTimerState(remaining: remaining);

    _timer = Timer.periodic(const Duration(seconds: 1), (t) {
      remaining -= const Duration(seconds: 1);
      if (!mounted) {
        t.cancel();
        return;
      }
      if (remaining <= Duration.zero) {
        t.cancel();
        state = const SleepTimerState();
        _ref.read(mediaControllerProvider.notifier).stop();
      } else {
        state = SleepTimerState(remaining: remaining);
      }
    });
  }

  void cancelTimer() {
    _timer?.cancel();
    _timer = null;
    state = const SleepTimerState();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
