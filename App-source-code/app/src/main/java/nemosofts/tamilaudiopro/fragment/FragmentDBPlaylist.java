package nemosofts.tamilaudiopro.fragment;

import static android.view.WindowManager.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.WRAP_CONTENT;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Dialog;
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

import java.util.ArrayList;
import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.AudioByDBPlaylistActivity;
import nemosofts.tamilaudiopro.activity.DownloadActivity;
import nemosofts.tamilaudiopro.activity.OfflineMusicActivity;
import nemosofts.tamilaudiopro.adapter.AdapterDBPlaylist;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class FragmentDBPlaylist extends Fragment {

    DBHelper dbHelper;
    Helper helper;
    RecyclerView rv;
    AdapterDBPlaylist adapterDBPlaylist;
    ArrayList<ItemMyPlayList> arrayList;
    FrameLayout frameLayout;
    SearchView searchView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_playlist, container, false);

        dbHelper = new DBHelper(getActivity());

        helper = new Helper(getActivity(), (position, type) -> openAudioByDBPlaylistActivity(position));

        arrayList = new ArrayList<>();
        arrayList.addAll(dbHelper.loadPlayList(true));

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
        }, true);

        rv.setAdapter(adapterDBPlaylist);
        setEmpty();

        addMenuProvider();
        return rootView;
    }

    private void openAudioByDBPlaylistActivity(int position) {
        Intent intent = new Intent(getActivity(), AudioByDBPlaylistActivity.class);
        intent.putExtra("item", adapterDBPlaylist.getItem(position));
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
            LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(getString(R.string.error_no_playlist_found));

            myView.findViewById(R.id.ll_empty_try).setVisibility(View.GONE);

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

    @SuppressLint("NotifyDataSetChanged")
    private void openAddPlaylistDialog() {
        final InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_playlist);
        final EditText addText = dialog.findViewById(R.id.et_dialog_addplay);
        dialog.findViewById(R.id.tv_submit).setOnClickListener(view -> {
            if (!addText.getText().toString().trim().isEmpty()) {
                arrayList.clear();
                arrayList.addAll(dbHelper.addPlayList(addText.getText().toString(), true));
                Toast.makeText(getActivity(), getString(R.string.playlist_added), Toast.LENGTH_SHORT).show();
                adapterDBPlaylist.notifyDataSetChanged();
                setEmpty();
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.tv_cancel).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.iv_close).setOnClickListener(view -> dialog.dismiss());
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        new Handler(Looper.getMainLooper()).post(() -> {
            inputMethodManager.showSoftInput(addText, 0);
            addText.requestFocus();
        });
    }

    @Override
    public void onDestroy() {
        try {
            dbHelper.close();
            if (adapterDBPlaylist != null) {
                adapterDBPlaylist.closeDatabase();
            }
        } catch (Exception e) {
            Log.e("FragmentDBPlaylist", "Error in onDestroy",e);
        }
        super.onDestroy();
    }
}