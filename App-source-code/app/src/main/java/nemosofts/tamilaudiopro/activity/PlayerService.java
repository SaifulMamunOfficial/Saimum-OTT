package nemosofts.tamilaudiopro.activity;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.ui.PlayerNotificationManager;
import androidx.nemosofts.utils.NetworkUtils;

import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import com.saimum.saimummusic.BuildConfig;
import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.item.ItemAlbums;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
import nemosofts.tamilaudiopro.utils.GlobalBus;
import nemosofts.tamilaudiopro.utils.MessageEvent;
import nemosofts.tamilaudiopro.utils.encrypt.EncryptedFileDataSourceFactory;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;
import nemosofts.tamilaudiopro.utils.receiver.MediaButtonIntentReceiver;

@UnstableApi
public class PlayerService extends IntentService implements Player.Listener {

    private static final String TAG = "PlayerService";
    public static final String ACTION_TOGGLE = "action.ACTION_TOGGLE";
    public static final String ACTION_PLAY = "action.ACTION_PLAY";
    public static final String ACTION_NEXT = "action.ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "action.ACTION_PREVIOUS";
    public static final String ACTION_STOP = "action.ACTION_STOP";
    public static final String ACTION_SEEKTO = "action.ACTION_SEEKTO";

    public static final String ACTION_START_TIMER = "action.ACTION_START_TIMER";
    public static final String ACTION_STOP_TIMER = "action.ACTION_STOP_TIMER";

    public static ExoPlayer exoPlayer = null;
    MediaSessionCompat mMediaSession;
    DefaultBandwidthMeter bandwidthMeter;
    DataSource.Factory dataSourceFactory;
    static PlayerService playerService;
    Helper helper;
    SPHelper spHelper;
    DBHelper dbHelper;
    Boolean isNewSong = false;
    Bitmap bitmap;
    ComponentName componentName;
    AudioManager mAudioManager;
    PowerManager.WakeLock mWakeLock;

    Cipher mCipher = null;
    SecretKeySpec secretKeySpec;
    byte[] secretKey = BuildConfig.ENC_KEY.getBytes();
    byte[] initialIv = BuildConfig.IV.getBytes();

    PlayerNotificationManager notificationManager;
    NotificationReceiver notificationReceiver;
    PlayerNotificationManager.BitmapCallback callbackBitmap;

    public PlayerService() {
        super(null);
    }

    public static PlayerService getInstance() {
        if (playerService == null) {
            playerService = new PlayerService();
        }
        return playerService;
    }

    public static ExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    @NonNull
    public static Boolean getIsPlayling() {
        return exoPlayer != null && exoPlayer.getPlayWhenReady();
    }

    public long getDuration() {
        if (exoPlayer == null) {
            return 0;
        }
        return exoPlayer.getDuration();
    }

    public int getAudioSessionID() {
        if (exoPlayer == null) {
            return 0;
        }
        return exoPlayer.getAudioSessionId();
    }

    private CountDownTimer countDownTimer;
    private void startTimer(long countdownDuration) {
        long timeLeft = countdownDuration - System.currentTimeMillis();
        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // this method is empty
            }

            @Override
            public void onFinish() {
                try {
                    Intent intent = new Intent(getApplicationContext(), PlayerService.class);
                    intent.setAction(PlayerService.ACTION_STOP);
                    startService(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error onFinish Timer");
                }
            }
        };
        countDownTimer.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // this method is empty
    }

    @Override
    public void onCreate() {

        helper = new Helper(getApplicationContext());
        dbHelper = new DBHelper(getApplicationContext());
        spHelper = new SPHelper(getApplicationContext());

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        componentName = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(componentName);

        try {
            registerReceiver(onCallIncome, new IntentFilter("android.intent.action.PHONE_STATE"));
            registerReceiver(onHeadPhoneDetect, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error on registerReceiver",e);
        }

        bandwidthMeter = new DefaultBandwidthMeter.Builder(getApplicationContext()).build();

        exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
        exoPlayer.addListener(this);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.setReferenceCounted(false);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();
        exoPlayer.setAudioAttributes(audioAttributes, true);
        mMediaSession = new MediaSessionCompat(this, getResources().getString(R.string.app_name));
        mMediaSession.setActive(true);

        notificationReceiver = new NotificationReceiver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, addIntentFilter(), RECEIVER_EXPORTED);
        } else {
            registerReceiver(notificationReceiver, addIntentFilter());
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_STICKY;
        }
        try {
            String action = intent.getAction();
            switch (action) {
                case ACTION_START_TIMER -> startTimer(spHelper.getSleepTime());
                case ACTION_STOP_TIMER -> stopTimer();
                case ACTION_PLAY -> startNewSong();
                case ACTION_TOGGLE -> togglePlay();
                case ACTION_SEEKTO -> seekTo(intent.getExtras().getLong("seekto"));
                case ACTION_STOP -> stop(intent);
                case ACTION_PREVIOUS -> handlePrevious();
                case ACTION_NEXT -> handleNext();
                default -> {
                    return START_STICKY;
                }
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error onStartCommand",e);
        }
        return START_STICKY;
    }

    private void handleNext() {
        boolean isOnline = !Callback.getIsOnline() || NetworkUtils.isConnected(this);
        if (isOnline) {
            next();
            exoPlayer.seekToDefaultPosition(Callback.getPlayPos());
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePrevious() {
        boolean isOnline = !Callback.getIsOnline() || NetworkUtils.isConnected(this);
        if (isOnline) {
            previous();
            exoPlayer.seekToDefaultPosition(Callback.getPlayPos());
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private void createNoti() {
        String channelId = getResources().getString(R.string.app_name);
        int notificationId = 111111;
        notificationManager = new PlayerNotificationManager.Builder(this, notificationId, channelId)
                .setNotificationListener(notificationListener)
                .setMediaDescriptionAdapter(descriptionAdapter)
                .setChannelImportance(NotificationManager.IMPORTANCE_HIGH)
                .setSmallIconResourceId(R.drawable.ic_audio_track)
                .setChannelDescriptionResourceId(R.string.app_name)
                .setNextActionIconResourceId(R.drawable.ic_skip_next)
                .setPreviousActionIconResourceId(R.drawable.ic_skip_previous)
                .setPlayActionIconResourceId(R.drawable.ic_play)
                .setPauseActionIconResourceId(R.drawable.ic_pause)
                .setChannelNameResourceId(R.string.app_name)
                .build();

        notificationManager.setPlayer(exoPlayer);
        notificationManager.setPriority(NotificationCompat.PRIORITY_MAX);

        notificationManager.setUsePlayPauseActions(true);
        notificationManager.setUseFastForwardAction(false);
        notificationManager.setUseRewindAction(false);
        notificationManager.setUseNextAction(true);
        notificationManager.setUsePreviousAction(true);
        notificationManager.setUseStopAction(true);
        notificationManager.setUseNextActionInCompactView(true);
        notificationManager.setUsePreviousActionInCompactView(true);

        MediaSessionCompat.Token compatToken = mMediaSession.getSessionToken();
        MediaSession.Token frameworkToken = (MediaSession.Token) compatToken.getToken();
        notificationManager.setMediaSessionToken(frameworkToken);

        updateNotiImage();
    }

    PlayerNotificationManager.NotificationListener notificationListener = new PlayerNotificationManager.NotificationListener() {
        @Override
        public void onNotificationPosted(int notificationId, @NonNull Notification notification, boolean ongoing) {
            if (ongoing) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
                } else {
                    startForeground(notificationId, notification);
                }
            }
        }
    };

    PlayerNotificationManager.MediaDescriptionAdapter descriptionAdapter = new PlayerNotificationManager.MediaDescriptionAdapter() {
        @Override
        public @Unmodifiable CharSequence getCurrentContentTitle(Player player) {
            int playPos = player.getCurrentWindowIndex();
            if (playPos != Callback.getPlayPos()){
                changePlayerPlayPos(playPos);
            }
            return Callback.getArrayListPlay().get(Callback.getPlayPos()).getTitle();
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(@NonNull Player player) {
            Intent notificationIntent = new Intent(PlayerService.this, LauncherActivity.class);
            notificationIntent.setAction(Intent.ACTION_MAIN);
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            notificationIntent.putExtra("isnoti", true);
            return PendingIntent.getActivity(PlayerService.this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        }

        @Nullable
        @Override
        public @Unmodifiable CharSequence getCurrentContentText(@NonNull Player player) {
            return Callback.getArrayListPlay().get(Callback.getPlayPos()).getArtist();
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(@NonNull Player player, @NonNull PlayerNotificationManager.BitmapCallback callback) {
            callbackBitmap = callback;
            return bitmap;
        }
    };

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        Player.Listener.super.onPlaybackStateChanged(playbackState);
        if (playbackState == Player.STATE_READY) {
            exoPlayer.play();
            if (Boolean.TRUE.equals(isNewSong)) {
                isNewSong = false;
                Callback.setIsPlayed(true);
                setBuffer(false);
                GlobalBus.getBus().postSticky(Callback.getArrayListPlay().get(Callback.getPlayPos()));
                if (notificationManager == null) {
                    createNoti();
                } else {
                    updateNotiImage();
                }
            }
        }
    }

    @Override
    public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
        Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
        if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
            onCompletion();
        }
    }

    private void onCompletion() {
        if (Boolean.TRUE.equals(Callback.getIsRepeat())) {
            exoPlayer.seekToDefaultPosition(Callback.getPlayPos());
        } else {
            if (Boolean.TRUE.equals(Callback.getIsShuffle())) {
                Callback.setPlayPos(ApplicationUtil.getRandomValue((Callback.getArrayListPlay().size() - 1) + 1));
                exoPlayer.seekToDefaultPosition(Callback.getPlayPos());
            } else {
                if (Callback.getPlayPos() < (Callback.getArrayListPlay().size() - 1)) {
                    Callback.setPlayPos(Callback.getPlayPos() + 1);
                } else {
                    Callback.setPlayPos(0);
                }
            }
        }
        Callback.setIsPlayed(true);
        setBuffer(false);
        GlobalBus.getBus().postSticky(Callback.getArrayListPlay().get(Callback.getPlayPos()));
        if (notificationManager == null) {
            createNoti();
        } else {
            updateNotiImage();
        }
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        Player.Listener.super.onIsPlayingChanged(isPlaying);
        changePlayPause(isPlaying);
        if (isPlaying) {
            if (!mWakeLock.isHeld()) {
                mWakeLock.acquire(60000);
            }
        } else {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    @Override
    public void onPlayerError(@NonNull PlaybackException error) {
        Player.Listener.super.onPlayerError(error);
        exoPlayer.setPlayWhenReady(false);
        setBuffer(false);
        changePlayPause(false);
    }

    private void startNewSong() {

        isNewSong = true;
        setBuffer(true);

        try {
            if (Boolean.TRUE.equals(Callback.getIsOnline()) || Boolean.FALSE.equals(Callback.getIsDownloaded())) {
                dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), "tamilaudiopro"), bandwidthMeter);
            } else {
                if (mCipher == null) {
                    final String AES_ALGORITHM = "AES";
                    final String AES_TRANSFORMATION = "AES/CTR/NoPadding";

                    secretKeySpec = new SecretKeySpec(secretKey, AES_ALGORITHM);
                    try {
                        mCipher = Cipher.getInstance(AES_TRANSFORMATION);
                        mCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(initialIv));
                    } catch (Exception e) {
                        ApplicationUtil.log(TAG, "Error on startNewSong",e);
                    }
                }
                dataSourceFactory = new EncryptedFileDataSourceFactory(mCipher, secretKeySpec, new IvParameterSpec(initialIv), bandwidthMeter);
            }

            boolean isLoadMultipleMedia = Callback.getIsNewAdded() || exoPlayer.getMediaItemCount() == 0 || Callback.getPlayPos() >= exoPlayer.getMediaItemCount();
            if (isLoadMultipleMedia) {
                loadMultipleMedia();
            }
            exoPlayer.seekToDefaultPosition(Callback.getPlayPos());
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);

            if (Boolean.FALSE.equals(Callback.getIsDownloaded())) {
                dbHelper.addToRecent(Callback.getArrayListPlay().get(Callback.getPlayPos()), Callback.getIsOnline());
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error on startNewSong",e);
        }
    }

    private void loadMultipleMedia() {
        List<MediaSource> mediaItemsSource = new ArrayList<>();
        String description = "Media description for item ";
        for (ItemSong playerItem : Callback.getArrayListPlay()) {
            MediaMetadata metadata = new MediaMetadata.Builder()
                    .setTitle(playerItem.getTitle())
                    .setSubtitle(playerItem.getArtist())
                    .setDescription(description + playerItem.getTitle())
                    .build();
            MediaSource sampleSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(new MediaItem.Builder()
                            .setMediaMetadata(metadata)
                            .setUri(getAudioURL(playerItem))
                            .build());
            mediaItemsSource.add(sampleSource);
        }
        exoPlayer.setMediaSources(mediaItemsSource);
    }

    private Uri getAudioURL(ItemSong playerItem) {
        switch (Callback.getAudioQuality()) {
            case 2 -> {
                if (!playerItem.getAudioUrlHigh().isEmpty()) {
                    return Uri.parse(playerItem.getAudioUrlHigh());
                } else if (!playerItem.getAudioUrlLow().isEmpty()) {
                    return Uri.parse(playerItem.getAudioUrlLow());
                } else {
                    return Uri.parse(playerItem.getUrl());
                }
            }
            case 3 -> {
                if (!playerItem.getAudioUrlLow().isEmpty()) {
                    return Uri.parse(playerItem.getAudioUrlLow());
                } else {
                    return Uri.parse(playerItem.getUrl());
                }
            }
            default -> {
                return Uri.parse(playerItem.getUrl());
            }
        }
    }

    public void addMediaSource(Uri uri) {
        if (dataSourceFactory != null && exoPlayer != null) {
            exoPlayer.addMediaSource(new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(new MediaItem.Builder().setUri(uri).build()));
        }
    }

    private void togglePlay() {
        exoPlayer.setPlayWhenReady(!exoPlayer.getPlayWhenReady());
        changePlayPause(exoPlayer.getPlayWhenReady());
    }

    private void previous() {
        isNewSong = true;
        setBuffer(true);
        if (Boolean.TRUE.equals(Callback.getIsShuffle())) {
            Callback.setPlayPos(ApplicationUtil.getRandomValue((Callback.getArrayListPlay().size() - 1) + 1));
        } else {
            if (Callback.getPlayPos() > 0) {
                Callback.setPlayPos(Callback.getPlayPos() - 1);
            } else {
                Callback.setPlayPos(Callback.getArrayListPlay().size() - 1);
            }
        }
        if (exoPlayer.getMediaItemCount() != 0) {
            exoPlayer.setPlayWhenReady(true);
        } else {
            startNewSong();
        }
    }

    private void next() {
        isNewSong = true;
        setBuffer(true);
        if (Boolean.TRUE.equals(Callback.getIsShuffle())) {
            Callback.setPlayPos(ApplicationUtil.getRandomValue((Callback.getArrayListPlay().size() - 1) + 1));
        } else {
            if (Callback.getPlayPos() < (Callback.getArrayListPlay().size() - 1)) {
                Callback.setPlayPos(Callback.getPlayPos() + 1);
            } else {
                Callback.setPlayPos(0);
            }
        }
        if (exoPlayer.getMediaItemCount() != 0) {
            exoPlayer.setPlayWhenReady(true);
        } else {
            startNewSong();
        }
    }

    public void previousNoti() {
        isNewSong = true;
        setBuffer(true);

        if (Callback.getPlayPos() > 0) {
            Callback.setPlayPos(Callback.getPlayPos() - 1);
        } else {
            Callback.setPlayPos(Callback.getArrayListPlay().size() - 1);
        }
    }

    public void nextNoti() {
        isNewSong = true;
        setBuffer(true);

        if (Callback.getPlayPos() < (Callback.getArrayListPlay().size() - 1)) {
            Callback.setPlayPos(Callback.getPlayPos() + 1);
        } else {
            Callback.setPlayPos(0);
        }
    }

    private void seekTo(long seek) {
        exoPlayer.seekTo((int) seek);
    }

    private void changePlayPause(Boolean flag) {
        try {
            changeEquilizer();
            GlobalBus.getBus().postSticky(new MessageEvent(flag, "playicon"));
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error changePlayPause",e);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void updateNotiImage() {
        new AsyncTaskExecutor<String, String, String>() {

            @Override
            protected void onPostExecute(String s) {
                // this method is empty
            }

            @Override
            protected String doInBackground(String strings) {
                try {
                    if(Boolean.TRUE.equals(Callback.getIsOnline())) {
                        ApplicationUtil.responsePost(Callback.API_URL, helper.getAPIRequest(Method.METHOD_SINGLE_SONG,0,
                                Callback.getArrayListPlay().get(Callback.getPlayPos()).getId(),"","","",
                                "","","","","", "","", "", null));
                    }
                    getBitmapFromURL(Callback.getArrayListPlay().get(Callback.getPlayPos()).getImageBig());
                    return "1";
                } catch (Exception e) {
                    return "0";
                }

            }
        }.execute();
    }

    private void setBuffer(Boolean isBuffer) {
        if (Boolean.FALSE.equals(isBuffer)) {
            changeEquilizer();
        }
        GlobalBus.getBus().postSticky(new MessageEvent(isBuffer, "buffer"));
    }

    private void changeEquilizer() {
        GlobalBus.getBus().postSticky(new ItemAlbums("", "", "", ""));
    }

    private void changePlayerPlayPos(int playPos) {
        try {
            if (Callback.getIsDownloaded().equals(Boolean.FALSE)) {
                dbHelper.addToRecent(Callback.getArrayListPlay().get(Callback.getPlayPos()), Callback.getIsOnline());
            }
            Callback.setPlayPos(playPos);
            GlobalBus.getBus().postSticky(Callback.getArrayListPlay().get(Callback.getPlayPos()));
            if (notificationManager != null) {
                updateNotiImage();
            }
            changeEquilizer();
        } catch (Exception e) {
            Log.e(TAG, "Error changePlayerPlayPos", e);
        }
    }

    private void getBitmapFromURL(String src) {
        try {
            if (Boolean.TRUE.equals(Callback.getIsOnline())) {
                InputStream input = getInputStream(src);
                bitmap = BitmapFactory.decodeStream(input);

            } else {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(src));
                } catch (Exception e) {
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_song_light);
                }
            }
            if (callbackBitmap != null) {
                callbackBitmap.onBitmap(bitmap);
            }
        } catch (IOException e) {
            ApplicationUtil.log(TAG, "Error getBitmapFromURL",e);
        }
    }

    private static InputStream getInputStream(String src) throws IOException {
        URL url = new URL(src);
        InputStream input;
        if (src.contains("https://")) {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
        } else {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
        }
        return input;
    }

    BroadcastReceiver onCallIncome = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            try {
                String a = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                if (a != null && exoPlayer.getPlayWhenReady()
                        && (a.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)
                        || a.equals(TelephonyManager.EXTRA_STATE_RINGING))) {
                    exoPlayer.setPlayWhenReady(false);
                }
            } catch (Exception e) {
                ApplicationUtil.log(TAG, "Error onCallIncome",e);
            }
        }
    };

    BroadcastReceiver onHeadPhoneDetect = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (exoPlayer.getPlayWhenReady()) {
                    togglePlay();
                }
            } catch (Exception e) {
                ApplicationUtil.log(TAG, "Error onHeadPhoneDetect",e);
            }
        }
    };

    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = focusChange -> {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
            try {
                if (exoPlayer.getPlayWhenReady()) {
                    togglePlay();
                }
            } catch (Exception e) {
                ApplicationUtil.log(TAG, "Error onAudioFocusChangeListener",e);
            }
        }
    };

    private class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (Objects.requireNonNull(intent.getAction())) {
                case PlayerNotificationManager.ACTION_PREVIOUS :
                    previousNoti();
                    break;
                case PlayerNotificationManager.ACTION_NEXT :
                    nextNoti();
                    break;
                case PlayerNotificationManager.ACTION_STOP :
                    stop(intent);
                    break;
                default:
                    break;
            }
        }
    }

    @NonNull
    private IntentFilter addIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerNotificationManager.ACTION_PREVIOUS);
        intentFilter.addAction(PlayerNotificationManager.ACTION_NEXT);
        intentFilter.addAction(PlayerNotificationManager.ACTION_NEXT);
        intentFilter.addAction(PlayerNotificationManager.ACTION_STOP);
        return intentFilter;
    }

    private void stop(Intent intent) {
        try {
            Callback.setIsPlayed(false);

            exoPlayer.setPlayWhenReady(false);
            changePlayPause(false);
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
            try {
                mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
                mAudioManager.unregisterMediaButtonEventReceiver(componentName);
                unregisterReceiver(onCallIncome);
                unregisterReceiver(onHeadPhoneDetect);
            } catch (Exception e) {
                ApplicationUtil.log(TAG, "Error stop unregisterReceiver",e);
            }
            stopService(intent);
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error stop",e);
        }
    }

    @Override
    public void onDestroy() {
        try {
            Callback.setIsPlayed(false);

            try {
                mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
                mAudioManager.unregisterMediaButtonEventReceiver(componentName);
                unregisterReceiver(onCallIncome);
                unregisterReceiver(onHeadPhoneDetect);
            } catch (Exception e) {
                ApplicationUtil.log(TAG, "Error onDestroy unregisterReceiver",e);
            }
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error onDestroy",e);
        }

        try {
            unregisterReceiver(notificationReceiver);
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            if (dbHelper != null){
                dbHelper.close();
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error onDestroy all",e);
        }
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        try {
            if (!exoPlayer.getPlayWhenReady())
                stopForeground(true);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error onTaskRemoved",e);
        }
    }
}