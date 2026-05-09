package nemosofts.tamilaudiopro.activity;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;
import androidx.nemosofts.Application;
import androidx.nemosofts.theme.ThemeEngine;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.onesignal.OneSignal;

import com.saimum.saimummusic.BuildConfig;
import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.utils.advertising.AppOpenAdManager;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;

public class MyApplication extends Application implements ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    DBHelper dbHelper;
    private SPHelper spHelper;
    private AppOpenAdManager appOpenAdManager;
    private Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        this.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        appOpenAdManager = new AppOpenAdManager(getApplicationContext());

        spHelper = new SPHelper(getApplicationContext());

        FirebaseAnalytics.getInstance(getApplicationContext());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        try {
            dbHelper = new DBHelper(getApplicationContext());
            dbHelper.onCreate(dbHelper.getWritableDatabase());
            dbHelper.getAbout();
        } catch (Exception e) {
            Log.e("MyApplication", "Error db", e);
        }

        // OneSignal Initialization
        OneSignal.initWithContext(this, getString(R.string.onesignal_app_id));

        new Helper(getApplicationContext()).initializeAds();

        setThemeEngine();
    }

    @Override
    public String setProductID() {
        return "27341717";
    }

    @Override
    public String setApplicationID() {
        return BuildConfig.APPLICATION_ID;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        if (Callback.getIsOpenAd() && !Callback.getIsAppOpenAdShown()
                && (!spHelper.getIsSubscribed() || spHelper.getIsAdOn())) {
            appOpenAdManager.showAdIfAvailable(currentActivity);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        // no data
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (Boolean.FALSE.equals(appOpenAdManager.getIsShowingAd())) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // no data
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        // no data
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        // no data
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        // no data
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        // no data
    }

    public void loadAd(@NonNull Activity activity) {
        if (Callback.getIsOpenAd() && spHelper != null
                && (!spHelper.getIsSubscribed() || spHelper.getIsAdOn())) {
            appOpenAdManager.loadAd(activity);
        }
    }

    public AppOpenAdManager getAppOpenAdManager() {
        return appOpenAdManager;
    }

    private void setThemeEngine() {
        try {
            if (Boolean.TRUE.equals(new SPHelper(getApplicationContext()).getIsFirst())) {
                ThemeEngine themeEngine = new ThemeEngine(getApplicationContext());
                int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES){
                    if (themeEngine.getThemePage() != 2){
                        themeEngine.setThemeMode(true);
                        themeEngine.setThemePage(2);
                    }
                } else if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO && themeEngine.getThemePage() != 0){
                    themeEngine.setThemeMode(false);
                    themeEngine.setThemePage(0);
                }
            }
        } catch (Exception e) {
            Log.e("MyApplication", "Error set theme engine", e);
        }
    }
}
