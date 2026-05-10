import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../theme/app_colors.dart';
import '../utils/sleep_timer_provider.dart';
import '../../features/media/controllers/media_controller.dart';

// ---------------------------------------------------------------------------
// Entry Points
// ---------------------------------------------------------------------------

Future<void> showPlaybackSpeedSheet(BuildContext context) {
  return showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.transparent,
    isScrollControlled: true,
    useRootNavigator: true,
    builder: (_) => const _PlaybackSpeedSheet(),
  );
}

Future<void> showSleepTimerSheet(BuildContext context) {
  return showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.transparent,
    isScrollControlled: true,
    useRootNavigator: true,
    builder: (_) => const _SleepTimerSheet(),
  );
}

// ---------------------------------------------------------------------------
// Playback Speed Sheet
// ---------------------------------------------------------------------------

class _PlaybackSpeedSheet extends ConsumerWidget {
  const _PlaybackSpeedSheet();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final speed = ref.watch(mediaControllerProvider).speed;

    return _BaseModalSheet(
      title: 'Playback Speed',
      icon: Icons.speed_rounded,
      iconColor: AppColors.primary,
      trailing: Text(
        '${speed == speed.truncateToDouble() ? speed.toInt() : speed}x',
        style: const TextStyle(
          fontSize: 13,
          fontWeight: FontWeight.w600,
          color: AppColors.primary,
        ),
      ),
      child: _SpeedRow(currentSpeed: speed),
    );
  }
}

// ---------------------------------------------------------------------------
// Sleep Timer Sheet
// ---------------------------------------------------------------------------

class _SleepTimerSheet extends ConsumerWidget {
  const _SleepTimerSheet();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final timer = ref.watch(sleepTimerProvider);

    return _BaseModalSheet(
      title: 'Sleep Timer',
      icon: Icons.bedtime_rounded,
      iconColor: AppColors.accent,
      trailing: timer.isActive ? _TimerBadge(label: timer.label) : null,
      child: _TimerRow(timerState: timer),
    );
  }
}

// ---------------------------------------------------------------------------
// Shared Base Layout
// ---------------------------------------------------------------------------

class _BaseModalSheet extends StatelessWidget {
  final String title;
  final IconData icon;
  final Color iconColor;
  final Widget? trailing;
  final Widget child;

  const _BaseModalSheet({
    required this.title,
    required this.icon,
    required this.iconColor,
    required this.child,
    this.trailing,
  });

  @override
  Widget build(BuildContext context) {
    return BackdropFilter(
      filter: ImageFilter.blur(sigmaX: 24, sigmaY: 24),
      child: Container(
        decoration: const BoxDecoration(
          color: Color(0xCC111114),
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
                Row(
                  children: [
                    Icon(icon, color: iconColor, size: 18),
                    const SizedBox(width: 8),
                    Text(
                      title,
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: AppColors.onSurface,
                      ),
                    ),
                    const Spacer(),
                    if (trailing != null) trailing!,
                  ],
                ),
                const SizedBox(height: 20),
                child,
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
// Internal Components (SpeedRow, TimerRow, etc.)
// ---------------------------------------------------------------------------

class _SpeedRow extends ConsumerWidget {
  final double currentSpeed;
  static const _speeds = [0.5, 0.75, 1.0, 1.25, 1.5, 2.0];

  const _SpeedRow({required this.currentSpeed});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Wrap(
      spacing: 12,
      runSpacing: 12,
      children: _speeds.map((s) {
        final active = (currentSpeed - s).abs() < 0.01;
        final label = s == s.truncateToDouble() ? '${s.toInt()}x' : '${s}x';
        return GestureDetector(
          onTap: () => ref.read(mediaControllerProvider.notifier).setSpeed(s),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 180),
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
            decoration: BoxDecoration(
              color: active
                  ? AppColors.primary.withValues(alpha: 0.2)
                  : AppColors.glassFill,
              borderRadius: BorderRadius.circular(14),
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
                    const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                decoration: BoxDecoration(
                  color: AppColors.glassFill,
                  borderRadius: BorderRadius.circular(14),
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
          const SizedBox(height: 20),
          SizedBox(
            width: double.infinity,
            child: OutlinedButton.icon(
              onPressed: () =>
                  ref.read(sleepTimerProvider.notifier).cancelTimer(),
              icon: const Icon(Icons.cancel_outlined, size: 16),
              label: Text('Cancel Timer (${timerState.label} remaining)'),
              style: OutlinedButton.styleFrom(
                foregroundColor: AppColors.error,
                side: const BorderSide(color: AppColors.error),
                padding: const EdgeInsets.symmetric(vertical: 14),
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14)),
                textStyle:
                    const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
              ),
            ),
          ),
        ],
      ],
    );
  }
}

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
