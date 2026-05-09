// ignore_for_file: experimental_member_use
import 'dart:async';
import 'dart:io';

import 'package:encrypt/encrypt.dart' as enc;
import 'package:flutter/foundation.dart';
import 'package:just_audio/just_audio.dart';

import 'download_service.dart';
import 'encryption_service.dart';

/// Custom [StreamAudioSource] that on-the-fly decrypts AES-256-CBC encrypted
/// chunk files (`.enc`) stored in [chunksDir].
///
/// Byte-range requests from just_audio are mapped to the minimal set of chunks
/// needed, decrypted in an isolate, sliced precisely, then streamed back.
/// Memory usage is bounded to ~1–2 decrypted chunks at a time.
class DecryptionAudioSource extends StreamAudioSource {
  final String mediaId;

  /// Absolute path to the directory holding `0.enc`, `1.enc`, …
  final String chunksDir;

  final int totalChunks;

  /// Total size of the original (unencrypted) audio file.
  final int fileSize;

  final enc.Key _key;
  final enc.IV _iv;

  DecryptionAudioSource({
    required this.mediaId,
    required this.chunksDir,
    required this.totalChunks,
    required this.fileSize,
    required enc.IV iv,
  })  : _key = EncryptionService.instance.deriveKey(mediaId),
        _iv = iv;

  // ---------------------------------------------------------------------------
  // StreamAudioSource contract
  // ---------------------------------------------------------------------------

  @override
  Future<StreamAudioResponse> request([int? start, int? end]) async {
    final from = start ?? 0;
    final to = end ?? fileSize;

    final controller = StreamController<List<int>>();
    _streamRange(from, to, controller);

    return StreamAudioResponse(
      sourceLength: fileSize,
      contentLength: to - from,
      offset: from,
      stream: controller.stream,
      contentType: 'audio/mpeg',
    );
  }

  // ---------------------------------------------------------------------------
  // Internal streaming
  // ---------------------------------------------------------------------------

  void _streamRange(
    int byteStart,
    int byteEnd,
    StreamController<List<int>> controller,
  ) {
    unawaited(_doStreamRange(byteStart, byteEnd, controller));
  }

  Future<void> _doStreamRange(
    int byteStart,
    int byteEnd,
    StreamController<List<int>> controller,
  ) async {
    try {
      final startChunk = byteStart ~/ kChunkSize;
      final endChunk = (byteEnd - 1) ~/ kChunkSize;

      for (var i = startChunk; i <= endChunk && i < totalChunks; i++) {
        final chunkFile = File('$chunksDir/$i.enc');
        final cipherBytes = await chunkFile.readAsBytes();

        // Decrypt in a compute isolate so the UI thread stays free.
        final plain = await compute(_decryptIsolate, _DecryptTask(
          cipher: cipherBytes,
          keyBytes: _key.bytes,
          ivBytes: _iv.bytes,
        ));

        // The byte range within this decrypted chunk that we actually need.
        final chunkOriginStart = i * kChunkSize;
        final chunkOriginEnd = chunkOriginStart + plain.length;

        final sliceStart = (byteStart > chunkOriginStart)
            ? byteStart - chunkOriginStart
            : 0;
        final sliceEnd = (byteEnd < chunkOriginEnd)
            ? byteEnd - chunkOriginStart
            : plain.length;

        if (sliceStart < sliceEnd) {
          controller.add(plain.sublist(sliceStart, sliceEnd));
        }
      }

      await controller.close();
    } catch (e, st) {
      controller.addError(e, st);
      await controller.close();
    }
  }

}

// ---------------------------------------------------------------------------
// Isolate payload
// ---------------------------------------------------------------------------

class _DecryptTask {
  final Uint8List cipher;
  final Uint8List keyBytes;
  final Uint8List ivBytes;
  const _DecryptTask({
    required this.cipher,
    required this.keyBytes,
    required this.ivBytes,
  });
}

Uint8List _decryptIsolate(_DecryptTask task) {
  return EncryptionService.instance.decryptChunk(
    task.cipher,
    enc.Key(task.keyBytes),
    enc.IV(task.ivBytes),
  );
}
