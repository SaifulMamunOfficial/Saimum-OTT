import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/repositories/favorite_repository.dart';

final favoriteIdsProvider = StateNotifierProvider<FavoriteNotifier, List<int>>((ref) {
  return FavoriteNotifier(ref.watch(favoriteRepositoryProvider));
});

class FavoriteNotifier extends StateNotifier<List<int>> {
  final FavoriteRepository _repository;

  FavoriteNotifier(this._repository) : super([]) {
    _init();
  }

  Future<void> _init() async {
    state = await _repository.getAllFavoriteIds();
    // Background sync
    await _repository.syncWithCloud();
    state = await _repository.getAllFavoriteIds();
  }

  Future<void> toggle(int postId) async {
    final isFav = await _repository.toggleFavorite(postId);
    if (isFav) {
      state = [...state, postId];
    } else {
      state = state.where((id) => id != postId).toList();
    }
  }

  bool isFavorite(int postId) {
    return state.contains(postId);
  }
}
