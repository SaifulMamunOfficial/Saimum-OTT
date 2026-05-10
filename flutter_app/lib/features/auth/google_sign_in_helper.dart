import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_sign_in/google_sign_in.dart';

/// Starts Google account picker + OAuth consent (native SDK).
///
/// Laravel token exchange is backend-dependent — wire `/auth/google` when ready.
class AuthGoogleSignInHelper {
  AuthGoogleSignInHelper._();

  static final GoogleSignIn _gsi = GoogleSignIn(
    scopes: const ['email', 'profile'],
  );

  /// Returns Google profile info after interactive sign-in, or `null` if cancelled.
  /// Throws [PlatformException] on misconfigured OAuth clients.
  static Future<GoogleSignInAccount?> pickAccount() async {
    return _gsi.signIn();
  }

  static String readableError(Object e) {
    if (e is PlatformException) {
      return e.message ?? 'Google Sign-In failed.';
    }
    return 'Google Sign-In failed.';
  }

  static void showSnack(BuildContext context, String message) {
    ScaffoldMessenger.of(context).hideCurrentSnackBar();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        behavior: SnackBarBehavior.floating,
        content: Text(message),
      ),
    );
  }
}
