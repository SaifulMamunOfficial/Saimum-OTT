package nemosofts.tamilaudiopro.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.theme.ColorUtils;
import androidx.nemosofts.material.BlurImage;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.palette.graphics.Palette;

import com.jetradarmobile.snowfall.SnowfallView;
import com.nemosofts.swipebutton.SwipeButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.executor.LoadFav;
import nemosofts.tamilaudiopro.interfaces.SuccessListener;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.MessageEvent;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;

public class DriveModeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DriveModeActivity";
    Helper helper;
    SPHelper spHelper;
    private TextView musicTitle;
    private ImageView blurBg;
    private final Handler seekHandler = new Handler(Looper.getMainLooper());
    private TextView currentTime;
    private TextView totalTime;
    private SeekBar seekBar;
    private ImageView shuffle;
    private ImageView previous;
    private ImageView next;
    private ImageView repeat;
    private ImageView play;
    private ImageView fav;
    private ProgressBar loading;

    @SuppressLint("SetTextI18n")
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
        hideNavigationBarStatusBars();

        helper = new Helper(this);
        spHelper = new SPHelper(this);

        if (Boolean.TRUE.equals(spHelper.isDriveKeepScreen())){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        musicTitle = findViewById(R.id.tv_title_drive_mode);
        blurBg = findViewById(R.id.iv_drive_mode);

        seekBar = findViewById(R.id.seekbar_music);
        currentTime = findViewById(R.id.tv_music_time);
        totalTime = findViewById(R.id.tv_music_total_time);

        currentTime.setText("00:00");
        totalTime.setText("00:00");

        loading = findViewById(R.id.pb_music_loading);
        play = findViewById(R.id.iv_music_play);
        shuffle = findViewById(R.id.iv_music_shuffle);
        previous = findViewById(R.id.iv_music_previous);
        next = findViewById(R.id.iv_music_next);
        repeat = findViewById(R.id.iv_music_repeat);
        fav = findViewById(R.id.iv_drive_mode_fav);

        play.setOnClickListener(this);
        shuffle.setOnClickListener(this);
        previous.setOnClickListener(this);
        next.setOnClickListener(this);
        repeat.setOnClickListener(this);
        fav.setOnClickListener(this);

        findViewById(R.id.iv_drive_mode_close).setOnClickListener(view -> finish());

        setSeekBarChangeListener();

        SwipeButton enableButton = findViewById(R.id.swipe_btn);
        enableButton.setOnStateChangeListener(active -> {
            if (active){
                finish();
            }
        });

        SnowfallView snowFall = findViewById(R.id.drive_snow_fall);
        if (Boolean.TRUE.equals(spHelper.isDriveSnowFall())){
            snowFall.restartFalling();
        } else {
            snowFall.stopFalling();
        }
        snowFall.setVisibility(Boolean.TRUE.equals(spHelper.isDriveSnowFall()) ? View.VISIBLE : View.GONE);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // this method is empty
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void setSeekBarChangeListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // this method is empty
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // this method is empty
            }

            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                try {
                    Intent intent = new Intent(DriveModeActivity.this, PlayerService.class);
                    intent.setAction(PlayerService.ACTION_SEEKTO);
                    intent.putExtra("seekto", ApplicationUtil.getSeekFromPercentage(progress, PlayerService.getInstance().getDuration()));
                    startService(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error in seek",e);
                }
            }
        });

    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_drive_mode;
    }

    @SuppressLint("SetTextI18n")
    public void changeText(ItemSong itemAudio) {
        if (itemAudio != null){
            musicTitle.setText(itemAudio.getTitle());
            changeFav(itemAudio.getIsFavourite());
            try {
                Target target = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        try {
                            int blurAmount = spHelper.getBlurAmountDrive();
                            blurBg.setImageBitmap(BlurImage.fastBlur(bitmap, 1f, blurAmount));
                        } catch (Exception e) {
                            Log.e(TAG, "Error in blur",e);
                        }
                    }
                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        blurBg.setImageResource(R.drawable.placeholder_song_night);
                    }
                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        // this method is empty
                    }
                };
                blurBg.setTag(target);
                if (Boolean.TRUE.equals(Callback.getIsOnline())) {
                    Picasso.get()
                            .load(itemAudio.getImageBig())
                            .placeholder(R.drawable.placeholder_song_night)
                            .into(target);
                } else {
                    Picasso.get()
                            .load(Uri.parse(itemAudio.getImageBig()))
                            .placeholder(R.drawable.placeholder_song_night)
                            .into(target);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in blur",e);
            }
            setColorText(itemAudio);
        }
    }

    private void setColorText(ItemSong itemAudio) {
        if (itemAudio == null){
            return;
        }
        if (Boolean.TRUE.equals(spHelper.isDriveColor())){
            String lodeURL = Boolean.TRUE.equals(Callback.getIsOnline()) ? itemAudio.getImageBig() : String.valueOf(Uri.parse(itemAudio.getImageBig()));
            Picasso.get()
                    .load(lodeURL)
                    .centerCrop()
                    .resize(100, 100)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            if (bitmap == null || isFinishing()){
                                return;
                            }
                            bitmapLoadedEnd(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            // this method is empty
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            // this method is empty
                        }
                    });
        }
    }

    private void bitmapLoadedEnd(Bitmap bitmap) {
        if (bitmap == null){
            return;
        }
        Palette.from(bitmap).generate(palette -> {
            if (palette == null){
                return;
            }
            Palette.Swatch textSwatch = palette.getVibrantSwatch();
            if (textSwatch == null) {
                return;
            }
            try {
                // findViewById(R.id.view_1).setBackgroundTintList(ColorStateList.valueOf(textSwatch.getRgb()));
                // findViewById(R.id.view_2).setBackgroundTintList(ColorStateList.valueOf(textSwatch.getRgb()));
                // These views are no longer present in the redesigned layout
            } catch (Exception e) {
                Log.e(TAG, "Error in color",e);
            }
        });
    }

    public void changeFav(Boolean isFav) {
        fav.setImageDrawable(ContextCompat.getDrawable(this, Boolean.TRUE.equals(isFav) ? R.drawable.ic_heart_fill : R.drawable.ic_heart_line));
        fav.setColorFilter(ColorUtils.colorWhite(this));
    }

    private final Runnable run = this::seekUpdating;

    @OptIn(markerClass = UnstableApi.class)
    public void seekUpdating() {
        try {
            seekBar.setProgress(ApplicationUtil.getProgressPercentage(PlayerService.getExoPlayer().getCurrentPosition(), PlayerService.getInstance().getDuration()));
            currentTime.setText(ApplicationUtil.milliSecondsToTimer(PlayerService.getExoPlayer().getCurrentPosition(), PlayerService.getInstance().getDuration()));
            totalTime.setText(ApplicationUtil.milliSecondsToTimer(PlayerService.getExoPlayer().getDuration(), PlayerService.getInstance().getDuration()));
            seekBar.setSecondaryProgress(PlayerService.getExoPlayer().getBufferedPercentage());
            if (PlayerService.getExoPlayer().getPlayWhenReady() && Boolean.TRUE.equals(Callback.getIsAppOpen())) {
                seekHandler.removeCallbacks(run);
                seekHandler.postDelayed(run, 1000);
            }
        } catch (Exception e) {
           Log.e(TAG, "Error in seek",e);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(@NonNull View view) {
        int id = view.getId();
        if (id == R.id.iv_music_play){
            playPause();
        } else if (id == R.id.iv_music_previous){
            previous();
        } else if (id == R.id.iv_music_next){
            next();
        } else if (id == R.id.iv_music_shuffle){
            setShuffle();
        } else if (id == R.id.iv_music_repeat){
            setRepeat();
        } else if (id == R.id.iv_drive_mode_fav){
            doFav();
        }
    }

    private void doFav() {
        if (NetworkUtils.isConnected(this)) {
            LoadFav loadFav = new LoadFav(new SuccessListener() {
                @Override
                public void onStart() {
                    changeFav(!Callback.getArrayListPlay().get(Callback.getPlayPos()).getIsFavourite());
                }

                @Override
                public void onEnd(String success, String favSuccess, String message) {
                    if (isFinishing()){
                        return;
                    }
                    if (success.equals("1")) {
                        if (favSuccess.equals("1")) {
                            Callback.getArrayListPlay().get(Callback.getPlayPos()).setIsFavourite(true);
                        } else if (favSuccess.equals("-2")) {
                            DialogUtil.verifyDialog(DriveModeActivity.this, getString(R.string.error_unauthorized_access), message, () -> {
                            });
                        } else {
                            Callback.getArrayListPlay().get(Callback.getPlayPos()).setIsFavourite(false);
                        }
                        changeFav(Callback.getArrayListPlay().get(Callback.getPlayPos()).getIsFavourite());
                        Toast.makeText(DriveModeActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DriveModeActivity.this, getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                    }
                }
            }, helper.getAPIRequest(Method.METHOD_FAV, 0,
                    Callback.getArrayListPlay().get(Callback.getPlayPos()).getId(), "", "",
                    "", spHelper.getUserId(), "", "", "", "",
                    "", "", "songs", null));
            loadFav.execute();
        } else {
            Toast.makeText(this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public void playPause() {
        if (!Callback.getArrayListPlay().isEmpty()) {
            Intent intent = new Intent(DriveModeActivity.this, PlayerService.class);
            if (Boolean.TRUE.equals(Callback.getIsPlayed())) {
                intent.setAction(PlayerService.ACTION_TOGGLE);
                startService(intent);
            } else {
                boolean isOnline = !Callback.getIsOnline() || NetworkUtils.isConnected(this);
                if (isOnline) {
                    intent.setAction(PlayerService.ACTION_PLAY);
                    startService(intent);
                } else {
                    Toast.makeText(DriveModeActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(DriveModeActivity.this, getString(R.string.error_no_songs_selected), Toast.LENGTH_SHORT).show();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public void next() {
        if (!Callback.getArrayListPlay().isEmpty()) {
            boolean isOnline = !Callback.getIsOnline() || NetworkUtils.isConnected(this);
            if (isOnline) {
                Intent intent = new Intent(DriveModeActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_NEXT);
                startService(intent);
            } else {
                Toast.makeText(DriveModeActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(DriveModeActivity.this, getString(R.string.error_no_songs_selected), Toast.LENGTH_SHORT).show();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public void previous() {
        if (!Callback.getArrayListPlay().isEmpty()) {
            boolean isOnline = !Callback.getIsOnline() || NetworkUtils.isConnected(this);
            if (isOnline) {
                Intent intent = new Intent(DriveModeActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_PREVIOUS);
                startService(intent);
            } else {
                Toast.makeText(DriveModeActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(DriveModeActivity.this, getString(R.string.error_no_songs_selected), Toast.LENGTH_SHORT).show();
        }
    }

    public void setRepeat() {
        if (Boolean.TRUE.equals(Callback.getIsRepeat())) {
            Callback.setIsRepeat(false);
            repeat.setImageDrawable(ContextCompat.getDrawable(DriveModeActivity.this, R.drawable.ic_repeat));
            repeat.setColorFilter(ColorUtils.colorWhite(this));
        } else {
            Callback.setIsRepeat(true);
            repeat.setImageDrawable(ContextCompat.getDrawable(DriveModeActivity.this, R.drawable.ic_repeat_one));
            repeat.setColorFilter(ContextCompat.getColor(DriveModeActivity.this, R.color.ns_classic_primary));
        }
    }

    public void setShuffle() {
        if (Boolean.TRUE.equals(Callback.getIsShuffle())) {
            Callback.setIsShuffle(false);
            shuffle.setColorFilter(ColorUtils.colorWhite(this));
        } else {
            Callback.setIsShuffle(true);
            shuffle.setColorFilter(ContextCompat.getColor(DriveModeActivity.this, R.color.ns_classic_primary));
        }
    }

    public void changePlayPauseIcon(Boolean isPlay) {
        if (Boolean.FALSE.equals(isPlay)) {
            play.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play));
        } else {
            play.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause));
        }
        seekUpdating();
    }

    public void isBuffering(Boolean isBuffer) {
        if (Boolean.FALSE.equals(isBuffer)) {
            play.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
            changePlayPauseIcon(true);
        } else {
            play.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }
        seekBar.setEnabled(Boolean.FALSE.equals(isBuffer));
        play.setEnabled(Boolean.FALSE.equals(isBuffer));
        next.setEnabled(Boolean.FALSE.equals(isBuffer));
        previous.setEnabled(Boolean.FALSE.equals(isBuffer));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSongChange(ItemSong itemSong) {
        changeText(itemSong);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBufferChange(@NonNull MessageEvent messageEvent) {
        if (messageEvent.message.equals("buffer")) {
            isBuffering(messageEvent.flag);
        } else {
            changePlayPauseIcon(messageEvent.flag);
        }
    }

    @Override
    public void onStart() {
        GlobalBus.getBus().register(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        GlobalBus.getBus().unregister(this);
        super.onStop();
    }

    @Override
    public void onPause() {
        seekHandler.removeCallbacks(run);
        super.onPause();
    }

    public void hideNavigationBarStatusBars() {
        try {
            Window window = getWindow();
            View decorView = window.getDecorView();

            // Allow layout to extend behind system bars
            WindowCompat.setDecorFitsSystemWindows(window, false);

            // Use AndroidX controller to manage insets across all versions
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(window, decorView);

            // Hide both status and navigation bars
            controller.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());

            // Enable immersive sticky (swipe to show system bars temporarily)
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        } catch (Exception e) {
            Log.e("LauncherActivity", "Failed to hide Navigation Bar & Status Bar", e);
        }
    }
}