package nemosofts.tamilaudiopro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.ProgressDialog;
import androidx.nemosofts.material.SmoothCheckBox;
import androidx.nemosofts.material.Toasty;
import androidx.nemosofts.utils.NetworkUtils;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.executor.LoadLogin;
import nemosofts.tamilaudiopro.executor.LoadRegister;
import nemosofts.tamilaudiopro.interfaces.LoginListener;
import nemosofts.tamilaudiopro.interfaces.SocialLoginListener;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.helper.Helper;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;

public class SignInActivity extends AppCompatActivity {

    private String from = "";
    private Helper helper;
    private SPHelper sharedPref;
    private EditText etLoginEmail;
    private EditText etLoginPassword;
    private SmoothCheckBox rememberMe;
    private FirebaseAuth mAuth;
    private boolean isVisibility = false;
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

        mAuth = FirebaseAuth.getInstance();
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception e) {
            Log.e("SignInActivity", "Error FirebaseAuth signOut", e);
        }

        from = getIntent().getStringExtra("from");

        helper = new Helper(this);
        sharedPref = new SPHelper(this);

        progressDialog = new ProgressDialog(SignInActivity.this);

        rememberMe = findViewById(R.id.cb_remember_me);
        etLoginEmail = findViewById(R.id.et_login_email);
        etLoginPassword = findViewById(R.id.et_login_password);

        if(Boolean.TRUE.equals(sharedPref.getIsRemember())) {
            etLoginEmail.setText(sharedPref.getEmail());
            etLoginPassword.setText(sharedPref.getPassword());
        }

        onClickListener();
    }

    private void onClickListener() {
        findViewById(R.id.ll_checkbox).setOnClickListener(v -> rememberMe.setChecked(!rememberMe.isChecked()));
        findViewById(R.id.tv_login_btn).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.tv_login_signup).setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        findViewById(R.id.tv_skip_btn).setOnClickListener(view -> {
            sharedPref.setIsFirst(false);
            openMainActivity();
        });
        findViewById(R.id.tv_forgot_pass).setOnClickListener(view -> startActivity(new Intent(SignInActivity.this, ForgotPasswordActivity.class)));

        LinearLayout linearLayout = findViewById(R.id.ll_login_google);
        linearLayout.setVisibility(Boolean.TRUE.equals(new SPHelper(this).getIsGoogleLogin()) ? View.VISIBLE : View.GONE);
        linearLayout.setOnClickListener(view -> {
            if (NetworkUtils.isConnected(this)) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(SignInActivity.this, gso);

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                signInLauncher.launch(signInIntent);
            } else {
                Toasty.makeText(SignInActivity.this, getString(R.string.error_internet_not_connected), Toasty.ERROR);
            }
        });

        ImageView passVisibility = findViewById(R.id.iv_visibility);
        passVisibility.setImageResource(isVisibility ? R.drawable.ic_login_visibility : R.drawable.ic_login_visibility_off);
        passVisibility.setOnClickListener(v -> {
            isVisibility = !isVisibility;
            passVisibility.setImageResource(isVisibility ? R.drawable.ic_login_visibility : R.drawable.ic_login_visibility_off);
            etLoginPassword.setTransformationMethod(isVisibility ? HideReturnsTransformationMethod.getInstance()  : PasswordTransformationMethod.getInstance());
        });
    }

    // Register ActivityResultLauncher for Google Sign-In
    ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                firebaseAuthWithGoogle(task.getResult().getIdToken());
            } catch (Exception e) {
                Toasty.makeText(SignInActivity.this, getString(R.string.error_login_google), Toasty.ERROR);
            }
        } else {
            Toasty.makeText(SignInActivity.this, getString(R.string.error_login_google), Toasty.ERROR);
        }
    });

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null){
                    loadLoginSocial(user.getDisplayName(), user.getEmail(), user.getUid());
                } else {
                    Toasty.makeText(SignInActivity.this, "Failed to Sign IN", Toasty.ERROR);
                }
            } else {
                Toasty.makeText(SignInActivity.this, "Failed to Sign IN", Toasty.ERROR);
            }
        });
    }

    private void attemptLogin() {
        etLoginEmail.setError(null);
        etLoginPassword.setError(null);

        // Store values at the time of the login attempt.
        String email = etLoginEmail.getText().toString();
        String password = etLoginPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            etLoginPassword.setError(getString(R.string.error_cannot_empty));
            focusView = etLoginPassword;
            cancel = true;
        }
        if (etLoginPassword.getText().toString().endsWith(" ")) {
            etLoginPassword.setError(getString(R.string.error_pass_end_space));
            focusView = etLoginPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            etLoginEmail.setError(getString(R.string.error_cannot_empty));
            focusView = etLoginEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            etLoginEmail.setError(getString(R.string.error_invalid_email));
            focusView = etLoginEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            loadLogin();
        }
    }

    private boolean isEmailValid(@NonNull String email) {
        return email.contains("@") && !email.contains(" ");
    }

    private void loadLogin() {
        if (!NetworkUtils.isConnected(this)) {
            Toasty.makeText(SignInActivity.this, getString(R.string.error_internet_not_connected), Toasty.ERROR);
            return;
        }

        LoadLogin loadLogin = new LoadLogin(new LoginListener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, String loginSuccess, String message, String userID,
                              String userName, String userGender, String userPhone,String profile) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                if (!success.equals("1")) {
                    Toasty.makeText(SignInActivity.this, getString(R.string.error_server_not_connected), Toasty.ERROR);
                }

                if (loginSuccess.equals("1")) {
                    sharedPref.setLoginDetails(userID, userName, userPhone, etLoginEmail.getText().toString(),
                            userGender, profile, "", rememberMe.isChecked(),
                            etLoginPassword.getText().toString(), Method.LOGIN_TYPE_NORMAL
                    );
                    sharedPref.setIsFirst(false);
                    sharedPref.setIsLogged(true);
                    sharedPref.setIsAutoLogin(true);

                    Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();

                    if (from.equals("app")) {
                        finish();
                    } else {
                        openMainActivity();
                    }
                } else {
                    Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        }, helper.getAPIRequest(Method.METHOD_LOGIN, 0,"","","","",
                "","", etLoginEmail.getText().toString(),"","",
                etLoginPassword.getText().toString(),"", Method.LOGIN_TYPE_NORMAL,null));
        loadLogin.execute();
    }

    private void loadLoginSocial(final String name, String email, final String authId) {
        if (!NetworkUtils.isConnected(this)) {
            Toasty.makeText(SignInActivity.this, getString(R.string.error_internet_not_connected), Toasty.ERROR);
            return;
        }

        LoadRegister loadRegister = new LoadRegister(new SocialLoginListener() {
            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onEnd(String success, String registerSuccess, String message, String userID,
                              String userName, String email, String authId) {
                if (isFinishing()){
                    return;
                }
                progressDialog.dismiss();
                if (!success.equals("1")) {
                    Toasty.makeText(SignInActivity.this, getString(R.string.error_server_not_connected), Toasty.ERROR);
                    return;
                }

                if (registerSuccess.equals("1")) {
                    sharedPref.setLoginDetails(userID, userName, "", email, "", "",
                            authId, rememberMe.isChecked(), "", Method.LOGIN_TYPE_GOOGLE);
                    sharedPref.setIsFirst(false);
                    sharedPref.setIsLogged(true);
                    sharedPref.setIsAutoLogin(true);

                    Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();

                    if (from.equals("app")) {
                        finish();
                    } else {
                        openMainActivity();
                    }
                } else {
                    registerFailed(message);
                }
            }
        }, helper.getAPIRequest(Method.METHOD_REGISTER, 0, "", "", "",
                "", "", name, email, "", "", "", authId,
                Method.LOGIN_TYPE_GOOGLE, null));
        loadRegister.execute();
    }

    private void registerFailed(@NonNull String message) {
        if (message.contains("already") || message.contains("Invalid email format")) {
            etLoginEmail.setError(message);
            etLoginEmail.requestFocus();
        } else {
            Toasty.makeText(SignInActivity.this, message, Toasty.ERROR);
        }

        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception e) {
            Log.e("SignInActivity", "Error FirebaseAuth signOut", e);
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_sign_in;
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