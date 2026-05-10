import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../api_adapter/auth_repository.dart';
import '../api_adapter/models/user_model.dart';

// ---------------------------------------------------------------------------
// State
// ---------------------------------------------------------------------------

enum AuthStatus { loading, unauthenticated, authenticated, error }

class AuthState {
  final AuthStatus status;
  final UserModel? user;
  final String? errorMessage;

  const AuthState({
    this.status = AuthStatus.unauthenticated,
    this.user,
    this.errorMessage,
  });

  bool get isAuthenticated => status == AuthStatus.authenticated;
  bool get isLoading => status == AuthStatus.loading;

  /// True when no session exists — the user is browsing without an account.
  bool get isGuest => user == null && status != AuthStatus.loading;

  // Convenience accessors so existing UI doesn't need migration all at once.
  String? get userEmail => user?.email;
  String? get userName => user?.name;
  String? get userRole => user?.role;
  bool get isStudent => user?.isStudent ?? false;

  AuthState copyWith({
    AuthStatus? status,
    UserModel? user,
    String? errorMessage,
    bool clearUser = false,
    bool clearError = false,
  }) {
    return AuthState(
      status: status ?? this.status,
      user: clearUser ? null : (user ?? this.user),
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
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

  /// Checks secure storage for an existing session on every cold start.
  Future<void> _restoreSession() async {
    final token = await _repo.getStoredToken();
    final user = await _repo.getStoredUser();

    if (token != null && user != null) {
      _repo.applyToken(token);
      state = AuthState(status: AuthStatus.authenticated, user: user);
    } else {
      state = const AuthState(status: AuthStatus.unauthenticated);
    }
  }

  // ---------------------------------------------------------------------------
  // Login
  // ---------------------------------------------------------------------------

  /// Calls the real Laravel API. Falls back to mock session on connection
  /// errors so local dev works without a running backend.
  Future<void> login(String email, String password) async {
    state = state.copyWith(
      status: AuthStatus.loading,
      clearError: true,
    );
    try {
      final user = await _repo.login(email, password);
      state = AuthState(status: AuthStatus.authenticated, user: user);
    } on DioException catch (e) {
      if (e.type == DioExceptionType.connectionError ||
          e.type == DioExceptionType.unknown) {
        // Real API unreachable — use mock session for local dev.
        final user = await _repo.storeMockSession(email);
        state = AuthState(status: AuthStatus.authenticated, user: user);
      } else {
        final msg = _extractMessage(e) ?? 'Login failed. Please try again.';
        state = state.copyWith(
          status: AuthStatus.error,
          errorMessage: msg,
        );
      }
    } catch (e) {
      state = state.copyWith(
        status: AuthStatus.error,
        errorMessage: 'An unexpected error occurred.',
      );
    }
  }

  // ---------------------------------------------------------------------------
  // Register
  // ---------------------------------------------------------------------------

  Future<void> register({
    required String name,
    required String email,
    required String password,
    String role = 'user',
    String? studentId,
  }) async {
    state = state.copyWith(status: AuthStatus.loading, clearError: true);
    try {
      final user = await _repo.register(
        name: name,
        email: email,
        password: password,
        role: role,
        studentId: studentId,
      );
      state = AuthState(status: AuthStatus.authenticated, user: user);
    } on DioException catch (e) {
      if (e.type == DioExceptionType.connectionError ||
          e.type == DioExceptionType.unknown) {
        final user = await _repo.storeMockSession(email);
        state = AuthState(
          status: AuthStatus.authenticated,
          user: user.copyWith(name: name),
        );
      } else {
        final msg = _extractMessage(e) ?? 'Registration failed. Try again.';
        state = state.copyWith(
          status: AuthStatus.error,
          errorMessage: msg,
        );
      }
    } catch (e) {
      state = state.copyWith(
        status: AuthStatus.error,
        errorMessage: 'An unexpected error occurred.',
      );
    }
  }

  // ---------------------------------------------------------------------------
  // Logout
  // ---------------------------------------------------------------------------

  Future<void> logout() async {
    state = state.copyWith(status: AuthStatus.loading);
    await _repo.logout();
    state = const AuthState(status: AuthStatus.unauthenticated);
  }

  // ---------------------------------------------------------------------------
  // Private helpers
  // ---------------------------------------------------------------------------

  String? _extractMessage(DioException e) {
    try {
      final data = e.response?.data;
      if (data is Map) {
        return data['MSG'] as String? ?? 
               data['message'] as String? ?? 
               data['error'] as String?;
      }
    } catch (_) {}
    return null;
  }
}
