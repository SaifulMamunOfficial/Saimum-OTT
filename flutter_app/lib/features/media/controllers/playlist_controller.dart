import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/models/playlist.dart';
import '../data/repositories/playlist_repository.dart';

final playlistsProvider = StateNotifierProvider<PlaylistNotifier, List<PlaylistModel>>((ref) {
  return PlaylistNotifier(ref.watch(playlistRepositoryProvider));
});

class PlaylistNotifier extends StateNotifier<List<PlaylistModel>> {
  final PlaylistRepository _repository;

  PlaylistNotifier(this._repository) : super([]) {
    _init();
  }

  Future<void> _init() async {
    state = await _repository.getLocalPlaylists();
    await _repository.syncWithCloud();
    state = await _repository.getLocalPlaylists();
  }

  Future<void> createPlaylist(String name) async {
    await _repository.createPlaylist(name);
    state = await _repository.getLocalPlaylists();
  }

  Future<void> addSong(int playlistId, int songId) async {
    await _repository.addSongToPlaylist(playlistId, songId);
    state = await _repository.getLocalPlaylists();
  }
}
