package nemosofts.tamilaudiopro.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.annotation.OptIn;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.adapter.AdapterAllSongList;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class AudioByDBPlaylistActivity extends NSoftsPlayerActivity {

    private static final String LOG_TAG = "AudioByDBPlaylistActivity";
    Toolbar toolbarPlaylist;
    ThemeEngine themeEngine;
    RecyclerView rv;
    ItemMyPlayList itemMyPlayList;
    AdapterAllSongList adapter;
    List<ItemSong> arrayList;
    ProgressBar progressBar;
    FrameLayout frameLayout;
    String addedFrom = "my_play";
    SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_audio_by_my_playlist, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        bottomNav.setVisibility(View.GONE);
        adViewPlayer.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.GONE);

        itemMyPlayList = (ItemMyPlayList) getIntent().getSerializableExtra("item");
        if (itemMyPlayList != null){
            addedFrom = addedFrom + itemMyPlayList.getName();
        }

        themeEngine = new ThemeEngine(this);
        helper = new Helper(this, (position, type) -> openPlayerService(position));
        helper.showBannerAd(adViewPlayer,"");

        toolbarPlaylist = findViewById(R.id.toolbar_playlist);
        setSupportActionBar(toolbarPlaylist);
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

        frameLayout = findViewById(R.id.fl_empty);
        progressBar = findViewById(R.id.pb_audio_by_playlist);
        progressBar.setVisibility(View.GONE);
        rv = findViewById(R.id.rv_audio_by_playlist);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());

        arrayList = dbHelper.loadDataPlaylist(itemMyPlayList.getId(), true);

        setAdapter();
        setupBackPressHandler();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openPlayerService(int position) {
        Callback.setIsOnline(true);
        if (!Callback.getAddedFrom().equals(addedFrom)) {
            Callback.getArrayListPlay().clear();
            Callback.setArrayListPlay(arrayList);
            Callback.setAddedFrom(addedFrom);
            Callback.setIsNewAdded(true);
        }
        Callback.setPlayPos(position);

        Intent intent = new Intent(AudioByDBPlaylistActivity.this, PlayerService.class);
        intent.setAction(PlayerService.ACTION_PLAY);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        assert searchView != null;
        searchView.setOnQueryTextListener(queryTextListener);
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
            if (adapter != null && (!searchView.isIconified())) {
                adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }
            return true;
        }
    };

    private void setAdapter() {
        adapter = new AdapterAllSongList(AudioByDBPlaylistActivity.this, arrayList, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                helper.showInterAd(position, "");
            }

            @Override
            public void onItemZero() {
                setEmpty();
            }
        }, "playlist");
        rv.setAdapter(adapter);
        setEmpty();
        loadNativeAds();
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

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(getString(R.string.error_no_songs_found));

            myView.findViewById(R.id.ll_empty_try).setVisibility(View.GONE);

            myView.findViewById(R.id.btn_empty_downloads).setOnClickListener(v -> {
                Intent intent = new Intent(AudioByDBPlaylistActivity.this, DownloadActivity.class);
                startActivity(intent);
            });

            myView.findViewById(R.id.btn_empty_music_lib).setOnClickListener(v -> {
                Intent intentLib = new Intent(AudioByDBPlaylistActivity.this, OfflineMusicActivity.class);
                startActivity(intentLib);
            });

            frameLayout.addView(myView);
        }
    }

    private void loadNativeAds() {
        if (helper.canLoadNativeAds(this, Callback.PAGE_NATIVE_POST) && arrayList.size() >= 10) {
            switch (Callback.getAdNetwork()) {
                case Callback.AD_TYPE_ADMOB -> {
                    AdLoader.Builder builder = new AdLoader.Builder(this, Callback.getAdmobNativeAdID());
                    AdLoader adLoader = builder.forNativeAd(
                            nativeAd -> {
                                // A native ad loaded successfully, check if the ad loader has finished loading
                                // and if so, insert the ads into the list.
                                try {
                                    adapter.addAds(nativeAd);
                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "onReceiveAd:", e);
                                }
                            }).build();

                    // Load the Native Express ad.
                    adLoader.loadAds(new AdRequest.Builder().build(), 5);
                }
                case Callback.AD_TYPE_STARTAPP -> {
                    StartAppNativeAd nativeAd = new StartAppNativeAd(this);
                    nativeAd.loadAd(new NativeAdPreferences()
                            .setAdsNumber(3)
                            .setAutoBitmapDownload(true)
                            .setPrimaryImageSize(2), new AdEventListener() {
                        @Override
                        public void onReceiveAd(@NonNull Ad ad) {
                            if (adapter != null) {
                                adapter.addNativeAds(nativeAd.getNativeAds());
                            }
                        }

                        @Override
                        public void onFailedToReceiveAd(Ad ad) {
                            // this method is empty
                        }
                    });
                }
                case Callback.AD_TYPE_APPLOVIN, Callback.AD_TYPE_WORTISE ->
                        adapter.setNativeAds(true);
                default -> adapter.setNativeAds(false);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEquilizerChange(ItemAlbums itemAlbums) {
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
        GlobalBus.getBus().removeStickyEvent(itemAlbums);
    }

    @Override
    public void onDestroy() {
        if(adapter != null) {
            adapter.closeDatabase();
        }
        super.onDestroy();
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