import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/api_adapter/home_repository.dart';
import '../../../core/api_adapter/models/song_model.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/song_play_count_row.dart';
import '../../../core/widgets/song_thumbnail.dart';
import '../controllers/media_controller.dart';
import '../shared/download_icon_button.dart';

final allSongsProvider = FutureProvider.family<Map<String, dynamic>, int>((ref, page) {
  return ref.watch(homeRepositoryProvider).getAllSongs(page: page);
});

class AllSongsPage extends ConsumerStatefulWidget {
  const AllSongsPage({super.key});

  @override
  ConsumerState<AllSongsPage> createState() => _AllSongsPageState();
}

class _AllSongsPageState extends ConsumerState<AllSongsPage> {
  int _currentPage = 1;
  final List<SongModel> _songs = [];
  bool _isLoadingMore = false;
  bool _hasMore = true;

  @override
  void initState() {
    super.initState();
    _loadSongs();
  }

  Future<void> _loadSongs() async {
    if (_isLoadingMore || !_hasMore) return;
    setState(() => _isLoadingMore = true);

    try {
      final data = await ref.read(homeRepositoryProvider).getAllSongs(page: _currentPage);
      final newSongs = data['tracks'] as List<SongModel>;
      final lastPage = data['last_page'] as int;

      setState(() {
        _songs.addAll(newSongs);
        _hasMore = _currentPage < lastPage;
        _currentPage++;
        _isLoadingMore = false;
      });
    } catch (e) {
      setState(() => _isLoadingMore = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: AppColors.background,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new_rounded, color: Colors.white, size: 20),
          onPressed: () => context.pop(),
        ),
        title: const Text('All Songs',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800, color: Colors.white)),
      ),
      body: NotificationListener<ScrollNotification>(
        onNotification: (ScrollNotification scrollInfo) {
          if (scrollInfo.metrics.pixels == scrollInfo.metrics.maxScrollExtent) {
            _loadSongs();
          }
          return true;
        },
        child: ListView.separated(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 160),
          itemCount: _songs.length + (_hasMore ? 1 : 0),
          separatorBuilder: (_, __) => const SizedBox(height: 12),
          itemBuilder: (context, index) {
            if (index == _songs.length) {
              return const Center(
                  child: Padding(
                padding: EdgeInsets.all(16.0),
                child: CircularProgressIndicator(color: AppColors.primary),
              ));
            }
            final song = _songs[index];
            return _SongTile(song: song, index: index);
          },
        ),
      ),
    );
  }
}

class _SongTile extends ConsumerWidget {
  final SongModel song;
  final int index;
  const _SongTile({required this.song, required this.index});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return GestureDetector(
      onTap: () => ref.read(mediaControllerProvider.notifier).play(
            song.audioUrl,
            title: song.title,
            artist: song.artist,
            artworkUrl: song.thumbnail,
            songId: song.id,
          ),
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: AppColors.surfaceTwo,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: AppColors.glassBorder, width: 0.5),
        ),
        child: Row(
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: SizedBox(
                width: 50,
                height: 50,
                child: SongThumbnail(url: song.thumbnail),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(song.title,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                          fontSize: 14, fontWeight: FontWeight.w700, color: Colors.white)),
                  Text(song.artist,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(fontSize: 12, color: Colors.white70)),
                  const SizedBox(height: 5),
                  SongPlayCountRow(
                    totalViews: song.totalViews,
                    iconSize: 13,
                    fontSize: 12,
                  ),
                ],
              ),
            ),
            DownloadIconButton(song: song),
            const SizedBox(width: 8),
            const Icon(Icons.play_circle_outline_rounded, color: AppColors.primary, size: 28),
          ],
        ),
      ),
    );
  }
}
