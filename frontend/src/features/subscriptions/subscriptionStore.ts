import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Category, Subscription } from '../../types';

interface SubscriptionState {
  /** Keyed by user email so subscriptions survive switching accounts on the same device */
  subscriptions: Record<string, Subscription>;
  getFor:          (email: string) => Subscription;
  toggleCategory:  (email: string, category: Category) => void;
  toggleEnabled:   (email: string) => void;
  setEmail:        (userEmail: string, notifyEmail: string) => void;
  isSubscribed:    (email: string, category: Category) => boolean;
}

const defaultSubscription = (email: string): Subscription => ({
  email,
  categories: [],
  enabled: true,
});

export const useSubscriptionStore = create<SubscriptionState>()(
  persist(
    (set, get) => ({
      subscriptions: {},

      getFor: (email) =>
        get().subscriptions[email] ?? defaultSubscription(email),

      toggleCategory: (email, category) =>
        set(state => {
          const current = state.subscriptions[email] ?? defaultSubscription(email);
          const has = current.categories.includes(category);
          return {
            subscriptions: {
              ...state.subscriptions,
              [email]: {
                ...current,
                categories: has
                  ? current.categories.filter(c => c !== category)
                  : [...current.categories, category],
              },
            },
          };
        }),

      toggleEnabled: (email) =>
        set(state => {
          const current = state.subscriptions[email] ?? defaultSubscription(email);
          return {
            subscriptions: {
              ...state.subscriptions,
              [email]: { ...current, enabled: !current.enabled },
            },
          };
        }),

      setEmail: (userEmail, notifyEmail) =>
        set(state => {
          const current = state.subscriptions[userEmail] ?? defaultSubscription(userEmail);
          return {
            subscriptions: {
              ...state.subscriptions,
              [userEmail]: { ...current, email: notifyEmail },
            },
          };
        }),

      isSubscribed: (email, category) => {
        const sub = get().subscriptions[email];
        return !!(sub?.enabled && sub.categories.includes(category));
      },
    }),
    { name: 'ping_subscriptions' }
  )
);
