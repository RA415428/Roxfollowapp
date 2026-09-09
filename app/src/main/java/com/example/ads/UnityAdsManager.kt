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

    // Real Game ID + placement are live now, so test mode is off (real ads will serve).
    // While you're personally testing, register this phone as a "Test device" in the
    // Unity dashboard (Monetization > Settings > Test device) so your own taps/impressions
    // don't count as invalid traffic.
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

    fun loadRewardedAd() {
        UnityAds.load(REWARDED_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                Log.d(TAG, "Rewarded ad loaded: $placementId")
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?
            ) {
                Log.e(TAG, "Rewarded ad failed to load: $error - $message")
            }
        })
    }

    /**
     * Shows a rewarded ad. [onReward] is called ONLY if the user watches the ad to completion —
     * this is where you should grant coins/currency. [onFailedOrSkipped] covers every other
     * outcome (not loaded yet, user skipped, network error) — do not grant a reward there.
     */
    fun showRewardedAd(
        activity: Activity,
        onReward: () -> Unit,
        onFailedOrSkipped: (() -> Unit)? = null
    ) {
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
                        // SKIPPED or unknown -> no reward
                        onFailedOrSkipped?.invoke()
                    }
                    // Pre-load the next one immediately
                    loadRewardedAd()
                }
            }
        )
    }

    fun isRewardedAdReady(): Boolean =
        UnityAds.getPlacementState(REWARDED_PLACEMENT_ID) == UnityAds.PlacementState.READY
}
