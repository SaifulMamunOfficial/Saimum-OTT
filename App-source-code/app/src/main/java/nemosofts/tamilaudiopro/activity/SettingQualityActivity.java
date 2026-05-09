package nemosofts.tamilaudiopro.activity;

import static android.view.WindowManager.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.WRAP_CONTENT;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;

public class SettingQualityActivity extends AppCompatActivity {

    private SPHelper spHelper;

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
        toolbar.setTitle(getResources().getString(R.string.settings));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(view -> finish());
        }

        spHelper = new SPHelper(this);

        findViewById(R.id.ll_audio_quality).setOnClickListener(view -> setAudioQuality(false));
        findViewById(R.id.ll_download_quality).setOnClickListener(view -> setAudioQuality(true));
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_setting_quality;
    }

    private void setAudioQuality(Boolean isDownload) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_quality);

        AtomicInteger quality = new AtomicInteger(spHelper.getAudioQuality(isDownload));
        RadioGroup rg =  dialog.findViewById(R.id.rg_quality);

        if (quality.get() == 1) {
            rg.check(R.id.rd_normal);
        }
        if (quality.get() == 2) {
            rg.check(R.id.rd_high);
        }
        if (quality.get() == 3) {
            rg.check(R.id.rd_low);
        }

        RadioButton high =  dialog.findViewById(R.id.rd_high);
        RadioButton normal =  dialog.findViewById(R.id.rd_normal);
        RadioButton low =  dialog.findViewById(R.id.rd_low);

        normal.setOnClickListener(view -> quality.set(1));
        low.setOnClickListener(view -> quality.set(3));
        high.setOnClickListener(view -> quality.set(2));

        dialog.findViewById(R.id.iv_quality_close).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.tv_no_btn_quality).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.tv_yes_btn_quality).setOnClickListener(view -> {
            spHelper.setAudioQuality(isDownload, quality.get());
            dialog.dismiss();
        });
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(MATCH_PARENT, WRAP_CONTENT);
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