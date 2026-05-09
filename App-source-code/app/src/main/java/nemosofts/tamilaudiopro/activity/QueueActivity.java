package nemosofts.tamilaudiopro.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.adapter.AdapterQueueSongList;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.MessageEvent;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class QueueActivity extends AppCompatActivity {

    private static final String TAG = "QueueActivity";
    private Helper helper;
    private RecyclerView rv;
    private AdapterQueueSongList adapterQueueSongList;
    private ProgressBar pbQueue;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(view -> handleBackPressed());
        }

        helper = new Helper(this);
        helper = new Helper(this, (position, type) -> openPlayerService());

        pbQueue = findViewById(R.id.pb_queue);

        rv = findViewById(R.id.rv_queue);
        LinearLayoutManager manager = new LinearLayoutManager(QueueActivity.this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(manager);
        rv.setItemAnimator(new DefaultItemAnimator());

        loadQueue();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPressed();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void handleBackPressed() {
        try {
            GlobalBus.getBus().postSticky(true);
            Callback.setIsNewAdded(true);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "handleBackPressed" ,e);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openPlayerService() {
        Intent intent = new Intent(QueueActivity.this, PlayerService.class);
        intent.setAction(PlayerService.ACTION_PLAY);
        startService(intent);
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_queue;
    }

    private void loadQueue() {
        if (NetworkUtils.isConnected(this) && (!Callback.getArrayListPlay().isEmpty())){
            adapterQueueSongList = new AdapterQueueSongList(QueueActivity.this,Callback.getArrayListPlay(), (itemData, position) -> {
                Callback.setAddedFrom(TAG);
                Callback.setIsNewAdded(true);
                Callback.setPlayPos(position);
                helper.showInterAd(position,"");
            });
            rv.setAdapter(adapterQueueSongList);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEquilizerChange(ItemAlbums itemAlbums) {
        try {
            if (!Callback.getArrayListPlay().isEmpty()){
                adapterQueueSongList.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e(TAG, "onEquilizerChange" ,e);
        }
        GlobalBus.getBus().removeStickyEvent(itemAlbums);
    }
    public void isBuffering(@NonNull Boolean isBuffer) {
        if (isBuffer) {
            pbQueue.setVisibility(View.VISIBLE);
        } else {
            pbQueue.setVisibility(View.INVISIBLE);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBufferChange(@NonNull MessageEvent messageEvent) {
        if (messageEvent.message.equals("buffer")) {
            isBuffering(messageEvent.flag);
        }
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
}