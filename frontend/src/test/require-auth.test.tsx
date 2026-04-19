import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import RequireAuth from "@/components/auth/RequireAuth";

const {
  clearSessionMock,
  getAccessTokenMock,
  refreshSessionMock,
  shouldAttemptAuthRefreshMock,
  clearRefreshSessionHintMock,
} = vi.hoisted(() => ({
  clearSessionMock: vi.fn(),
  getAccessTokenMock: vi.fn(),
  refreshSessionMock: vi.fn(),
  shouldAttemptAuthRefreshMock: vi.fn(),
  clearRefreshSessionHintMock: vi.fn(),
}));

vi.mock("@/lib/auth/session", () => ({
  clearSession: clearSessionMock,
  getAccessToken: getAccessTokenMock,
  refreshSession: refreshSessionMock,
}));

vi.mock("@/lib/server-state/auth-store", () => ({
  clearRefreshSessionHint: clearRefreshSessionHintMock,
  shouldAttemptAuthRefresh: shouldAttemptAuthRefreshMock,
}));

function renderProtectedRoute(initialEntry: string = "/protected") {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route path="/login" element={<div>Login page</div>} />
        <Route element={<RequireAuth />}>
          <Route path="/protected" element={<div>Protected page</div>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe("RequireAuth", () => {
  beforeEach(() => {
    clearRefreshSessionHintMock.mockReset();
    clearSessionMock.mockReset();
    getAccessTokenMock.mockReset();
    shouldAttemptAuthRefreshMock.mockReset();
    refreshSessionMock.mockReset();
    shouldAttemptAuthRefreshMock.mockReturnValue(true);
  });

  it("renders the protected route when an in-memory access token is already available", async () => {
    getAccessTokenMock.mockReturnValue("stub-access-token-123");

    renderProtectedRoute();

    expect(await screen.findByText("Protected page")).toBeInTheDocument();
    expect(refreshSessionMock).not.toHaveBeenCalled();
    expect(clearSessionMock).not.toHaveBeenCalled();
  });

  it("refreshes the session before rendering a protected route when memory is empty", async () => {
    getAccessTokenMock.mockReturnValue(null);
    refreshSessionMock.mockResolvedValue({
      accessToken: "refreshed-access-token-456",
      expiresAt: Date.now() + 60_000,
    });

    renderProtectedRoute();

    expect(await screen.findByText("Protected page")).toBeInTheDocument();
    expect(refreshSessionMock).toHaveBeenCalledTimes(1);
    expect(clearSessionMock).not.toHaveBeenCalled();
  });

  it("clears the session and redirects to /login when refresh fails", async () => {
    getAccessTokenMock.mockReturnValue(null);
    refreshSessionMock.mockRejectedValue(new Error("Refresh rejected by server"));

    renderProtectedRoute();

    await waitFor(() => {
      expect(screen.getByText("Login page")).toBeInTheDocument();
    });

    expect(clearSessionMock).toHaveBeenCalledTimes(1);
  });

  it("redirects to /login without calling refresh when there is no refresh hint", async () => {
    getAccessTokenMock.mockReturnValue(null);
    shouldAttemptAuthRefreshMock.mockReturnValue(false);

    renderProtectedRoute();

    await waitFor(() => {
      expect(screen.getByText("Login page")).toBeInTheDocument();
    });

    expect(refreshSessionMock).not.toHaveBeenCalled();
    expect(clearSessionMock).toHaveBeenCalledTimes(1);
  });
});
