import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/api_adapter/home_repository.dart';
import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/song_play_count_row.dart';
import '../../../core/widgets/song_thumbnail.dart';
import '../controllers/home_controller.dart';
import '../controllers/media_controller.dart';
import '../video/video_player_controller.dart';
import '../../../core/providers/auth_provider.dart';

class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final homeData = ref.watch(homeDataProvider);

    return homeData.when(
      loading: () => const Center(
          child: CircularProgressIndicator(color: AppColors.primary)),
      error: (err, stack) => Center(
          child: Text('Error loading data: $err',
              style: const TextStyle(color: Colors.white70))),
      data: (data) {
        final trending = data['trending'] as List<SongModel>;
        final recent = data['recent'] as List<SongModel>;
        final banners = data['banners'] as List<dynamic>;
        final albums = data['albums'] as List<dynamic>;
        final artists = data['artists'] as List<dynamic>;

        // Dynamic sections list — in a real app, this list could come from the API
        final sections = data['sections'] as List<dynamic>? ?? [
          {'type': 'trending', 'title': 'Trending Now'},
          {'type': 'playlists', 'title': 'Popular Playlists'},
          {'type': 'albums', 'title': 'Top Albums'},
          {'type': 'artists', 'title': 'Featured Artists'},
          {'type': 'mood_relax', 'title': 'Mood: Relax'},
          {'type': 'recent', 'title': 'Recent Releases'},
          {'type': 'weekly_charts', 'title': 'Weekly Top 20'},
          {'type': 'global_hits', 'title': 'Global Hits'},
          {'type': 'saimum_specials', 'title': 'Saimum Specials'},
          {'type': 'video_highlights', 'title': 'Video Highlights'},
          {'type': 'nasheed', 'title': 'Nasheed Specials'},
          {'type': 'old_is_gold', 'title': 'Old is Gold'},
          {'type': 'editors_choice', 'title': 'Editor\'s Choice'},
        ];

        return CustomScrollView(
          physics: const BouncingScrollPhysics(),
          slivers: [
            SliverAppBar(
              pinned: true,
              backgroundColor: AppColors.background,
              toolbarHeight: 0.1,
              elevation: 0,
            ),
            const SliverToBoxAdapter(child: _HomeHeader()),
            SliverToBoxAdapter(child: _HeroSlider(banners: banners)),
            const SliverToBoxAdapter(child: SizedBox(height: 16)),

            for (final section in sections) ...[
              if (section['type'] == 'trending') ...[
                SliverToBoxAdapter(
                  child: _SectionRow(section['title'],
                      onSeeAll: () => context.push('/all-songs')),
                ),
                SliverToBoxAdapter(child: _TrendingScroll(songs: trending.take(10).toList())),
                const SliverToBoxAdapter(child: SizedBox(height: 16)),
              ] else if (section['type'] == 'albums') ...[
                SliverToBoxAdapter(
                  child: _SectionRow(section['title'],
                      onSeeAll: () => context.push('/all-albums')),
                ),
                SliverToBoxAdapter(child: _AlbumScroll(albums: albums.take(10).toList())),
                const SliverToBoxAdapter(child: SizedBox(height: 16)),
              ] else if (section['type'] == 'artists') ...[
                SliverToBoxAdapter(
                  child: _SectionRow(section['title'],
                      onSeeAll: () => context.push('/all-artists')),
                ),
                SliverToBoxAdapter(child: _ArtistScroll(artists: artists.take(10).toList())),
                const SliverToBoxAdapter(child: SizedBox(height: 16)),
              ] else if (section['type'] == 'recent') ...[
                SliverToBoxAdapter(
                  child: _SectionRow(section['title'],
                      onSeeAll: () => context.push('/all-songs')),
                ),
                SliverList.separated(
                  itemCount: recent.take(10).length,
                  separatorBuilder: (_, _) =>
                      const Divider(height: 1, color: AppColors.glassBorder),
                  itemBuilder: (context, i) =>
                      _RecentTile(song: recent[i], index: i),
                ),
                const SliverToBoxAdapter(child: SizedBox(height: 16)),
              ] else ...[
                // Placeholder for other 10+ dynamic sections
                // In a real app, these would fetch their own data or use nested list data
                SliverToBoxAdapter(
                  child: _SectionRow(section['title'],
                      onSeeAll: () => {}),
                ),
                SliverToBoxAdapter(
                  child: _GenericSectionScroll(
                    // Reusing trending or recent as placeholder data
                    songs: section['type'].toString().contains('video') ? [] : trending.reversed.take(8).toList(),
                    type: section['type'],
                  ),
                ),
                const SliverToBoxAdapter(child: SizedBox(height: 16)),
              ]
            ],

            const SliverToBoxAdapter(child: SizedBox(height: 100)),
          ],
        );
      },
    );
  }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------

class _HomeHeader extends ConsumerWidget {
  const _HomeHeader();
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(authProvider).user;
    final name = user?.name ?? 'User';

    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
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
              Text('Discover, $name',
                  style: const TextStyle(
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
  final List<dynamic> banners;
  const _HeroSlider({required this.banners});
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
            itemCount: widget.banners.length,
            onPageChanged: (i) => setState(() => _page = i),
            itemBuilder: (_, i) =>
                _HeroCard(banner: widget.banners[i], active: i == _page),
          ),
        ),
        const SizedBox(height: 12),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: List.generate(widget.banners.length, (i) {
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
  final dynamic banner;
  final bool active;
  const _HeroCard({required this.banner, required this.active});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final title = banner['title'] ?? '';
    final info = banner['info'] ?? '';
    final image = banner['image'] ?? '';

    return AnimatedScale(
      scale: active ? 1.0 : 0.93,
      duration: const Duration(milliseconds: 300),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 6),
        child: GestureDetector(
          onTap: () async {
            final bid = banner['id'];
            if (bid != null) {
              try {
                final details = await ref.read(homeRepositoryProvider).getBannerDetails(bid);
                final tracks = details['tracks'] as List<SongModel>;
                if (tracks.isNotEmpty) {
                  ref.read(mediaControllerProvider.notifier).play(
                        tracks.first.audioUrl,
                        title: tracks.first.title,
                        artist: tracks.first.artist,
                        artworkUrl: tracks.first.thumbnail,
                        songId: tracks.first.id,
                      );
                }
              } catch (e) {
                context.go('/music');
              }
            }
          },
          child: ClipRRect(
            borderRadius: BorderRadius.circular(20),
            child: Stack(
              fit: StackFit.expand,
              children: [
                _SongImage(url: image),
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
                      Text(title,
                          style: const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.w800,
                              color: Colors.white)),
                      if (info.isNotEmpty)
                        Text(info,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
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
  final List<SongModel> songs;
  const _TrendingScroll({required this.songs});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return SizedBox(
      height: 175,
      child: ListView.separated(
        padding: const EdgeInsets.symmetric(horizontal: 20),
        scrollDirection: Axis.horizontal,
        physics: const BouncingScrollPhysics(),
        itemCount: songs.length,
        separatorBuilder: (_, _) => const SizedBox(width: 12),
        itemBuilder: (_, i) => _TrendingCard(song: songs[i]),
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
            songId: song.id,
          ),
      child: SizedBox(
        width: 120,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(14),
              child: SizedBox(
                width: 120,
                height: 112,
                child: _SongImage(url: song.thumbnail),
              ),
            ),
            const SizedBox(height: 5),
            Text(song.title,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    height: 1.2,
                    color: AppColors.onSurface)),
            Text(song.artist,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                    fontSize: 11,
                    height: 1.2,
                    color: AppColors.onSurfaceMuted)),
            const SizedBox(height: 3),
            SongPlayCountRow(totalViews: song.totalViews, iconSize: 12, fontSize: 11),
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
      subtitle: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(song.artist,
              style: const TextStyle(
                  fontSize: 12, color: AppColors.onSurfaceMuted)),
          const SizedBox(height: 4),
          SongPlayCountRow(totalViews: song.totalViews, iconSize: 13, fontSize: 12),
        ],
      ),
      trailing: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text('$mins:$secs',
              style:
                  const TextStyle(fontSize: 12, color: AppColors.onSurfaceMuted)),
          const SizedBox(width: 8),
          IconButton(
            icon: const Icon(Icons.download_for_offline_outlined,
                size: 20, color: AppColors.onSurfaceMuted),
            onPressed: () {
              // TODO: Implement download logic
            },
          ),
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
// Albums horizontal scroll
// ---------------------------------------------------------------------------

class _AlbumScroll extends StatelessWidget {
  final List<dynamic> albums;
  const _AlbumScroll({required this.albums});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 136,
      child: ListView.separated(
        padding: const EdgeInsets.symmetric(horizontal: 20),
        scrollDirection: Axis.horizontal,
        physics: const BouncingScrollPhysics(),
        itemCount: albums.length,
        separatorBuilder: (_, _) => const SizedBox(width: 12),
        itemBuilder: (_, i) => _GenericCard(
          title: albums[i]['name'],
          imageUrl: albums[i]['image'],
          onTap: () => context.go('/album/${albums[i]['id']}'),
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Artists horizontal scroll
// ---------------------------------------------------------------------------

class _ArtistScroll extends StatelessWidget {
  final List<dynamic> artists;
  const _ArtistScroll({required this.artists});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 136,
      child: ListView.separated(
        padding: const EdgeInsets.symmetric(horizontal: 20),
        scrollDirection: Axis.horizontal,
        physics: const BouncingScrollPhysics(),
        itemCount: artists.length,
        separatorBuilder: (_, _) => const SizedBox(width: 12),
        itemBuilder: (_, i) => _GenericCard(
          title: artists[i]['name'],
          imageUrl: artists[i]['image'],
          isRound: true,
          onTap: () => context.go('/artist/${artists[i]['id']}'),
        ),
      ),
    );
  }
}

class _GenericCard extends StatelessWidget {
  final String title;
  final String imageUrl;
  final bool isRound;
  final VoidCallback? onTap;
  const _GenericCard({
    required this.title,
    required this.imageUrl,
    this.isRound = false,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: SizedBox(
        width: 100,
        child: Column(
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(isRound ? 50 : 12),
              child: SizedBox(
                width: 100,
                height: 100,
                child: _SongImage(url: imageUrl),
              ),
            ),
            const SizedBox(height: 6),
            Text(
              title,
              maxLines: 1,
              textAlign: TextAlign.center,
              style: const TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w600,
                color: AppColors.onSurface,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Shared thumbnail widget — delegates to global SongThumbnail (cached)
// ---------------------------------------------------------------------------

class _SongImage extends StatelessWidget {
  final String url;
  const _SongImage({required this.url});

  @override
  Widget build(BuildContext context) => SongThumbnail(url: url);
}

// ---------------------------------------------------------------------------
// Generic dynamic section scroll — can handle songs, playlists, or moods
// ---------------------------------------------------------------------------

class _GenericSectionScroll extends StatelessWidget {
  final List<SongModel> songs;
  final String type;
  const _GenericSectionScroll({required this.songs, required this.type});

  @override
  Widget build(BuildContext context) {
    if (songs.isEmpty && !type.toString().contains('video')) {
      return const SizedBox.shrink();
    }

    // Different card styles based on type
    final isSquare = type == 'playlists' || type == 'albums' || type == 'editors_choice';
    final isRound = type == 'artists';

    return SizedBox(
      height: isSquare ? 136 : 175,
      child: ListView.separated(
        padding: const EdgeInsets.symmetric(horizontal: 20),
        scrollDirection: Axis.horizontal,
        physics: const BouncingScrollPhysics(),
        itemCount: songs.isEmpty ? 5 : songs.length, // Mock items if empty
        separatorBuilder: (_, _) => const SizedBox(width: 12),
        itemBuilder: (_, i) {
          if (songs.isEmpty) {
            // Skeleton / Placeholder UI
            return _GenericCard(
              title: 'Loading...',
              imageUrl: '',
              isRound: isRound,
            );
          }
          final song = songs[i];
          if (isSquare) {
            return _GenericCard(
              title: song.title,
              imageUrl: song.thumbnail,
              onTap: () => context.push('/music'),
            );
          }
          return _TrendingCard(song: song);
        },
      ),
    );
  }
}
