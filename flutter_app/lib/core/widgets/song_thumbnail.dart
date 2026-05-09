import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../theme/app_colors.dart';

/// Cached network image thumbnail with a graceful placeholder/error fallback.
/// Drop-in replacement for bare `Image.network` calls across the app.
class SongThumbnail extends StatelessWidget {
  final String? url;
  final BoxFit fit;
  final Widget? fallback;

  const SongThumbnail({
    super.key,
    required this.url,
    this.fit = BoxFit.cover,
    this.fallback,
  });

  @override
  Widget build(BuildContext context) {
    final resolvedUrl = url;
    if (resolvedUrl == null || resolvedUrl.isEmpty) {
      return _Fallback(custom: fallback);
    }

    return CachedNetworkImage(
      imageUrl: resolvedUrl,
      fit: fit,
      fadeInDuration: const Duration(milliseconds: 200),
      placeholder: (BuildContext ctx, String url) => Container(
        color: AppColors.surfaceOne,
        child: const Center(
          child: SizedBox(
            width: 20,
            height: 20,
            child: CircularProgressIndicator(
              strokeWidth: 1.5,
              color: AppColors.primary,
            ),
          ),
        ),
      ),
      errorWidget: (BuildContext ctx, String url, Object err) =>
          _Fallback(custom: fallback),
    );
  }
}

class _Fallback extends StatelessWidget {
  final Widget? custom;
  const _Fallback({this.custom});

  @override
  Widget build(BuildContext context) {
    return custom ??
        Container(
          color: AppColors.surfaceTwo,
          child: const Icon(
            Icons.music_note_rounded,
            color: AppColors.onSurfaceMuted,
          ),
        );
  }
}
