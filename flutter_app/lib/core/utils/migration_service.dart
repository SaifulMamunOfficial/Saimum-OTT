import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

const _channel = MethodChannel('com.saimum.saimummusic/legacy_migration');
const _storage = FlutterSecureStorage(
  aOptions: AndroidOptions(encryptedSharedPreferences: true),
);

class LegacyMigrationService {
  Future<void> runIfNeeded() async {
    final completed = await _storage.read(key: 'migration_completed');
    if (completed == 'true') return;

    try {
      final result = await _channel.invokeMethod<Map<Object?, Object?>>(
        'readLegacyUser',
      );

      if (result == null) {
        await _markDone();
        return;
      }

      final data = result.cast<String, String>();

      await Future.wait([
        if ((data['uid'] ?? '').isNotEmpty)
          _storage.write(key: 'user_id', value: data['uid']),
        if ((data['name'] ?? '').isNotEmpty)
          _storage.write(key: 'user_name', value: data['name']),
        if ((data['email'] ?? '').isNotEmpty)
          _storage.write(key: 'user_email', value: data['email']),
        if ((data['mobile'] ?? '').isNotEmpty)
          _storage.write(key: 'user_mobile', value: data['mobile']),
        if ((data['loginType'] ?? '').isNotEmpty)
          _storage.write(key: 'login_type', value: data['loginType']),
        if ((data['auth_id'] ?? '').isNotEmpty)
          _storage.write(key: 'auth_id', value: data['auth_id']),
        if ((data['profile'] ?? '').isNotEmpty)
          _storage.write(key: 'profile_image', value: data['profile']),
        _storage.write(key: 'is_logged_in', value: 'true'),
      ]);

      await _channel.invokeMethod<void>('markMigrationComplete');
      await _markDone();
    } on PlatformException {
      // Native side error — log in Phase 8. Migration skipped, not fatal.
    } on MissingPluginException {
      // Running on non-Android (web/desktop) — skip silently.
    }
  }

  Future<void> _markDone() async {
    await _storage.write(key: 'migration_completed', value: 'true');
  }
}
