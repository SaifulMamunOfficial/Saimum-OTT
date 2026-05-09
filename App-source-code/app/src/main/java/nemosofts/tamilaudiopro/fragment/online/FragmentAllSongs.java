package nemosofts.tamilaudiopro.fragment.online;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.DownloadActivity;
import nemosofts.tamilaudiopro.activity.OfflineMusicActivity;
import nemosofts.tamilaudiopro.activity.PlayerService;
import nemosofts.tamilaudiopro.adapter.AdapterAllSongList;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.executor.LoadAudio;
import nemosofts.tamilaudiopro.fragment.search.FragmentSearchSong;
import nemosofts.tamilaudiopro.interfaces.AudioListener;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;
import nemosofts.tamilaudiopro.utils.recycler.EndlessRecyclerViewScrollListener;


public class FragmentAllSongs extends Fragment {

    private static final String TAG = "FragmentAllSongs";
    private Helper helper;
    private RecyclerView rv;
    private AdapterAllSongList adapter;
    private ArrayList<ItemSong> arrayList;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private String errorMsg;
    int page = 1;
    private Boolean isOver = false;
    private Boolean isScroll = false;
    private Boolean isLoading = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audio, container, false);

        helper = new Helper(requireContext(), (position, type) -> openPlayerService(position));

        arrayList = new ArrayList<>();

        frameLayout = rootView.findViewById(R.id.fl_empty);
        progressBar = rootView.findViewById(R.id.pb_audio);
        rv = rootView.findViewById(R.id.rv_audio);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());

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

        loadSongs();

        addMenuProvider();
        return rootView;
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openPlayerService(int position) {
        Callback.setIsOnline(true);
        if (!Callback.getAddedFrom().equals(TAG)) {
            Callback.getArrayListPlay().clear();
            Callback.setArrayListPlay(arrayList);
            Callback.setAddedFrom(TAG);
            Callback.setIsNewAdded(true);
        }
        Callback.setPlayPos(position);

        Intent intent = new Intent(getActivity(), PlayerService.class);
        intent.setAction(PlayerService.ACTION_PLAY);
        requireContext().startService(intent);
    }

    private void addMenuProvider() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.menu_search, menu);

                // Configure the search menu item
                MenuItem item = menu.findItem(R.id.menu_search);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
                SearchView searchView = (SearchView) item.getActionView();
                if (searchView != null) {
                    searchView.setOnQueryTextListener(queryTextListener);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle menu item selection if necessary
                return false;
            }
        }, getViewLifecycleOwner());
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            Callback.setSearchItem(s.replace(" ", "%20"));
            FragmentSearchSong fsearch = new FragmentSearchSong();
            FragmentManager fm = getParentFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.hide(getParentFragmentManager().getFragments().get(getParentFragmentManager().getBackStackEntryCount()));
            ft.add(R.id.fragment, fsearch, getString(R.string.search));
            ft.addToBackStack(getString(R.string.search));
            ft.commit();
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return true;
        }
    };

    private void loadSongs() {
        if (!NetworkUtils.isConnected(requireContext())) {
            errorMsg = getString(R.string.error_internet_not_connected);
            setEmpty();
            return;
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
            public void onEnd(String success, String verifyStatus,
                              String message, ArrayList<ItemSong> arrayListSong) {
                if (getActivity() == null) {
                    return;
                }
                if (success.equals("1")) {
                    if (!verifyStatus.equals("-1") && !verifyStatus.equals("-2")) {
                        loadSongEnd(arrayListSong);
                    } else {
                        DialogUtil.verifyDialog(requireActivity(), getString(R.string.error_unauthorized_access), message, () -> {
                        });
                    }
                } else {
                    errorMsg = getString(R.string.error_server);
                    setEmpty();
                }
                progressBar.setVisibility(View.GONE);
                isLoading = false;
            }
        }, helper.getAPIRequest(Method.METHOD_SONG_ALL, page, "", "", "",
                "",new SPHelper(getActivity()).getUserId(), "", "",
                "","","","","", null));
        loadSong.execute();
    }

    private void loadSongEnd(ArrayList<ItemSong> arrayListSong) {
        if (arrayListSong.isEmpty()) {
            isOver = true;
            errorMsg = getString(R.string.error_no_songs_found);
            setEmpty();
        } else {
            arrayList.addAll(arrayListSong);
            if (Boolean.TRUE.equals(isScroll) && Callback.getAddedFrom().equals(TAG)) {
                Callback.getArrayListPlay().clear();
                Callback.setArrayListPlay(arrayList);
                try {
                    GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
                } catch (Exception e) {
                    Log.e(TAG, "Error in postSticky", e);
                }
            }
            page = page + 1;
            setAdapter();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setAdapter() {
        if (Boolean.FALSE.equals(isScroll)) {
            adapter = new AdapterAllSongList(getActivity(), arrayList, new ClickListenerPlayList() {
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
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
                Intent intent = new Intent(getActivity(), DownloadActivity.class);
                startActivity(intent);
            });

            myView.findViewById(R.id.btn_empty_music_lib).setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), OfflineMusicActivity.class);
                startActivity(intent);
            });

            frameLayout.addView(myView);
        }
    }

    private void loadNativeAds() {
        if (helper.canLoadNativeAds(requireContext(), Callback.PAGE_NATIVE_POST) && arrayList.size() >= 10) {
            switch (Callback.getAdNetwork()) {
                case Callback.AD_TYPE_ADMOB -> {
                    AdLoader.Builder builder = new AdLoader.Builder(requireContext(), Callback.getAdmobNativeAdID());
                    AdLoader adLoader = builder.forNativeAd(
                            nativeAd -> {
                                // A native ad loaded successfully, check if the ad loader has finished loading
                                // and if so, insert the ads into the list.
                                try {
                                    adapter.addAds(nativeAd);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error in addAds", e);
                                }
                            }).build();

                    // Load the Native Express ad.
                    adLoader.loadAds(new AdRequest.Builder().build(), 5);
                }
                case Callback.AD_TYPE_STARTAPP -> {
                    StartAppNativeAd nativeAd = new StartAppNativeAd(requireContext());
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
        if (adapter != null){
            adapter.notifyDataSetChanged();
        }
        GlobalBus.getBus().removeStickyEvent(itemAlbums);
    }

    @Override
    public void onStart() {
        super.onStart();
        GlobalBus.getBus().register(this);
    }

    @Override
    public void onStop() {
        GlobalBus.getBus().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        try {
            if (adapter != null) {
                adapter.closeDatabase();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in closeDatabase", e);
        }
        super.onDestroy();
    }
}