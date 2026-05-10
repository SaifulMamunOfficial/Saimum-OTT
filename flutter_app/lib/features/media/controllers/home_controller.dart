import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:saimummusic/core/api_adapter/home_repository.dart';

final homeDataProvider = FutureProvider<Map<String, dynamic>>((ref) async {
  final repo = ref.watch(homeRepositoryProvider);
  return repo.getHomeData();
});
