import 'dart:io';

import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

/// Checks whether the OS is throttling Saimum Music's background execution
/// and provides a one-call helper to open the system battery optimisation
/// settings screen.
///
/// Android-only: on other platforms every method is a safe no-op.
class BatteryOptimizationService {
  BatteryOptimizationService._();
  static final BatteryOptimizationService instance =
      BatteryOptimizationService._();

  /// Returns `true` when the app is already whitelisted from battery
  /// optimisation (i.e. background execution is unrestricted).
  Future<bool> isIgnoringBatteryOptimizations() async {
    if (!Platform.isAndroid) return true;
    try {
      return await Permission.ignoreBatteryOptimizations.isGranted;
    } on PlatformException {
      return false;
    }
  }

  /// Opens the system dialog that lets the user add the app to the
  /// "Unrestricted" / "Don't optimise battery" list.
  ///
  /// The dialog is shown by Android itself — no custom UI is needed here.
  Future<void> requestWhitelisting() async {
    if (!Platform.isAndroid) return;
    try {
      await Permission.ignoreBatteryOptimizations.request();
    } on PlatformException catch (e) {
      // Fallback: open the full battery-optimisation settings screen.
      if (e.code == 'ERROR_LAUNCHING_INTENT') {
        await openAppSettings();
      }
    }
  }
}
