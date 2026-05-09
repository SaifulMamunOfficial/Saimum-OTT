import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:isar/isar.dart';

/// Provides the already-opened Isar instance.
/// Must be called only after [Isar.open] completes in main().
final isarProvider = Provider<Isar>((ref) {
  return Isar.getInstance('saimum_db') ??
      (throw StateError('Isar not initialized. Call Isar.open() in main() first.'));
});
