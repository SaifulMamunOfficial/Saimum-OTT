import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'api_client.dart';
import 'models/user_model.dart';

const _keyToken = 'auth_token';
const _keyUserProfile = 'user_profile_json';
const _keyOnboarding = 'onboarding_v1_completed';

const _storage = FlutterSecureStorage(
  aOptions: AndroidOptions(encryptedSharedPreferences: true),
);

class AuthRepository {
  // ---------------------------------------------------------------------------
  // Remote calls
  // ---------------------------------------------------------------------------

  /// POST /login — fetches token + user profile, persists both.
  Future<UserModel> login(String email, String password) async {
    final response = await ApiClient.instance.dio.post(
      'login',
      data: {
        'user_email': email,
        'user_password': password,
      },
    );
    final data = response.data; // Response is already at top level based on controller
    final user = UserModel.fromJson(data);
    await _storeUser(user);
    ApiClient.instance.setAuthToken(user.token);
    return user;
  }

  /// POST /register — creates account, persists session.
  Future<UserModel> register({
    required String name,
    required String email,
    required String password,
    String role = 'user',
    String? studentId,
  }) async {
    final response = await ApiClient.instance.dio.post(
      'register',
      data: {
        'user_name': name,
        'user_email': email,
        'user_password': password,
        'role': role,
        'student_id': studentId,
      },
    );
    final data = response.data;
    final user = UserModel.fromJson(data);
    await _storeUser(user);
    ApiClient.instance.setAuthToken(user.token);
    return user;
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

  /// GET /auth/profile — re-fetches and refreshes stored user data.
  Future<UserModel?> fetchProfile() async {
    final response = await ApiClient.instance.dio.get('auth/profile');
    final data = response.data['data'] as Map<String, dynamic>?;
    if (data == null) return null;

    // Merge new data with the stored token (profile endpoint may omit it).
    final storedToken = await getStoredToken() ?? '';
    final user = UserModel.fromJson({...data, 'token': storedToken});
    await _storeUser(user);
    return user;
  }

  // ---------------------------------------------------------------------------
  // Local storage helpers
  // ---------------------------------------------------------------------------

  Future<UserModel?> getStoredUser() async {
    final json = await _storage.read(key: _keyUserProfile);
    if (json == null) return null;
    try {
      return UserModel.fromJsonString(json);
    } catch (_) {
      return null;
    }
  }

  Future<String?> getStoredToken() => _storage.read(key: _keyToken);

  /// Re-attach a previously stored token to the Dio headers.
  void applyToken(String token) => ApiClient.instance.setAuthToken(token);

  // ---------------------------------------------------------------------------
  // Onboarding state
  // ---------------------------------------------------------------------------

  Future<bool> hasSeenOnboarding() async {
    return (await _storage.read(key: _keyOnboarding)) == 'true';
  }

  Future<void> markOnboardingComplete() async {
    await _storage.write(key: _keyOnboarding, value: 'true');
  }

  // ---------------------------------------------------------------------------
  // Dev / Test helpers
  // ---------------------------------------------------------------------------

  /// Dev-only — stores a dummy session without calling the API.
  Future<UserModel> storeMockSession(String email) async {
    final user = UserModel(
      id: 0,
      name: 'Dev User',
      email: email,
      role: 'user',
      token: 'dev_mock_token',
    );
    await _storeUser(user);
    ApiClient.instance.setAuthToken(user.token);
    return user;
  }

  // ---------------------------------------------------------------------------
  // Private
  // ---------------------------------------------------------------------------

  Future<void> _storeUser(UserModel user) async {
    await Future.wait([
      _storage.write(key: _keyToken, value: user.token),
      _storage.write(key: _keyUserProfile, value: user.toJsonString()),
    ]);
  }

  Future<void> _clearSession() async {
    await Future.wait([
      _storage.delete(key: _keyToken),
      _storage.delete(key: _keyUserProfile),
    ]);
  }
}
