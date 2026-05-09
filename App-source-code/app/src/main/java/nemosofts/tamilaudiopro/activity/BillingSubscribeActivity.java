package nemosofts.tamilaudiopro.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.saimum.saimummusic.BuildConfig;
import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.adapter.PlanAdapter;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.item.ItemPlan;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;
import okhttp3.RequestBody;

public class BillingSubscribeActivity extends AppCompatActivity {

    private Helper helper;
    private SPHelper spHelper;
    private ProgressBar pb;
    private RecyclerView rv;
    private TextView proceed;
    private String errorMsg;
    private FrameLayout frameLayout;
    private ArrayList<ItemPlan> mListItem;
    private int selectedPlan = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bg_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());

        helper = new Helper(this);
        spHelper = new SPHelper(this);

        mListItem = new ArrayList<>();
        pb = findViewById(R.id.pb);
        frameLayout = findViewById(R.id.fl_empty);
        proceed = findViewById(R.id.tv_btn_proceed);

        rv = findViewById(R.id.rv_plan);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(BillingSubscribeActivity.this, LinearLayoutManager.VERTICAL, false));
        rv.setFocusable(false);
        rv.setNestedScrollingEnabled(false);

        findViewById(R.id.tv_terms).setOnClickListener(view -> openWebActivity());

        getPlan();
    }

    private void openWebActivity() {
        Intent intent1 = new Intent(BillingSubscribeActivity.this, WebActivity.class);
        intent1.putExtra("web_url", BuildConfig.BASE_URL+"terms.php");
        intent1.putExtra("page_title", getResources().getString(R.string.terms_and_conditions));
        startActivity(intent1);
    }

    private void getPlan() {
        if (!NetworkUtils.isConnected(this)) {
            errorMsg = getString(R.string.error_internet_not_connected);
            setEmpty();
            return;
        }

        LoadPlan loadPlan = new LoadPlan(new PlanListener() {
            @Override
            public void onStart() {
                pb.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
                frameLayout.setVisibility(View.GONE);
            }

            @Override
            public void onEnd(String success, boolean verifyStatus, String message,
                              ArrayList<ItemPlan> itemPlans) {
                if (isFinishing()){
                    return;
                }
                if (success.equals("1")) {
                    if (verifyStatus) {
                        rv.setVisibility(View.VISIBLE);
                        pb.setVisibility(View.GONE);
                        mListItem.addAll(itemPlans);
                        displayData();
                    } else {
                        DialogUtil.verifyDialog(BillingSubscribeActivity.this, getString(R.string.error_unauthorized_access), message, () -> {
                        });
                    }
                } else {
                    errorMsg = getString(R.string.error_server_not_connected);
                    setEmpty();
                }
            }
        },helper.getAPIRequest(Method.METHOD_PLAN, 0, "", "", "",
                "", "", "", "", "", "", "",
                "", "", null));
        loadPlan.execute();
    }

    @SuppressLint("SetTextI18n")
    private void displayData() {
        PlanAdapter adapter = new PlanAdapter(BillingSubscribeActivity.this, mListItem);
        rv.setAdapter(adapter);
        adapter.select(-1);

        adapter.setOnItemClickListener(position -> {
            selectedPlan = position;
            adapter.select(position);
            proceed.setText("Try for "+ mListItem.get(position).getPlanPrice()+" "
                    +mListItem.get(position).getPlanCurrencyCode());
        });

        proceed.setOnClickListener(view -> {
            if(spHelper.isLogged()) {
                if (!spHelper.getIsSubscribed()){
                    if (selectedPlan != -1){
                        Intent intent = getIntentBillingConnector();
                        startActivity(intent);
                    } else {
                        proceed.setText("no selected");
                    }
                } else {
                    Toast.makeText(BillingSubscribeActivity.this, "Item already subscribed", Toast.LENGTH_SHORT).show();
                }
            } else {
                helper.clickLogin();
            }
        });
    }

    @NonNull
    private Intent getIntentBillingConnector() {
        ItemPlan itemPlan = mListItem.get(selectedPlan);
        Intent intent = new Intent(BillingSubscribeActivity.this, BillingConnectorActivity.class);
        intent.putExtra("planId", itemPlan.getPlanId());
        intent.putExtra("planName", itemPlan.getPlanName());
        intent.putExtra("planPrice", itemPlan.getPlanPrice());
        intent.putExtra("planDuration", itemPlan.getPlanDuration());
        intent.putExtra("planCurrencyCode", itemPlan.getPlanCurrencyCode());
        intent.putExtra("subscription_id", itemPlan.getSubscriptionID());
        intent.putExtra("base_key", itemPlan.getBaseKey());
        return intent;
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_billing_subscribe;
    }

    private static class LoadPlan extends AsyncTaskExecutor<String, String, String> {

        private final RequestBody requestBody;
        private final PlanListener planListener;
        private final ArrayList<ItemPlan> arrayList = new ArrayList<>();
        private String message = "";
        private boolean verifyStatus = true;

        public LoadPlan(PlanListener planListener, RequestBody requestBody) {
            this.planListener = planListener;
            this.requestBody = requestBody;
        }

        @Override
        protected void onPreExecute() {
            planListener.onStart();
            super.onPreExecute();
        }

        @NonNull
        @Override
        protected String doInBackground(String strings) {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
            try {
                JSONObject jOb = new JSONObject(json);
                JSONArray jsonArray = jOb.getJSONArray(Callback.TAG_ROOT);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject objJson = jsonArray.getJSONObject(i);

                    if (!objJson.has(Callback.TAG_SUCCESS)) {

                        String id = objJson.getString("id");
                        String planName = objJson.getString("plan_name");
                        String planDuration = objJson.getString("plan_duration");
                        String planPrice = objJson.getString("plan_price");
                        String currencyCode = objJson.getString("currency_code");
                        String subscriptionID = objJson.getString("subscription_id");
                        String baseKey = objJson.getString("base_key");

                        ItemPlan item = new ItemPlan(id, planName, planDuration, planPrice,
                                currencyCode, subscriptionID, baseKey);
                        arrayList.add(item);
                    } else {
                        verifyStatus = objJson.getBoolean(Callback.TAG_SUCCESS);
                        message = objJson.getString(Callback.TAG_MSG);
                    }
                }
                return "1";
            } catch (Exception ee) {
                return "0";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            planListener.onEnd(s, verifyStatus, message, arrayList);
        }
    }

    private void setEmpty() {
        pb.setVisibility(View.GONE);
        rv.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);

        frameLayout.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.row_empty, null);

        TextView textView = myView.findViewById(R.id.tv_empty_msg);
        textView.setText(errorMsg);

        myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> getPlan());

        frameLayout.addView(myView);
    }

    private interface PlanListener {
        void onStart();
        void onEnd(String success, boolean verifyStatus, String message, ArrayList<ItemPlan> itemPlans);
    }
}