import {
  clearSession,
  getSession,
  setSession,
  type SessionTokens,
} from "@/lib/auth/session";

const REFRESH_SESSION_HINT_KEY = "r8n.refresh-session-expected";

export function getAuthSession() {
  return getSession();
}

export function setAuthSession(session: SessionTokens | null): void {
  if (!session) {
    clearSession();
    clearRefreshSessionHint();
    return;
  }

  setSession(session);
  setRefreshSessionHint();
}

export function clearAuthSession(): void {
  clearSession();
  clearRefreshSessionHint();
}

export function shouldAttemptAuthRefresh(): boolean {
  return readStorageValue(REFRESH_SESSION_HINT_KEY) === "true";
}

export function clearRefreshSessionHint(): void {
  writeStorageValue(REFRESH_SESSION_HINT_KEY, null);
}

function setRefreshSessionHint(): void {
  writeStorageValue(REFRESH_SESSION_HINT_KEY, "true");
}

function readStorageValue(key: string): string | null {
  if (typeof window === "undefined") {
    return null;
  }

  try {
    return window.localStorage.getItem(key);
  } catch {
    return null;
  }
}

function writeStorageValue(key: string, value: string | null): void {
  if (typeof window === "undefined") {
    return;
  }

  try {
    if (value === null) {
      window.localStorage.removeItem(key);
      return;
    }

    window.localStorage.setItem(key, value);
  } catch {
    // Ignore storage failures; they should not break auth flow.
  }
}
