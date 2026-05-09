import 'package:isar/isar.dart';

part 'download_manifest.g.dart';

@collection
class DownloadManifest {
  Id id = Isar.autoIncrement;

  @Index(unique: true, replace: true)
  late String mediaId;

  late String localPath;

  late int fileSize;

  late bool isCompleted;

  String? encryptionIv;
}
