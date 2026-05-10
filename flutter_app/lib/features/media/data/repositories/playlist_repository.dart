import 'package:flutter/foundation.dart';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:isar/isar.dart';

import '../../../../core/api_adapter/api_client.dart';
import '../../../../core/models/playlist.dart';
import '../../../../core/utils/db_service.dart';

final playlistRepositoryProvider = Provider<PlaylistRepository>((ref) {
  return PlaylistRepository(
    ref.watch(isarProvider),
    ref.watch(apiClientProvider),
  );
});

class PlaylistRepository {
  final Isar _isar;
  final Dio _dio;

  PlaylistRepository(this._isar, this._dio);

  Future<List<PlaylistModel>> getLocalPlaylists() async {
    return await _isar.playlistModels.where().findAll();
  }

  Future<PlaylistModel> createPlaylist(String name) async {
    final playlist = PlaylistModel()
      ..name = name
      ..createdAt = DateTime.now()
      ..updatedAt = DateTime.now()
      ..isSynced = false;

    await _isar.writeTxn(() => _isar.playlistModels.put(playlist));

    // Try Cloud Sync
    try {
      final response = await _dio.post('/playlists', data: {'name': name});
      if (response.statusCode == 200) {
        final cloudData = response.data['data'];
        playlist.cloudId = cloudData['id'];
        playlist.isSynced = true;
        await _isar.writeTxn(() => _isar.playlistModels.put(playlist));
      }
    } catch (e) {
      debugPrint('Playlist cloud sync failed: $e');
    }

    return playlist;
  }

  Future<void> addSongToPlaylist(int localPlaylistId, int songId) async {
    final playlist = await _isar.playlistModels.get(localPlaylistId);
    if (playlist == null) return;

    if (!playlist.songIds.contains(songId)) {
      playlist.songIds = [...playlist.songIds, songId];
      playlist.updatedAt = DateTime.now();
      playlist.isSynced = false;
      await _isar.writeTxn(() => _isar.playlistModels.put(playlist));

      // Cloud Sync
      if (playlist.cloudId != null) {
        try {
          await _dio.post('/playlists/${playlist.cloudId}/songs', data: {'song_id': songId});
          playlist.isSynced = true;
          await _isar.writeTxn(() => _isar.playlistModels.put(playlist));
        } catch (e) {
          debugPrint('Add song to cloud playlist failed: $e');
        }
      }
    }
  }

  Future<void> syncWithCloud() async {
    try {
      final response = await _dio.get('/playlists');
      if (response.statusCode == 200) {
        final List<dynamic> cloudPlaylists = response.data['data'];

        await _isar.writeTxn(() async {
          for (var cp in cloudPlaylists) {
            final cloudId = cp['id'] as int;
            final existing = await _isar.playlistModels.where().cloudIdEqualTo(cloudId).findFirst();

            if (existing == null) {
              // Fetch songs for this playlist
              final detailRes = await _dio.get('/playlists/$cloudId');
              final List<dynamic> songs = detailRes.data['data']['songs'] ?? [];
              final songIds = songs.map((s) => s['song_id'] as int).toList();

              final newPlaylist = PlaylistModel()
                ..cloudId = cloudId
                ..name = cp['name']
                ..songIds = songIds
                ..createdAt = DateTime.parse(cp['created_at'])
                ..updatedAt = DateTime.parse(cp['updated_at'])
                ..isSynced = true;
              await _isar.playlistModels.put(newPlaylist);
            }
          }
        });
      }
    } catch (e) {
      debugPrint('Playlist sync error: $e');
    }
  }
}
