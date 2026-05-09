import 'package:isar/isar.dart';

part 'download_manifest.g.dart';

@collection
class DownloadManifest {
  Id id = Isar.autoIncrement;

  @Index(unique: true, replace: true)
  late String mediaId;

  late String title;
  late String artist;
  late String thumbnailUrl;

  late String localPath;

  late int fileSize;

  late int totalChunks;
  late int downloadedChunks;

  late bool isCompleted;

  String? encryptionIv;
}
