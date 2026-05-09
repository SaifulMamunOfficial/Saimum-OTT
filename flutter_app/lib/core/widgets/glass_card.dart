import 'dart:ui';

import 'package:flutter/material.dart';

import '../theme/app_colors.dart';

/// A frosted-glass card using [BackdropFilter].
/// The parent must have a non-transparent background for the blur to be visible.
class GlassCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry padding;
  final BorderRadius borderRadius;
  final double blur;
  final Color fill;
  final Color borderColor;
  final double borderWidth;

  const GlassCard({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(16),
    this.borderRadius = const BorderRadius.all(Radius.circular(20)),
    this.blur = 12,
    this.fill = AppColors.glassFill,
    this.borderColor = AppColors.glassBorder,
    this.borderWidth = 1,
  });

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: borderRadius,
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: blur, sigmaY: blur),
        child: Container(
          decoration: BoxDecoration(
            color: fill,
            borderRadius: borderRadius,
            border: Border.all(color: borderColor, width: borderWidth),
          ),
          padding: padding,
          child: child,
        ),
      ),
    );
  }
}

/// A glass card with a subtle top-highlight stroke — use for "active" state.
class GlassCardAccent extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry padding;
  final BorderRadius borderRadius;

  const GlassCardAccent({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(16),
    this.borderRadius = const BorderRadius.all(Radius.circular(20)),
  });

  @override
  Widget build(BuildContext context) {
    return GlassCard(
      padding: padding,
      borderRadius: borderRadius,
      fill: AppColors.glassFill,
      borderColor: AppColors.primary.withValues(alpha: 0.4),
      borderWidth: 1.2,
      child: child,
    );
  }
}
