package nemosofts.tamilaudiopro.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ImageHelperView;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.NetworkUtils;

import java.io.File;
import java.io.IOException;

import com.saimum.saimummusic.BuildConfig;
import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.executor.LoadRegister;
import nemosofts.tamilaudiopro.interfaces.SocialLoginListener;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.MediaPath;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import okhttp3.RequestBody;

public class SignUpActivity extends AppCompatActivity {

    private Helper helper;
    private EditText etEmail;
    private EditText etFullName;
    private EditText etTelephone;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private String gender = "";
    private ImageHelperView ivProfile;
    private String imagePath = "";
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

        helper = new Helper(this);

        progressDialog = new ProgressDialog(SignUpActivity.this);

        etEmail = findViewById(R.id.et_register_email);
        etFullName = findViewById(R.id.et_register_full_name);
        etTelephone = findViewById(R.id.et_register_telephone);
        etPassword = findViewById(R.id.et_register_password);
        etConfirmPassword = findViewById(R.id.et_register_confirm_password);
        ivProfile = findViewById(R.id.iv_profile_sign);

        onClickListener();
    }

    private void onClickListener() {
        findViewById(R.id.rd_male).setOnClickListener(view -> gender = "Male");
        findViewById(R.id.rd_female).setOnClickListener(view -> gender = "Female");
        findViewById(R.id.tv_login_signup).setOnClickListener(view -> finish());
        findViewById(R.id.tv_terms).setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, WebActivity.class);
            intent.putExtra("web_url", BuildConfig.BASE_URL+"data.php?terms");
            intent.putExtra("page_title", getResources().getString(R.string.terms_and_conditions));
            ActivityCompat.startActivity(SignUpActivity.this, intent, null);
        });
        findViewById(R.id.tv_privacy_policy).setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, WebActivity.class);
            intent.putExtra("web_url", BuildConfig.BASE_URL+"data.php?privacy_policy");
            intent.putExtra("page_title", getResources().getString(R.string.privacy_policy));
            ActivityCompat.startActivity(SignUpActivity.this, intent, null);
        });
        findViewById(R.id.rl_sign_up_pro).setOnClickListener(v -> pickImage());
        findViewById(R.id.btn_register).setOnClickListener(view -> {
            if (validate()) {
                loadRegister();
            }
        });
    }

    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            pickImageLauncher.launch(intent);
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            pickImageLauncher.launch(Intent.createChooser(intent, getString(R.string.select_image)));
        }
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    imagePath = MediaPath.getPathImage(this, imageUri);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        ivProfile.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(SignUpActivity.this, "Error pick Image.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @NonNull
    private Boolean validate() {
        if (etEmail.getText().toString().trim().isEmpty()) {
            etEmail.setError(getResources().getString(R.string.error_email));
            etEmail.requestFocus();
            return false;
        } else if (!isEmailValid(etEmail.getText().toString())) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return false;
        } else if (etFullName.getText().toString().trim().isEmpty()) {
            etFullName.setError(getResources().getString(R.string.error_name));
            etFullName.requestFocus();
            return false;
        } else if (etTelephone.getText().toString().trim().isEmpty()) {
            etTelephone.setError(getResources().getString(R.string.error_phone));
            etTelephone.requestFocus();
            return false;
        } else if (etPassword.getText().toString().isEmpty()) {
            etPassword.setError(getResources().getString(R.string.error_password));
            etPassword.requestFocus();
            return false;
        } else if (etPassword.getText().toString().endsWith(" ")) {
            etPassword.setError(getResources().getString(R.string.error_pass_end_space));
            etPassword.requestFocus();
            return false;
        } else if (etConfirmPassword.getText().toString().isEmpty()) {
            etConfirmPassword.setError(getResources().getString(R.string.error_cpassword));
            etConfirmPassword.requestFocus();
            return false;
        } else if (!etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            etConfirmPassword.setError(getResources().getString(R.string.error_pass_not_match));
            etConfirmPassword.requestFocus();
            return false;
        } else if (gender.isEmpty()) {
            Toasty.makeText(SignUpActivity.this, getResources().getString(R.string.error_gender), Toasty.ERROR);
            return false;
        } else {
            return true;
        }
    }

    private boolean isEmailValid(@NonNull String email) {
        return email.contains("@") && !email.contains(" ");
    }

    private void loadRegister() {
        if (!NetworkUtils.isConnected(this)) {
            Toasty.makeText(SignUpActivity.this, getResources().getString(R.string.error_internet_not_connected), Toasty.ERROR);
            return;
        }

        RequestBody requestBody;
        if (imagePath.isEmpty()){
            requestBody = helper.getAPIRequest(
                    Method.METHOD_REGISTER,0,"","","","", "",
                    etFullName.getText().toString(),etEmail.getText().toString(),
                    etTelephone.getText().toString(),gender,
                    etConfirmPassword.getText().toString(),"", Method.LOGIN_TYPE_NORMAL, null);
        } else {
            requestBody = helper.getAPIRequest(
                    Method.METHOD_REGISTER,0,"","", "","","",
                    etFullName.getText().toString(),etEmail.getText().toString(),etTelephone.getText().toString(),gender,
                    etConfirmPassword.getText().toString(),"", Method.LOGIN_TYPE_NORMAL,new File(imagePath));
        }
        LoadRegister loadRegister = new LoadRegister(new SocialLoginListener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, String registerSuccess, String message, String userID,
                              String userName, String email, String authID) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                handleRegister(success, registerSuccess, message);
            }
        },requestBody);
        loadRegister.execute();
    }

    private void handleRegister(@NonNull String success, String registerSuccess, String message) {
        if (!success.equals("1")) {
            Toasty.makeText(SignUpActivity.this, getResources().getString(R.string.error_server_not_connected), Toasty.ERROR);
            return;
        }
        switch (registerSuccess) {
            case "1":
                Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("from", "");
                startActivity(intent);
                finish();
                break;
            case "-1":
                DialogUtil.verifyDialog(SignUpActivity.this, getString(R.string.error_unauthorized_access), message, () -> {
                });
                break;
            default:
                if (message.contains("already") || message.contains("Invalid email format")) {
                    etEmail.setError(message);
                    etEmail.requestFocus();
                } else {
                    Toasty.makeText(SignUpActivity.this, message, Toasty.ERROR);
                }
                break;
        }
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_sign_up;
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