package nemosofts.tamilaudiopro.interfaces;

public interface SuccessListener {
    void onStart();
    void onEnd(String success, String registerSuccess, String message);
}