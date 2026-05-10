import 'package:flutter/material.dart';

import '../theme/app_colors.dart';
import '../utils/play_count_format.dart';

/// Headphones + compact play count (Spotify-style track meta row).
class SongPlayCountRow extends StatelessWidget {
  final int totalViews;
  final double iconSize;
  final double fontSize;

  const SongPlayCountRow({
    super.key,
    required this.totalViews,
    this.iconSize = 13,
    this.fontSize = 12,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(
          Icons.headphones_rounded,
          size: iconSize,
          color: AppColors.onSurfaceMuted.withValues(alpha: 0.85),
        ),
        const SizedBox(width: 4),
        Text(
          formatCompactPlayCount(totalViews),
          style: TextStyle(
            fontSize: fontSize,
            fontWeight: FontWeight.w600,
            letterSpacing: 0.15,
            color: AppColors.onSurfaceMuted.withValues(alpha: 0.92),
          ),
        ),
      ],
    );
  }
}
