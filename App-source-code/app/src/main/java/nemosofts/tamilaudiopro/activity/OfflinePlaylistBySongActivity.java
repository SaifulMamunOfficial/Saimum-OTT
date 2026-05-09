package nemosofts.tamilaudiopro.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.adapter.AdapterOFSongList;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.ClickDeleteListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class OfflinePlaylistBySongActivity extends NSoftsPlayerActivity {

    Toolbar toolbarPlaylist;
    RecyclerView rv;
    ItemMyPlayList itemMyPlayList;
    AdapterOFSongList adapter;
    List<ItemSong> arrayList;
    ProgressBar progressBar;
    FrameLayout frameLayout;
    Boolean isLoaded = false;
    String addedFrom = "offplay";
    SearchView searchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_offline_playlist_audio, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        bottomNav.setVisibility(View.GONE);
        adViewPlayer.setVisibility(View.VISIBLE);

        itemMyPlayList = (ItemMyPlayList) getIntent().getSerializableExtra("item");
        if (itemMyPlayList == null){
            addedFrom = addedFrom + itemMyPlayList.getName();
        }

        helper = new Helper(this, (position, type) -> openPlayerService());
        dbHelper = new DBHelper(this);

        toolbar.setVisibility(View.GONE);

        toolbarPlaylist = findViewById(R.id.toolbar_playlist);
        setSupportActionBar(toolbarPlaylist);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (Boolean.TRUE.equals(new ThemeEngine(this).getIsThemeMode())){
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backspace_white);
            } else {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backspace_black);
            }
        }

        arrayList = new ArrayList<>();

        frameLayout = findViewById(R.id.fl_empty);
        progressBar = findViewById(R.id.pb_song_by_playlist);
        progressBar.setVisibility(View.GONE);

        rv = findViewById(R.id.rv_song_by_playlist);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setNestedScrollingEnabled(false);

        setClickListener();

        new LoadOfflineSongs().execute();

        setupBackPressHandler();
    }

    private void setClickListener() {
        findViewById(R.id.ll_delete).setOnClickListener(v -> openDeleteDialog());
        findViewById(R.id.ll_add_2_offplay).setOnClickListener(v -> {
            Intent intent = new Intent(OfflinePlaylistBySongActivity.this, OfflinePlaylistAddActivity.class);
            intent.putExtra("pid", itemMyPlayList.getId());
            startActivity(intent);
        });
        findViewById(R.id.ll_edit).setOnClickListener(v -> {
            Intent intent = new Intent(OfflinePlaylistBySongActivity.this, SelectSongActivity.class);
            intent.putExtra("type", getString(R.string.edit));
            intent.putExtra("pid", itemMyPlayList.getId());
            intent.putExtra("array", new ArrayList<>(arrayList));
            startActivity(intent);
        });
        findViewById(R.id.ll_addQueue).setOnClickListener(v -> {
            if (!arrayList.isEmpty()) {
                if (Callback.getIsOnline() || Callback.getIsDownloaded()) {
                    showQueueAlert();
                } else {
                    Callback.setArrayListPlay(arrayList);
                    GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
                    Toast.makeText(OfflinePlaylistBySongActivity.this, getString(R.string.queue_updated), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(OfflinePlaylistBySongActivity.this, getString(R.string.no_songs_to_add_queue), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openPlayerService() {
        Intent intent = new Intent(OfflinePlaylistBySongActivity.this, PlayerService.class);
        intent.setAction(PlayerService.ACTION_PLAY);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        if (searchView != null){
            searchView.setOnQueryTextListener(queryTextListener);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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

    @SuppressLint("StaticFieldLeak")
    private class LoadOfflineSongs extends AsyncTaskExecutor<String, String, String> {

        @Override
        protected void onPreExecute() {
            arrayList.clear();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String strings) {
            arrayList = dbHelper.loadDataPlaylist(itemMyPlayList.getId(), false);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (isFinishing()){
                return;
            }
            setAdapter();
        }
    }

    private void setAdapter() {
        adapter = new AdapterOFSongList(OfflinePlaylistBySongActivity.this, arrayList, new ClickDeleteListenerPlayList() {
            @Override
            public void onClick(int position) {
                Callback.setIsOnline(false);
                if(!Callback.getAddedFrom().equals(addedFrom)) {
                    Callback.getArrayListPlay().clear();
                    Callback.setArrayListPlay(arrayList);
                    Callback.setAddedFrom(addedFrom);
                    Callback.setIsNewAdded(true);
                }
                Callback.setPlayPos(position);
                helper.showInterAd(position, "");
            }

            @Override
            public void onItemZero() {
                setEmpty();
            }

            @Override
            public void onDelete(int pos, Exception exception, int deleteRequestUriR, int deleteRequestUriQ) {
                // this method is empty
            }
        }, getString(R.string.playlist));
        rv.setAdapter(adapter);
        setEmpty();
    }

    private void openDeleteDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(OfflinePlaylistBySongActivity.this, R.style.dialogTheme);
        dialog.setTitle(getString(R.string.delete));
        dialog.setMessage(getString(R.string.sure_delete_playlist));
        dialog.setPositiveButton(getString(R.string.delete), (dialogInterface, i) -> {
            dbHelper.removePlayList(itemMyPlayList.getId(), false);
            finish();
        });
        dialog.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {

        });
        dialog.show();
    }

    private void showQueueAlert() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(OfflinePlaylistBySongActivity.this, R.style.dialogTheme);
        dialog.setTitle(getString(R.string.add_to_queue));
        dialog.setMessage(getString(R.string.off_add_qeue_alert));
        dialog.setPositiveButton(getString(R.string.add), (dialogInterface, i) -> {
            Callback.getArrayListPlay().clear();
            Callback.setArrayListPlay(arrayList);

            Toast.makeText(OfflinePlaylistBySongActivity.this, getString(R.string.queue_updated), Toast.LENGTH_SHORT).show();
            GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));

            Callback.setIsOnline(false);
            if(!Callback.getAddedFrom().equals(addedFrom)) {
                Callback.getArrayListPlay().clear();
                Callback.setArrayListPlay(arrayList);
                Callback.setAddedFrom(addedFrom);
                Callback.setIsNewAdded(true);
            }
            Callback.setPlayPos(0);

            helper.showInterAd(0, "");
        });
        dialog.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {

        });
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    public void setEmpty() {
        if (!arrayList.isEmpty()) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);
            TextView btnText = myView.findViewById(R.id.tv_empty);
            btnText.setText(getString(R.string.refresh));

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(getString(R.string.error_no_playlist_found));

            myView.findViewById(R.id.ll_empty_try).setVisibility(View.GONE);

            myView.findViewById(R.id.btn_empty_downloads).setOnClickListener(v -> {
                Intent intent = new Intent(OfflinePlaylistBySongActivity.this, DownloadActivity.class);
                startActivity(intent);
            });

            myView.findViewById(R.id.btn_empty_music_lib).setVisibility(View.GONE);

            frameLayout.addView(myView);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        if (Boolean.TRUE.equals(isLoaded)) {
            new LoadOfflineSongs().execute();
        } else {
            isLoaded = true;
        }
        super.onResume();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        try {
            dbHelper.close();
            if(adapter != null) {
                adapter.closeDatabase();
            }
        } catch (Exception e) {
            Log.e("OfflinePlaylistBySongActivity", "Error closeDatabase: " ,e);
        }
        super.onDestroy();
    }

    private void setupBackPressHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    // Let the system handle the back
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}