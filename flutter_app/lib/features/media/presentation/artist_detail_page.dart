import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/api_adapter/mock_data.dart';
import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/song_thumbnail.dart';
import '../controllers/media_controller.dart';
import '../shared/download_icon_button.dart';

class ArtistDetailPage extends ConsumerWidget {
  final int artistId;
  const ArtistDetailPage({super.key, required this.artistId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final artist = artistById(artistId);
    if (artist == null) {
      return Scaffold(
        backgroundColor: AppColors.background,
        appBar: AppBar(backgroundColor: AppColors.background),
        body: const Center(
          child: Text('Artist not found',
              style: TextStyle(color: AppColors.onSurfaceMuted)),
        ),
      );
    }
    final songs = songsForArtist(artist.name);
    final albums =
        kMockAlbums.where((a) => a.artistName == artist.name).toList();

    return Scaffold(
      backgroundColor: AppColors.background,
      body: CustomScrollView(
        physics: const BouncingScrollPhysics(),
        slivers: [
          // ── Header ──────────────────────────────────────────────────────
          SliverAppBar(
            expandedHeight: 300,
            pinned: true,
            backgroundColor: AppColors.background,
            leading: IconButton(
              icon: const Icon(Icons.arrow_back_ios_new_rounded,
                  color: AppColors.onSurface, size: 20),
              onPressed: () => context.pop(),
            ),
            // Title only visible in the pinned/collapsed bar — never overlaps
            // the header content that already shows the name.
            title: Text(
              artist.name,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w700,
                  color: AppColors.onSurface),
            ),
            flexibleSpace: FlexibleSpaceBar(
              // No title here — using SliverAppBar.title to avoid double-name
              // overlap during the expand/collapse animation.
              collapseMode: CollapseMode.pin,
              stretchModes: const [],
              background: _ArtistHeader(artist: artist),
            ),
          ),

          // ── Top Tracks ──────────────────────────────────────────────────
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(20, 24, 20, 12),
              child: Text(
                songs.isEmpty ? 'No tracks found' : 'Top Tracks',
                style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w700,
                    color: AppColors.onSurface,
                    letterSpacing: -0.3),
              ),
            ),
          ),
          if (songs.isEmpty)
            const SliverToBoxAdapter(
              child: Padding(
                padding: EdgeInsets.symmetric(horizontal: 20),
                child: Text('No tracks available yet.',
                    style: TextStyle(color: AppColors.onSurfaceMuted)),
              ),
            )
          else
            SliverList.builder(
              itemCount: songs.length,
              itemBuilder: (_, i) =>
                  _ArtistTrackTile(song: songs[i], index: i),
            ),

          // ── Albums ──────────────────────────────────────────────────────
          if (albums.isNotEmpty) ...[
            const SliverToBoxAdapter(
              child: Padding(
                padding: EdgeInsets.fromLTRB(20, 28, 20, 12),
                child: Text('Albums',
                    style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w700,
                        color: AppColors.onSurface,
                        letterSpacing: -0.3)),
              ),
            ),
            SliverToBoxAdapter(
              child: SizedBox(
                height: 182,
                child: ListView.builder(
                  scrollDirection: Axis.horizontal,
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  itemCount: albums.length,
                  itemBuilder: (_, i) => _AlbumCard(album: albums[i]),
                ),
              ),
            ),
          ],

          const SliverToBoxAdapter(child: SizedBox(height: 120)),
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Artist header — blurred bg + circular sharp avatar + bio
// ---------------------------------------------------------------------------

class _ArtistHeader extends StatelessWidget {
  final MockArtist artist;
  const _ArtistHeader({required this.artist});

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        // Blurred background (fills entire header)
        Positioned.fill(
          child: ImageFiltered(
            imageFilter: ImageFilter.blur(sigmaX: 42, sigmaY: 42),
            child: SongThumbnail(
              url: artist.imageUrl,
              fallback: Container(color: AppColors.surfaceTwo),
            ),
          ),
        ),
        // Dark scrim
        Positioned.fill(
          child: DecoratedBox(
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Colors.black.withValues(alpha: 0.35),
                  Colors.black.withValues(alpha: 0.75),
                ],
              ),
            ),
          ),
        ),
        // Content — name is NOT repeated here; SliverAppBar.title handles
        // it in the pinned bar to prevent double-name overlap.
        Positioned(
          bottom: 0,
          left: 0,
          right: 0,
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Circular sharp avatar with neon border
                Container(
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(color: AppColors.primary, width: 2.5),
                    boxShadow: [
                      BoxShadow(
                          color: AppColors.primary.withValues(alpha: 0.35),
                          blurRadius: 20),
                    ],
                  ),
                  child: ClipOval(
                    child: SizedBox(
                      width: 96,
                      height: 96,
                      child: SongThumbnail(
                        url: artist.imageUrl,
                        fallback: Container(
                          color: AppColors.surfaceOne,
                          child: const Icon(Icons.person_rounded,
                              color: AppColors.primary, size: 40),
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                // Artist name — large, bold, constrained to one line
                Text(
                  artist.name,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.w800,
                      color: Colors.white,
                      letterSpacing: -0.4),
                ),
                const SizedBox(height: 6),
                // Bio — max 3 lines, constrained width
                Text(
                  artist.bio,
                  textAlign: TextAlign.center,
                  maxLines: 3,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontSize: 12, color: Colors.white60),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

// ---------------------------------------------------------------------------
// Track tile
// ---------------------------------------------------------------------------

class _ArtistTrackTile extends ConsumerWidget {
  final SongModel song;
  final int index;
  const _ArtistTrackTile({required this.song, required this.index});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ListTile(
      contentPadding:
          const EdgeInsets.symmetric(horizontal: 20, vertical: 2),
      leading: SizedBox(
        width: 40,
        child: Center(
          child: Text('${index + 1}',
              style: const TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w600,
                  color: AppColors.onSurfaceMuted)),
        ),
      ),
      title: Text(song.title,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(
              fontWeight: FontWeight.w600,
              fontSize: 14,
              color: AppColors.onSurface)),
      subtitle: Text(song.album ?? '',
          style: const TextStyle(
              fontSize: 12, color: AppColors.onSurfaceMuted)),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          DownloadIconButton(song: song),
          const SizedBox(width: 8),
          const Icon(Icons.play_circle_outline_rounded,
              color: AppColors.primary, size: 30),
        ],
      ),
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
// Album card (for the horizontal row)
// ---------------------------------------------------------------------------

class _AlbumCard extends StatelessWidget {
  final MockAlbum album;
  const _AlbumCard({required this.album});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => context.go('/album/${album.id}'),
      child: Padding(
        padding: const EdgeInsets.only(right: 14),
        child: SizedBox(
          width: 128,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: AspectRatio(
                  aspectRatio: 1,
                  child: SongThumbnail(
                    url: album.imageUrl,
                    fallback: Container(
                      color: AppColors.surfaceTwo,
                      child: const Icon(Icons.album_rounded,
                          color: AppColors.primary, size: 32),
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 8),
              Text(album.title,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      color: AppColors.onSurface)),
              Text('${album.releaseYear}',
                  style: const TextStyle(
                      fontSize: 11, color: AppColors.onSurfaceMuted)),
            ],
          ),
        ),
      ),
    );
  }
}
