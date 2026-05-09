// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'playback_snapshot.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetPlaybackSnapshotCollection on Isar {
  IsarCollection<PlaybackSnapshot> get playbackSnapshots => this.collection();
}

const PlaybackSnapshotSchema = CollectionSchema(
  name: r'PlaybackSnapshot',
  id: -4229008343719500448,
  properties: {
    r'extrasJson': PropertySchema(
      id: 0,
      name: r'extrasJson',
      type: IsarType.string,
    ),
    r'lastPlayed': PropertySchema(
      id: 1,
      name: r'lastPlayed',
      type: IsarType.dateTime,
    ),
    r'mediaId': PropertySchema(
      id: 2,
      name: r'mediaId',
      type: IsarType.string,
    ),
    r'positionMillis': PropertySchema(
      id: 3,
      name: r'positionMillis',
      type: IsarType.long,
    )
  },
  estimateSize: _playbackSnapshotEstimateSize,
  serialize: _playbackSnapshotSerialize,
  deserialize: _playbackSnapshotDeserialize,
  deserializeProp: _playbackSnapshotDeserializeProp,
  idName: r'id',
  indexes: {
    r'mediaId': IndexSchema(
      id: -8001372983137409759,
      name: r'mediaId',
      unique: true,
      replace: true,
      properties: [
        IndexPropertySchema(
          name: r'mediaId',
          type: IndexType.hash,
          caseSensitive: true,
        )
      ],
    )
  },
  links: {},
  embeddedSchemas: {},
  getId: _playbackSnapshotGetId,
  getLinks: _playbackSnapshotGetLinks,
  attach: _playbackSnapshotAttach,
  version: '3.1.0+1',
);

int _playbackSnapshotEstimateSize(
  PlaybackSnapshot object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.extrasJson;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.mediaId.length * 3;
  return bytesCount;
}

void _playbackSnapshotSerialize(
  PlaybackSnapshot object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.extrasJson);
  writer.writeDateTime(offsets[1], object.lastPlayed);
  writer.writeString(offsets[2], object.mediaId);
  writer.writeLong(offsets[3], object.positionMillis);
}

PlaybackSnapshot _playbackSnapshotDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = PlaybackSnapshot();
  object.extrasJson = reader.readStringOrNull(offsets[0]);
  object.id = id;
  object.lastPlayed = reader.readDateTime(offsets[1]);
  object.mediaId = reader.readString(offsets[2]);
  object.positionMillis = reader.readLong(offsets[3]);
  return object;
}

P _playbackSnapshotDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readStringOrNull(offset)) as P;
    case 1:
      return (reader.readDateTime(offset)) as P;
    case 2:
      return (reader.readString(offset)) as P;
    case 3:
      return (reader.readLong(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _playbackSnapshotGetId(PlaybackSnapshot object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _playbackSnapshotGetLinks(PlaybackSnapshot object) {
  return [];
}

void _playbackSnapshotAttach(
    IsarCollection<dynamic> col, Id id, PlaybackSnapshot object) {
  object.id = id;
}

extension PlaybackSnapshotByIndex on IsarCollection<PlaybackSnapshot> {
  Future<PlaybackSnapshot?> getByMediaId(String mediaId) {
    return getByIndex(r'mediaId', [mediaId]);
  }

  PlaybackSnapshot? getByMediaIdSync(String mediaId) {
    return getByIndexSync(r'mediaId', [mediaId]);
  }

  Future<bool> deleteByMediaId(String mediaId) {
    return deleteByIndex(r'mediaId', [mediaId]);
  }

  bool deleteByMediaIdSync(String mediaId) {
    return deleteByIndexSync(r'mediaId', [mediaId]);
  }

  Future<List<PlaybackSnapshot?>> getAllByMediaId(List<String> mediaIdValues) {
    final values = mediaIdValues.map((e) => [e]).toList();
    return getAllByIndex(r'mediaId', values);
  }

  List<PlaybackSnapshot?> getAllByMediaIdSync(List<String> mediaIdValues) {
    final values = mediaIdValues.map((e) => [e]).toList();
    return getAllByIndexSync(r'mediaId', values);
  }

  Future<int> deleteAllByMediaId(List<String> mediaIdValues) {
    final values = mediaIdValues.map((e) => [e]).toList();
    return deleteAllByIndex(r'mediaId', values);
  }

  int deleteAllByMediaIdSync(List<String> mediaIdValues) {
    final values = mediaIdValues.map((e) => [e]).toList();
    return deleteAllByIndexSync(r'mediaId', values);
  }

  Future<Id> putByMediaId(PlaybackSnapshot object) {
    return putByIndex(r'mediaId', object);
  }

  Id putByMediaIdSync(PlaybackSnapshot object, {bool saveLinks = true}) {
    return putByIndexSync(r'mediaId', object, saveLinks: saveLinks);
  }

  Future<List<Id>> putAllByMediaId(List<PlaybackSnapshot> objects) {
    return putAllByIndex(r'mediaId', objects);
  }

  List<Id> putAllByMediaIdSync(List<PlaybackSnapshot> objects,
      {bool saveLinks = true}) {
    return putAllByIndexSync(r'mediaId', objects, saveLinks: saveLinks);
  }
}

extension PlaybackSnapshotQueryWhereSort
    on QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QWhere> {
  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension PlaybackSnapshotQueryWhere
    on QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QWhereClause> {
  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterWhereClause> idEqualTo(
      Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterWhereClause>
      idNotEqualTo(Id id) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(
              IdWhereClause.lessThan(upper: id, includeUpper: false),
            )
            .addWhereClause(
              IdWhereClause.greaterThan(lower: id, includeLower: false),
            );
      } else {
        return query
            .addWhereClause(
              IdWhereClause.greaterThan(lower: id, includeLower: false),
            )
            .addWhereClause(
              IdWhereClause.lessThan(upper: id, includeUpper: false),
            );
      }
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterWhereClause>
      idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterWhereClause>
      idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterWhereClause> idBetween(
    Id lowerId,
    Id upperId, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: lowerId,
        includeLower: includeLower,
        upper: upperId,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterWhereClause>
      mediaIdEqualTo(String mediaId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'mediaId',
        value: [mediaId],
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterWhereClause>
      mediaIdNotEqualTo(String mediaId) {
    return QueryBuilder.apply(this, (query) {
      if (query.whereSort == Sort.asc) {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'mediaId',
              lower: [],
              upper: [mediaId],
              includeUpper: false,
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'mediaId',
              lower: [mediaId],
              includeLower: false,
              upper: [],
            ));
      } else {
        return query
            .addWhereClause(IndexWhereClause.between(
              indexName: r'mediaId',
              lower: [mediaId],
              includeLower: false,
              upper: [],
            ))
            .addWhereClause(IndexWhereClause.between(
              indexName: r'mediaId',
              lower: [],
              upper: [mediaId],
              includeUpper: false,
            ));
      }
    });
  }
}

extension PlaybackSnapshotQueryFilter
    on QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QFilterCondition> {
  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      extrasJsonIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'extrasJson',
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      extrasJsonIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'extrasJson',
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      extrasJsonEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'extrasJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      extrasJsonGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'extrasJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      extrasJsonLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'extrasJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      extrasJsonBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'extrasJson',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      extrasJsonStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'extrasJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      extrasJsonEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'extrasJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      extrasJsonContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'extrasJson',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      extrasJsonMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'extrasJson',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      extrasJsonIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'extrasJson',
        value: '',
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      extrasJsonIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'extrasJson',
        value: '',
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      idGreaterThan(
    Id value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      idLessThan(
    Id value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      idBetween(
    Id lower,
    Id upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'id',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      lastPlayedEqualTo(DateTime value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'lastPlayed',
        value: value,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      lastPlayedGreaterThan(
    DateTime value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'lastPlayed',
        value: value,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      lastPlayedLessThan(
    DateTime value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'lastPlayed',
        value: value,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      lastPlayedBetween(
    DateTime lower,
    DateTime upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'lastPlayed',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      mediaIdEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'mediaId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      mediaIdGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'mediaId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      mediaIdLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'mediaId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      mediaIdBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'mediaId',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      mediaIdStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'mediaId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      mediaIdEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'mediaId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      mediaIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'mediaId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      mediaIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'mediaId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      mediaIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'mediaId',
        value: '',
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      mediaIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'mediaId',
        value: '',
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      positionMillisEqualTo(int value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'positionMillis',
        value: value,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      positionMillisGreaterThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'positionMillis',
        value: value,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      positionMillisLessThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'positionMillis',
        value: value,
      ));
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterFilterCondition>
      positionMillisBetween(
    int lower,
    int upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'positionMillis',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }
}

extension PlaybackSnapshotQueryObject
    on QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QFilterCondition> {}

extension PlaybackSnapshotQueryLinks
    on QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QFilterCondition> {}

extension PlaybackSnapshotQuerySortBy
    on QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QSortBy> {
  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      sortByExtrasJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'extrasJson', Sort.asc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      sortByExtrasJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'extrasJson', Sort.desc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      sortByLastPlayed() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastPlayed', Sort.asc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      sortByLastPlayedDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastPlayed', Sort.desc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      sortByMediaId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'mediaId', Sort.asc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      sortByMediaIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'mediaId', Sort.desc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      sortByPositionMillis() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'positionMillis', Sort.asc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      sortByPositionMillisDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'positionMillis', Sort.desc);
    });
  }
}

extension PlaybackSnapshotQuerySortThenBy
    on QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QSortThenBy> {
  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      thenByExtrasJson() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'extrasJson', Sort.asc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      thenByExtrasJsonDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'extrasJson', Sort.desc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      thenByLastPlayed() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastPlayed', Sort.asc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      thenByLastPlayedDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'lastPlayed', Sort.desc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      thenByMediaId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'mediaId', Sort.asc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      thenByMediaIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'mediaId', Sort.desc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      thenByPositionMillis() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'positionMillis', Sort.asc);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QAfterSortBy>
      thenByPositionMillisDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'positionMillis', Sort.desc);
    });
  }
}

extension PlaybackSnapshotQueryWhereDistinct
    on QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QDistinct> {
  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QDistinct>
      distinctByExtrasJson({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'extrasJson', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QDistinct>
      distinctByLastPlayed() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'lastPlayed');
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QDistinct> distinctByMediaId(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'mediaId', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QDistinct>
      distinctByPositionMillis() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'positionMillis');
    });
  }
}

extension PlaybackSnapshotQueryProperty
    on QueryBuilder<PlaybackSnapshot, PlaybackSnapshot, QQueryProperty> {
  QueryBuilder<PlaybackSnapshot, int, QQueryOperations> idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<PlaybackSnapshot, String?, QQueryOperations>
      extrasJsonProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'extrasJson');
    });
  }

  QueryBuilder<PlaybackSnapshot, DateTime, QQueryOperations>
      lastPlayedProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'lastPlayed');
    });
  }

  QueryBuilder<PlaybackSnapshot, String, QQueryOperations> mediaIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'mediaId');
    });
  }

  QueryBuilder<PlaybackSnapshot, int, QQueryOperations>
      positionMillisProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'positionMillis');
    });
  }
}
