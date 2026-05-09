package nemosofts.tamilaudiopro.utils.helper;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.nemosofts.material.SmoothCheckBox;
import androidx.nemosofts.utils.EncrypterUtils;
import androidx.nemosofts.utils.NetworkUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.wortise.ads.WortiseSdk;
import com.wortise.ads.banner.BannerAd;
import com.wortise.ads.interstitial.InterstitialAd;
import com.wortise.ads.rewarded.RewardedAd;
import com.wortise.ads.rewarded.models.Reward;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.saimum.saimummusic.R;
import nemosofts.tamilaudiopro.activity.DownloadService;
import nemosofts.tamilaudiopro.activity.PlayerService;
import nemosofts.tamilaudiopro.activity.SignInActivity;
import nemosofts.tamilaudiopro.adapter.AdapterPlaylistDialog;
import nemosofts.tamilaudiopro.callback.Callback;
import nemosofts.tamilaudiopro.callback.Method;
import nemosofts.tamilaudiopro.dialog.DialogUtil;
import nemosofts.tamilaudiopro.interfaces.ClickListenerPlayList;
import nemosofts.tamilaudiopro.interfaces.InterAdListener;
import nemosofts.tamilaudiopro.interfaces.RewardAdListener;
import nemosofts.tamilaudiopro.item.ItemMyPlayList;
import nemosofts.tamilaudiopro.item.ItemSong;
import nemosofts.tamilaudiopro.utils.ApplicationUtil;
import nemosofts.tamilaudiopro.utils.AsyncTaskExecutor;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Helper {

    private static final String TAG = "Helper";
    private final Context ctx;
    private final DBHelper dbHelper;
    private InterAdListener interAdListener;
    boolean isRewarded = false;

    public Helper(Context ctx) {
        this.ctx = ctx;
        dbHelper = new DBHelper(ctx);
    }

    public Helper(Context ctx, InterAdListener interAdListener) {
        this.ctx = ctx;
        this.interAdListener = interAdListener;
        dbHelper = new DBHelper(ctx);
    }

    public RequestBody getAPIRequest(String helper, int page, String itemID, String catID,
                                     String searchText, String reportMessage, String userID,
                                     String name, String email, String mobile, String gender,
                                     String password, String authID, String loginType, File file) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd' 'HH:mm:ss").create();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(gson);
        jsObj.addProperty("helper_name", helper);
        jsObj.addProperty("application_id", ctx.getPackageName());

        final String TAG_USER_ID = "user_id";
        final String TAG_USER_EMAIL = "user_email";
        final String TAG_USER_PASSWORD = "user_password";

        final String TAG_POST_ID = "post_id";
        final String TAG_SEARCH_TEXT = "search_text";
        final String TAG_SEARCH_TYPE = "search_type";

        if (Method.METHOD_APP_DETAILS.equals(helper)){
            jsObj.addProperty(TAG_USER_ID, userID);
        } else if (Method.METHOD_LOGIN.equals(helper)){
            jsObj.addProperty(TAG_USER_EMAIL, email);
            jsObj.addProperty(TAG_USER_PASSWORD, password);
            jsObj.addProperty("auth_id", authID);
            jsObj.addProperty("type", loginType);
        } else if (Method.METHOD_REGISTER.equals(helper)){
            jsObj.addProperty("user_name", name);
            jsObj.addProperty(TAG_USER_EMAIL, email);
            jsObj.addProperty("user_phone", mobile);
            jsObj.addProperty("user_gender", gender);
            jsObj.addProperty(TAG_USER_PASSWORD, password);
            jsObj.addProperty("auth_id", authID);
            jsObj.addProperty("type", loginType);
        } else if (Method.METHOD_PROFILE.equals(helper)) {
            jsObj.addProperty(TAG_USER_ID, userID);
        } else if (Method.METHOD_ACCOUNT_DELETE.equals(helper)) {
            jsObj.addProperty(TAG_USER_ID, userID);
        } else if (Method.METHOD_EDIT_PROFILE.equals(helper)){
            jsObj.addProperty(TAG_USER_ID, userID);
            jsObj.addProperty("user_name", name);
            jsObj.addProperty(TAG_USER_EMAIL, email);
            jsObj.addProperty("user_phone", mobile);
            jsObj.addProperty(TAG_USER_PASSWORD, password);
        } else if (Method.METHOD_USER_IMAGES_UPDATE.equals(helper)){
            jsObj.addProperty(TAG_USER_ID, userID);
            jsObj.addProperty("type", loginType);
        } else if (Method.METHOD_FORGOT_PASSWORD.equals(helper)){
            jsObj.addProperty(TAG_USER_EMAIL, email);
        } else if (Method.METHOD_NOTIFICATION.equals(helper)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty(TAG_USER_ID, userID);
        } else if (Method.METHOD_REMOVE_NOTIFICATION.equals(helper)) {
            jsObj.addProperty(TAG_POST_ID, itemID);
            jsObj.addProperty(TAG_USER_ID, userID);
        } else if (Method.METHOD_REPORT.equals(helper)) {
            jsObj.addProperty(TAG_POST_ID, itemID);
            jsObj.addProperty(TAG_USER_ID, userID);
            jsObj.addProperty("report_title", searchText);
            jsObj.addProperty("report_msg", reportMessage);
        } else if (Method.METHOD_GET_RATINGS.equals(helper)) {
            jsObj.addProperty(TAG_POST_ID, itemID);
            jsObj.addProperty("device_id", userID);
        } else if (Method.METHOD_RATINGS.equals(helper)) {
            jsObj.addProperty(TAG_POST_ID, itemID);
            jsObj.addProperty("device_id", userID);
            jsObj.addProperty("rate", authID);
            jsObj.addProperty("message", reportMessage);
        } else if (Method.METHOD_RATINGS_POST.equals(helper)) {
            jsObj.addProperty(TAG_POST_ID, itemID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_HOME.equals(helper)) {
            jsObj.addProperty(TAG_USER_ID, userID);
            if(!itemID.isEmpty()) {
                jsObj.addProperty("songs_ids", itemID);
            }
        } else if (Method.METHOD_HOME_DETAILS.equals(helper)) {
            jsObj.addProperty("id", itemID);
            jsObj.addProperty(TAG_USER_ID, userID);
        } else if (Method.METHOD_SINGLE_SONG.equals(helper)) {
            jsObj.addProperty("song_id", itemID);
            jsObj.addProperty(TAG_USER_ID, userID);
        } else if (Method.METHOD_DOWNLOAD_COUNT.equals(helper)) {
            jsObj.addProperty("song_id", itemID);
        }  else if (Method.METHOD_FAV.equals(helper)) {
            jsObj.addProperty(TAG_POST_ID, itemID);
            jsObj.addProperty(TAG_USER_ID, userID);
            jsObj.addProperty("type", loginType);
        } else if (Method.METHOD_SONG_BY_RECENT.equals(helper)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("songs_ids", itemID);
            jsObj.addProperty(TAG_USER_ID, userID);
        } else if (Method.METHOD_SUGGESTION.equals(helper)) {
            jsObj.addProperty(TAG_USER_ID, userID);
            jsObj.addProperty("suggest_title", searchText);
            jsObj.addProperty("suggest_message", reportMessage);
        }  else if (Method.METHOD_NEWS.equals(helper)) {
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_SONG_BY_TRENDING.equals(helper)) {
            jsObj.addProperty(TAG_USER_ID, userID);
        } else if (Method.METHOD_SONG_ALL.equals(helper)) {
            jsObj.addProperty(TAG_USER_ID, userID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_ARTIST.equals(helper)
                || Method.METHOD_CAT.equals(helper)
                || Method.METHOD_SERVER_PLAYLIST.equals(helper)
                || Method.METHOD_ALBUMS.equals(helper)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty(TAG_SEARCH_TEXT, searchText);
            jsObj.addProperty(TAG_SEARCH_TYPE, loginType);
        } else if (Method.METHOD_SEARCH_AUDIO.equals(helper)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty(TAG_SEARCH_TEXT, searchText);
            jsObj.addProperty(TAG_SEARCH_TYPE, loginType);
            jsObj.addProperty(TAG_USER_ID, userID);
        } else if (Method.METHOD_SEARCH.equals(helper)) {
            jsObj.addProperty(TAG_SEARCH_TEXT, searchText);
            jsObj.addProperty(TAG_USER_ID, userID);
        } else if (Method.METHOD_ALBUMS_CAT_ID.equals(helper)) {
            jsObj.addProperty("cat_id", catID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_ALBUMS_ART_ID.equals(helper)) {
            jsObj.addProperty("artist_id", itemID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_SONG_BY_CAT.equals(helper)) {
            jsObj.addProperty("cat_id", catID);
            jsObj.addProperty(TAG_USER_ID, userID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_SONG_BY_ALBUMS.equals(helper)) {
            jsObj.addProperty("album_id", itemID);
            jsObj.addProperty(TAG_USER_ID, userID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_SONG_BY_ARTIST.equals(helper)) {
            jsObj.addProperty("artist_name", searchText);
            jsObj.addProperty(TAG_USER_ID, userID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_SONG_BY_PLAYLIST.equals(helper)) {
            jsObj.addProperty("playlist_id", itemID);
            jsObj.addProperty(TAG_USER_ID, userID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Method.METHOD_SONG_BY_BANNER.equals(helper)) {
            jsObj.addProperty("banner_id", itemID);
            jsObj.addProperty(TAG_USER_ID, userID);
            jsObj.addProperty("page", String.valueOf(page));
        }  else if (Method.METHOD_POST_BY_FAV.equals(helper)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty(TAG_USER_ID, userID);
            jsObj.addProperty("type", loginType);
        }

        if (helper.equals(Method.METHOD_REGISTER) || helper.equals(Method.METHOD_SUGGESTION) || helper.equals(Method.METHOD_USER_IMAGES_UPDATE)) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            if (file != null) {
                builder.addFormDataPart("image_data", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
            }
            return builder.addFormDataPart("data", EncrypterUtils.toBase64(jsObj.toString())).build();
        }
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data", EncrypterUtils.toBase64(jsObj.toString()))
                .build();
    }

    public void initializeAds() {
        try {
            if (Callback.getAdNetwork().equals(Callback.AD_TYPE_ADMOB) || Callback.getAdNetwork().equals(Callback.AD_TYPE_META)) {
                MobileAds.initialize(ctx, initializationStatus -> {
                });
            }
            if (Callback.getAdNetwork().equals(Callback.AD_TYPE_STARTAPP)) {
                StartAppSDK.init(ctx, Callback.getStartappAppID());
                StartAppAd.disableSplash();
                StartAppSDK.setUserConsent(ctx, "pas", System.currentTimeMillis(), new GDPRChecker(ctx).canLoadAd());
            }
            if (Callback.getAdNetwork().equals(Callback.AD_TYPE_APPLOVIN) && (!AppLovinSdk.getInstance(ctx).isInitialized())) {
                AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration.builder(ctx.getString(R.string.applovin_sdk_key))
                        .setMediationProvider(AppLovinMediationProvider.MAX)
                        .setTestDeviceAdvertisingIds(Arrays.asList("656822d9-18de-4120-994e-44d4245a4d63", "249d75a2-1ef2-8ff9-8885-c50384843a66"))
                        .build();

                // Initialize the SDK with the configuration
                AppLovinSdk.getInstance( ctx ).initialize(initConfig, sdkConfig -> {
                });
            }
            if (Callback.getAdNetwork().equals(Callback.AD_TYPE_IRONSOURCE)) {
                IronSource.init(ctx, Callback.getIronsourceAppKey(), () -> {
                });
            }
            if (Callback.getAdNetwork().equals(Callback.AD_TYPE_UNITY)) {
                UnityAds.initialize(ctx, Callback.getUnityGameID(), true, new IUnityAdsInitializationListener() {
                    @Override
                    public void onInitializationComplete() {
                        // document why this method is empty
                    }

                    @Override
                    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                        // document why this method is empty
                    }
                });
            }
            if (Callback.getAdNetwork().equals(Callback.AD_TYPE_YANDEX)) {
                com.yandex.mobile.ads.common.MobileAds.initialize(ctx, () -> {
                });
            }
            if (Callback.getAdNetwork().equals(Callback.AD_TYPE_WORTISE) && !WortiseSdk.isInitialized()) {
                WortiseSdk.initialize(ctx, Callback.getWortiseAppID());
            }
        } catch (Exception e) {
            ApplicationUtil.log(TAG,"Error ads load");
        }
    }

    public Object showBannerAd(LinearLayout linearLayout, String page) {
        if (isBannerAd(page)){
            switch (Callback.getAdNetwork()) {
                case Callback.AD_TYPE_ADMOB, Callback.AD_TYPE_META:
                    Bundle extras = new Bundle();
                    AdView adViewAdmob = new AdView(ctx);
                    AdRequest adRequest;
                    if (Callback.getAdNetwork().equals(Callback.AD_TYPE_ADMOB)) {
                        adRequest = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                .build();
                    } else {
                        adRequest = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                .addNetworkExtrasBundle(FacebookMediationAdapter.class, extras)
                                .build();
                    }
                    adViewAdmob.setAdUnitId(Callback.getAdmobBannerAdID());
                    adViewAdmob.setAdSize(AdSize.BANNER);
                    linearLayout.addView(adViewAdmob);
                    adViewAdmob.loadAd(adRequest);
                    return adViewAdmob;
                case Callback.AD_TYPE_WORTISE:
                    BannerAd mBannerAd = new BannerAd(ctx);
                    mBannerAd.setAdSize(com.wortise.ads.AdSize.HEIGHT_50);
                    mBannerAd.setAdUnitId(Callback.getWortiseBannerAdID());
                    linearLayout.addView(mBannerAd);
                    mBannerAd.loadAd();
                    return mBannerAd;
                case Callback.AD_TYPE_STARTAPP:
                    Banner startAppBanner = new Banner(ctx);
                    startAppBanner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    linearLayout.addView(startAppBanner);
                    startAppBanner.loadAd();
                    return startAppBanner;
                case Callback.AD_TYPE_UNITY:
                    BannerView bannerView = new BannerView((Activity) ctx, Callback.getUnityBannerAdID(), new UnityBannerSize(320, 50));
                    linearLayout.addView(bannerView);
                    bannerView.load();
                    return bannerView;
                case Callback.AD_TYPE_APPLOVIN:
                    MaxAdView adView = new MaxAdView(Callback.getApplovinBannerAdID(), ctx);
                    int heightPx = ctx.getResources().getDimensionPixelSize(R.dimen.banner_height);
                    adView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx));
                    linearLayout.addView(adView);
                    adView.loadAd();
                    return adView;
                case Callback.AD_TYPE_IRONSOURCE:
                    IronSourceBannerLayout iBannerAd  = IronSource.createBanner((Activity) ctx, ISBannerSize.BANNER);
                    linearLayout.addView(iBannerAd);
                    IronSource.loadBanner(iBannerAd);
                    return iBannerAd;
                case Callback.AD_TYPE_YANDEX:
                    BannerAdView yBannerAd = new BannerAdView(ctx);
                    int heightPx2 = ctx.getResources().getDimensionPixelSize(R.dimen.banner_height);
                    yBannerAd.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx2));
                    yBannerAd.setAdUnitId(Callback.getYandexBannerAdID());
                    com.yandex.mobile.ads.common.AdRequest yadRequest = new com.yandex.mobile.ads.common.AdRequest.Builder().build();
                    linearLayout.addView(yBannerAd);
                    yBannerAd.loadAd(yadRequest);
                    return yBannerAd;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public void showInterAd(final int pos, final String type) {
        if (isInterAd()){
            switch (Callback.getAdNetwork()) {
                case Callback.AD_TYPE_ADMOB, Callback.AD_TYPE_META -> {
                    final AdManagerInterAdmob adManagerInterAdmob = new AdManagerInterAdmob(ctx);
                    if (adManagerInterAdmob.getAd() != null) {
                        adManagerInterAdmob.getAd().setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                AdManagerInterAdmob.setAd(null);
                                adManagerInterAdmob.createAd();
                                interAdListener.onClick(pos, type);
                                super.onAdDismissedFullScreenContent();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull @NotNull com.google.android.gms.ads.AdError adError) {
                                AdManagerInterAdmob.setAd(null);
                                adManagerInterAdmob.createAd();
                                interAdListener.onClick(pos, type);
                                super.onAdFailedToShowFullScreenContent(adError);
                            }
                        });
                        adManagerInterAdmob.getAd().show((Activity) ctx);
                    } else {
                        AdManagerInterAdmob.setAd(null);
                        adManagerInterAdmob.createAd();
                        interAdListener.onClick(pos, type);
                    }
                }
                case Callback.AD_TYPE_STARTAPP -> {
                    final AdManagerInterStartApp adManagerInterStartApp = new AdManagerInterStartApp(ctx);
                    if (adManagerInterStartApp.getAd() != null && adManagerInterStartApp.getAd().isReady()) {
                        adManagerInterStartApp.getAd().showAd(new AdDisplayListener() {
                            @Override
                            public void adHidden(Ad ad) {
                                AdManagerInterStartApp.setAd(null);
                                adManagerInterStartApp.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void adDisplayed(Ad ad) {
                                // this method is empty
                            }

                            @Override
                            public void adClicked(Ad ad) {
                                // this method is empty
                            }

                            @Override
                            public void adNotDisplayed(Ad ad) {
                                AdManagerInterStartApp.setAd(null);
                                adManagerInterStartApp.createAd();
                                interAdListener.onClick(pos, type);
                            }
                        });
                    } else {
                        AdManagerInterStartApp.setAd(null);
                        adManagerInterStartApp.createAd();
                        interAdListener.onClick(pos, type);
                    }
                }
                case Callback.AD_TYPE_UNITY -> {
                    final AdManagerInterUnity adManagerInterUnity = new AdManagerInterUnity();
                    if (AdManagerInterUnity.getAd()) {
                        UnityAds.show((Activity) ctx, Callback.getUnityInterstitialAdID(), new IUnityAdsShowListener() {
                            @Override
                            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                                AdManagerInterUnity.setAd();
                                adManagerInterUnity.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onUnityAdsShowStart(String placementId) {
                                // this method is empty
                            }

                            @Override
                            public void onUnityAdsShowClick(String placementId) {
                                // this method is empty
                            }

                            @Override
                            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                                AdManagerInterUnity.setAd();
                                adManagerInterUnity.createAd();
                                interAdListener.onClick(pos, type);
                            }
                        });
                    } else {
                        AdManagerInterUnity.setAd();
                        adManagerInterUnity.createAd();
                        interAdListener.onClick(pos, type);
                    }
                }
                case Callback.AD_TYPE_APPLOVIN -> {
                    final AdManagerInterApplovin adManagerInterApplovin = new AdManagerInterApplovin(ctx);
                    if (adManagerInterApplovin.getAd() != null && adManagerInterApplovin.getAd().isReady()) {
                        adManagerInterApplovin.getAd().setListener(new MaxAdListener() {
                            @Override
                            public void onAdLoaded(@NonNull MaxAd ad) {
                                // this method is empty
                            }

                            @Override
                            public void onAdDisplayed(@NonNull MaxAd ad) {
                                // this method is empty
                            }

                            @Override
                            public void onAdHidden(@NonNull MaxAd ad) {
                                AdManagerInterApplovin.setAd(null);
                                adManagerInterApplovin.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onAdClicked(@NonNull MaxAd ad) {
                                // this method is empty
                            }

                            @Override
                            public void onAdLoadFailed(@NonNull String adUnitId, @NonNull MaxError error) {
                                AdManagerInterApplovin.setAd(null);
                                adManagerInterApplovin.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onAdDisplayFailed(@NonNull MaxAd ad, @NonNull MaxError error) {
                                AdManagerInterApplovin.setAd(null);
                                adManagerInterApplovin.createAd();
                                interAdListener.onClick(pos, type);
                            }
                        });
                        adManagerInterApplovin.getAd().showAd();
                    } else {
                        AdManagerInterStartApp.setAd(null);
                        adManagerInterApplovin.createAd();
                        interAdListener.onClick(pos, type);
                    }
                }
                case Callback.AD_TYPE_IRONSOURCE -> {
                    if (IronSource.isInterstitialReady()) {
                        IronSource.setLevelPlayInterstitialListener(new LevelPlayInterstitialListener() {
                            @Override
                            public void onAdReady(AdInfo adInfo) {
                                // this method is empty
                            }

                            @Override
                            public void onAdLoadFailed(IronSourceError ironSourceError) {
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onAdOpened(AdInfo adInfo) {
                                // this method is empty
                            }

                            @Override
                            public void onAdShowSucceeded(AdInfo adInfo) {
                                // this method is empty
                            }

                            @Override
                            public void onAdShowFailed(IronSourceError ironSourceError, AdInfo adInfo) {
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onAdClicked(AdInfo adInfo) {
                                // this method is empty
                            }

                            @Override
                            public void onAdClosed(AdInfo adInfo) {
                                interAdListener.onClick(pos, type);
                            }
                        });
                        IronSource.showInterstitial();
                    } else {
                        interAdListener.onClick(pos, type);
                    }
                    IronSource.init(ctx, Callback.getIronsourceAppKey(), IronSource.AD_UNIT.INTERSTITIAL);
                    IronSource.loadInterstitial();
                }
                case Callback.AD_TYPE_YANDEX -> {
                    final AdManagerInterYandex adManagerInterYandex = new AdManagerInterYandex(ctx);
                    if (adManagerInterYandex.getAd() != null) {
                        adManagerInterYandex.getAd().setAdEventListener(new InterstitialAdEventListener() {
                            @Override
                            public void onAdShown() {
                                // this method is empty
                            }

                            @Override
                            public void onAdFailedToShow(@NonNull AdError adError) {
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onAdDismissed() {
                                AdManagerInterYandex.setAd(null);
                                adManagerInterYandex.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onAdClicked() {
                                // this method is empty
                            }

                            @Override
                            public void onAdImpression(@Nullable ImpressionData impressionData) {
                                // this method is empty
                            }
                        });
                        adManagerInterYandex.getAd().show((Activity) ctx);
                    } else {
                        AdManagerInterYandex.setAd(null);
                        adManagerInterYandex.createAd();
                        interAdListener.onClick(pos, type);
                    }
                }
                case Callback.AD_TYPE_WORTISE -> {
                    final AdManagerInterWortise adManagerInterWortise = new AdManagerInterWortise(ctx);
                    if (adManagerInterWortise.getAd() != null && adManagerInterWortise.getAd().isAvailable()) {
                        adManagerInterWortise.getAd().setListener(new InterstitialAd.Listener() {

                            @Override
                            public void onInterstitialFailedToLoad(@NonNull InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                                AdManagerInterWortise.setAd(null);
                                adManagerInterWortise.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onInterstitialShown(@NonNull InterstitialAd interstitialAd) {
                                // this method is empty
                            }

                            @Override
                            public void onInterstitialLoaded(@NonNull InterstitialAd interstitialAd) {
                                // this method is empty
                            }

                            @Override
                            public void onInterstitialImpression(@NonNull InterstitialAd interstitialAd) {
                                // this method is empty
                            }

                            @Override
                            public void onInterstitialFailedToShow(@NonNull InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                                AdManagerInterWortise.setAd(null);
                                adManagerInterWortise.createAd();
                                interAdListener.onClick(pos, type);
                            }


                            @Override
                            public void onInterstitialDismissed(@NonNull InterstitialAd interstitialAd) {
                                AdManagerInterWortise.setAd(null);
                                adManagerInterWortise.createAd();
                                interAdListener.onClick(pos, type);
                            }

                            @Override
                            public void onInterstitialClicked(@NonNull InterstitialAd interstitialAd) {
                                // this method is empty
                            }
                        });
                        adManagerInterWortise.getAd().showAd();
                    } else {
                        AdManagerInterWortise.setAd(null);
                        adManagerInterWortise.createAd();
                        interAdListener.onClick(pos, type);
                    }
                }
                default -> interAdListener.onClick(pos, type);
            }
        } else {
            interAdListener.onClick(pos, type);
        }
    }

    public void showRewardAds(int pos, RewardAdListener rewardAdListener) {
        if (Boolean.TRUE.equals(Callback.getIsRewardAd())
                && Boolean.TRUE.equals(Callback.getIsAdsStatus())
                && !new SPHelper(ctx).getIsSubscribed()) {
            if (Boolean.TRUE.equals(new SPHelper(ctx).getIsRewardAdWarned())) {
                loadRewardAds(rewardAdListener, pos);
            } else {
                openRewardVideoAdAlert(rewardAdListener, pos);
            }
        } else {
            rewardAdListener.onPurchases(pos);
        }
    }

    public void loadRewardAds(RewardAdListener rewardAdListener, int pos) {
        if (new GDPRChecker(ctx).canLoadAd()) {
            switch (Callback.getAdNetwork()) {
                case Callback.AD_TYPE_ADMOB :
                    final RewardAdAdmob rewardAdAdmob = new RewardAdAdmob(ctx);
                    if (rewardAdAdmob.getAd() != null) {
                        rewardAdAdmob.getAd().setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                RewardAdAdmob.setAd(null);
                                rewardAdAdmob.createAd();
                                if (isRewarded) {
                                    rewardAdListener.onClick(true, pos);
                                }
                                super.onAdDismissedFullScreenContent();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull @NotNull com.google.android.gms.ads.AdError adError) {
                                RewardAdAdmob.setAd(null);
                                rewardAdAdmob.createAd();
                                rewardAdListener.onClick(false, pos);
                                super.onAdFailedToShowFullScreenContent(adError);
                            }
                        });
                        rewardAdAdmob.getAd().show((Activity) ctx, rewardItem -> isRewarded = true);
                    } else {
                        RewardAdAdmob.setAd(null);
                        rewardAdAdmob.createAd();
                        rewardAdListener.onClick(false, pos);
                    }
                    break;
                case Callback.AD_TYPE_WORTISE :
                    final RewardAdWortise rewardAdWortise = new RewardAdWortise(ctx);
                    if (rewardAdWortise.getAd() != null && rewardAdWortise.getAd().isAvailable()) {
                        rewardAdWortise.getAd().setListener(new RewardedAd.Listener() {

                            @Override
                            public void onRewardedShown(@NonNull RewardedAd rewardedAd) {
                                // this method is empty
                            }

                            @Override
                            public void onRewardedLoaded(@NonNull RewardedAd rewardedAd) {
                                // this method is empty
                            }

                            @Override
                            public void onRewardedImpression(@NonNull RewardedAd rewardedAd) {
                                // this method is empty
                            }

                            @Override
                            public void onRewardedFailedToShow(@NonNull RewardedAd rewardedAd, @NonNull com.wortise.ads.AdError adError) {
                                RewardAdWortise.setAd(null);
                                rewardAdWortise.createAd();
                                rewardAdListener.onClick(false, pos);
                            }

                            @Override
                            public void onRewardedFailedToLoad(@NonNull RewardedAd rewardedAd, @NonNull com.wortise.ads.AdError adError) {
                                RewardAdWortise.setAd(null);
                                rewardAdWortise.createAd();
                                rewardAdListener.onClick(false, pos);
                            }

                            @Override
                            public void onRewardedDismissed(@NonNull RewardedAd rewardedAd) {
                                RewardAdWortise.setAd(null);
                                rewardAdWortise.createAd();
                                if (isRewarded) {
                                    rewardAdListener.onClick(true, pos);
                                }
                            }

                            @Override
                            public void onRewardedCompleted(@NonNull RewardedAd rewardedAd, @NonNull Reward reward) {
                                isRewarded = true;
                            }

                            @Override
                            public void onRewardedClicked(@NonNull RewardedAd rewardedAd) {
                                // this method is empty
                            }
                        });
                        rewardAdWortise.getAd().showAd();
                    } else {
                        RewardAdWortise.setAd(null);
                        rewardAdWortise.createAd();
                        rewardAdListener.onClick(false, pos);
                    }
                    break;
                case Callback.AD_TYPE_STARTAPP :
                    final RewardAdStartApp rewardAdStartApp = new RewardAdStartApp(ctx);
                    if (rewardAdStartApp.getAd() != null && rewardAdStartApp.getAd().isReady()) {
                        rewardAdStartApp.getAd().showAd(new AdDisplayListener() {
                            @Override
                            public void adHidden(Ad ad) {
                                RewardAdStartApp.setAd(null);
                                rewardAdStartApp.createAd();
                                if (isRewarded) {
                                    rewardAdListener.onClick(true, pos);
                                }
                            }

                            @Override
                            public void adDisplayed(Ad ad) {
                                // this method is empty
                            }

                            @Override
                            public void adClicked(Ad ad) {
                                // this method is empty
                            }

                            @Override
                            public void adNotDisplayed(Ad ad) {
                                RewardAdStartApp.setAd(null);
                                rewardAdStartApp.createAd();
                                rewardAdListener.onClick(false, pos);
                            }
                        });
                        rewardAdStartApp.getAd().setVideoListener(() -> isRewarded = true);
                    } else {
                        RewardAdStartApp.setAd(null);
                        rewardAdStartApp.createAd();
                        rewardAdListener.onClick(false, pos);
                    }
                    break;
                case Callback.AD_TYPE_APPLOVIN :
                    final RewardAdApplovin rewardAdApplovin = new RewardAdApplovin(ctx);
                    if (RewardAdApplovin.getAd() != null && RewardAdApplovin.getAd().isReady()) {
                        RewardAdApplovin.getAd().setListener(new MaxRewardedAdListener() {


                            @Override
                            public void onAdLoaded(@NonNull MaxAd maxAd) {
                                // this method is empty
                            }

                            @Override
                            public void onAdDisplayed(@NonNull MaxAd maxAd) {
                                // this method is empty
                            }

                            @Override
                            public void onAdHidden(@NonNull MaxAd maxAd) {
                                RewardAdApplovin.setAd(null);
                                rewardAdApplovin.createAd();
                                if (isRewarded) {
                                    rewardAdListener.onClick(true, pos);
                                }
                            }

                            @Override
                            public void onAdClicked(@NonNull MaxAd maxAd) {
                                // this method is empty
                            }

                            @Override
                            public void onAdLoadFailed(@NonNull String s, @NonNull MaxError maxError) {
                                RewardAdApplovin.setAd(null);
                                rewardAdApplovin.createAd();
                                rewardAdListener.onClick(false, pos);
                            }

                            @Override
                            public void onAdDisplayFailed(@NonNull MaxAd maxAd, @NonNull MaxError maxError) {
                                RewardAdApplovin.setAd(null);
                                rewardAdApplovin.createAd();
                                rewardAdListener.onClick(false, pos);
                            }

                            @Override
                            public void onUserRewarded(@NonNull MaxAd maxAd, @NonNull MaxReward maxReward) {
                                isRewarded = true;
                            }

                        });
                        RewardAdApplovin.getAd().showAd();
                    } else {
                        RewardAdApplovin.setAd(null);
                        rewardAdApplovin.createAd();
                        rewardAdListener.onClick(false, pos);
                    }
                    break;
                case Callback.AD_TYPE_UNITY :
                    final RewardAdUnity rewardAdUnity = new RewardAdUnity();
                    if (RewardAdUnity.getAd()) {
                        UnityAds.show((Activity) ctx, Callback.getUnityRewardAdID(), new IUnityAdsShowListener() {
                            @Override
                            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                                RewardAdUnity.setAd(false);
                                rewardAdUnity.createAd();
                                rewardAdListener.onClick(false, pos);
                            }

                            @Override
                            public void onUnityAdsShowStart(String placementId) {
                                // this method is empty
                            }

                            @Override
                            public void onUnityAdsShowClick(String placementId) {
                                // this method is empty
                            }

                            @Override
                            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                                RewardAdUnity.setAd(false);
                                rewardAdUnity.createAd();
                                rewardAdListener.onClick(true, pos);
                            }
                        });
                    } else {
                        RewardAdUnity.setAd(false);
                        rewardAdUnity.createAd();
                        rewardAdListener.onClick(false, pos);
                    }
                    break;
                default:
                    break;
            }
        } else {
            rewardAdListener.onClick(false, pos);
        }
    }

    public void openRewardVideoAdAlert(RewardAdListener rewardAdListener, int pos) {
        BottomSheetDialog dialog = new BottomSheetDialog(ctx);
        @SuppressLint("InflateParams") View view = ((Activity) ctx).getLayoutInflater()
                .inflate(R.layout.row_bottom_videoad, null);
        dialog.setContentView(view);

        SmoothCheckBox smoothCheckBox = dialog.findViewById(R.id.cb_videoad);

        TextView logout = dialog.findViewById(R.id.btn_bottom_logout);
        if (logout != null){
            logout.setOnClickListener(view1 -> {
                dialog.dismiss();
                if (smoothCheckBox != null){
                    new SPHelper(ctx).setIsRewardAdWarned(smoothCheckBox.isChecked());
                } else {
                    new SPHelper(ctx).setIsRewardAdWarned(false);
                }
                loadRewardAds(rewardAdListener, pos);
            });
        }


        TextView cancel = dialog.findViewById(R.id.btn_bottom_cancel);
        if (cancel != null){
            cancel.setOnClickListener(view1 -> dialog.dismiss());
        }

        LinearLayout checkbox = dialog.findViewById(R.id.ll_checkbox);
        if (checkbox != null){
            checkbox.setOnClickListener(view1 -> {
                if (smoothCheckBox != null){
                    smoothCheckBox.setChecked(true);
                }
            });
        }

        TextView subscribe = dialog.findViewById(R.id.btn_bottom_subscribe);
        if (subscribe != null){
            subscribe.setOnClickListener(v -> DialogUtil.premiumDialog((Activity) ctx));
        }

        dialog.show();
    }

    private boolean isInterAd() {
        if (NetworkUtils.isConnected(ctx) && Boolean.TRUE.equals(Callback.getIsInterAd())
                && Boolean.TRUE.equals(Callback.getIsAdsStatus())
                && new GDPRChecker(ctx).canLoadAd()
                && !new SPHelper(ctx).getIsSubscribed()) {
            Callback.setAdCount(Callback.getAdCount() + 1);
            return Callback.getAdCount() % Callback.getInterstitialAdShow() == 0;
        } else {
            return false;
        }
    }

    private boolean isBannerAd(String page) {
        if (NetworkUtils.isConnected(ctx) && Boolean.TRUE.equals(Callback.getIsAdsStatus())
                && new GDPRChecker(ctx).canLoadAd()
                && !new SPHelper(ctx).getIsSubscribed()) {
            return switch (page) {
                case Callback.PAGE_HOME -> Callback.getIsBannerAdHome();
                case Callback.PAGE_POST_DETAILS -> Callback.getIsBannerAdPostDetails();
                case Callback.PAGE_CAT_DETAILS -> Callback.getIsBannerAdCatDetails();
                case Callback.PAGE_SEARCH -> Callback.getIsBannerAdSearch();
                default -> true;
            };
        } else {
            return false;
        }
    }

    public boolean canLoadNativeAds(Context context, String page) {
        if (Boolean.TRUE.equals(Callback.getIsAdsStatus())
                && new GDPRChecker(context).canLoadAd()
                && !new SPHelper(ctx).getIsSubscribed()) {
            if (page.equals(Callback.PAGE_NATIVE_POST)){
                return Callback.getIsNativeAdPost();
            } else if (page.equals(Callback.PAGE_NATIVE_CAT)){
                return Callback.getIsNativeAdCat();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void clickLogin() {
        SPHelper sharePref = new SPHelper(ctx);
        if (sharePref.isLogged()) {
            logout((Activity) ctx, sharePref);
            Toast.makeText(ctx, ctx.getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(ctx, SignInActivity.class);
            intent.putExtra("from", "app");
            ctx.startActivity(intent);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public void logout(Activity activity, SPHelper sharePref) {
        try {
            if (PlayerService.getExoPlayer() != null) {
                Intent intent = new Intent(ctx, PlayerService.class);
                intent.setAction(PlayerService.ACTION_STOP);
                ctx.startService(intent);
            }
        } catch (Exception e) {
           Log.e(TAG, "Error in logout",e);
        }

        if (sharePref.getLoginType().equals(Method.LOGIN_TYPE_GOOGLE)) {
            FirebaseAuth.getInstance().signOut();
        }
        sharePref.setIsAutoLogin(false);
        sharePref.setIsLogged(false);
        sharePref.setLoginDetails("", "", "", "", "", "",
                "", false, "", Method.LOGIN_TYPE_NORMAL);
        Intent intent1 = new Intent(ctx, SignInActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.putExtra("from", "");
        ctx.startActivity(intent1);
        activity.finish();
    }

    public boolean isYoutubeAppInstalled() {
        Intent mIntent = ctx.getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
        return mIntent != null;
    }

    public void shareSong(ItemSong itemSong, Boolean isOnline) {
        if (Boolean.TRUE.equals(isOnline)) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, ctx.getResources().getString(R.string.share_song));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, ctx.getResources().getString(R.string.listening)
                    + " - "
                    + itemSong.getTitle()
                    + "\n\nvia "
                    + ctx.getResources().getString(R.string.app_name)
                    + " - http://play.google.com/store/apps/details?id=" + ctx.getPackageName()
            );
            ctx.startActivity(Intent.createChooser(sharingIntent, ctx.getResources().getString(R.string.share_song)));
        } else {
            try {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("audio/mp3");
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse(itemSong.getUrl()));
                share.putExtra(android.content.Intent.EXTRA_TEXT, ctx.getResources().getString(R.string.listening)
                        + " - "
                        + itemSong.getTitle()
                        + "\n\nvia "
                        + ctx.getResources().getString(R.string.app_name)
                        + " - http://play.google.com/store/apps/details?id=" + ctx.getPackageName()
                );
                ctx.startActivity(Intent.createChooser(share, ctx.getResources().getString(R.string.share_song)));
            } catch (Exception e) {
                Log.e(TAG, "Error in shareSong",e);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void download(final ItemSong itemSong) {
        File root = new File(ctx.getExternalFilesDir("").getAbsolutePath() + File.separator + "/temp");
        if (!root.exists()) {
            root.mkdirs();
        }

        String a = String.valueOf(System.currentTimeMillis());
        String name = ApplicationUtil.getRandomValue((999999 - 100000) + 100000) + a.substring(a.length() - 6, a.length() - 1);
        File file = new File(root, name + ".mp3");

        if (Boolean.FALSE.equals(dbHelper.checkDownload(itemSong.getId()))) {
            String url = getUrl(itemSong);

            if (DownloadService.getInstance() == null){
                return;
            }

            Intent serviceIntent = new Intent(ctx, DownloadService.class);
            serviceIntent.setAction(Boolean.FALSE.equals(DownloadService.isDownloading())
                    ? DownloadService.ACTION_START
                    : DownloadService.ACTION_ADD
            );

            serviceIntent.putExtra("downloadUrl", url);
            serviceIntent.putExtra("file_path", root.toString());
            serviceIntent.putExtra("file_name", file.getName());
            serviceIntent.putExtra("item", itemSong);
            ctx.startService(serviceIntent);

            new AsyncTaskExecutor<String, String, String>() {
                @Override
                protected String doInBackground(String strings) {
                    try {
                        ApplicationUtil.responsePost(Callback.API_URL, getAPIRequest(Method.METHOD_DOWNLOAD_COUNT,
                                0, itemSong.getId(),"","","",
                                "","","","","", "",
                                "", "", null)
                        );
                        return "1";
                    } catch (Exception e) {
                        return "0";
                    }
                }

                @Override
                protected void onPostExecute(String result) {
                    // this method is empty
                }
            }.execute();
        } else {
            Toast.makeText(ctx, ctx.getResources().getString(R.string.already_download), Toast.LENGTH_SHORT).show();
        }
    }

    private static String getUrl(ItemSong itemSong) {
        String url;
        switch (Callback.getDownloadQuality()) {
            case 2 -> {
                if (!itemSong.getAudioUrlHigh().isEmpty()) {
                    url = itemSong.getAudioUrlHigh();
                } else if (!itemSong.getAudioUrlLow().isEmpty()) {
                    url = itemSong.getAudioUrlLow();
                } else {
                    url = itemSong.getUrl();
                }
            }
            case 3 -> {
                if (!itemSong.getAudioUrlLow().isEmpty()) {
                    url = itemSong.getAudioUrlLow();
                } else {
                    url = itemSong.getUrl();
                }
            }
            default -> url = itemSong.getUrl();
        }
        return url;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void openPlaylists(final ItemSong itemSong, final Boolean isOnline) {
        final Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_playlist);
        final List<ItemMyPlayList> arrayListPlaylist = dbHelper.loadPlayList(isOnline);

        final TextView textView = dialog.findViewById(R.id.tv_empty_dialog_pl);
        final RecyclerView recyclerView = dialog.findViewById(R.id.rv_dialog_playlist);

        final TextView btnAdd = dialog.findViewById(R.id.button_dialog_addplaylist);
        final EditText etAdd = dialog.findViewById(R.id.et_playlist_name);
        LinearLayoutManager manager = new LinearLayoutManager(ctx);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        final AdapterPlaylistDialog adapterPlaylist = new AdapterPlaylistDialog(arrayListPlaylist, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                dbHelper.addToPlayList(itemSong, arrayListPlaylist.get(position).getId(), isOnline);
                Toast.makeText(ctx, ctx.getString(R.string.song_add_to_playlist) + arrayListPlaylist.get(position).getName(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

            @Override
            public void onItemZero() {
                textView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
        recyclerView.setAdapter(adapterPlaylist);
        if (arrayListPlaylist.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        btnAdd.setOnClickListener(v -> {
            dialog.findViewById(R.id.ll_add_playlist).setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.GONE);
        });

        dialog.findViewById(R.id.iv_playlist_close).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.button_add).setOnClickListener(v -> {
            if (!etAdd.getText().toString().trim().isEmpty()) {
                arrayListPlaylist.clear();
                arrayListPlaylist.addAll(dbHelper.addPlayList(etAdd.getText().toString(), isOnline));
                adapterPlaylist.notifyDataSetChanged();
                textView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                Toast.makeText(ctx, ctx.getString(R.string.playlist_added), Toast.LENGTH_SHORT).show();

                etAdd.setText("");
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.enter_playlist_name), Toast.LENGTH_SHORT).show();
            }
        });

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null){
            window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        }
    }

    @SuppressLint("Range")
    public void getListOfflineSongs() {
        Callback.getArrayListOfflineSongs().clear();
        ContentResolver contentResolver = ctx.getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, MediaStore.MediaColumns.TITLE + " ASC");
        if (songCursor != null && songCursor.moveToFirst()) {
            do {
                String id = String.valueOf(songCursor.getLong(songCursor.getColumnIndex(BaseColumns._ID)));
                String title = songCursor.getString(songCursor.getColumnIndex(MediaStore.MediaColumns.TITLE));
                String artist = songCursor.getString(songCursor.getColumnIndex(MediaStore.MediaColumns.ARTIST));
                String url = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString();

                long albumId = songCursor.getLong(songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String image = getAlbumArtUri(albumId).toString();

                String desc = ctx.getString(R.string.title) + " - " + title + "</br>" + ctx.getString(R.string.artist) + " - " + artist;

                Callback.setArrayListOfflineSongs(new ItemSong(id, artist, url, url, url, image,
                        title, desc, "", "0", "0", "0",
                        false));

            } while (songCursor.moveToNext());
        }
    }

    public Uri getAlbumArtUri(int albumID) {
        Uri songCover = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(songCover, albumID);
    }

    public Uri getAlbumArtUri(long albumID) {
        Uri songCover = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(songCover, albumID);
    }
}
