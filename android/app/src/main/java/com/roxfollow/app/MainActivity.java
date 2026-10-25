package com.roxfollow.app;

import android.os.Bundle;
import android.os.Handler;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.webkit.WebView;

import androidx.core.content.ContextCompat;

import com.getcapacitor.BridgeActivity;
import com.roxfollow.app.ads.UnityAdsManager;

public class MainActivity extends BridgeActivity {
    private final Handler loaderHandler = new Handler();
    private FrameLayout startupOverlay;
    private long loaderStartedAt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(UnityAdsPlugin.class);
        super.onCreate(savedInstanceState);
        UnityAdsManager.initialize(getApplicationContext());

        showStartupLoader();
    }

    private void showStartupLoader() {
        View content = findViewById(android.R.id.content);
        if (!(content instanceof ViewGroup)) return;

        FrameLayout overlay = new FrameLayout(this);
        overlay.setBackground(ContextCompat.getDrawable(this, R.drawable.splash));

        ProgressBar spinner = new ProgressBar(
            this, null, android.R.attr.progressBarStyle
        );
        spinner.setIndeterminate(true);
        spinner.setIndeterminateTintList(
            ColorStateList.valueOf(Color.rgb(236, 90, 169))
        );

        int size = (int) (36 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lp.bottomMargin = (int) (72 * getResources().getDisplayMetrics().density);
        overlay.addView(spinner, lp);

        startupOverlay = overlay;
        ((ViewGroup) content).addView(
            overlay,
            new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        );

        loaderStartedAt = System.currentTimeMillis();
        loaderHandler.postDelayed(this::checkStartupReady, 300);
    }

    private void checkStartupReady() {
        if (startupOverlay == null) return;

        if (System.currentTimeMillis() - loaderStartedAt > 15000) {
            removeStartupLoader();
            return;
        }

        try {
            WebView webView = getBridge().getWebView();
            webView.evaluateJavascript(
                "(document.readyState === 'complete' && !!document.querySelector('#root > *'))",
                result -> {
                    if (startupOverlay == null) return;
                    if ("true".equals(result)) {
                        removeStartupLoader();
                    } else {
                        loaderHandler.postDelayed(this::checkStartupReady, 300);
                    }
                }
            );
        } catch (Exception e) {
            loaderHandler.postDelayed(this::checkStartupReady, 300);
        }
    }

    private void removeStartupLoader() {
        loaderHandler.removeCallbacksAndMessages(null);
        if (startupOverlay != null) {
            ViewGroup parent = (ViewGroup) startupOverlay.getParent();
            if (parent != null) parent.removeView(startupOverlay);
            startupOverlay = null;
        }
    }

    @Override
    public void onDestroy() {
        loaderHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
