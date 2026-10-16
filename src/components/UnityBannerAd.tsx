import { useEffect } from 'react';
import {
  isNativeApp,
  showNativeUnityBanner,
  hideNativeUnityBanner,
} from '../utils/nativeAds';

/** Keep retrying on native Android until Unity reports that the banner loaded. */
export function UnityBannerAd() {
  useEffect(() => {
    if (!isNativeApp()) return;

    let cancelled = false;
    let timer: ReturnType<typeof setTimeout> | undefined;
    let delay = 3000;

    const tryShow = async () => {
      if (cancelled) return;

      let shown = false;
      try {
        shown = await showNativeUnityBanner(62);
      } catch (error) {
        console.warn('Unity banner attempt failed:', error);
      }

      if (cancelled) return;

      if (shown) {
        delay = 15000;
      } else {
        delay = Math.min(delay + 2000, 30000);
      }

      timer = setTimeout(tryShow, delay);
    };

    void tryShow();

    return () => {
      cancelled = true;
      if (timer) clearTimeout(timer);
      void hideNativeUnityBanner();
    };
  }, []);

  return (
    <div
      aria-hidden="true"
      className="w-full h-[50px] shrink-0 pointer-events-none"
    />
  );
}
