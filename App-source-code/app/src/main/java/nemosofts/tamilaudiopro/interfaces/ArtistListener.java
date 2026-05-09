package nemosofts.tamilaudiopro.interfaces;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.item.ItemArtist;

public interface ArtistListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemArtist> arrayList);
}