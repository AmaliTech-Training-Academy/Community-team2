export function timeAgo(dateStr: string): string {
  const diff = (Date.now() - new Date(dateStr).getTime()) / 1000;
  if (diff < 60) return 'just now';
  if (diff < 3600) return `about ${Math.floor(diff / 60)} min ago`;
  if (diff < 86400) return `about ${Math.floor(diff / 3600)} hour${Math.floor(diff / 3600) > 1 ? 's' : ''} ago`;
  return `${Math.floor(diff / 86400)} day${Math.floor(diff / 86400) > 1 ? 's' : ''} ago`;
}

export function initials(name: string): string {
  return name?.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2) || '?';
}

// ── JWT utilities ────────────────────────────────────────────────────────────

export interface JwtPayload {
  sub: string;           // subject — usually email or userId
  name?: string;
  email?: string;
  role?: string;
  roles?: string[];
  exp?: number;          // expiry — Unix timestamp in seconds
  iat?: number;          // issued at
  [key: string]: unknown;
}

/**
 * Decode a JWT without verifying the signature (verification is the backend's job).
 * Returns null on any malformed input so callers can safely handle it.
 */
export function decodeJwt(token: string): JwtPayload | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    // Base64url → Base64 → JSON
    const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = atob(payload.padEnd(payload.length + (4 - (payload.length % 4)) % 4, '='));
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
