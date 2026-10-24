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
    let retryTimer: ReturnType<typeof setTimeout> | undefined;
    let scrollTimer: ReturnType<typeof setTimeout> | undefined;
    let requesting = false;
    let bannerLoaded = false;
    let hiddenForScroll = false;
    let lastMargin = -1;

    const syncBanner = async () => {
      if (cancelled || requesting) return;

      const anchor = bannerAnchorRef.current;
      if (!anchor) return;

      const rect = anchor.getBoundingClientRect();

      // Never let the native overlay cover the top bar / coin balance.
      // Hide it once its reserved slot has scrolled under the header.
      if (rect.top < 62 || rect.bottom <= 62) {
        if (!hiddenForScroll) {
          hiddenForScroll = true;
          void hideNativeUnityBanner();
        }
        return;
      }

      hiddenForScroll = false;
      const nextMargin = Math.round(rect.top);

      if (bannerLoaded && nextMargin === lastMargin) return;

      requesting = true;
      try {
        const shown = await showNativeUnityBanner(nextMargin);
        if (shown) {
          bannerLoaded = true;
          lastMargin = nextMargin;
        } else {
          bannerLoaded = false;
        }
      } catch (error) {
        console.warn('Unity banner position sync failed:', error);
        bannerLoaded = false;
      } finally {
        requesting = false;
      }
    };

    const onScroll = () => {
      if (cancelled) return;

      // Hide immediately to prevent the native view lagging over page content.
      const anchor = bannerAnchorRef.current;
      if (anchor && anchor.getBoundingClientRect().top < 62) {
        hiddenForScroll = true;
        void hideNativeUnityBanner();
      }

      if (scrollTimer) clearTimeout(scrollTimer);
      scrollTimer = setTimeout(() => {
        void syncBanner();
      }, 180);
    };

    const tryShow = async () => {
      if (cancelled) return;
      await syncBanner();

      if (!cancelled && !bannerLoaded) {
        retryTimer = setTimeout(() => {
          void tryShow();
        }, 2500);
      }
    };

    document.addEventListener('scroll', onScroll, {
      passive: true,
      capture: true,
    });

    retryTimer = setTimeout(() => {
      void tryShow();
    }, 1200);

    return () => {
      cancelled = true;
      document.removeEventListener('scroll', onScroll, true);
      if (retryTimer) clearTimeout(retryTimer);
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
