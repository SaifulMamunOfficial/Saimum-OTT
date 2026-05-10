import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/api_adapter/mock_data.dart';
import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/song_play_count_row.dart';
import '../../../core/widgets/song_thumbnail.dart';
import '../controllers/media_controller.dart';
import '../shared/download_icon_button.dart';
import '../../../core/api_adapter/home_repository.dart';

final albumDetailsProvider = FutureProvider.family<Map<String, dynamic>, int>((ref, id) {
  return ref.watch(homeRepositoryProvider).getAlbumDetails(id);
});

class AlbumDetailPage extends ConsumerWidget {
  final int albumId;
  const AlbumDetailPage({super.key, required this.albumId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final albumAsync = ref.watch(albumDetailsProvider(albumId));

    return albumAsync.when(
      loading: () => const Scaffold(
          backgroundColor: AppColors.background,
          body: Center(child: CircularProgressIndicator())),
      error: (err, stack) => Scaffold(
          backgroundColor: AppColors.background,
          body: Center(child: Text('Error: $err', style: const TextStyle(color: Colors.white)))),
      data: (data) {
        final album = data['album'];
        final tracks = data['tracks'] as List<SongModel>;

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
                      color: Colors.white, size: 20),
                  onPressed: () => context.pop(),
                ),
                flexibleSpace: FlexibleSpaceBar(
                  background: Stack(
                    fit: StackFit.expand,
                    children: [
                      SongThumbnail(url: album['image']),
                      Container(
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            begin: Alignment.topCenter,
                            end: Alignment.bottomCenter,
                            colors: [
                              Colors.transparent,
                              AppColors.background.withValues(alpha: 0.5),
                              AppColors.background,
                            ],
                          ),
                        ),
                      ),
                      Positioned(
                        bottom: 20,
                        left: 20,
                        right: 20,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Text(
                              album['name'],
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(
                                  fontSize: 24,
                                  fontWeight: FontWeight.w900,
                                  color: Colors.white,
                                  height: 1.1),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              album['artist'],
                              style: const TextStyle(
                                  fontSize: 15,
                                  fontWeight: FontWeight.w500,
                                  color: Colors.white70),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),

              // ── Play All button ──────────────────────────────────────────────
              if (tracks.isNotEmpty)
                SliverToBoxAdapter(
                  child: Padding(
                    padding: const EdgeInsets.fromLTRB(20, 24, 20, 4),
                    child: Row(
                      children: [
                        FilledButton.icon(
                          onPressed: () => ref
                              .read(mediaControllerProvider.notifier)
                              .play(
                                tracks.first.audioUrl,
                                title: tracks.first.title,
                                artist: tracks.first.artist,
                                artworkUrl: tracks.first.thumbnail,
                                songId: tracks.first.id,
                              ),
                          icon: const Icon(Icons.play_arrow_rounded, size: 22),
                          label: const Text('Play All'),
                          style: FilledButton.styleFrom(
                            backgroundColor: AppColors.primary,
                            foregroundColor: Colors.black,
                            padding: const EdgeInsets.symmetric(
                                horizontal: 24, vertical: 12),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),

              // ── Tracklist ────────────────────────────────────────────────────
              SliverList.separated(
                itemCount: tracks.length,
                separatorBuilder: (_, _) => const SizedBox(height: 8),
                itemBuilder: (_, i) => _TrackTile(song: tracks[i], index: i),
              ),

              const SliverToBoxAdapter(child: SizedBox(height: 120)),
            ],
          ),
        );
      },
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
      subtitle: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(song.artist,
              style: const TextStyle(
                  fontSize: 12, color: AppColors.onSurfaceMuted)),
          const SizedBox(height: 4),
          SongPlayCountRow(totalViews: song.totalViews, iconSize: 12, fontSize: 11),
        ],
      ),
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
            songId: song.id,
          ),
    );
  }
}
