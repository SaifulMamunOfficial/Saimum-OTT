import 'dart:async';

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

// ---------------------------------------------------------------------------
// Provider
// ---------------------------------------------------------------------------

final connectivityProvider =
    StateNotifierProvider<ConnectivityNotifier, bool>(
  (ref) => ConnectivityNotifier(),
);

// ---------------------------------------------------------------------------
// Notifier — true = online, false = offline
// ---------------------------------------------------------------------------

class ConnectivityNotifier extends StateNotifier<bool> {
  late final StreamSubscription<List<ConnectivityResult>> _sub;

  ConnectivityNotifier() : super(true) {
    _sub = Connectivity().onConnectivityChanged.listen(_onChanged);
    // Immediately resolve current state.
    Connectivity().checkConnectivity().then(_onChanged);
  }

  void _onChanged(List<ConnectivityResult> results) {
    final online = results.any(
      (r) =>
          r == ConnectivityResult.mobile ||
          r == ConnectivityResult.wifi ||
          r == ConnectivityResult.ethernet,
    );
    if (state != online) state = online;
  }

  bool get isOnline => state;

  @override
  void dispose() {
    _sub.cancel();
    super.dispose();
  }
}
