/**
 * ─────────────────────────────────────────────────────────────────────────────
 *  Notification Service
 *
 *  In MOCK mode  — shows an in-app toast for each subscriber whose category
 *                  matches the new post, simulating what the backend would do.
 *
 *  In REAL mode  — calls POST /api/notifications/subscribe and the backend
 *                  (Spring Boot + JavaMailSender) sends the actual email.
 *                  The frontend only needs to make sure the subscription is
 *                  registered server-side via syncSubscription().
 * ─────────────────────────────────────────────────────────────────────────────
 */

import type { Post, Category } from '../../types';
import { useSubscriptionStore } from './subscriptionStore';
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

// ── Mock in-app notification ──────────────────────────────────────────────────
// Called by postsStore.createPost() in mock mode so the submitting user gets
// immediate feedback that notifications would have been sent.
//
// We build the notification messages and return them as strings — the caller
// (postsStore or PostModal) passes them to the toast system.
export function getMockNotificationMessages(
  post: Post,
  currentUserEmail: string
): string[] {
  const state = useSubscriptionStore.getState();
  const messages: string[] = [];

  // Iterate every registered subscription
  for (const [userEmail, sub] of Object.entries(state.subscriptions)) {
    if (!sub.enabled) continue;
    if (!sub.categories.includes(post.category as Category)) continue;

    // Don't notify the author about their own post
    if (userEmail === currentUserEmail) continue;

    messages.push(
      `📧 Email sent to ${sub.email} about new "${post.category}" post: "${post.title}"`
    );
  }

  return messages;
}
