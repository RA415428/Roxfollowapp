package com.roxfollow.app.ads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static Context appContext;
    private static boolean isInitialized = false;
    private static boolean isInitializing = false;
    private static boolean isLoadingRewarded = false;
    private static volatile boolean rewardedAdLoaded = false;
    private static int initAttempts = 0;
    private static int loadAttempts = 0;

    public interface RewardCallback {
        void onReward();
    }

    public interface FailCallback {
        void onFailedOrSkipped();
    }

    public static synchronized void initialize(Context context) {
        if (context != null) appContext = context.getApplicationContext();
        if (isInitialized || isInitializing || appContext == null) return;

        isInitializing = true;
        initAttempts++;
        Log.d(TAG, "Unity initialization attempt " + initAttempts);

        try {
            UnityAds.initialize(appContext, GAME_ID, TEST_MODE,
                new IUnityAdsInitializationListener() {
                    @Override
                    public void onInitializationComplete() {
                        synchronized (UnityAdsManager.class) {
                            isInitialized = true;
                            isInitializing = false;
                            initAttempts = 0;
                        }
                        Log.d(TAG, "Unity Ads initialized successfully; testMode=false");
                        loadRewardedAd();
                    }

                    @Override
                    public void onInitializationFailed(
                        UnityAds.UnityAdsInitializationError error,
                        String message
                    ) {
                        synchronized (UnityAdsManager.class) {
                            isInitialized = false;
                            isInitializing = false;
                        }
                        Log.e(TAG, "Unity Ads init failed: " + error + " - " + message);
                        scheduleInitializationRetry();
                    }
                });
        } catch (Exception error) {
            synchronized (UnityAdsManager.class) {
                isInitializing = false;
                isInitialized = false;
            }
            Log.e(TAG, "Unity initialize exception", error);
            scheduleInitializationRetry();
        }
    }

    private static void scheduleInitializationRetry() {
        long delay = Math.min(60000L, 5000L * Math.max(1, Math.min(initAttempts, 12)));
        handler.postDelayed(() -> {
            Context context;
            synchronized (UnityAdsManager.class) {
                if (isInitialized || isInitializing) return;
                context = appContext;
            }
            if (context != null) initialize(context);
        }, delay);
    }

    public static void loadRewardedAd() {
        synchronized (UnityAdsManager.class) {
            if (!isInitialized || isLoadingRewarded || rewardedAdLoaded) return;
            isLoadingRewarded = true;
        }

        try {
            UnityAds.load(REWARDED_PLACEMENT_ID, new IUnityAdsLoadListener() {
                @Override
                public void onUnityAdsAdLoaded(String placementId) {
                    synchronized (UnityAdsManager.class) {
                        isLoadingRewarded = false;
                        rewardedAdLoaded = true;
                        loadAttempts = 0;
                    }
                    Log.d(TAG, "Rewarded ad loaded: " + placementId);
                }

                @Override
                public void onUnityAdsFailedToLoad(
                    String placementId,
                    UnityAds.UnityAdsLoadError error,
                    String message
                ) {
                    synchronized (UnityAdsManager.class) {
                        isLoadingRewarded = false;
                        rewardedAdLoaded = false;
                    }
                    Log.e(TAG, "Rewarded load failed: " + error + " - " + message);
                    scheduleRewardedRetry();
                }
            });
        } catch (Exception error) {
            synchronized (UnityAdsManager.class) {
                isLoadingRewarded = false;
                rewardedAdLoaded = false;
            }
            Log.e(TAG, "Rewarded load exception", error);
            scheduleRewardedRetry();
        }
    }

    private static void scheduleRewardedRetry() {
        long delay;
        synchronized (UnityAdsManager.class) {
            loadAttempts++;
            delay = Math.min(60000L, 5000L * Math.max(1, Math.min(loadAttempts, 12)));
        }
        handler.postDelayed(UnityAdsManager::loadRewardedAd, delay);
    }

    public static boolean isRewardedAdReady() {
        return isInitialized && rewardedAdLoaded;
    }

    public static void showRewardedAd(
        Activity activity,
        RewardCallback onReward,
        FailCallback onFailedOrSkipped
    ) {
        if (activity == null || !isRewardedAdReady()) {
            if (onFailedOrSkipped != null) onFailedOrSkipped.onFailedOrSkipped();
            loadRewardedAd();
            return;
        }

        rewardedAdLoaded = false;
        try {
            UnityAds.show(activity, REWARDED_PLACEMENT_ID, new UnityAdsShowOptions(),
                new IUnityAdsShowListener() {
                    @Override
                    public void onUnityAdsShowFailure(
                        String placementId,
                        UnityAds.UnityAdsShowError error,
                        String message
                    ) {
                        Log.e(TAG, "Rewarded show failed: " + error + " - " + message);
                        if (onFailedOrSkipped != null) onFailedOrSkipped.onFailedOrSkipped();
                        loadRewardedAd();
                    }

                    @Override
                    public void onUnityAdsShowStart(String placementId) { }

                    @Override
                    public void onUnityAdsShowClick(String placementId) { }

                    @Override
                    public void onUnityAdsShowComplete(
                        String placementId,
                        UnityAds.UnityAdsShowCompletionState state
                    ) {
                        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                            if (onReward != null) onReward.onReward();
                        } else if (onFailedOrSkipped != null) {
                            onFailedOrSkipped.onFailedOrSkipped();
                        }
                        loadRewardedAd();
                    }
                });
        } catch (Exception error) {
            Log.e(TAG, "Rewarded show exception", error);
            if (onFailedOrSkipped != null) onFailedOrSkipped.onFailedOrSkipped();
            loadRewardedAd();
        }
    }
}
