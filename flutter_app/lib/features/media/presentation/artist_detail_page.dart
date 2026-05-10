import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/song_play_count_row.dart';
import '../../../core/widgets/song_thumbnail.dart';
import '../controllers/media_controller.dart';
import '../shared/download_icon_button.dart';
import '../../../core/api_adapter/home_repository.dart';

final artistDetailsProvider = FutureProvider.family<Map<String, dynamic>, int>((ref, id) {
  return ref.watch(homeRepositoryProvider).getArtistDetails(id);
});

/// Artist name in hero: wraps to multiple lines; scales down uniformly if still
/// too tall for [maxHeight], so long names are not cut with ellipsis.
class _ResponsiveArtistHeroName extends StatelessWidget {
  final String name;
  const _ResponsiveArtistHeroName({required this.name});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 44,
      width: double.infinity,
      child: FittedBox(
        fit: BoxFit.scaleDown,
        child: Text(
          name,
          style: const TextStyle(
            fontSize: 34,
            fontWeight: FontWeight.w900,
            color: Colors.white,
            letterSpacing: -1.0,
          ),
        ),
      ),
    );
  }
}

class ArtistDetailPage extends ConsumerWidget {
  final int artistId;
  const ArtistDetailPage({super.key, required this.artistId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final artistAsync = ref.watch(artistDetailsProvider(artistId));

    return artistAsync.when(
      loading: () => const Scaffold(
          backgroundColor: AppColors.background,
          body: Center(child: CircularProgressIndicator())),
      error: (err, stack) => Scaffold(
          backgroundColor: AppColors.background,
          body: Center(child: Text('Error: $err', style: const TextStyle(color: Colors.white)))),
      data: (data) {
        final artist = data['artist'] as Map<String, dynamic>;
        final tracks = data['tracks'] as List<SongModel>;
        final albums = (data['albums'] as List<dynamic>?)
                ?.map((e) => Map<String, dynamic>.from(e as Map))
                .toList() ??
            [];

        return Scaffold(
          backgroundColor: AppColors.background,
          body: CustomScrollView(
            physics: const BouncingScrollPhysics(),
            slivers: [
              SliverAppBar(
                expandedHeight: 420,
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
                      SongThumbnail(url: artist['image'].toString()),
                      Container(
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            begin: Alignment.topCenter,
                            end: Alignment.bottomCenter,
                            stops: const [0.3, 0.7, 1.0],
                            colors: [
                              Colors.black.withValues(alpha: 0.1),
                              AppColors.background.withValues(alpha: 0.6),
                              AppColors.background,
                            ],
                          ),
                        ),
                      ),
                      Positioned(
                        bottom: 12,
                        left: 20,
                        right: 20,
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          crossAxisAlignment: CrossAxisAlignment.center,
                          children: [
                            const SizedBox(height: 12),
                            _ResponsiveArtistHeroName(name: artist['name'].toString()),
                            const SizedBox(height: 8),
                            // --- Short Bio Section (2 lines max) ---
                            Padding(
                              padding: const EdgeInsets.symmetric(horizontal: 24),
                              child: Text(
                                artist['bio']?.toString() ?? 'A legendary artist contributing to the soulful melodies of Saimum.',
                                textAlign: TextAlign.center,
                                maxLines: 2,
                                overflow: TextOverflow.ellipsis,
                                style: TextStyle(
                                  fontSize: 14,
                                  fontWeight: FontWeight.w400,
                                  color: Colors.white.withValues(alpha: 0.8),
                                  height: 1.4,
                                ),
                              ),
                            ),
                            const SizedBox(height: 12),
                            Text(
                              tracks.isEmpty && albums.isEmpty
                                  ? 'No music yet'
                                  : '${albums.length} ${albums.length == 1 ? 'album' : 'albums'} · ${tracks.length} ${tracks.length == 1 ? 'track' : 'tracks'}',
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                fontSize: 13,
                                fontWeight: FontWeight.w600,
                                color: AppColors.primary.withValues(alpha: 0.9),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),

              if (albums.isNotEmpty) ...[
                SliverToBoxAdapter(
                  child: Padding(
                    padding: const EdgeInsets.fromLTRB(20, 8, 0, 8),
                    child: Row(
                      children: [
                        const Text(
                          'Albums',
                          style: TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.w800,
                            color: AppColors.onSurface,
                            letterSpacing: -0.3,
                          ),
                        ),
                        const Spacer(),
                      ],
                    ),
                  ),
                ),
                SliverToBoxAdapter(
                  child: SizedBox(
                    height: 200,
                    child: ListView.separated(
                      padding: const EdgeInsets.fromLTRB(20, 0, 20, 8),
                      scrollDirection: Axis.horizontal,
                      itemCount: albums.length,
                      separatorBuilder: (_, _) => const SizedBox(width: 14),
                      itemBuilder: (_, i) => _ArtistAlbumCard(album: albums[i]),
                    ),
                  ),
                ),
              ],

              SliverToBoxAdapter(
                child: Padding(
                  padding: EdgeInsets.fromLTRB(20, albums.isEmpty ? 20 : 12, 20, 12),
                  child: Row(
                    children: [
                      const Text(
                        'Tracks',
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.w800,
                          color: AppColors.onSurface,
                          letterSpacing: -0.3,
                        ),
                      ),
                      const Spacer(),
                    ],
                  ),
                ),
              ),
              if (tracks.isEmpty)
                const SliverToBoxAdapter(
                  child: Padding(
                    padding: EdgeInsets.symmetric(horizontal: 32),
                    child: Text(
                      'No tracks found for this artist.',
                      textAlign: TextAlign.center,
                      style: TextStyle(color: AppColors.onSurfaceMuted, fontSize: 14),
                    ),
                  ),
                )
              else
                SliverPadding(
                  padding: const EdgeInsets.fromLTRB(16, 0, 16, 160),
                  sliver: SliverList(
                    delegate: SliverChildBuilderDelegate(
                      (context, i) => Padding(
                        padding: const EdgeInsets.only(bottom: 10),
                        child: _ArtistTrackTile(song: tracks[i], index: i),
                      ),
                      childCount: tracks.length,
                    ),
                  ),
                ),
            ],
          ),
        );
      },
    );
  }
}

class _ArtistAlbumCard extends StatelessWidget {
  final Map<String, dynamic> album;
  const _ArtistAlbumCard({required this.album});

  @override
  Widget build(BuildContext context) {
    final id = album['id'];
    final aid = id is int ? id : int.tryParse('$id') ?? 0;
    final name = album['name']?.toString() ?? '';
    final image = album['image']?.toString() ?? '';
    final artistLine = album['artist']?.toString() ?? '';

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: aid > 0 ? () => context.push('/album/$aid') : null,
        borderRadius: BorderRadius.circular(16),
        child: SizedBox(
          width: 140,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(16),
                  child: SongThumbnail(
                    url: image,
                    fallback: Container(
                      color: AppColors.surfaceTwo,
                      child: const Icon(
                        Icons.album_rounded,
                        color: AppColors.primary,
                        size: 36,
                      ),
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 10),
              Text(
                name,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.w700,
                  color: AppColors.onSurface,
                  height: 1.25,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                artistLine,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                  fontSize: 11,
                  color: AppColors.onSurfaceMuted,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ArtistTrackTile extends ConsumerWidget {
  final SongModel song;
  final int index;
  const _ArtistTrackTile({required this.song, required this.index});

  String _formatDuration(int seconds) {
    if (seconds <= 0) return '';
    final m = seconds ~/ 60;
    final s = seconds % 60;
    return '${m.toString()}:${s.toString().padLeft(2, '0')}';
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final durationLabel = _formatDuration(song.duration);
    final albumName = song.album?.trim();
    final secondaryParts = <String>[];
    if (albumName != null && albumName.isNotEmpty) secondaryParts.add(albumName);
    if (durationLabel.isNotEmpty) secondaryParts.add(durationLabel);

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: () => ref.read(mediaControllerProvider.notifier).play(
              song.audioUrl,
              title: song.title,
              artist: song.artist,
              artworkUrl: song.thumbnail,
              songId: song.id,
            ),
        borderRadius: BorderRadius.circular(16),
        child: Ink(
          decoration: BoxDecoration(
            color: AppColors.surfaceOne,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: AppColors.glassBorder.withValues(alpha: 0.12)),
          ),
          child: Padding(
            padding: const EdgeInsets.fromLTRB(10, 10, 8, 10),
            child: Row(
              children: [
                ClipRRect(
                  borderRadius: BorderRadius.circular(12),
                  child: SizedBox(
                    width: 56,
                    height: 56,
                    child: Stack(
                      fit: StackFit.expand,
                      children: [
                        SongThumbnail(
                          url: song.thumbnail,
                          fallback: Container(
                            color: AppColors.surfaceTwo,
                            child: const Icon(Icons.music_note_rounded,
                                color: AppColors.onSurfaceMuted, size: 26),
                          ),
                        ),
                        Positioned(
                          left: 6,
                          top: 6,
                          child: DecoratedBox(
                            decoration: BoxDecoration(
                              color: Colors.black.withValues(alpha: 0.55),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Padding(
                              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                              child: Text(
                                '${index + 1}',
                                style: const TextStyle(
                                  fontSize: 11,
                                  fontWeight: FontWeight.w700,
                                  color: Colors.white,
                                ),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        song.title,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          fontWeight: FontWeight.w700,
                          fontSize: 15,
                          color: AppColors.onSurface,
                          letterSpacing: -0.2,
                        ),
                      ),
                      if (secondaryParts.isNotEmpty) ...[
                        const SizedBox(height: 4),
                        Text(
                          secondaryParts.join(' · '),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                            fontSize: 12,
                            color: AppColors.onSurfaceMuted,
                          ),
                        ),
                      ],
                      const SizedBox(height: 6),
                      SongPlayCountRow(
                        totalViews: song.totalViews,
                        iconSize: 12,
                        fontSize: 11,
                      ),
                    ],
                  ),
                ),
                DownloadIconButton(song: song),
                IconButton(
                  style: IconButton.styleFrom(
                    foregroundColor: AppColors.primary,
                  ),
                  icon: const Icon(Icons.play_circle_filled_rounded, size: 40),
                  onPressed: () => ref.read(mediaControllerProvider.notifier).play(
                        song.audioUrl,
                        title: song.title,
                        artist: song.artist,
                        artworkUrl: song.thumbnail,
                        songId: song.id,
                      ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
