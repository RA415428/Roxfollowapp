import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.roxfollow.app',
  appName: 'Rox Follow',
  webDir: 'dist',
  android: {
    allowMixedContent: true
  }
};

export default config;
