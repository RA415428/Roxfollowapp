package com.roxfollow.app;

import android.app.Activity;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.roxfollow.app.ads.UnityAdsManager;

@CapacitorPlugin(name = "UnityAdsNative")
public class UnityAdsPlugin extends Plugin {

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
            () -> {
                // Ad failed or was skipped before completion — no reward, nothing to notify.
            }
        );

        JSObject ret = new JSObject();
        ret.put("requested", true);
        call.resolve(ret);
    }
}
