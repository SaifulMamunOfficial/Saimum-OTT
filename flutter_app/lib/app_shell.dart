import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:media_kit_video/media_kit_video.dart';

import 'core/router/app_router.dart';
import 'core/theme/app_colors.dart';
import 'core/theme/app_theme.dart';
import 'features/media/controllers/media_controller.dart';
import 'features/media/presentation/full_player_page.dart';
import 'features/media/presentation/video_player_page.dart';
import 'features/media/shared/media_session_orchestrator.dart';
import 'features/media/video/video_player_controller.dart';

// ---------------------------------------------------------------------------
// App Root — sets up MaterialApp.router + theme
// ---------------------------------------------------------------------------

class AppShell extends ConsumerWidget {
  const AppShell({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return MaterialApp.router(
      title: 'Saimum Music',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.dark,
      routerConfig: ref.watch(routerProvider),
    );
  }
}

// ---------------------------------------------------------------------------
// Main Shell — wraps every route with background + bottom nav + mini player
// ---------------------------------------------------------------------------

class MainShell extends ConsumerWidget {
  final Widget child;
  const MainShell({super.key, required this.child});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final videoCtrl = ref.read(videoPlayerControllerProvider.notifier);

    // Auto-open VideoPlayerPage when orchestrator switches to video mode.
    ref.listen<OrchestratorState>(
      mediaSessionOrchestratorProvider,
      (prev, next) {
        if ((prev == null || !prev.isVideoActive) &&
            next.isVideoActive &&
            context.mounted) {
          showVideoPlayer(context);
        }
      },
    );

    return Scaffold(
      backgroundColor: AppColors.background,
      body: Stack(
        children: [
          const _BackgroundOrbs(),
          // Keep Video in tree at all times to pre-register the platform
          // texture before playVideo() is called — prevents blank screen race.
          Visibility(
            visible: false,
            maintainState: true,
            child: Video(controller: videoCtrl.videoController),
          ),
          child,
          // Floating profile avatar — hidden on detail pages (/artist, /album)
          // to prevent overlap with their own SliverAppBar back button / title.
          Builder(builder: (ctx) {
            final loc = GoRouterState.of(ctx).matchedLocation;
            final onDetail = loc.startsWith('/artist/') ||
                loc.startsWith('/album/');
            if (onDetail) return const SizedBox.shrink();
            return Positioned(
              top: MediaQuery.paddingOf(ctx).top + 8,
              right: 16,
              child: _ProfileAvatar(),
            );
          }),
        ],
      ),
      bottomNavigationBar: const Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          _MiniPlayer(),
          _BottomNav(),
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Floating Profile Avatar
// ---------------------------------------------------------------------------

class _ProfileAvatar extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => context.go('/profile'),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(22),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 18, sigmaY: 18),
          child: Container(
            width: 38,
            height: 38,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: AppColors.glassFill,
              border: Border.all(
                color: AppColors.primary.withValues(alpha: 0.5),
                width: 1.5,
              ),
            ),
            child: const Icon(Icons.person_rounded,
                color: AppColors.primary, size: 20),
          ),
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Glass Bottom Navigation Bar — 5 tabs, icons only
// ---------------------------------------------------------------------------

class _BottomNav extends ConsumerWidget {
  const _BottomNav();

  static const _tabs = [
    (icon: Icons.home_outlined, active: Icons.home_rounded, path: '/'),
    (icon: Icons.music_note_outlined, active: Icons.music_note_rounded, path: '/music'),
    (icon: Icons.play_circle_outline_rounded, active: Icons.play_circle_rounded, path: '/video'),
    (icon: Icons.search_outlined, active: Icons.search_rounded, path: '/search'),
    (icon: Icons.library_music_outlined, active: Icons.library_music_rounded, path: '/library'),
  ];

  int _index(BuildContext context) {
    final loc = GoRouterState.of(context).matchedLocation;
    if (loc.startsWith('/music')) return 1;
    if (loc.startsWith('/video')) return 2;
    if (loc.startsWith('/search')) return 3;
    if (loc.startsWith('/library')) return 4;
    return 0;
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selected = _index(context);
    return ClipRect(
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 20, sigmaY: 20),
        child: NavigationBar(
          height: 56,
          backgroundColor: AppColors.glassFill,
          surfaceTintColor: Colors.transparent,
          indicatorColor: AppColors.primary.withValues(alpha: 0.18),
          selectedIndex: selected,
          labelBehavior: NavigationDestinationLabelBehavior.alwaysHide,
          onDestinationSelected: (i) => context.go(_tabs[i].path),
          destinations: _tabs
              .map((t) => NavigationDestination(
                    icon: Icon(t.icon, color: AppColors.onSurfaceMuted),
                    selectedIcon: Icon(t.active, color: AppColors.primary),
                    label: '',
                  ))
              .toList(),
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Grounded Mini Player — full-width, fused with Bottom Nav
// ---------------------------------------------------------------------------

class _MiniPlayer extends ConsumerWidget {
  const _MiniPlayer();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final orch = ref.watch(mediaSessionOrchestratorProvider);
    if (!orch.hasActiveItem) return const SizedBox.shrink();

    final item = orch.currentItem!;
    final isPlaying = orch.status == PlaybackStatus.playing;
    final isLoading = orch.status == PlaybackStatus.loading;
    final isVideo = orch.mode == PlaybackMode.video;
    final progress = orch.duration.inMilliseconds > 0
        ? (orch.position.inMilliseconds / orch.duration.inMilliseconds)
            .clamp(0.0, 1.0)
        : 0.0;

    // Full-width, no margins — grounded directly above the nav bar.
    // GestureDetector covers everything; the IconButton absorbs its own taps.
    return GestureDetector(
      onTap: () =>
          isVideo ? showVideoPlayer(context) : showFullPlayer(context),
      child: ClipRect(
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 30, sigmaY: 30),
          child: Container(
            height: 72,
            decoration: BoxDecoration(
              color: AppColors.background.withValues(alpha: 0.80),
              border: const Border(
                top: BorderSide(color: Colors.white12, width: 0.5),
              ),
            ),
            child: Column(
              children: [
                // ── Content row ──────────────────────────────────────────
                Expanded(
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    child: Row(
                      children: [
                        // Thumbnail — 56dp × 56dp, 6dp radius
                        ClipRRect(
                          borderRadius: BorderRadius.circular(6),
                          child: SizedBox(
                            width: 56,
                            height: 56,
                            child: item.artworkUrl != null
                                ? Image.network(
                                    item.artworkUrl!,
                                    fit: BoxFit.cover,
                                    errorBuilder: (_, _, _) =>
                                        _fallback(isVideo),
                                  )
                                : _fallback(isVideo),
                          ),
                        ),
                        const SizedBox(width: 14),

                        // Song info
                        Expanded(
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                item.title,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontWeight: FontWeight.w700,
                                  fontSize: 14,
                                  color: AppColors.onSurface,
                                  letterSpacing: -0.2,
                                ),
                              ),
                              if (item.artist != null)
                                Text(
                                  item.artist!,
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: TextStyle(
                                    fontSize: 12,
                                    fontWeight: FontWeight.w400,
                                    color: AppColors.onSurface
                                        .withValues(alpha: 0.7),
                                  ),
                                ),
                            ],
                          ),
                        ),

                        // Play / Pause — large, clean, right-anchored
                        if (isLoading)
                          const SizedBox(
                            width: 26,
                            height: 26,
                            child: CircularProgressIndicator(
                                strokeWidth: 2.2,
                                color: AppColors.primary),
                          )
                        else
                          IconButton(
                            iconSize: 34,
                            icon: Icon(
                              isPlaying
                                  ? Icons.pause_rounded
                                  : Icons.play_arrow_rounded,
                              color: AppColors.primary,
                            ),
                            onPressed: isPlaying
                                ? () => _pause(ref, orch.mode)
                                : () => _resume(ref, orch.mode),
                            padding: const EdgeInsets.all(8),
                          ),
                      ],
                    ),
                  ),
                ),

                // ── 1px neon progress line at very bottom ────────────────
                LinearProgressIndicator(
                  value: progress,
                  minHeight: 1,
                  backgroundColor: Colors.transparent,
                  valueColor:
                      const AlwaysStoppedAnimation<Color>(AppColors.primary),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _fallback(bool isVideo) => Container(
        color: AppColors.surfaceOne,
        child: Icon(isVideo ? Icons.videocam : Icons.music_note,
            color: AppColors.primary, size: 22),
      );

  void _pause(WidgetRef ref, PlaybackMode mode) {
    if (mode == PlaybackMode.video) {
      ref.read(videoPlayerControllerProvider.notifier).pause();
    } else {
      ref.read(mediaControllerProvider.notifier).pause();
    }
  }

  void _resume(WidgetRef ref, PlaybackMode mode) {
    if (mode == PlaybackMode.video) {
      ref.read(videoPlayerControllerProvider.notifier).resume();
    } else {
      ref.read(mediaControllerProvider.notifier).resume();
    }
  }
}

// ---------------------------------------------------------------------------
// Background gradient orbs
// ---------------------------------------------------------------------------

class _BackgroundOrbs extends StatelessWidget {
  const _BackgroundOrbs();

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.sizeOf(context);
    return SizedBox.expand(
      child: Stack(
        children: [
          Positioned(
            top: -80,
            left: -60,
            child: _orb(200, AppColors.primary.withValues(alpha: 0.15)),
          ),
          Positioned(
            bottom: -60,
            right: -40,
            child: _orb(220, AppColors.accent.withValues(alpha: 0.20)),
          ),
          Positioned(
            top: size.height * 0.45,
            left: size.width * 0.25,
            child: _orb(160, AppColors.primary.withValues(alpha: 0.07)),
          ),
        ],
      ),
    );
  }

  Widget _orb(double size, Color color) => ImageFiltered(
        imageFilter: ImageFilter.blur(sigmaX: 60, sigmaY: 60),
        child: Container(
          width: size,
          height: size,
          decoration: BoxDecoration(color: color, shape: BoxShape.circle),
        ),
      );
}
