package nemosofts.tamilaudiopro.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import nemosofts.tamilaudiopro.adapter.AdapterRating;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.ReviewDialog;
import nemosofts.tamilaudiopro.executor.LoadPostRating;
import nemosofts.tamilaudiopro.interfaces.RatingPostListener;
import nemosofts.tamilaudiopro.item.ItemRating;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.recycler.EndlessRecyclerViewScrollListener;

public class CommentActivity extends AppCompatActivity {

    private static final String LOG_TAG = "CommentActivity";
    private Helper helper;
    private RecyclerView rv;
    private AdapterRating adapter;
    private ArrayList<ItemRating> arrayList;
    private int currentItem = 0;
    private int page = 1;
    private Boolean isOver = false;
    private Boolean isScroll = false;
    private String errorMsg;
    private FrameLayout frameLayout;
    private ProgressBar pb;

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

        Intent intent = getIntent();
        currentItem = intent.getIntExtra("current_item",0);

        helper = new Helper(this);

        arrayList = new ArrayList<>();

        frameLayout = findViewById(R.id.fl_empty);
        rv = findViewById(R.id.rv_comment);
        pb = findViewById(R.id.pb);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        rv.setLayoutManager(manager);
        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(manager) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (Boolean.FALSE.equals(isOver)) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        isScroll = true;
                        loadComments();
                    }, 0);
                } else {
                    adapter.hideHeader();
                }
            }
        });
        findViewById(R.id.ll_btn_rating_send).setOnClickListener(view -> showRateDialog());
        loadComments();
    }

    private void showRateDialog() {
        if (!Callback.getArrayListPlay().isEmpty()) {
            ReviewDialog reviewDialog = new ReviewDialog(this, new ReviewDialog.RatingDialogListener() {
                @Override
                public void onShow() {
                    // this method is empty
                }

                @Override
                public void onGetRating(String rating, String message) {
                    // this method is empty
                }

                @Override
                public void onDismiss(String success, String rateSuccess, String message, int rating, String userRating, String userMessage) {
                    if (success.equals("1")) {
                        if (rateSuccess.equals("1")) {
                            try {
                                Callback.getArrayListPlay().get(currentItem).setAverageRating(String.valueOf(rating));
                                Callback.getArrayListPlay().get(currentItem).setUserRating(String.valueOf(userRating));
                                Callback.getArrayListPlay().get(currentItem).setUserMessage(String.valueOf(userMessage));
                                refreshData();
                            } catch (Exception e) {
                               Log.e(LOG_TAG, "Error reviewDialog",e);
                            }
                        }
                        Toast.makeText(CommentActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CommentActivity.this, getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            reviewDialog.showDialog(Callback.getArrayListPlay().get(currentItem).getId(),
                    Callback.getArrayListPlay().get(currentItem).getUserRating(),
                    Callback.getArrayListPlay().get(currentItem).getUserMessage()
            );
        } else {
            Toast.makeText(CommentActivity.this, getString(R.string.error_no_songs_selected), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshData() {
        arrayList.clear();
        isOver = false;
        isScroll = false;
        page = 1;
        if (adapter != null){
            adapter.notifyDataSetChanged();
        }
        loadComments();
    }

    private void loadComments() {
        if (!NetworkUtils.isConnected(this)) {
            errorMsg = getString(R.string.error_internet_not_connected);
            setEmpty();
            return;
        }
        LoadPostRating loadComments = new LoadPostRating(new RatingPostListener() {
            @Override
            public void onStart() {
                if (arrayList.isEmpty()) {
                    frameLayout.setVisibility(View.GONE);
                    rv.setVisibility(View.GONE);
                    pb.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onEnd(String success, String verifyStatus, String message, ArrayList<ItemRating> arrayListRating) {
                if (isFinishing()){
                    return;
                }
                loadCommentsEnd(success, arrayListRating);
            }
        },helper.getAPIRequest(Method.METHOD_RATINGS_POST, page,
                Callback.getArrayListPlay().get(currentItem).getId(), "", "",
                "", "", "", "", "","","",
                "","",null));
        loadComments.execute();
    }

    private void loadCommentsEnd(String success, ArrayList<ItemRating> arrayListRating) {
        if (!success.equals("1")) {
            errorMsg = getString(R.string.error_server_not_connected);
            setEmpty();
            return;
        }
        if (arrayListRating.isEmpty()) {
            isOver = true;
            try {
                adapter.hideHeader();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error in hideHeader",e);
            }
            errorMsg = getString(R.string.no_rating);
            setEmpty();
        } else {
            arrayList.addAll(arrayListRating);
            page = page + 1;
            setAdapter();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter() {
        if(Boolean.FALSE.equals(isScroll)) {
            adapter = new AdapterRating(arrayList);
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

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errorMsg);

            myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> loadComments());

            myView.findViewById(R.id.btn_empty_downloads).setVisibility(View.GONE);
            myView.findViewById(R.id.btn_empty_music_lib).setVisibility(View.GONE);

            frameLayout.addView(myView);
        }
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_comment;
    }
}