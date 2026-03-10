import { useState, useCallback, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuthStore } from "../../features/auth/authStore";
import { initials } from "../../utils";
import Logo from "../../assets/images/Logo.svg?react";
import AnalyticsIcon from "../../assets/images/analytics.svg?react";
import LogoutIcon from "../../assets/images/logout.svg?react";
import HamburgerIcon from "../../assets/images/hamburger.svg?react";
import CloseIcon from "../../assets/images/close.svg?react";

function UserAvatar({
  name,
  size = "sm",
}: {
  name: string;
  size?: "sm" | "md";
}) {
  const cls = size === "md" ? "h-10 w-10 text-sm" : "h-8 w-8 text-xs";
  return (
    <div
      className={`${cls} flex shrink-0 items-center justify-center rounded-full bg-gray-300 font-medium text-blue-gray-dark`}
    >
      {initials(name)}
    </div>
  );
}

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
      <nav
        data-testid="navbar"
        className="sticky top-0 z-40 border-b border-borderstroke bg-background"
      >
        <div className="mx-auto flex w-full max-w-300 items-start px-6 py-2.5">
          <div className="flex h-10.25 w-full items-center justify-between gap-5">
            <Link data-testid="navbar-logo" to="/" className="shrink-0">
              <Logo className="h-9.5 w-25" />
            </Link>

            {/* ── Desktop right section (md+) ───────────────────── */}
            <div className="hidden items-center gap-5 md:flex">
              {/* Analytics — admin only */}
              {user?.role === "ADMIN" && (
                <button
                  data-testid="navbar-analytics-link"
                  onClick={() => navigate("/dashboard")}
                  className="flex h-10.25 items-center justify-center gap-2 rounded-lg px-5 text-sm font-medium text-[#061C2A] transition-opacity hover:opacity-70"
                >
                  <AnalyticsIcon aria-hidden="true" className="h-5 w-5" />
                  Analytics
                </button>
              )}

              {/* User info */}
              <div className="flex items-center gap-2.5">
                <UserAvatar name={user?.name || ""} />
                <div className="flex flex-col items-start gap-1 leading-none">
                  <span
                    data-testid="navbar-username"
                    className="text-sm font-semibold leading-none text-blue-gray-dark"
                  >
                    {user?.name}
                  </span>
                  <span
                    data-testid="navbar-email"
                    className="text-xs font-normal leading-none text-blue-gray"
                  >
                    {user?.email}
                  </span>
                </div>
              </div>

              {/* Logout */}
              <button
                data-testid="navbar-logout-btn"
                onClick={handleLogout}
                className="flex h-10.25 items-center justify-center gap-2 rounded-lg px-5 text-sm font-medium text-red-600 transition-opacity hover:opacity-70"
              >
                <LogoutIcon aria-hidden="true" className="h-5 w-5" />
                Log out
              </button>
            </div>

            {/* ── Mobile hamburger (<md) ─────────────────────────── */}
            <button
              data-testid="navbar-mobile-menu-btn"
              onClick={() => setDrawerOpen(true)}
              className="text-blue-gray-dark md:hidden"
              aria-label="Open menu"
            >
              <HamburgerIcon aria-hidden="true" className="h-5.5 w-5.5" />
            </button>
          </div>
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
              <CloseIcon aria-hidden="true" className="h-6 w-6" />
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
                  <AnalyticsIcon aria-hidden="true" className="h-5 w-5" />
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
              <LogoutIcon aria-hidden="true" className="h-5 w-5" />
              Log out
            </button>
          </div>
        </div>
      )}
    </>
  );
}
