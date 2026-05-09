package nemosofts.tamilaudiopro.executor;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.HomeListener;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.item.ItemArtist;
import nemosofts.tamilaudiopro.item.ItemCat;
import nemosofts.tamilaudiopro.item.ItemHomeSlider;
import nemosofts.tamilaudiopro.item.ItemPost;
import nemosofts.tamilaudiopro.item.ItemServerPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import okhttp3.RequestBody;

public class LoadHome extends AsyncTaskExecutor<String, String, String> {

    RequestBody requestBody;
    HomeListener homeListener;
    ArrayList<ItemPost> arrayListPost = new ArrayList<>();
    String message = "";
    String successAPI = "1";

    Context context;

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

    private static final String TAG_SLIDER = "slider";


    public LoadHome(Context context, HomeListener homeListener, RequestBody requestBody) {
        this.context = context;
        this.homeListener = homeListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        homeListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
            JSONObject mainJson = new JSONObject(json);

            try {

                JSONObject jsonObject = mainJson.getJSONObject(Callback.TAG_ROOT);

                ItemPost itemPost;
                String postTitle;
                String postType;
                String postId;

                if (jsonObject.has(TAG_SLIDER)) {

                    JSONArray jsonArrayBanner = jsonObject.getJSONArray(TAG_SLIDER);

                    postTitle = "Home Banners";
                    postType = TAG_SLIDER;
                    itemPost = new ItemPost("", postTitle, postType, false);

                    ArrayList<ItemHomeSlider> arrayListBanner = new ArrayList<>();
                    for (int i = 0; i < jsonArrayBanner.length(); i++) {
                        JSONObject objJsonBanner = jsonArrayBanner.getJSONObject(i);

                        String bannerID = objJsonBanner.getString("bid");
                        String bannerTitle = objJsonBanner.getString("banner_title");
                        String bannerDesc = objJsonBanner.getString("banner_info");
                        String bannerImage = objJsonBanner.getString("banner_image");

                        arrayListBanner.add(new ItemHomeSlider(bannerID, bannerTitle, bannerDesc, bannerImage));
                    }
                    itemPost.setArrayListBanner(arrayListBanner);
                    arrayListPost.add(itemPost);
                }

                if (jsonObject.has("recently_songs")) {

                    JSONArray jsonArrayRecent = jsonObject.getJSONArray("recently_songs");

                    if (jsonArrayRecent.length() > 0) {

                        postTitle = context.getString(R.string.recently_played);
                        postType = "recent";
                        itemPost = new ItemPost("", postTitle, postType, false);

                        ArrayList<ItemSong> arrayListRecent = new ArrayList<>();
                        for (int i = 0; i < jsonArrayRecent.length(); i++) {
                            JSONObject objJson = jsonArrayRecent.getJSONObject(i);

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
                            arrayListRecent.add(objItem);
                        }
                        itemPost.setArrayListSongs(arrayListRecent);
                        arrayListPost.add(itemPost);
                    }
                }

                if (jsonObject.has("trending_songs")) {

                    JSONArray jsonArrayTrending = jsonObject.getJSONArray("trending_songs");

                    if (jsonArrayTrending.length() > 0) {

                        postTitle = context.getString(R.string.trending_songs);
                        postType = "trending";
                        itemPost = new ItemPost("", postTitle, postType, false);

                        ArrayList<ItemSong> arrayListTrending = new ArrayList<>();
                        for (int i = 0; i < jsonArrayTrending.length(); i++) {
                            JSONObject objJson = jsonArrayTrending.getJSONObject(i);

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
                            arrayListTrending.add(objItem);
                        }
                        itemPost.setArrayListSongs(arrayListTrending);
                        arrayListPost.add(itemPost);
                    }
                }

                if (jsonObject.has("home_artist")) {

                    JSONArray jsonArrayArtist = jsonObject.getJSONArray("home_artist");

                    if (jsonArrayArtist.length() > 0) {

                        postTitle = context.getString(R.string.artist);
                        postType = "artist";
                        itemPost = new ItemPost("", postTitle, postType, false);

                        ArrayList<ItemArtist> arrayListArtist = new ArrayList<>();
                        for (int i = 0; i < jsonArrayArtist.length(); i++) {
                            JSONObject objJson = jsonArrayArtist.getJSONObject(i);

                            String id = objJson.getString("id");
                            String artistName = objJson.getString("artist_name");
                            String imageBig = objJson.getString("artist_image").replace(" ", "%20");
                            if (imageBig.isEmpty()) {
                                imageBig = "null";
                            }

                            ItemArtist objItem = new ItemArtist(id,artistName,imageBig);
                            arrayListArtist.add(objItem);
                        }
                        itemPost.setArrayListArtist(arrayListArtist);
                        arrayListPost.add(itemPost);
                    }
                }

                if (jsonObject.has("home_album")) {

                    JSONArray jsonArrayAlbums = jsonObject.getJSONArray("home_album");

                    if (jsonArrayAlbums.length() > 0) {

                        postTitle = context.getString(R.string.albums);
                        postType = "album";
                        itemPost = new ItemPost("", postTitle, postType, false);

                        ArrayList<ItemAlbums> arrayListAlbums = new ArrayList<>();
                        for (int i = 0; i < jsonArrayAlbums.length(); i++) {
                            JSONObject objJson = jsonArrayAlbums.getJSONObject(i);

                            String id = objJson.getString("aid");
                            String albumName = objJson.getString("album_name");
                            String imageBig = objJson.getString("album_image").replace(" ", "%20");
                            if (imageBig.isEmpty()) {
                                imageBig = "null";
                            }

                            ItemAlbums objItem = new ItemAlbums(id,albumName,imageBig,"");
                            arrayListAlbums.add(objItem);
                        }
                        itemPost.setArrayListAlbums(arrayListAlbums);
                        arrayListPost.add(itemPost);
                    }
                }

                if (jsonObject.has("home_sections")) {

                    JSONArray jsonArraySection = jsonObject.getJSONArray("home_sections");

                    for (int j = 0; j < jsonArraySection.length(); j++) {

                        JSONObject jObjHome = jsonArraySection.getJSONObject(j);

                        postId = jObjHome.getString("home_id");
                        postTitle = jObjHome.getString("home_title");
                        postType = jObjHome.getString("home_type");
                        itemPost = new ItemPost(postId, postTitle, postType , true);

                        JSONArray jsonArrayHomeContent = jObjHome.getJSONArray("home_content");

                        switch (postType) {
                            case "category" -> {
                                ArrayList<ItemCat> arrayListCat = new ArrayList<>();
                                for (int i = 0; i < jsonArrayHomeContent.length(); i++) {
                                    JSONObject objJson = jsonArrayHomeContent.getJSONObject(i);

                                    String id = objJson.getString(TAG_POST_ID);
                                    String name = objJson.getString(TAG_POST_TITLE);
                                    String image = objJson.getString(TAG_POST_IMG).replace(" ", "%20");
                                    if (image.isEmpty()) {
                                        image = "null";
                                    }

                                    ItemCat itemCat = new ItemCat(id, name, image);

                                    arrayListCat.add(itemCat);
                                }
                                itemPost.setArrayListCategories(arrayListCat);
                            }
                            case "song" -> {
                                ArrayList<ItemSong> arrayListSongs = new ArrayList<>();
                                for (int i = 0; i < jsonArrayHomeContent.length(); i++) {
                                    JSONObject objJson = jsonArrayHomeContent.getJSONObject(i);

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
                            }
                            case "playlist" -> {
                                ArrayList<ItemServerPlayList> arrayListPlaylist = new ArrayList<>();
                                for (int i = 0; i < jsonArrayHomeContent.length(); i++) {
                                    JSONObject objJson = jsonArrayHomeContent.getJSONObject(i);

                                    String id = objJson.getString(TAG_POST_ID);
                                    String name = objJson.getString(TAG_POST_TITLE);
                                    String image = objJson.getString(TAG_POST_IMG).replace(" ", "%20");
                                    if (image.isEmpty()) {
                                        image = "null";
                                    }

                                    ItemServerPlayList itemServerPlayList = new ItemServerPlayList(id, name, image);

                                    arrayListPlaylist.add(itemServerPlayList);
                                }
                                itemPost.setArrayListPlaylist(arrayListPlaylist);
                            }
                            case "artist" -> {
                                ArrayList<ItemArtist> arrayListArtist = new ArrayList<>();
                                for (int i = 0; i < jsonArrayHomeContent.length(); i++) {
                                    JSONObject objJson = jsonArrayHomeContent.getJSONObject(i);

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
                            }
                            case "album" -> {
                                ArrayList<ItemAlbums> arrayListAlbums = new ArrayList<>();
                                for (int i = 0; i < jsonArrayHomeContent.length(); i++) {
                                    JSONObject objJson = jsonArrayHomeContent.getJSONObject(i);

                                    String id = objJson.getString(TAG_POST_ID);
                                    String name = objJson.getString(TAG_POST_TITLE);
                                    String image = objJson.getString(TAG_POST_IMG).replace(" ", "%20");
                                    if (image.isEmpty()) {
                                        image = "null";
                                    }

                                    ItemAlbums objItem = new ItemAlbums(id, name, image, "");
                                    arrayListAlbums.add(objItem);
                                }
                                itemPost.setArrayListAlbums(arrayListAlbums);
                            }
                            default -> {
                                // no data
                            }
                        }
                        arrayListPost.add(itemPost);
                    }
                }
            } catch (Exception e) {
                JSONArray jsonArray = mainJson.getJSONArray(Callback.TAG_ROOT);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                successAPI = jsonObject.getString(Callback.TAG_SUCCESS);
                message = jsonObject.getString(Callback.TAG_MSG);
            }
            return "1";
        } catch (Exception e) {
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        homeListener.onEnd(s, message, arrayListPost);
    }
}