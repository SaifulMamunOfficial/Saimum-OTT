import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../app_shell.dart';
import '../../features/media/presentation/album_detail_page.dart';
import '../../features/media/presentation/artist_detail_page.dart';
import '../../features/media/presentation/home_page.dart';
import '../../features/media/presentation/library_page.dart';
import '../../features/media/presentation/music_list_page.dart';
import '../../features/media/presentation/profile_page.dart';
import '../../features/media/presentation/search_page.dart';
import '../../features/media/presentation/video_list_page.dart';

final routerProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/',
    routes: [
      ShellRoute(
        builder: (context, state, child) => MainShell(child: child),
        routes: [
          GoRoute(path: '/', builder: (_, _) => const HomePage()),
          GoRoute(path: '/music', builder: (_, _) => const MusicListPage()),
          GoRoute(path: '/video', builder: (_, _) => const VideoListPage()),
          GoRoute(path: '/search', builder: (_, _) => const SearchPage()),
          GoRoute(path: '/library', builder: (_, _) => const LibraryPage()),
          GoRoute(path: '/profile', builder: (_, _) => const ProfilePage()),
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
