/**
 * Notification Service
 *
 * Calls the backend subscription endpoints so category subscriptions are
 * synced server-side.
 */

import type { Category } from "../../types";
import axiosInstance from "../../api/axiosInstance";
import { resolveCategoryId } from "../../api/communityApi";

async function subscribeToCategory(category: Category): Promise<void> {
  const categoryId = await resolveCategoryId(category);
  await axiosInstance.post(`/subscriptions/categories/${categoryId}`);
}

export async function syncSubscription(payload: {
  email: string;
  categories: Category[];
  enabled: boolean;
}): Promise<void> {
  if (!payload.enabled || payload.categories.length === 0) {
    return;
  }

  const uniqueCategories = [...new Set(payload.categories)];
  const results = await Promise.allSettled(
    uniqueCategories.map((category) => subscribeToCategory(category)),
  );

  const failedCount = results.filter(
    (result) => result.status === "rejected",
  ).length;

  if (failedCount > 0) {
    throw new Error(
      failedCount === uniqueCategories.length
        ? "We couldn't save your category subscriptions right now. Please try again."
        : "Some category subscriptions could not be saved. Please try again.",
    );
  }
}

export async function unsubscribeAll(_email: string): Promise<void> {
  // No unsubscribe endpoint has been provided by the backend yet.
  return Promise.resolve();
}
