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
    @Default(0) int duration,
    String? album,
    String? genre,
    /// From API `total_views` — streams / play count for UI.
    @JsonKey(name: 'total_views') @Default(0) int totalViews,
  }) = _SongModel;

  factory SongModel.fromJson(Map<String, dynamic> json) => _$SongModelFromJson(json);
}
