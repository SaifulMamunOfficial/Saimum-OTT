import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:saimummusic/core/api_adapter/api_client.dart';
import 'models/song_model.dart';

final homeRepositoryProvider = Provider((ref) => HomeRepository());

class HomeRepository {
  Future<Map<String, dynamic>> getHomeData() async {
    final response = await ApiClient.instance.dio.get('home');
    final data = response.data;

    const baseUrl = 'http://192.168.0.170/saimum/Laravel-Backend-Code/images/';
    return {
      'banners': (data['banners'] as List<dynamic>).map((e) => {
        'id': e['bid'],
        'title': e['banner_title'],
        'image': e['banner_image'] != null ? '$baseUrl${e['banner_image']}' : '',
        'info': e['banner_info'],
        'post_id': e['banner_post_id'],
      }).toList(),
      'trending': (data['trending'] as List<dynamic>)
          .map((e) => SongModel.fromJson(_normalizeSong(e as Map<String, dynamic>)))
          .toList(),
      'recent': (data['recent'] as List<dynamic>)
          .map((e) => SongModel.fromJson(_normalizeSong(e as Map<String, dynamic>)))
          .toList(),
      'music_trending': (data['music_trending'] as List<dynamic>)
          .map((e) => SongModel.fromJson(_normalizeSong(e as Map<String, dynamic>)))
          .toList(),
      'music_recent': (data['music_recent'] as List<dynamic>)
          .map((e) => SongModel.fromJson(_normalizeSong(e as Map<String, dynamic>)))
          .toList(),
      'albums': (data['albums'] as List<dynamic>).map((e) => {
        'id': e['aid'],
        'name': e['album_name'],
        'image': e['album_image'] != null ? '$baseUrl${e['album_image']}' : '',
      }).toList(),
      'artists': (data['artists'] as List<dynamic>).map((e) => {
        'id': e['id'],
        'name': e['artist_name'],
        'image': e['artist_image'] != null ? '$baseUrl${e['artist_image']}' : '',
      }).toList(),
    };
  }

  Map<String, dynamic> _normalizeSong(Map<String, dynamic> json, {String? fallbackThumbnail}) {
    const baseUrl = 'http://192.168.0.170/saimum/Laravel-Backend-Code/images/';
    final idRaw = json['id'] ?? json['audio_id'];
    final id = idRaw is num ? idRaw.toInt() : int.tryParse('$idRaw') ?? 0;

    final viewsRaw = json['total_views'];
    final totalViews = viewsRaw is num
        ? viewsRaw.toInt()
        : int.tryParse('$viewsRaw') ?? 0;

    // Artwork priority: audio_thumbnail > album_image > artist_image > fallbackThumbnail
    String thumbnail = '';
    if (json['audio_thumbnail'] != null && json['audio_thumbnail'].toString().isNotEmpty) {
      thumbnail = '$baseUrl${json['audio_thumbnail']}';
    } else if (json['album_image'] != null && json['album_image'].toString().isNotEmpty) {
      thumbnail = '$baseUrl${json['album_image']}';
    } else if (json['artist_image'] != null && json['artist_image'].toString().isNotEmpty) {
      thumbnail = '$baseUrl${json['artist_image']}';
    } else if (fallbackThumbnail != null) {
      thumbnail = fallbackThumbnail;
    }

    return {
      'id': id,
      'title': (json['title'] ?? json['audio_title']) as String? ?? 'Unknown Title',
      'artist': (json['artist'] ?? json['audio_artist']) as String? ?? 'Unknown Artist',
      'audio_url': (json['audio_url'] ?? json['audio_url_high'] ?? json['audio_url_low']) as String? ?? '',
      'thumbnail': thumbnail,
      'duration': (json['duration'] as num?)?.toInt() ?? 0,
      'album': (json['album'] ?? json['album_name']) as String?,
      'genre': json['genre'] as String?,
      'total_views': totalViews,
    };
  }

  /// POST `/api/songs/{id}/increment-views` — returns new `total_views` or throws.
  Future<int> incrementSongViews(int songId) async {
    final response = await ApiClient.instance.dio.post(
      'songs/$songId/increment-views',
    );
    final data = response.data;
    if (data is Map<String, dynamic>) {
      final v = data['total_views'];
      if (v is num) return v.toInt();
      if (v is String) return int.tryParse(v) ?? 0;
    }
    return 0;
  }

  Future<Map<String, dynamic>> getAlbumDetails(int id) async {
    final response = await ApiClient.instance.dio.get('album/$id');
    final data = response.data;
    const baseUrl = 'http://192.168.0.170/saimum/Laravel-Backend-Code/images/';
    final albumImg = data['album']['album_image'] != null ? '$baseUrl${data['album']['album_image']}' : '';
    
    return {
      'album': {
        'id': data['album']['aid'],
        'name': data['album']['album_name'],
        'image': albumImg,
        'artist': data['album']['album_artist'] ?? 'Various Artists',
      },
      'tracks': (data['tracks'] as List).map((e) => SongModel.fromJson(_normalizeSong(e, fallbackThumbnail: albumImg))).toList(),
    };
  }

  Future<Map<String, dynamic>> getArtistDetails(int id) async {
    final response = await ApiClient.instance.dio.get('artist/$id');
    final data = response.data;
    const baseUrl = 'http://192.168.0.170/saimum/Laravel-Backend-Code/images/';
    final artistImg = data['artist']['artist_image'] != null ? '$baseUrl${data['artist']['artist_image']}' : '';

    final rawAlbums = data['albums'];
    final albumsList = rawAlbums is List<dynamic> ? rawAlbums : const <dynamic>[];

    return {
      'artist': {
        'id': data['artist']['id'],
        'name': data['artist']['artist_name'],
        'image': data['artist']['artist_image'] != null ? '$baseUrl${data['artist']['artist_image']}' : '',
        'bio': data['artist']['artist_info'] ?? data['artist']['bio'],
      },
      'albums': albumsList.map((e) {
        final m = e as Map<String, dynamic>;
        return {
          'id': m['aid'],
          'name': m['album_name'],
          'image': m['album_image'] != null ? '$baseUrl${m['album_image']}' : '',
          'artist': m['album_artist'] ?? data['artist']['artist_name'] ?? 'Various Artists',
        };
      }).toList(),
      'tracks': (data['tracks'] as List).map((e) => SongModel.fromJson(_normalizeSong(e, fallbackThumbnail: artistImg))).toList(),
    };
  }

  Future<Map<String, dynamic>> getBannerDetails(int id) async {
    final response = await ApiClient.instance.dio.get('banner/$id');
    final data = response.data;
    const baseUrl = 'http://192.168.0.170/saimum/Laravel-Backend-Code/images/';

    return {
      'banner': {
        'id': data['banner']['bid'],
        'title': data['banner']['banner_title'],
        'image': data['banner']['banner_image'] != null ? '$baseUrl${data['banner']['banner_image']}' : '',
      },
      'tracks': (data['tracks'] as List).map((e) => SongModel.fromJson(_normalizeSong(e))).toList(),
    };
  }

  Future<Map<String, dynamic>> getAllSongs({int page = 1}) async {
    final response = await ApiClient.instance.dio.get('all-songs?page=$page');
    final data = response.data;
    
    return {
      'tracks': (data['data']['data'] as List).map((e) => SongModel.fromJson(_normalizeSong(e))).toList(),
      'last_page': data['data']['last_page'],
      'current_page': data['data']['current_page'],
    };
  }

  Future<Map<String, dynamic>> getAllAlbums({int page = 1}) async {
    final response = await ApiClient.instance.dio.get('all-albums?page=$page');
    final data = response.data;
    const baseUrl = 'http://192.168.0.170/saimum/Laravel-Backend-Code/images/';

    return {
      'albums': (data['data']['data'] as List).map((e) => {
        'id': e['aid'],
        'name': e['album_name'],
        'image': e['album_image'] != null ? '$baseUrl${e['album_image']}' : '',
        'artist': e['album_artist'] ?? 'Various Artists',
      }).toList(),
      'last_page': data['data']['last_page'],
      'current_page': data['data']['current_page'],
    };
  }

  Future<Map<String, dynamic>> getAllArtists({int page = 1}) async {
    final response = await ApiClient.instance.dio.get('all-artists?page=$page');
    final data = response.data;
    const baseUrl = 'http://192.168.0.170/saimum/Laravel-Backend-Code/images/';

    return {
      'artists': (data['data']['data'] as List).map((e) => {
        'id': e['id'],
        'name': e['artist_name'],
        'image': e['artist_image'] != null ? '$baseUrl${e['artist_image']}' : '',
      }).toList(),
      'last_page': data['data']['last_page'],
      'current_page': data['data']['current_page'],
    };
  }
}
