package nemosofts.tamilaudiopro.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.adapter.AdapterSelectableSongList;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class SelectSongActivity extends AppCompatActivity {

    Toolbar toolbar;
    Helper methods;
    DBHelper dbHelper;
    RecyclerView rv;
    ArrayList<ItemSong> arrayList;
    FrameLayout frameLayout;
    AdapterSelectableSongList adapter;
    CheckBox checkBox;
    TextView selectAdd;
    String pid = "";
    String type = "";
    String playID = "";

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
        type = getIntent().getStringExtra("type");
        if (type != null && type.equals(getString(R.string.playlist))) {
            playID = getIntent().getStringExtra("play_id");
        }

        dbHelper = new DBHelper(this);
        methods = new Helper(this);

        toolbar = this.findViewById(R.id.toolbar_select);
        toolbar.setTitle("0 " + getString(R.string.selected));
        this.setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        selectAdd = findViewById(R.id.tv_select_add);
        selectAdd.setTypeface(selectAdd.getTypeface(), Typeface.BOLD);
        frameLayout = findViewById(R.id.fl_empty);

        arrayList = new ArrayList<>();
        switch (type) {
            case "recent":
                arrayList.addAll(dbHelper.loadDataRecent(false, "30"));
                break;
            case "playlist":
                arrayList.addAll(dbHelper.loadDataPlaylist(playID, false));
                break;
            case "edit":
                ArrayList<ItemSong> items = (ArrayList<ItemSong>) getIntent().getSerializableExtra("array");
                if (items != null) {
                    arrayList.addAll(items);
                }
                selectAdd.setText(getString(R.string.remove));
                break;
            default:
                arrayList.addAll(Callback.getArrayListOfflineSongs());
                break;
        }

        rv = findViewById(R.id.rv_select);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setNestedScrollingEnabled(false);

        adapter = new AdapterSelectableSongList(arrayList, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                if (getSupportActionBar() != null){
                    getSupportActionBar().setTitle(adapter.getSelectedCounts() + " " + getString(R.string.selected));
                }
            }

            @Override
            public void onItemZero() {
                // this method is empty
            }
        });

        selectClickListener();

        rv.setAdapter(adapter);
        setEmpty();
    }

    private void selectClickListener() {
        selectAdd.setOnClickListener(v -> {
            List<ItemSong> checkedArray = adapter.getSelectedIDs();
            if (!checkedArray.isEmpty()) {
                for (int i = 0; i < checkedArray.size(); i++) {
                    if (!type.equals(getString(R.string.edit))) {
                        dbHelper.addToPlayList(checkedArray.get(i), pid, false);
                    } else {
                        dbHelper.removeFromPlayList(checkedArray.get(i).getId(), false);
                    }
                }
                finish();
            } else {
                Toast.makeText(SelectSongActivity.this, getString(R.string.select_song), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_select_song;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_checkbox, menu);
        checkBox = (CheckBox) menu.findItem(R.id.menu_cb).getActionView();
        Objects.requireNonNull(checkBox).setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.toggleSelectAll(isChecked);
            if (getSupportActionBar() != null){
                getSupportActionBar().setTitle(adapter.getSelectedCounts() + " " + getString(R.string.selected));
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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
            TextView btnTitle = myView.findViewById(R.id.tv_empty);
            btnTitle.setText(getString(R.string.refresh));

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(getString(R.string.error_no_songs_found));

            myView.findViewById(R.id.ll_empty_try).setVisibility(View.GONE);

            myView.findViewById(R.id.btn_empty_downloads).setOnClickListener(v -> {
                Intent intent = new Intent(SelectSongActivity.this, DownloadActivity.class);
                startActivity(intent);
            });

            myView.findViewById(R.id.btn_empty_music_lib).setOnClickListener(v -> {
                Intent intentLib = new Intent(SelectSongActivity.this, OfflineMusicActivity.class);
                startActivity(intentLib);
            });

            frameLayout.addView(myView);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            dbHelper.close();
        } catch (Exception e) {
            Log.e("SelectSongActivity","Error dbHelper close", e);
        }
        super.onDestroy();
    }
}