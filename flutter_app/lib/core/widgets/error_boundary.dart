import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

import '../theme/app_colors.dart';

/// Branded "something went wrong" screen that replaces the default red
/// debug-error overlay in both debug and release builds.
///
/// Shown whenever `ErrorWidget.builder` is triggered, e.g. a widget's
/// `build()` method throws an unhandled exception.
class AppErrorScreen extends StatelessWidget {
  final FlutterErrorDetails details;
  const AppErrorScreen({super.key, required this.details});

  @override
  Widget build(BuildContext context) {
    return Directionality(
      textDirection: TextDirection.ltr,
      child: Scaffold(
        backgroundColor: AppColors.background,
        body: SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 32),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                // Icon
                Container(
                  width: 88,
                  height: 88,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: AppColors.primary.withValues(alpha: 0.12),
                    border: Border.all(
                      color: AppColors.primary.withValues(alpha: 0.3),
                      width: 1.5,
                    ),
                  ),
                  child: const Icon(
                    Icons.error_outline_rounded,
                    color: AppColors.primary,
                    size: 40,
                  ),
                ),
                const SizedBox(height: 28),

                // Title
                const Text(
                  'Something went wrong',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    color: AppColors.onSurface,
                    fontSize: 22,
                    fontWeight: FontWeight.w700,
                    letterSpacing: -0.5,
                  ),
                ),
                const SizedBox(height: 12),

                // Subtitle
                Text(
                  'Saimum Music encountered an unexpected error.\n'
                  'Please restart the app. The issue has been logged.',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    color: AppColors.onSurface.withValues(alpha: 0.6),
                    fontSize: 14,
                    height: 1.5,
                  ),
                ),

                // Debug-only error detail
                if (kDebugMode) ...[
                  const SizedBox(height: 24),
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(14),
                    decoration: BoxDecoration(
                      color: AppColors.surfaceOne,
                      borderRadius: BorderRadius.circular(12),
                      border:
                          Border.all(color: AppColors.glassBorder, width: 1),
                    ),
                    child: Text(
                      details.exceptionAsString(),
                      style: const TextStyle(
                        color: AppColors.onSurfaceMuted,
                        fontSize: 11,
                        fontFamily: 'monospace',
                      ),
                      maxLines: 6,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ],
            ),
          ),
        ),
      ),
    );
  }
}
