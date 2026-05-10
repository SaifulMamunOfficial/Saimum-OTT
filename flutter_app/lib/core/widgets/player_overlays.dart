import 'dart:ui';
import 'package:share_plus/share_plus.dart';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:audio_service/audio_service.dart';

import '../../features/media/controllers/playlist_controller.dart';
import '../../features/media/controllers/favorite_controller.dart';
import '../../features/media/shared/media_session_orchestrator.dart';
import '../../features/media/controllers/media_controller.dart';
import '../api_adapter/mock_data.dart';
import '../theme/app_colors.dart';
import 'player_modals.dart';
import 'song_thumbnail.dart';

// ---------------------------------------------------------------------------
// More Options bottom sheet
// ---------------------------------------------------------------------------

void showMoreOptionsSheet(BuildContext context, int? songId) {
  showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.transparent,
    useRootNavigator: true,
    builder: (_) => _MoreOptionsSheet(playerContext: context, songId: songId),
  );
}

class _MoreOptionsSheet extends ConsumerWidget {
  final BuildContext playerContext;
  final int? songId;
  const _MoreOptionsSheet({required this.playerContext, this.songId});

  static const _items = [
    (icon: Icons.playlist_add, label: 'Add to Playlist', id: 'playlist'),
    (icon: Icons.speed_rounded, label: 'Playback Speed', id: 'speed'),
    (icon: Icons.bedtime_rounded, label: 'Sleep Timer', id: 'timer'),
    (icon: Icons.equalizer_rounded, label: 'Equalizer', id: 'equalizer'),
    (icon: Icons.drive_eta_rounded, label: 'Driving Mode', id: 'drive'),
    (icon: Icons.flag_outlined, label: 'Report Issue', id: 'report'),
    (icon: Icons.share_rounded, label: 'Share Song', id: 'share'),
  ];

  @override
  Widget build(BuildContext context, WidgetRef ref) {
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
                  onTap: () => _onTap(context, ref, item.id),
                ),
              ),
              const SizedBox(height: 8),
            ],
          ),
        ),
      ),
    );
  }

  void _onTap(BuildContext ctx, WidgetRef ref, String id) async {
    Navigator.of(ctx).pop();
    await Future<void>.delayed(const Duration(milliseconds: 200));
    if (!playerContext.mounted) return;

    switch (id) {
      case 'playlist':
        showAddToPlaylistDialog(playerContext, songId);
        break;
      case 'speed':
        showPlaybackSpeedSheet(playerContext);
        break;
      case 'timer':
        showSleepTimerSheet(playerContext);
        break;
      case 'drive':
        showDrivingModeOverlay(playerContext);
        break;
      case 'share':
        final orch = ref.read(mediaSessionOrchestratorProvider);
        final extras = orch.currentItem?.extras;
        final songId = extras?['songId'];
        final title = orch.currentItem?.title ?? 'Music';
        if (songId != null) {
          // Use a friendly link instead of the raw audio URL for security
          Share.share('Listen to this beautiful track on Saimum: $title\nhttps://saimum.org/song?id=$songId');
        }
        break;
      case 'report':
        final orch = ref.read(mediaSessionOrchestratorProvider);
        showReportIssueDialog(playerContext, orch.currentItem?.title);
        break;
      case 'equalizer':
        showAdvancedEqualizer(playerContext);
        break;
      default:
        break;
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
    final handler = ref.watch(audioHandlerProvider);
    final queueStream = handler.queue;
    final pbStateStream = handler.playbackState;

    return StreamBuilder<List<MediaItem>>(
      stream: queueStream,
      builder: (context, queueSnap) {
        final queueItems = queueSnap.data ?? [];
        
        return StreamBuilder<PlaybackState>(
          stream: pbStateStream,
          builder: (context, pbSnap) {
            final currentIndex = pbSnap.data?.queueIndex ?? 0;

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
                    itemCount: queueItems.length + 1,
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
                              Text('${queueItems.length} tracks',
                                  style: const TextStyle(
                                      fontSize: 12, color: AppColors.onSurfaceMuted)),
                            ],
                          ),
                        );
                      }
                      final item = queueItems[i - 1];
                      final isCurrent = i - 1 == currentIndex;
                      
                      return ListTile(
                        contentPadding:
                            const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
                        leading: ClipRRect(
                          borderRadius: BorderRadius.circular(8),
                          child: SizedBox(
                            width: 44, height: 44,
                            child: SongThumbnail(url: item.artUri?.toString()),
                          ),
                        ),
                        title: Text(item.title,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: TextStyle(
                                color: isCurrent
                                    ? AppColors.primary
                                    : AppColors.onSurface,
                                fontSize: 13,
                                fontWeight: FontWeight.w600)),
                        subtitle: Text(item.artist ?? 'Unknown',
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                                color: AppColors.onSurfaceMuted, fontSize: 11)),
                        trailing: isCurrent
                            ? const Icon(Icons.equalizer_rounded,
                                color: AppColors.primary, size: 18)
                            : null,
                        onTap: () {
                          handler.skipToQueueItem(i - 1);
                        },
                      );
                    },
                  ),
                ),
              ),
            );
          },
        );
      },
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
              const SizedBox(height: 40),
              _DummyLyrics(title: title),
              const SizedBox(height: 40),
              const Center(
                child: Text('Lyrics sync coming in Phase 7',
                    style: TextStyle(
                        color: AppColors.primary,
                        fontSize: 11,
                        fontWeight: FontWeight.w600,
                        letterSpacing: 1.2)),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _DummyLyrics extends StatelessWidget {
  final String? title;
  const _DummyLyrics({this.title});

  @override
  Widget build(BuildContext context) {
    final t = title ?? 'Song';
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _lyricLine('In the heart of the melody'),
        _lyricLine('Where the $t begins to flow'),
        _lyricLine('Voices echo in the starlight'),
        _lyricLine('Every rhythm that we know'),
        const SizedBox(height: 24),
        _lyricLine('Through the echoes of the night', isHighlight: true),
        _lyricLine('We are searching for the light', isHighlight: true),
        const SizedBox(height: 24),
        _lyricLine('It\'s the sound of $t'),
        _lyricLine('Lifting spirits high above'),
        _lyricLine('In this world of pure emotion'),
        _lyricLine('Bound together by our love'),
      ],
    );
  }

  Widget _lyricLine(String text, {bool isHighlight = false}) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Text(
          text,
          style: TextStyle(
            fontSize: isHighlight ? 20 : 18,
            fontWeight: isHighlight ? FontWeight.w800 : FontWeight.w600,
            color: isHighlight ? Colors.white : Colors.white.withValues(alpha: 0.5),
            height: 1.4,
          ),
        ),
      );
}

// ---------------------------------------------------------------------------
// Add to Playlist dialog
// ---------------------------------------------------------------------------

void showAddToPlaylistDialog(BuildContext context, int? songId) {
  if (songId == null) return;
  
  showDialog<void>(
    context: context,
    builder: (ctx) => _PlaylistSelectorDialog(songId: songId),
  );
}

class _PlaylistSelectorDialog extends ConsumerWidget {
  final int songId;
  const _PlaylistSelectorDialog({required this.songId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final playlists = ref.watch(playlistsProvider);

    return AlertDialog(
      backgroundColor: AppColors.surfaceTwo,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      title: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          const Text('Add to Playlist',
              style: TextStyle(color: AppColors.onSurface, fontSize: 18)),
          IconButton(
            icon: const Icon(Icons.add_box_outlined, color: AppColors.primary),
            onPressed: () => _showCreatePlaylist(context, ref),
          ),
        ],
      ),
      content: playlists.isEmpty
          ? Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Text('No playlists found.',
                    style: TextStyle(color: AppColors.onSurfaceMuted)),
                const SizedBox(height: 16),
                ElevatedButton(
                  onPressed: () => _showCreatePlaylist(context, ref),
                  child: const Text('Create New'),
                ),
              ],
            )
          : SizedBox(
              width: double.maxFinite,
              child: ListView.builder(
                shrinkWrap: true,
                itemCount: playlists.length,
                itemBuilder: (context, index) {
                  final p = playlists[index];
                  final alreadyIn = p.songIds.contains(songId);
                  return ListTile(
                    leading: const Icon(Icons.playlist_play_rounded, color: AppColors.primary),
                    title: Text(p.name, style: const TextStyle(color: AppColors.onSurface)),
                    subtitle: Text('${p.songIds.length} songs', style: const TextStyle(color: AppColors.onSurfaceMuted, fontSize: 11)),
                    trailing: alreadyIn 
                      ? const Icon(Icons.check_circle, color: AppColors.primary, size: 20)
                      : const Icon(Icons.add_circle_outline, color: AppColors.onSurfaceMuted, size: 20),
                    onTap: alreadyIn ? null : () {
                      ref.read(playlistsProvider.notifier).addSong(p.id, songId);
                      Navigator.pop(context);
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(content: Text('Added to ${p.name}')),
                      );
                    },
                  );
                },
              ),
            ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('Cancel', style: TextStyle(color: AppColors.onSurfaceMuted)),
        ),
      ],
    );
  }

  void _showCreatePlaylist(BuildContext context, WidgetRef ref) {
    final ctrl = TextEditingController();
    showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.surfaceTwo,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text('New Playlist', style: TextStyle(color: Colors.white)),
        content: TextField(
          controller: ctrl,
          autofocus: true,
          style: const TextStyle(color: Colors.white),
          decoration: const InputDecoration(
            hintText: 'Playlist name',
            hintStyle: TextStyle(color: Colors.white24),
            enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: Colors.white24)),
            focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: AppColors.primary)),
          ),
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
          TextButton(
            onPressed: () {
              if (ctrl.text.isNotEmpty) {
                ref.read(playlistsProvider.notifier).createPlaylist(ctrl.text);
                Navigator.pop(ctx);
              }
            },
            child: const Text('Create', style: TextStyle(color: AppColors.primary)),
          ),
        ],
      ),
    );
  }
}

// ---------------------------------------------------------------------------
// Driving Mode overlay
// ---------------------------------------------------------------------------

void showDrivingModeOverlay(BuildContext context) {
  showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.transparent,
    isScrollControlled: true,
    useRootNavigator: true,
    builder: (_) => const _DrivingModeSheet(),
  );
}

class _DrivingModeSheet extends StatefulWidget {
  const _DrivingModeSheet();

  @override
  State<_DrivingModeSheet> createState() => _DrivingModeSheetState();
}

class _DrivingModeSheetState extends State<_DrivingModeSheet> with SingleTickerProviderStateMixin {
  bool _isLocked = false;
  late AnimationController _unlockAnim;

  @override
  void initState() {
    super.initState();
    _unlockAnim = AnimationController(vsync: this, duration: const Duration(seconds: 2));
  }

  @override
  void dispose() {
    _unlockAnim.dispose();
    super.dispose();
  }

  void _onUnlockLongPressStart(LongPressStartDetails details) => _unlockAnim.forward();
  void _onUnlockLongPressEnd(LongPressEndDetails details) {
    if (_unlockAnim.value < 1.0) _unlockAnim.reverse();
  }

  @override
  Widget build(BuildContext context) {
    return Consumer(builder: (context, ref, _) {
      final orch = ref.watch(mediaSessionOrchestratorProvider);
      final ctrl = ref.read(mediaControllerProvider.notifier);
      final item = orch.currentItem;
      final isPlaying = orch.status == PlaybackStatus.playing;

      return Scaffold(
        backgroundColor: Colors.black,
        body: Stack(
          children: [
            // Background Blur
            Positioned.fill(
              child: item?.artworkUrl != null
                  ? Image.network(item!.artworkUrl!, fit: BoxFit.cover)
                  : const SizedBox(),
            ),
            Positioned.fill(
              child: BackdropFilter(
                filter: ImageFilter.blur(sigmaX: 80, sigmaY: 80),
                child: Container(color: Colors.black.withValues(alpha: 0.7)),
              ),
            ),
            SafeArea(
              child: Column(
                children: [
                  const SizedBox(height: 12), // Added spacing to lower the text
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    child: Row(
                      children: [
                        IconButton(
                          icon: const Icon(Icons.close,
                              color: Colors.white70, size: 28),
                          onPressed: () => Navigator.pop(context),
                        ),
                        const Spacer(),
                        const Icon(Icons.directions_car_rounded,
                            color: AppColors.primary, size: 24),
                        const SizedBox(width: 8),
                        const Text('DRIVING MODE',
                            style: TextStyle(
                                color: AppColors.primary,
                                fontSize: 12,
                                fontWeight: FontWeight.w900,
                                letterSpacing: 2)),
                        const Spacer(),
                        IconButton(
                          icon: Icon(_isLocked ? Icons.lock : Icons.lock_open, color: Colors.white54),
                          onPressed: () => setState(() => _isLocked = true),
                        ),
                      ],
                    ),
                  ),
                  const Spacer(),
                  // Large Artwork with Glow
                  Center(
                    child: Container(
                      width: 220,
                      height: 220,
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(24),
                        boxShadow: [
                          BoxShadow(
                            color: AppColors.primary.withValues(alpha: 0.2),
                            blurRadius: 40,
                            spreadRadius: 10,
                          ),
                        ],
                      ),
                      child: ClipRRect(
                        borderRadius: BorderRadius.circular(24),
                        child: SongThumbnail(url: item?.artworkUrl),
                      ),
                    ),
                  ),
                  const SizedBox(height: 40),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 40),
                    child: Column(
                      children: [
                        Text(
                          item?.title ?? 'Unknown',
                          textAlign: TextAlign.center,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                              fontSize: 32,
                              fontWeight: FontWeight.w900,
                              color: Colors.white,
                              letterSpacing: -0.5),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          item?.artist ?? 'Unknown',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                              fontSize: 20,
                              color: Colors.white.withValues(alpha: 0.6),
                              fontWeight: FontWeight.w500),
                        ),
                      ],
                    ),
                  ),
                  const Spacer(),
                  // Ultra Large Controls
                  IgnorePointer(
                    ignoring: _isLocked,
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 30),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          IconButton(
                            iconSize: 72,
                            icon: const Icon(Icons.skip_previous_rounded,
                                color: Colors.white),
                            onPressed: () => ctrl.skipToPrevious(),
                          ),
                          GestureDetector(
                            onTap: () => isPlaying ? ctrl.pause() : ctrl.resume(),
                            child: Container(
                              width: 120,
                              height: 120,
                              decoration: BoxDecoration(
                                color: AppColors.primary,
                                shape: BoxShape.circle,
                                boxShadow: [
                                  BoxShadow(
                                    color: AppColors.primary.withValues(alpha: 0.4),
                                    blurRadius: 30,
                                    spreadRadius: 5,
                                  ),
                                ],
                              ),
                              child: Icon(
                                isPlaying
                                    ? Icons.pause_rounded
                                    : Icons.play_arrow_rounded,
                                color: Colors.black,
                                size: 80,
                              ),
                            ),
                          ),
                          IconButton(
                            iconSize: 72,
                            icon: const Icon(Icons.skip_next_rounded,
                                color: Colors.white),
                            onPressed: () => ctrl.skipToNext(),
                          ),
                        ],
                      ),
                    ),
                  ),
                  const Spacer(flex: 2),
                ],
              ),
            ),
            
            // LOCK OVERLAY
            if (_isLocked)
              Positioned.fill(
                child: GestureDetector(
                  onTap: () {}, // Blocks taps
                  child: Container(
                    color: Colors.black.withValues(alpha: 0.4),
                    child: Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(Icons.lock, color: Colors.white70, size: 48),
                          const SizedBox(height: 24),
                          const Text('Screen Locked', style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
                          const SizedBox(height: 8),
                          const Text('Long press the button below to unlock', style: TextStyle(color: Colors.white54, fontSize: 14)),
                          const SizedBox(height: 48),
                          GestureDetector(
                            onLongPressStart: _onUnlockLongPressStart,
                            onLongPressEnd: _onUnlockLongPressEnd,
                            onLongPress: () {
                              setState(() => _isLocked = false);
                              _unlockAnim.reset();
                            },
                            child: Stack(
                              alignment: Alignment.center,
                              children: [
                                SizedBox(
                                  width: 80, height: 80,
                                  child: AnimatedBuilder(
                                    animation: _unlockAnim,
                                    builder: (context, _) => CircularProgressIndicator(
                                      value: _unlockAnim.value,
                                      strokeWidth: 4,
                                      color: AppColors.primary,
                                      backgroundColor: Colors.white12,
                                    ),
                                  ),
                                ),
                                Container(
                                  width: 64, height: 64,
                                  decoration: const BoxDecoration(color: Colors.white10, shape: BoxShape.circle),
                                  child: const Icon(Icons.fingerprint, color: Colors.white, size: 32),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
          ],
        ),
      );
    });
  }
}

// ---------------------------------------------------------------------------
// Report Issue Dialog
// ---------------------------------------------------------------------------

void showReportIssueDialog(BuildContext context, String? songTitle) {
  showDialog<void>(
    context: context,
    builder: (ctx) => _ReportIssueDialog(songTitle: songTitle),
  );
}

class _ReportIssueDialog extends StatefulWidget {
  final String? songTitle;
  const _ReportIssueDialog({this.songTitle});

  @override
  State<_ReportIssueDialog> createState() => _ReportIssueDialogState();
}

class _ReportIssueDialogState extends State<_ReportIssueDialog> {
  final _subjectCtrl = TextEditingController();
  final _detailsCtrl = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      backgroundColor: const Color(0xFF1A1A1E),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      title: const Text('Report an Issue',
          style: TextStyle(color: Colors.white, fontWeight: FontWeight.w800)),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Song Title',
                style: TextStyle(color: Colors.white54, fontSize: 12)),
            const SizedBox(height: 4),
            Container(
              padding: const EdgeInsets.all(12),
              width: double.infinity,
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.05),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(widget.songTitle ?? 'Unknown Song',
                  style: const TextStyle(color: Colors.white70)),
            ),
            const SizedBox(height: 16),
            const Text('Subject',
                style: TextStyle(color: Colors.white54, fontSize: 12)),
            TextField(
              controller: _subjectCtrl,
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(
                hintText: 'e.g. Audio quality, wrong lyrics',
                hintStyle: const TextStyle(color: Colors.white24),
                filled: true,
                fillColor: Colors.white.withValues(alpha: 0.05),
                border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide.none),
              ),
            ),
            const SizedBox(height: 16),
            const Text('Details',
                style: TextStyle(color: Colors.white54, fontSize: 12)),
            TextField(
              controller: _detailsCtrl,
              maxLines: 4,
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(
                hintText: 'Describe the problem...',
                hintStyle: const TextStyle(color: Colors.white24),
                filled: true,
                fillColor: Colors.white.withValues(alpha: 0.05),
                border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide.none),
              ),
            ),
            const SizedBox(height: 16),
            const Text('Attachment (Optional)',
                style: TextStyle(color: Colors.white54, fontSize: 12)),
            const SizedBox(height: 8),
            OutlinedButton.icon(
              onPressed: () {}, // Stub for attachment
              icon: const Icon(Icons.attach_file_rounded, size: 18),
              label: const Text('Add Screenshot'),
              style: OutlinedButton.styleFrom(
                  foregroundColor: Colors.white70,
                  side: const BorderSide(color: Colors.white12)),
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel',
                style: TextStyle(color: AppColors.onSurfaceMuted))),
        ElevatedButton(
          onPressed: () {
            Navigator.pop(context);
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Report submitted successfully!')),
            );
          },
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primary,
            foregroundColor: Colors.black,
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          ),
          child: const Text('Submit Report',
              style: TextStyle(fontWeight: FontWeight.w700)),
        ),
      ],
    );
  }
}

// ---------------------------------------------------------------------------
// Equalizer Overlay
// ---------------------------------------------------------------------------

void showAdvancedEqualizer(BuildContext context) {
  showModalBottomSheet<void>(
    context: context,
    backgroundColor: Colors.transparent,
    isScrollControlled: true,
    builder: (_) => const _EqualizerSheet(),
  );
}

class _EqualizerSheet extends StatefulWidget {
  const _EqualizerSheet();

  @override
  State<_EqualizerSheet> createState() => _EqualizerSheetState();
}

class _EqualizerSheetState extends State<_EqualizerSheet> {
  final List<double> _values = [0.5, 0.7, 0.4, 0.8, 0.6];
  final List<String> _bands = ['60Hz', '230Hz', '910Hz', '3.6kHz', '14kHz'];

  @override
  Widget build(BuildContext context) {
    return BackdropFilter(
      filter: ImageFilter.blur(sigmaX: 30, sigmaY: 30),
      child: Container(
        padding: const EdgeInsets.all(24),
        decoration: const BoxDecoration(
          color: Color(0xEE111114),
          borderRadius: BorderRadius.vertical(top: Radius.circular(32)),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 40, height: 4,
              decoration: BoxDecoration(
                  color: Colors.white24, borderRadius: BorderRadius.circular(2)),
            ),
            const SizedBox(height: 24),
            Row(
              children: [
                const Icon(Icons.graphic_eq_rounded,
                    color: AppColors.primary, size: 24),
                const SizedBox(width: 12),
                const Text('Advanced Equalizer',
                    style: TextStyle(
                        color: Colors.white,
                        fontSize: 20,
                        fontWeight: FontWeight.w800)),
                const Spacer(),
                Switch(
                  value: true,
                  onChanged: (v) {},
                  activeColor: AppColors.primary,
                ),
              ],
            ),
            const SizedBox(height: 40),
            SizedBox(
              height: 200,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: List.generate(5, (i) {
                  return Column(
                    children: [
                      Expanded(
                        child: RotatedBox(
                          quarterTurns: 3,
                          child: SliderTheme(
                            data: SliderThemeData(
                              trackHeight: 4,
                              thumbShape: const RoundSliderThumbShape(
                                  enabledThumbRadius: 8),
                              overlayShape: const RoundSliderOverlayShape(
                                  overlayRadius: 16),
                              activeTrackColor: AppColors.primary,
                              inactiveTrackColor: Colors.white12,
                              thumbColor: Colors.white,
                            ),
                            child: Slider(
                              value: _values[i],
                              onChanged: (v) => setState(() => _values[i] = v),
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(height: 12),
                      Text(_bands[i],
                          style: const TextStyle(
                              color: Colors.white54,
                              fontSize: 10,
                              fontWeight: FontWeight.w600)),
                    ],
                  );
                }),
              ),
            ),
            const SizedBox(height: 32),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _presetChip('Flat', true),
                _presetChip('Bass Boost', false),
                _presetChip('Vocal', false),
                _presetChip('Rock', false),
              ],
            ),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }

  Widget _presetChip(String label, bool active) => GestureDetector(
        onTap: () => setState(() {}), // Just for UI visual for now
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          decoration: BoxDecoration(
            color: active ? AppColors.primary : Colors.white.withValues(alpha: 0.05),
            borderRadius: BorderRadius.circular(20),
          ),
          child: Text(label,
              style: TextStyle(
                  color: active ? Colors.black : Colors.white70,
                  fontSize: 12,
                  fontWeight: FontWeight.w700)),
        ),
      );
}
