import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/providers/auth_provider.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/glass_card.dart';
import '../widgets/auth_widgets.dart';

class SignupPage extends ConsumerStatefulWidget {
  const SignupPage({super.key});

  @override
  ConsumerState<SignupPage> createState() => _SignupPageState();
}

class _SignupPageState extends ConsumerState<SignupPage>
    with SingleTickerProviderStateMixin {
  final _formKey = GlobalKey<FormState>();
  final _nameCtrl = TextEditingController();
  final _emailCtrl = TextEditingController();
  final _passCtrl = TextEditingController();
  final _confirmPassCtrl = TextEditingController();
  bool _obscurePass = true;
  bool _obscureConfirm = true;

  late final AnimationController _entryCtrl;
  late final Animation<double> _fade;
  late final Animation<Offset> _slide;

  @override
  void initState() {
    super.initState();
    _entryCtrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 600),
    )..forward();
    _fade = CurvedAnimation(parent: _entryCtrl, curve: Curves.easeOut);
    _slide = Tween<Offset>(
      begin: const Offset(0, 0.06),
      end: Offset.zero,
    ).animate(CurvedAnimation(parent: _entryCtrl, curve: Curves.easeOut));
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    _emailCtrl.dispose();
    _passCtrl.dispose();
    _confirmPassCtrl.dispose();
    _entryCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!(_formKey.currentState?.validate() ?? false)) return;
    FocusScope.of(context).unfocus();
    await ref.read(authProvider.notifier).register(
          name: _nameCtrl.text.trim(),
          email: _emailCtrl.text.trim(),
          password: _passCtrl.text.trim(),
        );
  }

  void _continueAsGuest() {
    FocusScope.of(context).unfocus();
    context.go('/');
  }

  void _goToLogin() {
    FocusScope.of(context).unfocus();
    context.go('/login');
  }

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authProvider);
    final isLoading = authState.isLoading;

    ref.listen<AuthState>(authProvider, (_, next) {
      if (next.isAuthenticated) {
        context.go('/');
      }
    });

    return Scaffold(
      backgroundColor: AppColors.background,
      resizeToAvoidBottomInset: true,
      body: Stack(
        children: [
          const AuthBackground(),
          SafeArea(
            child: LayoutBuilder(
              builder: (context, constraints) {
                return SingleChildScrollView(
                  padding: const EdgeInsets.symmetric(horizontal: 24),
                  child: ConstrainedBox(
                    constraints:
                        BoxConstraints(minHeight: constraints.maxHeight),
                    child: FadeTransition(
                      opacity: _fade,
                      child: SlideTransition(
                        position: _slide,
                        child: Center(
                          child: ConstrainedBox(
                            constraints: const BoxConstraints(maxWidth: 420),
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              crossAxisAlignment: CrossAxisAlignment.stretch,
                              children: [
                                const SizedBox(height: 12),

                                const Center(child: AuthBrandHero()),
                                const SizedBox(height: 36),

                                const Text(
                                  'Create account',
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                    fontSize: 30,
                                    fontWeight: FontWeight.w800,
                                    color: AppColors.onSurface,
                                    letterSpacing: -0.6,
                                  ),
                                ),
                                const SizedBox(height: 6),
                                const Text(
                                  "Join Saimum Music — it's free forever.",
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                    fontSize: 14,
                                    color: AppColors.onSurfaceMuted,
                                  ),
                                ),
                                const SizedBox(height: 26),

                                GlassCard(
                                  padding: const EdgeInsets.all(22),
                                  child: Form(
                                    key: _formKey,
                                    child: Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        AuthInputField(
                                          controller: _nameCtrl,
                                          label: 'Full name',
                                          hint: 'Your name',
                                          prefixIcon:
                                              Icons.person_outline_rounded,
                                          validator: (v) =>
                                              (v == null || v.trim().isEmpty)
                                                  ? 'Name is required'
                                                  : null,
                                        ),
                                        const SizedBox(height: 16),
                                        AuthInputField(
                                          controller: _emailCtrl,
                                          label: 'Email address',
                                          hint: 'you@example.com',
                                          keyboardType:
                                              TextInputType.emailAddress,
                                          prefixIcon: Icons.email_outlined,
                                          validator: _validateEmail,
                                        ),
                                        const SizedBox(height: 16),
                                        AuthInputField(
                                          controller: _passCtrl,
                                          label: 'Password',
                                          hint: '••••••••',
                                          obscureText: _obscurePass,
                                          prefixIcon:
                                              Icons.lock_outline_rounded,
                                          suffixIcon: IconButton(
                                            icon: Icon(
                                              _obscurePass
                                                  ? Icons
                                                      .visibility_off_outlined
                                                  : Icons.visibility_outlined,
                                              color: AppColors.onSurfaceMuted,
                                              size: 20,
                                            ),
                                            onPressed: () => setState(() =>
                                                _obscurePass = !_obscurePass),
                                          ),
                                          validator: _validatePassword,
                                        ),
                                        const SizedBox(height: 16),
                                        AuthInputField(
                                          controller: _confirmPassCtrl,
                                          label: 'Confirm password',
                                          hint: '••••••••',
                                          obscureText: _obscureConfirm,
                                          prefixIcon:
                                              Icons.lock_outline_rounded,
                                          textInputAction:
                                              TextInputAction.done,
                                          suffixIcon: IconButton(
                                            icon: Icon(
                                              _obscureConfirm
                                                  ? Icons
                                                      .visibility_off_outlined
                                                  : Icons.visibility_outlined,
                                              color: AppColors.onSurfaceMuted,
                                              size: 20,
                                            ),
                                            onPressed: () => setState(() =>
                                                _obscureConfirm =
                                                    !_obscureConfirm),
                                          ),
                                          validator: (v) {
                                            if (v == null || v.isEmpty) {
                                              return 'Please confirm your password';
                                            }
                                            if (v != _passCtrl.text) {
                                              return 'Passwords do not match';
                                            }
                                            return null;
                                          },
                                        ),
                                      ],
                                    ),
                                  ),
                                ),

                                if (authState.status == AuthStatus.error &&
                                    authState.errorMessage != null) ...[
                                  const SizedBox(height: 14),
                                  AuthErrorBanner(
                                      message: authState.errorMessage!),
                                ],

                                const SizedBox(height: 22),

                                SizedBox(
                                  height: 54,
                                  child: AuthPrimaryButton(
                                    label: 'Create Account',
                                    isLoading: isLoading,
                                    onTap: isLoading ? null : _submit,
                                  ),
                                ),

                                const SizedBox(height: 22),

                                Center(
                                  child: RichText(
                                    textAlign: TextAlign.center,
                                    text: TextSpan(
                                      style: const TextStyle(
                                        fontSize: 13,
                                        color: AppColors.onSurfaceMuted,
                                      ),
                                      children: [
                                        const TextSpan(
                                          text: 'Already have an account? ',
                                        ),
                                        WidgetSpan(
                                          alignment:
                                              PlaceholderAlignment.middle,
                                          child: GestureDetector(
                                            onTap: _goToLogin,
                                            child: const Text(
                                              'Sign In',
                                              style: TextStyle(
                                                fontSize: 13,
                                                color: AppColors.primary,
                                                fontWeight: FontWeight.w700,
                                              ),
                                            ),
                                          ),
                                        ),
                                      ],
                                    ),
                                  ),
                                ),

                                const SizedBox(height: 8),

                                Center(
                                  child: ContinueAsGuestButton(
                                    onTap: _continueAsGuest,
                                  ),
                                ),

                                const SizedBox(height: 24),
                              ],
                            ),
                          ),
                        ),
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  String? _validateEmail(String? v) {
    if (v == null || v.trim().isEmpty) return 'Email is required';
    final regex = RegExp(r'^[\w.+-]+@[\w-]+\.[a-zA-Z]{2,}$');
    if (!regex.hasMatch(v.trim())) return 'Enter a valid email';
    return null;
  }

  String? _validatePassword(String? v) {
    if (v == null || v.isEmpty) return 'Password is required';
    if (v.length < 6) return 'At least 6 characters required';
    return null;
  }
}
