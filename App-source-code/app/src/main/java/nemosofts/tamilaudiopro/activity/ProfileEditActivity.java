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
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ImageHelperView;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.NetworkUtils;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.executor.LoadStatus;
import nemosofts.tamilaudiopro.interfaces.SuccessListener;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.MediaPath;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;

public class ProfileEditActivity extends AppCompatActivity {

    private Helper helper;
    private SPHelper sharedPref;
    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPass;
    private EditText etcPass;
    private ImageHelperView profile;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> finish());

        progressDialog = new ProgressDialog(ProfileEditActivity.this);

        helper = new Helper(this);
        sharedPref = new SPHelper(this);

        etName = findViewById(R.id.editText_profedit_name);
        etEmail = findViewById(R.id.editText_profedit_email);
        etPhone = findViewById(R.id.editText_profedit_phone);
        etPass = findViewById(R.id.editText_profedit_password);
        etcPass = findViewById(R.id.editText_profedit_cpassword);

        if(sharedPref.getLoginType().equals(Method.LOGIN_TYPE_NORMAL)) {
            etcPass.setEnabled(true);
            etPass.setEnabled(true);
        } else {
            etcPass.setEnabled(false);
            etPass.setEnabled(false);
        }

        profile = findViewById(R.id.iv_profile_edit);
        Picasso.get()
                .load(sharedPref.getProfileImages())
                .placeholder(R.drawable.user_photo)
                .error(R.drawable.user_photo)
                .into(profile);

        findViewById(R.id.rl_profile_edit).setOnClickListener(v -> pickImage());
        findViewById(R.id.fab_save).setOnClickListener(view -> {
            if (validate()) {
                loadUpdateProfile();
            }
        });
        setProfileVar();
    }

    public void setProfileVar() {
        etName.setText(sharedPref.getUserName());
        etPhone.setText(sharedPref.getUserMobile());
        etEmail.setText(sharedPref.getEmail());
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
                        profile.setImageBitmap(bitmap);
                        uploadImage();
                    } catch (IOException e) {
                        Toast.makeText(ProfileEditActivity.this, "Error pick Image.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @NonNull
    private Boolean validate() {
        etName.setError(null);
        etEmail.setError(null);
        etcPass.setError(null);
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError(getString(R.string.error_cannot_empty));
            etName.requestFocus();
            return false;
        } else if (etEmail.getText().toString().trim().isEmpty()) {
            etEmail.setError(getString(R.string.error_password));
            etEmail.requestFocus();
            return false;
        } else if (etPass.getText().toString().endsWith(" ")) {
            etPass.setError(getString(R.string.error_pass_end_space));
            etPass.requestFocus();
            return false;
        } else if (!etPass.getText().toString().trim().equals(etcPass.getText().toString().trim())) {
            etcPass.setError(getString(R.string.error_password));
            etcPass.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void loadUpdateProfile() {
        if (!NetworkUtils.isConnected(this)) {
            Toasty.makeText(ProfileEditActivity.this, getString(R.string.error_internet_not_connected), Toasty.ERROR);
            return;
        }
        LoadStatus loadProfileEdit = new LoadStatus(new SuccessListener() {
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
                    if (registerSuccess.equals("1")) {
                        updateArray();
                        Callback.setIsProfileUpdate(true);
                        finish();
                        Toast.makeText(ProfileEditActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        if (message.contains("Email address already used")) {
                            etEmail.setError(message);
                            etEmail.requestFocus();
                        }
                    }
                } else {
                    Toasty.makeText(ProfileEditActivity.this, getString(R.string.error_server_not_connected), Toasty.ERROR);
                }
            }
        }, helper.getAPIRequest(Method.METHOD_EDIT_PROFILE, 0, "", "", "", "",
                sharedPref.getUserId(), etName.getText().toString(), etEmail.getText().toString(), etPhone.getText().toString(),
                "", etPass.getText().toString(), "", "", null));
        loadProfileEdit.execute();
    }

    private void updateArray() {
        sharedPref.setUserName(etName.getText().toString());
        sharedPref.setEmail(etEmail.getText().toString());
        sharedPref.setUserMobile(etPhone.getText().toString());
        if (!etPass.getText().toString().isEmpty()) {
            sharedPref.setRemember(false);
        }
    }

    public void uploadImage() {
        if (!NetworkUtils.isConnected(this)) {
            Toasty.makeText(ProfileEditActivity.this, getString(R.string.error_internet_not_connected), Toasty.ERROR);
            return;
        }
        LoadStatus loadStatus = new LoadStatus(new SuccessListener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, String status, String message) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                if (success.equals("1")) {
                    Callback.setIsProfileUpdate(true);
                    imagePath = "";
                    Toast.makeText(ProfileEditActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toasty.makeText(ProfileEditActivity.this, getString(R.string.error_server_not_connected), Toasty.ERROR);
                }
            }
        }, helper.getAPIRequest(Method.METHOD_USER_IMAGES_UPDATE, 0, "", "",
                "", "", sharedPref.getUserId(), "", "", "",
                "", "", "", "", new File(imagePath)));
        loadStatus.execute();
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_profile_edit;
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