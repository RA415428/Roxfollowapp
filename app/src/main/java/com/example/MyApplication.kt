package com.example

import android.app.Application
import com.example.ads.UnityAdsManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UnityAdsManager.initialize(this)
    }
}
