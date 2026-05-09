package nemosofts.tamilaudiopro.interfaces;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.item.ItemPost;

public interface HomeListener {
    void onStart();
    void onEnd(String success, String message, ArrayList<ItemPost> arrayListPost);
}
