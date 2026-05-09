import 'package:flutter/foundation.dart';
import 'package:isar/isar.dart';

import '../models/app_log.dart';

/// Lightweight local telemetry: writes structured log entries to Isar.
/// Max 500 entries are kept — oldest are pruned automatically.
///
/// Usage:
///   TelemetryService.instance.logError('AudioHandler', 'Source failed', st);
///   TelemetryService.instance.logInfo('Download', 'Chunk 3/10 complete');
class TelemetryService {
  TelemetryService._();
  static final TelemetryService instance = TelemetryService._();

  static const _maxLogs = 500;

  Isar? _isar;

  void init(Isar isar) => _isar = isar;

  Future<void> logError(
    String context,
    String message, [
    StackTrace? stackTrace,
  ]) =>
      _write(LogLevel.error, context, message,
          stackTrace: stackTrace?.toString());

  Future<void> logWarning(String context, String message) =>
      _write(LogLevel.warning, context, message);

  Future<void> logInfo(String context, String message) =>
      _write(LogLevel.info, context, message);

  Future<void> _write(
    LogLevel level,
    String context,
    String message, {
    String? stackTrace,
  }) async {
    final db = _isar;
    if (db == null) return;

    try {
      final entry = AppLog()
        ..level = level
        ..context = context
        ..message = message
        ..stackTrace = stackTrace
        ..timestamp = DateTime.now().millisecondsSinceEpoch;

      await db.writeTxn(() async {
        await db.appLogs.put(entry);
        // Prune oldest logs if over the limit.
        final count = await db.appLogs.count();
        if (count > _maxLogs) {
          // ignore: undefined_method — Isar 3.x findAll() resolved by CFE
          final oldest = await db.appLogs
              .where()
              .sortByTimestamp()
              .limit(count - _maxLogs)
              .findAll();
          await db.appLogs.deleteAll(oldest.map((l) => l.id).toList());
        }
      });

      if (kDebugMode) {
        debugPrint('[${level.name.toUpperCase()}] $context: $message');
      }
    } catch (e) {
      if (kDebugMode) debugPrint('[Telemetry] Write failed: $e');
    }
  }
}
