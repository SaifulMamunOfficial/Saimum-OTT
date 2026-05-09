package nemosofts.tamilaudiopro.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.fragment.offline.FragmentOFAlbums;
import nemosofts.tamilaudiopro.fragment.offline.FragmentOFArtist;
import nemosofts.tamilaudiopro.fragment.offline.FragmentOFPlaylist;
import nemosofts.tamilaudiopro.fragment.offline.FragmentOFSongs;

public class OfflineMusicActivity extends NSoftsPlayerActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_offline_music, contentFrameLayout);

        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        bottomNav.setVisibility(View.INVISIBLE);
        adViewPlayer.setVisibility(View.VISIBLE);

        toolbar.setVisibility(View.GONE);
        Toolbar toolbarOff = findViewById(R.id.toolbar_offline);
        toolbarOff.setTitle(getString(R.string.music_library));
        setSupportActionBar(toolbarOff);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (Boolean.TRUE.equals(new ThemeEngine(this).getIsThemeMode())){
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backspace_white);
            } else {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backspace_black);
            }
        }

        helper.showBannerAd(adViewPlayer,"");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(5);

        tabLayout = findViewById(R.id.tabs);

        if (checkPermissionDownload()) {
            initTabs();
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initTabs() {
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return switch (position) {
                case 0 -> FragmentOFSongs.newInstance();
                case 1 -> FragmentOFPlaylist.newInstance();
                case 2 -> FragmentOFArtist.newInstance();
                default -> FragmentOFAlbums.newInstance();
            };
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    @NonNull
    private Boolean checkPermissionDownload() {
        String permission;
        if (Build.VERSION.SDK_INT >= 33) {
            permission = READ_MEDIA_AUDIO;
        } else if (Build.VERSION.SDK_INT >= 29) {
            permission = READ_EXTERNAL_STORAGE;
        } else {
            permission = WRITE_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(OfflineMusicActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission);  // Request permission using the new API
            return false;
        }
        return true;
    }

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted ->{
                if (Boolean.TRUE.equals(isGranted)){
                    initTabs();
                }
                Toast.makeText(OfflineMusicActivity.this, Boolean.TRUE.equals(isGranted) ? "Permission granted"
                        : getResources().getString(R.string.error_cannot_use_features), Toast.LENGTH_SHORT).show();
            }
    );
}
