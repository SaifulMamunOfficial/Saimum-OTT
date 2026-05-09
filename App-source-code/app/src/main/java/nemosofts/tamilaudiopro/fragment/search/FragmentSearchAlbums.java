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
import android.widget.Toast;

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

public class FragmentSearchAlbums extends Fragment {

    private static final String TAG = "FragmentSearchAlbums";
    private Helper helper;
    private RecyclerView rv;
    private AdapterAlbums adapter;
    private ArrayList<ItemAlbums> arrayList;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private String errorMsg;
    private int page = 1;
    private Boolean isOver = false;
    private Boolean isScroll = false;
    private Boolean isLoading = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_normal, container, false);

        helper = new Helper(getActivity(), (position, type) -> openAudioByIDActivity(position));

        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).setTitle(getString(R.string.search_albums));
        ((MainActivity) requireActivity()).bottomNavigationView(5);

        arrayList = new ArrayList<>();

        frameLayout = rootView.findViewById(R.id.fl_empty);
        progressBar = rootView.findViewById(R.id.pb);
        rv = rootView.findViewById(R.id.rv);
        GridLayoutManager manager = new GridLayoutManager(getActivity(),2);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapter.getItemViewType(position) == -2 || adapter.isHeader(position)) ? manager.getSpanCount() : 1;
            }
        });
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());
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
        rv.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), (view, position) -> helper.showInterAd(position, "")));

        loadAlbums();

        addMenuProvider();
        return rootView;
    }

    private void openAudioByIDActivity(int position) {
        Intent intent = new Intent(getActivity(), AudioByIDActivity.class);
        intent.putExtra("type", getString(R.string.albums));
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
                page = 1;
                isScroll = false;
                Callback.setSearchItem(s.replace(" ", "%20"));
                arrayList.clear();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                loadAlbums();
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

    private void loadAlbums() {
        if (!NetworkUtils.isConnected(requireContext())) {
            errorMsg = getString(R.string.error_internet_not_connected);
            setEmpty();
            return;
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
            public void onEnd(String success, String verifyStatus,
                              String message, ArrayList<ItemAlbums> arrayListAlbums) {
                if (getActivity() == null) {
                    return;
                }
                if (success.equals("1")) {
                    loadAlbumsEnd(verifyStatus, message, arrayListAlbums);
                } else {
                    errorMsg = getString(R.string.error_server);
                    setEmpty();
                }
                progressBar.setVisibility(View.GONE);
                isLoading = false;
            }
        }, helper.getAPIRequest(Method.METHOD_ALBUMS, page, "", "",
                Callback.getSearchItem(), "","", "", "", "",
                "","","","search", null));
        loadAlbums.execute();
    }

    private void loadAlbumsEnd(String verifyStatus,
                               String message, ArrayList<ItemAlbums> arrayListAlbums) {
        if (verifyStatus.equals("-1")) {
            DialogUtil.verifyDialog(requireActivity(), getString(R.string.error_unauthorized_access), message, () -> {
            });
            return;
        }
        if (arrayListAlbums.isEmpty()) {
            isOver = true;
            errorMsg = getString(R.string.error_no_albums_found);
            try {
                adapter.hideHeader();
            } catch (Exception e) {
                Log.e(TAG, "Error hideHeader", e);
            }
            setEmpty();
        } else {
            page = page + 1;
            arrayList.addAll(arrayListAlbums);
            setAdapter();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setAdapter() {
        if (Boolean.FALSE.equals(isScroll)) {
            adapter = new AdapterAlbums(getActivity(), arrayList, true);
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
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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