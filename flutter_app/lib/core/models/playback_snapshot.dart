import 'package:isar/isar.dart';

part 'playback_snapshot.g.dart';

@collection
class PlaybackSnapshot {
  Id id = Isar.autoIncrement;

  @Index(unique: true, replace: true)
  late String mediaId;

  late int positionMillis;

  late DateTime lastPlayed;

  String? extrasJson;
}
