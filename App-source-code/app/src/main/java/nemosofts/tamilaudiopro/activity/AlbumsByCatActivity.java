package nemosofts.tamilaudiopro.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.adapter.AdapterAlbums;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.executor.LoadAlbums;
import nemosofts.tamilaudiopro.interfaces.AlbumsListener;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.recycler.EndlessRecyclerViewScrollListener;
import nemosofts.tamilaudiopro.utils.recycler.RecyclerItemClickListener;
import okhttp3.RequestBody;

public class AlbumsByCatActivity extends NSoftsPlayerActivity {

    private static final String LOG_TAG = "AlbumsByCatActivity";
    ThemeEngine themeEngine;
    RecyclerView rv;
    AdapterAlbums adapterAlbums;
    ArrayList<ItemAlbums> arrayList;
    ProgressBar progressBar;
    FrameLayout frameLayout;
    String errorMsg;
    int page = 1;
    int nativeAdPos = 0;
    Boolean isOver = false;
    Boolean isScroll = false;
    Boolean isLoading = false;
    AdLoader adLoader;
    final ArrayList<NativeAd> arrayListNativeAds = new ArrayList<>();
    String id = "";
    String name = "";
    String albumsType = "";
    SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.fragment_albums_by, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        bottomNav.setVisibility(View.GONE);
        adViewPlayer.setVisibility(View.VISIBLE);

        albumsType = getIntent().getStringExtra("type");
        id = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("name");

        themeEngine = new ThemeEngine(this);

        helper = new Helper(this, this::openAudioByIDActivity);
        helper.showBannerAd(adViewPlayer, Callback.PAGE_CAT_DETAILS);

        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (Boolean.TRUE.equals(themeEngine.getIsThemeMode())){
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backspace_white);
            } else {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backspace_black);
            }
        }

        arrayList = new ArrayList<>();

        progressBar = findViewById(R.id.pb);
        frameLayout = findViewById(R.id.fl_empty);
        rv = findViewById(R.id.rv);
        GridLayoutManager manager = new GridLayoutManager(AlbumsByCatActivity.this, 2);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapterAlbums.getItemViewType(position) == -2 || adapterAlbums.isHeader(position)) ? manager.getSpanCount() : 1;
            }
        });
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.addOnItemTouchListener(new RecyclerItemClickListener(AlbumsByCatActivity.this, (view, position) -> helper.showInterAd(position, "")));
        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(manager) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (Boolean.FALSE.equals(isOver) && (Boolean.FALSE.equals(isLoading))) {
                    isLoading = true;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        isScroll = true;
                        loadAlbums();
                    }, 0);
                }
            }
        });

        if(Callback.getNativeAdShow()%2 != 0) {
            nativeAdPos = Callback.getNativeAdShow() + 1;
        } else {
            nativeAdPos = Callback.getNativeAdShow();
        }

        loadAlbums();

        findViewById(R.id.ll_view_all_songs).setOnClickListener(view -> helper.showInterAd(0,"all"));

        setupBackPressHandler();
    }

    private void openAudioByIDActivity(int position, String type) {
        Intent intent = new Intent(AlbumsByCatActivity.this, AudioByIDActivity.class);
        if (type.isEmpty()){
            intent.putExtra("type", getString(R.string.albums));
            intent.putExtra("id", arrayList.get(position).getId());
            intent.putExtra("name", arrayList.get(position).getName());
        } else if (type.equals("all")){
            if (albumsType.equals(getString(R.string.categories))){
                intent.putExtra("type", getString(R.string.categories));
            } else {
                intent.putExtra("type", getString(R.string.artist));
            }
            intent.putExtra("id", id);
            intent.putExtra("name", name);
        }
        startActivity(intent);
    }

    private void loadAlbums() {
        if (NetworkUtils.isConnected(this)) {
            RequestBody requestBody;
            if (albumsType.equals(getString(R.string.categories))) {
                requestBody = helper.getAPIRequest(Method.METHOD_ALBUMS_CAT_ID, page, "",id ,
                        "", "", "", "", "", "",
                        "", "", "", "", null);
            } else if (albumsType.equals(getString(R.string.artist))) {
                requestBody = helper.getAPIRequest(Method.METHOD_ALBUMS_ART_ID, page, id, "",
                        "", "", "", "","", "",
                        "", "", "", "", null);
            } else {
                requestBody = helper.getAPIRequest(Method.METHOD_ALBUMS, page, "","",
                        "", "", "", "", "", "",
                        "", "", "", "", null);
            }
            LoadAlbums loadAlbums = new LoadAlbums(new AlbumsListener() {
                @Override
                public void onStart() {
                    if (arrayList.isEmpty()) {
                        frameLayout.setVisibility(View.GONE);
                        rv.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onEnd(String success,
                                  String verifyStatus, String message, ArrayList<ItemAlbums> arrayListAlbums) {
                    if (isFinishing()){
                        return;
                    }
                    if (success.equals("1")) {
                        loadAlbumsEnd(verifyStatus, message, arrayListAlbums);
                    } else {
                        isOver = true;
                        try {
                            adapterAlbums.hideHeader();
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error hideHeader",e);
                        }
                        errorMsg = getString(R.string.error_server);
                        setEmpty();
                    }
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                }
            },requestBody);
            loadAlbums.execute();
        } else {
            errorMsg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    private void loadAlbumsEnd(String verifyStatus, String message, ArrayList<ItemAlbums> arrayListAlbums) {
        if (verifyStatus.equals("-1")) {
            DialogUtil.verifyDialog(AlbumsByCatActivity.this, getString(R.string.error_unauthorized_access), message, () -> {
            });
            return;
        }
        if (arrayListAlbums.isEmpty()) {
            isOver = true;
            errorMsg = getString(R.string.error_no_albums_found);
            try {
                adapterAlbums.hideHeader();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error hideHeader",e);
            }
            setEmpty();
        } else {
            for (int i = 0; i < arrayListAlbums.size(); i++) {
                arrayList.add(arrayListAlbums.get(i));
                if (Callback.getIsNativeAdCat() && Callback.getIsAdsStatus()) {
                    int abc = arrayList.lastIndexOf(null);
                    if (nativeAdPos != 0 && ((arrayList.size() - (abc + 1)) % nativeAdPos == 0)) {
                        arrayList.add(null);
                    }
                }
            }
            page = page + 1;
            setAdapter();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        if (searchView != null){
            searchView.setOnQueryTextListener(queryTextListener);
        }
        return super.onCreateOptionsMenu(menu);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public boolean onQueryTextChange(String s) {
            if (adapterAlbums != null && (!searchView.isIconified())) {
                adapterAlbums.getFilter().filter(s);
                adapterAlbums.notifyDataSetChanged();
            }
            return true;
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    private void setAdapter() {
        if (Boolean.FALSE.equals(isScroll)) {
            adapterAlbums = new AdapterAlbums(AlbumsByCatActivity.this, arrayList, true);
            rv.setAdapter(adapterAlbums);
            setEmpty();
            loadNativeAds();
        } else {
            adapterAlbums.notifyDataSetChanged();
        }
    }

    private void loadNativeAds() {
        if (helper.canLoadNativeAds(this,Callback.PAGE_NATIVE_CAT)
                && Callback.getAdNetwork().equals(Callback.AD_TYPE_ADMOB)
                || Callback.getAdNetwork().equals(Callback.AD_TYPE_META)
                && arrayList.size() >= 10) {

            AdLoader.Builder builder = new AdLoader.Builder(AlbumsByCatActivity.this, Callback.getAdmobNativeAdID());
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
                    if (!adLoader.isLoading() && adapterAlbums != null) {
                        adapterAlbums.addAds(arrayListNativeAds);
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error addAds",e);
                }
            }).build();
            adLoader.loadAds(adRequest, 5);
        }
    }

    public void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);
            TextView btnText = myView.findViewById(R.id.tv_empty);

            if (errorMsg.equals(getString(R.string.error_no_albums_found))) {
                btnText.setText(getString(R.string.refresh));
            } else if (errorMsg.equals(getString(R.string.error_internet_not_connected))) {
                btnText.setText(getString(R.string.retry));
            } else if (errorMsg.equals(getString(R.string.error_server))) {
                btnText.setText(getString(R.string.retry));
            }

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errorMsg);

            myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> loadAlbums());
            myView.findViewById(R.id.btn_empty_downloads).setOnClickListener(v -> {
                Intent intent = new Intent(AlbumsByCatActivity.this, DownloadActivity.class);
                startActivity(intent);
            });

            myView.findViewById(R.id.btn_empty_music_lib).setOnClickListener(v -> {
                Intent intent = new Intent(AlbumsByCatActivity.this, OfflineMusicActivity.class);
                startActivity(intent);
            });

            frameLayout.addView(myView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBackPressHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    // Let the system handle the back
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}