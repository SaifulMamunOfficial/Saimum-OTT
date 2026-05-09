package nemosofts.tamilaudiopro.activity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.SearchView;
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
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class OfflineSongByActivity extends NSoftsPlayerActivity {

    private static final String TAG = "OfflineSongByActivity";
    RecyclerView rv;
    AdapterOFSongList adapterSons;
    List<ItemSong> arrayList;
    ProgressBar progressBar;
    String type = "";
    String id = "";
    String name = "";
    FrameLayout frameLayout;
    String addedFrom = "";
    SearchView searchView;

    private final String[] projection = {
            BaseColumns._ID,
            MediaStore.MediaColumns.TITLE,
            MediaStore.MediaColumns.ARTIST,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.fragment_audio, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        bottomNav.setVisibility(View.GONE);
        adViewPlayer.setVisibility(View.VISIBLE);

        type = getIntent().getStringExtra("type");
        id = getIntent().getStringExtra("id");
        name = getIntent().getStringExtra("name");

        helper = new Helper(this, (position, type) -> openPlayerService());
        helper.showBannerAd(adViewPlayer,Callback.PAGE_POST_DETAILS);

        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (Boolean.TRUE.equals(new ThemeEngine(this).getIsThemeMode())){
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backspace_white);
            }else {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backspace_black);
            }
        }

        arrayList = new ArrayList<>();

        frameLayout = findViewById(R.id.fl_empty);
        progressBar = findViewById(R.id.pb_audio);
        rv = findViewById(R.id.rv_audio);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());

        new LoadOfflineSongs().execute();

        setupBackPressHandler();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openPlayerService() {
        Intent intent = new Intent(OfflineSongByActivity.this, PlayerService.class);
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

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public boolean onQueryTextChange(String s) {
            if (adapterSons != null && (!searchView.isIconified())) {
                adapterSons.getFilter().filter(s);
                adapterSons.notifyDataSetChanged();
            }
            return true;
        }
    };

    @SuppressLint("StaticFieldLeak")
    class LoadOfflineSongs extends AsyncTaskExecutor<String, String, String> {

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
            getListOfSongs();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (isFinishing()){
                return;
            }
            setAdapter();
            progressBar.setVisibility(View.GONE);
        }
    }

    @SuppressLint("Range")
    public void getListOfSongs() {
        if (type.equals(getString(R.string.playlist))) {
            addedFrom = "offplay"+name;
            arrayList = dbHelper.loadDataPlaylist(id, false);
        } else if (type.equals(getString(R.string.albums))) {
            addedFrom = "offalbum"+name;
            String selection = "is_music != 0";

            selection = selection + " and album_id = " + id;

            final String sortOrder = MediaStore.MediaColumns.ALBUM + " ASC";

            try (Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    sortOrder)) {

                if (cursor != null && cursor.moveToFirst()) {
                    processCursor(cursor);
                }
            } catch (Exception e) {
                ApplicationUtil.log("Media", "Error querying media", e);
            }
        } else if (type.equals(getString(R.string.artist))) {
            addedFrom = "offartist"+name;
            String selection = "is_music != 0";

            selection = selection + " and artist_id = " + id;

            final String sortOrder = MediaStore.MediaColumns.ARTIST + "  ASC";

            try (Cursor cursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    sortOrder)) {

                if (cursor != null && cursor.moveToFirst()) {
                    processCursor(cursor);
                }
            } catch (Exception e) {
                ApplicationUtil.log("Media", "Error querying media", e);
            }
        }
    }

    @SuppressLint("Range")
    private void processCursor(Cursor cursor) {
        if (cursor == null){
            return;
        }
        do {
             String songID = String.valueOf(cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)));
            long durationLong = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.DURATION));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.ARTIST));
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

            String url = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songID).toString();
            String image = helper.getAlbumArtUri(albumId).toString();
            String duration = ApplicationUtil.milliSecondsToTimerDownload(durationLong);
            String desc = formatDescription(title, artist);

            arrayList.add(new ItemSong(
                    songID, artist, url, url, url, image, title, duration,
                    desc, "0", "0", "0", false
            ));
        } while (cursor.moveToNext());
    }

    @NonNull
    private String formatDescription(String title, String artist) {
        return getString(R.string.title) + " - " + title + "</br>" + getString(R.string.artist) + " - " + artist;
    }

    private void setAdapter() {
        adapterSons = new AdapterOFSongList(OfflineSongByActivity.this, arrayList, new ClickDeleteListenerPlayList() {
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
                // this method is empty
            }

            @Override
            public void onDelete(int pos, Exception exception, int deleteRequestUriR, int deleteRequestUriQ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && exception instanceof RecoverableSecurityException) {
                    try {
                        ArrayList<Uri> arrayListUri = new ArrayList<>();
                        arrayListUri.add(Uri.parse(adapterSons.getItem(pos).getUrl()));
                        PendingIntent editPendingIntent = MediaStore.createDeleteRequest(getContentResolver(), arrayListUri);
                        startIntentSenderForResult(editPendingIntent.getIntentSender(), deleteRequestUriR, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        ApplicationUtil.log(TAG,"Error startIntentSenderForResult");
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && exception instanceof RecoverableSecurityException exception2) {
                    try {
                        startIntentSenderForResult(exception2.getUserAction().getActionIntent().getIntentSender(), deleteRequestUriQ, null, 0, 0,    0);
                    } catch (IntentSender.SendIntentException e) {
                        ApplicationUtil.log(TAG,"Error startIntentSenderForResult");
                    }
                }
            }
        }, type);
        rv.setAdapter(adapterSons);
        setEmpty();
    }

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
            textView.setText(getString(R.string.error_no_songs_found));

            myView.findViewById(R.id.ll_empty_try).setVisibility(View.GONE);

            myView.findViewById(R.id.btn_empty_downloads).setOnClickListener(v -> {
                Intent intent = new Intent(OfflineSongByActivity.this, DownloadActivity.class);
                startActivity(intent);
            });

            myView.findViewById(R.id.btn_empty_music_lib).setVisibility(View.GONE);

            frameLayout.addView(myView);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEquilizerChange(ItemAlbums itemAlbums) {
        if(adapterSons != null) {
            adapterSons.notifyDataSetChanged();
        }
        GlobalBus.getBus().removeStickyEvent(itemAlbums);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapterSons.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        try {
            dbHelper.close();
            if(adapterSons != null) {
                adapterSons.closeDatabase();
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG,"Error closeDatabase: ",e);
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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