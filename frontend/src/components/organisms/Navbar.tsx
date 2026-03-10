import { useState, useCallback, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuthStore } from "../../features/auth/authStore";
import { initials } from "../../utils";
import Logo from "../../assets/images/Logo.svg?react";

// ── Inline icons ──────────────────────────────────────────────────────────────

function AnalyticsIcon() {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 16 16"
      fill="none"
      aria-hidden="true"
    >
      <rect x="1.5" y="8.5" width="3" height="6" rx="0.5" fill="currentColor" />
      <rect x="6.5" y="5.5" width="3" height="9" rx="0.5" fill="currentColor" />
      <rect
        x="11.5"
        y="2.5"
        width="3"
        height="12"
        rx="0.5"
        fill="currentColor"
      />
    </svg>
  );
}

function LogoutIcon() {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 16 16"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M6 14H3a1 1 0 0 1-1-1V3a1 1 0 0 1 1-1h3"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M10.5 11L14 8l-3.5-3"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M14 8H6.5"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  );
}

function HamburgerIcon() {
  return (
    <svg
      width="22"
      height="22"
      viewBox="0 0 22 22"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M3 6h16M3 11h16M3 16h16"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  );
}

function CloseIcon() {
  return (
    <svg
      width="24"
      height="24"
      viewBox="0 0 20 20"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M15 5L5 15M5 5l10 10"
        stroke="currentColor"
        strokeWidth="1.75"
        strokeLinecap="round"
      />
    </svg>
  );
}

function UserAvatar({
  name,
  size = "sm",
}: {
  name: string;
  size?: "sm" | "md";
}) {
  const cls = size === "md" ? "w-10 h-10 text-sm" : "w-8 h-8 text-xs";
  return (
    <div
      className={`${cls} rounded-full bg-gray-300 text-blue-gray-dark flex items-center justify-center font-bold shrink-0`}
    >
      {initials(name)}
    </div>
  );
}

// ── Navbar ────────────────────────────────────────────────────────────────────

export function Navbar() {
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const navigate = useNavigate();

  const [drawerOpen, setDrawerOpen] = useState(false);

  useEffect(() => {
    if (!drawerOpen) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prev;
    };
  }, [drawerOpen]);

  const handleLogout = useCallback(() => {
    setDrawerOpen(false);
    logout();
    navigate("/login");
  }, [logout, navigate]);

  return (
    <>
      {/* ── Top bar ──────────────────────────────────────────── */}
      <nav
        data-testid="navbar"
        className="bg-background border-b border-borderstroke sticky top-0 z-40"
      >
        <div className="max-w-5xl mx-auto px-6 h-15 flex items-center">
          <Link data-testid="navbar-logo" to="/" className="shrink-0">
            <Logo />
          </Link>

          <div className="flex-1" />

          {/* ── Desktop right section (md+) ───────────────────── */}
          <div className="hidden md:flex items-center gap-5">
            {/* Analytics — admin only */}
            {user?.role === "ADMIN" && (
              <button
                data-testid="navbar-analytics-link"
                onClick={() => navigate("/dashboard")}
                className="flex items-center gap-1.5 text-body-lg text-blue-gray-dark hover:opacity-70 transition-opacity"
              >
                <AnalyticsIcon />
                Analytics
              </button>
            )}

            {/* User info */}
            <div className="flex items-center gap-2.5">
              <UserAvatar name={user?.name || ""} />
              <div className="flex flex-col leading-tight">
                <span
                  data-testid="navbar-username"
                  className="text-body-sm font-semibold text-blue-gray-dark"
                >
                  {user?.name}
                </span>
                <span
                  data-testid="navbar-email"
                  className="text-[11px] text-gray-400"
                >
                  {user?.email}
                </span>
              </div>
            </div>

            {/* Logout */}
            <button
              data-testid="navbar-logout-btn"
              onClick={handleLogout}
              className="flex items-center gap-1.5 text-body-sm text-red-500 hover:opacity-70 transition-opacity"
            >
              <LogoutIcon />
              Log out
            </button>
          </div>

          {/* ── Mobile hamburger (<md) ─────────────────────────── */}
          <button
            data-testid="navbar-mobile-menu-btn"
            onClick={() => setDrawerOpen(true)}
            className="md:hidden text-blue-gray-dark"
            aria-label="Open menu"
          >
            <HamburgerIcon />
          </button>
        </div>
      </nav>

      {/* ── Mobile full-screen drawer ─────────────────────────── */}
      {drawerOpen && (
        <div
          data-testid="navbar-mobile-drawer"
          className="fixed inset-0 bg-background z-50 flex flex-col md:hidden"
        >
          {/* User header */}
          <div className="flex items-center justify-between px-5 py-4 border-b border-borderstroke">
            <div className="flex items-center gap-3">
              <UserAvatar name={user?.name || ""} size="md" />
              <div className="flex flex-col leading-tight">
                <span className="text-body-lg font-semibold text-blue-gray-dark">
                  {user?.name}
                </span>
                <span className="text-body-sm text-gray-400">
                  {user?.email}
                </span>
              </div>
            </div>
            <button
              data-testid="navbar-mobile-close-btn"
              onClick={() => setDrawerOpen(false)}
              className="text-blue-gray-dark p-1"
              aria-label="Close menu"
            >
              <CloseIcon />
            </button>
          </div>

          {/* Menu items */}
          <div className="flex flex-col px-5">
            {user?.role === "ADMIN" && (
              <>
                <button
                  data-testid="navbar-mobile-analytics-btn"
                  onClick={() => {
                    setDrawerOpen(false);
                    navigate("/dashboard");
                  }}
                  className="flex items-center gap-3 py-4 text-body-lg text-blue-gray-dark text-left hover:opacity-70 transition-opacity"
                >
                  <AnalyticsIcon />
                  Analytics
                </button>
                <div className="border-t border-borderstroke" />
              </>
            )}

            <button
              data-testid="navbar-mobile-logout-btn"
              onClick={handleLogout}
              className="flex items-center gap-3 py-4 text-body-lg text-red-500 text-left hover:opacity-70 transition-opacity"
            >
              <LogoutIcon />
              Log out
            </button>
          </div>
        </div>
      )}
    </>
  );
}
