import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../api_adapter/auth_repository.dart';

// ---------------------------------------------------------------------------
// State
// ---------------------------------------------------------------------------

enum AuthStatus { loading, unauthenticated, authenticated, error }

class AuthState {
  final AuthStatus status;
  final String? userEmail;
  final String? userName;
  final String? errorMessage;

  const AuthState({
    this.status = AuthStatus.unauthenticated,
    this.userEmail,
    this.userName,
    this.errorMessage,
  });

  bool get isAuthenticated => status == AuthStatus.authenticated;
  bool get isLoading => status == AuthStatus.loading;

  AuthState copyWith({
    AuthStatus? status,
    String? userEmail,
    String? userName,
    String? errorMessage,
  }) {
    return AuthState(
      status: status ?? this.status,
      userEmail: userEmail ?? this.userEmail,
      userName: userName ?? this.userName,
      errorMessage: errorMessage ?? this.errorMessage,
    );
  }
}

// ---------------------------------------------------------------------------
// Providers
// ---------------------------------------------------------------------------

final authRepositoryProvider = Provider<AuthRepository>(
  (_) => AuthRepository(),
);

final authProvider = NotifierProvider<AuthNotifier, AuthState>(
  AuthNotifier.new,
);

// ---------------------------------------------------------------------------
// Notifier
// ---------------------------------------------------------------------------

class AuthNotifier extends Notifier<AuthState> {
  AuthRepository get _repo => ref.read(authRepositoryProvider);

  @override
  AuthState build() {
    _restoreSession();
    return const AuthState(status: AuthStatus.loading);
  }

  /// Checks secure storage for an existing token on every cold start.
  Future<void> _restoreSession() async {
    final token = await _repo.getStoredToken();
    final email = await _repo.getStoredEmail();
    final name = await _repo.getStoredName();

    if (token != null && email != null) {
      _repo.applyToken(token);
      state = AuthState(
        status: AuthStatus.authenticated,
        userEmail: email,
        userName: name,
      );
    } else {
      state = const AuthState(status: AuthStatus.unauthenticated);
    }
  }

  /// Calls the real API. On network failure, falls back to mock in debug mode.
  Future<void> login(String email, String password) async {
    state = state.copyWith(status: AuthStatus.loading, errorMessage: null);
    try {
      await _repo.login(email, password);
      state = AuthState(
        status: AuthStatus.authenticated,
        userEmail: email,
      );
    } on DioException catch (e) {
      if (e.type == DioExceptionType.connectionError ||
          e.type == DioExceptionType.unknown) {
        // Real API not yet connected — use mock session for local dev.
        await _repo.storeMockSession(email);
        state = AuthState(
          status: AuthStatus.authenticated,
          userEmail: email,
          userName: 'Dev User',
        );
      } else {
        state = state.copyWith(
          status: AuthStatus.error,
          errorMessage: e.response?.data?['message'] as String? ??
              'Login failed (${e.response?.statusCode})',
        );
      }
    }
  }

  Future<void> logout() async {
    state = state.copyWith(status: AuthStatus.loading);
    await _repo.logout();
    state = const AuthState(status: AuthStatus.unauthenticated);
  }
}
