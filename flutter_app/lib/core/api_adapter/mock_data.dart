import 'models/song_model.dart';

// ---------------------------------------------------------------------------
// MockArtist — plain Dart, no code-gen needed
// ---------------------------------------------------------------------------

class MockArtist {
  final int id;
  final String name;
  final String imageUrl;
  final String bio;
  const MockArtist({
    required this.id,
    required this.name,
    required this.imageUrl,
    required this.bio,
  });
}

const List<MockArtist> kMockArtists = [
  MockArtist(id: 1, name: 'Echo Wave', imageUrl: 'https://picsum.photos/seed/a1/400/400', bio: 'Electronic producer known for neon-soaked synth landscapes.'),
  MockArtist(id: 2, name: 'Luna Frost', imageUrl: 'https://picsum.photos/seed/a2/400/400', bio: 'Chill-wave artist crafting midnight soundscapes.'),
  MockArtist(id: 3, name: 'Starfall', imageUrl: 'https://picsum.photos/seed/a3/400/400', bio: 'Ambient composer exploring cosmic frequencies.'),
];

// ---------------------------------------------------------------------------
// MockAlbum — plain Dart, no code-gen needed
// ---------------------------------------------------------------------------

class MockAlbum {
  final int id;
  final String title;
  final String artistName;
  final String imageUrl;
  final int releaseYear;
  const MockAlbum({
    required this.id,
    required this.title,
    required this.artistName,
    required this.imageUrl,
    required this.releaseYear,
  });
}

const List<MockAlbum> kMockAlbums = [
  MockAlbum(id: 1, title: 'Synthwave Dreams', artistName: 'Echo Wave', imageUrl: 'https://picsum.photos/seed/al1/300/300', releaseYear: 2023),
  MockAlbum(id: 2, title: 'City Lights', artistName: 'Luna Frost', imageUrl: 'https://picsum.photos/seed/al2/300/300', releaseYear: 2024),
  MockAlbum(id: 3, title: 'Outer Space', artistName: 'Starfall', imageUrl: 'https://picsum.photos/seed/al3/300/300', releaseYear: 2024),
];

// ---------------------------------------------------------------------------
// Lookup helpers
// ---------------------------------------------------------------------------

MockArtist? artistById(int id) {
  final m = kMockArtists.where((a) => a.id == id);
  return m.isEmpty ? null : m.first;
}

MockAlbum? albumById(int id) {
  final m = kMockAlbums.where((a) => a.id == id);
  return m.isEmpty ? null : m.first;
}

MockArtist? artistByName(String name) {
  final m = kMockArtists.where((a) => a.name == name);
  return m.isEmpty ? null : m.first;
}

MockAlbum? albumByTitle(String title) {
  final m = kMockAlbums.where((a) => a.title == title);
  return m.isEmpty ? null : m.first;
}

List<SongModel> songsForArtist(String artistName) =>
    kMockSongs.where((s) => s.artist == artistName).toList();

List<SongModel> songsForAlbum(String albumTitle) =>
    kMockSongs.where((s) => s.album == albumTitle).toList();

// ---------------------------------------------------------------------------
// Mock video items — replace with real API data in Phase 7+
// ---------------------------------------------------------------------------

class MockVideo {
  final int id;
  final String title;
  final String artist;
  final String url;
  final String thumb;
  const MockVideo({
    required this.id,
    required this.title,
    required this.artist,
    required this.url,
    required this.thumb,
  });
}

const List<MockVideo> kMockVideos = [
  MockVideo(id: 1, title: 'For Bigger Blazes', artist: 'Google Samples', url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', thumb: 'https://storage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg'),
  MockVideo(id: 2, title: 'Big Buck Bunny', artist: 'Blender Foundation', url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4', thumb: 'https://storage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg'),
  MockVideo(id: 3, title: 'Elephant Dream', artist: 'Blender Foundation', url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4', thumb: 'https://storage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg'),
  MockVideo(id: 4, title: 'For Bigger Escapes', artist: 'Google Samples', url: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', thumb: 'https://storage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerEscapes.jpg'),
];

/// Temporary mock songs — replace with real API once Task 3.2 endpoint is live.
const List<SongModel> kMockSongs = [
  SongModel(id: 1, title: 'Neon Horizons', artist: 'Echo Wave', audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3', thumbnail: 'https://picsum.photos/seed/s1/300/300', duration: 372, album: 'Synthwave Dreams', genre: 'Electronic', totalViews: 128400),
  SongModel(id: 2, title: 'Midnight Drive', artist: 'Luna Frost', audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3', thumbnail: 'https://picsum.photos/seed/s2/300/300', duration: 289, album: 'City Lights', genre: 'Chill', totalViews: 45200),
  SongModel(id: 3, title: 'Cosmic Drift', artist: 'Starfall', audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3', thumbnail: 'https://picsum.photos/seed/s3/300/300', duration: 321, album: 'Outer Space', genre: 'Ambient', totalViews: 8900),
  SongModel(id: 4, title: 'Violet Storm', artist: 'Neon Pulse', audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3', thumbnail: 'https://picsum.photos/seed/s4/300/300', duration: 254, album: 'Electric Skies', genre: 'Electronic', totalViews: 2100000),
  SongModel(id: 5, title: 'Ocean Glitch', artist: 'Wave.exe', audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3', thumbnail: 'https://picsum.photos/seed/s5/300/300', duration: 308, album: 'Deep Blue', genre: 'Lo-Fi', totalViews: 333),
  SongModel(id: 6, title: 'Solar Wind', artist: 'Astral Keys', audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3', thumbnail: 'https://picsum.photos/seed/s6/300/300', duration: 412, album: 'Solar Flares', genre: 'Ambient', totalViews: 12050),
  SongModel(id: 7, title: 'Binary Bloom', artist: 'Circuit Sage', audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3', thumbnail: 'https://picsum.photos/seed/s7/300/300', duration: 275, album: 'Code Garden', genre: 'Electronic', totalViews: 5600000),
  SongModel(id: 8, title: 'Aurora Pulse', artist: 'Deep Freeze', audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3', thumbnail: 'https://picsum.photos/seed/s8/300/300', duration: 340, album: 'Northern Lights', genre: 'Chill', totalViews: 42),
];

/// First 3 used as hero/featured cards.
List<SongModel> get kFeaturedSongs => kMockSongs.take(3).toList();

/// Songs 4–8 shown in trending horizontal scroll.
List<SongModel> get kTrendingSongs => kMockSongs.skip(3).toList();
