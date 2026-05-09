package nemosofts.tamilaudiopro.executor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.ArtistListener;
import nemosofts.tamilaudiopro.item.ItemArtist;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import okhttp3.RequestBody;

public class LoadArtist extends AsyncTaskExecutor<String, String, String> {

    private final RequestBody requestBody;
    private final ArtistListener artistListener;
    private final ArrayList<ItemArtist> arrayList = new ArrayList<>();
    private String verifyStatus = "0";
    private String message = "";

    public LoadArtist(ArtistListener artistListener, RequestBody requestBody) {
        this.artistListener = artistListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        artistListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
            JSONObject mainJson = new JSONObject(json);
            JSONArray jsonArray = mainJson.getJSONArray(Callback.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objJson = jsonArray.getJSONObject(i);

                if (!objJson.has(Callback.TAG_SUCCESS)) {
                    String id = objJson.getString("id");
                    String name = objJson.getString("artist_name");
                    String image = objJson.getString("artist_image").replace(" ", "%20");
                    if (image.isEmpty()) {
                        image = "null";
                    }

                    ItemArtist objItem = new ItemArtist(id, name, image);
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
        artistListener.onEnd(s, verifyStatus, message, arrayList);
    }
}