package nemosofts.tamilaudiopro.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.theme.ColorUtils;

import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.dialog.DialogUtil;

public class DialogActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.rl_splash).setBackgroundColor(ColorUtils.colorBg(this));

        String from = getIntent().getStringExtra("from");
        switch (Objects.requireNonNull(from)) {
            case Callback.DIALOG_TYPE_UPDATE -> DialogUtil.upgradeDialog(this, this::openMainActivity);
            case Callback.DIALOG_TYPE_MAINTENANCE -> DialogUtil.maintenanceDialog(this);
            case Callback.DIALOG_TYPE_DEVELOPER -> DialogUtil.dModeDialog(this);
            case Callback.DIALOG_TYPE_VPN -> DialogUtil.vpnDialog(this);
            default -> openMainActivity();
        }
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_launcher;
    }

    private void openMainActivity() {
        Intent intent = new Intent(DialogActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}