import {
  decodeJwt,
  initials,
  isTokenValid,
  timeAgo,
  toErrorMessage,
  USERNAME_HELP_TEXT,
  USERNAME_TAKEN_MESSAGE,
  validateUsername,
} from "../utils";

function encodeBase64Url(value: string) {
  return btoa(value)
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/g, "");
}

function createJwt(payload: Record<string, unknown>) {
  const header = encodeBase64Url(JSON.stringify({ alg: "HS256", typ: "JWT" }));
  const body = encodeBase64Url(JSON.stringify(payload));
  return `${header}.${body}.signature`;
}

describe("utils", () => {
  describe("timeAgo", () => {
    it("returns just now for a date 10 seconds ago", () => {
      expect(timeAgo(new Date(Date.now() - 10_000).toISOString())).toBe(
        "just now",
      );
    });

    it("returns about X min ago for a date 5 minutes ago", () => {
      expect(timeAgo(new Date(Date.now() - 5 * 60_000).toISOString())).toBe(
        "about 5 min ago",
      );
    });

    it("returns about X hours ago for a date 2 hours ago", () => {
      expect(
        timeAgo(new Date(Date.now() - 2 * 60 * 60_000).toISOString()),
      ).toBe("about 2 hours ago");
    });

    it("returns X days ago for a date 3 days ago", () => {
      expect(
        timeAgo(new Date(Date.now() - 3 * 24 * 60 * 60_000).toISOString()),
      ).toBe("3 days ago");
    });
  });

  describe("initials", () => {
    it("returns initials for a full name", () => {
      expect(initials("John Doe")).toBe("JD");
    });

    it("returns the first initial for a single word name", () => {
      expect(initials("Alice")).toBe("A");
    });

    it("returns a fallback for an empty string", () => {
      expect(initials("")).toBe("?");
    });

    it("returns a fallback for undefined", () => {
      expect(initials(undefined as unknown as string)).toBe("?");
    });
  });

  describe("decodeJwt", () => {
    it("returns null for a malformed string", () => {
      expect(decodeJwt("not-a-jwt")).toBeNull();
    });

    it("returns null if token has fewer than 3 parts", () => {
      expect(decodeJwt("one.two")).toBeNull();
    });

    it("returns the decoded payload for a valid token", () => {
      const payload = {
        sub: "123",
        email: "alice@example.com",
        exp: 2_000_000_000,
      };

      expect(decodeJwt(createJwt(payload))).toEqual(payload);
    });
  });

  describe("isTokenValid", () => {
    it("returns false for null", () => {
      expect(isTokenValid(null)).toBe(false);
    });

    it("returns false for a malformed token", () => {
      expect(isTokenValid("bad.token")).toBe(false);
    });

    it("returns true for a token with a future exp", () => {
      expect(
        isTokenValid(createJwt({ exp: Math.floor(Date.now() / 1000) + 3600 })),
      ).toBe(true);
    });

    it("returns false for a token with an exp in the past", () => {
      expect(
        isTokenValid(createJwt({ exp: Math.floor(Date.now() / 1000) - 3600 })),
      ).toBe(false);
    });
  });

  describe("toErrorMessage", () => {
    it("returns a plain string error unchanged", () => {
      expect(toErrorMessage("Invalid credentials")).toBe("Invalid credentials");
    });

    it("returns an Error message when present", () => {
      expect(toErrorMessage(new Error("Backend unavailable"))).toBe(
        "Backend unavailable",
      );
    });

    it("falls back for empty values", () => {
      expect(toErrorMessage("", "Fallback message")).toBe("Fallback message");
      expect(toErrorMessage(null, "Fallback message")).toBe("Fallback message");
    });
  });

  describe("validateUsername", () => {
    it("accepts valid usernames", () => {
      expect(validateUsername("kofi_osei")).toBeNull();
      expect(validateUsername("Gertrude.98")).toBeNull();
    });

    it("rejects usernames that are too short", () => {
      expect(validateUsername("ab")).toBe(
        "Username must be at least 3 characters.",
      );
    });

    it("rejects usernames with invalid characters", () => {
      expect(validateUsername("kofi osei")).toBe(
        "Use only letters, numbers, dots, or underscores.",
      );
    });

    it("rejects usernames that start or end with separators", () => {
      expect(validateUsername(".kofi")).toBe(
        "Username cannot start or end with a dot or underscore.",
      );
      expect(validateUsername("kofi_")).toBe(
        "Username cannot start or end with a dot or underscore.",
      );
    });

    it("rejects reserved usernames case-insensitively", () => {
      expect(validateUsername("Admin")).toBe(
        "This username is reserved. Please choose another one.",
      );
    });

    it("exposes username helper copy constants", () => {
      expect(USERNAME_HELP_TEXT).toContain("3-20 characters");
      expect(USERNAME_TAKEN_MESSAGE).toBe(
        "This username is already on our team. Try another?",
      );
    });
  });
});
