import {
  clearSession,
  getSession,
  setSession,
  type SessionTokens,
} from "@/lib/auth/session";

export function getAuthSession() {
  return getSession();
}

export function setAuthSession(session: SessionTokens | null): void {
  if (!session) {
    clearSession();
    return;
  }

  setSession(session);
}

export function clearAuthSession(): void {
  clearSession();
}
