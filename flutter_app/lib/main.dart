import 'package:audio_service/audio_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:isar/isar.dart';
import 'package:media_kit/media_kit.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';

import 'app_shell.dart';
import 'core/models/download_manifest.dart';
import 'core/models/playback_snapshot.dart';
import 'core/utils/migration_service.dart';
import 'features/media/audio/saimum_audio_handler.dart';
import 'features/media/controllers/media_controller.dart';

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

  // Boot 3: Open Isar DB
  final dir = await getApplicationDocumentsDirectory();
  await Isar.open(
    [PlaybackSnapshotSchema, DownloadManifestSchema],
    directory: dir.path,
    name: 'saimum_db',
  );

  // Boot 4: Request notification permission (Android 13+ requires runtime grant)
  await Permission.notification.request();

  // Boot 5: Initialize AudioService (singleton background handler)
  final audioHandler = await AudioService.init<SaimumAudioHandler>(
    builder: SaimumAudioHandler.new,
    config: const AudioServiceConfig(
      androidNotificationChannelId: 'com.saimum.saimummusic.channel.audio',
      androidNotificationChannelName: 'Saimum Music',
      androidNotificationOngoing: true,
      androidStopForegroundOnPause: true,
    ),
  );

  // Boot 6: Render UI
  // videoPlayerControllerProvider is pre-warmed inside _BootScreen on first
  // build via ref.watch — no explicit warm-up needed here.
  runApp(
    ProviderScope(
      overrides: [
        audioHandlerProvider.overrideWithValue(audioHandler),
      ],
      child: const AppShell(),
    ),
  );
}
