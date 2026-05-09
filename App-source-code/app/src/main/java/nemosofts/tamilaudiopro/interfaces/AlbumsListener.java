package nemosofts.tamilaudiopro.interfaces;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.item.ItemAlbums;

public interface AlbumsListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message,
               ArrayList<ItemAlbums> arrayListAlbums);
}