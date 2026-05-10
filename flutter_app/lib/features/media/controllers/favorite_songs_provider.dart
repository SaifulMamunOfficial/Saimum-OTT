import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../controllers/favorite_controller.dart';
import '../../../core/api_adapter/home_repository.dart';
import '../../../core/api_adapter/models/song_model.dart';

final favoriteSongsProvider = FutureProvider<List<SongModel>>((ref) async {
  final ids = ref.watch(favoriteIdsProvider);
  if (ids.isEmpty) return [];

  // In a real app, you'd fetch these specifically. 
  // For now, we'll try to get them from the 'all-songs' or similar, 
  // but better yet, let's assume we can fetch them via a repo method.
  // Since we don't have a 'getSongsByIds' yet, we'll use a placeholder logic 
  // or fetch a large chunk of songs and filter.
  
  final repo = ref.watch(homeRepositoryProvider);
  
  // Strategy: fetch all songs (page 1) and filter for favorites.
  // This is a temporary solution until a dedicated 'favorites' API is used.
  final allData = await repo.getAllSongs(page: 1);
  final List<SongModel> songs = allData['tracks'];
  
  return songs.where((s) => ids.contains(s.id)).toList();
});
