import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:media_kit_video/media_kit_video.dart' hide VideoState;

import '../../../core/api_adapter/mock_data.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/song_thumbnail.dart';
import '../shared/media_session_orchestrator.dart';
import '../video/video_player_controller.dart';

// ---------------------------------------------------------------------------
// Entry point
// ---------------------------------------------------------------------------

Future<void> showVideoPlayer(BuildContext context) =>
    Navigator.of(context, rootNavigator: true).push<void>(
      MaterialPageRoute(builder: (_) => const VideoPlayerPage()),
    );

// ---------------------------------------------------------------------------
// Page
// ---------------------------------------------------------------------------

class VideoPlayerPage extends ConsumerStatefulWidget {
  const VideoPlayerPage({super.key});

  @override
  ConsumerState<VideoPlayerPage> createState() => _VideoPlayerPageState();
}

class _VideoPlayerPageState extends ConsumerState<VideoPlayerPage> {
  bool _showControls = true;
  Timer? _hideTimer;

  @override
  void initState() {
    super.initState();
    _resetHideTimer();
  }

  @override
  void dispose() {
    _hideTimer?.cancel();
    super.dispose();
  }

  void _toggleControls() {
    setState(() => _showControls = !_showControls);
    if (_showControls) _resetHideTimer();
  }

  void _resetHideTimer() {
    _hideTimer?.cancel();
    _hideTimer = Timer(const Duration(seconds: 3), () {
      if (mounted) setState(() => _showControls = false);
    });
  }

  @override
  Widget build(BuildContext context) {
    final orch = ref.watch(mediaSessionOrchestratorProvider);
    final vs = ref.watch(videoPlayerControllerProvider);
    final videoCtrl = ref.read(videoPlayerControllerProvider.notifier);

    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: Column(
          children: [
            // ── Video area ─────────────────────────────────────────────────
            GestureDetector(
              behavior: HitTestBehavior.opaque,
              onTap: _toggleControls,
              child: Container(
                color: Colors.black,
                child: AspectRatio(
                  aspectRatio: 16 / 9,
                  child: Stack(
                    children: [
                      Video(controller: videoCtrl.videoController),
                      if (vs.isLoading)
                        const Center(
                          child: CircularProgressIndicator(
                              color: AppColors.primary, strokeWidth: 2.5),
                        ),
                      AnimatedOpacity(
                        opacity: _showControls ? 1.0 : 0.0,
                        duration: const Duration(milliseconds: 260),
                        child: IgnorePointer(
                          ignoring: !_showControls,
                          child: _VideoControls(
                            vs: vs,
                            videoCtrl: videoCtrl,
                            onClose: () => Navigator.of(context).pop(),
                            onActivity: _resetHideTimer,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),

            // ── Scrollable content ─────────────────────────────────────────
            Expanded(
              child: SingleChildScrollView(
                physics: const BouncingScrollPhysics(),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _VideoInfoSection(
                      orch: orch,
                      onSwitchToAudio: () {
                        videoCtrl.stop();
                        Navigator.of(context).pop();
                      },
                    ),
                    const Divider(height: 1, color: AppColors.glassBorder),
                    const Padding(
                      padding: EdgeInsets.fromLTRB(16, 16, 16, 8),
                      child: Text(
                        'Up Next',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.w700,
                          color: AppColors.onSurface,
                          letterSpacing: -0.2,
                        ),
                      ),
                    ),
                    ...kMockVideos.map((v) => _RelatedCard(video: v)),
                    const SizedBox(height: 24),
                  ],
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
// Controls overlay (on the video surface)
// ---------------------------------------------------------------------------

class _VideoControls extends StatefulWidget {
  final VideoState vs;
  final VideoPlayerController videoCtrl;
  final VoidCallback onClose;
  final VoidCallback onActivity;

  const _VideoControls({
    required this.vs,
    required this.videoCtrl,
    required this.onClose,
    required this.onActivity,
  });

  @override
  State<_VideoControls> createState() => _VideoControlsState();
}

class _VideoControlsState extends State<_VideoControls> {
  double? _drag;

  String _fmt(Duration d) {
    final m = d.inMinutes.remainder(60).toString().padLeft(2, '0');
    final s = d.inSeconds.remainder(60).toString().padLeft(2, '0');
    return '$m:$s';
  }

  @override
  Widget build(BuildContext context) {
    final vs = widget.vs;
    final total = vs.duration.inMilliseconds.toDouble();
    final current =
        _drag ?? vs.position.inMilliseconds.clamp(0, total.toInt()).toDouble();

    return DecoratedBox(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            Colors.black.withValues(alpha: 0.55),
            Colors.transparent,
            Colors.black.withValues(alpha: 0.65),
          ],
          stops: const [0.0, 0.4, 1.0],
        ),
      ),
      child: Stack(
        children: [
          // Back button
          Positioned(
            top: 8,
            left: 4,
            child: IconButton(
              icon: const Icon(Icons.arrow_back_ios_new_rounded,
                  color: Colors.white, size: 20),
              onPressed: widget.onClose,
            ),
          ),
          // Center play/pause
          Center(
            child: GestureDetector(
              onTap: () {
                widget.onActivity();
                vs.isPlaying
                    ? widget.videoCtrl.pause()
                    : widget.videoCtrl.resume();
              },
              child: Container(
                width: 56,
                height: 56,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: AppColors.primary.withValues(alpha: 0.85),
                  boxShadow: [
                    BoxShadow(
                        color: AppColors.primary.withValues(alpha: 0.35),
                        blurRadius: 18),
                  ],
                ),
                child: Icon(
                  vs.isPlaying ? Icons.pause_rounded : Icons.play_arrow_rounded,
                  color: Colors.white,
                  size: 30,
                ),
              ),
            ),
          ),
          // Bottom seek bar
          Positioned(
            bottom: 0,
            left: 0,
            right: 0,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                SliderTheme(
                  data: SliderThemeData(
                    trackHeight: 2.5,
                    thumbShape:
                        const RoundSliderThumbShape(enabledThumbRadius: 6),
                    overlayShape:
                        const RoundSliderOverlayShape(overlayRadius: 14),
                    activeTrackColor: AppColors.primary,
                    inactiveTrackColor: Colors.white30,
                    thumbColor: AppColors.primary,
                    overlayColor: AppColors.primary.withValues(alpha: 0.2),
                  ),
                  child: Slider(
                    value: total > 0 ? current.clamp(0.0, total) : 0.0,
                    min: 0,
                    max: total > 0 ? total : 1,
                    onChangeStart: (_) {
                      widget.onActivity();
                      setState(() => _drag = current);
                    },
                    onChanged: (v) => setState(() => _drag = v),
                    onChangeEnd: (v) {
                      widget.videoCtrl
                          .seek(Duration(milliseconds: v.round()));
                      setState(() => _drag = null);
                    },
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 0, 20, 10),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(_fmt(Duration(milliseconds: current.round())),
                          style: const TextStyle(
                              color: Colors.white70, fontSize: 11)),
                      Text(_fmt(vs.duration),
                          style: const TextStyle(
                              color: Colors.white70, fontSize: 11)),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Info section below the video
// ---------------------------------------------------------------------------

class _VideoInfoSection extends StatelessWidget {
  final OrchestratorState orch;
  final VoidCallback onSwitchToAudio;

  const _VideoInfoSection(
      {required this.orch, required this.onSwitchToAudio});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 14, 16, 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            orch.currentItem?.title ?? 'Video',
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              fontSize: 17,
              fontWeight: FontWeight.w700,
              color: AppColors.onSurface,
              letterSpacing: -0.3,
            ),
          ),
          if (orch.currentItem?.artist != null) ...[
            const SizedBox(height: 4),
            Text(
              orch.currentItem!.artist!,
              style: const TextStyle(
                  fontSize: 13, color: AppColors.onSurfaceMuted),
            ),
          ],
          const SizedBox(height: 12),
          OutlinedButton.icon(
            onPressed: onSwitchToAudio,
            icon: const Icon(Icons.headphones_rounded,
                size: 16, color: AppColors.primary),
            label: const Text('Switch to Audio',
                style: TextStyle(
                    color: AppColors.primary,
                    fontSize: 13,
                    fontWeight: FontWeight.w600)),
            style: OutlinedButton.styleFrom(
              side:
                  BorderSide(color: AppColors.primary.withValues(alpha: 0.6)),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(20)),
              padding:
                  const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            ),
          ),
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Related video card
// ---------------------------------------------------------------------------

class _RelatedCard extends ConsumerWidget {
  final MockVideo video;
  const _RelatedCard({required this.video});

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
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
        child: Row(
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: SizedBox(
                width: 120,
                height: 68,
                child: SongThumbnail(
                  url: video.thumb,
                  fallback: Container(
                    color: AppColors.surfaceTwo,
                    child: const Icon(Icons.videocam,
                        color: AppColors.onSurfaceMuted, size: 28),
                  ),
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    video.title,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                      color: AppColors.onSurface,
                    ),
                  ),
                  const SizedBox(height: 3),
                  Text(
                    video.artist,
                    style: const TextStyle(
                        fontSize: 11, color: AppColors.onSurfaceMuted),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
