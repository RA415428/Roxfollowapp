import React, { useEffect } from 'react';
import { getCachedUser } from '../utils/storage';

interface LoadingScreenProps {
  onComplete: (isOffline: boolean) => void;
}

export const LoadingScreen: React.FC<LoadingScreenProps> = ({ onComplete }) => {
  useEffect(() => {
    let isSubscribed = true;

    const checkAppInitialization = async () => {
      const isOnline = navigator.onLine;
      const cachedUser = getCachedUser();

      if (!isOnline) {
        if (cachedUser) {
          if (isSubscribed) onComplete(true);
          return;
        }
      }

      const timeoutId = setTimeout(() => {
        if (isSubscribed) {
          if (cachedUser) {
            onComplete(true);
          } else {
            onComplete(false);
          }
        }
      }, 4000);

      return () => clearTimeout(timeoutId);
    };

    checkAppInitialization();

    return () => {
      isSubscribed = false;
    };
  }, [onComplete]);

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-900 text-white">
      <div className="w-12 h-12 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin mb-4"></div>
      <p className="text-sm text-gray-400 font-medium">Loading Rox Follow...</p>
    </div>
  );
};
