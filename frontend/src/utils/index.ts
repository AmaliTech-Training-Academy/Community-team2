export function timeAgo(dateStr: string): string {
  const diff = (Date.now() - new Date(dateStr).getTime()) / 1000;
  if (diff < 60) return "just now";
  if (diff < 3600) return `about ${Math.floor(diff / 60)} min ago`;
  if (diff < 86400)
    return `about ${Math.floor(diff / 3600)} hour${Math.floor(diff / 3600) > 1 ? "s" : ""} ago`;
  return `${Math.floor(diff / 86400)} day${Math.floor(diff / 86400) > 1 ? "s" : ""} ago`;
}

export function initials(name: string): string {
  return (
    name
      ?.split(" ")
      .map((w) => w[0])
      .join("")
      .toUpperCase()
      .slice(0, 2) || "?"
  );
}

export function toErrorMessage(
  error: unknown,
  fallback = "Something went wrong. Please try again.",
): string {
  if (typeof error === "string") {
    const message = error.trim();
    return message || fallback;
  }

  if (error instanceof Error) {
    const message = error.message.trim();
    return message || fallback;
  }

  return fallback;
}

export const USERNAME_MIN_LENGTH = 3;
export const USERNAME_MAX_LENGTH = 20;
export const USERNAME_TAKEN_MESSAGE =
  "This username is already on our team. Try another?";
export const USERNAME_HELP_TEXT =
  "3-20 characters, letters and numbers only. Dots or underscores can be used in the middle.";

const RESERVED_USERNAMES = new Set(["admin", "support", "root", "null"]);

export function validateUsername(username: string): string | null {
  const value = username.trim();

  if (!value) {
    return "Username is required";
  }

  if (value.length < USERNAME_MIN_LENGTH) {
    return `Username must be at least ${USERNAME_MIN_LENGTH} characters.`;
  }

  if (value.length > USERNAME_MAX_LENGTH) {
    return `Username must be ${USERNAME_MAX_LENGTH} characters or fewer.`;
  }

  if (!/^[a-z0-9._]+$/i.test(value)) {
    return "Use only letters, numbers, dots, or underscores.";
  }

  if (/^[._]|[._]$/.test(value)) {
    return "Username cannot start or end with a dot or underscore.";
  }

  if (RESERVED_USERNAMES.has(value.toLowerCase())) {
    return "This username is reserved. Please choose another one.";
  }

  return null;
}

// ── JWT utilities ────────────────────────────────────────────────────────────

export interface JwtPayload {
  sub: string; // subject — usually email or userId
  name?: string;
  email?: string;
  role?: string;
  roles?: string[];
  exp?: number; // expiry — Unix timestamp in seconds
  iat?: number; // issued at
  [key: string]: unknown;
}

/**
 * Decode a JWT without verifying the signature (verification is the backend's job).
 * Returns null on any malformed input so callers can safely handle it.
 */
export function decodeJwt(token: string): JwtPayload | null {
  try {
    const parts = token.split(".");
    if (parts.length !== 3) return null;
    // Base64url → Base64 → JSON
    const payload = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = atob(
      payload.padEnd(payload.length + ((4 - (payload.length % 4)) % 4), "="),
    );
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}

/**
 * Returns true when the token is present and its `exp` claim is still in
 * the future (with a 30-second clock-skew buffer).
 */
export function isTokenValid(token: string | null): boolean {
  if (!token) return false;
  const payload = decodeJwt(token);
  if (!payload) return false;
  if (!payload.exp) return true; // no expiry claim — treat as valid
  return payload.exp * 1000 > Date.now() + 30_000;
}
