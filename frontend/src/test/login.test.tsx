import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Login from "@/pages/Login";

const { loginMock, navigateMock, setSessionMock } = vi.hoisted(() => ({
  loginMock: vi.fn(),
  navigateMock: vi.fn(),
  setSessionMock: vi.fn(),
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

vi.mock("@/lib/auth/session", () => ({
  setSession: setSessionMock,
}));

function renderLoginPage() {
  const queryClient = new QueryClient({
    defaultOptions: {
      mutations: {
        retry: false,
      },
      queries: {
        retry: false,
      },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={["/login"]}>
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
    setSessionMock.mockReset();
  });

  it("stores the login session in memory before redirecting to the dashboard", async () => {
    loginMock.mockResolvedValue({
      accessToken: "stub-access-token-123",
      expiresInMilliseconds: 60_000,
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
      expect(setSessionMock).toHaveBeenCalledWith({
        accessToken: "stub-access-token-123",
        expiresInMilliseconds: 60_000,
      });
      expect(navigateMock).toHaveBeenCalledWith("/", { replace: true });
    });

    expect(setSessionMock.mock.invocationCallOrder[0]).toBeLessThan(
      navigateMock.mock.invocationCallOrder[0],
    );
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
