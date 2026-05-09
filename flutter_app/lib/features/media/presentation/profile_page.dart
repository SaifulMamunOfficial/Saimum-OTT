import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/providers/auth_provider.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/glass_card.dart';

class ProfilePage extends ConsumerWidget {
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final auth = ref.watch(authProvider);

    return CustomScrollView(
      slivers: [
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 56, 20, 24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('Profile',
                    style: TextStyle(
                        fontSize: 28,
                        fontWeight: FontWeight.w800,
                        color: AppColors.onSurface,
                        letterSpacing: -0.5)),
                const SizedBox(height: 24),
                GlassCard(
                  child: switch (auth.status) {
                    AuthStatus.loading => const Center(
                        child: SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(strokeWidth: 2))),
                    AuthStatus.authenticated => Row(
                        children: [
                          Container(
                            width: 48,
                            height: 48,
                            decoration: const BoxDecoration(
                              color: AppColors.glassFill,
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(Icons.person,
                                color: AppColors.primary),
                          ),
                          const SizedBox(width: 14),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  auth.userName ?? 'User',
                                  style: const TextStyle(
                                      fontWeight: FontWeight.w700,
                                      fontSize: 15,
                                      color: AppColors.onSurface),
                                ),
                                Text(
                                  auth.userEmail ?? '',
                                  style: const TextStyle(
                                      fontSize: 12,
                                      color: AppColors.onSurfaceMuted),
                                ),
                              ],
                            ),
                          ),
                          TextButton(
                            onPressed: () =>
                                ref.read(authProvider.notifier).logout(),
                            child: const Text('Logout'),
                          ),
                        ],
                      ),
                    _ => ElevatedButton.icon(
                        onPressed: () => ref
                            .read(authProvider.notifier)
                            .login('test@test.com', 'password'),
                        icon: const Icon(Icons.login),
                        label: const Text('Login'),
                      ),
                  },
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
