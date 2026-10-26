package com.roxfollow.app;

import android.os.Bundle;
import android.graphics.Color;
import com.getcapacitor.BridgeActivity;
import com.roxfollow.app.ads.UnityAdsManager;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(UnityAdsPlugin.class);
        super.onCreate(savedInstanceState);

        // Prevent a white WebView flash before the existing React loader appears.
        if (getBridge() != null && getBridge().getWebView() != null) {
            getBridge().getWebView().setBackgroundColor(Color.rgb(15, 4, 18));
        }

        UnityAdsManager.initialize(getApplicationContext());
    }
}
