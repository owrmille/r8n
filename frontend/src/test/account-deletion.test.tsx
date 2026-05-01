import type { ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Settings from "@/pages/Settings";

const {
  clearSessionMock,
  requestAccountDeletionMock,
  navigateMock,
} = vi.hoisted(() => ({
  clearSessionMock: vi.fn(),
  requestAccountDeletionMock: vi.fn(),
  navigateMock: vi.fn(),
}));

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<typeof import("react-router-dom")>(
    "react-router-dom",
  );
  return { ...actual, useNavigate: () => navigateMock };
});

vi.mock("@/lib/api/users", () => ({
  usersApi: {
    getMe: vi.fn(),
    getUser: vi.fn(),
    getUserAvatar: vi.fn(),
    requestAccountDeletion: requestAccountDeletionMock,
  },
}));

vi.mock("@/lib/auth/session", () => ({
  clearSession: clearSessionMock,
  configureSessionRefresh: vi.fn(),
  getAccessToken: vi.fn(),
  getSession: vi.fn(),
  hasValidSession: vi.fn(),
  refreshSession: vi.fn(),
  setSession: vi.fn(),
  subscribeSession: vi.fn(() => () => {}),
}));

function renderSettings(queryClient: QueryClient): ReturnType<typeof render> {
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={["/settings"]}>
        <Settings />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

function makeQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
}

describe("account deletion", () => {
  beforeEach(() => {
    clearSessionMock.mockReset();
    requestAccountDeletionMock.mockReset();
    navigateMock.mockReset();
  });

  it("clears session and redirects to login after successful deletion", async () => {
    requestAccountDeletionMock.mockResolvedValue(undefined);
    const queryClient = makeQueryClient();
    queryClient.setQueryData(["users", "me"], {
      id: "00000000-0000-0000-0000-000000000000",
      name: "Test Testsson",
      email: "test@test.test",
    });

    renderSettings(queryClient);

    fireEvent.click(screen.getByRole("button", { name: "Delete account" }));

    const input = await screen.findByLabelText("Type your email to confirm");
    fireEvent.change(input, { target: { value: "test@test.test" } });

    fireEvent.click(screen.getByRole("button", { name: "Delete Account" }));

    await waitFor(() => {
      expect(requestAccountDeletionMock).toHaveBeenCalledWith({ email: "test@test.test" });
    });

    expect(clearSessionMock).toHaveBeenCalledTimes(1);
    expect(navigateMock).toHaveBeenCalledWith("/login", { replace: true });
  });

  it("does not clear session when deletion fails", async () => {
    requestAccountDeletionMock.mockRejectedValue(new Error("Server error"));
    const queryClient = makeQueryClient();
    queryClient.setQueryData(["users", "me"], {
      id: "00000000-0000-0000-0000-000000000000",
      name: "Test Testsson",
      email: "test@test.test",
    });

    renderSettings(queryClient);

    fireEvent.click(screen.getByRole("button", { name: "Delete account" }));

    const input = await screen.findByLabelText("Type your email to confirm");
    fireEvent.change(input, { target: { value: "test@test.test" } });

    fireEvent.click(screen.getByRole("button", { name: "Delete Account" }));

    await waitFor(() => {
      expect(requestAccountDeletionMock).toHaveBeenCalledTimes(1);
    });

    expect(clearSessionMock).not.toHaveBeenCalled();
    expect(navigateMock).not.toHaveBeenCalled();
  });
});
