package nemosofts.tamilaudiopro.interfaces;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.item.ItemServerPlayList;

public interface ServerPlaylistListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message,
               ArrayList<ItemServerPlayList> arrayList);
}