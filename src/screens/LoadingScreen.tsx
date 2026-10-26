import React from 'react';
import { APP_LOGO_SRC } from '../assets/appLogoBase64';

interface LoadingScreenProps {
  isOnline?: boolean;
}

export const LoadingScreen: React.FC<LoadingScreenProps> = ({
  isOnline = true,
}) => {
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

      {!isOnline && (
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
