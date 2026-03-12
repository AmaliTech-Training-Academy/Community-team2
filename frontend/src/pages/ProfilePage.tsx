import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useAuthStore } from "../features/auth/authStore";
import { useCategoriesStore } from "../features/categories/categoriesStore";
import { useSubscriptionStore } from "../features/subscriptions/subscriptionStore";
import {
  syncSubscription,
  unsubscribeAll,
} from "../features/subscriptions/notificationService";
import { useToast } from "../components/atoms/Toast";
import { initials } from "../utils";
import HomeIcon from "../assets/images/home.svg?react";
import ChevronRightIcon from "../assets/images/chevron-right.svg?react";
import UserIcon from "../assets/images/user.svg?react";
import MailIcon from "../assets/images/mail.svg?react";

const FALLBACK_CATEGORIES = ["Discussion", "News", "Events", "Alert"];

function Toggle({ enabled }: { enabled: boolean }) {
  return (
    <span
      className={`relative inline-flex h-7 w-12 items-center rounded-full transition-colors ${
        enabled ? "bg-blue-gray-light" : "bg-badge-gray"
      }`}
      aria-hidden="true"
    >
      <span
        className={`inline-block h-5 w-5 rounded-full bg-white transition-transform ${
          enabled ? "translate-x-6" : "translate-x-1"
        }`}
      />
    </span>
  );
}

export default function ProfilePage() {
  const user = useAuthStore((s) => s.user);
  const categories = useCategoriesStore((s) => s.categories);
  const fetchCategories = useCategoriesStore((s) => s.fetch);
  const getFor = useSubscriptionStore((s) => s.getFor);
  const setEmail = useSubscriptionStore((s) => s.setEmail);
  const toggleCategory = useSubscriptionStore((s) => s.toggleCategory);
  const toggleEnabled = useSubscriptionStore((s) => s.toggleEnabled);
  const toast = useToast();

  const subscription = useSubscriptionStore(
    useMemo(
      () => (state) => (user?.email ? state.getFor(user.email) : null),
      [user?.email],
    ),
  );
  const [notifyEmail, setNotifyEmail] = useState(user?.email ?? "");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetchCategories();
  }, [fetchCategories]);

  useEffect(() => {
    if (subscription) {
      setNotifyEmail(subscription.email || user?.email || "");
    }
  }, [subscription, user?.email]);

  const categoryOptions = useMemo(
    () => (categories.length > 0 ? categories : FALLBACK_CATEGORIES),
    [categories],
  );

  const handleToggleCategory = useCallback(
    (category: string) => {
      if (!user?.email) return;
      toggleCategory(user.email, category);
    },
    [toggleCategory, user?.email],
  );

  const handleToggleEnabled = useCallback(() => {
    if (!user?.email) return;
    toggleEnabled(user.email);
  }, [toggleEnabled, user?.email]);

  const handleSave = useCallback(async () => {
    if (!user?.email || !subscription) return;

    const trimmedEmail = notifyEmail.trim();
    if (!trimmedEmail || !/\S+@\S+\.\S+/.test(trimmedEmail)) {
      toast("Enter a valid email for notifications", "warning");
      return;
    }

    setSaving(true);
    setEmail(user.email, trimmedEmail);

    try {
      const nextSubscription = {
        ...getFor(user.email),
        email: trimmedEmail,
      };

      if (
        !nextSubscription.enabled ||
        nextSubscription.categories.length === 0
      ) {
        await unsubscribeAll(trimmedEmail);
      } else {
        await syncSubscription(nextSubscription);
      }

      toast("Profile preferences saved");
    } catch (error) {
      toast(
        error instanceof Error
          ? error.message
          : "Unable to save profile preferences right now.",
        "error",
      );
    } finally {
      setSaving(false);
    }
  }, [getFor, notifyEmail, setEmail, subscription, toast, user?.email]);

  if (!user || !subscription) return null;

  return (
    <div
      data-testid="profile-page"
      className="fade-in mx-auto w-full max-w-360"
    >
      <div className="mb-8">
        <div className="inline-flex items-center gap-3 rounded-lg border border-borderstroke bg-background px-4 py-2.5">
          <Link
            to="/"
            className="flex items-center gap-2 text-body-sm font-semibold text-blue-gray-dark hover:opacity-70 transition-opacity"
          >
            <HomeIcon width={18} height={18} />
            <span>Home</span>
          </Link>
          <span className="text-body-sm font-semibold text-blue-gray-dark">
            <ChevronRightIcon width={16} height={16} />
          </span>
          <span className="text-body-sm font-semibold text-blue-gray-dark">
            Profile
          </span>
        </div>
      </div>

      <div className="mb-8 flex flex-col gap-2">
        <h1 className="text-h-lg font-bold text-blue-gray-dark">Profile</h1>
        <p className="text-body-sm text-blue-gray">
          Manage your account details and choose which categories should send
          email alerts.
        </p>
      </div>

      <div className="flex flex-col gap-6 lg:flex-row lg:items-stretch">
        <section
          data-testid="profile-account-card"
          className="rounded-2xl border border-borderstroke bg-white p-6 lg:w-[46%] lg:self-stretch"
        >
          <div className="mb-6 flex items-center gap-4">
            <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-blue-gray-light text-white text-h-sm tracking-normal">
              {initials(user.name)}
            </div>
            <div className="min-w-0">
              <h2 className="text-h-md text-blue-gray-dark">{user.name}</h2>
              <p className="text-body-sm text-blue-gray">
                {user.role === "ADMIN" ? "Administrator" : "Community member"}
              </p>
            </div>
          </div>

          <div className="space-y-4">
            <div className="rounded-xl border border-borderstroke bg-gray-50 px-4 py-3">
              <div className="mb-1 flex items-center gap-2 text-body-sm font-semibold text-blue-gray-dark">
                <UserIcon aria-hidden="true" className="h-4 w-4" />
                Account name
              </div>
              <p className="text-body-sm text-blue-gray">{user.name}</p>
            </div>

            <div className="rounded-xl border border-borderstroke bg-gray-50 px-4 py-3">
              <div className="mb-1 flex items-center gap-2 text-body-sm font-semibold text-blue-gray-dark">
                <MailIcon aria-hidden="true" className="h-4 w-4" />
                Account email
              </div>
              <p className="text-body-sm break-all text-blue-gray">
                {user.email}
              </p>
            </div>

            <div
              className={`rounded-xl border border-borderstroke px-4 py-4 ${
                subscription.enabled ? "bg-gray-50" : "bg-orange-light"
              }`}
            >
              <p className="text-body-md font-semibold text-blue-gray-dark">
                Subscription summary
              </p>
              <p className="mt-1 text-body-sm text-blue-gray">
                {subscription.enabled
                  ? `${subscription.categories.length} categor${subscription.categories.length === 1 ? "y" : "ies"} selected for alerts.`
                  : "Alerts are currently paused for this account."}
              </p>
            </div>
          </div>
        </section>

        <section
          data-testid="profile-subscriptions-card"
          className="rounded-2xl border border-borderstroke bg-white p-6 lg:w-[54%] lg:self-stretch"
        >
          <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <h2 className="text-h-md text-blue-gray-dark">Email alerts</h2>
              <p className="mt-1 text-body-sm text-blue-gray">
                Choose the categories you want to follow and the email that
                should receive updates.
              </p>
            </div>

            <button
              data-testid="profile-enable-toggle"
              type="button"
              onClick={handleToggleEnabled}
              className="flex shrink-0 self-start items-center gap-3 rounded-xl border border-borderstroke px-3 py-2 text-body-sm font-semibold text-blue-gray-dark transition-colors hover:bg-gray-50 sm:mt-0.5"
            >
              <Toggle enabled={subscription.enabled} />
              <span>{subscription.enabled ? "On" : "Off"}</span>
            </button>
          </div>

          <div className="mb-6">
            <label
              htmlFor="profile-notify-email"
              className="mb-1.5 block text-body-sm text-blue-gray-light"
            >
              Notification email
            </label>
            <div className="relative">
              <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-icon">
                <MailIcon aria-hidden="true" className="h-4 w-4" />
              </span>
              <input
                id="profile-notify-email"
                data-testid="profile-notify-email-input"
                type="email"
                value={notifyEmail}
                onChange={(event) => setNotifyEmail(event.target.value)}
                className="w-full rounded-lg border border-gray-200 bg-gray-100 py-2.5 pl-9 pr-3 text-body-lg text-blue-gray-light placeholder:text-muted-icon transition-colors focus:border-navy focus:bg-white focus:outline-none"
                placeholder="name@example.com"
              />
            </div>
          </div>

          <div className={!subscription.enabled ? "opacity-55" : "opacity-100"}>
            <div className="mb-3 flex items-center justify-between gap-3">
              <h3 className="text-body-lg font-semibold text-blue-gray-dark">
                Categories to follow
              </h3>
              <span className="text-body-sm text-blue-gray">
                {subscription.categories.length} selected
              </span>
            </div>

            {!subscription.enabled && (
              <p className="mb-3 text-body-sm text-blue-gray">
                Alerts are paused. Category selections are saved, but emails
                will not be sent until you turn alerts back on.
              </p>
            )}

            <div className="grid gap-3 sm:grid-cols-2">
              {categoryOptions.map((category) => {
                const selected = subscription.categories.includes(category);

                return (
                  <button
                    key={category}
                    data-testid={`profile-category-btn-${category.toLowerCase().replace(/[^a-z]/g, "-")}`}
                    type="button"
                    onClick={() => handleToggleCategory(category)}
                    className={`rounded-2xl border px-4 py-4 text-left transition-colors ${
                      selected
                        ? "border-blue-gray-light bg-orange-light"
                        : "border-borderstroke bg-gray-50 hover:border-blue-gray"
                    }`}
                  >
                    <div className="flex items-center justify-between gap-3">
                      <span className="text-body-lg font-semibold text-blue-gray-dark">
                        {category}
                      </span>
                      <span
                        className={`flex h-6 w-6 items-center justify-center rounded-full text-body-md ${
                          selected
                            ? "bg-blue-gray-light text-white"
                            : "bg-white text-blue-gray"
                        }`}
                      >
                        {selected ? "✓" : "+"}
                      </span>
                    </div>
                    <p className="mt-2 text-body-sm text-blue-gray">
                      Receive email updates when new posts are published in this
                      category.
                    </p>
                  </button>
                );
              })}
            </div>
          </div>

          <div className="mt-6 flex justify-end">
            <button
              data-testid="profile-save-btn"
              type="button"
              onClick={handleSave}
              disabled={saving}
              className="w-full rounded-lg bg-blue-gray-light px-5 py-2.5 text-body-sm font-semibold text-white transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60 lg:w-1/3"
            >
              {saving ? "Saving…" : "Save preferences"}
            </button>
          </div>
        </section>
      </div>
    </div>
  );
}
