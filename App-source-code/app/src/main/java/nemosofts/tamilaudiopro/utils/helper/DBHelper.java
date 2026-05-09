package nemosofts.tamilaudiopro.utils.helper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.nemosofts.BuildConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.item.ItemAbout;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.encrypt.EncryptData;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = BuildConfig.APPLICATION_ID + "_" + "app.db";
    private final SQLiteDatabase db;
    EncryptData encryptData;
    final Context context;

    private static final  String TAG_PLAYLIST_ID = "id";
    private static final  String TAG_PLAYLIST_NAME = "name";

    private static final  String TAG_ID = "id";
    private static final  String TAG_SID = "sid";
    private static final  String TAG_PID = "pid";
    private static final  String TAG_TITLE = "title";
    private static final  String TAG_DESC = "description";
    private static final  String TAG_LYRICS = "lyrics";
    private static final  String TAG_ARTIST = "artist";
    private static final  String TAG_DURATION = "duration";
    private static final  String TAG_URL = "url";
    private static final  String TAG_URL_HIGH = "url_high";
    private static final  String TAG_URL_LOW = "url_low";
    private static final  String TAG_IMAGE = "image";
    private static final  String TAG_AVG_RATE = "avg_rate";
    private static final  String TAG_VIEWS = "views";
    private static final  String TAG_DOWNLOADS = "downloads";
    private static final String TAG_TEMP_NAME = "tempid";

    // Table Name
    private static final String TABLE_ABOUT = "about";
    private static final String TABLE_PLAYLIST = "playlist";
    private static final String TABLE_PLAYLIST_OFFLINE = "playlist_offline";
    private static final String TABLE_PLAYLIST_SONG = "playlistsong";
    private static final String TABLE_PLAYLIST_SONG_OFFLINE = "playlistsong_offline";
    private static final String TABLE_RECENT = "recent";
    private static final String TABLE_RECENT_OFFLINE = "recent_off";
    private static final String TABLE_DOWNLOAD_SONG = "download";

    private static final String TAG_ABOUT_EMAIL = "email";
    private static final String TAG_ABOUT_AUTHOR = "author";
    private static final String TAG_ABOUT_CONTACT = "contact";
    private static final String TAG_ABOUT_WEBSITE = "website";
    private static final String TAG_ABOUT_DESC = "description";
    private static final String TAG_ABOUT_DEVELOPED = "developed";
    private static final String TAG_ABOUT_ENVATO_API_KEY = "envato_key";
    private static final String TAG_ABOUT_MORE_APP = "more_apps";

    private final String[] columnsAbout = new String[]{
            TAG_ABOUT_EMAIL, TAG_ABOUT_AUTHOR, TAG_ABOUT_CONTACT, TAG_ABOUT_WEBSITE, TAG_ABOUT_DESC,
            TAG_ABOUT_DEVELOPED, TAG_ABOUT_ENVATO_API_KEY, TAG_ABOUT_MORE_APP
    };
    private final String[] columnsSong = new String[]{
            TAG_ID, TAG_SID, TAG_TITLE, TAG_DESC, TAG_ARTIST, TAG_DURATION, TAG_LYRICS,
            TAG_URL, TAG_URL_HIGH, TAG_URL_LOW, TAG_IMAGE, TAG_AVG_RATE, TAG_VIEWS, TAG_DOWNLOADS
    };
    private final String[] columnsPlaylistSong = new String[]{
            TAG_ID, TAG_SID, TAG_TITLE, TAG_DESC, TAG_ARTIST, TAG_DURATION, TAG_LYRICS,
            TAG_URL, TAG_URL_HIGH, TAG_URL_LOW, TAG_IMAGE, TAG_AVG_RATE, TAG_VIEWS, TAG_DOWNLOADS
    };
    private final String[] columnsDownloadSong = new String[]{
            TAG_ID, TAG_SID, TAG_TITLE, TAG_DESC, TAG_ARTIST, TAG_DURATION, TAG_LYRICS,
            TAG_URL, TAG_URL_HIGH, TAG_URL_LOW, TAG_IMAGE, TAG_AVG_RATE, TAG_VIEWS, TAG_DOWNLOADS,
            TAG_TEMP_NAME
    };
    private final String[] columnsPlaylist = new String[]{
            TAG_PLAYLIST_ID, TAG_PLAYLIST_NAME
    };

    private static final String CREATE_TABLE = "CREATE TABLE ";
    private static final String AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    private static final String TEXT_ONE = " TEXT,";
    private static final String TEXT_END = " TEXT";

    // Creating table playlist
    private static final String CREATE_TABLE_PLAYLIST = CREATE_TABLE + TABLE_PLAYLIST + "(" + TAG_PLAYLIST_ID
            + AUTOINCREMENT + TAG_PLAYLIST_NAME + " TEXT);";
    // Creating table playlist offline
    private static final String CREATE_TABLE_PLAYLIST_OFFLINE = CREATE_TABLE + TABLE_PLAYLIST_OFFLINE + "(" + TAG_PLAYLIST_ID
            + AUTOINCREMENT + TAG_PLAYLIST_NAME + " TEXT);";

    // Creating table about
    private static final String CREATE_TABLE_ABOUT = CREATE_TABLE + TABLE_ABOUT + " ("
            + TAG_ABOUT_EMAIL + TEXT_ONE
            + TAG_ABOUT_AUTHOR + TEXT_ONE
            + TAG_ABOUT_CONTACT + TEXT_ONE
            + TAG_ABOUT_WEBSITE + TEXT_ONE
            + TAG_ABOUT_DESC + TEXT_ONE
            + TAG_ABOUT_DEVELOPED + TEXT_ONE
            + TAG_ABOUT_ENVATO_API_KEY + TEXT_ONE
            + TAG_ABOUT_MORE_APP + TEXT_END
            + ")";

    // Creating table query
    private static final String CREATE_TABLE_PLAYLIST_SONG = CREATE_TABLE + TABLE_PLAYLIST_SONG + "(" +
            TAG_ID + AUTOINCREMENT
            + TAG_SID + TEXT_ONE
            + TAG_TITLE + TEXT_ONE
            + TAG_DESC + TEXT_ONE
            + TAG_LYRICS + TEXT_ONE
            + TAG_ARTIST + TEXT_ONE
            + TAG_DURATION + TEXT_ONE
            + TAG_URL + TEXT_ONE
            + TAG_URL_HIGH + TEXT_ONE
            + TAG_URL_LOW + TEXT_ONE
            + TAG_IMAGE + TEXT_ONE
            + TAG_PID + TEXT_ONE
            + TAG_AVG_RATE + TEXT_ONE
            + TAG_VIEWS + TEXT_ONE
            + TAG_DOWNLOADS + TEXT_END
            + ");";

    // Creating table query
    private static final String CREATE_TABLE_PLAYLIST_SONG_OFFLINE = CREATE_TABLE + TABLE_PLAYLIST_SONG_OFFLINE + "(" +
            TAG_ID + AUTOINCREMENT
            + TAG_SID + TEXT_ONE
            + TAG_TITLE + TEXT_ONE
            + TAG_DESC + TEXT_ONE
            + TAG_LYRICS + TEXT_ONE
            + TAG_ARTIST + TEXT_ONE
            + TAG_DURATION + TEXT_ONE
            + TAG_URL + TEXT_ONE
            + TAG_URL_HIGH + TEXT_ONE
            + TAG_URL_LOW + TEXT_ONE
            + TAG_IMAGE + TEXT_ONE
            + TAG_PID + TEXT_ONE
            + TAG_AVG_RATE + TEXT_ONE
            + TAG_VIEWS + TEXT_ONE
            + TAG_DOWNLOADS + TEXT_END
            + ");";

    // Creating table query
    private static final String CREATE_TABLE_RECENT = CREATE_TABLE + TABLE_RECENT + "(" +
            TAG_ID + AUTOINCREMENT
            + TAG_SID + TEXT_ONE
            + TAG_TITLE + TEXT_ONE
            + TAG_DESC + TEXT_ONE
            + TAG_LYRICS + TEXT_ONE
            + TAG_ARTIST + TEXT_ONE
            + TAG_DURATION + TEXT_ONE
            + TAG_URL + TEXT_ONE
            + TAG_URL_HIGH + TEXT_ONE
            + TAG_URL_LOW + TEXT_ONE
            + TAG_IMAGE + TEXT_ONE
            + TAG_AVG_RATE + TEXT_ONE
            + TAG_VIEWS + TEXT_ONE
            + TAG_DOWNLOADS + TEXT_END
            + ");";

    // Creating table query
    private static final String CREATE_TABLE_RECENT_OFFLINE = CREATE_TABLE + TABLE_RECENT_OFFLINE + "(" +
            TAG_ID + AUTOINCREMENT
            + TAG_SID + TEXT_ONE
            + TAG_TITLE + TEXT_ONE
            + TAG_DESC + TEXT_ONE
            + TAG_LYRICS + TEXT_ONE
            + TAG_ARTIST + TEXT_ONE
            + TAG_DURATION + TEXT_ONE
            + TAG_URL + TEXT_ONE
            + TAG_URL_HIGH + TEXT_ONE
            + TAG_URL_LOW + TEXT_ONE
            + TAG_IMAGE + TEXT_ONE
            + TAG_AVG_RATE + TEXT_ONE
            + TAG_VIEWS + TEXT_ONE
            + TAG_DOWNLOADS + TEXT_END
            + ");";

    // Creating table query
    private static final String CREATE_TABLE_DOWNLOAD = CREATE_TABLE + TABLE_DOWNLOAD_SONG + "("
            + TAG_ID + AUTOINCREMENT
            + TAG_SID + TEXT_ONE
            + TAG_TITLE + TEXT_ONE
            + TAG_DESC + TEXT_ONE
            + TAG_LYRICS + TEXT_ONE
            + TAG_ARTIST + TEXT_ONE
            + TAG_DURATION + TEXT_ONE
            + TAG_URL + TEXT_ONE
            + TAG_URL_HIGH + TEXT_ONE
            + TAG_URL_LOW + TEXT_ONE
            + TAG_IMAGE + TEXT_ONE
            + TAG_AVG_RATE + TEXT_ONE
            + TAG_VIEWS + TEXT_ONE
            + TAG_DOWNLOADS + TEXT_ONE
            + TAG_TEMP_NAME + TEXT_END
            + ");";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        encryptData = new EncryptData(context);
        this.context = context;
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_DOWNLOAD);
            db.execSQL(CREATE_TABLE_PLAYLIST);
            db.execSQL(CREATE_TABLE_PLAYLIST_OFFLINE);
            db.execSQL(CREATE_TABLE_PLAYLIST_SONG);
            db.execSQL(CREATE_TABLE_PLAYLIST_SONG_OFFLINE);
            db.execSQL(CREATE_TABLE_RECENT);
            db.execSQL(CREATE_TABLE_RECENT_OFFLINE);
            db.execSQL(CREATE_TABLE_ABOUT);
            addPlayListMyPlay(db, context.getString(R.string.myplaylist), true);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error creating table", e);
        }
    }

    // Upgrade -------------------------------------------------------------------------------------
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (db == null){
            return;
        }
        final String dropTable = "DROP TABLE IF EXISTS ";
        db.execSQL(dropTable + TABLE_DOWNLOAD_SONG);
        db.execSQL(dropTable + TABLE_PLAYLIST);
        db.execSQL(dropTable + TABLE_PLAYLIST_OFFLINE);
        db.execSQL(dropTable + TABLE_PLAYLIST_SONG);
        db.execSQL(dropTable + TABLE_PLAYLIST_SONG_OFFLINE);
        db.execSQL(dropTable + TABLE_RECENT);
        db.execSQL(dropTable + TABLE_RECENT_OFFLINE);
        db.execSQL(dropTable + TABLE_ABOUT);
        onCreate(db);
    }

    public void addPlayListMyPlay(SQLiteDatabase db, String playlist, Boolean isOnline) {
        if (db == null){
            return;
        }

        String table = Boolean.FALSE.equals(isOnline) ? TABLE_PLAYLIST_OFFLINE : TABLE_PLAYLIST;
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_PLAYLIST_NAME, playlist);
        db.insert(table, null, contentValues);
        loadPlayList(isOnline);
    }

    public List<ItemMyPlayList> addPlayList(String playlist, Boolean isOnline) {
        String table = Boolean.FALSE.equals(isOnline) ? TABLE_PLAYLIST_OFFLINE : TABLE_PLAYLIST;
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_PLAYLIST_NAME, playlist);
        db.insert(table, null, contentValues);
        return loadPlayList(isOnline);
    }

    public void addToDownloads(ItemSong itemSong) {
        if (itemSong == null){
            return;
        }
        String name = itemSong.getTitle().replace("'", "%27");
        String description = DatabaseUtils.sqlEscapeString(itemSong.getDescription());
        String lyrics = DatabaseUtils.sqlEscapeString(itemSong.getLyrics());

        String imageBig = encryptData.encrypt(itemSong.getImageBig());
        String url = encryptData.encrypt(itemSong.getUrl());
        String urlHigh = encryptData.encrypt(itemSong.getAudioUrlHigh());
        String urlLow = encryptData.encrypt(itemSong.getAudioUrlLow());

        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_SID, itemSong.getId());
        contentValues.put(TAG_TITLE, name);
        contentValues.put(TAG_DESC, description);
        contentValues.put(TAG_LYRICS, lyrics);
        contentValues.put(TAG_ARTIST, itemSong.getArtist());
        contentValues.put(TAG_URL, url);
        contentValues.put(TAG_URL_HIGH, urlHigh);
        contentValues.put(TAG_URL_LOW, urlLow);
        contentValues.put(TAG_IMAGE, imageBig);
        contentValues.put(TAG_AVG_RATE, itemSong.getAverageRating());
        contentValues.put(TAG_VIEWS, itemSong.getViews());
        contentValues.put(TAG_DOWNLOADS, itemSong.getDownloads());
        contentValues.put(TAG_TEMP_NAME, itemSong.getTempName());

        db.insert(TABLE_DOWNLOAD_SONG, null, contentValues);

    }

    @SuppressLint("Range")
    public void addToRecent(ItemSong itemSong, Boolean isOnline) {
        if (itemSong == null){
            return;
        }
        try (Cursor cursorDelete = db.query(TABLE_RECENT, columnsSong, null,
                null, null, null, null)) {
            if (cursorDelete.getCount() > 20) {
                cursorDelete.moveToFirst();
                String deleteId = cursorDelete.getString(cursorDelete.getColumnIndex(TAG_SID));
                db.delete(TABLE_RECENT, TAG_SID + "=?", new String[]{deleteId});
            }
        }

        String table;
        if (Boolean.TRUE.equals(isOnline)) {
            table = TABLE_RECENT;
        } else {
            table = TABLE_RECENT_OFFLINE;
        }
        if (checkRecent(itemSong.getId(), isOnline)) {
            db.delete(table, TAG_SID + "=" + itemSong.getId(), null);
        }

        String imageBig = encryptData.encrypt(itemSong.getImageBig().replace(" ", "%20"));
        String url = encryptData.encrypt(itemSong.getUrl());
        String urlHigh = encryptData.encrypt(itemSong.getAudioUrlHigh());
        String urlLow = encryptData.encrypt(itemSong.getAudioUrlLow());

        String description = DatabaseUtils.sqlEscapeString(itemSong.getDescription());
        String lyrics = DatabaseUtils.sqlEscapeString(itemSong.getLyrics());
        String name = itemSong.getTitle().replace("'", "%27");

        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_SID, itemSong.getId());
        contentValues.put(TAG_TITLE, name);
        contentValues.put(TAG_DESC, description);
        contentValues.put(TAG_LYRICS, lyrics);
        contentValues.put(TAG_ARTIST, itemSong.getArtist());
        contentValues.put(TAG_URL, url);
        contentValues.put(TAG_URL_HIGH, urlHigh);
        contentValues.put(TAG_URL_LOW, urlLow);
        contentValues.put(TAG_IMAGE, imageBig);
        contentValues.put(TAG_AVG_RATE, itemSong.getAverageRating());
        contentValues.put(TAG_VIEWS, itemSong.getViews());
        contentValues.put(TAG_DOWNLOADS, itemSong.getDownloads());

        db.insert(table, null, contentValues);
    }

    public void addToPlayList(ItemSong itemSong, String pid, Boolean isOnline) {
        if (itemSong == null){
            return;
        }
        String tableName = Boolean.FALSE.equals(isOnline) ? TABLE_PLAYLIST_SONG_OFFLINE : TABLE_PLAYLIST_SONG;
        if (checkPlaylist(itemSong.getId(), isOnline)) {
            db.delete(tableName, TAG_SID + "=" + itemSong.getId(), null);
        }
        String description = DatabaseUtils.sqlEscapeString(itemSong.getDescription());
        String lyrics = DatabaseUtils.sqlEscapeString(itemSong.getLyrics());
        String name = itemSong.getTitle().replace("'", "%27");

        String imageBig = encryptData.encrypt(itemSong.getImageBig().replace(" ", "%20"));
        String url = encryptData.encrypt(itemSong.getUrl());
        String urlHigh = encryptData.encrypt(itemSong.getAudioUrlHigh());
        String urlLow = encryptData.encrypt(itemSong.getAudioUrlLow());

        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_SID, itemSong.getId());
        contentValues.put(TAG_PID, pid);
        contentValues.put(TAG_TITLE, name);
        contentValues.put(TAG_DESC, description);
        contentValues.put(TAG_LYRICS, lyrics);
        contentValues.put(TAG_ARTIST, itemSong.getArtist());
        contentValues.put(TAG_URL, url);
        contentValues.put(TAG_URL_HIGH, urlHigh);
        contentValues.put(TAG_URL_LOW, urlLow);
        contentValues.put(TAG_IMAGE, imageBig);
        contentValues.put(TAG_AVG_RATE, itemSong.getAverageRating());
        contentValues.put(TAG_VIEWS, itemSong.getViews());
        contentValues.put(TAG_DOWNLOADS, itemSong.getDownloads());

        db.insert(tableName, null, contentValues);
    }

    @SuppressLint("Range")
    public List<ItemMyPlayList> loadPlayList(Boolean isOnline) {
        List<ItemMyPlayList> arrayList = new ArrayList<>();
        String tableName = Boolean.FALSE.equals(isOnline) ? TABLE_PLAYLIST_OFFLINE : TABLE_PLAYLIST;
        try (Cursor cursor = db.query(tableName, columnsPlaylist, null, null,
                null, null, TAG_PLAYLIST_NAME + " ASC")) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(TAG_PLAYLIST_ID));
                    String name = cursor.getString(cursor.getColumnIndex(TAG_PLAYLIST_NAME));

                    ItemMyPlayList objItem = new ItemMyPlayList(id, name, loadPlaylistImages(id, isOnline));
                    arrayList.add(objItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error loadPlayList", e);
        }
        return arrayList;
    }

    public void removeFromDownload(String id) {
        if (id == null){
            return;
        }
        try {
            db.delete(TABLE_DOWNLOAD_SONG, TAG_SID + "=" + id, null);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error remove download", e);
        }
    }

    public void removeFromPlayList(String id, Boolean isOnline) {
        if (id == null){
            return;
        }
        try {
            String table = Boolean.FALSE.equals(isOnline) ? TABLE_PLAYLIST_SONG_OFFLINE : TABLE_PLAYLIST_SONG;
            db.delete(table, TAG_SID + "=" + id, null);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error remove PlayList", e);
        }
    }

    public void removePlayList(String pid, Boolean isOnline) {
        if (pid == null){
            return;
        }
        try {
            String tableName = Boolean.FALSE.equals(isOnline) ? TABLE_PLAYLIST_OFFLINE : TABLE_PLAYLIST;
            db.delete(tableName, TAG_ID + "=" + pid, null);
            removePlayListAllSongs(pid, isOnline);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error remove PlayList", e);
        }
    }

    private void removePlayListAllSongs(String pid, Boolean isOnline) {
        if (pid == null){
            return;
        }
        try {
            String tableName = Boolean.FALSE.equals(isOnline) ? TABLE_PLAYLIST_SONG_OFFLINE : TABLE_PLAYLIST_SONG;
            db.delete(tableName, TAG_PID + "=" + pid, null);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error remove PlayListAllSongs", e);
        }
    }

    @NonNull
    private Boolean checkRecent(String id, Boolean isOnline) {
        if ( id == null){
            return false;
        }
        String table = Boolean.FALSE.equals(isOnline) ? TABLE_RECENT_OFFLINE : TABLE_RECENT;
        try (Cursor cursor = db.query(table, columnsSong, TAG_SID + "=" + id,
                null, null, null, null)) {
            return cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @NonNull
    private Boolean checkPlaylist(String id, Boolean isOnline) {
        if ( id == null){
            return false;
        }
        String table = Boolean.FALSE.equals(isOnline) ? TABLE_PLAYLIST_SONG_OFFLINE : TABLE_PLAYLIST_SONG;
        try (Cursor cursor = db.query(table, columnsPlaylistSong, TAG_SID + "=" + id, null, null, null, null)) {
            return cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("Range")
    public Boolean checkDownload(String id) {
        if ( id == null){
            return false;
        }
        File root = new File(context.getExternalFilesDir("").getAbsolutePath() + "/temp");
        try (Cursor cursor = db.query(TABLE_DOWNLOAD_SONG, columnsDownloadSong,
                TAG_SID + "=" + id, null, null, null, null)) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                String filename = cursor.getString(cursor.getColumnIndex(TAG_TEMP_NAME));
                File file = new File(root, filename + ".mp3");
                return file.exists();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("Range")
    public String getRecentIDs(String limit) {
        StringBuilder filterIDs = new StringBuilder();
        if (limit == null){
            return filterIDs.toString();
        }

        try (Cursor cursor = db.query(TABLE_RECENT, new String[]{TAG_SID}, null,
                null, null, null, TAG_ID + " DESC", limit)) {
            if (cursor.moveToFirst()) {
                do {
                    if (filterIDs.length() > 0) {
                        filterIDs.append(",");
                    }
                    filterIDs.append(cursor.getString(cursor.getColumnIndex(TAG_SID)));
                } while (cursor.moveToNext());
            }
        }
        return filterIDs.toString();
    }

    @SuppressLint("Range")
    public List<ItemSong> loadDataRecent(Boolean isOnline, String limit) {
        List<ItemSong> arrayList = new ArrayList<>();
        String table = Boolean.FALSE.equals(isOnline) ? TABLE_RECENT_OFFLINE : TABLE_RECENT;

        Cursor cursor;
        if (Boolean.TRUE.equals(isOnline)) {
            cursor = db.query(table, columnsSong, null, null, null, null, TAG_ID + " DESC", limit);
        } else {
            cursor = db.query(table, columnsSong, null, null, null, null, TAG_ID + " DESC");
        }
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {

                String id = cursor.getString(cursor.getColumnIndex(TAG_SID));
                String artist = cursor.getString(cursor.getColumnIndex(TAG_ARTIST));
                String name = cursor.getString(cursor.getColumnIndex(TAG_TITLE)).replace("%27", "'");

                String desc = cursor.getString(cursor.getColumnIndex(TAG_DESC));
                String lyrics = cursor.getString(cursor.getColumnIndex(TAG_LYRICS));

                String url = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_URL)));
                String urlHigh = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_URL_HIGH)));
                String urlLow = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_URL_LOW)));
                String imageBig = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_IMAGE)));

                String avgRate = cursor.getString(cursor.getColumnIndex(TAG_AVG_RATE));
                String views = cursor.getString(cursor.getColumnIndex(TAG_VIEWS));
                String downloads = cursor.getString(cursor.getColumnIndex(TAG_DOWNLOADS));

                ItemSong objItem = new ItemSong(id, artist, url, urlHigh, urlLow,imageBig, name, desc, lyrics, avgRate, views, downloads, false);
                arrayList.add(objItem);

                cursor.moveToNext();
            }
        }
        cursor.close();
        return arrayList;
    }

    @SuppressLint("Range")
    public List<ItemSong> loadDataPlaylist(String pid, Boolean isOnline) {
        String table = Boolean.FALSE.equals(isOnline) ? TABLE_PLAYLIST_SONG_OFFLINE : TABLE_PLAYLIST_SONG;
        List<ItemSong> arrayList = new ArrayList<>();
        try (Cursor cursor = db.query(table, columnsPlaylistSong, TAG_PID + "=" + pid,
                null, null, null, "")) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(TAG_SID));
                    String artist = cursor.getString(cursor.getColumnIndex(TAG_ARTIST));
                    String name = cursor.getString(cursor.getColumnIndex(TAG_TITLE)).replace("%27", "'");

                    String desc = cursor.getString(cursor.getColumnIndex(TAG_DESC));
                    String lyrics = cursor.getString(cursor.getColumnIndex(TAG_LYRICS));

                    String url = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_URL)));
                    String urlHigh = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_URL_HIGH)));
                    String urlLow = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_URL_LOW)));

                    String imageBig = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_IMAGE)));

                    String avgRate = cursor.getString(cursor.getColumnIndex(TAG_AVG_RATE));
                    String views = cursor.getString(cursor.getColumnIndex(TAG_VIEWS));
                    String downloads = cursor.getString(cursor.getColumnIndex(TAG_DOWNLOADS));

                    ItemSong objItem = new ItemSong(id, artist, url, urlHigh, urlLow, imageBig,
                            name, desc, lyrics, avgRate, views, downloads, false);
                    arrayList.add(objItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error loadDataPlaylist", e);
        }
        return arrayList;
    }

    @SuppressLint("Range")
    public List<ItemSong> loadDataDownload() {
        List<ItemSong> arrayList = new ArrayList<>();
        try (Cursor cursor = db.query(TABLE_DOWNLOAD_SONG, columnsDownloadSong, null,
                null, null, null, "")) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(TAG_SID));
                    String artist = cursor.getString(cursor.getColumnIndex(TAG_ARTIST));
                    String name = cursor.getString(cursor.getColumnIndex(TAG_TITLE)).replace("%27", "'");

                    String desc = cursor.getString(cursor.getColumnIndex(TAG_DESC));
                    String lyrics = cursor.getString(cursor.getColumnIndex(TAG_LYRICS));

                    String imageBig = Uri.fromFile(new File(encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_IMAGE))))).toString();

                    String avgRate = cursor.getString(cursor.getColumnIndex(TAG_AVG_RATE));
                    String views = cursor.getString(cursor.getColumnIndex(TAG_VIEWS));
                    String downloads = cursor.getString(cursor.getColumnIndex(TAG_DOWNLOADS));
                    String tempName = cursor.getString(cursor.getColumnIndex(TAG_TEMP_NAME));

                    String url = context.getExternalFilesDir("").getAbsolutePath() + File.separator + "temp/" + tempName;

                    ItemSong objItem = new ItemSong(id, artist, url, url, url, imageBig, name, desc, lyrics, avgRate, views, downloads,false);
                    objItem.setTempName(tempName);
                    arrayList.add(objItem);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Error loading live", e);
        }
        return arrayList;
    }

    @SuppressLint("Range")
    public List<String> loadPlaylistImages(String pid, Boolean isOnline) {
        List<String> arrayList = new ArrayList<>();
        String table = Boolean.FALSE.equals(isOnline) ? TABLE_PLAYLIST_SONG_OFFLINE : TABLE_PLAYLIST_SONG;
        Cursor cursor = db.query(table, new String[]{TAG_IMAGE}, TAG_PID + "=" + pid,
                null, null, null, "");
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < 4; i++) {
                try {
                    String imageSmall = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_IMAGE)));
                    arrayList.add(imageSmall);
                    cursor.moveToNext();
                } catch (Exception e) {
                    cursor.moveToFirst();
                    String imageSmall = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_IMAGE)));
                    arrayList.add(imageSmall);
                }
            }
            Collections.reverse(arrayList);
        } else {
            arrayList.add("1");
            arrayList.add("1");
            arrayList.add("1");
            arrayList.add("1");
        }
        cursor.close();
        return arrayList;
    }

    // About ---------------------------------------------------------------------------------------
    public void addToAbout() {
        try {
            db.delete(TABLE_ABOUT, null, null);

            ContentValues contentValues = new ContentValues();
            contentValues.put(TAG_ABOUT_EMAIL, Callback.getItemAbout().getEmail());
            contentValues.put(TAG_ABOUT_AUTHOR, Callback.getItemAbout().getAuthor());
            contentValues.put(TAG_ABOUT_CONTACT, Callback.getItemAbout().getContact());
            contentValues.put(TAG_ABOUT_WEBSITE, Callback.getItemAbout().getWebsite());
            contentValues.put(TAG_ABOUT_DESC, Callback.getItemAbout().getAppDesc());
            contentValues.put(TAG_ABOUT_DEVELOPED, Callback.getItemAbout().getDevelopedBY());
            contentValues.put(TAG_ABOUT_ENVATO_API_KEY, "");
            db.insert(TABLE_ABOUT, null, contentValues);
        } catch (Exception e) {
            Log.e(TAG, "Error add to about", e);
        }
    }

    @SuppressLint("Range")
    public Boolean getAbout() {
        try (Cursor c = db.query(TABLE_ABOUT, columnsAbout, null, null,
                null, null, null)) {
            if (c.moveToFirst()) {
                String email = c.getString(c.getColumnIndex(TAG_ABOUT_EMAIL));
                String author = c.getString(c.getColumnIndex(TAG_ABOUT_AUTHOR));
                String contact = c.getString(c.getColumnIndex(TAG_ABOUT_CONTACT));
                String website = c.getString(c.getColumnIndex(TAG_ABOUT_WEBSITE));
                String desc = c.getString(c.getColumnIndex(TAG_ABOUT_DESC));
                String developed = c.getString(c.getColumnIndex(TAG_ABOUT_DEVELOPED));
                String moreApps = c.getString(c.getColumnIndex(TAG_ABOUT_MORE_APP));

                Callback.setItemAbout(new ItemAbout(email, author, contact, website, desc, developed, moreApps));
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void close () {
        if (db != null && db.isOpen()) {
            db.close();
            super.close();
        }
    }
}