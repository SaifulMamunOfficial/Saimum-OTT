import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/api_adapter/home_repository.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/song_thumbnail.dart';

class AllArtistsPage extends ConsumerStatefulWidget {
  const AllArtistsPage({super.key});

  @override
  ConsumerState<AllArtistsPage> createState() => _AllArtistsPageState();
}

class _AllArtistsPageState extends ConsumerState<AllArtistsPage> {
  int _currentPage = 1;
  final List<dynamic> _artists = [];
  bool _isLoadingMore = false;
  bool _hasMore = true;

  @override
  void initState() {
    super.initState();
    _loadArtists();
  }

  Future<void> _loadArtists() async {
    if (_isLoadingMore || !_hasMore) return;
    setState(() => _isLoadingMore = true);

    try {
      final data = await ref.read(homeRepositoryProvider).getAllArtists(page: _currentPage);
      final newArtists = data['artists'] as List<dynamic>;
      final lastPage = data['last_page'] as int;

      setState(() {
        _artists.addAll(newArtists);
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
        title: const Text('All Artists',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.w800, color: Colors.white)),
      ),
      body: NotificationListener<ScrollNotification>(
        onNotification: (ScrollNotification scrollInfo) {
          if (scrollInfo.metrics.pixels == scrollInfo.metrics.maxScrollExtent) {
            _loadArtists();
          }
          return true;
        },
        child: GridView.builder(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 160),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 3,
            crossAxisSpacing: 12,
            mainAxisSpacing: 20,
            childAspectRatio: 0.75,
          ),
          itemCount: _artists.length + (_hasMore ? 1 : 0),
          itemBuilder: (context, index) {
            if (index == _artists.length) {
              return const Center(child: CircularProgressIndicator(color: AppColors.primary));
            }
            final artist = _artists[index];
            return _ArtistCard(artist: artist);
          },
        ),
      ),
    );
  }
}

class _ArtistCard extends StatelessWidget {
  final dynamic artist;
  const _ArtistCard({required this.artist});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => context.push('/artist/${artist['id']}'),
      child: Column(
        children: [
          Expanded(
            child: Container(
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(color: AppColors.primary.withValues(alpha: 0.3), width: 1),
              ),
              child: ClipOval(
                child: SongThumbnail(url: artist['image']),
              ),
            ),
          ),
          const SizedBox(height: 10),
          Text(
            artist['name'],
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            textAlign: TextAlign.center,
            style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: Colors.white),
          ),
        ],
      ),
    );
  }
}
