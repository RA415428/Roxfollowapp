package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions

/**
 * Central place for Unity Ads setup — Rox Follow app.
 * Game ID and Rewarded placement are the real, live values from the Unity dashboard.
 * Only rewarded video is used (no interstitial, no banner) by design.
 */
object UnityAdsManager {

    private const val TAG = "UnityAdsManager"

    private const val GAME_ID = "800368206"
    const val REWARDED_PLACEMENT_ID = "Rewarded_Android"

    private const val TEST_MODE = false

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        UnityAds.initialize(
            context,
            GAME_ID,
            TEST_MODE,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    Log.d(TAG, "Unity Ads initialized successfully")
                    isInitialized = true
                    loadRewardedAd()
                }

                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError?,
                    message: String?
                ) {
                    Log.e(TAG, "Unity Ads init failed: $error - $message")
                }
            }
        )
    }

    // ---------- Rewarded ----------

    @Volatile
    private var rewardedAdLoaded = false

    fun loadRewardedAd() {
        rewardedAdLoaded = false
        UnityAds.load(REWARDED_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                Log.d(TAG, "Rewarded ad loaded: $placementId")
                rewardedAdLoaded = true
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?
            ) {
                Log.e(TAG, "Rewarded ad failed to load: $error - $message")
                rewardedAdLoaded = false
            }
        })
    }

    fun showRewardedAd(
        activity: Activity,
        onReward: () -> Unit,
        onFailedOrSkipped: (() -> Unit)? = null
    ) {
        rewardedAdLoaded = false
        UnityAds.show(
            activity,
            REWARDED_PLACEMENT_ID,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    placementId: String?,
                    error: UnityAds.UnityAdsShowError?,
                    message: String?
                ) {
                    Log.e(TAG, "Rewarded ad show failed: $error - $message")
                    onFailedOrSkipped?.invoke()
                    loadRewardedAd()
                }

                override fun onUnityAdsShowStart(placementId: String?) {}

                override fun onUnityAdsShowClick(placementId: String?) {}

                override fun onUnityAdsShowComplete(
                    placementId: String?,
                    state: com.unity3d.ads.UnityAds.UnityAdsShowCompletionState?
                ) {
                    if (state == com.unity3d.ads.UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                        onReward()
                    } else {
                        onFailedOrSkipped?.invoke()
                    }
                    loadRewardedAd()
                }
            }
        )
    }

    fun isRewardedAdReady(): Boolean = rewardedAdLoaded
}
