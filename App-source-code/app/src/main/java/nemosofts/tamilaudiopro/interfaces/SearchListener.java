package nemosofts.tamilaudiopro.interfaces;

import java.util.ArrayList;

import nemosofts.tamilaudiopro.item.ItemPost;

public interface SearchListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemPost> arrayListPosts);
}
