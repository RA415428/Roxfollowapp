import { useEffect } from 'react';
import { isNativeApp, showNativeUnityBanner, hideNativeUnityBanner } from '../utils/nativeAds';

/** Native-only Unity banner; retry while Unity Ads initializes or loads. */
export function UnityBannerAd() {
  useEffect(() => {
    if (!isNativeApp()) return;
    let cancelled = false;
    let attempts = 0;
    let retryTimer: ReturnType<typeof setTimeout> | undefined;

    const show = async () => {
      if (cancelled || attempts >= 10) return;
      attempts += 1;
      try {
        const shown = await showNativeUnityBanner(62);
        if (!shown && !cancelled && attempts < 10) {
          retryTimer = setTimeout(show, 3000);
        }
      } catch (error) {
        console.warn('Unity banner unavailable:', error);
        if (!cancelled && attempts < 10) {
          retryTimer = setTimeout(show, 3000);
        }
      }
    };

    show();
    return () => {
      cancelled = true;
      if (retryTimer) clearTimeout(retryTimer);
      hideNativeUnityBanner().catch(() => {});
    };
  }, []);
  return <div aria-hidden="true" className="w-full h-[50px] shrink-0 pointer-events-none" />;
}
