package nemosofts.tamilaudiopro.interfaces;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.item.ItemSong;

public interface AudioListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemSong> arrayList);
}