import { doc, runTransaction, serverTimestamp } from 'firebase/firestore';
import { db } from '../lib/firebase';

const USER_CACHE_KEY = 'rox_cached_user';
const WALLET_CACHE_KEY = 'rox_cached_wallet';

export function getCachedUser() {
  const cached = localStorage.getItem(USER_CACHE_KEY);
  return cached ? JSON.parse(cached) : null;
}

export function setCachedUser(userData: any) {
  localStorage.setItem(USER_CACHE_KEY, JSON.stringify(userData));
}

export function getCachedWallet() {
  const cached = localStorage.getItem(WALLET_CACHE_KEY);
  return cached ? JSON.parse(cached) : null;
}

export async function awardCoinsSafely(userId: string, amount: number, transactionId: string): Promise<boolean> {
  if (!userId || amount <= 0 || !transactionId) return false;

  const userRef = doc(db, 'users', userId);
  const txRef = doc(db, 'transactions', `${userId}_${transactionId}`);

  try {
    await runTransaction(db, async (transaction) => {
      const existingTx = await transaction.get(txRef);
      if (existingTx.exists()) {
        throw new Error('Transaction already processed.');
      }

      const userDoc = await transaction.get(userRef);
      if (!userDoc.exists()) {
        throw new Error('User document does not exist.');
      }

      const currentCoins = userDoc.data().coins || 0;
      const newCoins = currentCoins + amount;

      transaction.update(userRef, {
        coins: newCoins,
        lastUpdated: serverTimestamp(),
      });

      transaction.set(txRef, {
        userId,
        amount,
        type: 'REWARD',
        createdAt: serverTimestamp(),
      });
    });

    const currentCache = getCachedWallet() || {};
    localStorage.setItem(WALLET_CACHE_KEY, JSON.stringify({ ...currentCache, coins: (currentCache.coins || 0) + amount }));

    return true;
  } catch (error) {
    console.error('Atomic balance update failed:', error);
    return false;
  }
}
