import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/providers/auth_provider.dart';
import '../../../core/theme/app_colors.dart';
import '../controllers/download_controller.dart';

/// Tri-state download button for a [SongModel]:
///
/// • **idle**        → outline download icon (muted)
/// • **downloading** → circular progress + percentage
/// • **completed**   → filled check-circle (green)
/// • **error**       → retry icon (warning orange)
///
/// For guests the button still renders, but tapping shows a [_GuestLoginSheet]
/// instead of starting the download.
class DownloadIconButton extends ConsumerWidget {
  final SongModel song;
  final double size;

  const DownloadIconButton({
    super.key,
    required this.song,
    this.size = 22,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isGuest = ref.watch(authProvider).isGuest;
    final mediaId = song.id.toString();
    final dlState = ref.watch(downloadControllerProvider);
    final status = dlState.statusOf(mediaId);
    final progress = dlState.progressOf(mediaId);

    void handleTap() {
      if (isGuest) {
        _GuestLoginSheet.show(context);
        return;
      }
      ref.read(downloadControllerProvider.notifier).download(song);
    }

    void handleCancelTap() {
      if (isGuest) {
        _GuestLoginSheet.show(context);
        return;
      }
      ref.read(downloadControllerProvider.notifier).cancelDownload(mediaId);
    }

    return SizedBox(
      width: size + 8,
      height: size + 8,
      child: switch (status) {
        DownloadStatus.idle || DownloadStatus.error => _IdleButton(
            size: size,
            isError: status == DownloadStatus.error,
            onTap: handleTap,
          ),
        DownloadStatus.downloading => _ProgressButton(
            size: size,
            progress: progress,
            onTap: handleCancelTap,
          ),
        DownloadStatus.completed => _DoneButton(size: size),
      },
    );
  }
}

// ---------------------------------------------------------------------------
// Guest login prompt bottom sheet
// ---------------------------------------------------------------------------

class _GuestLoginSheet extends StatelessWidget {
  const _GuestLoginSheet();

  static void show(BuildContext context) {
    showModalBottomSheet<void>(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (_) => const _GuestLoginSheet(),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      // Rises above the keyboard if one were to appear.
      padding: EdgeInsets.only(
        bottom: MediaQuery.viewInsetsOf(context).bottom,
      ),
      child: ClipRRect(
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 20, sigmaY: 20),
          child: Container(
            decoration: const BoxDecoration(
              color: Color(0xE6111114),
              borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
              border: Border(
                top: BorderSide(color: AppColors.glassBorder, width: 1),
              ),
            ),
            padding: const EdgeInsets.fromLTRB(28, 12, 28, 36),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Handle
                Container(
                  width: 36,
                  height: 4,
                  margin: const EdgeInsets.only(bottom: 24),
                  decoration: BoxDecoration(
                    color: AppColors.glassBorder,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),

                // Icon
                Container(
                  width: 64,
                  height: 64,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    gradient: const LinearGradient(
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                      colors: [AppColors.primary, AppColors.accent],
                    ),
                    boxShadow: [
                      BoxShadow(
                        color: AppColors.primary.withValues(alpha: 0.3),
                        blurRadius: 20,
                        offset: const Offset(0, 6),
                      ),
                    ],
                  ),
                  child: const Icon(
                    Icons.download_rounded,
                    color: AppColors.onPrimary,
                    size: 30,
                  ),
                ),
                const SizedBox(height: 20),

                // Title
                const Text(
                  'Login to Download',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.w800,
                    color: AppColors.onSurface,
                    letterSpacing: -0.3,
                  ),
                ),
                const SizedBox(height: 10),

                // Body
                const Text(
                  'Experience high-quality offline music by joining us.\n'
                  'Downloads are encrypted and only accessible in the app.',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 13,
                    height: 1.55,
                    color: AppColors.onSurfaceMuted,
                  ),
                ),
                const SizedBox(height: 28),

                // Login button
                SizedBox(
                  width: double.infinity,
                  height: 52,
                  child: GestureDetector(
                    onTap: () {
                      Navigator.of(context).pop();
                      context.go('/login');
                    },
                    child: Container(
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(14),
                        gradient: const LinearGradient(
                          colors: [AppColors.primary, AppColors.primaryDim],
                        ),
                        boxShadow: [
                          BoxShadow(
                            color: AppColors.primary.withValues(alpha: 0.28),
                            blurRadius: 16,
                            offset: const Offset(0, 5),
                          ),
                        ],
                      ),
                      alignment: Alignment.center,
                      child: const Text(
                        'Login Now',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.w700,
                          color: AppColors.onPrimary,
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 12),

                // Dismiss
                TextButton(
                  onPressed: () => Navigator.of(context).pop(),
                  child: const Text(
                    'Maybe later',
                    style: TextStyle(
                      color: AppColors.onSurfaceMuted,
                      fontSize: 13,
                    ),
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
// Sub-states (unchanged)
// ---------------------------------------------------------------------------

class _IdleButton extends StatelessWidget {
  final double size;
  final bool isError;
  final VoidCallback onTap;
  const _IdleButton(
      {required this.size, required this.isError, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      behavior: HitTestBehavior.opaque,
      child: Center(
        child: Icon(
          Icons.download_outlined,
          size: size,
          color: isError ? AppColors.warning : AppColors.onSurfaceMuted,
        ),
      ),
    );
  }
}

class _ProgressButton extends StatelessWidget {
  final double size;
  final double progress;
  final VoidCallback onTap;
  const _ProgressButton(
      {required this.size, required this.progress, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final pct = (progress * 100).round();
    return GestureDetector(
      onTap: onTap,
      behavior: HitTestBehavior.opaque,
      child: Center(
        child: SizedBox(
          width: size,
          height: size,
          child: Stack(
            alignment: Alignment.center,
            children: [
              CircularProgressIndicator(
                value: progress,
                strokeWidth: 2,
                color: AppColors.primary,
                backgroundColor: AppColors.glassBorder,
              ),
              Text(
                '$pct',
                style: TextStyle(
                  fontSize: size * 0.3,
                  fontWeight: FontWeight.w700,
                  color: AppColors.primary,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _DoneButton extends StatelessWidget {
  final double size;
  const _DoneButton({required this.size});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Icon(
        Icons.check_circle_rounded,
        size: size,
        color: AppColors.success,
      ),
    );
  }
}
