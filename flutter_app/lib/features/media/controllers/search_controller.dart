import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api_adapter/mock_data.dart';
import '../../../core/api_adapter/models/song_model.dart';

// ---------------------------------------------------------------------------
// State
// ---------------------------------------------------------------------------

class SearchState {
  final String query;
  final List<SongModel> results;
  final bool isSearching;

  const SearchState({
    this.query = '',
    this.results = const [],
    this.isSearching = false,
  });

  SearchState copyWith({
    String? query,
    List<SongModel>? results,
    bool? isSearching,
  }) =>
      SearchState(
        query: query ?? this.query,
        results: results ?? this.results,
        isSearching: isSearching ?? this.isSearching,
      );

  bool get isEmpty => query.isEmpty;
  bool get hasResults => results.isNotEmpty;
}

// ---------------------------------------------------------------------------
// Provider
// ---------------------------------------------------------------------------

final searchControllerProvider =
    StateNotifierProvider<SearchController, SearchState>(
  (_) => SearchController(),
);

// ---------------------------------------------------------------------------
// Controller
// ---------------------------------------------------------------------------

class SearchController extends StateNotifier<SearchState> {
  Timer? _debounce;

  SearchController() : super(const SearchState());

  void search(String query) {
    _debounce?.cancel();

    if (query.trim().isEmpty) {
      state = const SearchState();
      return;
    }

    // Show immediate "searching" indicator, then debounce the actual filter.
    state = state.copyWith(query: query, isSearching: true);

    _debounce = Timer(const Duration(milliseconds: 300), () {
      final q = query.toLowerCase().trim();
      final results = kMockSongs
          .where(
            (s) =>
                s.title.toLowerCase().contains(q) ||
                s.artist.toLowerCase().contains(q) ||
                (s.genre?.toLowerCase().contains(q) ?? false) ||
                (s.album?.toLowerCase().contains(q) ?? false),
          )
          .toList();

      if (mounted) {
        state = SearchState(
          query: query,
          results: results,
          isSearching: false,
        );
      }
    });
  }

  void clear() {
    _debounce?.cancel();
    state = const SearchState();
  }

  @override
  void dispose() {
    _debounce?.cancel();
    super.dispose();
  }
}
