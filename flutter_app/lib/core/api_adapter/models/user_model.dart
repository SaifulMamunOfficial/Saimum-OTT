import 'dart:convert';

/// Represents the authenticated user, including multi-role support.
///
/// `role` values expected from the Laravel backend:
///   - `'user'`    — regular subscriber (default)
///   - `'student'` — student plan; shows Academic Hub features
///   - `'admin'`   — internal admin (optional future use)
class UserModel {
  final int id;
  final String name;
  final String email;

  /// User's role — drives feature-flag visibility (e.g. Academic Hub).
  final String role;

  /// Only present when `role == 'student'`.
  final String? studentId;

  /// Bearer token for API requests.
  final String token;

  /// Remote URL for the profile avatar.
  final String? profileImage;

  const UserModel({
    required this.id,
    required this.name,
    required this.email,
    required this.role,
    required this.token,
    this.studentId,
    this.profileImage,
  });

  // ---------------------------------------------------------------------------
  // Convenience
  // ---------------------------------------------------------------------------

  bool get isStudent => role == 'student';
  bool get isAdmin => role == 'admin';

  // ---------------------------------------------------------------------------
  // Serialisation — stored as JSON string in flutter_secure_storage
  // ---------------------------------------------------------------------------

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: (json['id'] ?? json['user_id'] as num?)?.toInt() ?? 0,
      name: (json['name'] ?? json['user_name']) as String? ?? '',
      email: (json['email'] ?? json['user_email']) as String? ?? '',
      role: json['role'] as String? ?? 'user',
      token: (json['token'] ?? json['access_token']) as String? ?? '',
      studentId: json['student_id'] as String?,
      profileImage: (json['profile_image'] ?? json['profile_img']) as String?,
    );
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'email': email,
        'role': role,
        'token': token,
        if (studentId != null) 'student_id': studentId,
        if (profileImage != null) 'profile_image': profileImage,
      };

  /// Encodes this model to a JSON string suitable for SecureStorage.
  String toJsonString() => jsonEncode(toJson());

  /// Decodes a JSON string produced by [toJsonString].
  static UserModel fromJsonString(String s) =>
      UserModel.fromJson(jsonDecode(s) as Map<String, dynamic>);

  UserModel copyWith({
    int? id,
    String? name,
    String? email,
    String? role,
    String? token,
    String? studentId,
    String? profileImage,
  }) {
    return UserModel(
      id: id ?? this.id,
      name: name ?? this.name,
      email: email ?? this.email,
      role: role ?? this.role,
      token: token ?? this.token,
      studentId: studentId ?? this.studentId,
      profileImage: profileImage ?? this.profileImage,
    );
  }
}
