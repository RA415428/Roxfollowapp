import { useEffect } from 'react';
import { isNativeApp, showNativeUnityBanner, hideNativeUnityBanner } from '../utils/nativeAds';

/** Native-only Unity banner; no Adsterra/web fallback. */
export function UnityBannerAd() {
  useEffect(() => {
    if (!isNativeApp()) return;
    let cancelled = false;
    const show = () => {
      if (!cancelled) {
        showNativeUnityBanner(62).catch((error) =>
          console.warn('Unity banner unavailable:', error));
      }
    };
    show();
    window.addEventListener('resize', show);
    return () => {
      cancelled = true;
      window.removeEventListener('resize', show);
      hideNativeUnityBanner().catch(() => {});
    };
  }, []);
  return <div aria-hidden="true" className="w-full h-[50px] shrink-0 pointer-events-none" />;
}
