package com.roxfollow.app.ads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;

public class UnityAdsManager {

    private static final String TAG = "UnityAdsManager";

    private static final String GAME_ID = "800368206";
    public static final String REWARDED_PLACEMENT_ID = "Rewarded_Android";

    private static final boolean TEST_MODE = false;

    private static boolean isInitialized = false;
    private static volatile boolean rewardedAdLoaded = false;

    public interface RewardCallback {
        void onReward();
    }

    public interface FailCallback {
        void onFailedOrSkipped();
    }

    public static void initialize(Context context) {
        if (isInitialized) return;
        UnityAds.initialize(context, GAME_ID, TEST_MODE, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                Log.d(TAG, "Unity Ads initialized successfully");
                isInitialized = true;
                loadRewardedAd();
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                Log.e(TAG, "Unity Ads init failed: " + error + " - " + message);
            }
        });
    }

    public static void loadRewardedAd() {
        rewardedAdLoaded = false;
        UnityAds.load(REWARDED_PLACEMENT_ID, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                Log.d(TAG, "Rewarded ad loaded: " + placementId);
                rewardedAdLoaded = true;
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                Log.e(TAG, "Rewarded ad failed to load: " + error + " - " + message);
                rewardedAdLoaded = false;
            }
        });
    }

    public static boolean isRewardedAdReady() {
        return rewardedAdLoaded;
    }

    public static void showRewardedAd(Activity activity, RewardCallback onReward, FailCallback onFailedOrSkipped) {
        rewardedAdLoaded = false;
        UnityAds.show(activity, REWARDED_PLACEMENT_ID, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                Log.e(TAG, "Rewarded ad show failed: " + error + " - " + message);
                if (onFailedOrSkipped != null) onFailedOrSkipped.onFailedOrSkipped();
                loadRewardedAd();
            }

            @Override
            public void onUnityAdsShowStart(String placementId) {}

            @Override
            public void onUnityAdsShowClick(String placementId) {}

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                    if (onReward != null) onReward.onReward();
                } else {
                    if (onFailedOrSkipped != null) onFailedOrSkipped.onFailedOrSkipped();
                }
                loadRewardedAd();
            }
        });
    }
}
