export interface SessionTokens {
  accessToken: string;
  expiresInMilliseconds: number;
}

export interface SessionSnapshot {
  accessToken: string;
  expiresAt: number;
}

export type SessionRefreshHandler = () => Promise<SessionTokens>;

let session: SessionSnapshot | null = null;
let refreshHandler: SessionRefreshHandler | null = null;
let refreshPromise: Promise<SessionSnapshot> | null = null;

export function configureSessionRefresh(
  handler: SessionRefreshHandler | null,
): void {
  refreshHandler = handler;
}

export function setSession(
  tokens: SessionTokens,
  now: number = Date.now(),
): SessionSnapshot {
  session = {
    accessToken: tokens.accessToken,
    expiresAt: now + Math.max(0, tokens.expiresInMilliseconds),
  };

  return cloneSession(session);
}

export function clearSession(): void {
  session = null;
  refreshPromise = null;
}

export function getSession(): SessionSnapshot | null {
  return cloneSession(session);
}

export function hasValidSession(now: number = Date.now()): boolean {
  return session !== null && session.expiresAt > now;
}

export function getAccessToken(now: number = Date.now()): string | null {
  if (!hasValidSession(now)) {
    return null;
  }

  return session?.accessToken ?? null;
}

export async function refreshSession(): Promise<SessionSnapshot> {
  if (refreshPromise) {
    return refreshPromise;
  }

  if (!refreshHandler) {
    throw new Error("Session refresh handler is not configured.");
  }

  refreshPromise = (async () => {
    try {
      const refreshedTokens = await refreshHandler();
      return setSession(refreshedTokens);
    } catch (error) {
      clearSession();
      throw error;
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}

function cloneSession(value: SessionSnapshot | null): SessionSnapshot | null {
  if (!value) {
    return null;
  }

  return { ...value };
}
