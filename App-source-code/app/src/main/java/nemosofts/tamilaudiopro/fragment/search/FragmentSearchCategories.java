package nemosofts.tamilaudiopro.fragment.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import nemosofts.tamilaudiopro.adapter.AdapterCat;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.executor.LoadCat;
import nemosofts.tamilaudiopro.interfaces.CategoryListener;
import nemosofts.tamilaudiopro.item.ItemCat;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.recycler.EndlessRecyclerViewScrollListener;
import nemosofts.tamilaudiopro.utils.recycler.RecyclerItemClickListener;

public class FragmentSearchCategories extends Fragment {

    private Helper helper;
    private RecyclerView rv;
    private AdapterCat adapterCat;
    private ArrayList<ItemCat> arrayList;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private Boolean isLoading = false;
    private String errorMsg = "";
    private SearchView searchView;
    private int page = 1;
    private Boolean isOver = false;
    private Boolean isScroll = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_normal, container, false);

        helper = new Helper(getActivity(), (position, type) -> openAudioByIDActivity(position));

        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).setTitle(getString(R.string.search_categories));
        ((MainActivity) requireActivity()).bottomNavigationView(5);

        arrayList = new ArrayList<>();

        progressBar = rootView.findViewById(R.id.pb);
        frameLayout = rootView.findViewById(R.id.fl_empty);

        rv = rootView.findViewById(R.id.rv);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (adapterCat.getItemViewType(position) == -2 || adapterCat.isHeader(position)) ? manager.getSpanCount() : 1;
            }
        });
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), (view, position) -> helper.showInterAd(position, "")));
        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(manager) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (getActivity() == null){
                    return;
                }
                if (Boolean.FALSE.equals(isOver)) {
                    if (Boolean.FALSE.equals(isLoading)) {
                        isLoading = true;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            isScroll = true;
                            loadCategories();
                        }, 0);
                    }
                } else {
                    adapterCat.hideHeader();
                }
            }
        });

        loadCategories();

        addMenuProvider();
        return rootView;
    }

    private void openAudioByIDActivity(int position) {
        Intent intent = new Intent(getActivity(), AudioByIDActivity.class);
        intent.putExtra("type", getString(R.string.categories));
        intent.putExtra("id", adapterCat.getItem(position).getId());
        intent.putExtra("name", adapterCat.getItem(position).getName());
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
            if (adapterCat != null && (!searchView.isIconified())) {
                adapterCat.getFilter().filter(s);
                adapterCat.notifyDataSetChanged();
            }
            return true;
        }
    };

    private void loadCategories() {
        if (!NetworkUtils.isConnected(requireContext())) {
            errorMsg = getString(R.string.error_internet_not_connected);
            setEmpty();
            return;
        }
        LoadCat loadCat = new LoadCat(new CategoryListener() {
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
                              String message, ArrayList<ItemCat> arrayListCat) {
                if (getActivity() == null) {
                    return;
                }
                if (success.equals("1")) {
                    loadCatEnd(verifyStatus, message, arrayListCat);
                } else {
                    errorMsg = getString(R.string.error_server);
                    setEmpty();
                }
                progressBar.setVisibility(View.GONE);
                isLoading = false;
            }
        }, helper.getAPIRequest(Method.METHOD_CAT, page, "", "",
                Callback.getSearchItem(), "","", "", "", "",
                "","","","search", null));
        loadCat.execute();
    }

    private void loadCatEnd(String verifyStatus,
                            String message, ArrayList<ItemCat> arrayListCat) {
        if (verifyStatus.equals("-1")) {
            DialogUtil.verifyDialog(requireActivity(), getString(R.string.error_unauthorized_access), message, () -> {
            });
            return;
        }
        if (arrayListCat.isEmpty()) {
            isOver = true;
            errorMsg = getString(R.string.error_no_cat_found);
            setEmpty();
        } else {
            page = page + 1;
            arrayList.addAll(arrayListCat);
            setAdapter();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setAdapter() {
        if (Boolean.FALSE.equals(isScroll)) {
            adapterCat = new AdapterCat(getActivity(), arrayList);
            rv.setAdapter(adapterCat);
            setEmpty();
        } else {
            adapterCat.notifyDataSetChanged();
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
            LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);
            TextView btnText = myView.findViewById(R.id.tv_empty);

            if (errorMsg.equals(getString(R.string.error_no_cat_found))) {
                btnText.setText(getString(R.string.refresh));
            } else if (errorMsg.equals(getString(R.string.error_internet_not_connected))) {
                btnText.setText(getString(R.string.retry));
            } else if (errorMsg.equals(getString(R.string.error_server))) {
                btnText.setText(getString(R.string.retry));
            }

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errorMsg);

            myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> loadCategories());
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