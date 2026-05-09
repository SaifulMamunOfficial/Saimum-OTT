import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api_adapter/mock_data.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/song_thumbnail.dart';
import '../video/video_player_controller.dart';

// ---------------------------------------------------------------------------
// Page
// ---------------------------------------------------------------------------

class VideoListPage extends ConsumerWidget {
  const VideoListPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final featured = kMockVideos.first;
    final trending = kMockVideos;
    final popular = kMockVideos.reversed.toList();

    return CustomScrollView(
      physics: const BouncingScrollPhysics(),
      slivers: [
        // ── Featured Banner ──────────────────────────────────────────────────
        SliverToBoxAdapter(
          child: _FeaturedBanner(video: featured),
        ),

        // ── Trending Now ─────────────────────────────────────────────────────
        const SliverToBoxAdapter(child: SizedBox(height: 28)),
        const SliverToBoxAdapter(child: _OttSectionLabel('Trending Now')),
        SliverToBoxAdapter(child: _OttRow(videos: trending)),

        // ── Popular Videos ───────────────────────────────────────────────────
        const SliverToBoxAdapter(child: SizedBox(height: 28)),
        const SliverToBoxAdapter(child: _OttSectionLabel('Popular Videos')),
        SliverToBoxAdapter(child: _OttRow(videos: popular)),

        // ── By Category ─────────────────────────────────────────────────────
        const SliverToBoxAdapter(child: SizedBox(height: 28)),
        const SliverToBoxAdapter(child: _OttSectionLabel('By Category')),
        SliverToBoxAdapter(
          child: _CategoryBadges(),
        ),

        const SliverToBoxAdapter(child: SizedBox(height: 120)),
      ],
    );
  }
}

// ---------------------------------------------------------------------------
// OTT section label
// ---------------------------------------------------------------------------

class _OttSectionLabel extends StatelessWidget {
  final String text;
  const _OttSectionLabel(this.text);

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
// Featured Banner — large 16:9 hero with gradient + CTA
// ---------------------------------------------------------------------------

class _FeaturedBanner extends ConsumerWidget {
  final MockVideo video;
  const _FeaturedBanner({required this.video});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return GestureDetector(
      onTap: () => ref.read(videoPlayerControllerProvider.notifier).playVideo(
            video.url,
            title: video.title,
            artist: video.artist,
            artworkUrl: video.thumb,
          ),
      child: SizedBox(
        width: double.infinity,
        child: Stack(
          children: [
            // Thumbnail fills to top (under status bar)
            AspectRatio(
              aspectRatio: 16 / 9,
              child: SongThumbnail(
                url: video.thumb,
                fallback: Container(
                  color: AppColors.surfaceTwo,
                  child: const Icon(Icons.videocam,
                      size: 56, color: AppColors.onSurfaceMuted),
                ),
              ),
            ),

            // Top scrim (status bar legibility)
            Positioned(
              top: 0,
              left: 0,
              right: 0,
              height: 80,
              child: DecoratedBox(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      AppColors.background.withValues(alpha: 0.75),
                      Colors.transparent,
                    ],
                  ),
                ),
              ),
            ),

            // Safe area for header text
            Positioned(
              top: MediaQuery.paddingOf(context).top + 12,
              left: 20,
              child: const Text('Videos',
                  style: TextStyle(
                      fontSize: 30,
                      fontWeight: FontWeight.w800,
                      color: AppColors.onSurface,
                      letterSpacing: -0.7)),
            ),

            // Bottom gradient + info
            Positioned(
              bottom: 0,
              left: 0,
              right: 0,
              child: ClipRect(
                child: BackdropFilter(
                  filter: ImageFilter.blur(sigmaX: 0, sigmaY: 0),
                  child: Container(
                    padding:
                        const EdgeInsets.fromLTRB(16, 32, 16, 18),
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topCenter,
                        end: Alignment.bottomCenter,
                        colors: [
                          Colors.transparent,
                          Colors.black.withValues(alpha: 0.92),
                        ],
                      ),
                    ),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Container(
                                padding: const EdgeInsets.symmetric(
                                    horizontal: 8, vertical: 3),
                                decoration: BoxDecoration(
                                  color: AppColors.primary
                                      .withValues(alpha: 0.2),
                                  borderRadius: BorderRadius.circular(4),
                                  border: Border.all(
                                      color: AppColors.primary
                                          .withValues(alpha: 0.5),
                                      width: 1),
                                ),
                                child: const Text('FEATURED',
                                    style: TextStyle(
                                        fontSize: 9,
                                        fontWeight: FontWeight.w800,
                                        color: AppColors.primary,
                                        letterSpacing: 1.2)),
                              ),
                              const SizedBox(height: 6),
                              Text(video.title,
                                  maxLines: 2,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(
                                      fontSize: 20,
                                      fontWeight: FontWeight.w800,
                                      color: Colors.white,
                                      letterSpacing: -0.4)),
                              const SizedBox(height: 2),
                              Text(video.artist,
                                  style: const TextStyle(
                                      fontSize: 13,
                                      color: Colors.white70)),
                            ],
                          ),
                        ),
                        const SizedBox(width: 12),
                        FilledButton.icon(
                          onPressed: () => ref
                              .read(videoPlayerControllerProvider.notifier)
                              .playVideo(
                                video.url,
                                title: video.title,
                                artist: video.artist,
                                artworkUrl: video.thumb,
                              ),
                          icon: const Icon(Icons.play_arrow_rounded,
                              size: 18),
                          label: const Text('Watch Now',
                              style: TextStyle(
                                  fontWeight: FontWeight.w700,
                                  fontSize: 13)),
                          style: FilledButton.styleFrom(
                            backgroundColor: AppColors.primary,
                            foregroundColor: Colors.black,
                            padding: const EdgeInsets.symmetric(
                                horizontal: 14, vertical: 10),
                            shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(20)),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Horizontal OTT row
// ---------------------------------------------------------------------------

class _OttRow extends ConsumerWidget {
  final List<MockVideo> videos;
  const _OttRow({required this.videos});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return SizedBox(
      height: 148,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: videos.length,
        itemBuilder: (_, i) => _OttCard(video: videos[i]),
      ),
    );
  }
}

class _OttCard extends ConsumerWidget {
  final MockVideo video;
  const _OttCard({required this.video});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return GestureDetector(
      onTap: () => ref.read(videoPlayerControllerProvider.notifier).playVideo(
            video.url,
            title: video.title,
            artist: video.artist,
            artworkUrl: video.thumb,
          ),
      child: Padding(
        padding: const EdgeInsets.only(right: 12),
        child: SizedBox(
          width: 200,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(10),
                child: AspectRatio(
                  aspectRatio: 16 / 9,
                  child: Stack(
                    fit: StackFit.expand,
                    children: [
                      SongThumbnail(
                        url: video.thumb,
                        fallback: Container(
                          color: AppColors.surfaceTwo,
                          child: const Icon(Icons.videocam,
                              color: AppColors.onSurfaceMuted, size: 28),
                        ),
                      ),
                      Center(
                        child: Container(
                          width: 36,
                          height: 36,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: Colors.black.withValues(alpha: 0.5),
                          ),
                          child: const Icon(Icons.play_arrow_rounded,
                              color: Colors.white, size: 22),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 7),
              Text(video.title,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      color: AppColors.onSurface)),
              const SizedBox(height: 2),
              Text(video.artist,
                  style: const TextStyle(
                      fontSize: 11, color: AppColors.onSurfaceMuted)),
            ],
          ),
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Category badges
// ---------------------------------------------------------------------------

class _CategoryBadges extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    const cats = [
      (label: 'Short Films', icon: Icons.movie_rounded, color: AppColors.accent),
      (label: 'Animation', icon: Icons.animation_rounded, color: Color(0xFF4FC3F7)),
      (label: 'Nature', icon: Icons.park_rounded, color: Color(0xFF34C759)),
      (label: 'Music Videos', icon: Icons.music_video_rounded, color: AppColors.primary),
    ];
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Wrap(
        spacing: 10,
        runSpacing: 10,
        children: cats
            .map((c) => Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 14, vertical: 10),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(24),
                    color: c.color.withValues(alpha: 0.1),
                    border: Border.all(
                        color: c.color.withValues(alpha: 0.35), width: 1),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(c.icon, color: c.color, size: 16),
                      const SizedBox(width: 7),
                      Text(c.label,
                          style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.w600,
                              color: c.color)),
                    ],
                  ),
                ))
            .toList(),
      ),
    );
  }
}
