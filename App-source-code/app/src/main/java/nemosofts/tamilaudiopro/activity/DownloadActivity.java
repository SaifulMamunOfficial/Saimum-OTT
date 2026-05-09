package nemosofts.tamilaudiopro.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.adapter.AdapterOFSongList;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.ClickDeleteListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class DownloadActivity extends NSoftsPlayerActivity {

    private RecyclerView rv;
    private AdapterOFSongList adapter;
    private ArrayList<ItemSong> arrayList;
    private ProgressBar progressBar;

    private FrameLayout frameLayout;
    private String errorMsg = "";
    private SearchView searchView;
    private final String addedFrom = "download";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.fragment_audio, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        bottomNav.setVisibility(View.GONE);
        adViewPlayer.setVisibility(View.VISIBLE);

        toolbar.setTitle(getString(R.string.downloads));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (Boolean.TRUE.equals(new ThemeEngine(this).getIsThemeMode())){
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backspace_white);
            } else {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backspace_black);
            }
        }

        dbHelper = new DBHelper(DownloadActivity.this);
        helper = new Helper(DownloadActivity.this, (position, type) -> openPlayerService());
        helper.showBannerAd(adViewPlayer,"");

        errorMsg = getString(R.string.error_no_songs_found);

        arrayList = new ArrayList<>();

        progressBar = findViewById(R.id.pb_audio);
        frameLayout = findViewById(R.id.fl_empty);
        rv = findViewById(R.id.rv_audio);
        LinearLayoutManager manager = new LinearLayoutManager(DownloadActivity.this);
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());

        if(checkPermissionDownload()) {
            new LoadDownloadSongs().execute();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openPlayerService() {
        Intent intent = new Intent(DownloadActivity.this, PlayerService.class);
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
            if (adapter != null && (!searchView.isIconified())) {
                adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }
            return true;
        }
    };

    @SuppressLint("StaticFieldLeak")
    class LoadDownloadSongs extends AsyncTaskExecutor<String, String, String> {

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
                File fileRoot = new File(getExternalFilesDir("").getAbsolutePath() + File.separator + "temp");
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
            if (isFinishing()){
                return;
            }
            progressBar.setVisibility(View.GONE);
            setAdapterData();
        }
    }

    private void setAdapterData() {
        adapter = new AdapterOFSongList(DownloadActivity.this, arrayList, new ClickDeleteListenerPlayList() {
            @Override
            public void onClick(int position) {
                Callback.setIsOnline(false);
                if (!Callback.getAddedFrom().equals(addedFrom)) {
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
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);
            TextView btnTitle = myView.findViewById(R.id.tv_empty);

            if (errorMsg.equals(getString(R.string.error_no_albums_found))) {
                btnTitle.setText(getString(R.string.refresh));
            } else if (errorMsg.equals(getString(R.string.error_internet_not_connected))) {
                btnTitle.setText(getString(R.string.retry));
            } else if (errorMsg.equals(getString(R.string.error_server))) {
                btnTitle.setText(getString(R.string.retry));
            }

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errorMsg);

            myView.findViewById(R.id.ll_empty_try).setVisibility(View.GONE);
            myView.findViewById(R.id.btn_empty_downloads).setVisibility(View.GONE);
            myView.findViewById(R.id.btn_empty_music_lib).setOnClickListener(v -> startActivity(new Intent(DownloadActivity.this, OfflineMusicActivity.class)));

            frameLayout.addView(myView);
        }
    }

    @NonNull
    private Boolean checkPermissionDownload() {
        String permission;
        if (Build.VERSION.SDK_INT >= 33) {
            permission = READ_MEDIA_AUDIO;
        } else if (Build.VERSION.SDK_INT >= 29) {
            permission = READ_EXTERNAL_STORAGE;
        } else {
            permission = WRITE_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(DownloadActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission);  // Request permission using the new API
            return false;
        }
        return true;
    }

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted ->
                    Toast.makeText(DownloadActivity.this, Boolean.TRUE.equals(isGranted) ? "Permission granted"
                            : getResources().getString(R.string.error_cannot_use_features), Toast.LENGTH_SHORT).show()
    );

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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
    protected void onDestroy() {
        try {
            dbHelper.close();
            if(adapter != null) {
                adapter.closeDatabase();
            }
        } catch (Exception e) {
            Log.e("DownloadActivity", "Error in closeDatabase",e);
        }
        super.onDestroy();
    }
}