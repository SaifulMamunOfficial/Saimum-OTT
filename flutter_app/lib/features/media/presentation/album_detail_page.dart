import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/api_adapter/mock_data.dart';
import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/song_thumbnail.dart';
import '../controllers/media_controller.dart';
import '../shared/download_icon_button.dart';

class AlbumDetailPage extends ConsumerWidget {
  final int albumId;
  const AlbumDetailPage({super.key, required this.albumId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final album = albumById(albumId);
    if (album == null) {
      return Scaffold(
        backgroundColor: AppColors.background,
        appBar: AppBar(backgroundColor: AppColors.background),
        body: const Center(
          child: Text('Album not found',
              style: TextStyle(color: AppColors.onSurfaceMuted)),
        ),
      );
    }
    final tracks = songsForAlbum(album.title);

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
            flexibleSpace: FlexibleSpaceBar(
              title: Text(album.title,
                  style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                      color: AppColors.onSurface)),
              titlePadding:
                  const EdgeInsetsDirectional.only(start: 72, bottom: 16),
              background: _AlbumHeader(album: album, trackCount: tracks.length),
            ),
          ),

          // ── Play All button ──────────────────────────────────────────────
          if (tracks.isNotEmpty)
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(20, 20, 20, 4),
                child: Row(
                  children: [
                    FilledButton.icon(
                      onPressed: () =>
                          ref.read(mediaControllerProvider.notifier).play(
                                tracks.first.audioUrl,
                                title: tracks.first.title,
                                artist: tracks.first.artist,
                                artworkUrl: tracks.first.thumbnail,
                              ),
                      icon: const Icon(Icons.play_arrow_rounded, size: 20),
                      label: const Text('Play All',
                          style: TextStyle(
                              fontWeight: FontWeight.w700, fontSize: 14)),
                      style: FilledButton.styleFrom(
                        backgroundColor: AppColors.primary,
                        foregroundColor: Colors.black,
                        shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(24)),
                        padding: const EdgeInsets.symmetric(
                            horizontal: 24, vertical: 12),
                      ),
                    ),
                    const SizedBox(width: 12),
                    OutlinedButton.icon(
                      onPressed: () {
                        final artist = artistByName(album.artistName);
                        if (artist != null) {
                          context.go('/artist/${artist.id}');
                        }
                      },
                      icon: const Icon(Icons.person_outline_rounded,
                          size: 16, color: AppColors.primary),
                      label: Text(album.artistName,
                          style: const TextStyle(
                              color: AppColors.primary,
                              fontSize: 13,
                              fontWeight: FontWeight.w600)),
                      style: OutlinedButton.styleFrom(
                        side: BorderSide(
                            color: AppColors.primary.withValues(alpha: 0.5)),
                        shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(24)),
                        padding: const EdgeInsets.symmetric(
                            horizontal: 16, vertical: 10),
                      ),
                    ),
                  ],
                ),
              ),
            ),

          // ── Tracklist ────────────────────────────────────────────────────
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
              child: Text(
                tracks.isEmpty
                    ? 'No tracks found'
                    : '${tracks.length} Track${tracks.length == 1 ? '' : 's'}',
                style: const TextStyle(
                    fontSize: 13, color: AppColors.onSurfaceMuted),
              ),
            ),
          ),
          SliverList.separated(
            itemCount: tracks.length,
            separatorBuilder: (_, _) =>
                const Divider(height: 1, color: AppColors.glassBorder),
            itemBuilder: (_, i) =>
                _TrackTile(song: tracks[i], index: i),
          ),

          const SliverToBoxAdapter(child: SizedBox(height: 120)),
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Album header — cover art + gradient + metadata
// ---------------------------------------------------------------------------

class _AlbumHeader extends StatelessWidget {
  final MockAlbum album;
  final int trackCount;
  const _AlbumHeader({required this.album, required this.trackCount});

  @override
  Widget build(BuildContext context) {
    return Stack(
      fit: StackFit.expand,
      children: [
        // Album art
        SongThumbnail(
          url: album.imageUrl,
          fallback: Container(color: AppColors.surfaceTwo),
        ),

        // Gradient overlay
        Container(
          decoration: BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
              colors: [
                Colors.transparent,
                AppColors.background.withValues(alpha: 0.9),
              ],
              stops: const [0.4, 1.0],
            ),
          ),
        ),

        // Metadata at bottom
        Positioned(
          bottom: 20,
          left: 20,
          right: 20,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: AppColors.accent.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(4),
                  border: Border.all(
                      color: AppColors.accent.withValues(alpha: 0.5),
                      width: 1),
                ),
                child: const Text('ALBUM',
                    style: TextStyle(
                        fontSize: 9,
                        fontWeight: FontWeight.w800,
                        color: AppColors.accent,
                        letterSpacing: 1.2)),
              ),
              const SizedBox(height: 8),
              Text(album.title,
                  style: const TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.w800,
                      color: Colors.white,
                      letterSpacing: -0.5)),
              const SizedBox(height: 4),
              Text(
                '${album.artistName}  ·  ${album.releaseYear}  ·  $trackCount tracks',
                style: const TextStyle(fontSize: 13, color: Colors.white70),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

// ---------------------------------------------------------------------------
// Track tile
// ---------------------------------------------------------------------------

class _TrackTile extends ConsumerWidget {
  final SongModel song;
  final int index;
  const _TrackTile({required this.song, required this.index});

  String _fmt(int secs) {
    final m = (secs ~/ 60).toString().padLeft(2, '0');
    final s = (secs % 60).toString().padLeft(2, '0');
    return '$m:$s';
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ListTile(
      contentPadding:
          const EdgeInsets.symmetric(horizontal: 20, vertical: 2),
      leading: SizedBox(
        width: 32,
        child: Center(
          child: Text('${index + 1}',
              style: const TextStyle(
                  fontSize: 14,
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
      subtitle: Text(song.artist,
          style: const TextStyle(
              fontSize: 12, color: AppColors.onSurfaceMuted)),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          DownloadIconButton(song: song),
          const SizedBox(width: 12),
          Text(_fmt(song.duration),
              style: const TextStyle(
                  fontSize: 12, color: AppColors.onSurfaceMuted)),
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
