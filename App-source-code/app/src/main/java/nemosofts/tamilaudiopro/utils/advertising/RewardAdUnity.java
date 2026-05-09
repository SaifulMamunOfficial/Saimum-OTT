package nemosofts.tamilaudiopro.utils.advertising;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAds;

import nemosofts.tamilaudiopro.callback.Callback;

public class RewardAdUnity {

    private static boolean isAdLoaded = false;

    public RewardAdUnity() {
        // this constructor is empty
    }

    public void createAd() {
        UnityAds.load(Callback.getUnityRewardAdID(), new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                RewardAdUnity.setAd(true);
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                RewardAdUnity.setAd(false);
            }
        });
    }

    public static boolean getAd() {
        return isAdLoaded;
    }

    public static void setAd(boolean isLoaded) {
        isAdLoaded = isLoaded;
    }
}