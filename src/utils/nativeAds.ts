import { registerPlugin } from '@capacitor/core';

export interface UnityAdsNativePlugin {
  isAvailable(): Promise<{ available: boolean }>;
  showRewardedAd(): Promise<{ requested: boolean }>;
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
  try {
    const { available } = await UnityAdsNative.isAvailable();
    if (!available) {
      adInFlight = false;
      return false;
    }
    const { requested } = await UnityAdsNative.showRewardedAd();
    setTimeout(() => { adInFlight = false; }, 1000);
    return requested;
  } catch (e) {
    console.warn('Unity native ad bridge error:', e);
    adInFlight = false;
    return false;
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
