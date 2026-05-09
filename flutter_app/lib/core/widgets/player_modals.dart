import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../theme/app_colors.dart';
import '../utils/sleep_timer_provider.dart';
import '../../features/media/controllers/media_controller.dart';

// ---------------------------------------------------------------------------
// Public entry point
// ---------------------------------------------------------------------------

Future<void> showPlayerOptionsSheet(BuildContext context) {
  return showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.transparent,
    isScrollControlled: true,
    useRootNavigator: true,
    builder: (_) => const _PlayerOptionsSheet(),
  );
}

// ---------------------------------------------------------------------------
// Sheet widget
// ---------------------------------------------------------------------------

class _PlayerOptionsSheet extends ConsumerWidget {
  const _PlayerOptionsSheet();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final speed = ref.watch(mediaControllerProvider).speed;
    final timer = ref.watch(sleepTimerProvider);

    return BackdropFilter(
      filter: ImageFilter.blur(sigmaX: 24, sigmaY: 24),
      child: Container(
        decoration: const BoxDecoration(
          color: Color(0xCC111114), // ~80% opaque surface
          borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
          border: Border(top: BorderSide(color: AppColors.glassBorder)),
        ),
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(24, 12, 24, 24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Handle
                Center(
                  child: Container(
                    width: 36,
                    height: 4,
                    decoration: BoxDecoration(
                      color: AppColors.onSurfaceMuted.withValues(alpha: 0.4),
                      borderRadius: BorderRadius.circular(2),
                    ),
                  ),
                ),
                const SizedBox(height: 20),

                // ── Speed section ──────────────────────────────────────────
                Row(
                  children: [
                    const Icon(Icons.speed_rounded,
                        color: AppColors.primary, size: 18),
                    const SizedBox(width: 8),
                    const Text(
                      'Playback Speed',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: AppColors.onSurface,
                      ),
                    ),
                    const Spacer(),
                    Text(
                      '${speed == speed.truncateToDouble() ? speed.toInt() : speed}x',
                      style: const TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                        color: AppColors.primary,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 14),
                _SpeedRow(currentSpeed: speed),

                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 20),
                  child: Divider(color: AppColors.glassBorder),
                ),

                // ── Sleep timer section ────────────────────────────────────
                Row(
                  children: [
                    const Icon(Icons.bedtime_rounded,
                        color: AppColors.accent, size: 18),
                    const SizedBox(width: 8),
                    const Text(
                      'Sleep Timer',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: AppColors.onSurface,
                      ),
                    ),
                    if (timer.isActive) ...[
                      const Spacer(),
                      _TimerBadge(label: timer.label),
                    ],
                  ],
                ),
                const SizedBox(height: 14),
                _TimerRow(timerState: timer),
                const SizedBox(height: 8),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Speed selector row
// ---------------------------------------------------------------------------

class _SpeedRow extends ConsumerWidget {
  final double currentSpeed;
  static const _speeds = [0.5, 0.75, 1.0, 1.25, 1.5, 2.0];

  const _SpeedRow({required this.currentSpeed});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: _speeds.map((s) {
        final active = (currentSpeed - s).abs() < 0.01;
        final label = s == s.truncateToDouble()
            ? '${s.toInt()}x'
            : '${s}x';
        return GestureDetector(
          onTap: () =>
              ref.read(mediaControllerProvider.notifier).setSpeed(s),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 180),
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            decoration: BoxDecoration(
              color: active
                  ? AppColors.primary.withValues(alpha: 0.2)
                  : AppColors.glassFill,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(
                color: active ? AppColors.primary : AppColors.glassBorder,
                width: active ? 1.5 : 1,
              ),
            ),
            child: Text(
              label,
              style: TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w700,
                color: active ? AppColors.primary : AppColors.onSurfaceMuted,
              ),
            ),
          ),
        );
      }).toList(),
    );
  }
}

// ---------------------------------------------------------------------------
// Timer preset row
// ---------------------------------------------------------------------------

class _TimerRow extends ConsumerWidget {
  final SleepTimerState timerState;
  static const _presets = [
    (label: '15m', duration: Duration(minutes: 15)),
    (label: '30m', duration: Duration(minutes: 30)),
    (label: '45m', duration: Duration(minutes: 45)),
    (label: '60m', duration: Duration(minutes: 60)),
    (label: '90m', duration: Duration(minutes: 90)),
  ];

  const _TimerRow({required this.timerState});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: _presets.map((p) {
            return GestureDetector(
              onTap: () =>
                  ref.read(sleepTimerProvider.notifier).setTimer(p.duration),
              child: Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                decoration: BoxDecoration(
                  color: AppColors.glassFill,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.glassBorder),
                ),
                child: Text(
                  p.label,
                  style: const TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                    color: AppColors.onSurface,
                  ),
                ),
              ),
            );
          }).toList(),
        ),
        if (timerState.isActive) ...[
          const SizedBox(height: 12),
          SizedBox(
            width: double.infinity,
            child: OutlinedButton.icon(
              onPressed: () =>
                  ref.read(sleepTimerProvider.notifier).cancelTimer(),
              icon: const Icon(Icons.cancel_outlined, size: 16),
              label: Text('Cancel Timer  (${timerState.label} remaining)'),
              style: OutlinedButton.styleFrom(
                foregroundColor: AppColors.error,
                side: const BorderSide(color: AppColors.error),
                padding: const EdgeInsets.symmetric(vertical: 12),
                textStyle: const TextStyle(
                    fontSize: 13, fontWeight: FontWeight.w600),
              ),
            ),
          ),
        ],
      ],
    );
  }
}

// ---------------------------------------------------------------------------
// Active timer badge (shown next to section title)
// ---------------------------------------------------------------------------

class _TimerBadge extends StatelessWidget {
  final String label;
  const _TimerBadge({required this.label});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 3),
      decoration: BoxDecoration(
        color: AppColors.accent.withValues(alpha: 0.2),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.accent.withValues(alpha: 0.5)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.alarm, size: 11, color: AppColors.accent),
          const SizedBox(width: 4),
          Text(label,
              style: const TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.w700,
                  color: AppColors.accent)),
        ],
      ),
    );
  }
}
