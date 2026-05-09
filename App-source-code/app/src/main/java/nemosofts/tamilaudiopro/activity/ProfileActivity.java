package nemosofts.tamilaudiopro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.NetworkUtils;

import com.squareup.picasso.Picasso;

import com.saimum.saimummusic.BuildConfig;
import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.executor.LoadProfile;
import nemosofts.tamilaudiopro.executor.LoadStatus;
import nemosofts.tamilaudiopro.interfaces.ProfileListener;
import nemosofts.tamilaudiopro.interfaces.SuccessListener;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;

public class ProfileActivity extends AppCompatActivity {

    private Helper helper;
    private SPHelper sharedPref;
    private TextView name;
    private TextView email;
    private ImageView profile;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bg_page), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());

        helper = new Helper(this);
        sharedPref = new SPHelper(this);

        progressDialog = new ProgressDialog(ProfileActivity.this);

        name = findViewById(R.id.tv_profile_name);
        email = findViewById(R.id.tv_profile_email);
        profile = findViewById(R.id.iv_profile);

        if (sharedPref.isLogged() && !sharedPref.getUserId().isEmpty()) {
            loadUserProfile();
        } else {
            helper.clickLogin();
        }

        setupButton();

        LinearLayout adView = findViewById(R.id.ll_adView);
        helper.showBannerAd(adView,"");
    }

    private void setupButton() {
        findViewById(R.id.rl_profile).setOnClickListener(view -> startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class)));
        findViewById(R.id.iv_notifications).setOnClickListener(view -> startActivity(new Intent(ProfileActivity.this, NotificationActivity.class)));
        findViewById(R.id.ll_policy).setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, WebActivity.class);
            intent.putExtra("web_url", BuildConfig.BASE_URL+"privacy_policy.php");
            intent.putExtra("page_title", getResources().getString(R.string.privacy_policy));
            ActivityCompat.startActivity(ProfileActivity.this, intent, null);
        });
        findViewById(R.id.ll_terms).setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, WebActivity.class);
            intent.putExtra("web_url", BuildConfig.BASE_URL+"terms.php");
            intent.putExtra("page_title", getResources().getString(R.string.terms_and_conditions));
            ActivityCompat.startActivity(ProfileActivity.this, intent, null);
        });
        findViewById(R.id.ll_trash).setOnClickListener(view -> DialogUtil.trashDialog(this, new DialogUtil.DeleteListener() {
            @Override
            public void onDelete() {
                loadDelete();
            }

            @Override
            public void onCancel() {
                // no data
            }
        }));
        findViewById(R.id.ll_logout).setOnClickListener(view -> helper.clickLogin());
    }

    public void loadDelete() {
        if (NetworkUtils.isConnected(this)) {
            LoadStatus loadStatus = new LoadStatus(new SuccessListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String registerSuccess, String message) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        helper.clickLogin();
                    } else {
                        Toasty.makeText(ProfileActivity.this, getString(R.string.error_server_not_connected), Toasty.ERROR);
                    }
                }
            }, helper.getAPIRequest(Method.METHOD_ACCOUNT_DELETE, 0, "", "", "", "",
                    sharedPref.getUserId(), "", "", "", "", "", "", "", null));
            loadStatus.execute();
        } else {
            Toasty.makeText(ProfileActivity.this, getString(R.string.error_internet_not_connected), Toasty.ERROR);
        }
    }

    private void loadUserProfile() {
        if (!NetworkUtils.isConnected(this)) {
            Toasty.makeText(ProfileActivity.this, getString(R.string.error_internet_not_connected), Toasty.ERROR);
            return;
        }
        LoadProfile loadProfile = new LoadProfile(new ProfileListener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, String isApiSuccess, String message, String userID,
                              String userName, String email, String mobile, String gender, String profile) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                if (success.equals("1")) {
                    if (isApiSuccess.equals("1")) {
                        sharedPref.setUserName(userName);
                        sharedPref.setEmail(email);
                        sharedPref.setUserMobile(mobile);
                        sharedPref.setProfileImages(profile);
                        setVariables();
                    } else {
                        helper.logout(ProfileActivity.this, sharedPref);
                    }
                } else {
                    Toasty.makeText(ProfileActivity.this, getString(R.string.error_server_not_connected), Toasty.ERROR);
                }
            }
        },helper.getAPIRequest(Method.METHOD_PROFILE, 0, "", "", "", "",
                sharedPref.getUserId(), "", "", "", "", "", "", "", null));
        loadProfile.execute();
    }

    public void setVariables() {
        name.setText(sharedPref.getUserName());
        email.setText(sharedPref.getEmail());
        if (!sharedPref.getProfileImages().isEmpty()){
            findViewById(R.id.pb_iv_profile).setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(sharedPref.getProfileImages())
                    .placeholder(R.drawable.user_photo)
                    .error(R.drawable.user_photo)
                    .into(profile, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            findViewById(R.id.pb_iv_profile).setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            findViewById(R.id.pb_iv_profile).setVisibility(View.GONE);
                        }
                    });
        }
    }

    @Override
    public void onResume() {
        if (Boolean.TRUE.equals(Callback.getIsProfileUpdate())) {
            Callback.setIsProfileUpdate(false);
            loadUserProfile();
        }
        super.onResume();
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_profile;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }
}