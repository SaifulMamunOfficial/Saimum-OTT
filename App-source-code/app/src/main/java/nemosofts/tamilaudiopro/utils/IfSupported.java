package nemosofts.tamilaudiopro.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import nemosofts.tamilaudiopro.utils.helper.SPHelper;


public class IfSupported {

    private static final String TAG = "IfSupported";

    private IfSupported() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Applies Right-to-Left (RTL) layout direction if enabled in preferences.
     * <p>
     * Also hides system bars on Android R (API 30) and above when RTL is enabled.
     * </p>
     *
     * @param activity The activity to apply RTL direction to
     */
    public static void isRTL(Activity activity) {
        if (activity == null) {
            ApplicationUtil.log(TAG, "Activity context is null in isRTL");
            return;
        }
        try {
            if (Boolean.TRUE.equals(new SPHelper(activity).getIsRTL())) {
                Window window = activity.getWindow();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowInsetsController insetsController = window.getInsetsController();
                    if (insetsController != null) {
                        insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                        insetsController.hide(WindowInsets.Type.systemBars());
                    }
                }
                window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to apply RTL layout direction", e);
        }
    }

    /**
     * Enables screenshot prevention if configured in preferences.
     * <p>
     * Sets the FLAG_SECURE window flag to prevent screenshots and screen recording.
     * </p>
     *
     * @param mContext The activity context to apply screenshot prevention to
     */
    public static void isScreenshot(Activity mContext) {
        if (mContext == null) {
            ApplicationUtil.log(TAG, "Activity context is null isScreenshot");
            return;
        }
        try {
            if (Boolean.TRUE.equals(new SPHelper(mContext).getIsScreenshot())) {
                Window window = mContext.getWindow();
                window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to isScreenshot", e);
        }
    }

    /**
     * Keeps the screen on for the specified activity.
     * <p>
     * Sets the FLAG_KEEP_SCREEN_ON window flag to prevent the screen from dimming or turning off.
     * </p>
     *
     * @param mContext The activity to keep the screen on for
     */
    public static void keepScreenOn(Activity mContext) {
        if (mContext == null) {
            ApplicationUtil.log(TAG, "Activity context is null keepScreenOn");
            return;
        }
        try {
            Window window = mContext.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception e) {
            ApplicationUtil.log(TAG, "Failed to keep screen on", e);
        }
    }
}