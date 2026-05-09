package nemosofts.tamilaudiopro.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.nemosofts.coreprogress.ProgressHelper;
import androidx.nemosofts.coreprogress.ProgressUIListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.encrypt.Encrypter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

public class DownloadService extends Service {

    private static final String TAG = "DownloadService";
    private NotificationCompat.Builder myNotify;
    private RemoteViews rv;
    private OkHttpClient client;
    public static final String ACTION_STOP = "com.mydownload.action.STOP";
    public static final String ACTION_START = "com.mydownload.action.START";
    public static final String ACTION_ADD = "com.mydownload.action.ADD";
    private static final String CANCEL_TAG = "c_tag";
    private NotificationManager mNotificationManager;

    public static DownloadService downloadService;

    private Encrypter enc;
    private Boolean isDownloaded = false;
    private Thread thread;
    private Call call;
    public static int count = 0;
    private static final List<String> arrayListName = new ArrayList<>();
    private static final List<String> arrayListFilePath = new ArrayList<>();
    private static final List<String> arrayListURL = new ArrayList<>();
    private static final List<ItemSong> arrayListSong = new ArrayList<>();
    private static final int MY_NOTIFICATION_ID = 1002;
    private static final String DOWNLOAD_CHANNEL_ID = "download_ch_1";

    public static DownloadService getInstance() {
        if (downloadService == null) {
            downloadService = new DownloadService();
        }
        return downloadService;
    }

    public static Boolean isDownloading() {
        return !arrayListFilePath.isEmpty();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper(), msg -> {
        switch (msg.what) {
            case 1:
                int progress = Integer.parseInt(msg.obj.toString());
                if (!arrayListSong.isEmpty()) {
                    rv.setProgressBar(R.id.progress, 100, progress, false);
                    mNotificationManager.notify(MY_NOTIFICATION_ID, myNotify.build());
                }
                break;
            case 0:
                rv.setTextViewText(R.id.nf_title, arrayListSong.get(0).getTitle());
                rv.setTextViewText(R.id.nf_percentage, count - (arrayListURL.size() - 1) + "/" + count + " " + getString(R.string.downloading));
                mNotificationManager.notify(MY_NOTIFICATION_ID, myNotify.build());
                break;
            case 2:
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    ApplicationUtil.log("Thread", "Thread was interrupted", e);
                }
                rv.setProgressBar(R.id.progress, 100, 100, false);
                rv.setTextViewText(R.id.nf_percentage, count + "/" + count + " " + getString(R.string.downloaded));
                mNotificationManager.notify(MY_NOTIFICATION_ID, myNotify.build());
                count = 0;
                break;
            default:
                break;
        }
        return false;
    });

    @Override
    public void onCreate() {
        super.onCreate();

        enc = Encrypter.getInstance();
        enc.init(this);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        myNotify = new NotificationCompat.Builder(this, DOWNLOAD_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_audio_download)
                .setTicker(getResources().getString(R.string.downloading))
                .setWhen(System.currentTimeMillis())
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        rv = new RemoteViews(getPackageName(), R.layout.row_custom_notification);
        rv.setTextViewText(R.id.nf_title, getString(R.string.app_name));
        rv.setProgressBar(R.id.progress, 100, 0, false);
        rv.setTextViewText(R.id.nf_percentage, getResources().getString(R.string.downloading) + " " + "(0%)");

        Intent closeIntent = new Intent(this, DownloadService.class);
        closeIntent.setAction(ACTION_STOP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pCloseIntent = PendingIntent.getService(this, 0, closeIntent, flags);
        rv.setOnClickPendingIntent(R.id.iv_stop_download, pCloseIntent);

        myNotify.setCustomContentView(rv);

        startForegroundService();
    }

    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(MY_NOTIFICATION_ID, myNotify.build(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(MY_NOTIFICATION_ID, myNotify.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Online Channel download";
            NotificationChannel mChannel = new NotificationChannel(DOWNLOAD_CHANNEL_ID,
                    name, NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            stopForeground();
            stop(null);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error stopping service", e);
        }
    }

    private void stopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null && intent.getAction() != null) {
            return switch (intent.getAction()) {
                case ACTION_START -> {
                    handleStartAction(intent);
                    yield START_NOT_STICKY;
                }
                case ACTION_STOP -> {
                    stop(intent);
                    yield START_STICKY;
                }
                case ACTION_ADD -> {
                    handleAddAction(intent);
                    yield START_REDELIVER_INTENT;
                }
                default -> START_STICKY;
            };
        }
        return START_STICKY;
    }

    private void handleAddAction(Intent intent) {
        ItemSong itemVideoDownload = getItemSongDownload(intent);
        if (itemVideoDownload != null && !isAlreadyAdded(itemVideoDownload)) {
            count++;
            arrayListURL.add(intent.getStringExtra("downloadUrl"));
            arrayListFilePath.add(intent.getStringExtra("file_path"));
            arrayListName.add(intent.getStringExtra("file_name"));
            arrayListSong.add(itemVideoDownload);

            Message msg = mHandler.obtainMessage();
            msg.what = 0;
            mHandler.sendMessage(msg);
        }
    }

    private boolean isAlreadyAdded(ItemSong itemDownload) {
        for (ItemSong itemSong : arrayListSong) {
            if (itemSong.getId().equals(itemDownload.getId())) {
                return true;
            }
        }
        return false;
    }

    private void handleStartAction(Intent intent) {
        ItemSong item = getItemSongDownload(intent);
        if (item != null) {
            arrayListURL.add(intent.getStringExtra("downloadUrl"));
            arrayListFilePath.add(intent.getStringExtra("file_path"));
            arrayListName.add(intent.getStringExtra("file_name"));
            arrayListSong.add(item);
            count++;
            init();
        }
    }

    @SuppressWarnings("deprecation")
    public static ItemSong getItemSongDownload(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getSerializableExtra("item", ItemSong.class);
        } else {
            return (ItemSong) intent.getSerializableExtra("item");
        }
    }

    private void stop(Intent intent) {
        try {
            count = 0;
            if (client != null) {
                for (Call call1 : client.dispatcher().runningCalls()) {
                    if (Objects.equals(call1.request().tag(), CANCEL_TAG))
                        call1.cancel();
                }
            }
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
            deleteFirstFile(arrayListFilePath, arrayListName);

            arrayListSong.clear();
            arrayListName.clear();
            arrayListURL.clear();
            arrayListFilePath.clear();

            stopForeground();
            if (intent != null) {
                stopService(intent);
            } else {
                stopSelf();
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error stopping service", e);
        }
    }

    public void deleteFirstFile(List<String> filePaths, List<String> names) {
        if (filePaths == null || filePaths.isEmpty() || names.isEmpty()) {
            Log.e(TAG, "One or more lists are empty. Cannot delete file.");
            return;
        }

        try {
            File baseDir = new File(filePaths.get(0));
            String relativeName = names.get(0).replaceFirst(".mp3", "");
            File file = new File(baseDir, relativeName);

            boolean deleted;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.delete(file.toPath());
                deleted = true;
            } else {
                deleted = file.delete();
            }

            if (deleted) {
                ApplicationUtil.log(TAG, "File deleted successfully");
            } else {
                ApplicationUtil.log(TAG, "Failed to delete file: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "Unexpected error during file deletion", e);
        }
    }

    public void init() {
        thread = new Thread(() -> {
            isDownloaded = false;

            client = new OkHttpClient();
            Request.Builder builder = new Request.Builder()
                    .url(arrayListURL.get(0))
                    .addHeader("Accept-Encoding", "identity")
                    .get()
                    .tag(CANCEL_TAG);

            call = client.newCall(builder.build());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d(TAG, "Error in download", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    handleResponse(response);
                }
            });
        });
        thread.start();
    }

    private void handleResponse(Response response) {
        if (response == null|| response.body() == null){
            return;
        }
        ResponseBody responseBody = ProgressHelper.withProgress(response.body(), new ProgressUIListener() {
            @Override
            public void onUIProgressStart(long totalBytes) {
                super.onUIProgressStart(totalBytes);
                Message msg = mHandler.obtainMessage();
                msg.what = 0;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                if (Boolean.FALSE.equals(isDownloaded)) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = (int) (100 * percent) + "";
                    mHandler.sendMessage(msg);
                }
            }
        });

        try {
            BufferedSource source = responseBody.source();
            enc.encrypt(arrayListFilePath.get(0) + "/" + arrayListName.get(0), source, arrayListSong.get(0));
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error encrypt", e);
        }

        if (!arrayListURL.isEmpty()) {
            arrayListSong.remove(0);
            arrayListName.remove(0);
            arrayListFilePath.remove(0);
            arrayListURL.remove(0);
            if (!call.isCanceled() && !arrayListURL.isEmpty()) {
                init();
            } else {
                Message msg = mHandler.obtainMessage();
                msg.what = 2;
                msg.obj = "0";
                mHandler.sendMessage(msg);
                isDownloaded = true;
            }
        }
    }
}