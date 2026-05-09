package nemosofts.tamilaudiopro.interfaces;

public interface ClickDeleteListenerPlayList {
    void onClick(int position);
    void onItemZero();
    void onDelete(int pos, Exception exception, int deleteRequestUriR, int deleteRequestUriQ);
}
