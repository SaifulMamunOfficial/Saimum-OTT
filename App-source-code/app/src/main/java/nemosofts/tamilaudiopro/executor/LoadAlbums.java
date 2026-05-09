package nemosofts.tamilaudiopro.executor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.AlbumsListener;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import okhttp3.RequestBody;

public class LoadAlbums extends AsyncTaskExecutor<String, String, String> {

    private final RequestBody requestBody;
    private final AlbumsListener albumsListener;
    private final ArrayList<ItemAlbums> arrayList = new ArrayList<>();
    private String verifyStatus = "0";
    private String message = "";

    public LoadAlbums(AlbumsListener albumsListener, RequestBody requestBody) {
        this.albumsListener = albumsListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        albumsListener.onStart();
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
                    String id = objJson.getString("aid");
                    String name = objJson.getString("album_name");
                    String image = objJson.getString("album_image").replace(" ", "%20");
                    if (image.isEmpty()) {
                        image = "null";
                    }

                    ItemAlbums objItem = new ItemAlbums(id, name, image, "");
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
        albumsListener.onEnd(s, verifyStatus, message, arrayList);
    }
}