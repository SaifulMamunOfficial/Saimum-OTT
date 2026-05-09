import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/api_adapter/mock_data.dart';
import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/theme/app_colors.dart';
import '../controllers/media_controller.dart';
import '../shared/download_icon_button.dart';

// ---------------------------------------------------------------------------
// Genre palette
// ---------------------------------------------------------------------------

const _kGenres = [
  (label: 'All', icon: Icons.music_note_rounded, color: AppColors.primary),
  (label: 'Electronic', icon: Icons.bolt_rounded, color: Color(0xFF00F2FF)),
  (label: 'Chill', icon: Icons.ac_unit_rounded, color: Color(0xFF4FC3F7)),
  (label: 'Ambient', icon: Icons.nights_stay_rounded, color: AppColors.accent),
  (label: 'Lo-Fi', icon: Icons.headphones_rounded, color: Color(0xFFFF6B6B)),
  (label: 'Hip-Hop', icon: Icons.graphic_eq_rounded, color: Color(0xFFFFD700)),
];

// ---------------------------------------------------------------------------
// Page
// ---------------------------------------------------------------------------

// Mood → genre tag mapping
const _kMoodMap = <String, List<String>>{
  'Relax': ['Chill', 'Ambient'],
  'Party': ['Electronic'],
  'Workout': ['Electronic', 'Lo-Fi'],
  'Focus': ['Ambient', 'Lo-Fi'],
};

class MusicListPage extends ConsumerStatefulWidget {
  const MusicListPage({super.key});

  @override
  ConsumerState<MusicListPage> createState() => _MusicListPageState();
}

class _MusicListPageState extends ConsumerState<MusicListPage> {
  String? _mood;

  List<SongModel> _filtered(Iterable<SongModel> src) {
    if (_mood == null) return src.toList();
    final tags = _kMoodMap[_mood]!;
    final res =
        src.where((s) => tags.contains(s.genre)).toList();
    return res.isEmpty ? src.toList() : res;
  }

  @override
  Widget build(BuildContext context) {
    final topCharts = _filtered(kMockSongs.take(5));
    final newReleases = _filtered(kMockSongs.reversed.take(5));
    final quickPicks = _filtered(kMockSongs).take(6).toList();

    return CustomScrollView(
      physics: const BouncingScrollPhysics(),
      slivers: [
        // ── Header ──────────────────────────────────────────────────────────
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 64, 20, 20),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Music',
                    style: TextStyle(
                        fontSize: 30,
                        fontWeight: FontWeight.w800,
                        color: AppColors.onSurface,
                        letterSpacing: -0.7)),
                Text('${kMockSongs.length} songs',
                    style: const TextStyle(
                        fontSize: 13, color: AppColors.onSurfaceMuted)),
              ],
            ),
          ),
        ),

        // ── Mood Chips ───────────────────────────────────────────────────────
        SliverToBoxAdapter(
          child: _MoodChips(
            selected: _mood,
            onSelect: (m) => setState(
                () => _mood = _mood == m ? null : m),
          ),
        ),
        const SliverToBoxAdapter(child: SizedBox(height: 22)),

        // ── Genre circles ────────────────────────────────────────────────────
        const SliverToBoxAdapter(child: _GenreCircles()),
        const SliverToBoxAdapter(child: SizedBox(height: 28)),

        // ── Artists ──────────────────────────────────────────────────────────
        const SliverToBoxAdapter(child: _SectionLabel('Artists')),
        const SliverToBoxAdapter(child: _ArtistsRow()),
        const SliverToBoxAdapter(child: SizedBox(height: 28)),

        // ── Quick Picks (2×3 grid) ───────────────────────────────────────────
        const SliverToBoxAdapter(child: _SectionLabel('Quick Picks')),
        SliverToBoxAdapter(child: _QuickPicksGrid(songs: quickPicks)),
        const SliverToBoxAdapter(child: SizedBox(height: 28)),

        // ── Top Charts (horizontal) ──────────────────────────────────────────
        const SliverToBoxAdapter(child: _SectionLabel('Top Charts')),
        SliverToBoxAdapter(child: _HorizontalMusicRow(songs: topCharts)),
        const SliverToBoxAdapter(child: SizedBox(height: 28)),

        // ── New Releases (horizontal) ────────────────────────────────────────
        const SliverToBoxAdapter(child: _SectionLabel('New Releases')),
        SliverToBoxAdapter(child: _HorizontalMusicRow(songs: newReleases)),
        const SliverToBoxAdapter(child: SizedBox(height: 120)),
      ],
    );
  }
}

// ---------------------------------------------------------------------------
// Section label
// ---------------------------------------------------------------------------

class _SectionLabel extends StatelessWidget {
  final String text;
  const _SectionLabel(this.text);

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
// Genre circles — horizontal scroll
// ---------------------------------------------------------------------------

class _GenreCircles extends StatelessWidget {
  const _GenreCircles();

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 86,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: _kGenres.length,
        itemBuilder: (_, i) {
          final g = _kGenres[i];
          return Padding(
            padding: const EdgeInsets.symmetric(horizontal: 10),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  width: 54,
                  height: 54,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: g.color.withValues(alpha: 0.12),
                    border: Border.all(
                        color: g.color.withValues(alpha: 0.45), width: 1.5),
                  ),
                  child: Icon(g.icon, color: g.color, size: 22),
                ),
                const SizedBox(height: 6),
                Text(g.label,
                    style: const TextStyle(
                        fontSize: 10.5, color: AppColors.onSurfaceMuted)),
              ],
            ),
          );
        },
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Artists row — circular profiles, taps navigate to /artist/:id
// ---------------------------------------------------------------------------

class _ArtistsRow extends StatelessWidget {
  const _ArtistsRow();

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 104,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: kMockArtists.length,
        itemBuilder: (_, i) => _ArtistCircle(artist: kMockArtists[i]),
      ),
    );
  }
}

class _ArtistCircle extends StatelessWidget {
  final MockArtist artist;
  const _ArtistCircle({required this.artist});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => context.go('/artist/${artist.id}'),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 10),
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
                      color: AppColors.primary.withValues(alpha: 0.15),
                      blurRadius: 10),
                ],
              ),
              child: ClipOval(
                child: SizedBox(
                  width: 68,
                  height: 68,
                  child: Image.network(
                    artist.imageUrl,
                    fit: BoxFit.cover,
                    errorBuilder: (_, _, _) => Container(
                      color: AppColors.surfaceOne,
                      child: const Icon(Icons.person_rounded,
                          color: AppColors.primary, size: 28),
                    ),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 6),
            SizedBox(
              width: 72,
              child: Text(artist.name,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.w600,
                      color: AppColors.onSurface)),
            ),
          ],
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Mood chips — glowing glass row
// ---------------------------------------------------------------------------

const _kMoods = [
  (label: 'Relax', icon: Icons.spa_rounded, color: Color(0xFF4FC3F7)),
  (label: 'Party', icon: Icons.celebration_rounded, color: Color(0xFFFF6B6B)),
  (label: 'Workout', icon: Icons.fitness_center_rounded, color: Color(0xFFFFD700)),
  (label: 'Focus', icon: Icons.psychology_rounded, color: AppColors.accent),
];

class _MoodChips extends StatelessWidget {
  final String? selected;
  final ValueChanged<String> onSelect;
  const _MoodChips({required this.selected, required this.onSelect});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 44,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: _kMoods.length,
        itemBuilder: (_, i) {
          final m = _kMoods[i];
          final active = selected == m.label;
          return Padding(
            padding: const EdgeInsets.only(right: 10),
            child: GestureDetector(
              onTap: () => onSelect(m.label),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 200),
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 9),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(22),
                  color: active
                      ? m.color.withValues(alpha: 0.18)
                      : Colors.transparent,
                  border: Border.all(
                    color: active
                        ? m.color
                        : AppColors.glassBorder,
                    width: 1.5,
                  ),
                  boxShadow: active
                      ? [BoxShadow(
                          color: m.color.withValues(alpha: 0.28),
                          blurRadius: 12)]
                      : [],
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(m.icon,
                        size: 15,
                        color: active ? m.color : AppColors.onSurfaceMuted),
                    const SizedBox(width: 6),
                    Text(m.label,
                        style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w600,
                            color: active ? m.color : AppColors.onSurfaceMuted)),
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
// Quick Picks — 2-column compact grid
// ---------------------------------------------------------------------------

class _QuickPicksGrid extends ConsumerWidget {
  final List<SongModel> songs;
  const _QuickPicksGrid({required this.songs});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: GridView.builder(
        physics: const NeverScrollableScrollPhysics(),
        shrinkWrap: true,
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 2,
          crossAxisSpacing: 8,
          mainAxisSpacing: 8,
          childAspectRatio: 3.4,
        ),
        itemCount: songs.length,
        itemBuilder: (_, i) => _QuickPickTile(song: songs[i]),
      ),
    );
  }
}

class _QuickPickTile extends ConsumerWidget {
  final SongModel song;
  const _QuickPickTile({required this.song});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return GestureDetector(
      onTap: () => ref.read(mediaControllerProvider.notifier).play(
            song.audioUrl,
            title: song.title,
            artist: song.artist,
            artworkUrl: song.thumbnail,
          ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(6),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 10, sigmaY: 10),
          child: Container(
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(6),
              color: AppColors.surfaceTwo,
              border: Border.all(color: AppColors.glassBorder, width: 0.5),
            ),
            child: Row(
              children: [
                ClipRRect(
                  borderRadius: const BorderRadius.only(
                    topLeft: Radius.circular(6),
                    bottomLeft: Radius.circular(6),
                  ),
                  child: Image.network(
                    song.thumbnail,
                    width: 52,
                    height: 52,
                    fit: BoxFit.cover,
                    errorBuilder: (_, _, _) => Container(
                      width: 52,
                      color: AppColors.surfaceOne,
                      child: const Icon(Icons.music_note,
                          color: AppColors.primary, size: 18),
                    ),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    song.title,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w600,
                        color: AppColors.onSurface),
                  ),
                ),
                DownloadIconButton(song: song, size: 20),
                const SizedBox(width: 4),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Horizontal music card row
// ---------------------------------------------------------------------------

class _HorizontalMusicRow extends ConsumerWidget {
  final List<SongModel> songs;
  const _HorizontalMusicRow({required this.songs});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return SizedBox(
      height: 182,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        itemCount: songs.length,
        itemBuilder: (_, i) => _MusicCard(song: songs[i]),
      ),
    );
  }
}

class _MusicCard extends ConsumerWidget {
  final SongModel song;
  const _MusicCard({required this.song});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return GestureDetector(
      onTap: () => ref.read(mediaControllerProvider.notifier).play(
            song.audioUrl,
            title: song.title,
            artist: song.artist,
            artworkUrl: song.thumbnail,
          ),
      child: Padding(
        padding: const EdgeInsets.only(right: 14),
        child: SizedBox(
          width: 128,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Stack(
                children: [
                  ClipRRect(
                    borderRadius: BorderRadius.circular(12),
                    child: AspectRatio(
                      aspectRatio: 1,
                      child: Image.network(
                        song.thumbnail,
                        fit: BoxFit.cover,
                        errorBuilder: (_, _, _) => Container(
                          color: AppColors.surfaceTwo,
                          child: const Icon(Icons.music_note,
                              color: AppColors.primary, size: 32),
                        ),
                      ),
                    ),
                  ),
                  // Download button badge
                  Positioned(
                    right: 4,
                    bottom: 4,
                    child: DownloadIconButton(song: song, size: 18),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Text(song.title,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      color: AppColors.onSurface)),
              const SizedBox(height: 2),
              GestureDetector(
                onTap: () {
                  final a = artistByName(song.artist);
                  if (a != null) context.go('/artist/${a.id}');
                },
                child: Text(song.artist,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        fontSize: 11,
                        color: artistByName(song.artist) != null
                            ? AppColors.primary
                            : AppColors.onSurfaceMuted)),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
