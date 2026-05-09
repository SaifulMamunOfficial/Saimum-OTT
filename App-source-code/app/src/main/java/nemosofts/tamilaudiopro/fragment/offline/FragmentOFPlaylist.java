package nemosofts.tamilaudiopro.fragment.offline;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.view.WindowManager.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.DownloadActivity;
import nemosofts.tamilaudiopro.activity.OfflinePlaylistBySongActivity;
import nemosofts.tamilaudiopro.adapter.AdapterDBPlaylist;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class FragmentOFPlaylist extends Fragment {

    private DBHelper dbHelper;
    private Helper helper;
    private RecyclerView rv;
    private AdapterDBPlaylist adapterDBPlaylist;
    private ArrayList<ItemMyPlayList> arrayList;
    private FrameLayout frameLayout;
    private Boolean isLoaded = false;
    private SearchView searchView;

    @NonNull
    @Contract(" -> new")
    public static FragmentOFPlaylist newInstance() {
        return new FragmentOFPlaylist();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_playlist, container, false);

        dbHelper = new DBHelper(getActivity());

        helper = new Helper(getActivity(), (position, type) -> {
            Intent intent = new Intent(getActivity(), OfflinePlaylistBySongActivity.class);
            intent.putExtra("item", adapterDBPlaylist.getItem(position));
            startActivity(intent);
        });

        arrayList = new ArrayList<>();
        arrayList.addAll(dbHelper.loadPlayList(false));

        frameLayout = rootView.findViewById(R.id.fl_empty);
        rv = rootView.findViewById(R.id.rv_myplaylist);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        rv.setLayoutManager(gridLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setNestedScrollingEnabled(false);

        rootView.findViewById(R.id.fab_my_playlist).setOnClickListener(view -> openAddPlaylistDialog());

        adapterDBPlaylist = new AdapterDBPlaylist(getActivity(), arrayList, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                helper.showInterAd(position, "");
            }

            @Override
            public void onItemZero() {
                setEmpty();
            }
        }, false);
        rv.setAdapter(adapterDBPlaylist);
        setEmpty();

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
            if (adapterDBPlaylist != null && (!searchView.isIconified())) {
                adapterDBPlaylist.getFilter().filter(s);
                adapterDBPlaylist.notifyDataSetChanged();
            }
            return true;
        }
    };

    public void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);
            TextView btnText = myView.findViewById(R.id.tv_empty);
            btnText.setText(getString(R.string.refresh));

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(getString(R.string.error_no_playlist_found));

            myView.findViewById(R.id.ll_empty_try).setVisibility(View.GONE);
            myView.findViewById(R.id.btn_empty_music_lib).setVisibility(View.GONE);
            myView.findViewById(R.id.btn_empty_downloads).setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DownloadActivity.class);
                startActivity(intent);
            });

            frameLayout.addView(myView);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void openAddPlaylistDialog() {
        final InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(INPUT_METHOD_SERVICE);
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_playlist);
        final EditText editText = dialog.findViewById(R.id.et_dialog_addplay);
        dialog.findViewById(R.id.tv_cancel).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.iv_close).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.tv_submit).setOnClickListener(view -> {
            if (!editText.getText().toString().trim().isEmpty()) {
                arrayList.clear();
                arrayList.addAll(dbHelper.addPlayList(editText.getText().toString(), false));
                Toast.makeText(getActivity(), getString(R.string.playlist_added), Toast.LENGTH_SHORT).show();
                adapterDBPlaylist.notifyDataSetChanged();
                setEmpty();
                dialog.dismiss();
            }
        });
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        new Handler(Looper.getMainLooper()).post(() -> {
            editText.requestFocus();
            inputMethodManager.showSoftInput(editText, 0);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser && adapterDBPlaylist != null) {
            arrayList.clear();
            arrayList.addAll(dbHelper.loadPlayList(false));
            adapterDBPlaylist.notifyDataSetChanged();
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        if (Boolean.TRUE.equals(isLoaded) && adapterDBPlaylist != null) {
            arrayList.clear();
            arrayList.addAll(dbHelper.loadPlayList(false));
            adapterDBPlaylist.notifyDataSetChanged();
        } else {
            isLoaded = true;
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        try {
            dbHelper.close();
            if(adapterDBPlaylist != null) {
                adapterDBPlaylist.closeDatabase();
            }
        } catch (Exception e) {
            ApplicationUtil.log("FragmentOFPlaylist","Error closeDatabase",e);
        }
        super.onDestroy();
    }
}