package nemosofts.tamilaudiopro.executor;

import org.json.JSONArray;
import org.json.JSONObject;

import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.SuccessListener;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import okhttp3.RequestBody;

public class LoadFav extends AsyncTaskExecutor<String, String, String> {

    private final RequestBody requestBody;
    private final SuccessListener successListener;
    private String success = "0";
    private String message = "";

    public LoadFav(SuccessListener successListener, RequestBody requestBody) {
        this.successListener = successListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        successListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
            JSONObject mainJson = new JSONObject(json);
            JSONArray jsonArray = mainJson.getJSONArray(Callback.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);

                success = c.getString(Callback.TAG_SUCCESS);
                message = c.getString(Callback.TAG_MSG);
            }
            return "1";
        } catch (Exception e) {
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        successListener.onEnd(s, success, message);
    }
}