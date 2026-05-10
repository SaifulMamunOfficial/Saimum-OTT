import 'package:flutter/foundation.dart';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:isar/isar.dart';

import '../../../../core/api_adapter/api_client.dart';
import '../../../../core/models/favorite.dart';
import '../../../../core/utils/db_service.dart';

final favoriteRepositoryProvider = Provider<FavoriteRepository>((ref) {
  return FavoriteRepository(
    ref.watch(isarProvider),
    ref.watch(apiClientProvider),
  );
});

class FavoriteRepository {
  final Isar _isar;
  final Dio _dio;

  FavoriteRepository(this._isar, this._dio);

  /// Toggles favorite status locally and on cloud.
  Future<bool> toggleFavorite(int postId, {String type = 'audio'}) async {
    // 1. Update Locally
    final localFav = await _isar.favoriteModels.where().postIdEqualTo(postId).findFirst();
    
    if (localFav != null) {
      await _isar.writeTxn(() => _isar.favoriteModels.delete(localFav.id));
    } else {
      final newFav = FavoriteModel()
        ..postId = postId
        ..type = type
        ..createdAt = DateTime.now()
        ..isSynced = false;
      await _isar.writeTxn(() => _isar.favoriteModels.put(newFav));
    }

    // 2. Try Sync to Cloud (Fire and forget, or handle background sync)
    try {
      await _dio.post('/favorites/toggle', data: {
        'post_id': postId,
        'type': type,
      });
      
      // Update local sync status if successful
      final updatedFav = await _isar.favoriteModels.where().postIdEqualTo(postId).findFirst();
      if (updatedFav != null) {
        updatedFav.isSynced = true;
        await _isar.writeTxn(() => _isar.favoriteModels.put(updatedFav));
      }
    } catch (e) {
      // If cloud fails, it stays isSynced = false for later background sync
      debugPrint('Cloud toggle failed, will retry later: $e');
    }

    return localFav == null; // returns true if now favorited
  }

  /// Checks if a post is favorited (local check for instant UI response).
  Future<bool> isFavorited(int postId) async {
    final count = await _isar.favoriteModels.where().postIdEqualTo(postId).count();
    return count > 0;
  }

  /// Fetches favorites from cloud and merges with local.
  Future<void> syncWithCloud() async {
    try {
      final response = await _dio.get('/favorites');
      if (response.statusCode == 200) {
        final List<dynamic> cloudFavs = response.data['data'];
        
        await _isar.writeTxn(() async {
          for (var fav in cloudFavs) {
            final postId = fav['post_id'] as int;
            final type = fav['type'] as String;
            
            final exists = await _isar.favoriteModels.where().postIdEqualTo(postId).findFirst();
            if (exists == null) {
              final newFav = FavoriteModel()
                ..postId = postId
                ..type = type
                ..createdAt = DateTime.now()
                ..isSynced = true;
              await _isar.favoriteModels.put(newFav);
            } else if (!exists.isSynced) {
              exists.isSynced = true;
              await _isar.favoriteModels.put(exists);
            }
          }
        });
      }
    } catch (e) {
      debugPrint('Sync with cloud failed: $e');
    }
  }

  /// Gets all favorite IDs (useful for UI lists).
  Future<List<int>> getAllFavoriteIds() async {
    final favs = await _isar.favoriteModels.where().findAll();
    return favs.map((e) => e.postId).toList();
  }
}
