import { fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import EditProfile from "@/pages/EditProfile";

const {
  deleteAvatarMutateMock,
  toastMock,
  uploadAvatarMutateMock,
  useDeleteMyAvatarMutationMock,
  useMeMock,
  useUploadMyAvatarMutationMock,
  useUserAvatarMock,
} = vi.hoisted(() => ({
  deleteAvatarMutateMock: vi.fn(),
  toastMock: vi.fn(),
  uploadAvatarMutateMock: vi.fn(),
  useDeleteMyAvatarMutationMock: vi.fn(),
  useMeMock: vi.fn(),
  useUploadMyAvatarMutationMock: vi.fn(),
  useUserAvatarMock: vi.fn(),
}));

vi.mock("@/components/UserAvatar", () => ({
  default: ({ name }: { name: string }) => (
    <div data-testid="user-avatar">{name}</div>
  ),
}));

vi.mock("@/hooks/use-toast", () => ({
  toast: toastMock,
}));

vi.mock("@/lib/server-state/hooks/users", () => ({
  useDeleteMyAvatarMutation: useDeleteMyAvatarMutationMock,
  useMe: useMeMock,
  useUploadMyAvatarMutation: useUploadMyAvatarMutationMock,
  useUserAvatar: useUserAvatarMock,
}));

function renderEditProfile() {
  const renderResult = render(
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
  beforeEach(() => {
    deleteAvatarMutateMock.mockReset();
    toastMock.mockReset();
    uploadAvatarMutateMock.mockReset();
    useMeMock.mockReturnValue({
      data: {
        id: "00000000-0000-0000-0000-000000000000",
        name: "Test Testsson",
      },
    });
    useUserAvatarMock.mockReturnValue({ data: null });
    useUploadMyAvatarMutationMock.mockReturnValue({
      isPending: false,
      mutate: uploadAvatarMutateMock,
    });
    useDeleteMyAvatarMutationMock.mockReturnValue({
      isPending: false,
      mutate: deleteAvatarMutateMock,
    });
  });

  it("uploads a valid selected image file", () => {
    const { fileInput } = renderEditProfile();
    const file = new File(["avatar"], "avatar.png", { type: "image/png" });

    fireEvent.change(fileInput, { target: { files: [file] } });

    expect(uploadAvatarMutateMock).toHaveBeenCalledWith({ file });
    expect(toastMock).not.toHaveBeenCalled();
  });

  it("rejects unsupported file types before upload", () => {
    const { fileInput } = renderEditProfile();
    const file = new File(["not-image"], "avatar.txt", { type: "text/plain" });

    fireEvent.change(fileInput, { target: { files: [file] } });

    expect(uploadAvatarMutateMock).not.toHaveBeenCalled();
    expect(toastMock).toHaveBeenCalledWith({
      title: "Image not uploaded",
      description: "Please choose a PNG, JPEG, or WebP image.",
    });
  });

  it("deletes the current profile image", () => {
    useUserAvatarMock.mockReturnValue({
      data: new Blob(["avatar"], { type: "image/png" }),
    });
    renderEditProfile();

    fireEvent.click(screen.getByRole("button", { name: /remove image/i }));

    expect(deleteAvatarMutateMock).toHaveBeenCalledTimes(1);
  });
});
