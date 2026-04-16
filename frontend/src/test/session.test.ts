import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  clearSession,
  configureSessionRefresh,
  getAccessToken,
  getSession,
  hasValidSession,
  refreshSession,
  setSession,
} from "@/lib/auth/session";

describe("session manager", () => {
  beforeEach(() => {
    clearSession();
    configureSessionRefresh(null);
  });

  it("stores the access token only in module memory", () => {
    const setItemSpy = vi.spyOn(Storage.prototype, "setItem");
    const removeItemSpy = vi.spyOn(Storage.prototype, "removeItem");

    const snapshot = setSession(
      {
        accessToken: "stub-access-token-123",
        expiresInMilliseconds: 5_000,
      },
      1_000,
    );

    expect(snapshot).toEqual({
      accessToken: "stub-access-token-123",
      expiresAt: 6_000,
    });
    expect(getSession()).toEqual(snapshot);
    expect(getAccessToken(5_999)).toBe("stub-access-token-123");
    expect(hasValidSession(5_999)).toBe(true);
    expect(setItemSpy).not.toHaveBeenCalled();
    expect(removeItemSpy).not.toHaveBeenCalled();
  });

  it("clears the stored session state", () => {
    setSession(
      {
        accessToken: "stub-access-token-123",
        expiresInMilliseconds: 5_000,
      },
      1_000,
    );

    clearSession();

    expect(getSession()).toBeNull();
    expect(getAccessToken(2_000)).toBeNull();
    expect(hasValidSession(2_000)).toBe(false);
  });

  it("returns null for expired sessions", () => {
    setSession(
      {
        accessToken: "stub-access-token-123",
        expiresInMilliseconds: 250,
      },
      1_000,
    );

    expect(getAccessToken(1_249)).toBe("stub-access-token-123");
    expect(getAccessToken(1_250)).toBeNull();
    expect(hasValidSession(1_250)).toBe(false);
  });

  it("reuses one refresh request for concurrent callers", async () => {
    const refreshHandler = vi.fn().mockImplementation(async () => {
      await Promise.resolve();

      return {
        accessToken: "refreshed-access-token-456",
        expiresInMilliseconds: 1_000,
      };
    });
    configureSessionRefresh(refreshHandler);

    const [first, second] = await Promise.all([
      refreshSession(),
      refreshSession(),
    ]);

    expect(refreshHandler).toHaveBeenCalledTimes(1);
    expect(first).toEqual(second);
    expect(getSession()).toEqual(first);
    expect(getAccessToken(first.expiresAt - 1)).toBe("refreshed-access-token-456");
  });

  it("clears the session when refresh fails", async () => {
    setSession(
      {
        accessToken: "stale-access-token-123",
        expiresInMilliseconds: 5_000,
      },
      1_000,
    );
    configureSessionRefresh(
      vi.fn().mockRejectedValue(new Error("Refresh rejected by server")),
    );

    await expect(refreshSession()).rejects.toThrow("Refresh rejected by server");
    expect(getSession()).toBeNull();
    expect(getAccessToken(2_000)).toBeNull();
  });
});
