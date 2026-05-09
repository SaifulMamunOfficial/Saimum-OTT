package nemosofts.tamilaudiopro.executor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.SearchListener;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.item.ItemArtist;
import nemosofts.tamilaudiopro.item.ItemCat;
import nemosofts.tamilaudiopro.item.ItemPost;
import nemosofts.tamilaudiopro.item.ItemServerPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import okhttp3.RequestBody;

public class LoadSearch extends AsyncTaskExecutor<String, String, String> {

    SearchListener searchListener;
    RequestBody requestBody;
    ArrayList<ItemPost> arrayListPost = new ArrayList<>();

    private static final String TAG_ID = "id";
    private static final String TAG_AUDIO_TITLE = "audio_title";
    private static final String TAG_AUDIO_URL = "audio_url";
    private static final String TAG_AUDIO_URL_HIGH = "audio_url_high";
    private static final String TAG_AUDIO_URL_LOW = "audio_url_low";
    private static final String TAG_AUDIO_POSTER = "image";
    private static final String TAG_ARTIST = "audio_artist";
    private static final String TAG_DESCRIPTION = "audio_description";
    private static final String TAG_RATE_AVG = "rate_avg";
    private static final String TAG_AUDIO_VIEWS = "total_views";
    private static final String TAG_AUDIO_DOWNLOAD = "total_download";
    private static final String TAG_FAV = "is_favourite";

    private static final String TAG_POST_ID = "post_id";
    private static final String TAG_POST_TITLE = "post_title";
    private static final String TAG_POST_IMG = "post_image";

    public LoadSearch(SearchListener searchListener, RequestBody requestBody) {
        this.searchListener = searchListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        searchListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
            JSONObject mainJson = new JSONObject(json);
            JSONObject jsonObjectRoot = mainJson.getJSONObject(Callback.TAG_ROOT);

            ItemPost itemPost;
            String postTitle;
            String postType;
            String postId;

            if (jsonObjectRoot.has("songs_list")) {
                postId = "1";
                postTitle = "Songs";
                postType = "songs";
                itemPost = new ItemPost(postId, postTitle, postType, false);

                ArrayList<ItemSong> arrayListSongs = new ArrayList<>();
                JSONArray jsonArraySong = jsonObjectRoot.getJSONArray("songs_list");
                for (int i = 0; i < jsonArraySong.length(); i++) {
                    JSONObject objJson = jsonArraySong.getJSONObject(i);

                    String id = objJson.getString(TAG_ID);
                    String name = objJson.getString(TAG_AUDIO_TITLE);
                    String url = objJson.getString(TAG_AUDIO_URL);
                    String audioUrlHigh = objJson.getString(TAG_AUDIO_URL_HIGH);
                    String audioUrlLow = objJson.getString(TAG_AUDIO_URL_LOW);
                    String thumb = objJson.getString(TAG_AUDIO_POSTER).replace(" ", "%20");
                    String artist = objJson.getString(TAG_ARTIST);
                    String desc = objJson.getString(TAG_DESCRIPTION);
                    String avgRate = objJson.getString(TAG_RATE_AVG);
                    String views = objJson.getString(TAG_AUDIO_VIEWS);
                    String downloads = objJson.getString(TAG_AUDIO_DOWNLOAD);
                    boolean isFav = objJson.getBoolean(TAG_FAV);

                    ItemSong objItem = new ItemSong(id, artist, url, audioUrlHigh, audioUrlLow,
                            thumb, name, desc, desc, avgRate, views, downloads, isFav
                    );
                    arrayListSongs.add(objItem);
                }
                itemPost.setArrayListSongs(arrayListSongs);
                arrayListPost.add(itemPost);
            }

            if (jsonObjectRoot.has("artist_list")) {
                postId = "2";
                postTitle = "Artist";
                postType = "artists";
                itemPost = new ItemPost(postId, postTitle, postType, false);

                ArrayList<ItemArtist> arrayListArtist = new ArrayList<>();
                JSONArray jsonArrayArtist = jsonObjectRoot.getJSONArray("artist_list");
                for (int i = 0; i < jsonArrayArtist.length(); i++) {
                    JSONObject objJson = jsonArrayArtist.getJSONObject(i);

                    String id = objJson.getString(TAG_POST_ID);
                    String name = objJson.getString(TAG_POST_TITLE);
                    String image = objJson.getString(TAG_POST_IMG).replace(" ", "%20");
                    if (image.isEmpty()) {
                        image = "null";
                    }

                    ItemArtist objItem = new ItemArtist(id, name, image);
                    arrayListArtist.add(objItem);
                }
                itemPost.setArrayListArtist(arrayListArtist);
                arrayListPost.add(itemPost);
            }

            if (jsonObjectRoot.has("album_list")) {
                ArrayList<ItemAlbums> arrayListAlbums = new ArrayList<>();

                JSONArray jsonArrayAlbums = jsonObjectRoot.getJSONArray("album_list");

                postId = "3";
                postTitle = "Albums";
                postType = "albums";
                itemPost = new ItemPost(postId,  postTitle, postType, false);

                for (int i = 0; i < jsonArrayAlbums.length(); i++) {
                    JSONObject objJson = jsonArrayAlbums.getJSONObject(i);

                    String id = objJson.getString(TAG_POST_ID);
                    String name = objJson.getString(TAG_POST_TITLE);
                    String image = objJson.getString(TAG_POST_IMG).replace(" ", "%20");
                    if (image.isEmpty()) {
                        image = "null";
                    }

                    ItemAlbums objItem = new ItemAlbums(id, name, image,"");
                    arrayListAlbums.add(objItem);
                }
                itemPost.setArrayListAlbums(arrayListAlbums);
                arrayListPost.add(itemPost);
            }

            if (jsonObjectRoot.has("category_list")) {
                ArrayList<ItemCat> arrayListCat = new ArrayList<>();

                JSONArray jsonArrayCategory = jsonObjectRoot.getJSONArray("category_list");

                postId = "4";
                postTitle = "Categories";
                postType = "categories";
                itemPost = new ItemPost(postId, postTitle, postType, false);

                for (int i = 0; i < jsonArrayCategory.length(); i++) {
                    JSONObject objJson = jsonArrayCategory.getJSONObject(i);

                    String id = objJson.getString(TAG_POST_ID);
                    String name = objJson.getString(TAG_POST_TITLE);
                    String image = objJson.getString(TAG_POST_IMG).replace(" ", "%20");
                    if (image.isEmpty()) {
                        image = "null";
                    }

                    ItemCat objItem = new ItemCat(id, name, image);
                    arrayListCat.add(objItem);
                }
                itemPost.setArrayListCategories(arrayListCat);
                arrayListPost.add(itemPost);
            }

            if (jsonObjectRoot.has("playlist_list")) {
                ArrayList<ItemServerPlayList> arrayListPlaylist = new ArrayList<>();

                JSONArray jsonArrayPlaylist = jsonObjectRoot.getJSONArray("playlist_list");

                postId = "5";
                postTitle = "Playlist";
                postType = "playlists";
                itemPost = new ItemPost(postId, postTitle, postType, false);

                for (int i = 0; i < jsonArrayPlaylist.length(); i++) {
                    JSONObject objJson = jsonArrayPlaylist.getJSONObject(i);

                    String id = objJson.getString(TAG_POST_ID);
                    String name = objJson.getString(TAG_POST_TITLE);
                    String image = objJson.getString(TAG_POST_IMG).replace(" ", "%20");
                    if (image.isEmpty()) {
                        image = "null";
                    }

                    ItemServerPlayList objItem = new ItemServerPlayList(id, name, image);
                    arrayListPlaylist.add(objItem);
                }
                itemPost.setArrayListPlaylist(arrayListPlaylist);
                arrayListPost.add(itemPost);
            }
            return "1";
        } catch (Exception e) {
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        searchListener.onEnd(s, arrayListPost);
    }
}