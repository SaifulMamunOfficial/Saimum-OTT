// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'download_manifest.dart';

// **************************************************************************
// IsarCollectionGenerator
// **************************************************************************

// coverage:ignore-file
// ignore_for_file: duplicate_ignore, non_constant_identifier_names, constant_identifier_names, invalid_use_of_protected_member, unnecessary_cast, prefer_const_constructors, lines_longer_than_80_chars, require_trailing_commas, inference_failure_on_function_invocation, unnecessary_parenthesis, unnecessary_raw_strings, unnecessary_null_checks, join_return_with_assignment, prefer_final_locals, avoid_js_rounded_ints, avoid_positional_boolean_parameters, always_specify_types

extension GetDownloadManifestCollection on Isar {
  IsarCollection<DownloadManifest> get downloadManifests => this.collection();
}

const DownloadManifestSchema = CollectionSchema(
  name: r'DownloadManifest',
  id: 8391474692485648913,
  properties: {
    r'encryptionIv': PropertySchema(
      id: 0,
      name: r'encryptionIv',
      type: IsarType.string,
    ),
    r'fileSize': PropertySchema(
      id: 1,
      name: r'fileSize',
      type: IsarType.long,
    ),
    r'isCompleted': PropertySchema(
      id: 2,
      name: r'isCompleted',
      type: IsarType.bool,
    ),
    r'localPath': PropertySchema(
      id: 3,
      name: r'localPath',
      type: IsarType.string,
    ),
    r'mediaId': PropertySchema(
      id: 4,
      name: r'mediaId',
      type: IsarType.string,
    )
  },
  estimateSize: _downloadManifestEstimateSize,
  serialize: _downloadManifestSerialize,
  deserialize: _downloadManifestDeserialize,
  deserializeProp: _downloadManifestDeserializeProp,
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
  getId: _downloadManifestGetId,
  getLinks: _downloadManifestGetLinks,
  attach: _downloadManifestAttach,
  version: '3.1.0+1',
);

int _downloadManifestEstimateSize(
  DownloadManifest object,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  var bytesCount = offsets.last;
  {
    final value = object.encryptionIv;
    if (value != null) {
      bytesCount += 3 + value.length * 3;
    }
  }
  bytesCount += 3 + object.localPath.length * 3;
  bytesCount += 3 + object.mediaId.length * 3;
  return bytesCount;
}

void _downloadManifestSerialize(
  DownloadManifest object,
  IsarWriter writer,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  writer.writeString(offsets[0], object.encryptionIv);
  writer.writeLong(offsets[1], object.fileSize);
  writer.writeBool(offsets[2], object.isCompleted);
  writer.writeString(offsets[3], object.localPath);
  writer.writeString(offsets[4], object.mediaId);
}

DownloadManifest _downloadManifestDeserialize(
  Id id,
  IsarReader reader,
  List<int> offsets,
  Map<Type, List<int>> allOffsets,
) {
  final object = DownloadManifest();
  object.encryptionIv = reader.readStringOrNull(offsets[0]);
  object.fileSize = reader.readLong(offsets[1]);
  object.id = id;
  object.isCompleted = reader.readBool(offsets[2]);
  object.localPath = reader.readString(offsets[3]);
  object.mediaId = reader.readString(offsets[4]);
  return object;
}

P _downloadManifestDeserializeProp<P>(
  IsarReader reader,
  int propertyId,
  int offset,
  Map<Type, List<int>> allOffsets,
) {
  switch (propertyId) {
    case 0:
      return (reader.readStringOrNull(offset)) as P;
    case 1:
      return (reader.readLong(offset)) as P;
    case 2:
      return (reader.readBool(offset)) as P;
    case 3:
      return (reader.readString(offset)) as P;
    case 4:
      return (reader.readString(offset)) as P;
    default:
      throw IsarError('Unknown property with id $propertyId');
  }
}

Id _downloadManifestGetId(DownloadManifest object) {
  return object.id;
}

List<IsarLinkBase<dynamic>> _downloadManifestGetLinks(DownloadManifest object) {
  return [];
}

void _downloadManifestAttach(
    IsarCollection<dynamic> col, Id id, DownloadManifest object) {
  object.id = id;
}

extension DownloadManifestByIndex on IsarCollection<DownloadManifest> {
  Future<DownloadManifest?> getByMediaId(String mediaId) {
    return getByIndex(r'mediaId', [mediaId]);
  }

  DownloadManifest? getByMediaIdSync(String mediaId) {
    return getByIndexSync(r'mediaId', [mediaId]);
  }

  Future<bool> deleteByMediaId(String mediaId) {
    return deleteByIndex(r'mediaId', [mediaId]);
  }

  bool deleteByMediaIdSync(String mediaId) {
    return deleteByIndexSync(r'mediaId', [mediaId]);
  }

  Future<List<DownloadManifest?>> getAllByMediaId(List<String> mediaIdValues) {
    final values = mediaIdValues.map((e) => [e]).toList();
    return getAllByIndex(r'mediaId', values);
  }

  List<DownloadManifest?> getAllByMediaIdSync(List<String> mediaIdValues) {
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

  Future<Id> putByMediaId(DownloadManifest object) {
    return putByIndex(r'mediaId', object);
  }

  Id putByMediaIdSync(DownloadManifest object, {bool saveLinks = true}) {
    return putByIndexSync(r'mediaId', object, saveLinks: saveLinks);
  }

  Future<List<Id>> putAllByMediaId(List<DownloadManifest> objects) {
    return putAllByIndex(r'mediaId', objects);
  }

  List<Id> putAllByMediaIdSync(List<DownloadManifest> objects,
      {bool saveLinks = true}) {
    return putAllByIndexSync(r'mediaId', objects, saveLinks: saveLinks);
  }
}

extension DownloadManifestQueryWhereSort
    on QueryBuilder<DownloadManifest, DownloadManifest, QWhere> {
  QueryBuilder<DownloadManifest, DownloadManifest, QAfterWhere> anyId() {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(const IdWhereClause.any());
    });
  }
}

extension DownloadManifestQueryWhere
    on QueryBuilder<DownloadManifest, DownloadManifest, QWhereClause> {
  QueryBuilder<DownloadManifest, DownloadManifest, QAfterWhereClause> idEqualTo(
      Id id) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IdWhereClause.between(
        lower: id,
        upper: id,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterWhereClause>
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

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterWhereClause>
      idGreaterThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.greaterThan(lower: id, includeLower: include),
      );
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterWhereClause>
      idLessThan(Id id, {bool include = false}) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(
        IdWhereClause.lessThan(upper: id, includeUpper: include),
      );
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterWhereClause> idBetween(
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

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterWhereClause>
      mediaIdEqualTo(String mediaId) {
    return QueryBuilder.apply(this, (query) {
      return query.addWhereClause(IndexWhereClause.equalTo(
        indexName: r'mediaId',
        value: [mediaId],
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterWhereClause>
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

extension DownloadManifestQueryFilter
    on QueryBuilder<DownloadManifest, DownloadManifest, QFilterCondition> {
  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      encryptionIvIsNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNull(
        property: r'encryptionIv',
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      encryptionIvIsNotNull() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(const FilterCondition.isNotNull(
        property: r'encryptionIv',
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      encryptionIvEqualTo(
    String? value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'encryptionIv',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      encryptionIvGreaterThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'encryptionIv',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      encryptionIvLessThan(
    String? value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'encryptionIv',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      encryptionIvBetween(
    String? lower,
    String? upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'encryptionIv',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      encryptionIvStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'encryptionIv',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      encryptionIvEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'encryptionIv',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      encryptionIvContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'encryptionIv',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      encryptionIvMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'encryptionIv',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      encryptionIvIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'encryptionIv',
        value: '',
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      encryptionIvIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'encryptionIv',
        value: '',
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      fileSizeEqualTo(int value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'fileSize',
        value: value,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      fileSizeGreaterThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'fileSize',
        value: value,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      fileSizeLessThan(
    int value, {
    bool include = false,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'fileSize',
        value: value,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      fileSizeBetween(
    int lower,
    int upper, {
    bool includeLower = true,
    bool includeUpper = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'fileSize',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      idEqualTo(Id value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'id',
        value: value,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
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

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
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

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
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

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      isCompletedEqualTo(bool value) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'isCompleted',
        value: value,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      localPathEqualTo(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'localPath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      localPathGreaterThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        include: include,
        property: r'localPath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      localPathLessThan(
    String value, {
    bool include = false,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.lessThan(
        include: include,
        property: r'localPath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      localPathBetween(
    String lower,
    String upper, {
    bool includeLower = true,
    bool includeUpper = true,
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.between(
        property: r'localPath',
        lower: lower,
        includeLower: includeLower,
        upper: upper,
        includeUpper: includeUpper,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      localPathStartsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.startsWith(
        property: r'localPath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      localPathEndsWith(
    String value, {
    bool caseSensitive = true,
  }) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.endsWith(
        property: r'localPath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      localPathContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'localPath',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      localPathMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'localPath',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      localPathIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'localPath',
        value: '',
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      localPathIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'localPath',
        value: '',
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
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

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
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

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
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

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
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

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
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

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
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

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      mediaIdContains(String value, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.contains(
        property: r'mediaId',
        value: value,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      mediaIdMatches(String pattern, {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.matches(
        property: r'mediaId',
        wildcard: pattern,
        caseSensitive: caseSensitive,
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      mediaIdIsEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.equalTo(
        property: r'mediaId',
        value: '',
      ));
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterFilterCondition>
      mediaIdIsNotEmpty() {
    return QueryBuilder.apply(this, (query) {
      return query.addFilterCondition(FilterCondition.greaterThan(
        property: r'mediaId',
        value: '',
      ));
    });
  }
}

extension DownloadManifestQueryObject
    on QueryBuilder<DownloadManifest, DownloadManifest, QFilterCondition> {}

extension DownloadManifestQueryLinks
    on QueryBuilder<DownloadManifest, DownloadManifest, QFilterCondition> {}

extension DownloadManifestQuerySortBy
    on QueryBuilder<DownloadManifest, DownloadManifest, QSortBy> {
  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      sortByEncryptionIv() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'encryptionIv', Sort.asc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      sortByEncryptionIvDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'encryptionIv', Sort.desc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      sortByFileSize() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'fileSize', Sort.asc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      sortByFileSizeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'fileSize', Sort.desc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      sortByIsCompleted() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isCompleted', Sort.asc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      sortByIsCompletedDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isCompleted', Sort.desc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      sortByLocalPath() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'localPath', Sort.asc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      sortByLocalPathDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'localPath', Sort.desc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      sortByMediaId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'mediaId', Sort.asc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      sortByMediaIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'mediaId', Sort.desc);
    });
  }
}

extension DownloadManifestQuerySortThenBy
    on QueryBuilder<DownloadManifest, DownloadManifest, QSortThenBy> {
  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      thenByEncryptionIv() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'encryptionIv', Sort.asc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      thenByEncryptionIvDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'encryptionIv', Sort.desc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      thenByFileSize() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'fileSize', Sort.asc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      thenByFileSizeDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'fileSize', Sort.desc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy> thenById() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.asc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      thenByIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'id', Sort.desc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      thenByIsCompleted() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isCompleted', Sort.asc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      thenByIsCompletedDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'isCompleted', Sort.desc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      thenByLocalPath() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'localPath', Sort.asc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      thenByLocalPathDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'localPath', Sort.desc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      thenByMediaId() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'mediaId', Sort.asc);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QAfterSortBy>
      thenByMediaIdDesc() {
    return QueryBuilder.apply(this, (query) {
      return query.addSortBy(r'mediaId', Sort.desc);
    });
  }
}

extension DownloadManifestQueryWhereDistinct
    on QueryBuilder<DownloadManifest, DownloadManifest, QDistinct> {
  QueryBuilder<DownloadManifest, DownloadManifest, QDistinct>
      distinctByEncryptionIv({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'encryptionIv', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QDistinct>
      distinctByFileSize() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'fileSize');
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QDistinct>
      distinctByIsCompleted() {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'isCompleted');
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QDistinct>
      distinctByLocalPath({bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'localPath', caseSensitive: caseSensitive);
    });
  }

  QueryBuilder<DownloadManifest, DownloadManifest, QDistinct> distinctByMediaId(
      {bool caseSensitive = true}) {
    return QueryBuilder.apply(this, (query) {
      return query.addDistinctBy(r'mediaId', caseSensitive: caseSensitive);
    });
  }
}

extension DownloadManifestQueryProperty
    on QueryBuilder<DownloadManifest, DownloadManifest, QQueryProperty> {
  QueryBuilder<DownloadManifest, int, QQueryOperations> idProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'id');
    });
  }

  QueryBuilder<DownloadManifest, String?, QQueryOperations>
      encryptionIvProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'encryptionIv');
    });
  }

  QueryBuilder<DownloadManifest, int, QQueryOperations> fileSizeProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'fileSize');
    });
  }

  QueryBuilder<DownloadManifest, bool, QQueryOperations> isCompletedProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'isCompleted');
    });
  }

  QueryBuilder<DownloadManifest, String, QQueryOperations> localPathProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'localPath');
    });
  }

  QueryBuilder<DownloadManifest, String, QQueryOperations> mediaIdProperty() {
    return QueryBuilder.apply(this, (query) {
      return query.addPropertyName(r'mediaId');
    });
  }
}
