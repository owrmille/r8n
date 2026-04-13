import { useSyncExternalStore } from "react";
import {
  clearSession,
  getAccessToken,
  getSession,
  setSession,
  subscribeSession,
  type SessionSnapshot,
  type SessionTokens,
} from "@/lib/auth/session";

export type AuthSession = SessionSnapshot;

export function getAuthSession(): AuthSession | null {
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

function useAuthSession(): AuthSession | null {
  return useSyncExternalStore(
    subscribeSession,
    () => getSession(),
    () => null,
  );
}

export function useAccessToken(): string | null {
  useAuthSession();
  return getAccessToken();
}
