package nemosofts.tamilaudiopro.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.NetworkUtils;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.executor.LoadStatus;
import nemosofts.tamilaudiopro.interfaces.SuccessListener;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Helper helper;
    private EditText etEmail;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IfSupported.isRTL(this);
        IfSupported.isScreenshot(this);
        IfSupported.keepScreenOn(this);
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

        progressDialog = new ProgressDialog(ForgotPasswordActivity.this);

        etEmail = findViewById(R.id.et_forgot_email);

        findViewById(R.id.ll_btn_forgot_send).setOnClickListener(v -> {
            if (!etEmail.getText().toString().trim().isEmpty()) {
                loadForgotPass();
            } else {
                Toasty.makeText(ForgotPasswordActivity.this, getString(R.string.error_email), Toasty.ERROR);
            }
        });

        LinearLayout adView = findViewById(R.id.ll_adView);
        helper.showBannerAd(adView,"");
    }

    private void loadForgotPass() {
        if (!NetworkUtils.isConnected(this)) {
            Toasty.makeText(ForgotPasswordActivity.this, getString(R.string.error_internet_not_connected), Toasty.ERROR);
            return;
        }
        LoadStatus loadForgotPass = new LoadStatus(new SuccessListener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, String registerSuccess, String message) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                if (success.equals("1")) {
                    DialogUtil.verifyDialog(ForgotPasswordActivity.this, getString(R.string.app_name), message, () -> etEmail.setText(""));
                    Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toasty.makeText(ForgotPasswordActivity.this, getString(R.string.error_server_not_connected), Toasty.ERROR);
                }
            }
        }, helper.getAPIRequest(Method.METHOD_FORGOT_PASSWORD, 0, "", "",
                "", "", "", "", etEmail.getText().toString(),
                "", "", "", "", "", null));
        loadForgotPass.execute();
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_forgot_password;
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