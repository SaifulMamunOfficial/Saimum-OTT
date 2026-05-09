package nemosofts.tamilaudiopro.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.util.ArrayList;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.adapter.AdapterNotify;
import nemosofts.tamilaudiopro.executor.LoadNotify;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.interfaces.NotifyListener;
import nemosofts.tamilaudiopro.item.ItemNotify;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;
import nemosofts.tamilaudiopro.utils.recycler.EndlessRecyclerViewScrollListener;

public class NotificationActivity extends AppCompatActivity {

    private Helper helper;
    private SPHelper sharedPref;
    private RecyclerView rv;
    private AdapterNotify adapter;
    private ArrayList<ItemNotify> arrayList;
    private ProgressBar pb;
    private String errorMsg;
    private FrameLayout frameLayout;
    private int page = 1;
    private Boolean isOver = false;
    private Boolean isScroll = false;

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
        sharedPref = new SPHelper(this);

        arrayList = new ArrayList<>();
        errorMsg = getString(R.string.no_notification);
        frameLayout = findViewById(R.id.fl_empty);
        rv = findViewById(R.id.rv);
        pb = findViewById(R.id.pb);

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(llm);
        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(llm) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (Boolean.FALSE.equals(isOver)) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        isScroll = true;
                        loadData();
                    }, 0);
                } else {
                    adapter.hideHeader();
                }
            }
        });

        loadData();

        LinearLayout adView = findViewById(R.id.ll_adView);
        helper.showBannerAd(adView,"");
    }

    private void loadData() {
        if (NetworkUtils.isConnected(this)) {
            LoadNotify loadNotification = new LoadNotify(new NotifyListener() {
                @Override
                public void onStart() {
                    if (arrayList.isEmpty()) {
                        pb.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onEnd(String success, ArrayList<ItemNotify> notificationArrayList) {
                    if (!isFinishing()){
                        handleLoad(success, notificationArrayList);
                    }
                }
            }, helper.getAPIRequest(Method.METHOD_NOTIFICATION, page, "", "", "",
                    "", sharedPref.getUserId(), "", "", "", "",
                    "", "", "", null));
            loadNotification.execute();
        } else {
            errorMsg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    private void handleLoad(@NonNull String success, ArrayList<ItemNotify> notificationArrayList) {
        if (success.equals("1")) {
            if (notificationArrayList.isEmpty()) {
                isOver = true;
                try {
                    adapter.hideHeader();
                } catch (Exception e) {
                    Log.e("NotificationActivity", "Failed to loader hideHeader", e);
                }
                errorMsg = getString(R.string.no_notification);
                setEmpty();
            } else {
                page = page + 1;
                arrayList.addAll(notificationArrayList);
                setAdapter();
            }
        } else {
            errorMsg = getString(R.string.error_server_not_connected);
            setEmpty();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter() {
        if (Boolean.FALSE.equals(isScroll)) {
            adapter = new AdapterNotify(NotificationActivity.this, arrayList);
            rv.setAdapter(adapter);
            setEmpty();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            pb.setVisibility(View.INVISIBLE);
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.row_empty, null);

            TextView tvEmptyMsg = myView.findViewById(R.id.tv_empty_msg);
            tvEmptyMsg.setText(errorMsg);

            myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> loadData());

            frameLayout.addView(myView);
        }
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_notification;
    }
}