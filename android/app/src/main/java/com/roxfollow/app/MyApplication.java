package com.roxfollow.app;

import android.app.Application;

import com.roxfollow.app.ads.UnityAdsManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UnityAdsManager.initialize(this);
    }
}
