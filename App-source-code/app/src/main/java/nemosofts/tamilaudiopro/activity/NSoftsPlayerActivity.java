package nemosofts.tamilaudiopro.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.BlurImage;
import androidx.nemosofts.material.ImageHelperView;
import androidx.nemosofts.material.PlayPauseButton;
import androidx.nemosofts.material.ToggleView;
import androidx.nemosofts.theme.ColorUtils;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.palette.graphics.Palette;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.jetradarmobile.snowfall.SnowfallView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.dialog.FeedBackDialog;
import nemosofts.tamilaudiopro.dialog.ReviewDialog;
import nemosofts.tamilaudiopro.executor.LoadFav;
import nemosofts.tamilaudiopro.interfaces.RewardAdListener;
import nemosofts.tamilaudiopro.interfaces.SuccessListener;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.LoadColor;
import nemosofts.tamilaudiopro.utils.MessageEvent;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;

public class NSoftsPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "NSoftsPlayerActivity";
    boolean isDarkMode;
    Helper helper;
    DBHelper dbHelper;
    SPHelper spHelper;
    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;

    AudioManager am;
    Boolean isExpand = false;
    SlidingUpPanelLayout mLayout;

    // MINI PLAYER ---------------------------------------------------------------------------------
    RelativeLayout minHeader;
    LinearLayout bottomNav;
    ToggleView navHome;
    ToggleView navCategories;
    ToggleView navArtist;
    ToggleView navAlbums;
    ToggleView navRecently;
    LinearLayout adViewPlayer;
    ImageView minPrevious;
    ImageView minPlay;
    ImageView minNext;
    ProgressBar pbMin;
    CircularProgressIndicator circularMin;
    ProgressBar progressBarMin;
    MaterialTextView minTitle;
    Handler seekHandler = new Handler(Looper.getMainLooper());
    TextView timer;

    // MAIN PLAYER ---------------------------------------------------------------------------------
    public ViewPager viewPager;
    ImagePagerAdapter imagePagerAdapter;
    ImageView volumeDown;
    ImageView volumeUp;
    RelativeLayout musicLoading;
    PlayPauseButton playPauseButton;
    SeekBar seekBarMusic;
    TextView currentTime;
    TextView totalTime;
    TextView songCount;
    ImageView musicShuffle;
    ImageView musicPrevious;
    ImageView musicNext;
    ImageView musicRepeat;
    MaterialTextView musicTitle;
    MaterialTextView musicArtist;
    RatingBar ratingBar;
    NestedScrollView playerLyricsView;
    WebView audioLyrics;
    AudioManager audioManager;
    SeekBar volumeSeekBar;
    Boolean isExpandVolume = true;
    // Option --------------------------------------------------------------------------------------
    ImageView optionEqualizer;
    ImageView optionAddToPlaylist;
    ImageView optionDownload;
    ImageView optionRate;
    ImageView optionFav;
    ImageView optionQueue;
    ImageView optionMenu;
    ImageView optionLyrics;
    View viewPlaylist;
    View viewDownload;
    View viewFav;
    View viewEqualizer;
    View viewRate;

    Map<Integer, Runnable> clickActions = new HashMap<>();

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        spHelper = new SPHelper(this);
        spHelper.getThemeDetails();
        super.onCreate(savedInstanceState);
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);

        EdgeToEdge.enable(this);
        // Window Insets padding for drawer layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Window Insets padding for background page
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sliding_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Callback.context = this;
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        isDarkMode = new ThemeEngine(this).getIsThemeMode();
        helper = new Helper(this);
        dbHelper = new DBHelper(this);

        toolbar = findViewById(R.id.toolbar_offline_music);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setToolbarNavigationClickListener(view -> drawer.openDrawer(GravityCompat.START));
        if (isDarkMode) {
            toggle.setHomeAsUpIndicator(R.drawable.ic_menu_white);
        } else {
            toggle.setHomeAsUpIndicator(R.drawable.ic_menu_black);
        }
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(false);

        //  MINI PLAYER
        bottomNav = findViewById(R.id.ll_bottom_nav);
        adViewPlayer = findViewById(R.id.ll_adView_player);
        adViewPlayer.setVisibility(View.GONE);

        navHome = findViewById(R.id.tv_nav_home);
        navHome.setBadgeText("");
        navCategories = findViewById(R.id.tv_nav_categories);
        navArtist = findViewById(R.id.tv_nav_artist);
        navAlbums = findViewById(R.id.tv_nav_albums);
        navRecently = findViewById(R.id.tv_nav_recently);

        mLayout = findViewById(R.id.sliding_layout);
        minHeader = findViewById(R.id.rl_min_header);
        minHeader.setOnClickListener(v -> {
        });
        findViewById(R.id.iv_open_player).setOnClickListener(v -> {
            if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });
        findViewById(R.id.iv_open_player_2).setOnClickListener(v -> {
            if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        progressBarMin = findViewById(R.id.progressBar_min);
        circularMin = findViewById(R.id.circular_min);
        minTitle = findViewById(R.id.tv_min_title);
        pbMin = findViewById(R.id.pb_min);
        minPrevious = findViewById(R.id.iv_min_previous);
        minPlay = findViewById(R.id.iv_min_play);
        minNext = findViewById(R.id.iv_min_next);

        minPlay.setOnClickListener(this);
        minNext.setOnClickListener(this);
        minPrevious.setOnClickListener(this);

        // MAIN PLAYER
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        findViewById(R.id.rl_c).setOnClickListener(v -> {
        });

        volumeDown = findViewById(R.id.volumeDown);
        volumeUp = findViewById(R.id.volumeUp);
        musicLoading = findViewById(R.id.rl_music_loading);
        playPauseButton = findViewById(R.id.iv_music_play);
        seekBarMusic = findViewById(R.id.seekbar_music);
        currentTime = findViewById(R.id.tv_music_time);
        totalTime = findViewById(R.id.tv_music_total_time);
        musicShuffle = findViewById(R.id.iv_music_shuffle);
        musicPrevious = findViewById(R.id.iv_music_previous);
        musicNext = findViewById(R.id.iv_music_next);
        musicRepeat = findViewById(R.id.iv_music_repeat);
        ratingBar = findViewById(R.id.rb_music);
        musicTitle = findViewById(R.id.tv_music_title);
        musicArtist = findViewById(R.id.tv_music_artist);
        songCount = findViewById(R.id.tv_music_song_count);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);

        optionEqualizer = findViewById(R.id.iv_player_equalizer);
        optionAddToPlaylist = findViewById(R.id.iv_player_add2playlist);
        optionDownload = findViewById(R.id.iv_player_download);
        optionRate = findViewById(R.id.iv_player_rate);
        optionFav = findViewById(R.id.iv_player_fav);
        optionQueue = findViewById(R.id.iv_player_queue);
        optionMenu = findViewById(R.id.iv_player_option);
        optionLyrics = findViewById(R.id.iv_player_lyrics);

        viewPlaylist = findViewById(R.id.view_player_playlist);
        viewDownload = findViewById(R.id.view_player_download);
        viewFav = findViewById(R.id.view_player_fav);
        viewRate = findViewById(R.id.view_player_rate);
        viewEqualizer = findViewById(R.id.view_player_equalizer);

        audioLyrics = findViewById(R.id.player_lyrics);
        playerLyricsView = findViewById(R.id.player_lyrics_nv);
        audioLyrics.setOnClickListener(view -> {
        });
        playerLyricsView.setOnClickListener(view -> {
        });

        currentTime.setText("00:00");
        totalTime.setText("00:00");

        playPauseButton.setOnClickListener(this);
        musicShuffle.setOnClickListener(this);
        musicPrevious.setOnClickListener(this);
        musicNext.setOnClickListener(this);
        musicRepeat.setOnClickListener(this);

        optionEqualizer.setOnClickListener(this);
        optionAddToPlaylist.setOnClickListener(this);
        optionDownload.setOnClickListener(this);
        optionRate.setOnClickListener(this);
        optionFav.setOnClickListener(this);
        optionQueue.setOnClickListener(this);
        optionMenu.setOnClickListener(this);
        optionLyrics.setOnClickListener(this);

        initClickActions();

        imagePagerAdapter = new ImagePagerAdapter();

        viewPager = findViewById(R.id.viewPager_song);
        viewPager.setOffscreenPageLimit(5);

        timer = findViewById(R.id.textView_timer);
        if (Boolean.TRUE.equals(spHelper.getIsSleepTimeOn())) {
            spHelper.setCheckSleepTime();
            updateTimer(spHelper.getSleepTime());
        }

        setVolumeSeekBar();
        setPanelSlideListener();
        setViewPagerChangeListener();
        setMusicSeekBar();
        snowFall();

        musicRepeat.setImageDrawable(ContextCompat.getDrawable(this, Boolean.TRUE.equals(Callback.getIsRepeat()) ? R.drawable.ic_repeat : R.drawable.ic_repeat_one));

        if (Boolean.FALSE.equals(Callback.getIsShuffle())) {
            if (Callback.getNowPlayingScreen() == 3 || Callback.getNowPlayingScreen() == 4){
                musicShuffle.setColorFilter(ColorUtils.colorWhite(NSoftsPlayerActivity.this));
            } else {
                musicShuffle.setColorFilter(ColorUtils.colorTitleSub(NSoftsPlayerActivity.this));
            }
        } else {
            musicShuffle.setColorFilter(ContextCompat.getColor(NSoftsPlayerActivity.this, R.color.ns_classic_primary));
        }

        if (Callback.getNowPlayingScreen() == 3 || Callback.getNowPlayingScreen() == 4){
            loadIconColor();
        }
    }

    private void setMusicSeekBar() {
        seekBarMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                    Intent intent = new Intent(NSoftsPlayerActivity.this, PlayerService.class);
                    intent.setAction(PlayerService.ACTION_SEEKTO);
                    intent.putExtra("seekto", ApplicationUtil.getSeekFromPercentage(progress, PlayerService.getInstance().getDuration()));
                    startService(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error in seekbar", e);
                }
            }
        });
    }

    private void setViewPagerChangeListener() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // this method is empty
            }

            @Override
            public void onPageSelected(int position) {
                changeTextPager(Callback.getArrayListPlay().get(position));
                View view = viewPager.findViewWithTag("myview" + position);
                if (view != null) {
                    ImageView iv = view.findViewById(R.id.iv_vp_play);
                    if (Callback.getPlayPos() == position) {
                        iv.setVisibility(View.GONE);
                    } else {
                        iv.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // this method is empty
            }
        });
    }

    private void snowFall() {
        SnowfallView snowFall = findViewById(R.id.view_snow_fall);
        if (isDarkMode || Callback.getNowPlayingScreen() == 3 || Callback.getNowPlayingScreen() == 4){
            if (Boolean.TRUE.equals(spHelper.isSnowFall())){
                snowFall.restartFalling();
            } else {
                snowFall.stopFalling();
            }
            snowFall.setVisibility(Boolean.TRUE.equals(spHelper.isSnowFall()) ? View.VISIBLE : View.GONE);
        } else {
            snowFall.stopFalling();
            snowFall.setVisibility(View.GONE);
        }
    }

    private void setPanelSlideListener() {
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset == 0.0f) {
                    isExpand = false;
                    minHeader.setVisibility(View.VISIBLE);
                    minHeader.setAlpha(1.0f); // Ensure it's fully visible.
                } else if (slideOffset < 1.0f) { // Implies slideOffset > 0.0f.
                    minHeader.setVisibility(View.VISIBLE);
                    minHeader.setAlpha(1.0f - slideOffset);
                } else { // Implies slideOffset == 1.0f.
                    isExpand = true;
                    minHeader.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    try {
                        if (viewPager.getAdapter() == null || Callback.getIsNewAdded()
                                || !Callback.getAddedFrom().equals(imagePagerAdapter.getIsLoadedFrom())) {
                            viewPager.setAdapter(imagePagerAdapter);
                        }
                        viewPager.setCurrentItem(Callback.getPlayPos());
                    } catch (Exception e) {
                        imagePagerAdapter.notifyDataSetChanged();
                        viewPager.setCurrentItem(Callback.getPlayPos());
                    }
                    animatorVolumeSeekBar();
                }
            }
        });
    }

    private void animatorVolumeSeekBar() {
        boolean isVolume = spHelper.isVolume() && volumeSeekBar.getVisibility() == View.VISIBLE
                && volumeSeekBar.getProgress() != audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (isVolume){
            ValueAnimator anim = ValueAnimator.ofInt(volumeSeekBar.getProgress(), audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            anim.setDuration(400);
            anim.addUpdateListener(animation -> {
                int animProgress = (Integer) animation.getAnimatedValue();
                volumeSeekBar.setProgress(animProgress);
            });
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animator) {
                    isExpandVolume = false;
                }

                @Override
                public void onAnimationEnd(@NonNull Animator animator) {
                    isExpandVolume = true;
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animator) {
                    isExpandVolume = true;
                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animator) {
                    // this method is empty
                }
            });
            anim.start();
        }
    }

    private void setVolumeSeekBar() {
        volumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeSeekBar.setProgress(volumeLevel);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (Boolean.TRUE.equals(isExpandVolume)){
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
                    if (volumeDown.getVisibility() == View.VISIBLE && i == 0){
                        volumeDown.setImageResource(R.drawable.ic_volume_mute);
                    } else {
                        volumeDown.setImageResource(R.drawable.ic_volume_down);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // this method is empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // this method is empty
            }
        });

        int visibility = Boolean.TRUE.equals(spHelper.isVolume()) ? View.VISIBLE : View.GONE;
        volumeSeekBar.setVisibility(visibility);
        volumeDown.setVisibility(visibility);
        volumeUp.setVisibility(visibility);
    }

    private void loadIconColor() {
        musicTitle.setTextColor(ColorUtils.colorWhite(this));
        musicArtist.setTextColor(ColorUtils.colorWhite(this));
        songCount.setTextColor(ColorUtils.colorWhite(this));
        currentTime.setTextColor(ColorUtils.colorWhite(this));
        totalTime.setTextColor(ColorUtils.colorWhite(this));
        seekBarMusic.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.colorWhite(this)));

        volumeDown.setColorFilter(ColorUtils.colorWhite(this));
        volumeUp.setColorFilter(ColorUtils.colorWhite(this));

        optionEqualizer.setColorFilter(ColorUtils.colorWhite(this));
        optionAddToPlaylist.setColorFilter(ColorUtils.colorWhite(this));
        optionDownload.setColorFilter(ColorUtils.colorWhite(this));
        optionRate.setColorFilter(ColorUtils.colorWhite(this));
        optionFav.setColorFilter(ColorUtils.colorWhite(this));
        optionQueue.setColorFilter(ColorUtils.colorWhite(this));
        optionMenu.setColorFilter(ColorUtils.colorWhite(this));
        optionLyrics.setColorFilter(ColorUtils.colorWhite(this));

        musicShuffle.setColorFilter(ColorUtils.colorWhite(this));
        musicPrevious.setColorFilter(ColorUtils.colorWhite(this));
        musicNext.setColorFilter(ColorUtils.colorWhite(this));
        musicRepeat.setColorFilter(ColorUtils.colorWhite(this));
    }

    private void changeTextPager(ItemSong itemSong) {
        if (itemSong == null) {
            return;
        }
        ratingBar.setRating(Integer.parseInt(itemSong.getAverageRating()));
        musicArtist.setText(itemSong.getArtist());
        musicTitle.setText(itemSong.getTitle());
        String count = (viewPager.getCurrentItem() + 1) + "/" + Callback.getArrayListPlay().size();
        songCount.setText(count);
    }


    public void changeText(ItemSong itemSong, final String page) {
        if (itemSong == null){
            return;
        }
        //  MINI PLAYER
        String subName;
        String artistName="";
        if (!itemSong.getArtist().isEmpty()) {
            artistName = itemSong.getArtist();
        }
        subName = itemSong.getTitle() +" • "+ artistName;
        minTitle.setText(subName);
        if (!minTitle.getText().toString().isEmpty()){
            minTitle.setSelected(true);
        }

        // MAIN PLAYER
        ratingBar.setRating(Integer.parseInt(itemSong.getAverageRating()));
        musicTitle.setText(itemSong.getTitle());
        musicArtist.setText(itemSong.getArtist());
        String count = Callback.getPlayPos() + 1 + "/" + Callback.getArrayListPlay().size();
        songCount.setText(count);
        changeFav(itemSong.getIsFavourite());

        if (Boolean.TRUE.equals(Callback.getIsOnline())) {
            if (ratingBar.getVisibility() == View.GONE) {
                ratingBar.setVisibility(View.VISIBLE);
                optionFav.setVisibility(View.VISIBLE);
                viewFav.setVisibility(View.VISIBLE);
                optionRate.setVisibility(View.GONE);
                viewRate.setVisibility(View.GONE);
                optionAddToPlaylist.setVisibility(View.VISIBLE);
            }
            optionDownload.setVisibility(Boolean.TRUE.equals(spHelper.getIsSongDownload()) ? View.VISIBLE : View.GONE);
            viewDownload.setVisibility(Boolean.TRUE.equals(spHelper.getIsSongDownload()) ? View.VISIBLE : View.GONE);
        } else {

            optionAddToPlaylist.setVisibility(Boolean.TRUE.equals(spHelper.getIsSongDownload()) ? View.GONE : View.VISIBLE);
            if (ratingBar.getVisibility() == View.VISIBLE) {
                ratingBar.setVisibility(View.GONE);
                optionFav.setVisibility(View.GONE);
                viewFav.setVisibility(View.GONE);
                optionRate.setVisibility(View.GONE);
                viewRate.setVisibility(View.GONE);
                optionDownload.setVisibility(View.GONE);
                viewDownload.setVisibility(View.GONE);
            }
        }

        optionEqualizer.setVisibility(Boolean.TRUE.equals(Callback.getIsOnline()) ? View.GONE : View.VISIBLE);
        viewEqualizer.setVisibility(Boolean.TRUE.equals(Callback.getIsOnline()) ? View.GONE : View.VISIBLE);

        if (Callback.getNowPlayingScreen() == 2){
            loadColorTitle(itemSong);
        }

        if (Callback.getNowPlayingScreen() == 3){
            ImageView playerBlur = findViewById(R.id.iv_bg_player_blur);
            playerBlur.setImageResource(R.drawable.shadow_up_now_play);
            new LoadColor(playerBlur).execute(itemSong.getImageBig());
        }

        if (Callback.getNowPlayingScreen() == 4){
            loadBgBlur(itemSong);
        }

        if (viewPager.getAdapter() == null || Callback.getIsNewAdded() || !Callback.getAddedFrom().equals(imagePagerAdapter.getIsLoadedFrom())) {
            viewPager.setAdapter(imagePagerAdapter);
            Callback.setIsNewAdded(false);
        }
        try {
            viewPager.setCurrentItem(Callback.getPlayPos());
        } catch (Exception e) {
            imagePagerAdapter.notifyDataSetChanged();
            viewPager.setCurrentItem(Callback.getPlayPos());
        }
    }

    private void loadBgBlur(ItemSong itemSong) {
        final  ImageView playerBlur = findViewById(R.id.iv_bg_player_blur);
        try {
            String lodeURL = Boolean.TRUE.equals(Callback.getIsOnline()) ? itemSong.getImageBig() : String.valueOf(Uri.parse(itemSong.getImageBig()));
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    try {
                        int blurAmount = spHelper.getBlurAmount();
                        int effectiveBlur = (blurAmount >= 0 && blurAmount < 6) ? 5 : blurAmount;
                        playerBlur.setImageBitmap(BlurImage.fastBlur(bitmap, 1f, effectiveBlur));
                        findViewById(R.id.iv_bg_blur).setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e(TAG, "Error in blur", e);
                    }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    handleLoadFailure();
                }
                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    // this method is empty
                }
            };
            playerBlur.setTag(target);
            Picasso.get()
                    .load(lodeURL)
                    .placeholder(isDarkMode ? R.drawable.placeholder_song_night : R.drawable.placeholder_song_light)
                    .into(target);
        } catch (Exception e) {
            handleLoadFailure();
        }
    }

    private void handleLoadFailure() {
        ImageView playerBlur = findViewById(R.id.iv_bg_player_blur);
        playerBlur.setImageResource(R.drawable.shadow_up_now_play);
        findViewById(R.id.iv_bg_blur).setVisibility(View.INVISIBLE);
    }

    private void loadColorTitle(ItemSong itemSong) {
        try {
            String lodeURL = Boolean.TRUE.equals(Callback.getIsOnline()) ? itemSong.getImageBig() : String.valueOf(Uri.parse(itemSong.getImageBig()));
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
        } catch (Exception e) {
            Log.e(TAG, "Error in color Title", e);
        }
    }

    private void bitmapLoadedEnd(Bitmap bitmap) {
        if (bitmap == null){
            return;
        }
        try {
            Palette.from(bitmap).generate(palette -> {
                if (palette == null){
                    return;
                }

                Palette.Swatch textSwatch = palette.getVibrantSwatch();
                if (textSwatch == null){
                    return;
                }

                try {
                    musicTitle.setBackgroundTintList(ColorStateList.valueOf(textSwatch.getRgb()));
                    musicArtist.setBackgroundTintList(ColorStateList.valueOf(textSwatch.getRgb()));
                    musicTitle.setTextColor(ColorUtils.colorWhite(NSoftsPlayerActivity.this));
                    musicArtist.setTextColor(ColorUtils.colorWhite(NSoftsPlayerActivity.this));
                } catch (Exception e) {
                    Log.e(TAG, "Error in color", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in color", e);
        }
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private final LayoutInflater inflater;
        private String loadedPage = "";

        private ImagePagerAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return Callback.getArrayListPlay().size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }

        String getIsLoadedFrom() {
            return loadedPage;
        }

        @OptIn(markerClass = UnstableApi.class)
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View imageLayout = getInflaterLayoutID(container, inflater);

            boolean isTextColor = Callback.getNowPlayingScreen() != 1;

            ImageHelperView imageView = imageLayout.findViewById(R.id.image);
            final ImageView imageViewPlay = imageLayout.findViewById(R.id.iv_vp_play);
            final ProgressBar spinner = imageLayout.findViewById(R.id.loading);
            final RelativeLayout rlText = imageLayout.findViewById(R.id.rl_text);

            loadedPage = Callback.getAddedFrom();

            if (Callback.getPlayPos() == position) {
                imageViewPlay.setVisibility(View.GONE);
            }

            if (Boolean.TRUE.equals(Callback.getIsOnline())) {
                Picasso.get()
                        .load(Callback.getArrayListPlay().get(position).getImageBig())
                        .placeholder(isDarkMode ? R.drawable.placeholder_song_night : R.drawable.placeholder_song_light)
                        .into(imageView, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                spinner.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                spinner.setVisibility(View.GONE);
                            }
                        });

                if (isTextColor) {
                    Picasso.get()
                            .load(Callback.getArrayListPlay().get(position).getImageBig())
                            .centerCrop()
                            .resize(100, 100)
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    if (bitmap == null){
                                        return;
                                    }
                                    try {
                                        Palette.from(bitmap).generate(palette -> {
                                            if (palette == null){
                                                return;
                                            }
                                            Palette.Swatch textSwatch = palette.getVibrantSwatch();
                                            if (textSwatch == null){
                                                return;
                                            }
                                            rlText.setBackgroundTintList(ColorStateList.valueOf(textSwatch.getRgb()));
                                        });
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error in color ImagePagerAdapter", e);
                                    }
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
            } else {
                Picasso.get()
                        .load(Uri.parse(Callback.getArrayListPlay().get(position).getImageBig()))
                        .placeholder(isDarkMode ? R.drawable.placeholder_song_night : R.drawable.placeholder_song_light)
                        .into(imageView);

                if (isTextColor) {
                    Picasso.get()
                            .load(Uri.parse(Callback.getArrayListPlay().get(position).getImageBig()))
                            .centerCrop()
                            .resize(100, 100)
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    if (bitmap == null){
                                        return;
                                    }
                                    try {
                                        Palette.from(bitmap).generate(palette -> {
                                            if (palette == null){
                                                return;
                                            }
                                            Palette.Swatch textSwatch = palette.getVibrantSwatch();
                                            if (textSwatch == null) {
                                                return;
                                            }
                                            rlText.setBackgroundTintList(ColorStateList.valueOf(textSwatch.getRgb()));
                                        });
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error in Background ImagePagerAdapter", e);
                                    }
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

                spinner.setVisibility(View.GONE);
            }

            imageViewPlay.setOnClickListener(v -> {
                Callback.setPlayPos(viewPager.getCurrentItem());
                boolean online = !Callback.getIsOnline() || NetworkUtils.isConnected(NSoftsPlayerActivity.this);
                if (online) {
                    Intent intent = new Intent(NSoftsPlayerActivity.this, PlayerService.class);
                    intent.setAction(PlayerService.ACTION_PLAY);
                    startService(intent);
                    imageViewPlay.setVisibility(View.GONE);
                } else {
                    Toast.makeText(NSoftsPlayerActivity.this, getResources().getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
                }
            });

            imageLayout.setTag("myview" + position);
            container.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    public View getInflaterLayoutID(ViewGroup container, LayoutInflater inflater) {
        switch (Callback.getNowPlayingScreen()) {
            case 1 -> {
                return inflater.inflate(R.layout.row_viewpager_player_circle, container, false);
            }
            case 2 -> {
                return inflater.inflate(R.layout.row_viewpager_player_flat, container, false);
            }
            case 5 -> {
                return inflater.inflate(R.layout.row_viewpager_player_corner, container, false);
            }
            case 6 -> {
                return inflater.inflate(R.layout.row_viewpager_player_corner_bottom, container, false);
            }
            default -> {
                return inflater.inflate(R.layout.row_viewpager_player_normal, container, false);
            }
        }
    }

    private final Runnable run = this::seekUpdating;

    @OptIn(markerClass = UnstableApi.class)
    public void seekUpdating() {
        try {
            circularMin.setProgress(ApplicationUtil.getProgressPercentage(PlayerService.getExoPlayer().getCurrentPosition(), PlayerService.getInstance().getDuration()));
            progressBarMin.setProgress(ApplicationUtil.getProgressPercentage(PlayerService.getExoPlayer().getCurrentPosition(), PlayerService.getInstance().getDuration()));

            seekBarMusic.setProgress(ApplicationUtil.getProgressPercentage(PlayerService.getExoPlayer().getCurrentPosition(), PlayerService.getInstance().getDuration()));
            currentTime.setText(ApplicationUtil.milliSecondsToTimer(PlayerService.getExoPlayer().getCurrentPosition(), PlayerService.getInstance().getDuration()));
            totalTime.setText(ApplicationUtil.milliSecondsToTimer(PlayerService.getExoPlayer().getDuration(), PlayerService.getInstance().getDuration()));
            seekBarMusic.setSecondaryProgress(PlayerService.getExoPlayer().getBufferedPercentage());

            if (PlayerService.getExoPlayer().getPlayWhenReady() && Boolean.TRUE.equals(Callback.getIsAppOpen())) {
                seekHandler.removeCallbacks(run);
                seekHandler.postDelayed(run, 1000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in seekUpdating", e);
        }
    }

    @Override
    public int setContentViewID() {
        if (Callback.getNowPlayingScreen() == 2) {
            return R.layout.activity_base_flat;
        }
        return R.layout.activity_base_normal;
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initClickActions() {
        clickActions.put(R.id.iv_min_play, this::playPause);
        clickActions.put(R.id.iv_music_play, this::playPause);
        clickActions.put(R.id.iv_min_next, () -> handlePlaybackAction(PlayerService.ACTION_NEXT));
        clickActions.put(R.id.iv_music_next, () -> handlePlaybackAction(PlayerService.ACTION_NEXT));
        clickActions.put(R.id.iv_min_previous, () -> handlePlaybackAction(PlayerService.ACTION_PREVIOUS));
        clickActions.put(R.id.iv_music_previous, () -> handlePlaybackAction(PlayerService.ACTION_PREVIOUS));
        clickActions.put(R.id.iv_music_shuffle, this::setShuffle);
        clickActions.put(R.id.iv_music_repeat, this::setRepeat);
        clickActions.put(R.id.iv_player_equalizer, this::openEqualizer);
        clickActions.put(R.id.iv_player_add2playlist, this::openAddPlaylist);
        clickActions.put(R.id.iv_player_download, this::downloadAudio);
        clickActions.put(R.id.iv_player_rate, this::showRateDialog);
        clickActions.put(R.id.iv_player_fav, this::doFav);
        clickActions.put(R.id.iv_player_queue, this::openQueue);
        clickActions.put(R.id.iv_player_lyrics, this::openLyrics);
        clickActions.put(R.id.iv_player_option, this::openOptionPopUp);
    }

    @OptIn(markerClass = UnstableApi.class)
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        if (view == null){
            return;
        }
        Runnable action = clickActions.get(view.getId());
        if (action != null) {
            action.run();
        }
    }

    private void doFav() {
        if (spHelper.isLogged()) {
            if (isArrayListEmpty() && Boolean.TRUE.equals(Callback.getIsOnline())) {
                loadFav(Callback.getArrayListPlay().get(Callback.getPlayPos()).getId(), Callback.getPlayPos());
            }
        } else {
            helper.clickLogin();
        }
    }

    private void loadFav(String id, final int playPos) {
        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        LoadFav loadFav = new LoadFav(new SuccessListener() {
            @Override
            public void onStart() {
                changeFav(!Callback.getArrayListPlay().get(playPos).getIsFavourite());
            }

            @Override
            public void onEnd(String success, String favSuccess, String message) {
                if (isFinishing()){
                    return;
                }
                if (success.equals("1")) {
                    Callback.getArrayListPlay().get(playPos).setIsFavourite(favSuccess.equals("1"));
                    changeFav(Callback.getArrayListPlay().get(playPos).getIsFavourite());
                    Toast.makeText(NSoftsPlayerActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NSoftsPlayerActivity.this, getResources().getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                }
            }
        }, helper.getAPIRequest(Method.METHOD_FAV, 0, id, "", "",
                "", spHelper.getUserId(), "", "", "", "",
                "", "", "songs", null));
        loadFav.execute();
    }

    private void openQueue() {
        if (isArrayListEmpty()) {
            startActivity(new Intent(NSoftsPlayerActivity.this, QueueActivity.class));
        }
    }

    private void downloadAudio() {
        if (!checkPerDownload()) {
            checkPerDownload();
            return;
        }
        if (isArrayListEmpty()) {
            try{
                if (spHelper.getRewardCredit() != 0){
                    spHelper.useRewardCredit(1);
                    Toast.makeText(NSoftsPlayerActivity.this, "Your Total Credit ("+spHelper.getRewardCredit()+")", Toast.LENGTH_SHORT).show();
                    helper.download(Callback.getArrayListPlay().get(viewPager.getCurrentItem()));
                } else {
                    helper.showRewardAds(viewPager.getCurrentItem(), new RewardAdListener() {
                        @Override
                        public void onClick(boolean isLoad, int pos) {
                            if (isLoad){
                                spHelper.addRewardCredit(Callback.getRewardCredit());
                                spHelper.useRewardCredit(1);
                                Toast.makeText(NSoftsPlayerActivity.this, "Your Total Credit ("+spHelper.getRewardCredit()+")", Toast.LENGTH_SHORT).show();
                                helper.download(Callback.getArrayListPlay().get(pos));
                            } else {
                                Toast.makeText(NSoftsPlayerActivity.this, "Display Failed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPurchases(int pos) {
                            helper.download(Callback.getArrayListPlay().get(viewPager.getCurrentItem()));
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in downloadAudio", e);
            }
        }
    }

    private void openAddPlaylist() {
        if (isArrayListEmpty()) {
            helper.openPlaylists(Callback.getArrayListPlay().get(viewPager.getCurrentItem()), Callback.getIsOnline());
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openEqualizer() {
        if(PlayerService.getInstance() != null && Callback.getIsPlayed()) {
            Intent intent = new Intent(NSoftsPlayerActivity.this, EqualizerActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(NSoftsPlayerActivity.this, "play a song", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRateDialog() {
        if (isArrayListEmpty()) {
            ReviewDialog reviewDialog = new ReviewDialog(this, new ReviewDialog.RatingDialogListener() {
                @Override
                public void onShow() {
                    // this method is empty
                }

                @Override
                public void onGetRating(String rating, String message) {
                    Callback.getArrayListPlay().get(viewPager.getCurrentItem()).setUserRating(String.valueOf(rating));
                    Callback.getArrayListPlay().get(viewPager.getCurrentItem()).setUserMessage(message);
                }

                @Override
                public void onDismiss(String success, String rateSuccess,
                                      String message, int rating, String userRating, String userMessage) {
                    if (isFinishing()){
                        return;
                    }
                    if (!success.equals("1")) {
                        Toast.makeText(NSoftsPlayerActivity.this, getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (rateSuccess.equals("1")) {
                        try {
                            Callback.getArrayListPlay().get(viewPager.getCurrentItem()).setAverageRating(String.valueOf(rating));
                            Callback.getArrayListPlay().get(viewPager.getCurrentItem()).setUserRating(String.valueOf(userRating));
                            Callback.getArrayListPlay().get(viewPager.getCurrentItem()).setUserMessage(String.valueOf(userMessage));
                            ratingBar.setRating(rating);
                        } catch (Exception e) {
                            Log.e(TAG, "Error in showRateDialog", e);
                        }
                    }
                    Toast.makeText(NSoftsPlayerActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
            reviewDialog.showDialog(
                    Callback.getArrayListPlay().get(viewPager.getCurrentItem()).getId(),
                    Callback.getArrayListPlay().get(viewPager.getCurrentItem()).getUserRating(),
                    Callback.getArrayListPlay().get(viewPager.getCurrentItem()).getUserMessage()
            );
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void openLyrics() {
        if (isArrayListEmpty()) {
            try {
                Transition transition = new Fade();
                transition.setDuration(500);
                TransitionManager.beginDelayedTransition(viewPager, transition);
                TransitionManager.beginDelayedTransition(playerLyricsView, transition);
            } catch (Exception e) {
                Log.e(TAG, "Error in openLyrics", e);
            }

            if (viewPager.getVisibility() == View.VISIBLE){
                viewPager.setVisibility(View.INVISIBLE);
                playerLyricsView.setVisibility(View.VISIBLE);
            } else {
                playerLyricsView.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
            }

            audioLyrics.getSettings().setJavaScriptEnabled(true);
            String htmlText = Callback.getArrayListPlay().get(Callback.getPlayPos()).getDescription();

            String htmlString = getHtmlString(htmlText);
            audioLyrics.setScrollbarFadingEnabled(true);
            audioLyrics.setBackgroundColor(Color.TRANSPARENT);
            audioLyrics.loadDataWithBaseURL("", htmlString, "text/html", "utf-8", null);
        }
    }

    @NonNull
    private String getHtmlString(@NonNull String htmlText) {
        // Constants for reusable strings
        final String BODY_STYLE_TEMPLATE = "<style>body{color:%s !important; text-align:center; font-weight:bold;}</style>";
        final String CSS_STYLE_TEMPLATE = "<style type=\"text/css\">%s</style>";
        final String CONTENT_WRAPPER = "<div>%s</div>";
        final String RTL_TEMPLATE = "<html dir=\"rtl\" lang=\"\"><body>%s%s</body></html>";

        // Determine styling
        boolean needsDarkStyle = (Callback.getNowPlayingScreen() == 4) || isDarkMode;
        String textColor = needsDarkStyle ? "#fff" : "#000";
        String textSize = getTextSize();

        // Build style components
        String bodyStyle = String.format(BODY_STYLE_TEMPLATE, textColor);
        String cssStyle = String.format(CSS_STYLE_TEMPLATE, textSize);
        String wrappedContent = String.format(CONTENT_WRAPPER, htmlText);

        // Combine components
        String styledContent = bodyStyle + cssStyle + wrappedContent;

        // Handle RTL if needed
        return Boolean.TRUE.equals(spHelper.getIsRTL())
                ? String.format(RTL_TEMPLATE, bodyStyle + cssStyle, wrappedContent)
                : styledContent;
    }

    @NonNull
    private String getTextSize() {
        String textSize;
        int textData = spHelper.getTextSize();
        if (1 == textData){
            textSize = "body{font-size:13px;}";
        } else if (2 == textData){
            textSize = "body{font-size:15px;}";
        } else if (3 == textData){
            textSize = "body{font-size:17px;}";
        } else if (4 == textData){
            textSize = "body{font-size:19px;}";
        } else if (5 == textData){
            textSize = "body{font-size:20px;}";
        } else {
            textSize = "body{font-size:12px;}";
        }
        return textSize;
    }

    @OptIn(markerClass = UnstableApi.class)
    private void openOptionPopUp() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.bottom_sheet_player, null);
        BottomSheetDialog dialog = new BottomSheetDialog(NSoftsPlayerActivity.this);
        dialog.setContentView(view);

        LinearLayout driveMode = dialog.findViewById(R.id.ll_sheet_drive_mode);
        LinearLayout equalizer = dialog.findViewById(R.id.ll_sheet_equalizer);
        LinearLayout report = dialog.findViewById(R.id.ll_sheet_report);
        LinearLayout youtube = dialog.findViewById(R.id.ll_sheet_youtube);
        LinearLayout share = dialog.findViewById(R.id.ll_sheet_share);
        LinearLayout comment = dialog.findViewById(R.id.ll_sheet_comment);
        LinearLayout timerView = dialog.findViewById(R.id.ll_sheet_timer);

        TextView tvStartTimer = dialog.findViewById(R.id.tv_start_timer);
        if (Boolean.FALSE.equals(new SPHelper(this).getIsSleepTimeOn())) {
            Objects.requireNonNull(tvStartTimer).setText(R.string.start_timer);
        } else {
            Objects.requireNonNull(tvStartTimer).setText(R.string.cancel_timer);
        }

        if (Boolean.FALSE.equals(Callback.getIsOnline())) {
            Objects.requireNonNull(report).setVisibility(View.GONE);
            Objects.requireNonNull(comment).setVisibility(View.GONE);
        }
        if (!helper.isYoutubeAppInstalled()) {
            Objects.requireNonNull(youtube).setVisibility(View.GONE);
        }

        Objects.requireNonNull(driveMode).setOnClickListener(v -> {
            dialog.dismiss();
            if (isArrayListEmpty()) {
                startActivity(new Intent(NSoftsPlayerActivity.this, DriveModeActivity.class));
            }
        });
        Objects.requireNonNull(equalizer).setOnClickListener(v -> {
            dialog.dismiss();
            if(PlayerService.getInstance() != null && Callback.getIsPlayed()) {
                startActivity(new Intent(NSoftsPlayerActivity.this, EqualizerActivity.class));
            } else {
                Toast.makeText(NSoftsPlayerActivity.this, "play a song", Toast.LENGTH_SHORT).show();
            }
        });
        Objects.requireNonNull(report).setOnClickListener(v -> {
            dialog.dismiss();
            if (isArrayListEmpty()) {
                new FeedBackDialog(this).showDialog(
                        Callback.getArrayListPlay().get(viewPager.getCurrentItem()).getId(),
                        Callback.getArrayListPlay().get(viewPager.getCurrentItem()).getTitle()
                );
            }
        });
        Objects.requireNonNull(youtube).setOnClickListener(v -> {
            dialog.dismiss();
            openYoutube();
        });
        Objects.requireNonNull(share).setOnClickListener(v -> {
            dialog.dismiss();
            shareSong();
        });
        Objects.requireNonNull(comment).setOnClickListener(v -> {
            dialog.dismiss();
            openCommentActivity();
        });
        Objects.requireNonNull(timerView).setOnClickListener(v -> {
            dialog.dismiss();
            if (Boolean.FALSE.equals(new SPHelper(this).getIsSleepTimeOn())) {
                setStartTimer();
            } else {
                setCancelTimer();
            }
        });
        dialog.show();
    }

    private void openYoutube() {
        if (isArrayListEmpty()) {
            Intent intent = new Intent(Intent.ACTION_SEARCH);
            intent.setPackage("com.google.android.youtube");
            intent.putExtra("query", Callback.getArrayListPlay().get(viewPager.getCurrentItem()).getTitle());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void openCommentActivity() {
        if (isArrayListEmpty()) {
            Intent intentComment = new Intent(NSoftsPlayerActivity.this, CommentActivity.class);
            intentComment.putExtra("current_item", viewPager.getCurrentItem());
            intentComment.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentComment);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setStartTimer() {
        DialogUtil.timerStartDialog(this, (hours, minute) -> {
            String time = hours + ":" + minute;
            long totalTimer = ApplicationUtil.convertLong(time) + System.currentTimeMillis();

            int id = ApplicationUtil.getRandomValue(100);

            spHelper.setSleepTime(true, totalTimer, id);

            Intent intent = new Intent(this, PlayerService.class);
            intent.setAction(PlayerService.ACTION_START_TIMER);
            startService(intent);

            updateTimer(totalTimer);
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setCancelTimer() {
        DialogUtil.timerCancelDialog(this, () -> {
            timer.setVisibility(View.GONE);
            spHelper.setSleepTime(false, 0, 0);
            Intent intent = new Intent(this, PlayerService.class);
            intent.setAction(PlayerService.ACTION_STOP_TIMER);
            startService(intent);
            Toast.makeText(NSoftsPlayerActivity.this, getString(R.string.stop_timer), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateTimer(long totalTimer) {
        long timeLeft = totalTimer - System.currentTimeMillis();
        if (timeLeft > 0) {

            @SuppressLint("DefaultLocale")
            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timeLeft),
                    TimeUnit.MILLISECONDS.toMinutes(timeLeft) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(timeLeft) % TimeUnit.MINUTES.toSeconds(1));
            timer.setVisibility(View.VISIBLE);
            timer.setText(hms);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (Boolean.TRUE.equals(spHelper.getIsSleepTimeOn())) {
                    updateTimer(totalTimer);
                } else {
                    timer.setVisibility(View.GONE);
                }
            }, 1000);
        } else {
            timer.setVisibility(View.GONE);
        }
    }

    private void shareSong() {
        if (isArrayListEmpty()) {
            if (Callback.getIsOnline() || Callback.getIsDownloaded()) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_song));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.listening)
                        + " - "
                        + Callback.getArrayListPlay().get(viewPager.getCurrentItem()).getTitle()
                        + "\n\nvia "
                        + getResources().getString(R.string.app_name)
                        + " - http://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_song)));
            } else {
                if (checkPer()) {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("audio/mp3");
                    share.putExtra(Intent.EXTRA_STREAM, Uri.parse(Callback.getArrayListPlay().get(viewPager.getCurrentItem()).getUrl()));
                    share.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.listening)
                            + " - "
                            + Callback.getArrayListPlay().get(viewPager.getCurrentItem()).getTitle()
                            + "\n\nvia "
                            + getResources().getString(R.string.app_name)
                            + " - http://play.google.com/store/apps/details?id=" + getPackageName());
                    startActivity(Intent.createChooser(share, getResources().getString(R.string.share_song)));
                }
            }
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public void playPause() {
        if (isArrayListEmpty()) {
            Intent intent = new Intent(NSoftsPlayerActivity.this, PlayerService.class);
            if (Boolean.TRUE.equals(Callback.getIsPlayed())) {
                intent.setAction(PlayerService.ACTION_TOGGLE);
                startService(intent);
            } else {
                boolean online = !Callback.getIsOnline() || NetworkUtils.isConnected(this);
                if (online) {
                    intent.setAction(PlayerService.ACTION_PLAY);
                    startService(intent);
                } else {
                    Toast.makeText(NSoftsPlayerActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void handlePlaybackAction(String action) {
        if (isArrayListEmpty()) {
            boolean online = !Callback.getIsOnline() || NetworkUtils.isConnected(this);
            if (online) {
                Intent intent = new Intent(NSoftsPlayerActivity.this, PlayerService.class);
                intent.setAction(action);
                startService(intent);
            } else {
                Toast.makeText(NSoftsPlayerActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isArrayListEmpty() {
        if (!Callback.getArrayListPlay().isEmpty()) {
            return true;
        } else {
            Toast.makeText(NSoftsPlayerActivity.this, getString(R.string.error_no_songs_selected), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void changeFav(Boolean isFav) {
        optionFav.setImageDrawable(ContextCompat.getDrawable(this, Boolean.TRUE.equals(isFav) ? R.drawable.ic_heart_fill : R.drawable.ic_heart_line));
        if (Callback.getNowPlayingScreen() == 3 || Callback.getNowPlayingScreen() == 4){
            optionFav.setColorFilter(ColorUtils.colorWhite(NSoftsPlayerActivity.this));
        } else {
            optionFav.setColorFilter(ColorUtils.colorTitle(NSoftsPlayerActivity.this));
        }
    }

    public void setRepeat() {
        musicRepeat.setImageDrawable(ContextCompat.getDrawable(this, Boolean.TRUE.equals(Callback.getIsRepeat()) ? R.drawable.ic_repeat : R.drawable.ic_repeat_one));
        Callback.setIsRepeat(!Callback.getIsRepeat());
    }

    public void setShuffle() {
        if (Boolean.TRUE.equals(Callback.getIsShuffle())) {
            Callback.setIsShuffle(false);
            if (Callback.getNowPlayingScreen() == 3 || Callback.getNowPlayingScreen() == 4){
                musicShuffle.setColorFilter(ColorUtils.colorWhite(NSoftsPlayerActivity.this));
            } else {
                musicShuffle.setColorFilter(ColorUtils.colorTitleSub(NSoftsPlayerActivity.this));
            }
        } else {
            Callback.setIsShuffle(true);
            musicShuffle.setColorFilter(ContextCompat.getColor(NSoftsPlayerActivity.this, R.color.ns_classic_primary));
        }
    }

    public void changePlayPauseIcon(Boolean isPlay) {
        minPlay.setImageDrawable(ContextCompat.getDrawable(this, Boolean.FALSE.equals(isPlay) ? R.drawable.ic_play : R.drawable.ic_pause));
        if (!musicTitle.getText().toString().isEmpty()){
            musicTitle.setSelected(Boolean.TRUE.equals(isPlay));
        }
        if (!musicArtist.getText().toString().isEmpty()){
            musicArtist.setSelected(Boolean.TRUE.equals(isPlay));
        }
        playPauseButton.change(!isPlay);
        seekUpdating();
    }

    public void isBuffering(Boolean isBuffer) {
        if (Boolean.FALSE.equals(isBuffer)) {
            playPauseButton.setVisibility(View.VISIBLE);
            musicLoading.setVisibility(View.INVISIBLE);
            pbMin.setVisibility(View.INVISIBLE);
            circularMin.setVisibility(View.VISIBLE);
            changePlayPauseIcon(true);
        } else {
            playPauseButton.setVisibility(View.INVISIBLE);
            musicLoading.setVisibility(View.VISIBLE);
            pbMin.setVisibility(View.VISIBLE);
            circularMin.setVisibility(View.INVISIBLE);
        }

        if (isBuffer != null){
            minNext.setEnabled(!isBuffer);
            minPrevious.setEnabled(!isBuffer);

            musicNext.setEnabled(!isBuffer);
            musicPrevious.setEnabled(!isBuffer);
            optionDownload.setEnabled(!isBuffer);
            seekBarMusic.setEnabled(!isBuffer);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSongChange(ItemSong itemSong) {
        changeText(itemSong, "home");
        Callback.context = NSoftsPlayerActivity.this;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBufferChange(@NonNull MessageEvent messageEvent) {
        if (messageEvent.message.equals("buffer")) {
            isBuffering(messageEvent.flag);
        } else {
            changePlayPauseIcon(messageEvent.flag);
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onViewPagerChanged(ItemMyPlayList itemMyPlayList) {
        imagePagerAdapter.notifyDataSetChanged();
        songCount.setText(Callback.getPlayPos() + 1 + "/" + Callback.getArrayListPlay().size());
        GlobalBus.getBus().removeStickyEvent(itemMyPlayList);
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
    protected void onPause() {
        try {
            seekHandler.removeCallbacks(run);
        } catch (Exception e) {
            Log.e(TAG, "Error in onPause", e);
        }
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @NonNull
    private Boolean checkPermission(@NonNull String permission, @NonNull ActivityResultLauncher<String> launcher) {
        if (ContextCompat.checkSelfPermission(NSoftsPlayerActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(permission); // Request permission using the new API
            return false;
        }
        return true;
    }

    // Define a single launcher for reuse
    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted ->
                    Toast.makeText(NSoftsPlayerActivity.this,
                            Boolean.TRUE.equals(isGranted) ? "Permission granted"
                                    : getResources().getString(R.string.error_cannot_use_features),
                            Toast.LENGTH_SHORT).show()
    );

    // Usage for download permission
    @NonNull
    private Boolean checkPerDownload() {
        String permission;
        if (Build.VERSION.SDK_INT >= 33) {
            permission = READ_MEDIA_AUDIO;
        } else if (Build.VERSION.SDK_INT >= 29) {
            permission = READ_EXTERNAL_STORAGE;
        } else {
            permission = WRITE_EXTERNAL_STORAGE;
        }
        return checkPermission(permission, permissionLauncher);
    }

    // Usage for phone state permission
    @NonNull
    private Boolean checkPer() {
        return checkPermission(READ_PHONE_STATE, permissionLauncher);
    }
}