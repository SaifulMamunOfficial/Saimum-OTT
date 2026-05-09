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
import nemosofts.tamilaudiopro.adapter.AdapterArtist;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.item.ItemArtist;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.recycler.RecyclerItemClickListener;

public class FragmentOFArtist extends Fragment {

    private RecyclerView rv;
    private AdapterArtist adapterArtist;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private String errorMsg = "";
    private SearchView searchView;
    private Boolean isLoaded = false;

    @NonNull
    @Contract(" -> new")
    public static FragmentOFArtist newInstance() {
        return new FragmentOFArtist();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_normal, container, false);

        Helper helper = new Helper(getActivity(), (position, type) -> {
            Intent intent = new Intent(getActivity(), OfflineSongByActivity.class);
            intent.putExtra("type", getString(R.string.artist));
            intent.putExtra("id", adapterArtist.getItem(position).getId());
            intent.putExtra("name", adapterArtist.getItem(position).getName());
            startActivity(intent);
        });
        errorMsg = getString(R.string.error_no_artist_found);

        progressBar = rootView.findViewById(R.id.pb);
        frameLayout = rootView.findViewById(R.id.fl_empty);
        rv = rootView.findViewById(R.id.rv);

        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3);
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
            if (adapterArtist != null && (!searchView.isIconified())) {
                adapterArtist.getFilter().filter(s);
                adapterArtist.notifyDataSetChanged();
            }
            return true;
        }
    };

    private class LoadOfflineArtist extends AsyncTaskExecutor<String, String, String> {

        @Override
        protected void onPreExecute() {
            frameLayout.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String strings) {
            if (Callback.getArrayListOfflineArtist().isEmpty()) {
                getListOfArtist();
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
    private void getListOfArtist() {
        try {
            if (getActivity() == null) {
                return;
            }

            final Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
            final String id = BaseColumns._ID;
            final String artist_name = MediaStore.MediaColumns.ARTIST;

            final String[] columns = {id, artist_name};
            Cursor cursor = getActivity().getContentResolver().query(uri, columns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String aid = String.valueOf(cursor.getLong(cursor.getColumnIndex(id)));
                    String name = cursor.getString(cursor.getColumnIndex(artist_name));
                    String image = "null";


                    Callback.setArrayListOfflineArtist(new ItemArtist(aid, name, image));

                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
           ApplicationUtil.log("FragmentOFArtist","Error getListOfArtist");
        }
    }

    public void setAdapter() {
        if (adapterArtist == null) {
            adapterArtist = new AdapterArtist(getActivity(), Callback.getArrayListOfflineArtist());
            rv.setAdapter(adapterArtist);
            isLoaded = true;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    adapterArtist.hideHeader();
                } catch (Exception e) {
                    ApplicationUtil.log("FragmentOFArtist","hideHeader");
                }
            },1000);
        }
        setEmpty();
    }

    public void setEmpty() {
        if (!Callback.getArrayListOfflineArtist().isEmpty()) {
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
            new LoadOfflineArtist().execute();
        }
        super.setUserVisibleHint(isVisibleToUser);
    }
}