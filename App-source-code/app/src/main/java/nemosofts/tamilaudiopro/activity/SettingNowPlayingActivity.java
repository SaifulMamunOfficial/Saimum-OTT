package nemosofts.tamilaudiopro.activity;

import static android.view.WindowManager.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.WRAP_CONTENT;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
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
import androidx.viewpager.widget.ViewPager;

import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.adapter.NowPlayingScreenAdapter;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.dialog.TextSizeDialog;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.IfSupported;
import nemosofts.tamilaudiopro.utils.NowPlayingScreen;
import nemosofts.tamilaudiopro.utils.helper.SPHelper;

public class SettingNowPlayingActivity extends AppCompatActivity {

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

        Switcher switchVolume = findViewById(R.id.switch_volume);
        switchVolume.setChecked(spHelper.isVolume());
        switchVolume.setOnCheckedChangeListener((view, isChecked) -> {
            spHelper.setVolume(isChecked);
            Callback.setRecreate(true);
        });

        Switcher switchSnowFall = findViewById(R.id.switch_snow_fall);
        switchSnowFall.setChecked(spHelper.isSnowFall());
        switchSnowFall.setOnCheckedChangeListener((view, isChecked) -> {
            spHelper.setSnowFall(isChecked);
            Callback.setRecreate(true);
        });

        findViewById(R.id.ll_now_playing).setOnClickListener(view -> openNowPlaying());
        findViewById(R.id.ll_text_size).setOnClickListener(view -> new TextSizeDialog(this).showDialog());

        setBlur();
    }

    private void setBlur() {
        TextView blurText = findViewById(R.id.tv_blur_now);
        SeekBar sbBlur = findViewById(R.id.sb_blur_now);
        sbBlur.setMax(80);
        sbBlur.setProgress(spHelper.getBlurAmount());
        blurText.setText(String.valueOf(spHelper.getBlurAmount()));
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
                spHelper.setBlurAmount(progress);
                blurText.setText(String.valueOf(progress));
                Callback.setRecreate(true);
            }
        });
    }

    private void openNowPlaying() {
        final int[] viewPagerPosition = new int[1];
        Dialog dialog = new Dialog(SettingNowPlayingActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_now_playing_screen);
        final ViewPager viewPager = dialog.findViewById(R.id.now_playing_screen_view_pager);
        viewPager.setAdapter(new NowPlayingScreenAdapter(this));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // this method is empty
            }

            @Override
            public void onPageSelected(int position) {
                viewPagerPosition[0] = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // this method is empty
            }
        });
        viewPager.setPageMargin((int) ApplicationUtil.convertDpToPixel(32, getResources()));
        viewPager.setCurrentItem(spHelper.getNowPlayingScreen().ordinal());
        dialog.findViewById(R.id.tv_submit_btn).setOnClickListener(view -> {
            NowPlayingScreen nowPlayingScreen = NowPlayingScreen.values()[viewPagerPosition[0]];
            spHelper.setNowPlayingScreen(nowPlayingScreen);
            dialog.dismiss();
            Callback.setNowPlayingScreen(spHelper.getNowPlayingScreen().ordinal());
            Callback.setRecreate(true);
        });
        dialog.findViewById(R.id.tv_cancel_btn).setOnClickListener(view -> dialog.dismiss());
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(MATCH_PARENT, WRAP_CONTENT);
    }

    @Override
    public int setContentViewID() {
        return R.layout.activity_setting_now_playing;
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