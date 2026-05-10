import 'package:isar/isar.dart';

part 'favorite.g.dart';

@collection
class FavoriteModel {
  Id id = Isar.autoIncrement;

  @Index(unique: true, replace: true)
  late int postId;

  late String type; // e.g., 'audio'

  late DateTime createdAt;

  // Track if this is synced to cloud
  bool isSynced = false;
}
