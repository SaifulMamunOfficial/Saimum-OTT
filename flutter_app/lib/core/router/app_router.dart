import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../app_shell.dart';
import '../../features/auth/presentation/login_page.dart';
import '../../features/auth/presentation/onboarding_screen.dart';
import '../../features/auth/presentation/signup_page.dart';
import '../../features/auth/presentation/splash_screen.dart';
import '../../features/media/presentation/album_detail_page.dart';
import '../../features/media/presentation/artist_detail_page.dart';
import '../../features/media/presentation/home_page.dart';
import '../../features/media/presentation/library_page.dart';
import '../../features/media/presentation/music_list_page.dart';
import '../../features/media/presentation/profile_page.dart';
import '../../features/media/presentation/search_page.dart';
import '../../features/media/presentation/video_list_page.dart';
import '../../features/media/presentation/all_songs_page.dart';
import '../../features/media/presentation/all_albums_page.dart';
import '../../features/media/presentation/all_artists_page.dart';
import '../../features/student_hub/presentation/student_hub_screen.dart';
import '../providers/auth_provider.dart';

// ---------------------------------------------------------------------------
// Routes that should redirect an authenticated user away (back to Home).
// Guests are allowed everywhere except these in reverse.
// ---------------------------------------------------------------------------
const _authOnlyRoutes = {'/login', '/signup'};

// ---------------------------------------------------------------------------
// Auth-aware ChangeNotifier — triggers GoRouter's refreshListenable whenever
// the auth status changes, so the redirect is re-evaluated.
// ---------------------------------------------------------------------------

class _AuthRouterNotifier extends ChangeNotifier {
  final Ref _ref;
  AuthState _authState;

  _AuthRouterNotifier(this._ref)
      : _authState = _ref.read(authProvider) {
    _ref.listen<AuthState>(authProvider, (_, next) {
      _authState = next;
      notifyListeners();
    });
  }

  String? redirect(GoRouterState state) {
    final loc = state.matchedLocation;

    // Splash manages its own routing — never redirect away from it.
    if (loc == '/splash') return null;

    // Auth still resolving — let splash hold the screen.
    if (_authState.isLoading) return '/splash';

    // Authenticated user landed on a login/signup screen — send home.
    if (_authState.isAuthenticated && _authOnlyRoutes.contains(loc)) {
      return '/';
    }

    // Guests are allowed everywhere — individual features self-guard.
    return null;
  }
}

// ---------------------------------------------------------------------------
// Provider
// ---------------------------------------------------------------------------

final routerProvider = Provider<GoRouter>((ref) {
  final notifier = _AuthRouterNotifier(ref);

  return GoRouter(
    initialLocation: '/splash',
    refreshListenable: notifier,
    redirect: (_, state) => notifier.redirect(state),
    routes: [
      // ── Public / auth routes (no shell) ──────────────────────────────────
      GoRoute(
        path: '/splash',
        builder: (_, _) => const SplashScreen(),
      ),
      GoRoute(
        path: '/onboarding',
        builder: (_, _) => const OnboardingScreen(),
      ),
      GoRoute(
        path: '/login',
        builder: (_, _) => const LoginPage(),
      ),
      GoRoute(
        path: '/signup',
        builder: (_, _) => const SignupPage(),
      ),

      // ── Authenticated shell routes ────────────────────────────────────────
      ShellRoute(
        builder: (context, state, child) => MainShell(child: child),
        routes: [
          GoRoute(path: '/', builder: (_, _) => const HomePage()),
          GoRoute(path: '/music', builder: (_, _) => const MusicListPage()),
          GoRoute(path: '/video', builder: (_, _) => const VideoListPage()),
          GoRoute(path: '/search', builder: (_, _) => const SearchPage()),
          GoRoute(path: '/library', builder: (_, _) => const LibraryPage()),
          GoRoute(path: '/profile', builder: (_, _) => const ProfilePage()),
          GoRoute(path: '/all-songs', builder: (_, _) => const AllSongsPage()),
          GoRoute(path: '/all-albums', builder: (_, _) => const AllAlbumsPage()),
          GoRoute(path: '/all-artists', builder: (_, _) => const AllArtistsPage()),
          GoRoute(path: '/student-hub', builder: (_, _) => const StudentHubScreen()),
          GoRoute(
            path: '/artist/:id',
            builder: (_, state) {
              final id =
                  int.tryParse(state.pathParameters['id'] ?? '') ?? 0;
              return ArtistDetailPage(artistId: id);
            },
          ),
          GoRoute(
            path: '/album/:id',
            builder: (_, state) {
              final id =
                  int.tryParse(state.pathParameters['id'] ?? '') ?? 0;
              return AlbumDetailPage(albumId: id);
            },
          ),
        ],
      ),
    ],
  );
});
