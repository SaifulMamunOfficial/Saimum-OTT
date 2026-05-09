import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/models/download_manifest.dart';
import '../../../core/utils/db_service.dart';
import '../../../core/utils/download_service.dart';

// ---------------------------------------------------------------------------
// Download status
// ---------------------------------------------------------------------------

enum DownloadStatus { idle, downloading, completed, error }

// ---------------------------------------------------------------------------
// State
// ---------------------------------------------------------------------------

class DownloadState {
  /// Per-song progress: 0.0 → 1.0.
  final Map<String, double> progress;

  /// Per-song status.
  final Map<String, DownloadStatus> statuses;

  /// All completed manifests from Isar (for the Library section).
  final List<DownloadManifest> completedManifests;

  const DownloadState({
    this.progress = const {},
    this.statuses = const {},
    this.completedManifests = const [],
  });

  DownloadStatus statusOf(String mediaId) =>
      statuses[mediaId] ?? DownloadStatus.idle;

  double progressOf(String mediaId) => progress[mediaId] ?? 0.0;

  DownloadState copyWith({
    Map<String, double>? progress,
    Map<String, DownloadStatus>? statuses,
    List<DownloadManifest>? completedManifests,
  }) {
    return DownloadState(
      progress: progress ?? this.progress,
      statuses: statuses ?? this.statuses,
      completedManifests: completedManifests ?? this.completedManifests,
    );
  }
}

// ---------------------------------------------------------------------------
// Provider
// ---------------------------------------------------------------------------

final downloadControllerProvider =
    StateNotifierProvider<DownloadController, DownloadState>(
  (ref) => DownloadController(ref),
);

// ---------------------------------------------------------------------------
// Controller
// ---------------------------------------------------------------------------

class DownloadController extends StateNotifier<DownloadState> {
  final Ref _ref;
  final _cancelTokens = <String, DownloadCancelToken>{};

  DownloadController(this._ref) : super(const DownloadState()) {
    _loadCompletedDownloads();
  }

  // ── Initialization ─────────────────────────────────────────────────────────

  Future<void> _loadCompletedDownloads() async {
    final isar = _ref.read(isarProvider);
    // Isar 3.x defines findAll() on each terminal QueryBuilder state via separate
    // extensions (QAfterWhere, QAfterFilterCondition, etc.). The old analyzer
    // (5.x bundled with this SDK) fails to resolve these; the code compiles fine.
    // ignore: undefined_method
    final all = await isar.downloadManifests.where().anyId().findAll();
    final manifests = all.where((m) => m.isCompleted).toList();

    final statuses = <String, DownloadStatus>{};
    for (final m in manifests) {
      statuses[m.mediaId] = DownloadStatus.completed;
    }

    state = state.copyWith(
      statuses: statuses,
      completedManifests: manifests,
    );
  }

  // ── Download ───────────────────────────────────────────────────────────────

  Future<void> download(SongModel song) async {
    final mediaId = song.id.toString();

    if (state.statusOf(mediaId) == DownloadStatus.downloading) return;
    if (state.statusOf(mediaId) == DownloadStatus.completed) return;

    final cancelToken = DownloadCancelToken();
    _cancelTokens[mediaId] = cancelToken;

    _setStatus(mediaId, DownloadStatus.downloading);
    _setProgress(mediaId, 0.0);

    try {
      await DownloadService.instance.downloadAndEncrypt(
        mediaId: mediaId,
        title: song.title,
        artist: song.artist,
        thumbnailUrl: song.thumbnail,
        audioUrl: song.audioUrl,
        isar: _ref.read(isarProvider),
        cancelToken: cancelToken,
        onProgress: (p) => _setProgress(mediaId, p),
      );

      await _loadCompletedDownloads();
      _setProgress(mediaId, 1.0);
      _setStatus(mediaId, DownloadStatus.completed);
    } catch (e) {
      debugPrint('DownloadController: error downloading $mediaId → $e');
      _setStatus(mediaId, DownloadStatus.error);
    } finally {
      _cancelTokens.remove(mediaId);
    }
  }

  // ── Cancel / Delete ────────────────────────────────────────────────────────

  Future<void> cancelDownload(String mediaId) async {
    _cancelTokens[mediaId]?.cancel();
    _cancelTokens.remove(mediaId);
    _setStatus(mediaId, DownloadStatus.idle);
    _setProgress(mediaId, 0.0);
  }

  Future<void> deleteDownload(String mediaId) async {
    await DownloadService.instance.deleteDownload(
      mediaId,
      _ref.read(isarProvider),
    );
    await _loadCompletedDownloads();

    final newStatuses = Map<String, DownloadStatus>.from(state.statuses)
      ..remove(mediaId);
    final newProgress = Map<String, double>.from(state.progress)
      ..remove(mediaId);
    state = state.copyWith(statuses: newStatuses, progress: newProgress);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  void _setStatus(String mediaId, DownloadStatus s) {
    final updated = Map<String, DownloadStatus>.from(state.statuses)
      ..[mediaId] = s;
    state = state.copyWith(statuses: updated);
  }

  void _setProgress(String mediaId, double p) {
    final updated = Map<String, double>.from(state.progress)..[mediaId] = p;
    state = state.copyWith(progress: updated);
  }
}
