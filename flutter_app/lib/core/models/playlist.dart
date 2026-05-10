import 'package:isar/isar.dart';

part 'playlist.g.dart';

@collection
class PlaylistModel {
  Id id = Isar.autoIncrement;

  @Index()
  int? cloudId; // ID from Laravel

  late String name;
  String? imageUrl;

  late DateTime createdAt;
  late DateTime updatedAt;

  List<int> songIds = []; // Comma-separated list or just list of IDs

  bool isSynced = false;
}
