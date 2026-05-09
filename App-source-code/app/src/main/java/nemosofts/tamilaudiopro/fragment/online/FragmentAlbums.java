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
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAd;

import java.util.ArrayList;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.AudioByIDActivity;
import nemosofts.tamilaudiopro.activity.DownloadActivity;
import nemosofts.tamilaudiopro.activity.OfflineMusicActivity;
import nemosofts.tamilaudiopro.adapter.AdapterAlbums;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.executor.LoadAlbums;
import nemosofts.tamilaudiopro.fragment.search.FragmentSearchAlbums;
import nemosofts.tamilaudiopro.interfaces.AlbumsListener;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.recycler.EndlessRecyclerViewScrollListener;
import nemosofts.tamilaudiopro.utils.recycler.RecyclerItemClickListener;

public class FragmentAlbums extends Fragment {

    private static final String TAG = "FragmentAlbums";
    private Helper helper;
    private RecyclerView rv;
    private AdapterAlbums adapterAlbums;
    private ArrayList<ItemAlbums> arrayList;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private String errorMsg;
    private String homeSecId ="";
    private int page = 1;
    private int nativeAdPos = 0;
    private Boolean isOver = false;
    private Boolean isScroll = false;
    private Boolean isLoading = false;
    private Boolean isFromHome = false;
    private AdLoader adLoader;
    private final ArrayList<NativeAd> arrayListNativeAds = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_normal, container, false);

        helper = new Helper(getActivity(), (position, type) -> openAudioByIDActivity(position));

        try {
            if (getArguments() != null) {
                homeSecId = getArguments().getString("id");
                isFromHome = true;
            }
        } catch (Exception e) {
            homeSecId = "";
            isFromHome = false;
        }

        arrayList = new ArrayList<>();

        progressBar = rootView.findViewById(R.id.pb);
        frameLayout = rootView.findViewById(R.id.fl_empty);
        rv = rootView.findViewById(R.id.rv);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapterAlbums.getItemViewType(position) == -2 || adapterAlbums.isHeader(position)) ? manager.getSpanCount() : 1;
            }
        });

        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), (view, position) -> helper.showInterAd(position, "")));

        if(Boolean.FALSE.equals(isFromHome)) {
            rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(manager) {
                @Override
                public void onLoadMore(int p, int totalItemsCount) {
                    if (getActivity() == null){
                        return;
                    }
                    if (Boolean.FALSE.equals(isOver) && (Boolean.FALSE.equals(isLoading))) {
                        isLoading = true;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            isScroll = true;
                            loadAlbums();
                        }, 0);
                    }
                }
            });
        }

        if(Callback.getNativeAdShow()%2 != 0) {
            nativeAdPos = Callback.getNativeAdShow() + 1;
        } else {
            nativeAdPos = Callback.getNativeAdShow();
        }

        loadAlbums();

        addMenuProvider();
        return rootView;
    }

    private void openAudioByIDActivity(int position) {
        if(adapterAlbums.getItem(position) == null) {
           return;
        }
        Intent intent = new Intent(requireContext(), AudioByIDActivity.class);
        intent.putExtra("type", getString(R.string.albums));
        intent.putExtra("id", adapterAlbums.getItem(position).getId());
        intent.putExtra("name", adapterAlbums.getItem(position).getName());
        startActivity(intent);
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
        public boolean onQueryTextSubmit(@NonNull String s) {
            Callback.setSearchItem(s.replace(" ", "%20"));
            FragmentSearchAlbums fsearch = new FragmentSearchAlbums();
            FragmentManager fm = getParentFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.hide(getParentFragmentManager().getFragments().get(getParentFragmentManager().getBackStackEntryCount()));
            ft.add(R.id.fragment, fsearch, getString(R.string.search_albums));
            ft.addToBackStack(getString(R.string.search_albums));
            ft.commit();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    private void loadAlbums() {
        if (!NetworkUtils.isConnected(requireContext())) {
            errorMsg = getString(R.string.error_internet_not_connected);
            setEmpty();
            return;
        }
        String helperName = Boolean.TRUE.equals(isFromHome) ? Method.METHOD_HOME_DETAILS : Method.METHOD_ALBUMS;
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
            public void onEnd(String success, String verifyStatus,
                              String message, ArrayList<ItemAlbums> arrayListAlbums) {
                if (getActivity() == null) {
                    return;
                }
                if (success.equals("1")) {
                    loadAlbumsEnd(verifyStatus, message, arrayListAlbums);
                } else {
                    isOver = true;
                    try {
                        adapterAlbums.hideHeader();
                    } catch (Exception e) {
                        Log.e(TAG, "Error in hideHeader", e);
                    }
                    errorMsg = getString(R.string.error_server);
                    setEmpty();
                }
                progressBar.setVisibility(View.GONE);
                isLoading = false;
            }
        }, helper.getAPIRequest(helperName, page, homeSecId, "", "",
                "", "", "", "", "","",
                "","","", null));
        loadAlbums.execute();
    }

    private void loadAlbumsEnd(String verifyStatus,
                               String message,
                               ArrayList<ItemAlbums> arrayListAlbums) {
        if (verifyStatus.equals("-1")) {
            DialogUtil.verifyDialog(requireActivity(), getString(R.string.error_unauthorized_access), message, () -> {
            });
            return;
        }
        if (arrayListAlbums.isEmpty()) {
            isOver = true;
            errorMsg = getString(R.string.error_no_albums_found);
            try {
                adapterAlbums.hideHeader();
            } catch (Exception e) {
                Log.e(TAG, "Error in hideHeader", e);
            }
            setEmpty();
        } else {
            for (int i = 0; i < arrayListAlbums.size(); i++) {
                arrayList.add(arrayListAlbums.get(i));
                if (helper.canLoadNativeAds(requireContext(),Callback.PAGE_NATIVE_CAT)) {
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

    @SuppressLint("NotifyDataSetChanged")
    private void setAdapter() {
        if (Boolean.FALSE.equals(isScroll)) {
            adapterAlbums = new AdapterAlbums(getActivity(), arrayList, true);
            rv.setAdapter(adapterAlbums);
            setEmpty();
            loadNativeAds();
        } else {
            adapterAlbums.notifyDataSetChanged();
        }
    }

    private void loadNativeAds() {
        if (helper.canLoadNativeAds(requireContext(),Callback.PAGE_NATIVE_CAT)
                && Callback.getAdNetwork().equals(Callback.AD_TYPE_ADMOB)
                || Callback.getAdNetwork().equals(Callback.AD_TYPE_META)
                && arrayList.size() >= 10) {

            AdLoader.Builder builder = new AdLoader.Builder(requireContext(), Callback.getAdmobNativeAdID());
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
                    Log.e(TAG, "Error in addAds", e);
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
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater
                    .inflate(R.layout.layout_empty, null);
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
}