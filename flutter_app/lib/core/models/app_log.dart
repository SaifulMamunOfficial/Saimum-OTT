import 'package:isar/isar.dart';

part 'app_log.g.dart';

enum LogLevel { info, warning, error }

@collection
class AppLog {
  Id id = Isar.autoIncrement;

  @enumerated
  late LogLevel level;

  late String context;
  late String message;
  String? stackTrace;

  /// Milliseconds since epoch.
  @Index()
  late int timestamp;
}
