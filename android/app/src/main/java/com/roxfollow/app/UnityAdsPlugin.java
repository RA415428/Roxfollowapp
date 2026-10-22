package com.roxfollow.app;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.util.TypedValue;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.roxfollow.app.ads.UnityAdsManager;

@CapacitorPlugin(name = "UnityAdsNative")
public class UnityAdsPlugin extends Plugin {
    private BannerView unityBannerAd;
    private View unityBannerView;
    private boolean bannerErrorToastShown = false;

    // Show only the first banner failure per app session.
    private void reportBannerError(String message) {
        if (bannerErrorToastShown) return;
        bannerErrorToastShown = true;
        android.content.Context context = getContext();
        if (context == null) return;
        new Handler(Looper.getMainLooper(), null).post(() ->
            Toast.makeText(
                context,
                "Unity banner: " + message,
                Toast.LENGTH_LONG
            ).show()
        );
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            getContext().getResources().getDisplayMetrics()
        );
    }

    @PluginMethod
    public void isAvailable(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("available", UnityAdsManager.isRewardedAdReady());
        call.resolve(ret);
    }

    @PluginMethod
    public void showRewardedAd(PluginCall call) {
        Activity activity = getActivity();

        if (activity == null || !UnityAdsManager.isRewardedAdReady()) {
            JSObject ret = new JSObject();
            ret.put("requested", false);
            call.resolve(ret);
            return;
        }

        UnityAdsManager.showRewardedAd(
            activity,
            () -> activity.runOnUiThread(() ->
                bridge.getWebView().evaluateJavascript(
                    "window.onNativeRewardedAdCompleted && window.onNativeRewardedAdCompleted();",
                    null
                )
            ),
            () -> { }
        );

        JSObject ret = new JSObject();
        ret.put("requested", true);
        call.resolve(ret);
    }

    @PluginMethod
    public void showBanner(PluginCall call) {
        Activity activity = getActivity();

        if (activity == null || !UnityAds.isInitialized()) {
            reportBannerError(activity == null
                ? "Activity unavailable"
                : "Unity Ads is not initialized");
            JSObject ret = new JSObject();
            ret.put("shown", false);
            ret.put("error", "Unity Ads is not initialized");
            call.resolve(ret);
            return;
        }

        final int topMarginDp = call.getInt("topMarginDp", 62);

        activity.runOnUiThread(() -> {
            try {
                FrameLayout content = activity.findViewById(android.R.id.content);

                // Keep an already loaded banner instead of reloading it.
                if (unityBannerView != null
                    && unityBannerView.getParent() == content) {
                    JSObject ret = new JSObject();
                    ret.put("shown", true);
                    call.resolve(ret);
                    return;
                }

                if (unityBannerAd != null) {
                    unityBannerAd.destroy();
                    unityBannerAd = null;
                    unityBannerView = null;
                }

                BannerView banner = new BannerView(
                    activity,
                    "Banner_Android",
                    new UnityBannerSize(320, 50)
                );

                unityBannerAd = banner;

                final Handler bannerTimeoutHandler =
                    new Handler(Looper.getMainLooper());
                final boolean[] completed = {false};
                final Runnable[] timeoutTask = new Runnable[1];

                timeoutTask[0] = () -> {
                    if (completed[0] || unityBannerAd != banner) return;
                    completed[0] = true;
                    unityBannerAd = null;
                    unityBannerView = null;
                    try {
                        banner.destroy();
                    } catch (Exception ignored) { }
                    JSObject ret = new JSObject();
                    reportBannerError("load timed out after 10 seconds");
                    ret.put("shown", false);
                    ret.put("error", "Unity banner load timed out");
                    call.resolve(ret);
                };

                banner.setListener(new BannerView.IListener() {
                    @Override
                    public void onBannerLoaded(BannerView loadedBanner) {
                        activity.runOnUiThread(() -> {
                            if (unityBannerAd != banner || completed[0]) return;
                            completed[0] = true;
                            bannerTimeoutHandler.removeCallbacks(timeoutTask[0]);

                            ViewGroup parent = (ViewGroup) loadedBanner.getParent();
                            if (parent != null) parent.removeView(loadedBanner);

                            FrameLayout.LayoutParams params =
                                new FrameLayout.LayoutParams(
                                    dp(320),
                                    dp(50),
                                    Gravity.TOP | Gravity.CENTER_HORIZONTAL
                                );
                            params.topMargin = dp(topMarginDp);
                            content.addView(loadedBanner, params);
                            loadedBanner.setVisibility(View.VISIBLE);
                            loadedBanner.setAlpha(1f);
                            loadedBanner.setElevation(dp(100));
                            loadedBanner.setTranslationZ(dp(100));
                            content.bringChildToFront(loadedBanner);
                            loadedBanner.bringToFront();
                            content.requestLayout();
                            content.invalidate();
                            unityBannerView = loadedBanner;

                            JSObject ret = new JSObject();
                            ret.put("shown", true);
                            call.resolve(ret);
                        });
                    }

                    @Override
                    public void onBannerFailedToLoad(
                        BannerView failedBanner,
                        BannerErrorInfo errorInfo
                    ) {
                        activity.runOnUiThread(() -> {
                            if (unityBannerAd != banner || completed[0]) return;
                            completed[0] = true;
                            bannerTimeoutHandler.removeCallbacks(timeoutTask[0]);

                            unityBannerAd = null;
                            unityBannerView = null;
                            reportBannerError(errorInfo == null
                                ? "load failed (no details)"
                                : errorInfo.errorMessage);

                            JSObject ret = new JSObject();
                            ret.put("shown", false);
                            ret.put(
                                "error",
                                errorInfo == null
                                    ? "Unity banner failed to load"
                                    : errorInfo.errorMessage
                            );
                            call.resolve(ret);
                        });
                    }

                    @Override
                    public void onBannerShown(BannerView shownBanner) { }

                    @Override
                    public void onBannerClick(BannerView clickedBanner) { }

                    @Override
                    public void onBannerLeftApplication(BannerView leftBanner) { }
                });

                bannerTimeoutHandler.postDelayed(timeoutTask[0], 10000);
                banner.load();
            } catch (Exception error) {
                reportBannerError("exception: " + String.valueOf(error.getMessage()));
                JSObject ret = new JSObject();
                ret.put("shown", false);
                ret.put("error", String.valueOf(error.getMessage()));
                call.resolve(ret);
            }
        });
    }

    @PluginMethod
    public void hideBanner(PluginCall call) {
        Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(() -> {
                if (unityBannerAd != null) {
                    unityBannerAd.destroy();
                    unityBannerAd = null;
                    unityBannerView = null;
                } else if (unityBannerView != null
                    && unityBannerView.getParent() instanceof ViewGroup) {
                    ((ViewGroup) unityBannerView.getParent())
                        .removeView(unityBannerView);
                    unityBannerView = null;
                }
            });
        }

        JSObject ret = new JSObject();
        ret.put("hidden", true);
        call.resolve(ret);
    }
}
