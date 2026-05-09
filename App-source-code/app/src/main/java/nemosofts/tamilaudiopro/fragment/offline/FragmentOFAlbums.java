package nemosofts.tamilaudiopro.fragment.offline;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.provider.MediaStore;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Contract;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.DownloadActivity;
import nemosofts.tamilaudiopro.activity.OfflineSongByActivity;
import nemosofts.tamilaudiopro.adapter.AdapterAlbums;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.recycler.RecyclerItemClickListener;

public class FragmentOFAlbums extends Fragment {

    private Helper helper;
    private RecyclerView rv;
    private AdapterAlbums adapterAlbums;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private String errorMsg = "";
    private SearchView searchView;
    private Boolean isLoaded = false;

    @NonNull
    @Contract(" -> new")
    public static FragmentOFAlbums newInstance() {
        return new FragmentOFAlbums();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_normal, container, false);

        helper = new Helper(getActivity(), (position, type) -> {
            Intent intent = new Intent(getActivity(), OfflineSongByActivity.class);
            intent.putExtra("id", adapterAlbums.getItem(position).getId());
            intent.putExtra("name", adapterAlbums.getItem(position).getName());
            intent.putExtra("type", getString(R.string.albums));
            startActivity(intent);
        });
        errorMsg = getString(R.string.error_no_albums_found);

        progressBar = rootView.findViewById(R.id.pb);
        frameLayout = rootView.findViewById(R.id.fl_empty);
        rv = rootView.findViewById(R.id.rv);

        GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), (view, position) -> helper.showInterAd(position, "")));

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
            if (adapterAlbums != null && (!searchView.isIconified())) {
                adapterAlbums.getFilter().filter(s);
                adapterAlbums.notifyDataSetChanged();
            }
            return true;
        }
    };

    private class LoadOfflineAlbums extends AsyncTaskExecutor<String, String, String> {

        @Override
        protected void onPreExecute() {
            frameLayout.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String strings) {
            if (Callback.getArrayListOfflineAlbums().isEmpty()) {
                getListOfAlbums();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (getActivity() == null) {
                return;
            }
            setAdapter();
            progressBar.setVisibility(View.GONE);
        }
    }

    @SuppressLint("Range")
    private void getListOfAlbums() {
        if (getActivity() == null) {
            return;
        }
        final Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        final String _id = BaseColumns._ID;
        final String albumName = MediaStore.MediaColumns.ALBUM;
        final String artist =  MediaStore.MediaColumns.ARTIST;
        final String albumart = MediaStore.Audio.Albums.ALBUM_ART;
        final String tracks = MediaStore.Audio.Albums.NUMBER_OF_SONGS;

        final String[] columns = {_id, albumName, artist, albumart, tracks};
        Cursor cursor = getActivity().getContentResolver().query(uri, columns, null, null, null);

        // add to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = String.valueOf(cursor.getLong(cursor.getColumnIndex(_id)));
                String name = cursor.getString(cursor.getColumnIndex(albumName));
                String image = helper.getAlbumArtUri(Long.parseLong(id)).toString();
                String artistName = cursor.getString(cursor.getColumnIndex(artist));

                Callback.setArrayListOfflineAlbums(new ItemAlbums(id, name, image, artistName));

            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    public void setAdapter() {
        if (adapterAlbums == null) {
            adapterAlbums = new AdapterAlbums(getActivity(), Callback.getArrayListOfflineAlbums(), false);
            rv.setAdapter(adapterAlbums);
            isLoaded = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    adapterAlbums.hideHeader();
                } catch (Exception e) {
                    ApplicationUtil.log("FragmentOFAlbums","hideHeader");
                }
            },1000);
        }
        setEmpty();
    }

    public void setEmpty() {
        if (!Callback.getArrayListOfflineAlbums().isEmpty()) {
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
            btnText.setText(getString(R.string.refresh));

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errorMsg);

            myView.findViewById(R.id.ll_empty_try).setVisibility(View.GONE);
            myView.findViewById(R.id.btn_empty_music_lib).setVisibility(View.GONE);
            myView.findViewById(R.id.btn_empty_downloads).setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DownloadActivity.class);
                startActivity(intent);
            });

            frameLayout.addView(myView);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser && rv != null && (Boolean.FALSE.equals(isLoaded))) {
            new LoadOfflineAlbums().execute();
        }
        super.setUserVisibleHint(isVisibleToUser);
    }
}