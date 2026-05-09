import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/theme/app_colors.dart';
import '../controllers/download_controller.dart';

/// Tri-state download button for a [SongModel]:
///
/// • **idle**        → outline download icon (muted)
/// • **downloading** → circular progress + percentage
/// • **completed**   → filled check-circle (green)
/// • **error**       → retry icon (warning orange)
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
    final mediaId = song.id.toString();
    final dlState = ref.watch(downloadControllerProvider);
    final status = dlState.statusOf(mediaId);
    final progress = dlState.progressOf(mediaId);

    return SizedBox(
      width: size + 8,
      height: size + 8,
      child: switch (status) {
        DownloadStatus.idle || DownloadStatus.error => _IdleButton(
            size: size,
            isError: status == DownloadStatus.error,
            onTap: () => ref
                .read(downloadControllerProvider.notifier)
                .download(song),
          ),
        DownloadStatus.downloading => _ProgressButton(
            size: size,
            progress: progress,
            onTap: () => ref
                .read(downloadControllerProvider.notifier)
                .cancelDownload(mediaId),
          ),
        DownloadStatus.completed => _DoneButton(size: size),
      },
    );
  }
}

// ---------------------------------------------------------------------------
// Sub-states
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
          isError
              ? Icons.download_outlined
              : Icons.download_outlined,
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
