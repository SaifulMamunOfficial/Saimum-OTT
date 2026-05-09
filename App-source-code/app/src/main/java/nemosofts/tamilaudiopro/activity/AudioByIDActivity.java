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
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.nemosofts.utils.NetworkUtils;
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

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.adapter.AdapterAllSongList;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.executor.LoadAudio;
import nemosofts.tamilaudiopro.interfaces.AudioListener;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;
import nemosofts.tamilaudiopro.utils.recycler.EndlessRecyclerViewScrollListener;
import okhttp3.RequestBody;

public class AudioByIDActivity extends NSoftsPlayerActivity {

    private static final String LOG_TAG = "AudioByIDActivity";
    ThemeEngine themeEngine;
    RecyclerView rv;
    AdapterAllSongList adapter;
    ArrayList<ItemSong> arrayList;
    ProgressBar progressBar;
    String id = "";
    String name = "";
    String type = "";
    FrameLayout frameLayout;
    String errorMsg;
    SearchView searchView;
    Boolean isFromPush = false;
    int page = 1;
    String addedFrom = "";
    Boolean isOver = false;
    Boolean isScroll = false;
    Boolean isLoading = false;
    Boolean isShuffleAll = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.fragment_audio, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        bottomNav.setVisibility(View.GONE);
        adViewPlayer.setVisibility(View.VISIBLE);

        isFromPush = getIntent().getBooleanExtra("isPush", false);
        type = getIntent().getStringExtra("type");
        id = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("name");

        themeEngine = new ThemeEngine(this);
        helper = new Helper(this, (position, type) -> openPlayerService(position));
        helper.showBannerAd(adViewPlayer,Callback.PAGE_POST_DETAILS);

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

        frameLayout = findViewById(R.id.fl_empty);
        progressBar = findViewById(R.id.pb_audio);
        rv = findViewById(R.id.rv_audio);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());

        arrayList = new ArrayList<>();
        loadSongs();

        if(!type.equals(getString(R.string.banner))) {
            rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(manager) {
                @Override
                public void onLoadMore(int p, int totalItemsCount) {
                    if (Boolean.FALSE.equals(isOver) && (Boolean.FALSE.equals(isLoading))) {
                        isLoading = true;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            isScroll = true;
                            loadSongs();
                        }, 0);
                    }
                }
            });
        }

        findViewById(R.id.rl_play_all).setOnClickListener(view -> {
            isShuffleAll = false;
            if (!Callback.getAddedFrom().equals(addedFrom)){
                Callback.setIsOnline(true);
                if (!Callback.getArrayListPlay().isEmpty()){
                    Callback.getArrayListPlay().clear();
                }
                Callback.setArrayListPlay(arrayList);
                Callback.setAddedFrom(addedFrom);
                Callback.setIsNewAdded(true);
                Callback.setPlayPos(0);
                Toast.makeText(AudioByIDActivity.this, "Add to play all", Toast.LENGTH_SHORT).show();
                helper.showInterAd(0, "");
            } else {
                Toast.makeText(AudioByIDActivity.this, getString(R.string.already_added), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.rl_shuffle_all).setOnClickListener(view -> {
            if (isShuffleAll){
                isShuffleAll = false;
                Callback.setArrayListPlay(arrayList);
                GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
                Toast.makeText(AudioByIDActivity.this, getString(R.string.add_to_queue), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AudioByIDActivity.this, getString(R.string.already_added), Toast.LENGTH_SHORT).show();
            }
        });

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

        Intent intent = new Intent(AudioByIDActivity.this, PlayerService.class);
        intent.setAction(PlayerService.ACTION_PLAY);
        startService(intent);
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
            if (adapter != null && (!searchView.isIconified())) {
                adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }
            return true;
        }
    };

    private void loadSongs() {
        if (NetworkUtils.isConnected(this)) {
            RequestBody requestBody = null;
            if (type.equals(getString(R.string.categories))) {
                addedFrom = "cat" + name;
                requestBody = helper.getAPIRequest(Method.METHOD_SONG_BY_CAT, page, "", id,
                        "", "", new SPHelper(AudioByIDActivity.this).getUserId(),
                        "", "", "", "", "", "", "", null);
            } else if (type.equals(getString(R.string.albums))) {
                addedFrom = "albums" + name;
                requestBody = helper.getAPIRequest(Method.METHOD_SONG_BY_ALBUMS, page, id, "",
                        "", "", new SPHelper(AudioByIDActivity.this).getUserId(),
                        "", "", "", "", "", "", "",  null);
            } else if (type.equals(getString(R.string.artist))) {
                addedFrom = "artist" + name;
                requestBody = helper.getAPIRequest(Method.METHOD_SONG_BY_ARTIST, page, "",
                        "", name.replace(" ", "%20"), "",
                        new SPHelper(AudioByIDActivity.this).getUserId(), "","",
                        "", "", "", "", "", null);
            } else if (type.equals(getString(R.string.playlist))) {
                addedFrom = "serverplay" + name;
                requestBody = helper.getAPIRequest(Method.METHOD_SONG_BY_PLAYLIST, page, id,
                        "", "", "",
                        new SPHelper(AudioByIDActivity.this).getUserId(), "",
                        "", "", "", "", "", "",  null);
            } else if (type.equals(getString(R.string.banner))) {
                addedFrom = "banner" + name;
                requestBody = helper.getAPIRequest(Method.METHOD_SONG_BY_BANNER, page, id,"",
                        "", "", new SPHelper(AudioByIDActivity.this).getUserId(),
                        "", "", "", "", "", "", "",  null);
            }else if (type.equals(getString(R.string.favourite))) {
                addedFrom = "favourite" + name;
                requestBody = helper.getAPIRequest(Method.METHOD_POST_BY_FAV, page, "","",
                        "", "", new SPHelper(AudioByIDActivity.this).getUserId(),
                        "", "", "", "", "", "", "songs",  null);
            }

            LoadAudio loadSong = new LoadAudio(new AudioListener() {
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
                                  String verifyStatus, String message, ArrayList<ItemSong> arrayListCatBySong) {
                    if (success.equals("1")) {
                        if (!verifyStatus.equals("-1") && !verifyStatus.equals("-2")) {
                            if (arrayListCatBySong.isEmpty()) {
                                isOver = true;
                                errorMsg = getString(R.string.error_no_songs_found);
                                setEmpty();
                            } else {
                                arrayList.addAll(arrayListCatBySong);
                                if (Boolean.TRUE.equals(isScroll) && Callback.getAddedFrom().equals(addedFrom)) {
                                    Callback.getArrayListPlay().clear();
                                    Callback.setArrayListPlay(arrayList);
                                    try {
                                        GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
                                    } catch (Exception e) {
                                        Log.e(LOG_TAG, "Error in posting event" , e);
                                    }
                                }
                                page = page + 1;
                                setAdapter();
                            }
                        } else if (verifyStatus.equals("-2")) {
                            DialogUtil.verifyDialog(AudioByIDActivity.this, getString(R.string.error_unauthorized_access), message, () -> {
                            });
                        } else {
                            DialogUtil.verifyDialog(AudioByIDActivity.this, getString(R.string.error_unauthorized_access), message, () -> {
                            });
                        }
                    } else {
                        errorMsg = getString(R.string.error_server);
                        setEmpty();
                    }
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                }
            }, requestBody);
            loadSong.execute();
        } else {
            errorMsg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setAdapter() {
        if (Boolean.FALSE.equals(isScroll)) {
            adapter = new AdapterAllSongList(AudioByIDActivity.this, arrayList, new ClickListenerPlayList() {
                @Override
                public void onClick(int position) {
                    helper.showInterAd(position, "");
                }

                @Override
                public void onItemZero() {
                    // this method is empty
                }
            }, "online");
            rv.setAdapter(adapter);
            setEmpty();
            loadNativeAds();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    public void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            findViewById(R.id.vi_play_all_audio).setVisibility(View.VISIBLE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);
            TextView btnText = myView.findViewById(R.id.tv_empty);

            if (errorMsg.equals(getString(R.string.error_no_songs_found))) {
                btnText.setText(getString(R.string.refresh));
            } else if (errorMsg.equals(getString(R.string.error_internet_not_connected))) {
                btnText.setText(getString(R.string.retry));
            } else if (errorMsg.equals(getString(R.string.error_server))) {
                btnText.setText(getString(R.string.retry));
            }

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errorMsg);

            myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> loadSongs());

            myView.findViewById(R.id.btn_empty_downloads).setOnClickListener(v -> {
                Intent intent = new Intent(AudioByIDActivity.this, DownloadActivity.class);
                startActivity(intent);
            });

            myView.findViewById(R.id.btn_empty_music_lib).setOnClickListener(v -> {
                Intent intentLib = new Intent(AudioByIDActivity.this, OfflineMusicActivity.class);
                startActivity(intentLib);
            });

            frameLayout.addView(myView);
        }
    }

    private void loadNativeAds() {
        if (helper.canLoadNativeAds(AudioByIDActivity.this, Callback.PAGE_NATIVE_POST) && arrayList.size() >= 10) {
            switch (Callback.getAdNetwork()) {
                case Callback.AD_TYPE_ADMOB -> {
                    AdLoader.Builder builder = new AdLoader.Builder(AudioByIDActivity.this, Callback.getAdmobNativeAdID());
                    AdLoader adLoader = builder.forNativeAd(
                            nativeAd -> {
                                // A native ad loaded successfully, check if the ad loader has finished loading
                                // and if so, insert the ads into the list.
                                try {
                                    adapter.addAds(nativeAd);
                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "Error in addAds" , e);
                                }
                            }).build();

                    // Load the Native Express ad.
                    adLoader.loadAds(new AdRequest.Builder().build(), 5);
                }
                case Callback.AD_TYPE_STARTAPP -> {
                    StartAppNativeAd nativeAd = new StartAppNativeAd(AudioByIDActivity.this);
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
                } else if (Boolean.TRUE.equals(isFromPush)) {
                    Intent intent = new Intent(AudioByIDActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
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