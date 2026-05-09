import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/api_adapter/mock_data.dart';
import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/theme/app_colors.dart';
import '../controllers/media_controller.dart';
import '../video/video_player_controller.dart';

class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return CustomScrollView(
      physics: const BouncingScrollPhysics(),
      slivers: [
        // Header
        const SliverToBoxAdapter(child: _HomeHeader()),
        // Hero Slider — Featured Music
        SliverToBoxAdapter(
          child: _SectionRow('Featured',
              onSeeAll: () => context.go('/music')),
        ),
        const SliverToBoxAdapter(child: _HeroSlider()),
        const SliverToBoxAdapter(child: SizedBox(height: 24)),
        // Video Banner
        SliverToBoxAdapter(
          child: _SectionRow('Videos',
              onSeeAll: () => context.go('/video')),
        ),
        const SliverToBoxAdapter(child: _VideoBanner()),
        const SliverToBoxAdapter(child: SizedBox(height: 24)),
        // Trending
        SliverToBoxAdapter(
          child: _SectionRow('Trending Now',
              onSeeAll: () => context.go('/music')),
        ),
        const SliverToBoxAdapter(child: _TrendingScroll()),
        const SliverToBoxAdapter(child: SizedBox(height: 24)),
        // Recent Releases — limited to 4 with "See All" footer
        SliverToBoxAdapter(
          child: _SectionRow('Recent Releases',
              onSeeAll: () => context.go('/music')),
        ),
        SliverList.separated(
          itemCount: kMockSongs.length.clamp(0, 4),
          separatorBuilder: (_, _) =>
              const Divider(height: 1, color: AppColors.glassBorder),
          itemBuilder: (context, i) =>
              _RecentTile(song: kMockSongs[i], index: i),
        ),
        SliverToBoxAdapter(
          child: TextButton(
            onPressed: () => context.go('/music'),
            child: const Text('View All in Music →',
                style: TextStyle(
                    color: AppColors.primary,
                    fontSize: 13,
                    fontWeight: FontWeight.w600)),
          ),
        ),
        const SliverToBoxAdapter(child: SizedBox(height: 100)),
      ],
    );
  }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------

class _HomeHeader extends StatelessWidget {
  const _HomeHeader();
  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 56, 20, 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Good Evening',
                  style: TextStyle(
                      fontSize: 13,
                      color: AppColors.onSurfaceMuted,
                      letterSpacing: 0.5)),
              const Text('Discover Music',
                  style: TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.w800,
                      color: AppColors.onSurface,
                      letterSpacing: -0.5)),
            ],
          ),
          // Profile avatar — mirrors the MainShell floating avatar; tap goes to /profile
          const SizedBox(width: 38, height: 38), // spacer so layout matches avatar
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Section row — label + "See All →" button
// ---------------------------------------------------------------------------

class _SectionRow extends StatelessWidget {
  final String text;
  final VoidCallback onSeeAll;
  const _SectionRow(this.text, {required this.onSeeAll});

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.fromLTRB(20, 0, 12, 12),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(text,
                style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w700,
                    color: AppColors.onSurface,
                    letterSpacing: -0.3)),
            TextButton(
              onPressed: onSeeAll,
              style: TextButton.styleFrom(
                padding: const EdgeInsets.symmetric(horizontal: 8),
                minimumSize: Size.zero,
                tapTargetSize: MaterialTapTargetSize.shrinkWrap,
              ),
              child: const Text('See All',
                  style: TextStyle(
                      fontSize: 13,
                      color: AppColors.primary,
                      fontWeight: FontWeight.w600)),
            ),
          ],
        ),
      );
}

// ---------------------------------------------------------------------------
// Hero Slider
// ---------------------------------------------------------------------------

class _HeroSlider extends ConsumerStatefulWidget {
  const _HeroSlider();
  @override
  ConsumerState<_HeroSlider> createState() => _HeroSliderState();
}

class _HeroSliderState extends ConsumerState<_HeroSlider> {
  final _controller = PageController(viewportFraction: 0.88);
  int _page = 0;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        SizedBox(
          height: 200,
          child: PageView.builder(
            controller: _controller,
            itemCount: kFeaturedSongs.length,
            onPageChanged: (i) => setState(() => _page = i),
            itemBuilder: (_, i) =>
                _HeroCard(song: kFeaturedSongs[i], active: i == _page),
          ),
        ),
        const SizedBox(height: 12),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: List.generate(kFeaturedSongs.length, (i) {
            final active = i == _page;
            return AnimatedContainer(
              duration: const Duration(milliseconds: 250),
              margin: const EdgeInsets.symmetric(horizontal: 3),
              width: active ? 20 : 6,
              height: 6,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(3),
                color: active
                    ? AppColors.primary
                    : AppColors.onSurfaceMuted.withValues(alpha: 0.4),
              ),
            );
          }),
        ),
      ],
    );
  }
}

class _HeroCard extends ConsumerWidget {
  final SongModel song;
  final bool active;
  const _HeroCard({required this.song, required this.active});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return AnimatedScale(
      scale: active ? 1.0 : 0.93,
      duration: const Duration(milliseconds: 300),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 6),
        child: GestureDetector(
          onTap: () => ref.read(mediaControllerProvider.notifier).play(
                song.audioUrl,
                title: song.title,
                artist: song.artist,
                artworkUrl: song.thumbnail,
              ),
          child: ClipRRect(
            borderRadius: BorderRadius.circular(20),
            child: Stack(
              fit: StackFit.expand,
              children: [
                _SongImage(url: song.thumbnail),
                // Gradient overlay
                Container(
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topCenter,
                      end: Alignment.bottomCenter,
                      colors: [
                        Colors.transparent,
                        Colors.black.withValues(alpha: 0.8),
                      ],
                    ),
                  ),
                ),
                // Text overlay
                Positioned(
                  bottom: 16,
                  left: 16,
                  right: 16,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(song.title,
                          style: const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.w800,
                              color: Colors.white)),
                      Text(song.artist,
                          style: const TextStyle(
                              fontSize: 13, color: AppColors.onSurfaceMuted)),
                    ],
                  ),
                ),
                // Play icon
                Positioned(
                  top: 12,
                  right: 12,
                  child: Container(
                    width: 36,
                    height: 36,
                    decoration: BoxDecoration(
                      color: AppColors.primary.withValues(alpha: 0.2),
                      shape: BoxShape.circle,
                      border: Border.all(color: AppColors.primary.withValues(alpha: 0.6)),
                    ),
                    child: const Icon(Icons.play_arrow,
                        color: AppColors.primary, size: 20),
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

// ---------------------------------------------------------------------------
// Trending horizontal scroll
// ---------------------------------------------------------------------------

class _TrendingScroll extends ConsumerWidget {
  const _TrendingScroll();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return SizedBox(
      height: 160,
      child: ListView.separated(
        padding: const EdgeInsets.symmetric(horizontal: 20),
        scrollDirection: Axis.horizontal,
        physics: const BouncingScrollPhysics(),
        itemCount: kTrendingSongs.length,
        separatorBuilder: (_, _) => const SizedBox(width: 12),
        itemBuilder: (_, i) => _TrendingCard(song: kTrendingSongs[i]),
      ),
    );
  }
}

class _TrendingCard extends ConsumerWidget {
  final SongModel song;
  const _TrendingCard({required this.song});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return GestureDetector(
      onTap: () => ref.read(mediaControllerProvider.notifier).play(
            song.audioUrl,
            title: song.title,
            artist: song.artist,
            artworkUrl: song.thumbnail,
          ),
      child: SizedBox(
        width: 120,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(14),
              child: SizedBox(
                width: 120,
                height: 120,
                child: _SongImage(url: song.thumbnail),
              ),
            ),
            const SizedBox(height: 6),
            Text(song.title,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    color: AppColors.onSurface)),
            Text(song.artist,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                    fontSize: 11, color: AppColors.onSurfaceMuted)),
          ],
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Recent Releases tile
// ---------------------------------------------------------------------------

class _RecentTile extends ConsumerWidget {
  final SongModel song;
  final int index;
  const _RecentTile({required this.song, required this.index});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final mins = song.duration ~/ 60;
    final secs = (song.duration % 60).toString().padLeft(2, '0');
    return ListTile(
      contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
      leading: ClipRRect(
        borderRadius: BorderRadius.circular(10),
        child: SizedBox(
            width: 48, height: 48, child: _SongImage(url: song.thumbnail)),
      ),
      title: Text(song.title,
          style: const TextStyle(
              fontWeight: FontWeight.w600,
              fontSize: 14,
              color: AppColors.onSurface)),
      subtitle: Text(song.artist,
          style:
              const TextStyle(fontSize: 12, color: AppColors.onSurfaceMuted)),
      trailing: Text('$mins:$secs',
          style:
              const TextStyle(fontSize: 12, color: AppColors.onSurfaceMuted)),
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
// Video Banner — demo video card
// ---------------------------------------------------------------------------

const _kDemoVideo = (
  url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
  title: 'For Bigger Blazes',
  artist: 'Google Samples',
  thumb: 'https://storage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg',
);

class _VideoBanner extends ConsumerWidget {
  const _VideoBanner();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: GestureDetector(
        onTap: () => ref.read(videoPlayerControllerProvider.notifier).playVideo(
              _kDemoVideo.url,
              title: _kDemoVideo.title,
              artist: _kDemoVideo.artist,
              artworkUrl: _kDemoVideo.thumb,
            ),
        child: ClipRRect(
          borderRadius: BorderRadius.circular(16),
          child: Stack(
            children: [
              // Thumbnail
              SizedBox(
                height: 160,
                width: double.infinity,
                child: Image.network(
                  _kDemoVideo.thumb,
                  fit: BoxFit.cover,
                  errorBuilder: (_, _, _) => Container(
                    color: AppColors.surfaceTwo,
                    child: const Icon(Icons.videocam,
                        size: 48, color: AppColors.onSurfaceMuted),
                  ),
                ),
              ),
              // Gradient
              Positioned.fill(
                child: Container(
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topCenter,
                      end: Alignment.bottomCenter,
                      colors: [
                        Colors.transparent,
                        Colors.black.withValues(alpha: 0.75),
                      ],
                    ),
                  ),
                ),
              ),
              // Play button
              const Center(
                child: CircleAvatar(
                  radius: 28,
                  backgroundColor: Colors.white24,
                  child: Icon(Icons.play_arrow_rounded,
                      color: Colors.white, size: 36),
                ),
              ),
              // Labels
              Positioned(
                bottom: 12,
                left: 14,
                right: 14,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      _kDemoVideo.title,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 15,
                        fontWeight: FontWeight.w700,
                        letterSpacing: -0.2,
                      ),
                    ),
                    Text(
                      _kDemoVideo.artist,
                      style: const TextStyle(
                          color: Colors.white70, fontSize: 12),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Shared thumbnail widget with network fallback
// ---------------------------------------------------------------------------

class _SongImage extends StatelessWidget {
  final String url;
  const _SongImage({required this.url});

  @override
  Widget build(BuildContext context) {
    return Image.network(
      url,
      fit: BoxFit.cover,
      errorBuilder: (_, _, _) => Container(
        color: AppColors.surfaceTwo,
        child:
            const Icon(Icons.music_note, color: AppColors.onSurfaceMuted),
      ),
    );
  }
}
