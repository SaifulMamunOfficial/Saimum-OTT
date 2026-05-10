import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/providers/auth_provider.dart';
import '../../../core/theme/app_colors.dart';

// ---------------------------------------------------------------------------
// Data
// ---------------------------------------------------------------------------

class _OnboardingPage {
  final IconData icon;
  final Color iconColor;
  final Color glowColor;
  final String title;
  final String subtitle;
  final List<Color> gradientColors;

  const _OnboardingPage({
    required this.icon,
    required this.iconColor,
    required this.glowColor,
    required this.title,
    required this.subtitle,
    required this.gradientColors,
  });
}

const _pages = [
  _OnboardingPage(
    icon: Icons.library_music_rounded,
    iconColor: AppColors.primary,
    glowColor: AppColors.primary,
    title: 'Unlimited Music\n& Video',
    subtitle:
        'Stream thousands of songs and videos — Bangla, Hindi, and more.'
        '\nAd-free. Forever free.',
    gradientColors: [AppColors.primary, AppColors.primaryDim],
  ),
  _OnboardingPage(
    icon: Icons.lock_rounded,
    iconColor: AppColors.accent,
    glowColor: AppColors.accent,
    title: 'Secure Offline\nDownloads',
    subtitle:
        'AES-256 encrypted downloads keep your music safe.\n'
        'Play anything, anywhere — even without internet.',
    gradientColors: [AppColors.accent, AppColors.accentDim],
  ),
  _OnboardingPage(
    icon: Icons.school_rounded,
    iconColor: Color(0xFF34C759),
    glowColor: Color(0xFF34C759),
    title: 'Student\nAcademic Hub',
    subtitle:
        'Access study resources, lecture notes, and academic tools '
        '— all in one place. Coming soon for students.',
    gradientColors: [Color(0xFF34C759), Color(0xFF28A745)],
  ),
];

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

class OnboardingScreen extends ConsumerStatefulWidget {
  const OnboardingScreen({super.key});

  @override
  ConsumerState<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends ConsumerState<OnboardingScreen>
    with SingleTickerProviderStateMixin {
  final _pageController = PageController();
  int _currentPage = 0;

  late final AnimationController _fadeCtrl;
  late final Animation<double> _fadeAnim;

  @override
  void initState() {
    super.initState();
    _fadeCtrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 500),
    )..forward();
    _fadeAnim = CurvedAnimation(parent: _fadeCtrl, curve: Curves.easeIn);
  }

  @override
  void dispose() {
    _pageController.dispose();
    _fadeCtrl.dispose();
    super.dispose();
  }

  Future<void> _completeOnboarding() async {
    final repo = ref.read(authRepositoryProvider);
    await repo.markOnboardingComplete();
    if (!mounted) return;
    context.go('/');
  }

  @override
  Widget build(BuildContext context) {
    final page = _pages[_currentPage];
    final isLast = _currentPage == _pages.length - 1;

    return Scaffold(
      backgroundColor: AppColors.background,
      body: FadeTransition(
        opacity: _fadeAnim,
        child: Stack(
          children: [
            // ── Animated background orb ──────────────────────────────────────
            AnimatedPositioned(
              duration: const Duration(milliseconds: 520),
              curve: Curves.easeInOutCubic,
              top: -100 + _currentPage * 60.0,
              left: -80 + _currentPage * 40.0,
              child: _AnimatedOrb(color: page.glowColor),
            ),
            AnimatedPositioned(
              duration: const Duration(milliseconds: 520),
              curve: Curves.easeInOutCubic,
              bottom: -80 - _currentPage * 20.0,
              right: -60 + _currentPage * 10.0,
              child: _AnimatedOrb(
                color: page.glowColor.withValues(alpha: 0.6),
                size: 200,
              ),
            ),

            // ── Page content — smooth horizontal paging ────────────────────────
            PageView.builder(
              controller: _pageController,
              physics: const BouncingScrollPhysics(),
              allowImplicitScrolling: false,
              itemCount: _pages.length,
              onPageChanged: (i) => setState(() => _currentPage = i),
              itemBuilder: (_, i) => _OnboardingPageView(page: _pages[i]),
            ),

            // ── Bottom: dots only on pages 1–2; full CTA on final page ────────
            Positioned(
              left: 24,
              right: 24,
              bottom: MediaQuery.paddingOf(context).bottom + 36,
              child: AnimatedSwitcher(
                duration: const Duration(milliseconds: 380),
                switchInCurve: Curves.easeOutCubic,
                switchOutCurve: Curves.easeInCubic,
                transitionBuilder: (child, anim) => FadeTransition(
                  opacity: anim,
                  child: SlideTransition(
                    position: Tween<Offset>(
                      begin: const Offset(0, 0.12),
                      end: Offset.zero,
                    ).animate(anim),
                    child: child,
                  ),
                ),
                child: KeyedSubtree(
                  key: ValueKey<bool>(isLast),
                  child: isLast
                      ? Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            _DotRow(
                              count: _pages.length,
                              activeIndex: _currentPage,
                              activeColor: page.iconColor,
                            ),
                            const SizedBox(height: 28),
                            SizedBox(
                              width: double.infinity,
                              height: 58,
                              child: _GradientButton(
                                label: 'Get Started',
                                colors: page.gradientColors,
                                onTap: _completeOnboarding,
                              ),
                            ),
                          ],
                        )
                      : Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            _DotRow(
                              count: _pages.length,
                              activeIndex: _currentPage,
                              activeColor: page.iconColor,
                            ),
                          ],
                        ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Dot indicators
// ---------------------------------------------------------------------------

class _DotRow extends StatelessWidget {
  final int count;
  final int activeIndex;
  final Color activeColor;

  const _DotRow({
    required this.count,
    required this.activeIndex,
    required this.activeColor,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(
        count,
        (i) => AnimatedContainer(
          duration: const Duration(milliseconds: 280),
          curve: Curves.easeOutCubic,
          margin: const EdgeInsets.symmetric(horizontal: 4),
          width: i == activeIndex ? 26 : 7,
          height: 7,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(4),
            color: i == activeIndex ? activeColor : AppColors.glassBorder,
          ),
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Single page widget
// ---------------------------------------------------------------------------

class _OnboardingPageView extends StatelessWidget {
  final _OnboardingPage page;
  const _OnboardingPageView({required this.page});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.fromLTRB(
        28,
        0,
        28,
        MediaQuery.paddingOf(context).bottom + 220,
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(28),
            child: BackdropFilter(
              filter: ImageFilter.blur(sigmaX: 16, sigmaY: 16),
              child: Container(
                width: 96,
                height: 96,
                decoration: BoxDecoration(
                  color: page.iconColor.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(28),
                  border: Border.all(
                    color: page.iconColor.withValues(alpha: 0.3),
                    width: 1.5,
                  ),
                ),
                child: Icon(page.icon, color: page.iconColor, size: 44),
              ),
            ),
          ),
          const SizedBox(height: 32),
          Text(
            page.title,
            style: const TextStyle(
              fontSize: 34,
              fontWeight: FontWeight.w800,
              color: AppColors.onSurface,
              height: 1.15,
              letterSpacing: -0.8,
            ),
          ),
          const SizedBox(height: 16),
          Text(
            page.subtitle,
            style: const TextStyle(
              fontSize: 15,
              height: 1.6,
              color: AppColors.onSurfaceMuted,
              fontWeight: FontWeight.w400,
            ),
          ),
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Animated orb
// ---------------------------------------------------------------------------

class _AnimatedOrb extends StatelessWidget {
  final Color color;
  final double size;
  const _AnimatedOrb({required this.color, this.size = 280});

  @override
  Widget build(BuildContext context) {
    return ImageFiltered(
      imageFilter: ImageFilter.blur(sigmaX: 70, sigmaY: 70),
      child: Container(
        width: size,
        height: size,
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.18),
          shape: BoxShape.circle,
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Gradient CTA button — premium sizing on final step
// ---------------------------------------------------------------------------

class _GradientButton extends StatelessWidget {
  final String label;
  final List<Color> colors;
  final VoidCallback onTap;
  const _GradientButton({
    required this.label,
    required this.colors,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(18),
          gradient: LinearGradient(
            begin: Alignment.centerLeft,
            end: Alignment.centerRight,
            colors: colors,
          ),
          boxShadow: [
            BoxShadow(
              color: colors.first.withValues(alpha: 0.38),
              blurRadius: 22,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        alignment: Alignment.center,
        child: Text(
          label,
          style: const TextStyle(
            fontSize: 17,
            fontWeight: FontWeight.w800,
            color: AppColors.onPrimary,
            letterSpacing: 0.3,
          ),
        ),
      ),
    );
  }
}
