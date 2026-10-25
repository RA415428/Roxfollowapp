import React from 'react';
import { Loader2, Zap } from 'lucide-react';
import { APP_LOGO_SRC } from '../assets/appLogoBase64';

interface LoadingScreenProps { isOnline?: boolean; }

export const LoadingScreen: React.FC<LoadingScreenProps> = ({ isOnline = true }) => {
  return (
    <div className="min-h-screen bg-slate-950 flex flex-col items-center justify-center p-6 text-center select-none relative overflow-hidden">
      {/* Background ambient glow */}
      <div className="absolute top-1/3 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-pink-500/10 rounded-full blur-3xl pointer-events-none" />

      <div className="bg-slate-900/90 border border-slate-800 rounded-3xl p-6 w-full max-w-xs shadow-2xl space-y-5 relative backdrop-blur-md">
        {/* App Logo Header */}
        <div className="flex flex-col items-center">
          <div className="relative mb-3">
            <div className="w-16 h-16 rounded-2xl overflow-hidden shadow-xl shadow-pink-500/20 border border-slate-700/60 bg-slate-950 flex items-center justify-center p-0.5">
              <img
                src={APP_LOGO_SRC}
                alt="ROX FOLLOW Logo"
                className="w-full h-full object-cover rounded-[14px]"
                onError={(e) => {
                  (e.currentTarget as HTMLImageElement).src = '/app_logo.png';
                }}
              />
            </div>
            <div className="absolute -bottom-1 -right-1 bg-gradient-to-r from-pink-500 to-purple-600 text-white p-1 rounded-lg shadow-md">
              <Zap className="w-3 h-3 fill-current" />
            </div>
          </div>

          <h1 className="text-lg font-black bg-gradient-to-r from-white via-slate-100 to-amber-300 bg-clip-text text-transparent tracking-tight">
            ROX FOLLOW
          </h1>
          <p className="text-[10px] text-slate-400 font-semibold uppercase tracking-wider mt-0.5">
            SMM Growth Engine
          </p>
        </div>

        <div className="h-px bg-slate-800/80 w-full" />

        <div className="flex flex-col items-center gap-3 pt-2">
          <Loader2 className="w-6 h-6 animate-spin text-pink-400" />
          <p className="text-[11px] text-slate-400 font-medium">
            Please wait...
          </p>
          {!isOnline && (
            <p role="status" className="text-sm font-semibold text-red-500">
              Please check your internet connection
            </p>
          )}
        </div>
      </div>
    </div>
  );
};
