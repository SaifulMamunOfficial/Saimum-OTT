import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/api_adapter/mock_data.dart';
import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/theme/app_colors.dart';
import '../controllers/media_controller.dart';

class LibraryPage extends ConsumerWidget {
  const LibraryPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return CustomScrollView(
      physics: const BouncingScrollPhysics(),
      slivers: [
        // ── Header ──────────────────────────────────────────────────────────
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 64, 20, 20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('Your Library',
                    style: TextStyle(
                        fontSize: 30,
                        fontWeight: FontWeight.w800,
                        color: AppColors.onSurface,
                        letterSpacing: -0.7)),
                const SizedBox(height: 14),
                // Stats row
                Row(
                  children: [
                    _StatChip(label: '${kMockSongs.length}', sub: 'Songs',
                        color: AppColors.primary),
                    const SizedBox(width: 10),
                    _StatChip(label: '${kMockArtists.length}', sub: 'Artists',
                        color: AppColors.accent),
                    const SizedBox(width: 10),
                    _StatChip(label: '${kMockAlbums.length}', sub: 'Albums',
                        color: const Color(0xFF34C759)),
                  ],
                ),
              ],
            ),
          ),
        ),

        // ── All Music ────────────────────────────────────────────────────────
        const SliverToBoxAdapter(child: _SectionHeader('All Music')),
        SliverList.separated(
          itemCount: kMockSongs.length,
          separatorBuilder: (_, _) =>
              const Divider(height: 1, color: AppColors.glassBorder),
          itemBuilder: (_, i) => _LibSongTile(song: kMockSongs[i], index: i),
        ),

        // ── Artists ──────────────────────────────────────────────────────────
        const SliverToBoxAdapter(child: SizedBox(height: 28)),
        const SliverToBoxAdapter(child: _SectionHeader('Artists')),
        SliverPadding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          sliver: SliverGrid.builder(
            gridDelegate:
                const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 3,
              mainAxisSpacing: 16,
              crossAxisSpacing: 16,
              childAspectRatio: 0.85,
            ),
            itemCount: kMockArtists.length,
            itemBuilder: (_, i) =>
                _ArtistGridItem(artist: kMockArtists[i]),
          ),
        ),

        // ── Albums ───────────────────────────────────────────────────────────
        const SliverToBoxAdapter(child: SizedBox(height: 28)),
        const SliverToBoxAdapter(child: _SectionHeader('Albums')),
        SliverPadding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          sliver: SliverGrid.builder(
            gridDelegate:
                const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 2,
              mainAxisSpacing: 14,
              crossAxisSpacing: 14,
              childAspectRatio: 0.85,
            ),
            itemCount: kMockAlbums.length,
            itemBuilder: (_, i) =>
                _AlbumGridItem(album: kMockAlbums[i]),
          ),
        ),

        // ── Playlists ────────────────────────────────────────────────────────
        const SliverToBoxAdapter(child: SizedBox(height: 28)),
        const SliverToBoxAdapter(child: _SectionHeader('Playlists')),
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Column(
              children: [
                _PlaylistCard(
                    name: 'My Favorites',
                    icon: Icons.favorite_rounded,
                    color: const Color(0xFFFF453A),
                    count: 0),
                const SizedBox(height: 10),
                _PlaylistCard(
                    name: 'Recent Plays',
                    icon: Icons.history_rounded,
                    color: AppColors.primary,
                    count: 0),
                const SizedBox(height: 10),
                _PlaylistCard(
                    name: 'Downloaded',
                    icon: Icons.download_done_rounded,
                    color: const Color(0xFF34C759),
                    count: 0),
              ],
            ),
          ),
        ),

        const SliverToBoxAdapter(child: SizedBox(height: 120)),
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
          child: Image.network(song.thumbnail,
              fit: BoxFit.cover,
              errorBuilder: (_, _, _) => Container(
                  color: AppColors.surfaceOne,
                  child: const Icon(Icons.music_note,
                      color: AppColors.primary, size: 18))),
        ),
      ),
      title: Text(song.title,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(
              fontWeight: FontWeight.w600,
              fontSize: 13,
              color: AppColors.onSurface)),
      subtitle: Text(song.artist,
          style:
              const TextStyle(fontSize: 11, color: AppColors.onSurfaceMuted)),
      trailing: const Icon(Icons.play_circle_outline_rounded,
          color: AppColors.primary, size: 28),
      onTap: () => ref.read(mediaControllerProvider.notifier).play(
            song.audioUrl,
            title: song.title,
            artist: song.artist,
            artworkUrl: song.thumbnail,
          ),
    );
  }
}

// ---------------------------------------------------------------------------
// Artist grid item — circular photo + name
// ---------------------------------------------------------------------------

class _ArtistGridItem extends StatelessWidget {
  final MockArtist artist;
  const _ArtistGridItem({required this.artist});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => context.go('/artist/${artist.id}'),
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
                child: Image.network(artist.imageUrl,
                    fit: BoxFit.cover,
                    errorBuilder: (_, _, _) => Container(
                        color: AppColors.surfaceOne,
                        child: const Icon(Icons.person_rounded,
                            color: AppColors.primary, size: 28))),
              ),
            ),
          ),
          const SizedBox(height: 8),
          Text(artist.name,
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
  final MockAlbum album;
  const _AlbumGridItem({required this.album});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => context.go('/album/${album.id}'),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: SizedBox.expand(
                child: Image.network(album.imageUrl,
                    fit: BoxFit.cover,
                    errorBuilder: (_, _, _) => Container(
                        color: AppColors.surfaceTwo,
                        child: const Icon(Icons.album_rounded,
                            color: AppColors.primary, size: 36))),
              ),
            ),
          ),
          const SizedBox(height: 7),
          Text(album.title,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w600,
                  color: AppColors.onSurface)),
          Text(album.artistName,
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
                Text(count == 0 ? 'Coming in Phase 7' : '$count songs',
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
