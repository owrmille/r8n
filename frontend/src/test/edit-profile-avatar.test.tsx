import { QueryClientProvider } from "@tanstack/react-query";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import type { ReactElement } from "react";
import { MemoryRouter } from "react-router-dom";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const USER_ID = "00000000-0000-0000-0000-000000000000";
const ACCESS_TOKEN = "stub-access-token-123";
const AVATAR_MAX_SIZE_BYTES = 1024;

const { toastMock } = vi.hoisted(() => ({
  toastMock: vi.fn(),
}));

vi.mock("@/hooks/use-toast", () => ({
  toast: toastMock,
}));

let EditProfile: typeof import("@/pages/EditProfile")["default"];
let createQueryClient: typeof import("@/lib/server-state/query-client")["createQueryClient"];
let clearAuthSession: typeof import("@/lib/server-state/auth-store")["clearAuthSession"];
let setAuthSession: typeof import("@/lib/server-state/auth-store")["setAuthSession"];
let fetchMock: ReturnType<typeof vi.fn>;

function createUserResponse() {
  return new Response(
    JSON.stringify({
      id: USER_ID,
      name: "Test Testsson",
    }),
    {
      headers: { "Content-Type": "application/json" },
      status: 200,
    },
  );
}

function createAvatarResponse() {
  return new Response("avatar-bytes", {
    headers: { "Content-Type": "image/png" },
    status: 200,
  });
}

function createEmptyResponse() {
  return new Response(null, { status: 204 });
}

function renderEditProfile() {
  const renderResult = renderWithClient(
    <MemoryRouter>
      <EditProfile />
    </MemoryRouter>,
  );

  const fileInput = renderResult.container.querySelector(
    'input[type="file"]',
  ) as HTMLInputElement | null;

  if (!fileInput) {
    throw new Error("Expected profile image file input to be rendered.");
  }

  return {
    ...renderResult,
    fileInput,
  };
}

describe("EditProfile avatar controls", () => {
  beforeEach(async () => {
    vi.resetModules();

    fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);
    vi.stubEnv("VITE_AVATAR_MAX_SIZE_BYTES", String(AVATAR_MAX_SIZE_BYTES));

    ({ default: EditProfile } = await import("@/pages/EditProfile"));
    ({ createQueryClient } = await import("@/lib/server-state/query-client"));
    ({ clearAuthSession, setAuthSession } = await import("@/lib/server-state/auth-store"));

    setAuthSession({
      accessToken: ACCESS_TOKEN,
      expiresInMilliseconds: 60_000,
    });

    Object.defineProperty(URL, "createObjectURL", {
      configurable: true,
      value: vi.fn(),
    });
    Object.defineProperty(URL, "revokeObjectURL", {
      configurable: true,
      value: vi.fn(),
    });
    vi.spyOn(URL, "createObjectURL").mockReturnValue("blob:avatar-url");
    vi.spyOn(URL, "revokeObjectURL").mockImplementation(() => {});
    toastMock.mockReset();
  });

  afterEach(() => {
    clearAuthSession();
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
    vi.unstubAllEnvs();
  });

  it("uploads a valid selected image file", async () => {
    fetchMock
      .mockResolvedValueOnce(createUserResponse())
      .mockResolvedValueOnce(new Response(null, { status: 404 }))
      .mockResolvedValueOnce(createEmptyResponse())
      .mockResolvedValueOnce(createUserResponse())
      .mockResolvedValueOnce(createAvatarResponse());
    const { fileInput } = renderEditProfile();
    const file = new File(["avatar"], "avatar.png", { type: "image/png" });

    await waitForInitialAvatarFetch();
    fireEvent.change(fileInput, { target: { files: [file] } });

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledWith(
        "/api/users/me/avatar",
        expect.objectContaining({ method: "POST" }),
      );
    });

    const uploadRequest = fetchMock.mock.calls.find(
      ([url, requestInit]) =>
        url === "/api/users/me/avatar" && requestInit.method === "POST",
    )?.[1];
    expect(uploadRequest?.body).toBeInstanceOf(FormData);

    const uploadHeaders = new Headers(uploadRequest?.headers);
    expect(uploadHeaders.get("Authorization")).toBe(`Bearer ${ACCESS_TOKEN}`);
    expect(uploadHeaders.get("Content-Type")).toBeNull();
    await waitFor(() => {
      expect(toastMock).toHaveBeenCalledWith({
        title: "Profile image updated",
        description: "Your new image has been saved.",
      });
    });
  });

  it("rejects unsupported file types before upload", async () => {
    fetchMock
      .mockResolvedValueOnce(createUserResponse())
      .mockResolvedValueOnce(new Response(null, { status: 404 }));
    const { fileInput } = renderEditProfile();
    const file = new File(["not-image"], "avatar.txt", { type: "text/plain" });

    await waitForInitialAvatarFetch();
    fireEvent.change(fileInput, { target: { files: [file] } });

    expect(fetchMock).not.toHaveBeenCalledWith(
      "/api/users/me/avatar",
      expect.objectContaining({ method: "POST" }),
    );
    expect(toastMock).toHaveBeenCalledWith({
      title: "Image not uploaded",
      description: "Please choose a PNG, JPEG, or WebP image.",
    });
  });

  it("rejects images larger than the configured size before upload", async () => {
    fetchMock
      .mockResolvedValueOnce(createUserResponse())
      .mockResolvedValueOnce(new Response(null, { status: 404 }));
    const { fileInput } = renderEditProfile();
    const file = new File(
      [new Uint8Array(AVATAR_MAX_SIZE_BYTES + 1)],
      "avatar.png",
      { type: "image/png" },
    );

    await waitForInitialAvatarFetch();
    fireEvent.change(fileInput, { target: { files: [file] } });

    expect(screen.getByText("PNG, JPEG, or WebP. Max 1KB.")).toBeInTheDocument();
    expect(fetchMock).not.toHaveBeenCalledWith(
      "/api/users/me/avatar",
      expect.objectContaining({ method: "POST" }),
    );
    expect(toastMock).toHaveBeenCalledWith({
      title: "Image not uploaded",
      description: "Profile image must be 1KB or smaller.",
    });
  });

  it("deletes the current profile image", async () => {
    fetchMock
      .mockResolvedValueOnce(createUserResponse())
      .mockResolvedValueOnce(createAvatarResponse())
      .mockResolvedValueOnce(createEmptyResponse())
      .mockResolvedValueOnce(createUserResponse())
      .mockResolvedValueOnce(new Response(null, { status: 404 }));
    renderEditProfile();

    fireEvent.click(await screen.findByRole("button", { name: /remove image/i }));

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledWith(
        "/api/users/me/avatar",
        expect.objectContaining({ method: "DELETE" }),
      );
    });
    const deleteRequest = fetchMock.mock.calls.find(
      ([url, requestInit]) =>
        url === "/api/users/me/avatar" && requestInit.method === "DELETE",
    )?.[1];
    const deleteHeaders = new Headers(deleteRequest?.headers);
    expect(deleteHeaders.get("Authorization")).toBe(`Bearer ${ACCESS_TOKEN}`);
    expect(toastMock).toHaveBeenCalledWith({
      title: "Profile image removed",
      description: "Your profile now shows initials again.",
    });
  });
});

function renderWithClient(ui: ReactElement) {
  const queryClient = createQueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      {ui}
    </QueryClientProvider>,
  );
}

async function waitForInitialAvatarFetch() {
  await waitFor(() => {
    expect(fetchMock).toHaveBeenCalledWith(
      `/api/users/${USER_ID}/avatar`,
      expect.objectContaining({ method: "GET" }),
    );
  });
}
