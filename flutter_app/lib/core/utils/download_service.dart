import 'dart:async';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:encrypt/encrypt.dart' as enc;
import 'package:flutter/foundation.dart';
import 'package:isar/isar.dart';
import 'package:path_provider/path_provider.dart';

import '../models/download_manifest.dart';
import 'encryption_service.dart';

/// Chunk size for splitting audio files: 512 KB.
const kChunkSize = 512 * 1024;

/// Callback: receives a value from 0.0 to 1.0.
typedef ProgressCallback = void Function(double progress);

/// Cancellation token for in-flight downloads.
class DownloadCancelToken {
  bool _cancelled = false;
  bool get isCancelled => _cancelled;
  void cancel() => _cancelled = true;
}

/// Handles the full download pipeline:
/// 1. Fetch audio file via Dio to a temp location.
/// 2. Split into [kChunkSize] chunks, AES-256-CBC encrypt each, save as `{i}.enc`.
/// 3. Persist [DownloadManifest] to Isar, delete temp file.
class DownloadService {
  DownloadService._();
  static final DownloadService instance = DownloadService._();

  final _dio = Dio();

  /// Downloads and encrypts media, reporting progress via [onProgress].
  /// Throws on error; caller must handle and clean up.
  Future<void> downloadAndEncrypt({
    required String mediaId,
    required String title,
    required String artist,
    required String thumbnailUrl,
    required String audioUrl,
    required Isar isar,
    required ProgressCallback onProgress,
    DownloadCancelToken? cancelToken,
  }) async {
    final appDir = await getApplicationDocumentsDirectory();
    final downloadDir = Directory('${appDir.path}/downloads/$mediaId');
    await downloadDir.create(recursive: true);

    final tempFile = File('${downloadDir.path}/tmp_raw');

    try {
      // ── Stage 1: Download raw file (reports 0→0.6) ──────────────────────
      await _dio.download(
        audioUrl,
        tempFile.path,
        cancelToken: cancelToken != null ? CancelToken() : null,
        onReceiveProgress: (received, total) {
          if (total > 0) {
            onProgress((received / total) * 0.6);
          }
        },
        options: Options(
          responseType: ResponseType.bytes,
          followRedirects: true,
          receiveTimeout: const Duration(minutes: 5),
        ),
      );

      if (cancelToken?.isCancelled == true) {
        throw Exception('Download cancelled');
      }

      // ── Stage 2: Chunk + Encrypt (reports 0.6→1.0) ──────────────────────
      final rawBytes = await tempFile.readAsBytes();
      final fileSize = rawBytes.length;

      final totalChunks = (fileSize / kChunkSize).ceil();
      final encryption = EncryptionService.instance;
      final key = encryption.deriveKey(mediaId);
      final iv = encryption.generateIV();

      for (var i = 0; i < totalChunks; i++) {
        if (cancelToken?.isCancelled == true) {
          throw Exception('Download cancelled');
        }

        final start = i * kChunkSize;
        final end = (start + kChunkSize).clamp(0, fileSize);
        final plainChunk = Uint8List.sublistView(rawBytes, start, end);

        final cipherBytes = await compute(_encryptIsolate, _EncryptTask(
          plain: plainChunk,
          keyBytes: key.bytes,
          ivBytes: iv.bytes,
        ));

        await File('${downloadDir.path}/$i.enc').writeAsBytes(cipherBytes);

        onProgress(0.6 + ((i + 1) / totalChunks) * 0.4);
      }

      // ── Stage 3: Persist manifest to Isar ───────────────────────────────
      final manifest = DownloadManifest()
        ..mediaId = mediaId
        ..title = title
        ..artist = artist
        ..thumbnailUrl = thumbnailUrl
        ..localPath = downloadDir.path
        ..fileSize = fileSize
        ..totalChunks = totalChunks
        ..downloadedChunks = totalChunks
        ..isCompleted = true
        ..encryptionIv = enc.IV(iv.bytes).base64;

      await isar.writeTxn(() async {
        await isar.downloadManifests.put(manifest);
      });
    } finally {
      // Always delete the temp raw file — only .enc files may remain.
      if (await tempFile.exists()) {
        await tempFile.delete();
      }
    }
  }

  /// Deletes all `.enc` chunks and removes the manifest from Isar.
  Future<void> deleteDownload(String mediaId, Isar isar) async {
    final appDir = await getApplicationDocumentsDirectory();
    final downloadDir = Directory('${appDir.path}/downloads/$mediaId');

    if (await downloadDir.exists()) {
      await downloadDir.delete(recursive: true);
    }

    await isar.writeTxn(() async {
      await isar.downloadManifests.deleteByMediaId(mediaId);
    });
  }
}

// ---------------------------------------------------------------------------
// Isolate helpers (keeps encryption off the UI thread)
// ---------------------------------------------------------------------------

class _EncryptTask {
  final Uint8List plain;
  final Uint8List keyBytes;
  final Uint8List ivBytes;
  const _EncryptTask({
    required this.plain,
    required this.keyBytes,
    required this.ivBytes,
  });
}

Uint8List _encryptIsolate(_EncryptTask task) {
  return EncryptionService.instance.encryptChunk(
    task.plain,
    enc.Key(task.keyBytes),
    enc.IV(task.ivBytes),
  );
}
