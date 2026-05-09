package nemosofts.tamilaudiopro.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.nemosofts.AppCompatActivity;
import androidx.nemosofts.material.Switcher;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;

public class SettingDriveModeActivity extends AppCompatActivity {

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

        Toolbar toolbar = findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.settings));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(view -> finish());
        }

        spHelper = new SPHelper(this);

        Switcher switchDriveColor = findViewById(R.id.switch_drive_color);
        switchDriveColor.setChecked(spHelper.isDriveColor());
        switchDriveColor.setOnCheckedChangeListener((view, isChecked) -> spHelper.setDriveColor(isChecked));

        Switcher switchSnowFall = findViewById(R.id.switch_drive_snow_fall);
        switchSnowFall.setChecked(spHelper.isDriveSnowFall());
        switchSnowFall.setOnCheckedChangeListener((view, isChecked) -> spHelper.setDriveSnowFall(isChecked));

        Switcher switchKeepScreen = findViewById(R.id.switch_keep_screen);
        switchKeepScreen.setChecked(spHelper.isDriveKeepScreen());
        switchKeepScreen.setOnCheckedChangeListener((view, isChecked) -> spHelper.setDriveKeepScreen(isChecked));

        setBlur();
    }

    private void setBlur() {
        TextView blurText = findViewById(R.id.tv_blur);
        SeekBar sbBlur = findViewById(R.id.sb_blur);
        sbBlur.setMax(15);
        sbBlur.setProgress(spHelper.getBlurAmountDrive());
        blurText.setText(String.valueOf(spHelper.getBlurAmountDrive()));
        sbBlur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                blurText.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // this method is empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                spHelper.setBlurAmountDrive(progress);
                blurText.setText(String.valueOf(progress));
            }
        });
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_setting_drive_mode;
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