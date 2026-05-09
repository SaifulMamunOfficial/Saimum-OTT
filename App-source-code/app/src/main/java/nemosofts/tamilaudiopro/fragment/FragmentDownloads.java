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
import androidx.annotation.OptIn;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.OfflineMusicActivity;
import nemosofts.tamilaudiopro.activity.PlayerService;
import nemosofts.tamilaudiopro.adapter.AdapterOFSongList;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.ClickDeleteListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class FragmentDownloads extends Fragment {

    private static final String TAG = "FragmentDownloads";
    private DBHelper dbHelper;
    private Helper helper;
    private RecyclerView rv;
    private AdapterOFSongList adapter;
    private ArrayList<ItemSong> arrayList;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    private String errorMsg = "";
    private SearchView searchView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audio, container, false);

        dbHelper = new DBHelper(getActivity());
        helper = new Helper(getActivity(), (position, type) -> openPlayerService(position));
        errorMsg = getString(R.string.error_no_songs_found);

        arrayList = new ArrayList<>();

        progressBar = rootView.findViewById(R.id.pb_audio);
        frameLayout = rootView.findViewById(R.id.fl_empty);
        rv = rootView.findViewById(R.id.rv_audio);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());

        new LoadDownloadSongs().execute();

        addMenuProvider();
        return rootView;
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openPlayerService(int position) {
        Callback.setIsOnline(false);
        if (!Callback.getAddedFrom().equals(TAG)) {
            Callback.getArrayListPlay().clear();
            Callback.setArrayListPlay(arrayList);
            Callback.setAddedFrom(TAG);
            Callback.setIsNewAdded(true);
        }
        Callback.setPlayPos(position);

        Intent intent = new Intent(getActivity(), PlayerService.class);
        intent.setAction(PlayerService.ACTION_PLAY);
        requireActivity().startService(intent);
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

    private class LoadDownloadSongs extends AsyncTaskExecutor<String, String, String> {

        @Override
        protected void onPreExecute() {
            arrayList.clear();
            frameLayout.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String strings) {
            try {
                List<ItemSong> tempArray = dbHelper.loadDataDownload();
                File fileRoot = new File(requireActivity().getExternalFilesDir("").getAbsolutePath() + File.separator + "temp");
                File[] files = fileRoot.listFiles();
                if (files != null) {
                    for (File file : files) {
                        for (int j = 0; j < tempArray.size(); j++) {
                            if (new File(file.getAbsolutePath()).getName().contains(tempArray.get(j).getTempName())) {
                                ItemSong itemSong = tempArray.get(j);
                                itemSong.setUrl(file.getAbsolutePath());
                                arrayList.add(itemSong);
                                break;
                            }
                        }
                    }
                }
                return "1";
            } catch (Exception e) {
                return "0";
            }
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

    public void setAdapter() {
        adapter = new AdapterOFSongList(getActivity(), arrayList, new ClickDeleteListenerPlayList() {
            @Override
            public void onClick(int position) {
                helper.showInterAd(position, "");
            }

            @Override
            public void onItemZero() {
                // this method is empty
            }

            @Override
            public void onDelete(int pos, Exception exception, int deleteRequestUriR, int deleteRequestUriQ) {
                // this method is empty
            }
        }, "downloads");
        rv.setAdapter(adapter);
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
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);
            TextView btnText = myView.findViewById(R.id.tv_empty);

            if (errorMsg.equals(getString(R.string.error_no_songs_found))) {
                btnText.setText(getString(R.string.refresh));
            } else if (errorMsg.equals(getString(R.string.error_internet_not_connected))) {
                btnText.setText(getString(R.string.retry));
            } else if (errorMsg.equals(getString(R.string.error_server))) {
                btnText.setText(getString(R.string.retry));
            }

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errorMsg);

            myView.findViewById(R.id.ll_empty_try).setVisibility(View.GONE);
            myView.findViewById(R.id.btn_empty_downloads).setVisibility(View.GONE);

            myView.findViewById(R.id.btn_empty_music_lib).setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), OfflineMusicActivity.class);
                startActivity(intent);
            });

            frameLayout.addView(myView);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEquilizerChange(ItemAlbums itemAlbums) {
        if(adapter != null) {
            adapter.notifyDataSetChanged();
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

    @Override
    public void onDestroy() {
        try {
            if(adapter != null) {
                dbHelper.close();
                adapter.closeDatabase();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error closeDatabase", e);
        }
        super.onDestroy();
    }
}