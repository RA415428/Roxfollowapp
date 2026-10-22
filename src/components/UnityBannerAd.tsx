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
    let lastMargin = 62;
    let scrollTimer: ReturnType<typeof setTimeout> | undefined;

    const syncBannerWithScroll = () => {
      if (cancelled) return;
      if (scrollTimer) clearTimeout(scrollTimer);
      scrollTimer = setTimeout(() => {
        if (cancelled) return;
        const nextMargin = 62 - Math.round(window.scrollY);
        if (nextMargin === lastMargin) return;
        lastMargin = nextMargin;
        void showNativeUnityBanner(nextMargin).catch((error) => {
          console.warn('Unity banner scroll sync failed:', error);
        });
      }, 40);
    };

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

    window.addEventListener('scroll', syncBannerWithScroll, { passive: true });
    timer = setTimeout(() => { void tryShow(); }, 1500);

    return () => {
      cancelled = true;
      window.removeEventListener('scroll', syncBannerWithScroll);
      if (timer) clearTimeout(timer);
      if (scrollTimer) clearTimeout(scrollTimer);
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
