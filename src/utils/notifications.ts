import { Capacitor } from '@capacitor/core';
import {
  FirebaseMessaging,
  type TokenReceivedEvent,
  type NotificationReceivedEvent,
  type NotificationActionPerformedEvent,
} from '@capacitor-firebase/messaging';

export const LAST_ADMIN_NOTIFICATION_KEY = 'roxyefollow_last_admin_notification';

export interface NotificationPayload {
  id: string;
  title: string;
  message: string;
  timestamp: number;
  bannerUrl?: string;
}

const isNativeAndroid = () =>
  Capacitor.isNativePlatform() && Capacitor.getPlatform() === 'android';

/**
 * Initialize native Firebase Cloud Messaging.
 *
 * The Android app subscribes to the all_users topic so that
 * server-sent FCM notifications can reach all app installations.
 */
export const initializeNativePushNotifications = async (): Promise<boolean> => {
  if (!isNativeAndroid()) return false;

  try {
    const permission = await FirebaseMessaging.checkPermissions();

    if (permission.receive !== 'granted') {
      const requested = await FirebaseMessaging.requestPermissions();

      if (requested.receive !== 'granted') {
        console.warn('FCM notification permission was not granted.');
        return false;
      }
    }

    const tokenResult = await FirebaseMessaging.getToken();

    console.log('FCM token received:', tokenResult.token);

    await FirebaseMessaging.subscribeToTopic({
      topic: 'all_users',
    });

    console.log('FCM subscribed to topic: all_users');

    return true;
  } catch (error) {
    console.error('Native FCM initialization failed:', error);
    return false;
  }
};

/**
 * Register native FCM listeners.
 */
export const registerNativePushListeners = async () => {
  if (!isNativeAndroid()) return;

  await FirebaseMessaging.addListener(
    'tokenReceived',
    (event: TokenReceivedEvent) => {
      console.log('FCM token updated:', event.token);
    }
  );

  await FirebaseMessaging.addListener(
    'notificationReceived',
    (event: NotificationReceivedEvent) => {
      console.log('FCM notification received:', event.notification);
    }
  );

  await FirebaseMessaging.addListener(
    'notificationActionPerformed',
    (event: NotificationActionPerformedEvent) => {
      console.log('FCM notification action:', event.notification);
    }
  );
};

/**
 * Initialize native push notifications and listeners.
 */
export const initializePushNotifications = async (): Promise<boolean> => {
  if (!isNativeAndroid()) return false;

  await registerNativePushListeners();
  return initializeNativePushNotifications();
};

/* ------------------------------------------------------------------ */
/* Existing browser notification functionality — kept unchanged       */
/* ------------------------------------------------------------------ */

export const isNotificationSupported = (): boolean => {
  return typeof window !== 'undefined' && 'Notification' in window;
};

export const getNotificationPermission = (): NotificationPermission | 'unsupported' => {
  if (!isNotificationSupported()) return 'unsupported';
  return Notification.permission;
};

export const registerServiceWorker = async () => {
  if (typeof window !== 'undefined' && 'serviceWorker' in navigator) {
    try {
      const reg = await navigator.serviceWorker.register('/sw.js');
      console.log(
        'Service Worker registered successfully for push notifications:',
        reg.scope
      );
      return reg;
    } catch (err) {
      console.warn('Service Worker registration failed:', err);
    }
  }
  return null;
};

export const requestNotificationPermission = async (): Promise<
  NotificationPermission | 'unsupported'
> => {
  if (!isNotificationSupported()) return 'unsupported';

  try {
    const permission = await Notification.requestPermission();

    if (permission === 'granted') {
      await registerServiceWorker();
    }

    return permission;
  } catch (err) {
    console.warn('Notification permission error:', err);
    return Notification.permission;
  }
};

export const sendDeviceNotification = async (
  title: string,
  options?: {
    body?: string;
    icon?: string;
    tag?: string;
  }
) => {
  if (!isNotificationSupported()) return;

  if (Notification.permission === 'granted') {
    try {
      let swReg: ServiceWorkerRegistration | undefined;

      if ('serviceWorker' in navigator) {
        swReg = await navigator.serviceWorker.getRegistration();
      }

      const notifOptions = {
        body: options?.body || 'New update from Roxyefollow',
        icon: options?.icon || '/icon.png',
        tag: options?.tag || 'roxyefollow-notice-' + Date.now(),
        badge: '/icon.png',
      };

      if (swReg && 'showNotification' in swReg) {
        await swReg.showNotification(title, notifOptions);
      } else {
        const notification = new Notification(title, notifOptions);

        notification.onclick = () => {
          window.focus();
          notification.close();
        };
      }
    } catch (err) {
      console.warn('Failed to dispatch device notification:', err);
    }
  }
};

export const broadcastAdminNotification = async (
  title: string,
  message: string,
  bannerUrl?: string,
  adminPassword?: string
): Promise<boolean> => {
  const payload: NotificationPayload = {
    id: 'notif_' + Date.now(),
    title,
    message,
    timestamp: Date.now(),
    bannerUrl,
  };

  try {
    localStorage.setItem(
      LAST_ADMIN_NOTIFICATION_KEY,
      JSON.stringify(payload)
    );
  } catch (err) {
    console.warn('Failed to save last admin notification:', err);
  }

  /*
   * Real server-side FCM broadcast.
   * Firebase Admin credentials stay on the Render backend.
   * Nothing secret is placed in the APK.
   */
  let fcmSent = false;

  try {
    const response = await fetch(
      'https://roxfollowapp-1.onrender.com/api/notifications/send',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          title,
          message,
          bannerUrl,
          adminPassword,
        }),
      }
    );

    const result = await response.json().catch(() => ({}));

    if (response.ok && result?.success) {
      fcmSent = true;
      console.log('FCM notification sent successfully:', result);
    } else {
      console.warn(
        'FCM notification was not sent:',
        result?.error || `HTTP ${response.status}`
      );
    }
  } catch (err) {
    console.warn('FCM backend request failed:', err);
  }

  /*
   * Preserve the existing local/in-app notification behavior.
   */
  sendDeviceNotification(title, { body: message });

  if (typeof window !== 'undefined') {
    window.dispatchEvent(
      new CustomEvent('roxyefollow_push_notification', {
        detail: payload,
      })
    );
  }

  return fcmSent;
};
