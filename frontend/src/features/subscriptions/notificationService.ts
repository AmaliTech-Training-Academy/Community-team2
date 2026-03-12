/**
 * Notification Service
 *
 * Calls the backend subscription endpoints so notification preferences are
 * synced server-side.
 */

import type { Category } from '../../types';
import axiosInstance from '../../api/axiosInstance';

// ── Backend subscription endpoints ───────────────────────────────────────────
// Called once when the user saves their preferences (real mode only).
export async function syncSubscription(payload: {
  email: string;
  categories: Category[];
  enabled: boolean;
}): Promise<void> {
  try {
    await axiosInstance.post('/notifications/subscribe', payload);
  } catch {
    // Silently swallow — subscription sync is non-critical
    console.warn('[notificationService] Failed to sync subscription to backend.');
  }
}

export async function unsubscribeAll(email: string): Promise<void> {
  try {
    await axiosInstance.delete('/notifications/subscribe', { data: { email } });
  } catch {
    console.warn('[notificationService] Failed to unsubscribe from backend.');
  }
}
