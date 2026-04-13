import { useSyncExternalStore } from "react";

export interface AuthSession {
  accessToken: string;
  refreshToken?: string;
}

const STORAGE_KEY = "r8n.auth.session";
let currentSession: AuthSession | null = readSession();
const listeners = new Set<() => void>();

function readSession(): AuthSession | null {
  if (typeof window === "undefined") {
    return null;
  }

  const raw = window.sessionStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as AuthSession;
    if (!parsed.accessToken) {
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

function writeSession(session: AuthSession | null) {
  if (typeof window === "undefined") {
    return;
  }

  if (!session) {
    window.sessionStorage.removeItem(STORAGE_KEY);
    return;
  }

  window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
}

function notify() {
  listeners.forEach((listener) => listener());
}

export function getAuthSession(): AuthSession | null {
  return currentSession;
}

export function setAuthSession(session: AuthSession | null): void {
  currentSession = session;
  writeSession(session);
  notify();
}

export function clearAuthSession(): void {
  setAuthSession(null);
}

function useAuthSession(): AuthSession | null {
  return useSyncExternalStore(
    (listener) => {
      listeners.add(listener);

      const storageHandler = (event: StorageEvent) => {
        if (event.key === STORAGE_KEY) {
          currentSession = readSession();
          notify();
        }
      };

      if (typeof window !== "undefined") {
        window.addEventListener("storage", storageHandler);
      }

      return () => {
        listeners.delete(listener);
        if (typeof window !== "undefined") {
          window.removeEventListener("storage", storageHandler);
        }
      };
    },
    () => currentSession,
    () => null,
  );
}

export function useAccessToken(): string | null {
  return useAuthSession()?.accessToken ?? null;
}
