import 'dart:convert';
import 'dart:typed_data';

import 'package:crypto/crypto.dart';
import 'package:encrypt/encrypt.dart';

/// Singleton AES-256-CBC encryption service.
///
/// Key derivation: HMAC-SHA256(appSecret + mediaId) → 32-byte key.
/// The raw key is never stored on disk; it is derived at runtime on every use.
class EncryptionService {
  EncryptionService._();
  static final EncryptionService instance = EncryptionService._();

  // Non-sensitive salt mixed with mediaId. Not a user secret.
  static const _kAppSecret = 'saimum_media_salt_v1_7Xkp2z9Q';

  // ---------------------------------------------------------------------------
  // Public API
  // ---------------------------------------------------------------------------

  /// Generates a fresh 16-byte random IV for a new encryption session.
  IV generateIV() => IV.fromSecureRandom(16);

  /// Derives a 256-bit AES key from [mediaId] using HMAC-SHA256.
  /// The key exists only in memory for the duration of the call.
  Key deriveKey(String mediaId) {
    final hmac = Hmac(sha256, utf8.encode(_kAppSecret));
    final digest = hmac.convert(utf8.encode(mediaId));
    return Key(Uint8List.fromList(digest.bytes));
  }

  /// Encrypts [plainBytes] under [key] and [iv] using AES-256-CBC + PKCS7.
  /// Returns only the cipher bytes (IV is NOT prepended — it lives in the manifest).
  Uint8List encryptChunk(Uint8List plainBytes, Key key, IV iv) {
    final encrypter = Encrypter(AES(key, mode: AESMode.cbc));
    return encrypter.encryptBytes(plainBytes, iv: iv).bytes;
  }

  /// Decrypts [cipherBytes] back to plaintext using the same [key] and [iv].
  Uint8List decryptChunk(Uint8List cipherBytes, Key key, IV iv) {
    final encrypter = Encrypter(AES(key, mode: AESMode.cbc));
    final decrypted = encrypter.decryptBytes(Encrypted(cipherBytes), iv: iv);
    return Uint8List.fromList(decrypted);
  }
}
