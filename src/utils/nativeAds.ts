import { registerPlugin } from '@capacitor/core';

export interface UnityAdsNativePlugin {
  isAvailable(): Promise<{ available: boolean }>;
  showRewardedAd(): Promise<{ requested: boolean }>;
  showBanner(options: { topMarginDp: number }): Promise<{ shown: boolean; error?: string }>;
  hideBanner(): Promise<{ hidden: boolean }>;
  moveBanner(options: { topDp: number }): Promise<void>;
}

const UnityAdsNative = registerPlugin<UnityAdsNativePlugin>('UnityAdsNative');

export function isNativeApp(): boolean {
  const cap = (window as any).Capacitor;
  return !!cap && typeof cap.isNativePlatform === 'function' && cap.isNativePlatform();
}

let adInFlight = false;

export function isAdInFlight(): boolean {
  return adInFlight;
}

export async function requestNativeRewardedAd(): Promise<boolean> {
  if (!isNativeApp()) return false;
  if (adInFlight) return false;

  adInFlight = true;
  let requested = false;

  try {
    // Unity ko ad load karne ke liye maximum 20 seconds dein.
    for (let attempt = 0; attempt < 20; attempt++) {
      const { available } = await UnityAdsNative.isAvailable();

      if (available) {
        const result = await UnityAdsNative.showRewardedAd();
        requested = !!result.requested;
        return requested;
      }

      await new Promise(resolve => setTimeout(resolve, 1000));
    }

    console.warn("Unity Rewarded Ad not ready after 20 seconds.");
    return false;
  } catch (e) {
    console.warn("Unity native ad bridge error:", e);
    return false;
  } finally {
    // Ad ko chalne ke dauran duplicate request rokne ke liye.
    setTimeout(() => {
      adInFlight = false;
    }, requested ? 60000 : 1000);
  }
}

export async function isNativeAdReady(): Promise<boolean> {
  if (!isNativeApp()) return true;
  try {
    const { available } = await UnityAdsNative.isAvailable();
    return available;
  } catch (e) {
    console.warn('Unity native ad bridge error:', e);
    return false;
  }
}


export async function showNativeUnityBanner(topMarginDp = 62): Promise<boolean> {
  if (!isNativeApp()) return false;
  try {
    const result = await UnityAdsNative.showBanner({ topMarginDp });
    if (!result.shown) {
      console.warn('UNITY BANNER FAILURE:', result.error || 'No error returned');
    }
    return !!result.shown;
  } catch (e) {
    console.warn('Unity banner bridge error:', e);
    return false;
  }
}

export async function hideNativeUnityBanner(): Promise<void> {
  if (!isNativeApp()) return;
  try {
    await UnityAdsNative.hideBanner();
  } catch (e) {
    console.warn('Unity banner hide error:', e);
  }
}

export function moveNativeUnityBanner(topDp: number): void {
  if (!isNativeApp()) return;
  UnityAdsNative.moveBanner({ topDp }).catch(() => {});
}
