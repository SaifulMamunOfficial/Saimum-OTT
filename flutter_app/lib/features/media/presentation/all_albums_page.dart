import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/api_adapter/home_repository.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/song_thumbnail.dart';

class AllAlbumsPage extends ConsumerStatefulWidget {
  const AllAlbumsPage({super.key});

  @override
  ConsumerState<AllAlbumsPage> createState() => _AllAlbumsPageState();
}

class _AllAlbumsPageState extends ConsumerState<AllAlbumsPage> {
  int _currentPage = 1;
  final List<dynamic> _albums = [];
  bool _isLoadingMore = false;
  bool _hasMore = true;

  @override
  void initState() {
    super.initState();
    _loadAlbums();
  }

  Future<void> _loadAlbums() async {
    if (_isLoadingMore || !_hasMore) return;
    setState(() => _isLoadingMore = true);

    try {
      final data = await ref.read(homeRepositoryProvider).getAllAlbums(page: _currentPage);
      final newAlbums = data['albums'] as List<dynamic>;
      final lastPage = data['last_page'] as int;

      setState(() {
        _albums.addAll(newAlbums);
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
        title: const Text('All Albums',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800, color: Colors.white)),
      ),
      body: NotificationListener<ScrollNotification>(
        onNotification: (ScrollNotification scrollInfo) {
          if (scrollInfo.metrics.pixels == scrollInfo.metrics.maxScrollExtent) {
            _loadAlbums();
          }
          return true;
        },
        child: GridView.builder(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 160),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 2,
            crossAxisSpacing: 16,
            mainAxisSpacing: 16,
            childAspectRatio: 0.8,
          ),
          itemCount: _albums.length + (_hasMore ? 1 : 0),
          itemBuilder: (context, index) {
            if (index == _albums.length) {
              return const Center(child: CircularProgressIndicator(color: AppColors.primary));
            }
            final album = _albums[index];
            return _AlbumCard(album: album);
          },
        ),
      ),
    );
  }
}

class _AlbumCard extends StatelessWidget {
  final dynamic album;
  const _AlbumCard({required this.album});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => context.push('/album/${album['id']}'),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(
            child: ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: SongThumbnail(url: album['image']),
            ),
          ),
          const SizedBox(height: 8),
          Text(album['name'],
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                  fontSize: 14, fontWeight: FontWeight.w700, color: Colors.white)),
          Text(album['artist'],
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(fontSize: 12, color: Colors.white70)),
        ],
      ),
    );
  }
}
