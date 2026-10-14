package com.roxfollow.app;

import android.app.Activity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.util.TypedValue;
import com.unity3d.services.banners.BannerAd;
import com.unity3d.services.banners.BannerLoadConfiguration;
import com.unity3d.services.banners.BannerSize;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.roxfollow.app.ads.UnityAdsManager;

@CapacitorPlugin(name = "UnityAdsNative")
public class UnityAdsPlugin extends Plugin {
    private BannerAd unityBannerAd;
    private android.view.View unityBannerView;

    private int dp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
            getContext().getResources().getDisplayMetrics());
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
        if (activity == null || !com.unity3d.ads.UnityAds.isInitialized()) {
            JSObject ret = new JSObject();
            ret.put("shown", false);
            call.resolve(ret);
            return;
        }
        final int topMarginDp = call.getInt("topMarginDp", 62);
        activity.runOnUiThread(() -> {
            try {
                FrameLayout content = activity.findViewById(android.R.id.content);
                if (unityBannerView != null && unityBannerView.getParent() instanceof ViewGroup) {
                    ((ViewGroup) unityBannerView.getParent()).removeView(unityBannerView);
                }
                BannerLoadConfiguration configuration =
                    new BannerLoadConfiguration.Builder("Banner_Android", new BannerSize(320, 50)).build();
                BannerAd.load(configuration, (bannerAd, error) -> activity.runOnUiThread(() -> {
                    if (bannerAd == null || bannerAd.getView() == null) {
                        JSObject ret = new JSObject();
                        ret.put("shown", false);
                        ret.put("error", error == null ? "Unity banner did not load" : error.getMessage());
                        call.resolve(ret);
                        return;
                    }
                    unityBannerAd = bannerAd;
                    unityBannerView = bannerAd.getView();
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        dp(320), dp(50), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                    params.topMargin = dp(topMarginDp);
                    content.addView(unityBannerView, params);
                    JSObject ret = new JSObject();
                    ret.put("shown", true);
                    call.resolve(ret);
                }));
            } catch (Exception error) {
                JSObject ret = new JSObject();
                ret.put("shown", false);
                ret.put("error", error.getMessage());
                call.resolve(ret);
            }
        });
    }

    @PluginMethod
    public void hideBanner(PluginCall call) {
        Activity activity = getActivity();
        if (activity != null) activity.runOnUiThread(() -> {
            if (unityBannerView != null && unityBannerView.getParent() instanceof ViewGroup) {
                ((ViewGroup) unityBannerView.getParent()).removeView(unityBannerView);
            }
        });
        JSObject ret = new JSObject();
        ret.put("hidden", true);
        call.resolve(ret);
    }
}
