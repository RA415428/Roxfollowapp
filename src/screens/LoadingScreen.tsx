import React, { useEffect, useState } from 'react';
import { APP_LOGO_SRC } from '../assets/appLogoBase64';
import { fetchServerAdminConfig } from '../utils/storage';
import { getApiUrl } from '../utils/storage';

interface LoadingScreenProps {
  isOnline?: boolean;
  retryCount?: number;
}

export const LoadingScreen: React.FC<LoadingScreenProps> = ({
  isOnline = true,
  retryCount = 0,
}) => {
  const [showRetry, setShowRetry] = useState(false);

  useEffect(() => {
    let timer: NodeJS.Timeout;

    const attemptConnect = async () => {
      if (retryCount >= 3) {
        setShowRetry(true);
        return;
      }

      try {
        const res = await fetch(getApiUrl('/api/config'), {
          method: 'GET',
          cache: 'no-store',
          signal: AbortSignal.timeout(5000)
        });

        if (res.ok) {
          await fetchServerAdminConfig();
          // Connection established - proceed to main app
          timer = setTimeout(() => {
            // Signal parent to proceed
          }, 100);
        } else {
          retryCount += 1;
          attemptConnect();
        }
      } catch {
        retryCount += 1;
        attemptConnect();
      }
    };

    attemptConnect();

    return () => clearTimeout(timer);
  }, [isOnline, retryCount]);

  return (
    <main className="min-h-screen w-full bg-[#0f0412] flex flex-col items-center justify-center text-center p-6">
      <div className="relative flex items-center justify-center w-24 h-24 mb-7">
        <div
          aria-hidden="true"
          className="absolute inset-0 rounded-full border-[3px] border-pink-400/20 border-t-pink-400 border-r-purple-500 animate-spin"
        />
        <div className="w-16 h-16 rounded-2xl overflow-hidden bg-slate-900 border border-slate-700 shadow-lg shadow-pink-500/20">
          <img
            src={APP_LOGO_SRC}
            alt="ROX FOLLOW"
            className="w-full h-full object-cover"
            onError={(e) => {
              (e.currentTarget as HTMLImageElement).src = '/app_logo.png';
            }}
          />
        </div>
      </div>

      {showRetry && (
        <div className="bg-slate-900/95 border border-purple-500/30 rounded-2xl p-4 mb-6 text-center">
          <p className="text-sm text-purple-300 mb-2">
            Connection timed out. Opening app with cached data...
          </p>
          <button
            onClick={() => window.location.reload()}
            className="px-4 py-2 bg-purple-600 text-white rounded text-sm font-bold"
          >
            Retry Connection
          </button>
        </div>
      )}

      {!isOnline && !showRetry && (
        <p
          role="status"
          className="text-sm font-semibold text-amber-400 max-w-xs"
        >
          No internet connection. App will proceed with cached data.
        </p>
      )}

      {!isOnline && showRetry && (
        <p
          role="status"
          className="text-sm font-semibold text-red-500 max-w-xs"
        >
          Please check your internet connection
        </p>
      )}
    </main>
  );
};