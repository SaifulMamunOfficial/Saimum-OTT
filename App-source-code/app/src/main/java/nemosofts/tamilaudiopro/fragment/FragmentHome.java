package nemosofts.tamilaudiopro.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.DownloadActivity;
import nemosofts.tamilaudiopro.activity.OfflineMusicActivity;
import nemosofts.tamilaudiopro.adapter.home.AdapterHome;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.executor.LoadHome;
import nemosofts.tamilaudiopro.fragment.search.FragmentSearch;
import nemosofts.tamilaudiopro.interfaces.HomeListener;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.item.ItemPost;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class FragmentHome extends Fragment {

    private static final String TAG = "FragmentHome";
    private DBHelper dbHelper;
    private Helper helper;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private RecyclerView rv;
    private AdapterHome adapterHome;
    private ArrayList<ItemPost> arrayList;
    private String errorMsg;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        helper = new Helper(getActivity());

        arrayList = new ArrayList<>();
        dbHelper = new DBHelper(getActivity());

        progressBar = rootView.findViewById(R.id.pb_home);
        frameLayout = rootView.findViewById(R.id.fl_empty);

        rv = rootView.findViewById(R.id.rv_home);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
        rv.setItemAnimator(new DefaultItemAnimator());

        loadHome();

        addMenuProvider();
        return rootView;
    }

    private void loadHome() {
        if (!NetworkUtils.isConnected(requireContext())) {
            errorMsg = getString(R.string.error_internet_not_connected);
            setEmpty();
            return;
        }
        LoadHome loadHome = new LoadHome(requireContext(), new HomeListener() {
            @Override
            public void onStart() {
                frameLayout.setVisibility(View.GONE);
                rv.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onEnd(String success, String message, ArrayList<ItemPost> arrayListPost) {
                if (getActivity() == null) {
                    return;
                }
                if (success.equals("1")) {
                    loadHomeEnd(arrayListPost);
                } else {
                    errorMsg = getString(R.string.error_server);
                    setEmpty();
                }
                progressBar.setVisibility(View.GONE);
            }
        }, helper.getAPIRequest(Method.METHOD_HOME, 0, dbHelper.getRecentIDs("10"),
                "", "", "", "", "", "", "",
                "", "", "", "", null));
        loadHome.execute();
    }

    private void loadHomeEnd(ArrayList<ItemPost> arrayListPost) {
        if (arrayListPost.isEmpty()){
            errorMsg = getString(R.string.error_no_data_found);
            setEmpty();
            return;
        }
        if (Callback.getArrayListPlay().isEmpty()) {
            for (int i = 0; i < arrayListPost.size(); i++) {
                if(!arrayListPost.get(i).getArrayListSongs().isEmpty()) {
                    Callback.setArrayListPlay(arrayListPost.get(i).getArrayListSongs());
                    try {
                        GlobalBus.getBus().postSticky(Callback.getArrayListPlay().get(0));
                    } catch (Exception e) {
                        Log.e(TAG, "Error postSticky", e);
                    }
                    break;
                }
            }
        }
        arrayList.addAll(arrayListPost);
        if (Boolean.TRUE.equals(Callback.getIsAdsStatus())){
            arrayList.add(new ItemPost("100","ads","ads", false));
        }
        adapterHome = new AdapterHome(getActivity(), arrayList);
        rv.setAdapter(adapterHome);
        setEmpty();
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
            LayoutInflater inflater = (LayoutInflater) requireContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

            myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> loadHome());

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
            FragmentSearch search = new FragmentSearch();
            FragmentManager fm = getParentFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.hide(fm.findFragmentByTag(getString(R.string.nav_home)));
            ft.add(R.id.fragment, search, getString(R.string.search));
            ft.addToBackStack(getString(R.string.search));
            ft.commit();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEquilizerChange(ItemAlbums itemAlbums) {
        try {
            adapterHome.onEqualizerChange();
        } catch (Exception e) {
            Log.e(TAG, "Error onEqualizerChange", e);
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
}