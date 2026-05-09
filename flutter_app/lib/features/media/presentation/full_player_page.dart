import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/player_overlays.dart';
import '../../../core/widgets/song_thumbnail.dart';
import '../controllers/media_controller.dart';
import '../shared/media_session_orchestrator.dart';

// ---------------------------------------------------------------------------
// Public entry point
// ---------------------------------------------------------------------------

void showFullPlayer(BuildContext context) {
  showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.transparent,
    isScrollControlled: true,
    useRootNavigator: true,
    isDismissible: true,
    enableDrag: true,
    builder: (_) => const FullPlayerPage(),
  );
}

// ---------------------------------------------------------------------------
// Page
// ---------------------------------------------------------------------------

class FullPlayerPage extends ConsumerStatefulWidget {
  const FullPlayerPage({super.key});

  @override
  ConsumerState<FullPlayerPage> createState() => _FullPlayerPageState();
}

class _FullPlayerPageState extends ConsumerState<FullPlayerPage> {
  bool _isDragging = false;
  double _dragValue = 0;
  bool _isFavorited = false;
  bool _isShuffle = false;
  bool _isRepeat = false;

  String _fmt(Duration d) {
    final m = d.inMinutes.remainder(60).toString().padLeft(2, '0');
    final s = d.inSeconds.remainder(60).toString().padLeft(2, '0');
    return '$m:$s';
  }

  @override
  Widget build(BuildContext context) {
    final orch = ref.watch(mediaSessionOrchestratorProvider);
    final ctrl = ref.read(mediaControllerProvider.notifier);

    final item = orch.currentItem;
    final isPlaying = orch.status == PlaybackStatus.playing;
    final isLoading = orch.status == PlaybackStatus.loading;
    final maxSecs = orch.duration.inSeconds > 0
        ? orch.duration.inSeconds.toDouble()
        : 1.0;
    final posSecs = _isDragging
        ? _dragValue
        : orch.position.inSeconds.toDouble().clamp(0.0, maxSecs);

    return DraggableScrollableSheet(
      initialChildSize: 0.96,
      minChildSize: 0.5,
      maxChildSize: 0.96,
      snap: true,
      builder: (_, scrollCtrl) => ClipRRect(
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
        child: Stack(
          children: [
            if (item?.artworkUrl != null)
              _BlurredBackground(url: item!.artworkUrl!),
            Container(color: AppColors.background.withValues(alpha: 0.72)),
            SafeArea(
              child: SingleChildScrollView(
                controller: scrollCtrl,
                physics: const ClampingScrollPhysics(),
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 28),
                  child: Column(
                    children: [
                      const SizedBox(height: 12),
                      // Drag handle
                      Center(
                        child: Container(
                          width: 40, height: 4,
                          decoration: BoxDecoration(
                            color: AppColors.onSurfaceMuted.withValues(alpha: 0.4),
                            borderRadius: BorderRadius.circular(2),
                          ),
                        ),
                      ),
                      const SizedBox(height: 16),

                      // ── Header row ─────────────────────────────────────
                      Row(
                        children: [
                          IconButton(
                            icon: const Icon(Icons.keyboard_arrow_down_rounded,
                                color: AppColors.onSurfaceMuted, size: 28),
                            onPressed: () => Navigator.of(context).pop(),
                          ),
                          const Expanded(
                            child: Text('Now Playing',
                                textAlign: TextAlign.center,
                                style: TextStyle(
                                    fontSize: 12,
                                    fontWeight: FontWeight.w600,
                                    color: AppColors.onSurfaceMuted,
                                    letterSpacing: 1.2)),
                          ),
                          IconButton(
                            icon: const Icon(Icons.more_vert_rounded,
                                color: AppColors.onSurfaceMuted, size: 24),
                            onPressed: () => showMoreOptionsSheet(context),
                          ),
                        ],
                      ),
                      const SizedBox(height: 20),

                      // ── Artwork ────────────────────────────────────────
                      _ArtworkCard(url: item?.artworkUrl, isPlaying: isPlaying),
                      const SizedBox(height: 28),

                      // ── Song info ──────────────────────────────────────
                      Text(item?.title ?? 'Unknown',
                          textAlign: TextAlign.center,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                              fontSize: 22,
                              fontWeight: FontWeight.w800,
                              color: AppColors.onSurface,
                              letterSpacing: -0.5)),
                      const SizedBox(height: 6),
                      Text(item?.artist ?? '',
                          textAlign: TextAlign.center,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                              fontSize: 15,
                              color: AppColors.onSurfaceMuted,
                              fontWeight: FontWeight.w500)),
                      const SizedBox(height: 28),

                      // ── Seek bar ───────────────────────────────────────
                      _SeekBar(
                        posSecs: posSecs,
                        maxSecs: maxSecs,
                        posLabel: _fmt(orch.position),
                        durLabel: _fmt(orch.duration),
                        onChanged: (v) =>
                            setState(() { _isDragging = true; _dragValue = v; }),
                        onChangeEnd: (v) {
                          setState(() => _isDragging = false);
                          ctrl.seek(Duration(seconds: v.toInt()));
                        },
                      ),
                      const SizedBox(height: 20),

                      // ── Icon action bar ────────────────────────────────
                      _IconBar(
                        isFavorited: _isFavorited,
                        onFavourite: () =>
                            setState(() => _isFavorited = !_isFavorited),
                        onLyrics: () =>
                            showLyricsOverlay(context, item?.title, item?.artist),
                        onQueue: () => showQueueSheet(context),
                      ),
                      const SizedBox(height: 32),

                      // ── Main controls (5-button) ───────────────────────
                      _ControlRow(
                        isPlaying: isPlaying,
                        isLoading: isLoading,
                        isShuffle: _isShuffle,
                        isRepeat: _isRepeat,
                        onPlayPause: () { if (isPlaying) ctrl.pause(); },
                        onShuffle: () =>
                            setState(() => _isShuffle = !_isShuffle),
                        onRepeat: () =>
                            setState(() => _isRepeat = !_isRepeat),
                      ),
                      const SizedBox(height: 40),
                    ],
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
// Background
// ---------------------------------------------------------------------------

class _BlurredBackground extends StatelessWidget {
  final String url;
  const _BlurredBackground({required this.url});

  @override
  Widget build(BuildContext context) => Positioned.fill(
        child: ImageFiltered(
          imageFilter: ImageFilter.blur(sigmaX: 60, sigmaY: 60),
          child: SongThumbnail(
            url: url,
            fallback: Container(color: AppColors.surfaceTwo),
          ),
        ),
      );
}

// ---------------------------------------------------------------------------
// Artwork card with animated glow
// ---------------------------------------------------------------------------

class _ArtworkCard extends StatelessWidget {
  final String? url;
  final bool isPlaying;
  const _ArtworkCard({required this.url, required this.isPlaying});

  @override
  Widget build(BuildContext context) {
    // Fixed size derived from available width — never changes with playback state.
    final size = (MediaQuery.sizeOf(context).width - 56).clamp(240.0, 300.0);

    // Only the shadow animates (glow intensity). Size never changes → no layout jump.
    return AnimatedContainer(
      duration: const Duration(milliseconds: 400),
      width: size,
      height: size,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: AppColors.primary
                .withValues(alpha: isPlaying ? 0.38 : 0.08),
            blurRadius: isPlaying ? 64 : 14,
            // No spreadRadius — keeps shadow purely cosmetic, zero layout impact.
          ),
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.5),
            blurRadius: 24,
            offset: const Offset(0, 12),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(24),
        child: SongThumbnail(url: url, fallback: _fallback()),
      ),
    );
  }

  Widget _fallback() => Container(
        color: AppColors.surfaceTwo,
        child: const Icon(Icons.music_note_rounded,
            color: AppColors.primary, size: 80),
      );
}

// ---------------------------------------------------------------------------
// Seek bar
// ---------------------------------------------------------------------------

class _SeekBar extends StatelessWidget {
  final double posSecs, maxSecs;
  final String posLabel, durLabel;
  final ValueChanged<double> onChanged, onChangeEnd;

  const _SeekBar({
    required this.posSecs, required this.maxSecs,
    required this.posLabel, required this.durLabel,
    required this.onChanged, required this.onChangeEnd,
  });

  @override
  Widget build(BuildContext context) => Column(
        children: [
          SliderTheme(
            data: SliderThemeData(
              trackHeight: 3,
              thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 7),
              thumbColor: Colors.white,
              activeTrackColor: AppColors.primary,
              inactiveTrackColor: Colors.white.withValues(alpha: 0.15),
              overlayColor: AppColors.primary.withValues(alpha: 0.15),
              overlayShape: const RoundSliderOverlayShape(overlayRadius: 14),
            ),
            child: Slider(
              value: posSecs, max: maxSecs,
              onChanged: onChanged, onChangeEnd: onChangeEnd,
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(posLabel, style: const TextStyle(fontSize: 12, color: AppColors.onSurfaceMuted)),
                Text(durLabel, style: const TextStyle(fontSize: 12, color: AppColors.onSurfaceMuted)),
              ],
            ),
          ),
        ],
      );
}

// ---------------------------------------------------------------------------
// Icon action bar  (Favourite | Lyrics | Save | Queue)
// ---------------------------------------------------------------------------

class _IconBar extends StatelessWidget {
  final bool isFavorited;
  final VoidCallback onFavourite, onLyrics, onQueue;
  const _IconBar({
    required this.isFavorited,
    required this.onFavourite,
    required this.onLyrics,
    required this.onQueue,
  });

  @override
  Widget build(BuildContext context) => Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          _IconAction(
            icon: isFavorited ? Icons.favorite : Icons.favorite_border,
            label: 'Favourite',
            color: isFavorited ? Colors.redAccent : AppColors.onSurfaceMuted,
            onTap: onFavourite,
          ),
          _IconAction(icon: Icons.lyrics_outlined, label: 'Lyrics', onTap: onLyrics),
          _IconAction(icon: Icons.download_for_offline_outlined, label: 'Save', onTap: () {}),
          _IconAction(icon: Icons.queue_music_rounded, label: 'Queue', onTap: onQueue),
        ],
      );
}

class _IconAction extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;
  const _IconAction({
    required this.icon, required this.label,
    this.color = AppColors.onSurfaceMuted, required this.onTap,
  });

  @override
  Widget build(BuildContext context) => GestureDetector(
        onTap: onTap,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, color: color, size: 24),
            const SizedBox(height: 4),
            Text(label,
                style: const TextStyle(
                    fontSize: 10,
                    color: AppColors.onSurfaceMuted,
                    fontWeight: FontWeight.w500)),
          ],
        ),
      );
}

// ---------------------------------------------------------------------------
// 5-button control row: Shuffle | Prev | Play/Pause | Next | Repeat
// ---------------------------------------------------------------------------

class _ControlRow extends StatelessWidget {
  final bool isPlaying, isLoading, isShuffle, isRepeat;
  final VoidCallback onPlayPause, onShuffle, onRepeat;

  const _ControlRow({
    required this.isPlaying,
    required this.isLoading,
    required this.isShuffle,
    required this.isRepeat,
    required this.onPlayPause,
    required this.onShuffle,
    required this.onRepeat,
  });

  @override
  Widget build(BuildContext context) => Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // Shuffle
          IconButton(
            iconSize: 26,
            icon: Icon(
              Icons.shuffle_rounded,
              color: isShuffle
                  ? AppColors.primary
                  : AppColors.onSurfaceMuted.withValues(alpha: 0.45),
            ),
            onPressed: onShuffle,
          ),

          // Previous — stub (Phase 6)
          IconButton(
            iconSize: 36,
            icon: const Icon(Icons.skip_previous_rounded,
                color: AppColors.onSurface),
            onPressed: null,
          ),

          // Play / Pause — 72dp neon circle
          GestureDetector(
            onTap: isLoading ? null : onPlayPause,
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              width: 72, height: 72,
              decoration: BoxDecoration(
                color: AppColors.primary,
                shape: BoxShape.circle,
                boxShadow: [
                  BoxShadow(
                    color: AppColors.primary.withValues(alpha: 0.45),
                    blurRadius: 28, spreadRadius: 2,
                  ),
                ],
              ),
              child: isLoading
                  ? const Padding(
                      padding: EdgeInsets.all(22),
                      child: CircularProgressIndicator(
                          strokeWidth: 2.5, color: Colors.black),
                    )
                  : Icon(
                      isPlaying
                          ? Icons.pause_rounded
                          : Icons.play_arrow_rounded,
                      color: Colors.black, size: 40,
                    ),
            ),
          ),

          // Next — stub (Phase 6)
          IconButton(
            iconSize: 36,
            icon: const Icon(Icons.skip_next_rounded,
                color: AppColors.onSurface),
            onPressed: null,
          ),

          // Repeat
          IconButton(
            iconSize: 26,
            icon: Icon(
              Icons.repeat_rounded,
              color: isRepeat
                  ? AppColors.primary
                  : AppColors.onSurfaceMuted.withValues(alpha: 0.45),
            ),
            onPressed: onRepeat,
          ),
        ],
      );
}
