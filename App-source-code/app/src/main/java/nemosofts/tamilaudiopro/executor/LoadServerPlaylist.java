package nemosofts.tamilaudiopro.executor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.ServerPlaylistListener;
import nemosofts.tamilaudiopro.item.ItemServerPlayList;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import okhttp3.RequestBody;

public class LoadServerPlaylist extends AsyncTaskExecutor<String, String, String> {

    RequestBody requestBody;
    ServerPlaylistListener serverPlaylistListener;
    ArrayList<ItemServerPlayList> arrayList = new ArrayList<>();
    String verifyStatus = "0";
    String message = "";

    public LoadServerPlaylist(ServerPlaylistListener serverPlaylistListener, RequestBody requestBody) {
        this.serverPlaylistListener = serverPlaylistListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        serverPlaylistListener.onStart();
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
                    String id = objJson.getString("pid");
                    String name = objJson.getString("playlist_name");
                    String image = objJson.getString("playlist_image").replace(" ", "%20");
                    if (image.isEmpty()) {
                        image = "null";
                    }
                    ItemServerPlayList objItem = new ItemServerPlayList(id, name, image);
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
        serverPlaylistListener.onEnd(s, verifyStatus, message, arrayList);
    }
}