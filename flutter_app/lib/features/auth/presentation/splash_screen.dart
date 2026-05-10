import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/providers/auth_provider.dart';
import '../../../core/constants/app_constants.dart';
import '../../../core/theme/app_colors.dart';

class SplashScreen extends ConsumerStatefulWidget {
  const SplashScreen({super.key});

  @override
  ConsumerState<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends ConsumerState<SplashScreen>
    with TickerProviderStateMixin {
  late final AnimationController _logoCtrl;
  late final AnimationController _shimmerCtrl;

  late final Animation<double> _logoScale;
  late final Animation<double> _logoFade;
  late final Animation<double> _taglineFade;
  late final Animation<double> _taglineSlide;

  @override
  void initState() {
    super.initState();

    // Logo: scale 0.6→1.0 + fade 0→1, 800ms ease-out
    _logoCtrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 900),
    );
    _logoScale = Tween<double>(begin: 0.55, end: 1.0).animate(
      CurvedAnimation(parent: _logoCtrl, curve: Curves.easeOutBack),
    );
    _logoFade = Tween<double>(begin: 0, end: 1).animate(
      CurvedAnimation(
        parent: _logoCtrl,
        curve: const Interval(0.0, 0.6, curve: Curves.easeOut),
      ),
    );

    // Tagline: fade + upward slide, starts after logo at 600ms
    _taglineFade = Tween<double>(begin: 0, end: 1).animate(
      CurvedAnimation(
        parent: _logoCtrl,
        curve: const Interval(0.55, 1.0, curve: Curves.easeOut),
      ),
    );
    _taglineSlide = Tween<double>(begin: 18, end: 0).animate(
      CurvedAnimation(
        parent: _logoCtrl,
        curve: const Interval(0.55, 1.0, curve: Curves.easeOut),
      ),
    );

    // Shimmer bar at the bottom
    _shimmerCtrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1400),
    )..repeat();

    _logoCtrl.forward();
    _startNavigationTimer();
  }

  Future<void> _startNavigationTimer() async {
    // Wait for both: minimum splash duration AND auth resolution.
    await Future.wait([
      Future<void>.delayed(const Duration(milliseconds: 2800)),
      _waitForAuthResolution(),
    ]);
    if (!mounted) return;
    _navigateNext();
  }

  Future<void> _waitForAuthResolution() async {
    while (ref.read(authProvider).status == AuthStatus.loading) {
      await Future<void>.delayed(const Duration(milliseconds: 80));
    }
  }

  Future<void> _navigateNext() async {
    final auth = ref.read(authProvider);
    final repo = ref.read(authRepositoryProvider);

    if (auth.isAuthenticated) {
      context.go('/');
      return;
    }

    final seen = await repo.hasSeenOnboarding();
    if (!mounted) return;
    context.go(seen ? '/' : '/onboarding');
  }

  @override
  void dispose() {
    _logoCtrl.dispose();
    _shimmerCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      body: Stack(
        children: [
          // ── Background orbs ────────────────────────────────────────────────
          _BackgroundOrbs(),

          // ── Centred logo + tagline ─────────────────────────────────────────
          Center(
            child: AnimatedBuilder(
              animation: _logoCtrl,
              builder: (_, _) {
                return Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    // Logo mark
                    FadeTransition(
                      opacity: _logoFade,
                      child: ScaleTransition(
                        scale: _logoScale,
                        child: _LogoMark(),
                      ),
                    ),
                    const SizedBox(height: 20),

                    // App name
                    FadeTransition(
                      opacity: _logoFade,
                      child: const Text(
                        AppConstants.appName,
                        style: TextStyle(
                          fontSize: 28,
                          fontWeight: FontWeight.w800,
                          color: AppColors.onSurface,
                          letterSpacing: -0.5,
                        ),
                      ),
                    ),
                    const SizedBox(height: 8),

                    // Tagline
                    FadeTransition(
                      opacity: _taglineFade,
                      child: Transform.translate(
                        offset: Offset(0, _taglineSlide.value),
                        child: const Text(
                          'Music · Video · Learning',
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w400,
                            color: AppColors.onSurfaceMuted,
                            letterSpacing: 2.5,
                          ),
                        ),
                      ),
                    ),
                  ],
                );
              },
            ),
          ),

          // ── Loading shimmer at the bottom ─────────────────────────────────
          Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            child: _ShimmerBar(controller: _shimmerCtrl),
          ),
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Logo mark
// ---------------------------------------------------------------------------

class _LogoMark extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      width: 96,
      height: 96,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [AppColors.primary, AppColors.accent],
        ),
        boxShadow: [
          BoxShadow(
            color: AppColors.primary.withValues(alpha: 0.35),
            blurRadius: 32,
            spreadRadius: 2,
          ),
          BoxShadow(
            color: AppColors.accent.withValues(alpha: 0.25),
            blurRadius: 24,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: const Icon(
        Icons.music_note_rounded,
        color: AppColors.onPrimary,
        size: 44,
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Background orbs (same as AppShell)
// ---------------------------------------------------------------------------

class _BackgroundOrbs extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.sizeOf(context);
    return SizedBox.expand(
      child: Stack(
        children: [
          Positioned(
            top: -80,
            left: -60,
            child: _orb(220, AppColors.primary.withValues(alpha: 0.12)),
          ),
          Positioned(
            bottom: -60,
            right: -40,
            child: _orb(240, AppColors.accent.withValues(alpha: 0.17)),
          ),
          Positioned(
            top: size.height * 0.45,
            left: size.width * 0.25,
            child: _orb(160, AppColors.primary.withValues(alpha: 0.06)),
          ),
        ],
      ),
    );
  }

  Widget _orb(double s, Color c) => ImageFiltered(
        imageFilter: ImageFilter.blur(sigmaX: 60, sigmaY: 60),
        child: Container(
          width: s,
          height: s,
          decoration: BoxDecoration(color: c, shape: BoxShape.circle),
        ),
      );
}

// ---------------------------------------------------------------------------
// Animated shimmer bar (bottom loading indicator)
// ---------------------------------------------------------------------------

class _ShimmerBar extends AnimatedWidget {
  const _ShimmerBar({required AnimationController controller})
      : super(listenable: controller);

  @override
  Widget build(BuildContext context) {
    final t = (listenable as AnimationController).value;
    const barWidth = 120.0;
    final screenWidth = MediaQuery.sizeOf(context).width;
    final x = (t * (screenWidth + barWidth)) - barWidth;

    return SizedBox(
      height: 3,
      child: Stack(
        children: [
          Container(height: 3, color: AppColors.glassFill),
          Positioned(
            left: x,
            child: Container(
              width: barWidth,
              height: 3,
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    Colors.transparent,
                    AppColors.primary,
                    Colors.transparent,
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
