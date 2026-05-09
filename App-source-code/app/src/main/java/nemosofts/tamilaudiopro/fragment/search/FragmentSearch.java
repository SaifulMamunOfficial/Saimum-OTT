package nemosofts.tamilaudiopro.fragment.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.DownloadActivity;
import nemosofts.tamilaudiopro.activity.MainActivity;
import nemosofts.tamilaudiopro.activity.OfflineMusicActivity;
import nemosofts.tamilaudiopro.adapter.AdapterSearch;
import nemosofts.tamilaudiopro.executor.LoadSearch;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.interfaces.SearchListener;
import nemosofts.tamilaudiopro.item.ItemPost;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;

public class FragmentSearch extends Fragment {

    Helper helper;
    private RecyclerView rv;
    private AdapterSearch adapterSearch;
    private ArrayList<ItemPost> arrayList;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private String errorMsg;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_search, container, false);

        helper = new Helper(requireActivity(), (position, type) -> {

        });
        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).setTitle(getString(R.string.search));
        ((MainActivity) requireActivity()).bottomNavigationView(5);

        arrayList = new ArrayList<>();

        frameLayout = rootView.findViewById(R.id.fl_empty);
        progressBar = rootView.findViewById(R.id.pb_search);

        rv = rootView.findViewById(R.id.rv_search);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());

        loadSongs();

        addMenuProvider();
        return rootView;
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
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public boolean onQueryTextSubmit(String s) {
            if (NetworkUtils.isConnected(requireContext())) {
                Callback.setSearchItem(s.replace(" ", "%20"));
                arrayList.clear();
                if (adapterSearch != null){
                    adapterSearch.notifyDataSetChanged();
                }
                loadSongs();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    private void loadSongs() {
        if (NetworkUtils.isConnected(requireContext())) {
            LoadSearch loadSong = new LoadSearch(new SearchListener() {
                @Override
                public void onStart() {
                    arrayList.clear();
                    frameLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEnd(String success, ArrayList<ItemPost> arrayListPosts) {
                    if (getActivity() == null){
                        return;
                    }

                    if (success.equals("1")) {
                        if (arrayListPosts.isEmpty()){
                            errorMsg = getString(R.string.error_no_data_found);
                            setEmpty();
                        } else {
                            arrayList.addAll(arrayListPosts);
                            setAdapter();
                        }
                    } else {
                        errorMsg = getString(R.string.error_server);
                        setEmpty();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            }, helper.getAPIRequest(Method.METHOD_SEARCH, 0, "", "", Callback.getSearchItem(),
                    "",new SPHelper(getActivity()).getUserId(), "", "",
                    "","","","","", null));
            loadSong.execute();
        } else {
            errorMsg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    private void setAdapter() {
        adapterSearch = new AdapterSearch(getActivity(), arrayList);
        rv.setAdapter(adapterSearch);
        setEmpty();
    }

    public void setEmpty() {
        if (!arrayList.isEmpty()) {
            frameLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            frameLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
}