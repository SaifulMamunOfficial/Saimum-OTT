import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:go_router/go_router.dart';

import '../../../core/api_adapter/mock_data.dart';
import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/glass_card.dart';
import '../../../core/widgets/song_thumbnail.dart';
import '../controllers/media_controller.dart';
import '../controllers/search_controller.dart';
import '../shared/download_icon_button.dart';
import '../video/video_player_controller.dart';

// ---------------------------------------------------------------------------
// Static suggestion data
// ---------------------------------------------------------------------------

const _genres = ['Electronic', 'Chill', 'Ambient', 'Lo-Fi'];

// ---------------------------------------------------------------------------
// Page
// ---------------------------------------------------------------------------

class SearchPage extends ConsumerStatefulWidget {
  const SearchPage({super.key});

  @override
  ConsumerState<SearchPage> createState() => _SearchPageState();
}

class _SearchPageState extends ConsumerState<SearchPage> {
  final _textCtrl = TextEditingController();

  @override
  void dispose() {
    _textCtrl.dispose();
    super.dispose();
  }

  void _onChanged(String value) =>
      ref.read(searchControllerProvider.notifier).search(value);

  void _clear() {
    _textCtrl.clear();
    ref.read(searchControllerProvider.notifier).clear();
  }

  void _setQuery(String value) {
    _textCtrl.text = value;
    _textCtrl.selection =
        TextSelection.collapsed(offset: value.length);
    ref.read(searchControllerProvider.notifier).search(value);
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(searchControllerProvider);

    return CustomScrollView(
      keyboardDismissBehavior: ScrollViewKeyboardDismissBehavior.onDrag,
      physics: const BouncingScrollPhysics(),
      slivers: [
        // Header
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 56, 20, 16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Search',
                  style: TextStyle(
                    fontSize: 28,
                    fontWeight: FontWeight.w800,
                    color: AppColors.onSurface,
                    letterSpacing: -0.5,
                  ),
                ),
                const SizedBox(height: 16),
                _SearchBar(
                  controller: _textCtrl,
                  onChanged: _onChanged,
                  onClear: _clear,
                  isSearching: state.isSearching,
                ),
              ],
            ),
          ),
        ),

        // ── Empty state: genre chips + popular tracks ──────────────────────
        if (state.isEmpty) ...[
          SliverToBoxAdapter(
            child: _GenreChips(
              genres: _genres,
              onTap: _setQuery,
            ),
          ),
          const SliverToBoxAdapter(
            child: _SectionLabel('Popular Artists'),
          ),
          SliverToBoxAdapter(
            child: _ArtistChips(
              artists:
                  kMockSongs.map((s) => s.artist).toSet().toList(),
              onTap: _setQuery,
            ),
          ),
          const SliverToBoxAdapter(
            child: _SectionLabel('Featured Videos'),
          ),
          const SliverToBoxAdapter(child: _VideoDiscoveryRow()),
          const SliverToBoxAdapter(child: SizedBox(height: 8)),
          const SliverToBoxAdapter(
            child: _SectionLabel('All Tracks'),
          ),
          SliverList.builder(
            itemCount: kMockSongs.length,
            itemBuilder: (_, i) =>
                _SongResultTile(song: kMockSongs[i]),
          ),
        ],

        // ── Searching (debounce in progress) ──────────────────────────────
        if (!state.isEmpty && state.isSearching)
          const SliverToBoxAdapter(
            child: Padding(
              padding: EdgeInsets.only(top: 40),
              child: Center(
                child: SizedBox(
                  width: 24,
                  height: 24,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: AppColors.primary,
                  ),
                ),
              ),
            ),
          ),

        // ── No results ────────────────────────────────────────────────────
        if (!state.isEmpty && !state.isSearching && !state.hasResults)
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(20, 48, 20, 0),
              child: Column(
                children: [
                  const Icon(Icons.search_off,
                      color: AppColors.onSurfaceMuted, size: 48),
                  const SizedBox(height: 16),
                  Text(
                    'No results for "${state.query}"',
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      color: AppColors.onSurfaceMuted,
                      fontSize: 15,
                    ),
                  ),
                ],
              ),
            ),
          ),

        // ── Results ───────────────────────────────────────────────────────
        if (!state.isEmpty && !state.isSearching && state.hasResults) ...[
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(20, 0, 20, 12),
              child: Text(
                '${state.results.length} result${state.results.length == 1 ? '' : 's'}',
                style: const TextStyle(
                  fontSize: 13,
                  color: AppColors.onSurfaceMuted,
                ),
              ),
            ),
          ),
          SliverList.builder(
            itemCount: state.results.length,
            itemBuilder: (_, i) =>
                _SongResultTile(song: state.results[i]),
          ),
        ],

        const SliverToBoxAdapter(child: SizedBox(height: 120)),
      ],
    );
  }
}

// ---------------------------------------------------------------------------
// Glass Search Bar
// ---------------------------------------------------------------------------

class _SearchBar extends StatelessWidget {
  final TextEditingController controller;
  final ValueChanged<String> onChanged;
  final VoidCallback onClear;
  final bool isSearching;

  const _SearchBar({
    required this.controller,
    required this.onChanged,
    required this.onClear,
    required this.isSearching,
  });

  @override
  Widget build(BuildContext context) {
    return GlassCard(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Row(
        children: [
          const Icon(Icons.search_rounded, color: AppColors.onSurfaceMuted),
          const SizedBox(width: 12),
          Expanded(
            child: TextField(
              controller: controller,
              onChanged: onChanged,
              autofocus: false,
              style: const TextStyle(
                color: AppColors.onSurface,
                fontSize: 15,
              ),
              decoration: const InputDecoration(
                hintText: 'Songs, artists, genres…',
                hintStyle: TextStyle(
                  color: AppColors.onSurfaceMuted,
                  fontSize: 15,
                ),
                border: InputBorder.none,
                isDense: true,
                contentPadding: EdgeInsets.symmetric(vertical: 12),
              ),
            ),
          ),
          const SizedBox(width: 8),
          if (isSearching)
            const SizedBox(
              width: 16,
              height: 16,
              child: CircularProgressIndicator(
                strokeWidth: 1.5,
                color: AppColors.primary,
              ),
            )
          else if (controller.text.isNotEmpty)
            GestureDetector(
              onTap: onClear,
              child: Container(
                width: 20,
                height: 20,
                decoration: BoxDecoration(
                  color: AppColors.onSurfaceMuted.withValues(alpha: 0.25),
                  shape: BoxShape.circle,
                ),
                child: const Icon(Icons.close,
                    size: 12, color: AppColors.onSurface),
              ),
            ),
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Genre chips
// ---------------------------------------------------------------------------

class _GenreChips extends StatelessWidget {
  final List<String> genres;
  final ValueChanged<String> onTap;
  const _GenreChips({required this.genres, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const _SectionLabel('Browse Genres', paddingTop: 0),
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: genres
                .map(
                  (g) => GestureDetector(
                    onTap: () => onTap(g),
                    child: GlassCard(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 18, vertical: 10),
                      borderRadius: BorderRadius.circular(40),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(Icons.music_note,
                              size: 14, color: AppColors.primary),
                          const SizedBox(width: 6),
                          Text(g,
                              style: const TextStyle(
                                  fontSize: 13,
                                  fontWeight: FontWeight.w600,
                                  color: AppColors.onSurface)),
                        ],
                      ),
                    ),
                  ),
                )
                .toList(),
          ),
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Artist chips
// ---------------------------------------------------------------------------

class _ArtistChips extends StatelessWidget {
  final List<String> artists;
  final ValueChanged<String> onTap;
  const _ArtistChips({required this.artists, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 44,
      child: ListView.separated(
        padding: const EdgeInsets.symmetric(horizontal: 20),
        scrollDirection: Axis.horizontal,
        physics: const BouncingScrollPhysics(),
        itemCount: artists.length,
        separatorBuilder: (_, _) => const SizedBox(width: 10),
        itemBuilder: (_, i) => GestureDetector(
          onTap: () => onTap(artists[i]),
          child: GlassCard(
            padding:
                const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            borderRadius: BorderRadius.circular(40),
            child: Row(
              children: [
                const Icon(Icons.person,
                    size: 14, color: AppColors.accent),
                const SizedBox(width: 6),
                Text(artists[i],
                    style: const TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                        color: AppColors.onSurface)),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Video discovery row — OTT style, shown in empty/discovery state
// ---------------------------------------------------------------------------

class _VideoDiscoveryRow extends ConsumerWidget {
  const _VideoDiscoveryRow();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return SizedBox(
      height: 130,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: kMockVideos.length,
        itemBuilder: (_, i) {
          final v = kMockVideos[i];
          return GestureDetector(
            onTap: () => ref
                .read(videoPlayerControllerProvider.notifier)
                .playVideo(v.url, title: v.title, artist: v.artist, artworkUrl: v.thumb),
            child: Padding(
              padding: const EdgeInsets.only(right: 12),
              child: SizedBox(
                width: 185,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    ClipRRect(
                      borderRadius: BorderRadius.circular(10),
                      child: AspectRatio(
                        aspectRatio: 16 / 9,
                        child: Stack(fit: StackFit.expand, children: [
                          SongThumbnail(
                            url: v.thumb,
                            fallback: Container(
                              color: AppColors.surfaceTwo,
                              child: const Icon(Icons.videocam,
                                  color: AppColors.onSurfaceMuted),
                            ),
                          ),
                          Center(
                            child: Container(
                              width: 32, height: 32,
                              decoration: BoxDecoration(
                                  shape: BoxShape.circle,
                                  color: Colors.black.withValues(alpha: 0.55)),
                              child: const Icon(Icons.play_arrow_rounded,
                                  color: Colors.white, size: 20),
                            ),
                          ),
                        ]),
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(v.title,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w600,
                            color: AppColors.onSurface)),
                  ],
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Song result tile
// ---------------------------------------------------------------------------

class _SongResultTile extends ConsumerWidget {
  final SongModel song;
  const _SongResultTile({required this.song});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 5),
      child: GlassCard(
        padding: const EdgeInsets.all(10),
        child: Row(
          children: [
            // Thumbnail
            ClipRRect(
              borderRadius: BorderRadius.circular(10),
              child: SizedBox(
                width: 52,
                height: 52,
                child: SongThumbnail(
                  url: song.thumbnail,
                  fallback: Container(
                    color: AppColors.surfaceTwo,
                    child: const Icon(Icons.music_note,
                        color: AppColors.onSurfaceMuted),
                  ),
                ),
              ),
            ),
            const SizedBox(width: 12),

            // Metadata
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    song.title,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w700,
                      color: AppColors.onSurface,
                    ),
                  ),
                  const SizedBox(height: 2),
                  // Tappable artist name — navigates to artist page if known
                  GestureDetector(
                    onTap: () {
                      final a = artistByName(song.artist);
                      if (a != null) context.go('/artist/${a.id}');
                    },
                    child: Text(
                      song.artist,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: TextStyle(
                        fontSize: 12,
                        color: artistByName(song.artist) != null
                            ? AppColors.primary
                            : AppColors.onSurfaceMuted,
                        decoration: artistByName(song.artist) != null
                            ? TextDecoration.underline
                            : TextDecoration.none,
                        decorationColor: AppColors.primary,
                      ),
                    ),
                  ),
                  if (song.genre != null)
                    Text(
                      song.genre!,
                      style: const TextStyle(
                        fontSize: 11,
                        color: AppColors.onSurfaceMuted,
                      ),
                    ),
                ],
              ),
            ),

            // Actions
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                DownloadIconButton(song: song),
                const SizedBox(width: 8),
                IconButton(
                  icon: const Icon(Icons.play_circle_rounded,
                      color: AppColors.primary, size: 36),
                  onPressed: () =>
                      ref.read(mediaControllerProvider.notifier).play(
                            song.audioUrl,
                            title: song.title,
                            artist: song.artist,
                            artworkUrl: song.thumbnail,
                          ),
                  padding: EdgeInsets.zero,
                  constraints: const BoxConstraints(),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Shared section label
// ---------------------------------------------------------------------------

class _SectionLabel extends StatelessWidget {
  final String text;
  final double paddingTop;
  const _SectionLabel(this.text, {this.paddingTop = 8});

  @override
  Widget build(BuildContext context) => Padding(
        padding: EdgeInsets.fromLTRB(20, paddingTop, 20, 12),
        child: Text(
          text,
          style: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w700,
            color: AppColors.onSurface,
            letterSpacing: -0.3,
          ),
        ),
      );
}
