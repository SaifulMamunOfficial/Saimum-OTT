// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'song_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$SongModelImpl _$$SongModelImplFromJson(Map<String, dynamic> json) =>
    _$SongModelImpl(
      id: (json['id'] as num).toInt(),
      title: json['title'] as String,
      artist: json['artist'] as String,
      audioUrl: json['audio_url'] as String,
      thumbnail: json['thumbnail'] as String,
      duration: (json['duration'] as num?)?.toInt() ?? 0,
      album: json['album'] as String?,
      genre: json['genre'] as String?,
      totalViews: (json['total_views'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$$SongModelImplToJson(_$SongModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'title': instance.title,
      'artist': instance.artist,
      'audio_url': instance.audioUrl,
      'thumbnail': instance.thumbnail,
      'duration': instance.duration,
      'album': instance.album,
      'genre': instance.genre,
      'total_views': instance.totalViews,
    };
