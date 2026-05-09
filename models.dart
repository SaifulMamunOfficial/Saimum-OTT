import 'package:freezed_annotation/freezed_annotation.dart';

// এই ফাইলগুলোর নাম আপনার ডার্ট ফাইলের নামের সাথে মিলতে হবে।
// যদি ফাইলের নাম models.dart হয়, তবে নিচে models.freezed.dart লিখবেন।
part 'models.freezed.dart';
part 'models.g.dart';

// ==========================================
// 1. Audio / Song Model
// ==========================================
@freezed
class AudioModel with _$AudioModel {
  const factory AudioModel({
    @JsonKey(name: 'id') dynamic id,
    @JsonKey(name: 'audio_title') String? audioTitle,
    @JsonKey(name: 'audio_url') String? audioUrl,
    @JsonKey(name: 'audio_url_high') String? audioUrlHigh,
    @JsonKey(name: 'audio_url_low') String? audioUrlLow,
    @JsonKey(name: 'image') String? image,
    @JsonKey(name: 'audio_artist') String? audioArtist,
    @JsonKey(name: 'audio_description') String? audioDescription,
    @JsonKey(name: 'rate_avg') dynamic rateAvg,
    @JsonKey(name: 'total_views') dynamic totalViews,
    @JsonKey(name: 'total_download') dynamic totalDownload,
    @JsonKey(name: 'is_favourite') bool? isFavourite,
  }) = _AudioModel;

  factory AudioModel.fromJson(Map<String, dynamic> json) =>
      _$AudioModelFromJson(json);
}

// ==========================================
// 2. Category Model
// ==========================================
@freezed
class CategoryModel with _$CategoryModel {
  const factory CategoryModel({
    @JsonKey(name: 'cid') dynamic cid,
    @JsonKey(name: 'category_name') String? categoryName,
    @JsonKey(name: 'category_image') String? categoryImage,
  }) = _CategoryModel;

  factory CategoryModel.fromJson(Map<String, dynamic> json) =>
      _$CategoryModelFromJson(json);
}

// ==========================================
// 3. Artist Model
// ==========================================
@freezed
class ArtistModel with _$ArtistModel {
  const factory ArtistModel({
    @JsonKey(name: 'id') dynamic id,
    @JsonKey(name: 'artist_name') String? artistName,
    @JsonKey(name: 'artist_image') String? artistImage,
  }) = _ArtistModel;

  factory ArtistModel.fromJson(Map<String, dynamic> json) =>
      _$ArtistModelFromJson(json);
}

// ==========================================
// 4. Album Model
// ==========================================
@freezed
class AlbumModel with _$AlbumModel {
  const factory AlbumModel({
    @JsonKey(name: 'aid') dynamic aid,
    @JsonKey(name: 'album_name') String? albumName,
    @JsonKey(name: 'album_image') String? albumImage,
  }) = _AlbumModel;

  factory AlbumModel.fromJson(Map<String, dynamic> json) =>
      _$AlbumModelFromJson(json);
}

// ==========================================
// 5. Playlist Model
// ==========================================
@freezed
class PlaylistModel with _$PlaylistModel {
  const factory PlaylistModel({
    @JsonKey(name: 'pid') dynamic pid,
    @JsonKey(name: 'playlist_name') String? playlistName,
    @JsonKey(name: 'playlist_image') String? playlistImage,
  }) = _PlaylistModel;

  factory PlaylistModel.fromJson(Map<String, dynamic> json) =>
      _$PlaylistModelFromJson(json);
}

// ==========================================
// 6. Banner / Slider Model
// ==========================================
@freezed
class BannerModel with _$BannerModel {
  const factory BannerModel({
    @JsonKey(name: 'bid') dynamic bid,
    @JsonKey(name: 'banner_title') String? bannerTitle,
    @JsonKey(name: 'banner_info') String? bannerInfo,
    @JsonKey(name: 'banner_image') String? bannerImage,
  }) = _BannerModel;

  factory BannerModel.fromJson(Map<String, dynamic> json) =>
      _$BannerModelFromJson(json);
}

// ==========================================
// 7. Generic Post Model (Used in Search & Home Sections)
// ==========================================
@freezed
class GenericPostModel with _$GenericPostModel {
  const factory GenericPostModel({
    @JsonKey(name: 'post_id') dynamic postId,
    @JsonKey(name: 'post_title') String? postTitle,
    @JsonKey(name: 'post_image') String? postImage,
  }) = _GenericPostModel;

  factory GenericPostModel.fromJson(Map<String, dynamic> json) =>
      _$GenericPostModelFromJson(json);
}

// ==========================================
// 8. News Model
// ==========================================
@freezed
class NewsModel with _$NewsModel {
  const factory NewsModel({
    @JsonKey(name: 'id') dynamic id,
    @JsonKey(name: 'news_title') String? newsTitle,
    @JsonKey(name: 'news_url') String? newsUrl,
  }) = _NewsModel;

  factory NewsModel.fromJson(Map<String, dynamic> json) =>
      _$NewsModelFromJson(json);
}

// ==========================================
// 9. Subscription Model
// ==========================================
@freezed
class SubscriptionModel with _$SubscriptionModel {
  const factory SubscriptionModel({
    @JsonKey(name: 'id') dynamic id,
    @JsonKey(name: 'plan_name') String? planName,
    @JsonKey(name: 'plan_duration') String? planDuration,
    @JsonKey(name: 'plan_price') String? planPrice,
    @JsonKey(name: 'currency_code') String? currencyCode,
    @JsonKey(name: 'subscription_id') String? subscriptionId,
    @JsonKey(name: 'base_key') String? baseKey,
  }) = _SubscriptionModel;

  factory SubscriptionModel.fromJson(Map<String, dynamic> json) =>
      _$SubscriptionModelFromJson(json);
}

// ==========================================
// 10. Rating Model
// ==========================================
@freezed
class RatingModel with _$RatingModel {
  const factory RatingModel({
    @JsonKey(name: 'id') dynamic id,
    @JsonKey(name: 'rate') String? rate,
    @JsonKey(name: 'message') String? message,
    @JsonKey(name: 'user_name') String? userName,
    @JsonKey(name: 'user_profile') String? userProfile,
  }) = _RatingModel;

  factory RatingModel.fromJson(Map<String, dynamic> json) =>
      _$RatingModelFromJson(json);
}

// ==========================================
// 11. Notification Model
// ==========================================
@freezed
class NotificationModel with _$NotificationModel {
  const factory NotificationModel({
    @JsonKey(name: 'id') dynamic id,
    @JsonKey(name: 'notification_title') String? notificationTitle,
    @JsonKey(name: 'notification_msg') String? notificationMsg,
    @JsonKey(name: 'notification_on') String? notificationOn,
  }) = _NotificationModel;

  factory NotificationModel.fromJson(Map<String, dynamic> json) =>
      _$NotificationModelFromJson(json);
}

// ==========================================
// 12. User Model
// ==========================================
@freezed
class UserModel with _$UserModel {
  const factory UserModel({
    @JsonKey(name: 'user_id') dynamic userId,
    @JsonKey(name: 'user_name') String? userName,
    @JsonKey(name: 'user_email') String? userEmail,
    @JsonKey(name: 'user_phone') String? userPhone,
    @JsonKey(name: 'user_gender') String? userGender,
    @JsonKey(name: 'profile_img') String? profileImg,
    @JsonKey(name: 'auth_id') String? authId,
  }) = _UserModel;

  factory UserModel.fromJson(Map<String, dynamic> json) =>
      _$UserModelFromJson(json);
}

// ==========================================
// 13. App Details / Settings Model
// ==========================================
@freezed
class AppDetailsModel with _$AppDetailsModel {
  const factory AppDetailsModel({
    @JsonKey(name: 'app_email') String? appEmail,
    @JsonKey(name: 'app_author') String? appAuthor,
    @JsonKey(name: 'app_contact') String? appContact,
    @JsonKey(name: 'app_website') String? appWebsite,
    @JsonKey(name: 'app_description') String? appDescription,
    @JsonKey(name: 'app_developed_by') String? appDevelopedBy,
    @JsonKey(name: 'envato_api_key') String? envatoApiKey,
    @JsonKey(name: 'isRTL') String? isRtl,
    @JsonKey(name: 'isMaintenance') String? isMaintenance,
    @JsonKey(name: 'ad_status') String? adStatus,
    @JsonKey(name: 'ad_network') String? adNetwork,
    // Add other ad keys here if needed
  }) = _AppDetailsModel;

  factory AppDetailsModel.fromJson(Map<String, dynamic> json) =>
      _$AppDetailsModelFromJson(json);
}