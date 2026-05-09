package nemosofts.tamilaudiopro.executor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.NewsListener;
import nemosofts.tamilaudiopro.item.ItemNews;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import okhttp3.RequestBody;

public class LoadNews extends AsyncTaskExecutor<String, String, String> {

    private final NewsListener newsListener;
    private final ArrayList<ItemNews> arrayList;
    private final RequestBody requestBody;
    private String verifyStatus = "0";
    private String message = "";

    public LoadNews(NewsListener newsListener, RequestBody requestBody) {
        this.newsListener = newsListener;
        arrayList = new ArrayList<>();
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        newsListener.onStart();
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
                    String title = objJson.getString("news_title");
                    String url = objJson.getString("news_url");

                    ItemNews objItem = new ItemNews(id, title, url);
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
        newsListener.onEnd(s, verifyStatus, message, arrayList);
    }
}