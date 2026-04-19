import { render, screen, fireEvent } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { QueryState } from "@/components/server-state/QueryState";
import { getApiErrorMessage } from "@/lib/server-state/errors";
import { HttpError } from "@/lib/http-client";
import {
  setAuthSession,
  getAuthSession,
  clearAuthSession,
  shouldAttemptAuthRefresh,
} from "@/lib/server-state/auth-store";

// ---------------------------------------------------------------------------
// getApiErrorMessage
// ---------------------------------------------------------------------------

describe("getApiErrorMessage", () => {
  it("returns the HttpError message", () => {
    const error = new HttpError("Validation failed", 400);
    expect(getApiErrorMessage(error, "fallback")).toBe("Validation failed");
  });

  it("returns the message from a plain Error", () => {
    const error = new Error("Network error");
    expect(getApiErrorMessage(error, "fallback")).toBe("Network error");
  });

  it("returns the fallback for an unknown value", () => {
    expect(getApiErrorMessage({}, "fallback")).toBe("fallback");
    expect(getApiErrorMessage(null, "fallback")).toBe("fallback");
    expect(getApiErrorMessage(undefined, "fallback")).toBe("fallback");
  });
});

// ---------------------------------------------------------------------------
// auth-store
// ---------------------------------------------------------------------------

describe("auth-store", () => {
  beforeEach(() => {
    clearAuthSession();
  });

  it("returns null when no session is stored", () => {
    expect(getAuthSession()).toBeNull();
  });

  it("stores and retrieves a session", () => {
    setAuthSession({ accessToken: "tok-123", expiresInMilliseconds: 5_000 });
    expect(getAuthSession()).toEqual({
      accessToken: "tok-123",
      expiresAt: expect.any(Number),
    });
  });

  it("stores the access token in memory and only persists the refresh hint", () => {
    const setItemSpy = vi.spyOn(Storage.prototype, "setItem");
    const removeItemSpy = vi.spyOn(Storage.prototype, "removeItem");

    setAuthSession({ accessToken: "tok-123", expiresInMilliseconds: 5_000 });

    expect(setItemSpy).toHaveBeenCalledWith("r8n.refresh-session-expected", "true");
    expect(removeItemSpy).not.toHaveBeenCalled();
    expect(getAuthSession()).toEqual({
      accessToken: "tok-123",
      expiresAt: expect.any(Number),
    });
    expect(shouldAttemptAuthRefresh()).toBe(true);
  });

  it("clears the session", () => {
    setAuthSession({ accessToken: "tok-123", expiresInMilliseconds: 5_000 });
    clearAuthSession();
    expect(getAuthSession()).toBeNull();
    expect(shouldAttemptAuthRefresh()).toBe(false);
  });
});

// ---------------------------------------------------------------------------
// QueryState
// ---------------------------------------------------------------------------

describe("QueryState", () => {
  it("shows loading message while loading", () => {
    render(<QueryState isLoading>content</QueryState>);
    expect(screen.getByText("Loading...")).toBeInTheDocument();
    expect(screen.queryByText("content")).not.toBeInTheDocument();
  });

  it("shows a custom loading message", () => {
    render(<QueryState isLoading loadingMessage="Fetching data...">content</QueryState>);
    expect(screen.getByText("Fetching data...")).toBeInTheDocument();
  });

  it("shows error state with message from error object", () => {
    const error = new HttpError("Not found", 404);
    render(<QueryState isError error={error}>content</QueryState>);
    expect(screen.getByText("Unable to load")).toBeInTheDocument();
    expect(screen.getByText("Not found")).toBeInTheDocument();
  });

  it("shows error state with explicit errorMessage over error object", () => {
    const error = new HttpError("Not found", 404);
    render(<QueryState isError error={error} errorMessage="Custom error">content</QueryState>);
    expect(screen.getByText("Custom error")).toBeInTheDocument();
    expect(screen.queryByText("Not found")).not.toBeInTheDocument();
  });

  it("shows retry button and calls onRetry when clicked", () => {
    let retried = false;
    render(
      <QueryState isError onRetry={() => { retried = true; }}>
        content
      </QueryState>,
    );
    fireEvent.click(screen.getByRole("button", { name: "Try again" }));
    expect(retried).toBe(true);
  });

  it("shows empty message when isEmpty is true", () => {
    render(<QueryState isEmpty>content</QueryState>);
    expect(screen.getByText("Nothing to show yet.")).toBeInTheDocument();
    expect(screen.queryByText("content")).not.toBeInTheDocument();
  });

  it("shows a custom empty message", () => {
    render(<QueryState isEmpty emptyMessage="No results found.">content</QueryState>);
    expect(screen.getByText("No results found.")).toBeInTheDocument();
  });

  it("renders children when not loading, not error, not empty", () => {
    render(<QueryState><span>my content</span></QueryState>);
    expect(screen.getByText("my content")).toBeInTheDocument();
  });
});
