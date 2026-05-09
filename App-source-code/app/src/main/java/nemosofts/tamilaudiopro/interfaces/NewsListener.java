package nemosofts.tamilaudiopro.interfaces;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.item.ItemNews;

public interface NewsListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemNews> arrayList);
}
