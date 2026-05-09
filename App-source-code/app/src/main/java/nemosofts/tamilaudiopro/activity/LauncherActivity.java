package nemosofts.tamilaudiopro.activity;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.VerificationListener;
import androidx.nemosofts.VerificationTask;
import androidx.nemosofts.theme.ColorUtils;
import androidx.nemosofts.utils.NetworkUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.saimum.saimummusic.BuildConfig;
import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.executor.LoadAbout;
import nemosofts.tamilaudiopro.executor.LoadLogin;
import nemosofts.tamilaudiopro.interfaces.AboutListener;
import nemosofts.tamilaudiopro.interfaces.LoginListener;
import nemosofts.tamilaudiopro.utils.helper.DBHelper;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;
import nemosofts.tamilaudiopro.utils.purchases.BillingUpdate;

public class LauncherActivity extends AppCompatActivity implements VerificationListener {

    private Helper helper;
    private SPHelper spHelper;
    private DBHelper dbHelper;
    private ProgressBar pb;
    Application application;

    Animation CircleAnim;
    ImageView Circle1, CIrcle2, CIrcle3;

    private BillingUpdate billingUpdate;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        hideNavigationBarStatusBars();
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rl_splash), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        helper = new Helper(this);
        spHelper = new SPHelper(this);
        dbHelper = new DBHelper(this);

        Circle1 = findViewById(R.id.iv_splash_logoCircle1);
        CIrcle2 = findViewById(R.id.iv_splash_logoCircle2);
        CIrcle3 = findViewById(R.id.iv_splash_logoCircle3);

        CircleAnim = AnimationUtils.loadAnimation(this,R.anim.circle_splash01);

        Circle1.setAnimation(CircleAnim);
        CIrcle2.setAnimation(CircleAnim);
        CIrcle3.setAnimation(CircleAnim);

        pb = findViewById(R.id.pb_splash);

        findViewById(R.id.rl_splash).setBackgroundColor(ColorUtils.colorBg(this));

        if (PlayerService.getInstance() != null && Callback.getIsPlayed()) {
            Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        billingUpdate = new BillingUpdate(LauncherActivity.this, new BillingUpdate.Listener() {
            @Override
            public void onBillingServiceDisconnected() {
                if (isFinishing()){
                    return;
                }
                loadAboutData();
            }

            @Override
            public void onBillingSetupFinished(boolean isSubscribed) {
                if (isFinishing()){
                    return;
                }
                spHelper.setIsSubscribed(isSubscribed);
                loadAboutData();
            }
        });
    }

    private void loadAboutData() {
        if (!NetworkUtils.isConnected(this)) {
            if (Boolean.TRUE.equals(spHelper.getIsFirst())) {
                errorDialog(getString(R.string.error_internet_not_connected), getString(R.string.error_try_internet_connected));
                return;
            }
            try {
                dbHelper.getAbout();
                setSaveData();
            } catch (Exception e) {
                errorDialog(getString(R.string.error_internet_not_connected), getString(R.string.error_try_internet_connected));
            }
            return;
        }

        LoadAbout loadAbout = new LoadAbout(LauncherActivity.this, new AboutListener() {
            @Override
            public void onStart() {
                pb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onEnd(String success, String verifyStatus, String message){
                if (isFinishing()){
                    return;
                }
                pb.setVisibility(View.GONE);
                if (success.equals("1")){
                    if (!verifyStatus.equals("-1") && !verifyStatus.equals("-2")){
                        dbHelper.addToAbout();
                        setSaveData();
                    } else {
                        errorDialog(getString(R.string.error_unauthorized_access), message);
                    }
                } else {
                    errorDialog(getString(R.string.error_server), getString(R.string.error_server_not_connected));
                }
            }
        });
        loadAbout.execute();
    }

    private void setSaveData() {
        new VerificationTask(this, this).execute();
    }

    private void loadSettings() {
        if (Boolean.TRUE.equals(Callback.getIsAppUpdate()) && Callback.getAppNewVersion() != BuildConfig.VERSION_CODE){
            openDialogActivity(Callback.DIALOG_TYPE_UPDATE);
        } else if(Boolean.TRUE.equals(spHelper.getIsMaintenance())){
            openDialogActivity(Callback.DIALOG_TYPE_MAINTENANCE);
        } else {
            if (Boolean.TRUE.equals(spHelper.getIsFirst())) {
                if (Boolean.TRUE.equals(spHelper.getIsLogin())){
                    openSignInActivity();
                } else {
                    spHelper.setIsFirst(false);

                    application = getApplication();
                    ((MyApplication) application).loadAd(LauncherActivity.this);

                    openMainActivity();
                }
            } else {
                loadActivity();
            }
        }
    }

    private void loadActivity() {
        application = getApplication();
        ((MyApplication) application).loadAd(LauncherActivity.this);

        if (Boolean.FALSE.equals(spHelper.getIsAutoLogin())) {
            openMainActivity();
        } else {
            if (spHelper.getLoginType().equals(Method.LOGIN_TYPE_GOOGLE)) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    loadLogin(Method.LOGIN_TYPE_GOOGLE, spHelper.getAuthID());
                } else {
                    spHelper.setIsAutoLogin(false);
                    openMainActivity();
                }
            } else {
                loadLogin(Method.LOGIN_TYPE_NORMAL, "");
            }
        }
    }

    private void loadLogin(final String loginType, final String authID) {
        if (!NetworkUtils.isConnected(this)) {
            Toast.makeText(LauncherActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
            spHelper.setIsAutoLogin(false);
            openMainActivity();
            return;
        }
        LoadLogin loadLogin = new LoadLogin(new LoginListener() {
            @Override
            public void onStart() {
                pb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onEnd(String success, String loginSuccess, String message, String userID,
                              String userName, String userGender, String userPhone, String profile) {
                if (isFinishing()){
                    return;
                }
                pb.setVisibility(View.GONE);
                if (success.equals("1") && (!loginSuccess.equals("-1"))) {
                    spHelper.setLoginDetails(userID, userName, userPhone, spHelper.getEmail(), userGender,
                            profile, authID, spHelper.getIsRemember(), spHelper.getPassword(), loginType
                    );
                    spHelper.setIsLogged(true);
                }
                openMainActivity();
            }
        }, helper.getAPIRequest(Method.METHOD_LOGIN, 0,"","","",
                "","","",spHelper.getEmail(),"","",
                spHelper.getPassword(),authID,loginType,null));
        loadLogin.execute();
    }

    private void openMainActivity() {
        pb.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (!((MyApplication) application).getAppOpenAdManager().isAdAvailable()
                    || (Callback.getIsAppOpenAdShown()
                    && !((MyApplication) application).getAppOpenAdManager().getIsShowingAd())) {
                intent = new Intent(LauncherActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else if(((MyApplication) application).getAppOpenAdManager().isAdAvailable()
                    && !((MyApplication) application).getAppOpenAdManager().getIsShowingAd()) {
                ((MyApplication) application).getAppOpenAdManager().showAdIfAvailable(LauncherActivity.this);
            }
        }, 5500);
    }

    private void openSignInActivity() {
        Intent intent = new Intent(LauncherActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", "");
        startActivity(intent);
        finish();
    }

    private void openDialogActivity(String type) {
        Intent intent = new Intent(LauncherActivity.this, DialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("from", type);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStartPairing() {
        pb.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnected() {
        pb.setVisibility(View.GONE);
        loadSettings();
    }

    @Override
    public void onError(String message) {
        pb.setVisibility(View.GONE);
        if (message == null || message.isEmpty()){
            errorDialog(getString(R.string.error_unauthorized_access), message);
            return;
        }
        errorDialog(getString(R.string.error_server), getString(R.string.error_server_not_connected));
    }


    private void errorDialog(String title, String message) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(LauncherActivity.this, R.style.dialogTheme);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        if (title.equals(getString(R.string.error_internet_not_connected)) || title.equals(getString(R.string.error_server_not_connected))) {
            alertDialog.setNegativeButton(getString(R.string.retry), (dialog, which) -> loadAboutData());
        }
        alertDialog.setPositiveButton(getString(R.string.exit), (dialog, which) -> finish());
        alertDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (billingUpdate != null){
            billingUpdate.resume();
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (dbHelper != null){
                dbHelper.close();
            }
            if (billingUpdate != null) {
                billingUpdate.release();
            }
        } catch (Exception e) {
            Log.e("LauncherActivity", "Error in closing", e);
        }
        super.onDestroy();
    }

    public void hideNavigationBarStatusBars() {
        try {
            Window window = getWindow();
            View decorView = window.getDecorView();

            // Allow content to extend behind system bars
            WindowCompat.setDecorFitsSystemWindows(window, false);

            // Use compatible insets controller
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, decorView);

            // Hide status and navigation bars
            controller.hide(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());

            // Enable immersive sticky behavior (swipe to temporarily show bars)
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        } catch (Exception e) {
            Log.e("LauncherActivity", "Failed to hide Navigation Bar & Status Bar", e);
        }
    }
}