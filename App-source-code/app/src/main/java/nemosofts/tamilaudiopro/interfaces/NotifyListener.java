package nemosofts.tamilaudiopro.interfaces;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.item.ItemNotify;

public interface NotifyListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemNotify> notificationArrayList);
}