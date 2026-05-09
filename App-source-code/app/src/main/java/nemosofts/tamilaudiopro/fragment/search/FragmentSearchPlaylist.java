package nemosofts.tamilaudiopro.fragment.search;

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
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.AudioByIDActivity;
import nemosofts.tamilaudiopro.activity.DownloadActivity;
import nemosofts.tamilaudiopro.activity.MainActivity;
import nemosofts.tamilaudiopro.activity.OfflineMusicActivity;
import nemosofts.tamilaudiopro.adapter.AdapterServerPlaylist;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.executor.LoadServerPlaylist;
import nemosofts.tamilaudiopro.interfaces.ServerPlaylistListener;
import nemosofts.tamilaudiopro.item.ItemServerPlayList;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.recycler.EndlessRecyclerViewScrollListener;
import nemosofts.tamilaudiopro.utils.recycler.RecyclerItemClickListener;

public class FragmentSearchPlaylist extends Fragment {

    private static final String TAG = "FragmentSearchPlaylist";
    private Helper helper;
    private RecyclerView rv;
    private AdapterServerPlaylist adapter;
    private ArrayList<ItemServerPlayList> arrayList;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private String errorMsg;
    private SearchView searchView;
    private int page = 1;
    private Boolean isOver = false;
    private Boolean isScroll = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_normal, container, false);

        helper = new Helper(getActivity(), (position, type) -> openAudioByIDActivity(position));

        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).setTitle(getString(R.string.search_playlist));
        ((MainActivity) requireActivity()).bottomNavigationView(5);

        arrayList = new ArrayList<>();

        frameLayout = rootView.findViewById(R.id.fl_empty);
        progressBar = rootView.findViewById(R.id.pb);
        rv = rootView.findViewById(R.id.rv);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapter.getItemViewType(position) == -2 || adapter.isHeader(position)) ? manager.getSpanCount() : 1;
            }
        });
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), (view, position) -> helper.showInterAd(position, "")));
        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(manager) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (getActivity() == null) {
                    return;
                }
                if (Boolean.FALSE.equals(isOver)) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        isScroll = true;
                        loadPlaylist();
                    }, 0);
                } else {
                    adapter.hideHeader();
                }
            }
        });

        loadPlaylist();

        addMenuProvider();
        return rootView;
    }

    private void openAudioByIDActivity(int position) {
        Intent intent = new Intent(getActivity(), AudioByIDActivity.class);
        intent.putExtra("type", getString(R.string.playlist));
        intent.putExtra("id", adapter.getItem(position).getId());
        intent.putExtra("name", adapter.getItem(position).getName());
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
                searchView = (SearchView) item.getActionView();
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

    private void loadPlaylist() {
        if (!NetworkUtils.isConnected(requireContext())) {
            errorMsg = getString(R.string.error_internet_not_connected);
            setEmpty();
            return;
        }
        LoadServerPlaylist loadServerPlaylist = new LoadServerPlaylist(new ServerPlaylistListener() {
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
                              String message, ArrayList<ItemServerPlayList> arrayListPlaylist) {
                if (getActivity() == null) {
                    return;
                }
                if (success.equals("1")) {
                    loadServerPlaylistEnd(verifyStatus, message, arrayListPlaylist);
                } else {
                    errorMsg = getString(R.string.error_server);
                    setEmpty();
                }
                progressBar.setVisibility(View.GONE);
            }
        }, helper.getAPIRequest(Method.METHOD_SERVER_PLAYLIST, page, "", "",
                Callback.getSearchItem(), "","", "", "", "",
                "","","","search", null));
        loadServerPlaylist.execute();
    }

    private void loadServerPlaylistEnd(String verifyStatus, String message,
                                       ArrayList<ItemServerPlayList> arrayListPlaylist) {
        if (verifyStatus.equals("-1")) {
            DialogUtil.verifyDialog(requireActivity(), getString(R.string.error_unauthorized_access), message, () -> {
            });
            return;
        }
        if (arrayListPlaylist.isEmpty()) {
            isOver = true;
            errorMsg = getString(R.string.error_no_playlist_found);
            try {
                adapter.hideHeader();
            } catch (Exception e) {
                Log.e(TAG, "Error in hideHeader",e);
            }
            setEmpty();
        } else {
            page = page + 1;
            arrayList.addAll(arrayListPlaylist);
            setAdapter();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setAdapter() {
        if (Boolean.FALSE.equals(isScroll)) {
            adapter = new AdapterServerPlaylist(getActivity(), arrayList);
            rv.setAdapter(adapter);
            setEmpty();
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
            LayoutInflater inflater = (LayoutInflater) requireActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);
            TextView btnText = myView.findViewById(R.id.tv_empty);

            if (errorMsg.equals(getString(R.string.error_no_playlist_found))) {
                btnText.setText(getString(R.string.refresh));
            } else if (errorMsg.equals(getString(R.string.error_internet_not_connected))) {
                btnText.setText(getString(R.string.retry));
            } else if (errorMsg.equals(getString(R.string.error_server))) {
                btnText.setText(getString(R.string.retry));
            }

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errorMsg);

            myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> loadPlaylist());
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