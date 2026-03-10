import { useState, useEffect, useCallback } from "react";
import type { MouseEvent } from "react";
import { CATEGORIES, type Category } from "../../types";
import { useAuthStore } from "../../features/auth/authStore";
import { useSubscriptionStore } from "../../features/subscriptions/subscriptionStore";
import { syncSubscription } from "../../features/subscriptions/notificationService";
import { useToast } from "../atoms/Toast";

const USE_MOCK = import.meta.env.VITE_USE_MOCK === "true";

// ── Category accent colours matching Badge ─────────────────────────────────
const CAT_COLORS: Record<Category, string> = {
  Events: "bg-violet-100 text-violet-700 border-violet-200",
  "Lost & Found": "bg-red-100 text-red-600 border-red-200",
  Recommendations: "bg-emerald-100 text-emerald-700 border-emerald-200",
  "Help Requests": "bg-amber-100 text-amber-700 border-amber-200",
  News: "bg-blue-100 text-blue-700 border-blue-200",
};

const CAT_ICONS: Record<Category, string> = {
  Events: "🎉",
  "Lost & Found": "🔍",
  Recommendations: "⭐",
  "Help Requests": "🙋",
  News: "📰",
};

interface SubscriptionModalProps {
  onClose: () => void;
}

export function SubscriptionModal({ onClose }: SubscriptionModalProps) {
  const user = useAuthStore((s) => s.user);
  const toast = useToast();
  const { getFor, toggleCategory, toggleEnabled, setEmail } =
    useSubscriptionStore();

  const sub = getFor(user!.email);
  const [notifyEmail, setNotifyEmail] = useState(sub.email || user!.email);
  const [saving, setSaving] = useState(false);
  const [emailError, setEmailError] = useState("");

  // Keep local email state in sync if user switches accounts
  useEffect(() => {
    setNotifyEmail(sub.email || user!.email);
  }, [user!.email]);

  const EMAIL_RE = /\S+@\S+\.\S+/;

  const handleSave = useCallback(async () => {
    if (!EMAIL_RE.test(notifyEmail)) {
      setEmailError("Please enter a valid email address.");
      return;
    }
    setEmailError("");
    setSaving(true);

    // Persist email update locally
    setEmail(user!.email, notifyEmail);
    const updated = {
      email: notifyEmail,
      categories: sub.categories,
      enabled: sub.enabled,
    };

    if (!USE_MOCK) {
      await syncSubscription({
        ...updated,
        categories: updated.categories as Category[],
      });
    }

    setSaving(false);
    toast("Notification preferences saved!");
    onClose();
  }, [notifyEmail, sub, user, setEmail, toast, onClose]);

  const handleToggleGlobal = useCallback(() => {
    toggleEnabled(user!.email);
  }, [toggleEnabled, user]);

  const handleToggleCategory = useCallback(
    (cat: Category) => {
      toggleCategory(user!.email, cat);
    },
    [toggleCategory, user],
  );

  // Close on backdrop click
  const handleOverlayClick = useCallback(
    (e: MouseEvent<HTMLDivElement>) => {
      if (e.target === e.currentTarget) onClose();
    },
    [onClose],
  );

  const isEnabled = sub.enabled;
  const selectedCats = sub.categories;

  return (
    <div
      data-testid="subscription-modal-overlay"
      className="fixed inset-0 bg-black/45 z-[500] flex items-center justify-center p-4"
      onClick={handleOverlayClick}
    >
      <div
        data-testid="subscription-modal"
        className="modal-in bg-white rounded-xl shadow-2xl w-full max-w-md overflow-hidden"
      >
        {/* ── Header ── */}
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-gray-100">
          <div className="flex items-center gap-2.5">
            <span className="text-xl">🔔</span>
            <div>
              <h2
                data-testid="subscription-modal-title"
                className="text-base font-bold text-gray-900"
              >
                Email Notifications
              </h2>
              <p className="text-xs text-gray-400 mt-0.5">
                Get notified when new posts are added
              </p>
            </div>
          </div>
          <button
            data-testid="subscription-modal-close-btn"
            onClick={onClose}
            className="text-gray-400 hover:text-gray-700 text-xl leading-none p-1 rounded"
          >
            ×
          </button>
        </div>

        <div className="px-6 py-5 flex flex-col gap-5">
          {/* ── Global toggle ── */}
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-semibold text-gray-800">
                Enable notifications
              </p>
              <p className="text-xs text-gray-400">
                Turn off to pause all email alerts
              </p>
            </div>
            <button
              data-testid="subscription-global-toggle"
              onClick={handleToggleGlobal}
              className={`relative w-11 h-6 rounded-full transition-colors duration-200 focus:outline-none ${
                isEnabled ? "bg-navy" : "bg-gray-200"
              }`}
              role="switch"
              aria-checked={isEnabled}
            >
              <span
                className={`absolute top-0.5 left-0.5 w-5 h-5 rounded-full bg-white shadow-sm transition-transform duration-200 ${
                  isEnabled ? "translate-x-5" : "translate-x-0"
                }`}
              />
            </button>
          </div>

          {/* ── Category toggles ── */}
          <div
            className={`transition-opacity duration-200 ${isEnabled ? "opacity-100" : "opacity-40 pointer-events-none"}`}
          >
            <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-3">
              Notify me for new posts in:
            </p>
            <div className="grid grid-cols-1 gap-2">
              {CATEGORIES.map((cat) => {
                const active = selectedCats.includes(cat);
                return (
                  <button
                    key={cat}
                    data-testid={`subscription-category-${cat.toLowerCase().replace(/[^a-z]/g, "-")}`}
                    data-active={active}
                    onClick={() => handleToggleCategory(cat)}
                    className={`flex items-center gap-3 px-3.5 py-2.5 rounded-lg border text-sm font-medium transition-all duration-150 text-left ${
                      active
                        ? `${CAT_COLORS[cat]} border-current`
                        : "bg-white text-gray-600 border-gray-200 hover:border-gray-300 hover:bg-gray-50"
                    }`}
                  >
                    <span className="text-base flex-shrink-0">
                      {CAT_ICONS[cat]}
                    </span>
                    <span className="flex-1">{cat}</span>
                    <span
                      className={`w-4 h-4 rounded border flex-shrink-0 flex items-center justify-center transition-colors ${
                        active ? "bg-current border-current" : "border-gray-300"
                      }`}
                    >
                      {active && (
                        <span className="text-white text-[10px] leading-none">
                          ✓
                        </span>
                      )}
                    </span>
                  </button>
                );
              })}
            </div>

            {selectedCats.length === 0 && (
              <p className="text-xs text-amber-600 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2 mt-3">
                ⚠ Select at least one category to receive notifications.
              </p>
            )}
          </div>

          {/* ── Email address ── */}
          <div
            className={`transition-opacity duration-200 ${isEnabled ? "opacity-100" : "opacity-40 pointer-events-none"}`}
          >
            <label className="block text-xs font-medium text-gray-700 mb-1.5">
              Send notifications to
            </label>
            <input
              data-testid="subscription-email-input"
              type="email"
              value={notifyEmail}
              onChange={(e) => {
                setNotifyEmail(e.target.value);
                setEmailError("");
              }}
              className={`w-full px-3 py-2.5 border rounded-lg text-sm focus:outline-none transition-colors ${
                emailError
                  ? "border-red-400 bg-red-50"
                  : "border-gray-200 focus:border-navy"
              }`}
              placeholder="you@example.com"
            />
            {emailError && (
              <p
                data-testid="subscription-email-error"
                className="text-xs text-red-500 mt-1"
              >
                ⚠ {emailError}
              </p>
            )}
            {USE_MOCK && (
              <p className="text-xs text-gray-400 mt-1.5 flex items-center gap-1">
                <span>ℹ</span>
                Mock mode: emails are simulated as in-app toasts. Real emails
                sent when connected to the backend.
              </p>
            )}
          </div>

          {/* ── Actions ── */}
          <div className="flex justify-end gap-2 pt-1">
            <button
              data-testid="subscription-cancel-btn"
              onClick={onClose}
              className="px-4 py-2 text-sm font-semibold text-gray-700 bg-gray-100 border border-gray-200 rounded-lg hover:bg-gray-200 transition-colors"
            >
              Cancel
            </button>
            <button
              data-testid="subscription-save-btn"
              onClick={handleSave}
              disabled={saving}
              className="px-4 py-2 text-sm font-semibold text-white bg-navy rounded-lg hover:bg-[#1a3347] transition-colors disabled:opacity-60 disabled:cursor-not-allowed flex items-center gap-2"
            >
              {saving ? (
                <>
                  <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Saving…
                </>
              ) : (
                "Save preferences"
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
