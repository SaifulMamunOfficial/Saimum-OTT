package nemosofts.tamilaudiopro.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.theme.ThemeEngine;
import androidx.nemosofts.material.ToggleView;
import androidx.nemosofts.utils.NetworkUtils;

import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.executor.LoadAbout;
import nemosofts.tamilaudiopro.fragment.FragmentDBPlaylist;
import nemosofts.tamilaudiopro.fragment.FragmentDashBoard;
import nemosofts.tamilaudiopro.fragment.FragmentDownloads;
import nemosofts.tamilaudiopro.fragment.online.FragmentAlbums;
import nemosofts.tamilaudiopro.fragment.online.FragmentAllSongs;
import nemosofts.tamilaudiopro.fragment.online.FragmentArtist;
import nemosofts.tamilaudiopro.fragment.online.FragmentCategories;
import nemosofts.tamilaudiopro.fragment.online.FragmentRecentSongs;
import nemosofts.tamilaudiopro.fragment.online.FragmentServerPlaylist;
import nemosofts.tamilaudiopro.interfaces.AboutListener;
import nemosofts.tamilaudiopro.utils.advertising.AdManagerInterAdmob;
import nemosofts.tamilaudiopro.utils.advertising.AdManagerInterApplovin;
import nemosofts.tamilaudiopro.utils.advertising.AdManagerInterStartApp;
import nemosofts.tamilaudiopro.utils.advertising.AdManagerInterUnity;
import nemosofts.tamilaudiopro.utils.advertising.AdManagerInterWortise;
import nemosofts.tamilaudiopro.utils.advertising.AdManagerInterYandex;
import nemosofts.tamilaudiopro.utils.advertising.GDPRChecker;
import nemosofts.tamilaudiopro.utils.advertising.RewardAdAdmob;
import nemosofts.tamilaudiopro.utils.advertising.RewardAdApplovin;
import nemosofts.tamilaudiopro.utils.advertising.RewardAdStartApp;
import nemosofts.tamilaudiopro.utils.advertising.RewardAdUnity;
import nemosofts.tamilaudiopro.utils.advertising.RewardAdWortise;
import nemosofts.tamilaudiopro.utils.helper.Helper;

public class MainActivity extends NSoftsPlayerActivity implements NavigationView.OnNavigationItemSelectedListener {

    ThemeEngine themeEngine;
    FragmentManager fm;
    MenuItem menuLogin;
    MenuItem menuProfile;
    MenuItem menuSubscription;
    ReviewManager manager;
    ReviewInfo reviewInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.content_main, contentFrameLayout);

        Callback.setIsAppOpen(true);
        helper = new Helper(this);
        themeEngine = new ThemeEngine(this);

        fm = getSupportFragmentManager();

        navigationView.setNavigationItemSelectedListener(this);

        if (getSupportActionBar() != null) {
            if (Boolean.TRUE.equals(themeEngine.getIsThemeMode())) {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);
            } else {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black);
            }
        }

        Menu menu = navigationView.getMenu();
        menuLogin = menu.findItem(R.id.nav_login);
        menuProfile = menu.findItem(R.id.nav_profile);
        menuSubscription = menu.findItem(R.id.nav_subscription);

        new GDPRChecker(MainActivity.this).check();

        changeLoginName();
        loadAboutData();

        manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reviewInfo = task.getResult();
            }
        });

        setNavMenu();
        loadDashboardFrag();

        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
        OneSignal.getNotifications().requestPermission(false, Continue.none());

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleOnBack();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void setNavMenu() {
        navHome.setOnClickListener(view -> {
            if (!navHome.isActive()){
                pageChange(0);
            }
            bottomNavigationView(0);
        });
        navCategories.setOnClickListener(view -> {
            if (!navCategories.isActive()){
                pageChange(1);
            }
            bottomNavigationView(1);
        });
        navArtist.setOnClickListener(view -> {
            if (!navArtist.isActive()){
                pageChange(2);
            }
            bottomNavigationView(2);
        });
        navAlbums.setOnClickListener(view -> {
            if (!navAlbums.isActive()){
                pageChange(3);
            }
            bottomNavigationView(3);
        });
        navRecently.setOnClickListener(view -> {
            if (!navRecently.isActive()){
                pageChange(4);
            }
            bottomNavigationView(4);
        });

    }

    public void bottomNavigationView(int pos) {
        if (navHome == null || navCategories == null || navArtist == null || navAlbums == null || navRecently == null){
            return;
        }

        // List of navigation items
        ToggleView[] navItems = {navHome, navCategories, navArtist, navAlbums, navRecently};

        // Special handling for pos == 5
        if (pos == 5) {
            deactivateAll(navItems);
            return;
        }

        for (int i = 0; i < navItems.length; i++) {
            if (i == pos) {
                if (!navItems[i].isActive()) {
                    navItems[i].activate();
                    navItems[i].setBadgeText("");
                }
            } else {
                if (navItems[i].isActive()) {
                    navItems[i].deactivate();
                    navItems[i].setBadgeText(null);
                }
            }
        }
    }

    private void deactivateAll(ToggleView[] navItems) {
        if (navItems == null){
            return;
        }
        for (ToggleView navItem : navItems) {
            if (navItem.isActive()) {
                navItem.deactivate();
                navItem.setBadgeText(null);
            }
        }
    }

    private void loadDashboardFrag() {
        FragmentDashBoard f1 = new FragmentDashBoard();
        loadFrag(f1, getResources().getString(R.string.dashboard), fm);
        navigationView.setCheckedItem(R.id.nav_home);
    }

    public void loadFrag(Fragment f1, String name, FragmentManager fm) {
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStackImmediate();
        }

        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (!name.equals(getString(R.string.dashboard))) {
            ft.hide(fm.getFragments().get(fm.getBackStackEntryCount()));
            ft.add(R.id.fragment, f1, name);
            ft.addToBackStack(name);
        } else {
            ft.replace(R.id.fragment, f1, name);
        }
        ft.commit();

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(name);
        }

        if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    private void changeLoginName() {
        if (menuLogin != null) {
            menuSubscription.setVisible(true);
            if (spHelper.isLogged()) {
                menuProfile.setVisible(true);
                menuLogin.setTitle(getResources().getString(R.string.logout));
                menuLogin.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_logout));
                if (spHelper.getIsSubscribed()){
                    menuSubscription.setVisible(false);
                }
            } else {
                menuProfile.setVisible(false);
                menuLogin.setTitle(getResources().getString(R.string.login));
                menuLogin.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_login));
            }
        }
    }

    public void loadAboutData() {
        if (NetworkUtils.isConnected(this)) {
            LoadAbout loadAbout = new LoadAbout(MainActivity.this, new AboutListener() {
                @Override
                public void onStart() {
                    // this method is empty
                }

                @Override
                public void onEnd(String success, String verifyStatus, String message) {
                    if (isFinishing() && !success.equals("1")) {
                        return;
                    }
                    dbHelper.addToAbout();
                    helper.initializeAds();
                    initAds();
                }
            });
            loadAbout.execute();
        } else {
            try {
                dbHelper.getAbout();
            } catch (Exception e) {
                Log.e("MainActivity", "Error getAbout", e);
            }
        }
    }

    private void initAds() {
        if (Boolean.TRUE.equals(Callback.getIsInterAd()) && (!spHelper.getIsSubscribed() || spHelper.getIsAdOn())) {
            switch (Callback.getAdNetwork()) {
                case Callback.AD_TYPE_ADMOB :
                    AdManagerInterAdmob adManagerInterAdmob = new AdManagerInterAdmob(getApplicationContext());
                    adManagerInterAdmob.createAd();
                    break;
                case Callback.AD_TYPE_STARTAPP :
                    AdManagerInterStartApp adManagerInterStartApp = new AdManagerInterStartApp(getApplicationContext());
                    adManagerInterStartApp.createAd();
                    break;
                case Callback.AD_TYPE_APPLOVIN :
                    AdManagerInterApplovin adManagerInterApplovin = new AdManagerInterApplovin(MainActivity.this);
                    adManagerInterApplovin.createAd();
                    break;
                case Callback.AD_TYPE_YANDEX :
                    AdManagerInterYandex adManagerInterYandex = new AdManagerInterYandex(MainActivity.this);
                    adManagerInterYandex.createAd();
                    break;
                case Callback.AD_TYPE_WORTISE :
                    AdManagerInterWortise adManagerInterWortise = new AdManagerInterWortise(MainActivity.this);
                    adManagerInterWortise.createAd();
                    break;
                case Callback.AD_TYPE_UNITY :
                    AdManagerInterUnity adManagerInterUnity = new AdManagerInterUnity();
                    adManagerInterUnity.createAd();
                    break;
                default:
                    break;
            }
        }
        if (Boolean.TRUE.equals(Callback.getIsRewardAd()) && (!spHelper.getIsSubscribed() || spHelper.getIsAdOn())) {
            switch (Callback.getAdNetwork()) {
                case Callback.AD_TYPE_ADMOB :
                    RewardAdAdmob rewardAdAdmob = new RewardAdAdmob(getApplicationContext());
                    rewardAdAdmob.createAd();
                    break;
                case Callback.AD_TYPE_STARTAPP :
                    RewardAdStartApp rewardAdStartApp = new RewardAdStartApp(getApplicationContext());
                    rewardAdStartApp.createAd();
                    break;
                case Callback.AD_TYPE_APPLOVIN :
                    RewardAdApplovin rewardAdApplovin = new RewardAdApplovin(MainActivity.this);
                    rewardAdApplovin.createAd();
                    break;
                case Callback.AD_TYPE_WORTISE :
                    RewardAdWortise rewardAdWortise = new RewardAdWortise(getApplicationContext());
                    rewardAdWortise.createAd();
                    break;
                case Callback.AD_TYPE_UNITY :
                    RewardAdUnity rewardAdUnity = new RewardAdUnity();
                    rewardAdUnity.createAd();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home){
            if (!navHome.isActive()){
                pageChange(0);
            }
            bottomNavigationView(0);
        } else if (id == R.id.nav_categories){
            if (!navCategories.isActive()){
                pageChange(1);
            }
            bottomNavigationView(1);
        } else if (id == R.id.nav_artist){
            if (!navArtist.isActive()){
                pageChange(2);
            }
            bottomNavigationView(2);
        } else if (id == R.id.nav_albums){
            if (!navAlbums.isActive()){
                pageChange(3);
            }
            bottomNavigationView(3);
        } else if (id == R.id.nav_recently){
            if (!navRecently.isActive()){
                pageChange(4);
            }
            bottomNavigationView(4);
        } else if (id == R.id.nav_latest){
            FragmentAllSongs latest = new FragmentAllSongs();
            loadFrag(latest, getString(R.string.all_songs), fm);
            bottomNavigationView(5);
        } else if (id == R.id.nav_playlist){
            FragmentServerPlaylist server = new FragmentServerPlaylist();
            loadFrag(server, getString(R.string.playlist), fm);
            bottomNavigationView(5);
        } else if (id == R.id.nav_myplaylist){
            FragmentDBPlaylist myPlay = new FragmentDBPlaylist();
            loadFrag(myPlay, getString(R.string.myplaylist), fm);
            bottomNavigationView(5);
        } else if (id == R.id.nav_music_library){
            startActivity(new Intent(MainActivity.this, OfflineMusicActivity.class));
        } else if (id == R.id.nav_fav){
            if (spHelper.isLogged()) {
                Intent intentFav = new Intent(MainActivity.this, AudioByIDActivity.class);
                intentFav.putExtra("type", getString(R.string.favourite));
                intentFav.putExtra("id", "");
                intentFav.putExtra("name", getString(R.string.favourite));
                startActivity(intentFav);
            } else {
                helper.clickLogin();
            }
        } else if (id == R.id.nav_downloads){
            if (checkPerAudio()) {
                FragmentDownloads download = new FragmentDownloads();
                loadFrag(download, getString(R.string.downloads), fm);
                bottomNavigationView(5);
            }
        } else if (id == R.id.nav_news){
            startActivity(new Intent(MainActivity.this, NewsActivity.class));
        } else if (id == R.id.nav_suggest){
            startActivity(new Intent(MainActivity.this, SuggestionActivity.class));
        } else if (id == R.id.nav_subscription){
            startActivity(new Intent(MainActivity.this, BillingSubscribeActivity.class));
        } else if (id == R.id.nav_profile){
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_settings){
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
        } else if (id == R.id.nav_login){
            helper.clickLogin();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {
        changeLoginName();
        if (Callback.isRecreate()) {
            Callback.setRecreate(false);
            recreate();
        }
        super.onResume();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onDestroy() {
        Callback.setIsAppOpen(false);
        if (PlayerService.exoPlayer != null && !PlayerService.exoPlayer.getPlayWhenReady()) {
            Intent intent = new Intent(getApplicationContext(), PlayerService.class);
            intent.setAction(PlayerService.ACTION_STOP);
            stopService(intent);
        }
        super.onDestroy();
    }

    @NonNull
    private Boolean checkPerAudio() {
        String permission;
        if (Build.VERSION.SDK_INT >= 33) {
            permission = READ_MEDIA_AUDIO;
        } else if (Build.VERSION.SDK_INT >= 29) {
            permission = READ_EXTERNAL_STORAGE;
        } else {
            permission = WRITE_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission);  // Request permission using the new API
            return false;
        }
        return true;
    }

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted ->{
                if (Boolean.TRUE.equals(isGranted)){
                    FragmentDownloads download = new FragmentDownloads();
                    loadFrag(download, getString(R.string.downloads), fm);
                    bottomNavigationView(5);
                }
                Toast.makeText(MainActivity.this, Boolean.TRUE.equals(isGranted)
                        ? "Permission granted"
                        : getResources().getString(R.string.error_cannot_use_features), Toast.LENGTH_SHORT
                ).show();
            }
    );

    private void handleOnBack() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }  else if (mLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (!navHome.isActive()) {
            String title = fm.getFragments().get(fm.getBackStackEntryCount() - 1).getTag();
            if (title != null) {

                // Custom class to hold both navigation ID and bottom navigation index
                class NavInfo {
                    final int navId;
                    final int bottomNavIndex;
                    NavInfo(int navId, int bottomNavIndex) {
                        this.navId = navId;
                        this.bottomNavIndex = bottomNavIndex;
                    }
                }

                // Map to hold titles and corresponding NavInfo
                Map<String, NavInfo> titleToNavInfoMap = new HashMap<>();

                // Initialize the map with titles and corresponding actions
                titleToNavInfoMap.put(getString(R.string.dashboard), new NavInfo(R.id.nav_home, 0));
                titleToNavInfoMap.put(getString(R.string.nav_home), new NavInfo(R.id.nav_home, 0));
                titleToNavInfoMap.put(getString(R.string.categories), new NavInfo(R.id.nav_categories, 1));
                titleToNavInfoMap.put(getString(R.string.artist), new NavInfo(R.id.nav_artist, 2));
                titleToNavInfoMap.put(getString(R.string.albums), new NavInfo(R.id.nav_albums, 3));
                titleToNavInfoMap.put(getString(R.string.recently), new NavInfo(R.id.nav_recently, 4));
                titleToNavInfoMap.put(getString(R.string.all_songs), new NavInfo(R.id.nav_latest, 5));
                titleToNavInfoMap.put(getString(R.string.playlist), new NavInfo(R.id.nav_playlist, 5));
                titleToNavInfoMap.put(getString(R.string.myplaylist), new NavInfo(R.id.nav_myplaylist, 5));
                titleToNavInfoMap.put(getString(R.string.downloads), new NavInfo(R.id.nav_downloads, 5));
                titleToNavInfoMap.put(getString(R.string.search), new NavInfo(R.id.nav_home, 5));

                // Update the navigation view and bottom navigation view if the title is in the map
                NavInfo navInfo = titleToNavInfoMap.get(title);
                if (navInfo != null) {
                    navigationView.setCheckedItem(navInfo.navId);
                    bottomNavigationView(navInfo.bottomNavIndex);
                    pageChange(navInfo.bottomNavIndex);
                }
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(Objects.equals(title, getString(R.string.dashboard)) ? getString(R.string.nav_home) : title);
            }
        } else if (reviewInfo != null){
            Task<Void> flow = manager.launchReviewFlow(MainActivity.this, reviewInfo);
            flow.addOnCompleteListener(task1 -> DialogUtil.exitDialog(MainActivity.this));
        } else {
            DialogUtil.exitDialog(MainActivity.this);
        }
    }

    private void pageChange(int bottomNavIndex) {
        if (bottomNavIndex == 0){
            FragmentDashBoard home = new FragmentDashBoard();
            loadFrag(home, getString(R.string.dashboard), fm);
        } else if (bottomNavIndex == 1){
            FragmentCategories categories = new FragmentCategories();
            loadFrag(categories, getString(R.string.categories), fm);
        } else if (bottomNavIndex == 2){
            FragmentArtist artist = new FragmentArtist();
            loadFrag(artist, getString(R.string.artist), fm);
        } else if (bottomNavIndex == 3){
            FragmentAlbums albums = new FragmentAlbums();
            loadFrag(albums, getString(R.string.albums), fm);
        } else if (bottomNavIndex == 4){
            FragmentRecentSongs recently = new FragmentRecentSongs();
            loadFrag(recently, getString(R.string.recently), fm);
        }
    }
}