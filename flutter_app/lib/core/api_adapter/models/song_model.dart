import 'package:freezed_annotation/freezed_annotation.dart';

part 'song_model.freezed.dart';
part 'song_model.g.dart';

@freezed
class SongModel with _$SongModel {
  const factory SongModel({
    required int id,
    required String title,
    required String artist,
    @JsonKey(name: 'audio_url') required String audioUrl,
    required String thumbnail,
    /// Duration in seconds.
    required int duration,
    @JsonKey(name: 'album') String? album,
    @JsonKey(name: 'genre') String? genre,
  }) = _SongModel;

  factory SongModel.fromJson(Map<String, dynamic> json) =>
      _$SongModelFromJson(json);
}
