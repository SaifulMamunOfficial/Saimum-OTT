package nemosofts.tamilaudiopro.executor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.RatingPostListener;
import nemosofts.tamilaudiopro.item.ItemRating;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import okhttp3.RequestBody;

public class LoadPostRating extends AsyncTaskExecutor<String, String, String> {

    private final RatingPostListener listener;
    private final ArrayList<ItemRating> arrayList;
    private final RequestBody requestBody;
    private String verifyStatus = "0";
    private String message = "";

    public LoadPostRating(RatingPostListener listener, RequestBody requestBody) {
        this.listener = listener;
        arrayList = new ArrayList<>();
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
        super.onPreExecute();
    }

    @Override
    protected  String doInBackground(String strings)  {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
            JSONObject jOb = new JSONObject(json);
            JSONArray jsonArray = jOb.getJSONArray(Callback.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objJson = jsonArray.getJSONObject(i);

                if (!objJson.has(Callback.TAG_SUCCESS)) {

                    String id = objJson.getString("id");
                    String rate = objJson.getString("rate");
                    String meg = objJson.getString("message");
                    String userName = objJson.getString("user_name");
                    String userProfile = objJson.getString("user_profile").replace(" ", "%20");

                    ItemRating objItem = new ItemRating(id, rate, meg, userName, userProfile);
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