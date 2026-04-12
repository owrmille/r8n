import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Login from "@/pages/Login";
import { clearAuthSession, createQueryClient } from "@/lib/server-state";

const { loginMock, navigateMock } = vi.hoisted(() => ({
  loginMock: vi.fn(),
  navigateMock: vi.fn(),
}));

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual<typeof import("react-router-dom")>(
    "react-router-dom",
  );

  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock("@/lib/api", () => ({
  authApi: {
    login: loginMock,
  },
}));

function renderLoginPage() {
  const queryClient = createQueryClient({
    defaultOptions: {
      mutations: { retry: false },
      queries: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={["/login"]} future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <Routes>
          <Route path="/login" element={<Login />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("Login page", () => {
  beforeEach(() => {
    loginMock.mockReset();
    navigateMock.mockReset();
    clearAuthSession();
  });

  it("signs in through authApi and redirects to the dashboard", async () => {
    loginMock.mockResolvedValue({
      accessToken: "stub-access-token-123",
      expiresInMilliseconds: 0,
      refreshToken: "stub-refresh-token-123",
    });

    renderLoginPage();

    fireEvent.change(screen.getByLabelText("Login"), {
      target: { value: "test" },
    });
    fireEvent.change(screen.getByLabelText("Password"), {
      target: { value: "1234" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Sign in" }));

    await waitFor(() => {
      expect(loginMock).toHaveBeenCalledWith({
        login: "test",
        password: "1234",
      });
    });

    await waitFor(() => {
      expect(navigateMock).toHaveBeenCalledWith("/", { replace: true });
    });
  });

  it("does not call the login API from the unfinished sign-up flow", async () => {
    renderLoginPage();

    fireEvent.click(screen.getByRole("button", { name: "Create one" }));
    fireEvent.click(screen.getByRole("button", { name: "Create account" }));

    await waitFor(() => {
      expect(loginMock).not.toHaveBeenCalled();
    });
  });
});
