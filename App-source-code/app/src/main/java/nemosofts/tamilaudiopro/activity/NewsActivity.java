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

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.adapter.AdapterNews;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.executor.LoadNews;
import nemosofts.tamilaudiopro.interfaces.NewsListener;
import nemosofts.tamilaudiopro.item.ItemNews;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.recycler.EndlessRecyclerViewScrollListener;


public class NewsActivity extends NSoftsPlayerActivity {

    private static final String LOG_TAG = "NewsActivity";
    private RecyclerView rv;
    private AdapterNews adapterNews;
    private ArrayList<ItemNews> arrayList;
    private Boolean isOver = false;
    private Boolean isScroll = false;
    private int page = 1;
    private int nativeAdPos = 0;
    private GridLayoutManager grid;
    private ProgressBar pb;
    private FloatingActionButton fab;
    private String errorMsg;
    private FrameLayout frameLayout;
    private AdLoader adLoader;
    private final ArrayList<NativeAd> arrayListNativeAds = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_news, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        bottomNav.setVisibility(View.GONE);
        adViewPlayer.setVisibility(View.VISIBLE);

        toolbar.setVisibility(View.GONE);

        Toolbar toolbarPromote = this.findViewById(R.id.toolbar_news);
        setSupportActionBar(toolbarPromote);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbarPromote.setNavigationOnClickListener(view -> finish());

        helper = new Helper(NewsActivity.this, (position, type) -> {
            Intent intent = new Intent(NewsActivity.this, NewsWebActivity.class);
            intent.putExtra("URL",arrayList.get(position).getNewsUrl());
            startActivity(intent);
        });
        helper.showBannerAd(adViewPlayer,"");

        arrayList = new ArrayList<>();

        frameLayout = findViewById(R.id.fl_empty_news);
        fab = findViewById(R.id.fab_news);
        pb = findViewById(R.id.pb_news);
        rv = findViewById(R.id.rv_news);

        grid = new GridLayoutManager(NewsActivity.this, 1);
        grid.setSpanCount(1);
        grid.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapterNews.getItemViewType(position) >= 1000 || adapterNews.isHeader(position)) ? grid.getSpanCount() : 1;
            }
        });
        rv.setHasFixedSize(true);
        rv.setLayoutManager(grid);
        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (Boolean.FALSE.equals(isOver)) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        isScroll = true;
                        getData();
                    }, 0);
                } else {
                    adapterNews.hideHeader();
                }
            }
        });
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItem = grid.findFirstVisibleItemPosition();
                if (firstVisibleItem > 6) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }
        });
        fab.setOnClickListener(v -> rv.smoothScrollToPosition(0));

        if(Callback.getNativeAdShow()%2 != 0) {
            Callback.setNativeAdShow(Callback.getNativeAdShow() + 1);
        } else {
            Callback.setNativeAdShow(Callback.getNativeAdShow());
        }

        getData();
    }

    private void getData() {
        if (!NetworkUtils.isConnected(this)) {
            errorMsg = getString(R.string.error_internet_not_connected);
            setEmpty();
            return;
        }
        LoadNews loadNews = new LoadNews(new NewsListener() {
            @Override
            public void onStart() {
                if (arrayList.isEmpty()) {
                    pb.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onEnd(String success,
                              String verifyStatus, String message, ArrayList<ItemNews> arrayListNews) {
                if (isFinishing()){
                    return;
                }
                if (!success.equals("1")) {
                    errorMsg = getString(R.string.error_server);
                    setEmpty();
                    return;
                }
                loadNewsEnd(verifyStatus, message, arrayListNews);
            }
        }, helper.getAPIRequest(Method.METHOD_NEWS, page, "", "", "",
                "", "", "", "", "", "", "",
                "", "", null));
        loadNews.execute();
    }

    private void loadNewsEnd(String verifyStatus, String message, ArrayList<ItemNews> arrayListNews) {
        if (verifyStatus.equals("-1")) {
            DialogUtil.verifyDialog(NewsActivity.this, getString(R.string.error_unauthorized_access), message, () -> {
            });
            return;
        }

        if (arrayListNews.isEmpty()) {
            isOver = true;
            try {
                adapterNews.hideHeader();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error hide header" ,e);
            }
            errorMsg = getString(R.string.error_no_data_found);
            setEmpty();
            return;
        }

        for (int i = 0; i < arrayListNews.size(); i++) {
            arrayList.add(arrayListNews.get(i));
            if (Callback.getIsNativeAdPost() && Callback.getIsAdsStatus()) {
                int abc = arrayList.lastIndexOf(null);
                if (nativeAdPos != 0 && ((arrayList.size() - (abc + 1)) % nativeAdPos == 0)) {
                    arrayList.add(null);
                }
            }
        }
        page = page + 1;
        setAdapter();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter() {
        if(Boolean.FALSE.equals(isScroll)) {
            adapterNews = new AdapterNews(NewsActivity.this,  arrayList, (itemNews, position) -> helper.showInterAd(position, ""));
            rv.setAdapter(adapterNews);
            setEmpty();
            loadNativeAds();
        } else {
            adapterNews.notifyDataSetChanged();
        }
    }

    private void loadNativeAds() {
        if (Callback.getAdNetwork().equals(Callback.AD_TYPE_ADMOB)
                || Boolean.TRUE.equals(Callback.getAdNetwork().equals(Callback.AD_TYPE_META)
                && Boolean.TRUE.equals(Callback.getIsAdsStatus())
                && Callback.getIsNativeAdPost() && arrayList.size() >= 10)) {

            AdLoader.Builder builder = new AdLoader.Builder(NewsActivity.this, Callback.getAdmobNativeAdID());
            Bundle extras = new Bundle();
            AdRequest adRequest;
            if(Callback.getAdNetwork().equals(Callback.AD_TYPE_ADMOB)) {
                adRequest = new AdRequest.Builder()
                        .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                        .build();
            } else {
                adRequest = new AdRequest.Builder()
                        .addNetworkExtrasBundle(AdMobAdapter.class, new Bundle())
                        .addNetworkExtrasBundle(FacebookMediationAdapter.class, extras)
                        .build();
            }
            adLoader = builder.forNativeAd(nativeAd -> {
                try {
                    arrayListNativeAds.add(nativeAd);
                    if (!adLoader.isLoading() && adapterNews != null) {
                        adapterNews.addAds(arrayListNativeAds);
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error add ads" ,e);
                }
            }).build();
            adLoader.loadAds(adRequest, 5);
        }
    }

    public void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
            pb.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);
            TextView btnText = myView.findViewById(R.id.tv_empty);

            if (errorMsg.equals(getString(R.string.error_no_data_found))) {
                btnText.setText(getString(R.string.refresh));
            } else if (errorMsg.equals(getString(R.string.error_internet_not_connected))) {
                btnText.setText(getString(R.string.retry));
            } else if (errorMsg.equals(getString(R.string.error_server))) {
                btnText.setText(getString(R.string.retry));
            }

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errorMsg);

            myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> getData());
            myView.findViewById(R.id.btn_empty_downloads).setOnClickListener(v -> {
                Intent intent = new Intent(NewsActivity.this, DownloadActivity.class);
                startActivity(intent);
            });

            myView.findViewById(R.id.btn_empty_music_lib).setOnClickListener(v -> {
                Intent intent = new Intent(NewsActivity.this, OfflineMusicActivity.class);
                startActivity(intent);
            });

            frameLayout.addView(myView);
        }
    }
}