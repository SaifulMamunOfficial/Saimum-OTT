package nemosofts.tamilaudiopro.interfaces;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.item.ItemRating;

public interface RatingPostListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message,
               ArrayList<ItemRating> arrayListRating);
}
