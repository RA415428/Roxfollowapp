import { useEffect, useRef } from 'react';
import {
  isNativeApp,
  showNativeUnityBanner,
  hideNativeUnityBanner,
} from '../utils/nativeAds';

export function UnityBannerAd() {
  const bannerAnchorRef = useRef<HTMLDivElement>(null);
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

        const anchor = bannerAnchorRef.current;
        if (!anchor) return;

        // Match the native banner to the real on-screen position
        // of the placeholder directly beneath the TopBar.
        const nextMargin = Math.round(anchor.getBoundingClientRect().top);
        if (nextMargin === lastMargin) return;

        lastMargin = nextMargin;
        void showNativeUnityBanner(nextMargin).catch((error) => {
          console.warn('Unity banner position sync failed:', error);
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

    document.addEventListener('scroll', syncBannerWithScroll, { passive: true, capture: true });
    timer = setTimeout(() => { void tryShow(); }, 1500);

    return () => {
      cancelled = true;
      document.removeEventListener('scroll', syncBannerWithScroll, true);
      if (timer) clearTimeout(timer);
      if (scrollTimer) clearTimeout(scrollTimer);
      void hideNativeUnityBanner();
    };
  }, []);

  return (
    <div
      ref={bannerAnchorRef}
      aria-hidden="true"
      className="w-full h-[50px] shrink-0 pointer-events-none"
    />
  );
}
