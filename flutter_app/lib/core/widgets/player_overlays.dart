import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../features/media/controllers/media_controller.dart';
import '../../features/media/shared/media_session_orchestrator.dart';
import '../api_adapter/mock_data.dart';
import '../theme/app_colors.dart';
import 'player_modals.dart';

// ---------------------------------------------------------------------------
// More Options bottom sheet
// ---------------------------------------------------------------------------

void showMoreOptionsSheet(BuildContext context) {
  showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.transparent,
    useRootNavigator: true,
    builder: (_) => _MoreOptionsSheet(playerContext: context),
  );
}

class _MoreOptionsSheet extends StatelessWidget {
  final BuildContext playerContext;
  const _MoreOptionsSheet({required this.playerContext});

  static const _items = [
    (icon: Icons.playlist_add, label: 'Add to Playlist', id: 'playlist'),
    (icon: Icons.speed_rounded, label: 'Playback Speed', id: 'speed'),
    (icon: Icons.bedtime_rounded, label: 'Sleep Timer', id: 'timer'),
    (icon: Icons.equalizer_rounded, label: 'Equalizer', id: 'eq'),
    (icon: Icons.drive_eta_rounded, label: 'Driving Mode', id: 'drive'),
    (icon: Icons.flag_outlined, label: 'Report Issue', id: 'report'),
    (icon: Icons.share_rounded, label: 'Share Song', id: 'share'),
  ];

  @override
  Widget build(BuildContext context) {
    return BackdropFilter(
      filter: ImageFilter.blur(sigmaX: 24, sigmaY: 24),
      child: Container(
        decoration: const BoxDecoration(
          color: Color(0xCC111114),
          borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
          border: Border(top: BorderSide(color: AppColors.glassBorder)),
        ),
        child: SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const SizedBox(height: 12),
              Center(
                child: Container(
                  width: 36, height: 4,
                  decoration: BoxDecoration(
                    color: AppColors.onSurfaceMuted.withValues(alpha: 0.4),
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 8),
              ..._items.map(
                (item) => ListTile(
                  leading: Icon(item.icon, color: AppColors.primary, size: 22),
                  title: Text(item.label,
                      style: const TextStyle(
                          color: AppColors.onSurface,
                          fontSize: 14,
                          fontWeight: FontWeight.w500)),
                  onTap: () => _onTap(context, item.id),
                ),
              ),
              const SizedBox(height: 8),
            ],
          ),
        ),
      ),
    );
  }

  void _onTap(BuildContext ctx, String id) async {
    Navigator.of(ctx).pop();
    await Future<void>.delayed(const Duration(milliseconds: 200));
    if (!playerContext.mounted) return;
    switch (id) {
      case 'playlist':
        showAddToPlaylistDialog(playerContext);
      case 'speed':
      case 'timer':
        showPlayerOptionsSheet(playerContext);
      default:
        break; // Equalizer, Driving Mode, Report, Share — Phase 7+
    }
  }
}

// ---------------------------------------------------------------------------
// Up Next Queue sheet
// ---------------------------------------------------------------------------

void showQueueSheet(BuildContext context) {
  showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.transparent,
    isScrollControlled: true,
    useRootNavigator: true,
    builder: (_) => _QueueSheet(playerContext: context),
  );
}

class _QueueSheet extends ConsumerWidget {
  final BuildContext playerContext;
  const _QueueSheet({required this.playerContext});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final orch = ref.watch(mediaSessionOrchestratorProvider);
    final currentId = orch.currentItem?.id;

    return DraggableScrollableSheet(
      initialChildSize: 0.6,
      minChildSize: 0.3,
      maxChildSize: 0.92,
      snap: true,
      snapSizes: const [0.6, 0.92],
      builder: (_, scrollCtrl) => BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 24, sigmaY: 24),
        child: Container(
          decoration: const BoxDecoration(
            color: Color(0xCC111114),
            borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
            border: Border(top: BorderSide(color: AppColors.glassBorder)),
          ),
          child: ListView.builder(
            controller: scrollCtrl,
            itemCount: kMockSongs.length + 1,
            itemBuilder: (_, i) {
              if (i == 0) {
                return Padding(
                  padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Center(
                        child: Container(
                          width: 36, height: 4,
                          margin: const EdgeInsets.only(bottom: 16),
                          decoration: BoxDecoration(
                            color: AppColors.onSurfaceMuted.withValues(alpha: 0.4),
                            borderRadius: BorderRadius.circular(2),
                          ),
                        ),
                      ),
                      const Text('Up Next',
                          style: TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.w800,
                              color: AppColors.onSurface)),
                      Text('${kMockSongs.length} tracks',
                          style: const TextStyle(
                              fontSize: 12, color: AppColors.onSurfaceMuted)),
                    ],
                  ),
                );
              }
              final song = kMockSongs[i - 1];
              final isCurrent = song.audioUrl == currentId;
              return ListTile(
                contentPadding:
                    const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
                leading: ClipRRect(
                  borderRadius: BorderRadius.circular(8),
                  child: SizedBox(
                    width: 44, height: 44,
                    child: Image.network(song.thumbnail, fit: BoxFit.cover,
                        errorBuilder: (_, _, _) => Container(
                          color: AppColors.surfaceTwo,
                          child: const Icon(Icons.music_note,
                              color: AppColors.onSurfaceMuted),
                        )),
                  ),
                ),
                title: Text(song.title,
                    style: TextStyle(
                        color: isCurrent
                            ? AppColors.primary
                            : AppColors.onSurface,
                        fontSize: 13,
                        fontWeight: FontWeight.w600)),
                subtitle: Text(song.artist,
                    style: const TextStyle(
                        color: AppColors.onSurfaceMuted, fontSize: 11)),
                trailing: isCurrent
                    ? const Icon(Icons.equalizer_rounded,
                        color: AppColors.primary, size: 18)
                    : null,
                onTap: () {
                  ref.read(mediaControllerProvider.notifier).play(
                        song.audioUrl,
                        title: song.title,
                        artist: song.artist,
                        artworkUrl: song.thumbnail,
                      );
                  Navigator.of(context).pop();
                },
              );
            },
          ),
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Lyrics overlay
// ---------------------------------------------------------------------------

void showLyricsOverlay(BuildContext context, String? title, String? artist) {
  showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.transparent,
    isScrollControlled: true,
    useRootNavigator: true,
    builder: (_) => _LyricsSheet(title: title, artist: artist),
  );
}

class _LyricsSheet extends StatelessWidget {
  final String? title;
  final String? artist;
  const _LyricsSheet({this.title, this.artist});

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      initialChildSize: 0.7,
      minChildSize: 0.4,
      maxChildSize: 0.92,
      builder: (_, scrollCtrl) => BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 24, sigmaY: 24),
        child: Container(
          decoration: const BoxDecoration(
            color: Color(0xCC111114),
            borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
            border: Border(top: BorderSide(color: AppColors.glassBorder)),
          ),
          child: ListView(
            controller: scrollCtrl,
            padding: const EdgeInsets.fromLTRB(28, 16, 28, 40),
            children: [
              Center(
                child: Container(
                  width: 36, height: 4,
                  margin: const EdgeInsets.only(bottom: 20),
                  decoration: BoxDecoration(
                    color: AppColors.onSurfaceMuted.withValues(alpha: 0.4),
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              Row(
                children: [
                  const Icon(Icons.lyrics_outlined,
                      color: AppColors.primary, size: 18),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(title ?? 'Lyrics',
                        style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w700,
                            color: AppColors.onSurface)),
                  ),
                ],
              ),
              if (artist != null)
                Padding(
                  padding: const EdgeInsets.only(top: 4, left: 26),
                  child: Text(artist!,
                      style: const TextStyle(
                          fontSize: 13, color: AppColors.onSurfaceMuted)),
                ),
              const SizedBox(height: 32),
              const Text('Lyrics sync coming in Phase 7.\n\nWe\'ll integrate LRC timestamp data from our library so lyrics scroll in perfect time with the music.',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                      color: AppColors.onSurfaceMuted,
                      fontSize: 14,
                      height: 1.8)),
            ],
          ),
        ),
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Add to Playlist dialog
// ---------------------------------------------------------------------------

void showAddToPlaylistDialog(BuildContext context) {
  showDialog<void>(
    context: context,
    builder: (ctx) => AlertDialog(
      backgroundColor: AppColors.surfaceTwo,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      title: const Row(
        children: [
          Icon(Icons.playlist_add, color: AppColors.primary, size: 20),
          SizedBox(width: 10),
          Text('Add to Playlist',
              style:
                  TextStyle(color: AppColors.onSurface, fontSize: 16)),
        ],
      ),
      content: const Text(
          'Playlist management is coming in Phase 7.\nYour created playlists will appear here.',
          style: TextStyle(
              color: AppColors.onSurfaceMuted, fontSize: 14, height: 1.6)),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(ctx).pop(),
          child: const Text('Got it'),
        ),
      ],
    ),
  );
}
