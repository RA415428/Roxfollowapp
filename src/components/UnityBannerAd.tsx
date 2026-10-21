import { useEffect } from 'react';
import {
  isNativeApp,
  showNativeUnityBanner,
  hideNativeUnityBanner,
} from '../utils/nativeAds';

export function UnityBannerAd() {
  useEffect(() => {
    if (!isNativeApp()) return;

    let cancelled = false;
    let timer: ReturnType<typeof setTimeout> | undefined;
    let requesting = false;

    const tryShow = async () => {
      if (cancelled || requesting) return;
      requesting = true;

      let shown = false;
      try {
        shown = await showNativeUnityBanner(62);
      } catch (error) {
        console.warn('Unity banner attempt failed:', error);
      } finally {
        requesting = false;
      }

      if (cancelled) return;

      if (shown) {
        console.log('Unity banner loaded successfully');
        return;
      }

      timer = setTimeout(tryShow, 2000);
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
