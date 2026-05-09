import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'api_client.dart';

const _keyToken = 'auth_token';
const _keyEmail = 'user_email';
const _keyName = 'user_name';

const _storage = FlutterSecureStorage(
  aOptions: AndroidOptions(encryptedSharedPreferences: true),
);

class AuthRepository {
  /// POST /auth/login — stores token on success.
  Future<void> login(String email, String password) async {
    final response = await ApiClient.instance.dio.post(
      'auth/login',
      data: {'email': email, 'password': password},
    );
    final data = response.data['data'] as Map<String, dynamic>;
    final token = data['token'] as String;
    final name = data['name'] as String?;
    await _storeSession(token: token, email: email, name: name);
    ApiClient.instance.setAuthToken(token);
  }

  /// POST /auth/logout — clears token locally (best-effort server call).
  Future<void> logout() async {
    try {
      await ApiClient.instance.dio.post('auth/logout');
    } on DioException {
      // Server rejection on logout is non-fatal — clear locally regardless.
    }
    ApiClient.instance.clearAuthToken();
    await _clearSession();
  }

  /// GET /auth/profile
  Future<Map<String, dynamic>?> getUserProfile() async {
    final response = await ApiClient.instance.dio.get('auth/profile');
    return response.data['data'] as Map<String, dynamic>?;
  }

  Future<String?> getStoredToken() => _storage.read(key: _keyToken);
  Future<String?> getStoredEmail() => _storage.read(key: _keyEmail);
  Future<String?> getStoredName() => _storage.read(key: _keyName);

  /// Re-attach a previously stored token to the Dio headers.
  void applyToken(String token) => ApiClient.instance.setAuthToken(token);

  /// Dev/test only — stores a dummy session without calling the API.
  /// Remove or gate behind kDebugMode before production release.
  Future<void> storeMockSession(String email) async {
    const mockToken = 'dev_mock_token';
    await _storeSession(token: mockToken, email: email);
    ApiClient.instance.setAuthToken(mockToken);
  }

  Future<void> _storeSession({
    required String token,
    required String email,
    String? name,
  }) async {
    final writes = [
      _storage.write(key: _keyToken, value: token),
      _storage.write(key: _keyEmail, value: email),
    ];
    if (name != null) {
      writes.add(_storage.write(key: _keyName, value: name));
    }
    await Future.wait(writes);
  }

  Future<void> _clearSession() async {
    await Future.wait([
      _storage.delete(key: _keyToken),
      _storage.delete(key: _keyEmail),
      _storage.delete(key: _keyName),
    ]);
  }
}
