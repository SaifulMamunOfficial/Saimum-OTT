package nemosofts.tamilaudiopro.executor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.AudioListener;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import okhttp3.RequestBody;

public class LoadAudio extends AsyncTaskExecutor<String, String, String> {

    private final AudioListener listener;
    private final RequestBody requestBody;
    private final ArrayList<ItemSong> arrayList = new ArrayList<>();
    private String verifyStatus = "0";
    private String message = "";

    public LoadAudio(AudioListener listener, RequestBody requestBody) {
        this.listener = listener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);

            JSONObject mainJson = new JSONObject(json);
            JSONArray jsonArray = mainJson.getJSONArray(Callback.TAG_ROOT);

            if (jsonArray.length() > 0 && jsonArray.getJSONObject(0).has("songs_list")) {
                jsonArray = jsonArray.getJSONObject(0).getJSONArray("songs_list");
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objJson = jsonArray.getJSONObject(i);

                if (!objJson.has(Callback.TAG_SUCCESS)) {

                    String id = objJson.getString("id");
                    String name = objJson.getString("audio_title");
                    String url = objJson.getString("audio_url");
                    String audioUrlHigh = objJson.getString("audio_url_high");
                    String audioUrlLow = objJson.getString("audio_url_low");
                    String thumb = objJson.getString("image").replace(" ", "%20");
                    String artist = objJson.getString("audio_artist");
                    String desc = objJson.getString("audio_description");
                    String avgRate = objJson.getString("rate_avg");
                    String views = objJson.getString("total_views");
                    String downloads = objJson.getString("total_download");
                    boolean isFav = objJson.getBoolean("is_favourite");

                    ItemSong objItem = new ItemSong(id, artist, url, audioUrlHigh, audioUrlLow,
                            thumb, name, desc, desc, avgRate, views, downloads, isFav
                    );
                    arrayList.add(objItem);

                } else {
                    verifyStatus = objJson.getString(Callback.TAG_SUCCESS);
                    message = objJson.getString(Callback.TAG_MSG);
                }
            }
            return "1";
        } catch (Exception e) {
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onEnd(s, verifyStatus, message, arrayList);
    }
}