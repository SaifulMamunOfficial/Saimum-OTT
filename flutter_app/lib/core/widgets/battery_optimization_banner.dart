import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../theme/app_colors.dart';
import '../utils/battery_optimization_service.dart';

// ---------------------------------------------------------------------------
// Provider — resolves once per session; the user can re-check via refresh.
// ---------------------------------------------------------------------------

final _batteryOptimizedProvider = FutureProvider<bool>((ref) async {
  return BatteryOptimizationService.instance.isIgnoringBatteryOptimizations();
});

// ---------------------------------------------------------------------------
// Banner widget — only renders on Android when battery is NOT whitelisted.
// ---------------------------------------------------------------------------

/// Shows a dismissible amber warning banner when Android is throttling
/// background execution for this app.  Tapping "FIX NOW" launches the system
/// battery-optimisation dialog.
class BatteryOptimizationBanner extends ConsumerStatefulWidget {
  const BatteryOptimizationBanner({super.key});

  @override
  ConsumerState<BatteryOptimizationBanner> createState() =>
      _BatteryOptimizationBannerState();
}

class _BatteryOptimizationBannerState
    extends ConsumerState<BatteryOptimizationBanner> {
  bool _dismissed = false;

  @override
  Widget build(BuildContext context) {
    if (!Platform.isAndroid || _dismissed) return const SizedBox.shrink();

    final asyncValue = ref.watch(_batteryOptimizedProvider);

    return asyncValue.when(
      data: (isWhitelisted) {
        if (isWhitelisted) return const SizedBox.shrink();
        return _Banner(
          onFix: () async {
            await BatteryOptimizationService.instance.requestWhitelisting();
            // Re-check after user has interacted with the system dialog.
            // ignore: unused_result
            ref.invalidate(_batteryOptimizedProvider);
          },
          onDismiss: () => setState(() => _dismissed = true),
        );
      },
      loading: () => const SizedBox.shrink(),
      error: (Object e, StackTrace s) => const SizedBox.shrink(),
    );
  }
}

class _Banner extends StatelessWidget {
  final VoidCallback onFix;
  final VoidCallback onDismiss;

  const _Banner({required this.onFix, required this.onDismiss});

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.fromLTRB(16, 10, 8, 10),
        color: AppColors.warning.withValues(alpha: 0.95),
        child: SafeArea(
          bottom: false,
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Icon(Icons.battery_alert_rounded,
                  color: Colors.black87, size: 18),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Text(
                      'Background playback may be restricted',
                      style: TextStyle(
                          fontWeight: FontWeight.w700,
                          fontSize: 12,
                          color: Colors.black87),
                    ),
                    const SizedBox(height: 2),
                    const Text(
                      'Disable battery optimisation so music & downloads '
                      'keep running with the screen off.',
                      style: TextStyle(fontSize: 11, color: Colors.black87),
                    ),
                    const SizedBox(height: 6),
                    TextButton(
                      onPressed: onFix,
                      style: TextButton.styleFrom(
                        padding: EdgeInsets.zero,
                        minimumSize: Size.zero,
                        tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                        foregroundColor: AppColors.background,
                      ),
                      child: const Text(
                        'FIX NOW →',
                        style: TextStyle(
                            fontWeight: FontWeight.w800, fontSize: 12),
                      ),
                    ),
                  ],
                ),
              ),
              IconButton(
                icon: const Icon(Icons.close_rounded,
                    color: Colors.black87, size: 18),
                onPressed: onDismiss,
                padding: EdgeInsets.zero,
                constraints: const BoxConstraints(),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
