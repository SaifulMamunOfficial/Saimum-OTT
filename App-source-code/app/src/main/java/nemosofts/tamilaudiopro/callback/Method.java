package nemosofts.tamilaudiopro.callback;

public class Method {

    private Method() {
        throw new IllegalStateException("Utility class");
    }

    public static final String LOGIN_TYPE_NORMAL = "Normal";
    public static final String LOGIN_TYPE_GOOGLE = "Google";

    // Method
    public static final String METHOD_APP_DETAILS = "app_details";
    public static final String METHOD_LOGIN = "user_login";
    public static final String METHOD_REGISTER = "user_register";
    public static final String METHOD_PROFILE = "user_profile";
    public static final String METHOD_ACCOUNT_DELETE = "account_delete";
    public static final String METHOD_EDIT_PROFILE = "edit_profile";
    public static final String METHOD_USER_IMAGES_UPDATE = "user_images_update";
    public static final String METHOD_FORGOT_PASSWORD = "forgot_pass";
    public static final String METHOD_NOTIFICATION = "get_notification";
    public static final String METHOD_REMOVE_NOTIFICATION = "remove_notification";
    public static final String METHOD_REPORT = "post_report";
    public static final String METHOD_GET_RATINGS = "get_rating";
    public static final String METHOD_RATINGS = "post_rating";
    public static final String METHOD_RATINGS_POST = "get_rating_list";

    public static final String METHOD_HOME = "get_home";
    public static final String METHOD_SINGLE_SONG = "single_song";
    public static final String METHOD_DOWNLOAD_COUNT = "song_download";
    public static final String METHOD_SONG_BY_TRENDING = "trending_songs";
    public static final String METHOD_SONG_ALL = "all_songs";
    public static final String METHOD_FAV = "favourite_post";
    public static final String METHOD_CAT = "cat_list";
    public static final String METHOD_ARTIST = "artist_list";
    public static final String METHOD_ALBUMS = "album_list";
    public static final String METHOD_SONG_BY_RECENT = "get_recent_songs";
    public static final String METHOD_SERVER_PLAYLIST = "playlist";
    public static final String METHOD_HOME_DETAILS = "home_collections";
    public static final String METHOD_NEWS = "news_list";
    public static final String METHOD_SUGGESTION = "post_suggest";
    public static final String METHOD_SEARCH = "get_search";
    public static final String METHOD_SEARCH_AUDIO  = "get_search_audio";
    public static final String METHOD_ALBUMS_CAT_ID = "album_cat_id";
    public static final String METHOD_ALBUMS_ART_ID = "album_artist_id";
    //  AudioByIDActivity
    public static final String METHOD_SONG_BY_CAT = "cat_songs";
    public static final String METHOD_SONG_BY_ALBUMS = "album_songs";
    public static final String METHOD_SONG_BY_ARTIST = "artist_name_songs";
    public static final String METHOD_SONG_BY_PLAYLIST = "playlist_songs";
    public static final String METHOD_SONG_BY_BANNER = "banner_songs";
    public static final String METHOD_POST_BY_FAV = "get_favourite";

    //
    public static final String METHOD_PLAN = "subscription_list";
}
