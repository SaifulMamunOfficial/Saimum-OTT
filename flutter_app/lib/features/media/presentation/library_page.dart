import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/models/download_manifest.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/song_play_count_row.dart';
import '../../../core/widgets/song_thumbnail.dart';
import '../controllers/download_controller.dart';
import '../controllers/media_controller.dart';
import '../controllers/favorite_controller.dart';
import '../controllers/playlist_controller.dart';
import '../controllers/favorite_songs_provider.dart';
import '../controllers/home_controller.dart';
import '../shared/download_icon_button.dart';

class LibraryPage extends ConsumerWidget {
  const LibraryPage({super.key});

  void _showCreatePlaylistDialog(BuildContext context, WidgetRef ref) {
    final controller = TextEditingController();
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.surfaceTwo,
        title: const Text('New Playlist'),
        content: TextField(
          controller: controller,
          autofocus: true,
          decoration: const InputDecoration(
            hintText: 'Enter playlist name',
            hintStyle: TextStyle(color: AppColors.onSurfaceMuted),
          ),
          style: const TextStyle(color: AppColors.onSurface),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel', style: TextStyle(color: AppColors.onSurfaceMuted)),
          ),
          ElevatedButton(
            onPressed: () {
              if (controller.text.isNotEmpty) {
                ref.read(playlistsProvider.notifier).createPlaylist(controller.text);
                Navigator.pop(ctx);
              }
            },
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.primary),
            child: const Text('Create', style: TextStyle(color: Colors.black)),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final dlState = ref.watch(downloadControllerProvider);
    final favoriteSongsAsync = ref.watch(favoriteSongsProvider);
    final playlists = ref.watch(playlistsProvider);
    final homeDataAsync = ref.watch(homeDataProvider);

    return CustomScrollView(
      physics: const BouncingScrollPhysics(),
      slivers: [
        SliverAppBar(
          pinned: true,
          backgroundColor: AppColors.background,
          toolbarHeight: 0.1,
          elevation: 0,
        ),
        // ── Header ──────────────────────────────────────────────────────────
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 20, 20, 20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text('Your Library',
                        style: TextStyle(
                            fontSize: 30,
                            fontWeight: FontWeight.w800,
                            color: AppColors.onSurface,
                            letterSpacing: -0.7)),
                    IconButton(
                      icon: const Icon(Icons.add_box_rounded, color: AppColors.primary, size: 28),
                      onPressed: () => _showCreatePlaylistDialog(context, ref),
                    ),
                  ],
                ),
                const SizedBox(height: 14),
                Row(
                  children: [
                    _StatChip(
                        label: '${ref.watch(favoriteIdsProvider).length}', 
                        sub: 'Liked',
                        color: AppColors.primary),
                    const SizedBox(width: 10),
                    _StatChip(
                        label: '${dlState.completedManifests.length}', 
                        sub: 'Offline',
                        color: const Color(0xFF34C759)),
                    const SizedBox(width: 10),
                    _StatChip(
                        label: '${playlists.length}', 
                        sub: 'Playlists',
                        color: AppColors.accent),
                  ],
                ),
              ],
            ),
          ),
        ),

        // ── Favorite Songs ───────────────────────────────────────────────────
        const SliverToBoxAdapter(child: _SectionHeader('Favorite Songs')),
        favoriteSongsAsync.when(
          data: (songs) {
            if (songs.isEmpty) {
              return const SliverToBoxAdapter(
                child: Padding(
                  padding: EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                  child: Text('No liked songs yet.', style: TextStyle(fontSize: 13, color: AppColors.onSurfaceMuted)),
                ),
              );
            }
            return SliverList.separated(
              itemCount: songs.length,
              separatorBuilder: (_, _) => const Divider(height: 1, color: AppColors.glassBorder),
              itemBuilder: (_, i) => _LibSongTile(song: songs[i], index: i),
            );
          },
          loading: () => const SliverToBoxAdapter(child: Center(child: Padding(padding: EdgeInsets.all(20), child: CircularProgressIndicator()))),
          error: (e, _) => SliverToBoxAdapter(child: Center(child: Text('Error loading favorites: $e'))),
        ),

        // ── Downloaded Songs ─────────────────────────────────────────────────
        const SliverToBoxAdapter(child: SizedBox(height: 24)),
        const SliverToBoxAdapter(child: _SectionHeader('Downloaded Songs')),
        _DownloadedSongsList(),

        // ── Artists ──────────────────────────────────────────────────────────
        const SliverToBoxAdapter(child: SizedBox(height: 28)),
        const SliverToBoxAdapter(child: _SectionHeader('Featured Artists')),
        homeDataAsync.when(
          data: (data) {
            final artists = data['artists'] as List<dynamic>;
            return SliverPadding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              sliver: SliverGrid.builder(
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 3,
                  mainAxisSpacing: 16,
                  crossAxisSpacing: 16,
                  childAspectRatio: 0.85,
                ),
                itemCount: artists.length > 6 ? 6 : artists.length,
                itemBuilder: (_, i) {
                  final a = artists[i];
                  return _ArtistGridItem(
                    id: a['id'],
                    name: a['name'],
                    image: a['image'],
                  );
                },
              ),
            );
          },
          loading: () => const SliverToBoxAdapter(child: SizedBox.shrink()),
          error: (_, __) => const SliverToBoxAdapter(child: SizedBox.shrink()),
        ),

        // ── Albums ───────────────────────────────────────────────────────────
        const SliverToBoxAdapter(child: SizedBox(height: 28)),
        const SliverToBoxAdapter(child: _SectionHeader('Recent Albums')),
        homeDataAsync.when(
          data: (data) {
            final albums = data['albums'] as List<dynamic>;
            return SliverPadding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              sliver: SliverGrid.builder(
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  mainAxisSpacing: 14,
                  crossAxisSpacing: 14,
                  childAspectRatio: 0.85,
                ),
                itemCount: albums.length > 4 ? 4 : albums.length,
                itemBuilder: (_, i) {
                  final al = albums[i];
                  return _AlbumGridItem(
                    id: al['id'],
                    title: al['name'],
                    image: al['image'],
                    artistName: 'Various Artists',
                  );
                },
              ),
            );
          },
          loading: () => const SliverToBoxAdapter(child: SizedBox.shrink()),
          error: (_, __) => const SliverToBoxAdapter(child: SizedBox.shrink()),
        ),

        // ── Playlists ────────────────────────────────────────────────────────
        const SliverToBoxAdapter(child: SizedBox(height: 28)),
        const SliverToBoxAdapter(child: _SectionHeader('Your Collections')),
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Column(
              children: [
                _PlaylistCard(
                    name: 'My Playlists',
                    icon: Icons.playlist_play_rounded,
                    color: AppColors.accent,
                    count: playlists.length),
                const SizedBox(height: 10),
                _PlaylistCard(
                    name: 'Recently Played',
                    icon: Icons.history_rounded,
                    color: AppColors.primary,
                    count: 0),
              ],
            ),
          ),
        ),

        const SliverToBoxAdapter(child: SizedBox(height: 160)),
      ],
    );
  }
}

// ---------------------------------------------------------------------------
// Stat chip
// ---------------------------------------------------------------------------

class _StatChip extends StatelessWidget {
  final String label;
  final String sub;
  final Color color;
  const _StatChip(
      {required this.label, required this.sub, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(10),
        color: color.withValues(alpha: 0.1),
        border: Border.all(color: color.withValues(alpha: 0.35), width: 1),
      ),
      child: Column(
        children: [
          Text(label,
              style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w800,
                  color: color,
                  letterSpacing: -0.3)),
          Text(sub,
              style: const TextStyle(
                  fontSize: 11, color: AppColors.onSurfaceMuted)),
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Section header
// ---------------------------------------------------------------------------

class _SectionHeader extends StatelessWidget {
  final String text;
  const _SectionHeader(this.text);

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.fromLTRB(20, 0, 20, 12),
        child: Text(text,
            style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w700,
                color: AppColors.onSurface,
                letterSpacing: -0.3)),
      );
}

// ---------------------------------------------------------------------------
// Song tile (compact)
// ---------------------------------------------------------------------------

class _LibSongTile extends ConsumerWidget {
  final SongModel song;
  final int index;
  const _LibSongTile({required this.song, required this.index});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ListTile(
      contentPadding:
          const EdgeInsets.symmetric(horizontal: 20, vertical: 2),
      leading: ClipRRect(
        borderRadius: BorderRadius.circular(6),
          child: SizedBox(
          width: 46,
          height: 46,
          child: SongThumbnail(
            url: song.thumbnail,
            fallback: Container(
              color: AppColors.surfaceOne,
              child: const Icon(Icons.music_note,
                  color: AppColors.primary, size: 18),
            ),
          ),
        ),
      ),
      title: Text(song.title,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(
              fontWeight: FontWeight.w600,
              fontSize: 13,
              color: AppColors.onSurface)),
      subtitle: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(song.artist,
              style: const TextStyle(
                  fontSize: 11, color: AppColors.onSurfaceMuted)),
          const SizedBox(height: 3),
          SongPlayCountRow(totalViews: song.totalViews, iconSize: 11, fontSize: 10.5),
        ],
      ),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          DownloadIconButton(song: song),
          const SizedBox(width: 8),
          const Icon(Icons.play_circle_outline_rounded,
              color: AppColors.primary, size: 28),
        ],
      ),
      onTap: () => ref.read(mediaControllerProvider.notifier).play(
            song.audioUrl,
            title: song.title,
            artist: song.artist,
            artworkUrl: song.thumbnail,
            songId: song.id,
          ),
    );
  }
}

// ---------------------------------------------------------------------------
// Downloaded songs list — fetches from Isar
// ---------------------------------------------------------------------------

class _DownloadedSongsList extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final manifests = ref.watch(downloadControllerProvider).completedManifests;

    if (manifests.isEmpty) {
      return const SliverToBoxAdapter(
        child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 20, vertical: 10),
          child: Text('No offline songs yet.',
              style: TextStyle(fontSize: 13, color: AppColors.onSurfaceMuted)),
        ),
      );
    }

    return SliverList.builder(
      itemCount: manifests.length,
      itemBuilder: (_, i) {
        final m = manifests[i];
        return ListTile(
          contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 2),
          leading: ClipRRect(
            borderRadius: BorderRadius.circular(6),
              child: SizedBox(
              width: 46,
              height: 46,
              child: SongThumbnail(
                url: m.thumbnailUrl,
                fallback: Container(
                  color: AppColors.surfaceOne,
                  child: const Icon(Icons.music_note,
                      color: AppColors.primary, size: 18),
                ),
              ),
            ),
          ),
          title: Text(m.title,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                  fontWeight: FontWeight.w600,
                  fontSize: 13,
                  color: AppColors.onSurface)),
          subtitle: Text(m.artist,
              style: const TextStyle(fontSize: 11, color: AppColors.onSurfaceMuted)),
          trailing: IconButton(
            icon: const Icon(Icons.delete_outline_rounded,
                color: Colors.redAccent, size: 22),
            onPressed: () => ref
                .read(downloadControllerProvider.notifier)
                .deleteDownload(m.mediaId),
          ),
          onTap: () => ref.read(mediaControllerProvider.notifier).playOffline(
                m.mediaId,
                title: m.title,
                artist: m.artist,
                artworkUrl: m.thumbnailUrl,
              ),
        );
      },
    );
  }
}

// ---------------------------------------------------------------------------
// Artist grid item — circular photo + name
// ---------------------------------------------------------------------------

class _ArtistGridItem extends StatelessWidget {
  final int id;
  final String name;
  final String image;

  const _ArtistGridItem({
    required this.id,
    required this.name,
    required this.image,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => context.go('/artist/$id'),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              border: Border.all(
                  color: AppColors.primary.withValues(alpha: 0.5),
                  width: 1.5),
              boxShadow: [
                BoxShadow(
                    color: AppColors.primary.withValues(alpha: 0.12),
                    blurRadius: 12)
              ],
            ),
            child: ClipOval(
              child: SizedBox(
                width: 72,
                height: 72,
                child: SongThumbnail(
                  url: image,
                  fallback: Container(
                    color: AppColors.surfaceOne,
                    child: const Icon(Icons.person_rounded,
                        color: AppColors.primary, size: 28),
                  ),
                ),
              ),
            ),
          ),
          const SizedBox(height: 8),
          Text(name,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              textAlign: TextAlign.center,
              style: const TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w600,
                  color: AppColors.onSurface)),
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Album grid item — square cover + title
// ---------------------------------------------------------------------------

class _AlbumGridItem extends StatelessWidget {
  final int id;
  final String title;
  final String image;
  final String artistName;

  const _AlbumGridItem({
    required this.id,
    required this.title,
    required this.image,
    required this.artistName,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => context.go('/album/$id'),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: SizedBox.expand(
                child: SongThumbnail(
                  url: image,
                  fallback: Container(
                    color: AppColors.surfaceTwo,
                    child: const Icon(Icons.album_rounded,
                        color: AppColors.primary, size: 36),
                  ),
                ),
              ),
            ),
          ),
          const SizedBox(height: 7),
          Text(title,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w600,
                  color: AppColors.onSurface)),
          Text(artistName,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                  fontSize: 11, color: AppColors.onSurfaceMuted)),
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Playlist card — placeholder
// ---------------------------------------------------------------------------

class _PlaylistCard extends StatelessWidget {
  final String name;
  final IconData icon;
  final Color color;
  final int count;
  const _PlaylistCard(
      {required this.name,
      required this.icon,
      required this.color,
      required this.count});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(12),
        color: AppColors.surfaceTwo,
        border: Border.all(color: AppColors.glassBorder, width: 0.5),
      ),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(10),
              color: color.withValues(alpha: 0.15),
            ),
            child: Icon(icon, color: color, size: 22),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(name,
                    style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                        color: AppColors.onSurface)),
                Text(count == 0 ? 'No items' : '$count items',
                    style: const TextStyle(
                        fontSize: 11, color: AppColors.onSurfaceMuted)),
              ],
            ),
          ),
          const Icon(Icons.chevron_right_rounded,
              color: AppColors.onSurfaceMuted, size: 20),
        ],
      ),
    );
  }
}
