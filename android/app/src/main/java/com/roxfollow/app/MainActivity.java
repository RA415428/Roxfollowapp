package com.roxfollow.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import com.roxfollow.app.ads.UnityAdsManager;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(UnityAdsPlugin.class);
        super.onCreate(savedInstanceState);
        UnityAdsManager.initialize(getApplicationContext());
    }
}
