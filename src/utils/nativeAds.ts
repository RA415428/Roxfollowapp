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

export async function requestNativeRewardedAd(): Promise<boolean> {
  if (!isNativeApp()) return false;
  try {
    const { available } = await UnityAdsNative.isAvailable();
    if (!available) return false;
    const { requested } = await UnityAdsNative.showRewardedAd();
    return requested;
  } catch (e) {
    console.warn('Unity native ad bridge error:', e);
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
