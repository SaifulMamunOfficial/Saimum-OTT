package nemosofts.tamilaudiopro.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.adapter.AdapterDBPlaylist;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;

public class OfflinePlaylistAddActivity extends AppCompatActivity {

    DBHelper dbHelper;
    RecyclerView rv;
    AdapterDBPlaylist adapterDBPlaylist;
    ArrayList<ItemMyPlayList> arrayList;
    FrameLayout frameLayout;
    String pid = "";
    Boolean isLoaded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bg_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pid = getIntent().getStringExtra("pid");

        dbHelper = new DBHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar_add_2_offplay);
        toolbar.setTitle(getString(R.string.add_songs));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());

        arrayList = new ArrayList<>();
        arrayList.addAll(dbHelper.loadPlayList(false));

        frameLayout = findViewById(R.id.fl_empty);

        rv = findViewById(R.id.rv_add_2_offplay);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rv.setLayoutManager(gridLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setNestedScrollingEnabled(false);

        setClickListener();

        adapterDBPlaylist = new AdapterDBPlaylist(this, arrayList, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(OfflinePlaylistAddActivity.this, SelectSongActivity.class);
                intent.putExtra("pid", pid);
                intent.putExtra("type", getString(R.string.playlist));
                intent.putExtra("play_id", arrayList.get(position).getId());
                startActivity(intent);
            }

            @Override
            public void onItemZero() {
                // this method is empty
            }
        }, false);

        rv.setAdapter(adapterDBPlaylist);
        setEmpty();
    }

    private void setClickListener() {
        findViewById(R.id.ll_local).setOnClickListener(v -> {
            Intent intent = new Intent(OfflinePlaylistAddActivity.this, SelectSongActivity.class);
            intent.putExtra("pid", pid);
            intent.putExtra("type", getString(R.string.songs));
            startActivity(intent);
        });
        findViewById(R.id.ll_recent).setOnClickListener(v -> {
            Intent intent = new Intent(OfflinePlaylistAddActivity.this, SelectSongActivity.class);
            intent.putExtra("pid", pid);
            intent.putExtra("type", getString(R.string.recent));
            startActivity(intent);
        });
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_add_off_playlist;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
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
            textView.setText(getString(R.string.error_no_playlist_found));

            myView.findViewById(R.id.ll_empty_try).setVisibility(View.GONE);

            myView.findViewById(R.id.btn_empty_downloads).setOnClickListener(v -> {
                Intent intent = new Intent(OfflinePlaylistAddActivity.this, DownloadActivity.class);
                startActivity(intent);
            });

            myView.findViewById(R.id.btn_empty_music_lib).setVisibility(View.GONE);

            frameLayout.addView(myView);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        if (Boolean.TRUE.equals(isLoaded)) {
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
            Log.e("OfflinePlaylistAddActivity", "Error closeDatabase: " ,e);
        }
        super.onDestroy();
    }
}