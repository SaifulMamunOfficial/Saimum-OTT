import 'package:audio_service/audio_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:isar/isar.dart';
import 'package:media_kit/media_kit.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';

import 'app_shell.dart';
import 'core/models/app_log.dart';
import 'core/models/download_manifest.dart';
import 'core/models/playback_snapshot.dart';
import 'core/models/favorite.dart';
import 'core/models/playlist.dart';
import 'core/utils/migration_service.dart';
import 'core/utils/telemetry_service.dart';
import 'core/widgets/error_boundary.dart';
import 'features/media/audio/saimum_audio_handler.dart';
import 'features/media/controllers/media_controller.dart';
import 'core/constants/app_constants.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Boot 0: MediaKit must be initialized before any Player/VideoController
  MediaKit.ensureInitialized();

  // Boot 1: Warm up secure storage
  const secureStorage = FlutterSecureStorage(
    aOptions: AndroidOptions(encryptedSharedPreferences: true),
  );
  await secureStorage.containsKey(key: 'user_token');

  // Boot 2: One-time silent migration from legacy Java app
  await LegacyMigrationService().runIfNeeded();

  // Boot 3: Open Isar DB — include AppLogSchema for local telemetry
  final dir = await getApplicationDocumentsDirectory();
  final isar = await Isar.open(
    [
      PlaybackSnapshotSchema,
      DownloadManifestSchema,
      AppLogSchema,
      FavoriteModelSchema,
      PlaylistModelSchema,
    ],
    directory: dir.path,
    name: 'saimum_db',
  );

  // Boot 4: Initialise telemetry so it can capture errors from this point on
  TelemetryService.instance.init(isar);

  // Boot 5: Wire up global Flutter error handler → telemetry + custom error UI
  _setupErrorHandlers();

  // Boot 6: Request notification permission (Android 13+ requires runtime grant)
  await Permission.notification.request();

  // Boot 7: Initialize AudioService (singleton background handler)
  final audioHandler = await AudioService.init<SaimumAudioHandler>(
    builder: SaimumAudioHandler.new,
    config: const AudioServiceConfig(
      androidNotificationChannelId: 'com.saimum.saimummusic.channel.audio',
      androidNotificationChannelName: AppConstants.appName,
      androidNotificationOngoing: true,
      androidStopForegroundOnPause: true,
    ),
  );

  // Boot 8: Render UI
  runApp(
    ProviderScope(
      overrides: [
        audioHandlerProvider.overrideWithValue(audioHandler),
      ],
      child: const AppShell(),
    ),
  );
}

void _setupErrorHandlers() {
  // Replace Flutter's red-screen with our branded error widget.
  ErrorWidget.builder = (FlutterErrorDetails details) {
    return AppErrorScreen(details: details);
  };

  // Capture uncaught Flutter framework errors to local telemetry.
  final originalOnError = FlutterError.onError;
  FlutterError.onError = (FlutterErrorDetails details) {
    TelemetryService.instance.logError(
      'FlutterError',
      details.exceptionAsString(),
      details.stack,
    );
    originalOnError?.call(details);
  };

  // Capture Dart errors that escape the Flutter framework (e.g. isolates).
  WidgetsBinding.instance.platformDispatcher.onError = (error, stack) {
    TelemetryService.instance.logError(
      'PlatformDispatcher',
      error.toString(),
      stack,
    );
    return false; // let the platform handle it too
  };
}
